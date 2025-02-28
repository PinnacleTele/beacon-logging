package com.itextos.beacon.commonlib.redisconnectionprovider.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.JndiInfo;
import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.errorlog.ErrorLog;

public class RedisConfigLoader
{

    private static final Log    log                       = LogFactory.getLog(RedisConfigLoader.class);

    private static final int    COL_INDEX_REDIS_ID        = 1;
    private static final int    COL_INDEX_IP              = 2;
    private static final int    COL_INDEX_PORT            = 3;
    private static final int    COL_INDEX_PASSWORD        = 4;
    private static final int    COL_INDEX_DB              = 5;
    private static final int    COL_INDEX_CON_TIMEOUT_SEC = 6;
    private static final int    COL_INDEX_READ_TIMOUT_SEC = 7;
    private static final int    COL_INDEX_MAX_WAIT_SEC    = 8;
    private static final int    COL_INDEX_CLUSTER_NAME    = 9;
    private static final int    COL_INDEX_COMPONENT_NAME  = 10;
    private static final int    COL_INDEX_REDIS_MAP_ID    = 11;
    private static final int    COL_INDEX_SEQ_NO          = 12;
    private static final int    COL_INDEX_MAX_POOL_SIZE   = 13;
    private static final int    COL_INDEX_MAX_IDLE_COUNT  = 14;
    private static final int    COL_INDEX_MIN_IDLE_COUNT  = 15;
    private static final int    COL_INDEX_DEBUG_ENABLED   = 16;

