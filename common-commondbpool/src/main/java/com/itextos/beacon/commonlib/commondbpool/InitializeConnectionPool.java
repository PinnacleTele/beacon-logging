package com.itextos.beacon.commonlib.commondbpool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commondbpool.log.DataSourceLog;
import com.itextos.beacon.commonlib.commonpropertyloader.PropertiesPath;
import com.itextos.beacon.commonlib.commonpropertyloader.PropertyLoader;
import com.itextos.beacon.commonlib.constants.Constants;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.utility.CommonUtility;

final class InitializeConnectionPool
{

    private static final Log    log                    = LogFactory.getLog(InitializeConnectionPool.class);
    private static final String KEY_DB_TRACKER_ENABLED = "db.tracker.enabled";
    private static final String SYSTEM_SCHEMA          = "sysconfig";

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final InitializeConnectionPool INSTANCE = new InitializeConnectionPool();

    }

    static InitializeConnectionPool getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private boolean                      isStarted   = false;
    private boolean                      isStarting  = false;
    private final Map<Integer, JndiInfo> jndiInfoMap = new HashMap<>();

    private InitializeConnectionPool() 
    {
        final long startTime = System.currentTimeMillis();
        if (log.isDebugEnabled())
            log.debug("Initialize Connection pool started...");

        try {
			initialize();
		} catch (ItextosRuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        final long timeTaken = System.currentTimeMillis() - startTime;
        if (log.isDebugEnabled())
            log.debug("Completed Initializing Connection pool. Time taken '" + timeTaken + "'");
    }

    void initialize() throws ItextosRuntimeException
    {

        if (isStarted)
        {
            System.err.println("Already initialized. Make sure you are not calling InitializeConnectionPool.getInstance().initialize() multiple times");
            log.warn(new Exception("Already initialized. Make sure you are not calling InitializeConnectionPool.getInstance().initialize() multiple times"));
            return;
        }

        if (isStarting)
        {

            while (!isStarted)
            {
                log.warn("Datasource starting  is in progress.");

                try
                {
                    Thread.sleep(100);
                }
                catch (final InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
            return;
        }

        isStarting = true;

        if (log.isInfoEnabled())
            log.info("Initializing the connection Pools");

        final Properties props          = readProperties();
        final String     trackerEnabled = props.getProperty(KEY_DB_TRACKER_ENABLED);

        DataSourceCollection.getInstance().setTrackerEnabled(CommonUtility.isEnabled(trackerEnabled));

        createCommonPool(props);

        addJndiInfo(JndiInfo.SYSTEM_DB);

        populateOtherConfigInfo();

        isStarted = true;
    }

    private JndiInfo addJndiInfo(
            JndiInfo aJndiInfo)
    {
        if (aJndiInfo != null)
            jndiInfoMap.put(aJndiInfo.getId(), aJndiInfo);
        return aJndiInfo;
    }

    JndiInfo getJndiInfo(
            int aId)
    {
        final JndiInfo returnValue = jndiInfoMap.get(aId);

        if (returnValue == null)
        {
            log.fatal("Unable to find the Jndi info for the id '" + aId + "'");
            printAllJNDIInfoDetails(true);
        }
        return returnValue;
    }

    void printAllJNDIInfoDetails(
            boolean aPrintInFatal)
    {
        final boolean shallPrint = aPrintInFatal || log.isInfoEnabled();

        if (shallPrint)
        {
            final StringBuilder sb = new StringBuilder();
            sb.append("Available JNDI Info are **************** ").append(Constants.PLATFORM_NEW_LINE_CHAR);

            for (final Entry<Integer, JndiInfo> entry : jndiInfoMap.entrySet())
                sb.append("'").append(entry.getKey()).append("' = '").append(entry.getValue()).append("'").append(Constants.PLATFORM_NEW_LINE_CHAR);

            log.fatal(sb);
        }
    }

    boolean isJndiInfoLoadCompleted()
    {
        return isStarted;
    }

    private static Properties readProperties() throws ItextosRuntimeException
    {

        try
        {
            final PropertiesConfiguration pc = PropertyLoader.getInstance().getPropertiesConfiguration(PropertiesPath.COMMON_DATABASE_PROPERTIES, true);

            if (pc != null)
            {
                final Properties       props   = new Properties();
                final Iterator<String> keys    = pc.getKeys();
                String                 currKey = null;

                while (keys.hasNext())
                {
                    currKey = keys.next();
                    props.setProperty(currKey, pc.getString(currKey));
                }

                return props;
            }
            throw new ItextosRuntimeException("Unable to load the common db properties");
        }
        catch (final Exception exp)
        {
            log.error("Problem loading property file...", exp);
            throw new ItextosRuntimeException("Unable to load the common db properties");
        }
    }

    private static void createCommonPool(
            Properties properties)
    {
        final DataSourceConfig config = new DataSourceConfig(JndiInfo.SYSTEM_DB, properties);
        DataSourceCollection.getInstance().createDataSource(config.getDbConID(), config);

        if (log.isInfoEnabled())
            log.info("Common datasource configuration is done");
    }

    private void populateOtherConfigInfo()
    {
        final String sql = "select * from " + SYSTEM_SCHEMA + ".jndi_info";

        if (log.isInfoEnabled())
            log.info("Creating data source for the Schema '" + JndiInfo.SYSTEM_DB + "'");

        try (
                Connection con = getConnection(JndiInfo.SYSTEM_DB);
                PreparedStatement pstmt = con.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery();)
        {
            if (log.isInfoEnabled())
                log.info("Query to select records '" + sql + "'");

            int totalCount   = 0;
            int successCount = 0;

            while (rs.next())
            {
            	
            	try {
                totalCount++;
                final JndiInfo         jndiInfoId = new JndiInfo(rs.getInt("id"), rs.getString("description"));
                final DataSourceConfig config     = new DataSourceConfig(jndiInfoId);

                config.setUrl(rs.getString("url"));
                config.setDriverClassName(rs.getString("driver_class_name"));
                config.setUsername(rs.getString("username"));
                config.setPassword(DataSourceConfig.getDecryptedPassword(rs.getString("url"), rs.getString("username"), rs.getString("password")));
                config.setInitialSize(rs.getString("initial_size"));
                config.setMaxActive(rs.getString("max_active"));
                config.setMaxTotal(rs.getString("max_total"));
                config.setMaxIdle(rs.getString("max_idle"));
                config.setMinIdle(rs.getString("min_idle"));
                config.setMaxWait(rs.getString("max_wait"));
                config.setMaxWaitMillis(rs.getString("max_wait_millis"));
                config.setTimeBetweenEvictionRunsMillis(rs.getString("time_between_eviction_runs_millis"));
                config.setNumTestsPerEvictionRun(rs.getString("num_tests_per_eviction_run"));
                config.setMinEvictableIdleTimeMillis(rs.getString("min_evictable_idle_time_millis"));
                config.setValidationQuery(rs.getString("validation_query"));
                config.setTestOnBorrow(rs.getString("test_on_borrow"));
                config.setRemoveAbandoned(rs.getString("remove_abandoned"));
                config.setRemoveAbandonedOnMaintenance(rs.getString("remove_abandoned_on_maintenance"));
                config.setRemoveAbandonedOnBorrow(rs.getString("remove_abandoned_on_borrow"));
                config.setRemoveAbandonedTimeout(rs.getString("remove_abandoned_timeout_seconds"));
                config.setLogAbandoned(rs.getString("log_abandoned"));
                config.setAbandonedUsageTracking(rs.getString("abandoned_usage_tracking"));
                config.setConnectionProperties(rs.getString("other_connection_properties"));

                try
                {
                    DataSourceCollection.getInstance().createDataSource(config.getDbConID(), config);
                    DataSourceLog.log(" Select from "+ SYSTEM_SCHEMA + ".jndi_info"+  "  jndi info : "+ jndiInfoId);
                    addJndiInfo(jndiInfoId);
                    successCount++;
                }
                catch (final Exception e)
                {
                    log.error("Problem while initializing the " + "datasource for connection ID : '" + config.getDbConID() + "'", e);
                }
            	}catch(Exception ignore) {
            		
            	}
            }

            if (log.isInfoEnabled())
                log.info("Total datasource configuration read from the database : " + totalCount + ". Successfully added : " + successCount);

            if (log.isDebugEnabled())
                log.debug("Connection pools will be created only while getting first connection for the above added datasource configuration.");
        }
        catch (final Exception e)
        {
            log.error("Unable to fetch the connection configuration from the database.", e);
        }
    }

    private static Connection getConnection(
            JndiInfo aDBConID)
            throws Exception
    {
        final Connection con = DataSourceCollection.getInstance().getConnection(aDBConID);
        con.setAutoCommit(true);
        return con;
    }

}