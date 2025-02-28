package com.itextos.beacon.platform.prepaiddata.kannelstatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.DatabaseSchema;
import com.itextos.beacon.commonlib.commondbpool.JndiIdProperties;
import com.itextos.beacon.commonlib.commondbpool.JndiInfoHolder;
import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.redisconnectionprovider.RedisConnectionProvider;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

public class KannelInfoLoader
        implements
        ITimedProcess
{

    private static final Log    log                = LogFactory.getLog(KannelInfoLoader.class);
    private static final String SELECT_ROUTES_NAME = "select cm.carrier_name, crm.route_id " //
            + " from " //
            + " carrier_handover.carrier_master cm, " //
            + " carrier_handover.carrier_route_map crm " //
            + " where cm.carrier_id = crm.carrier_id";

    private static final String REDIS_KANNEL_KEY   = "kannel:available:*";

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final KannelInfoLoader INSTANCE = new KannelInfoLoader();

    }

    public static KannelInfoLoader getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private TimedProcessor                   mTimedProcessor = null;
    private boolean                          mCanContinue    = true;

    private Map<String, List<String>>        mRouteInfo      = new HashMap<>();
    private Map<String, Map<String, String>> mKannelInfo     = new HashMap<>();

    private KannelInfoLoader()
    {
        if (log.isDebugEnabled())
            log.debug("Starting the Kannel Info Loader : '");

        mTimedProcessor = new TimedProcessor("KannelInfoLoader", this, TimerIntervalConstant.DATA_REFRESHER_RELOAD_INTERVAL);
          
        ExecutorSheduler.getInstance().addTask(mTimedProcessor, "KannelInfoLoader");
        
        if (log.isDebugEnabled())
            log.debug("Kannel Info Loader started '");
    }

    @Override
    public boolean processNow()
    {
        if (log.isDebugEnabled())
            log.debug("KannelInfo Process Start");

        try
        {
            loadDataFromDb();
        }
        catch (final Exception e)
        {
            log.error("Ignorable exception. " + e.getMessage());
            return true;
        }

        try
        {
            getRouteInfoFromRedis();
        }
        catch (final Exception e)
        {
            log.error("Ignorable exception. " + e.getMessage());
            return true;
        }
        return false;
    }

    private void loadDataFromDb()
    {
        final Map<String, List<String>> lOperatorRouteMap = new HashMap<>();

        try (
                Connection lDBConnection = DBDataSourceFactory.getConnection(JndiInfoHolder.getJndiInfo(JndiIdProperties.getInstance().getJndiProperty(DatabaseSchema.CARRIER_HANDOVER.getKey())));
                PreparedStatement lPreparedStatement = lDBConnection.prepareStatement(SELECT_ROUTES_NAME);
                ResultSet lResultSet = lPreparedStatement.executeQuery();)
        {

            while (lResultSet.next())
            {
                final String       lOperator = lResultSet.getString("carrier_name");
                final String       lRouteIds = lResultSet.getString("route_id");
                final List<String> lList     = lOperatorRouteMap.computeIfAbsent(lOperator, k -> new ArrayList<>());
                lList.add(lRouteIds);
            }

            // if (log.isDebugEnabled())
            // log.debug("Operator and Route Data from DB is " + lOperatorRouteMap);

            if (!lOperatorRouteMap.isEmpty())
                mRouteInfo = lOperatorRouteMap;
        }
        catch (final Exception e)
        {
            log.error("Exception while get the Operators ", e);
        }
    }

    private void getRouteInfoFromRedis()
    {
        final Map<String, Map<String, String>> lRouteInfoMap = new HashMap<>();

        try (
                final Jedis jedis = RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, Component.KANNEL_REDIS, 1);
                Pipeline pipe = jedis.pipelined())
        {
            final Set<String>                                lHgetAll  = jedis.keys(REDIS_KANNEL_KEY);

            // if (log.isDebugEnabled())
            // log.debug("Kannel Info Keys Available in Redis are " + lHgetAll);

            final Map<String, Response<Map<String, String>>> redisData = new HashMap<>();

            for (final String key : lHgetAll)
            {
                // if (log.isDebugEnabled())
                // log.debug("Looking the Route Info for :" + key);

                final Response<Map<String, String>> routeDataResponse = pipe.hgetAll(key);
                redisData.put(key, routeDataResponse);
            }
            pipe.sync();

            for (final Entry<String, Response<Map<String, String>>> entry : redisData.entrySet())
            {
                final String                        key               = entry.getKey();
                final Response<Map<String, String>> routeDataResponse = entry.getValue();

                final Map<String, String>           routeData         = routeDataResponse.get();
                final String[]                      route             = key.split(":");

                if (route.length == 3)
                    lRouteInfoMap.put(route[2].toUpperCase(), routeData);
            }
            // if (log.isDebugEnabled())
            // log.debug("Number of Routes Data from Kannel Redis is : '" +
            // lRouteInfoMap.size() + "' Data are :" + lRouteInfoMap);

            if (!lRouteInfoMap.isEmpty())
                mKannelInfo = lRouteInfoMap;
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the data from Redis.", e);
        }
    }

    public Map<String, String> getKannelStatus(
            String aRouteId)
    {
        return mKannelInfo.computeIfAbsent(aRouteId, k -> new HashMap<>());
    }

    @Override
    public boolean canContinue()
    {
        return mCanContinue;
    }

    @Override
    public void stopMe()
    {
        mCanContinue = false;
        stopReaper();
    }

    private void stopReaper()
    {
    	
        if (mTimedProcessor != null)
            mTimedProcessor.stopReaper();
    	
    }

    public Set<String> getAllOperators()
    {
        return new TreeSet<>(mRouteInfo.keySet());
    }

    public List<String> getRoutesForOperator(
            String aOperator)
    {
        final List<String> temp = mRouteInfo.computeIfAbsent(aOperator, k -> new ArrayList<>());
        if (!temp.isEmpty())
            Collections.sort(temp);

        return temp;
    }

}