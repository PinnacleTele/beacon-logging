package com.itextos.beacon.inmemory.routeinfo.cache;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.DatabaseSchema;
import com.itextos.beacon.commonlib.commondbpool.JndiInfoHolder;
import com.itextos.beacon.commonlib.constants.Constants;
import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;
import com.itextos.beacon.inmemory.routeinfo.util.RouteUtil;

public class LoadOnnetTableInfo
        implements
        ITimedProcess
{

    private static Log           log                 = LogFactory.getLog(LoadOnnetTableInfo.class);

    private boolean              mCanContinue        = true;

    Map<String, String>          mOnneTableRouteInfo = new HashMap<>();
    private  TimedProcessor mTimedProcessor;

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final LoadOnnetTableInfo INSTANCE = new LoadOnnetTableInfo();

    }

    public static LoadOnnetTableInfo getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private LoadOnnetTableInfo()
    {

        try
        {
        	
            mTimedProcessor = new TimedProcessor("TimerThread-LoadOnnetTableInfo", this, TimerIntervalConstant.ONNET_TABLE_INFO_REFRESH);
        
            ExecutorSheduler.getInstance().addTask(mTimedProcessor, "LoadOnnetTableInfo");
            }
        catch (final Exception e)
        {
            final String s = "Exception while loading Onnet table information from DB";
            log.error(s, e);
       //     throw new ItextosRuntimeException(s, e);
        }
    }

    public Map<String, String> getOnnetTableRouteInfo()
    {
        return mOnneTableRouteInfo;
    }

    private void loadOnnetTableInfo()
    {
        Connection                lSQLConn            = null;
        Statement                 lStatement          = null;
        ResultSet                 lResultSet          = null;
        final Map<String, String> lOnneTableRouteInfo = new HashMap<>();

        try
        {
            lSQLConn   = DBDataSourceFactory.getConnectionFromThin(JndiInfoHolder.getInstance().getJndiInfoUsingName(DatabaseSchema.CARRIER_HANDOVER.getKey()));
            lStatement = lSQLConn.createStatement();
            // select * from operator_onnet_table_mapping
            lResultSet = lStatement.executeQuery("select * from carrier_onnet_table_map");

            while (lResultSet.next())
            {
                final String lRouteId   = CommonUtility.nullCheck(lResultSet.getString("route_id"), true);
                final String lTableName = CommonUtility.nullCheck(lResultSet.getString("table_name"), true);

                if (!lRouteId.isEmpty() && !lTableName.isEmpty())
                {
                    final Map<String, String> tempMap = getOnnetTableDetails(lSQLConn, lRouteId.toUpperCase(), lTableName);
                    if (tempMap != null)
                        lOnneTableRouteInfo.putAll(tempMap);
                }
            }

            mOnneTableRouteInfo = lOnneTableRouteInfo;
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            CommonUtility.closeResultSet(lResultSet);
            CommonUtility.closeStatement(lStatement);
            CommonUtility.closeConnection(lSQLConn);
        }
    }

    private static Map<String, String> getOnnetTableDetails(
            Connection aSQLConn,
            String aRouteId,
            String aOnnetTable)
            throws Exception
    {
        Statement                 lStatement                = null;
        ResultSet                 lResultSet                = null;
        final Map<String, String> lTempOnnetCustomRouteInfo = new HashMap<>();

        try
        {
            // Select * from {0}
            lStatement = aSQLConn.createStatement();
            lResultSet = lStatement.executeQuery(MessageFormat.format("Select * from {0}", aOnnetTable));
            final String lAccRouteType = aRouteId;

            while (lResultSet.next())
            {
                final String lClientId = Constants.NULL_STRING;
                String       lCarrier  = CommonUtility.nullCheck(lResultSet.getString("carrier"), true);
                lCarrier = lCarrier.isBlank() ? Constants.NULL_STRING : lCarrier.toLowerCase();
                String lCircle = CommonUtility.nullCheck(lResultSet.getString("circle"), true);
                lCircle = lCircle.isBlank() ? Constants.NULL_STRING : lCircle.toLowerCase();
                final String lTxnRoute      = lResultSet.getString("txn_route_id");
                final String lPromoRoute    = lResultSet.getString("promo_route_id");

                final String lTransRouteKey = CommonUtility.combine(lClientId, lCarrier, lCircle, lAccRouteType, MessageType.TRANSACTIONAL.getKey());
                final String lPromoRouteKey = CommonUtility.combine(lClientId, lCarrier, lCircle, lAccRouteType, MessageType.PROMOTIONAL.getKey());

                if (lTxnRoute != null)
                    if (StringUtils.isNumeric(lTxnRoute))
                    {
                        if (RouteUtil.isRouteGroupAvailable(lTxnRoute + "1"))
                            lTempOnnetCustomRouteInfo.put(lTransRouteKey, lTxnRoute);
                    }
                    else
                        if (StringUtils.isAlphanumeric(lTxnRoute) && RouteUtil.isTXNRoute(lTxnRoute))
                            lTempOnnetCustomRouteInfo.put(lTransRouteKey, lTxnRoute);

                if (lPromoRoute != null)
                    if (StringUtils.isNumeric(lPromoRoute))
                    {
                        if (RouteUtil.isRouteGroupAvailable(lPromoRoute + "0"))
                            lTempOnnetCustomRouteInfo.put(lPromoRouteKey, lPromoRoute);
                    }
                    else
                        if (StringUtils.isAlphanumeric(lPromoRoute) && RouteUtil.isPromoRoute(lPromoRoute))
                            lTempOnnetCustomRouteInfo.put(lPromoRouteKey, lPromoRoute);
            }
        }
        catch (final Exception e)
        {
            e.printStackTrace();
            throw e;
        }
        finally
        {
            CommonUtility.closeResultSet(lResultSet);
            CommonUtility.closeStatement(lStatement);
        }
        return lTempOnnetCustomRouteInfo;
    }

    @Override
    public boolean canContinue()
    {
        return mCanContinue;
    }

    @Override
    public boolean processNow()
    {

        try
        {
            loadOnnetTableInfo();
        }
        catch (final Exception e)
        {
            log.error("IGNORABLE Exception while reloading Onnet Table information from DB.", e);
        }
        return false;
    }

    @Override
    public void stopMe()
    {
        mCanContinue = false;

       
        if (mTimedProcessor != null)
            mTimedProcessor.stopReaper();
     
        
        
    }

}