    private static final String REDIS_SQL                 = "select" //
            + " rc.redis_id," + " ip," + " port," //
            + " password," + " db," + " con_timeout_sec," //
            + " read_timout_sec," + " max_wait_sec," + " ct.cluster_name," //
            + " c.component_name," + " ccrmm.datasource_map_id," + " ccrmd.seq_no," //
            + " ccrmd.max_pool_size," + " ccrmd.max_idle_count," + " ccrmd.min_idle_count," + " ccrmd.debug_enabled" //
            + " from" //
            + " redis_config rc," //
            + " cluster_type ct," //
            + " component c," //
            + " cluster_component_datasource_map_master ccrmm," //
            + " cluster_component_datasource_map_detail ccrmd" //
            + " where" //
            + " ccrmd.datasource_type = 'redis'" + " and ccrmd.datasource_map_id = ccrmm.datasource_map_id" + " and ccrmm.cluster_name = ct.cluster_name" //
            + " and c.component_name = ccrmm.component_name" //
            + " and rc.redis_id = ccrmd.datasource_id";

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final RedisConfigLoader INSTANCE = new RedisConfigLoader();

    }

    public static RedisConfigLoader getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private Map<ClusterType, Map<Component, Map<Integer, RedisConfig>>> clusterRedisTypeRedisConfigMap;

    private RedisConfigLoader()
    {
        loadDBData();
    }

    private void loadDBData()
    {

        Connection con = null;
        PreparedStatement pstmt =null;
        ResultSet rs =null;
        try{
        	  con = DBDataSourceFactory.getConnectionFromThin(JndiInfo.CONFIGURARION_DB);
              pstmt = con.prepareStatement(REDIS_SQL);
              rs = pstmt.executeQuery();
            clusterRedisTypeRedisConfigMap = getRedisConfigMap(rs);
        }
        catch (final Exception e)
        {
            log.error("", e);
        }finally {
        	
        	 CommonUtility.closeResultSet(rs);
             CommonUtility.closeStatement(pstmt);
             CommonUtility.closeConnection(con);
        }
    }

    public Map<Integer, RedisConfig> getRedisCongfiguration(
            ClusterType aClusterType,
            Component aRedisType)
    {
        final Map<Component, Map<Integer, RedisConfig>> redisTypeConfigMap = clusterRedisTypeRedisConfigMap.get(aClusterType);

        if (redisTypeConfigMap == null)
        {
            log.error("Redis configuration is not available for the cluster '" + aClusterType + "'");
            ErrorLog.log("Redis configuration is not available for the cluster '" + aClusterType + "'");
            return null;
        }

        final Map<Integer, RedisConfig> redisConfig = redisTypeConfigMap.get(aRedisType);

        if (redisConfig == null)
        {
            log.error("Redis configuration is not available for the Redis Type '" + aRedisType + "'");
            ErrorLog.log("Redis configuration is not available for the Redis Type '" + aRedisType + "'");

            return null;
        }
        return redisConfig;
    }

    private static Map<ClusterType, Map<Component, Map<Integer, RedisConfig>>> getRedisConfigMap(
            ResultSet aRs)
            throws SQLException, ItextosRuntimeException
    {
        final Map<ClusterType, Map<Component, Map<Integer, RedisConfig>>> tempClusterRedisTypeRedisConfigMap = new EnumMap<>(ClusterType.class);
        final String                                                      invalidMessage                     = "Invalid %s for Cluster '%s' Component '%s' Seq No '%s' RID '%s' IP '%s'";

        while (aRs.next())
        {
            final String      cluster        = CommonUtility.nullCheck(aRs.getString(COL_INDEX_CLUSTER_NAME), true);
            final String      component      = CommonUtility.nullCheck(aRs.getString(COL_INDEX_COMPONENT_NAME), true);
            final int         seqNo          = aRs.getInt(COL_INDEX_SEQ_NO);
            final String      rid            = CommonUtility.nullCheck(aRs.getString(COL_INDEX_REDIS_ID), true);
            final String      ip             = CommonUtility.nullCheck(aRs.getString(COL_INDEX_IP), true);
            final int         port           = aRs.getInt(COL_INDEX_PORT);
            final String      password       = CommonUtility.nullCheck(aRs.getString(COL_INDEX_PASSWORD), true);
            final int         db             = aRs.getInt(COL_INDEX_DB);
            final int         conTimeout     = aRs.getInt(COL_INDEX_CON_TIMEOUT_SEC);
            final int         readTimeout    = aRs.getInt(COL_INDEX_READ_TIMOUT_SEC);
            final int         maxWaitSec     = aRs.getInt(COL_INDEX_MAX_WAIT_SEC);
            final int         maxPoolSize    = aRs.getInt(COL_INDEX_MAX_POOL_SIZE);
            final int         maxIdleCount   = aRs.getInt(COL_INDEX_MAX_IDLE_COUNT);
            final int         minIdleCount   = aRs.getInt(COL_INDEX_MIN_IDLE_COUNT);
            final int         redisMapId     = aRs.getInt(COL_INDEX_REDIS_MAP_ID);
            final boolean     isDebugEnabled = CommonUtility.isEnabled(CommonUtility.nullCheck(aRs.getString(COL_INDEX_DEBUG_ENABLED), true));

            final ClusterType clusterType    = ClusterType.getCluster(cluster);

            if (clusterType == null)
            {
                log.error(String.format(invalidMessage, "Cluster", cluster, component, Integer.toString(seqNo), rid, ip));
                continue;
            }
            final Component componentType = Component.getComponent(component);

            if (componentType == null)
            {
                log.error(String.format(invalidMessage, "Component", cluster, component, Integer.toString(seqNo), rid, ip));
                continue;
            }

            if (redisMapId <= 0)
            {
                log.error(String.format(invalidMessage, "Redis Map Id", cluster, component, Integer.toString(seqNo), rid, ip));
                continue;
            }

            if (seqNo <= 0)
            {
                log.error(String.format(invalidMessage, "Sequence No", cluster, component, Integer.toString(seqNo), rid, ip));
                continue;
            }

            final Map<Component, Map<Integer, RedisConfig>> redisTypeConfigMap = tempClusterRedisTypeRedisConfigMap.computeIfAbsent(clusterType, k -> new EnumMap<>(Component.class));
            final Map<Integer, RedisConfig>                 redisConfigMap     = redisTypeConfigMap.computeIfAbsent(componentType, k -> new HashMap<>());
            final RedisConfig                               redisConfig        = new RedisConfig(componentType, redisMapId, seqNo, rid, ip, port, password, db, readTimeout, conTimeout, maxWaitSec,
                    maxPoolSize, maxIdleCount, minIdleCount, isDebugEnabled);
            redisConfig.isValidConfiig();
            redisConfigMap.put(seqNo, redisConfig);
        }
        return tempClusterRedisTypeRedisConfigMap;
    }

}
