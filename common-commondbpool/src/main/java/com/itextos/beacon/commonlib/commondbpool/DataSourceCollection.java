package com.itextos.beacon.commonlib.commondbpool;

import java.sql.Connection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commondbpool.log.DataSourceLog;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;

class DataSourceCollection
{

    private static Log log = LogFactory.getLog(DataSourceCollection.class);

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final DataSourceCollection INSTANCE = new DataSourceCollection();

    }

    static DataSourceCollection getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final Map<JndiInfo, DBDataSource> dataSourceHolder = new ConcurrentHashMap<>();
    private boolean                           trackerEnabled   = false;

    private DataSourceCollection()
    {}

    void addDataSource(
            JndiInfo aJndiInfo,
            DBDataSource aDataSource)
    {

        if (aJndiInfo == null)
        {
            log.error("Jndi Info cannot be null.");
            return;
        }

        if (aDataSource == null)
        {
            log.error("Datasource cannot be null.");
            return;
        }

        dataSourceHolder.put(aJndiInfo, aDataSource);

        if (log.isDebugEnabled())
            log.debug("ConnectionPoolFactory Adding configuration for " + aJndiInfo);
        
        DataSourceLog.log("ConnectionPoolFactory Adding configuration for " + aJndiInfo);
    }

    Connection getConnection(
            JndiInfo aJndiInfo)
            throws Exception
    {
        if (aJndiInfo == null)
            throw new ItextosException("JndiInfo cannot be null.");

        final DBDataSource temp = dataSourceHolder.get(aJndiInfo);

        if (temp == null)
            throw new ItextosException("Configuration is not initialised. ConnectionID : " + aJndiInfo);

        return temp.getConnection();
    }

    void setTrackerEnabled(
            boolean aTrackerEnabled)
    {
        trackerEnabled = aTrackerEnabled;
    }

    boolean isTrackerEnabled()
    {
        return trackerEnabled;
    }

    Map<ConnectionCount, Integer> getDataSourceStatistics(
            JndiInfo aConnectionID)
    {
        final DBDataSource temp = dataSourceHolder.get(aConnectionID);

        if (temp == null)
            return null;

        final Map<ConnectionCount, Integer> returnValue = new EnumMap<>(ConnectionCount.class);
        returnValue.put(ConnectionCount.INITIAL_CONNECTION_SIZE, temp.getInitialSize());
        returnValue.put(ConnectionCount.MAX_IDLE_CONNECTION, temp.getMaxIdle());
        returnValue.put(ConnectionCount.MAX_TOTAL_CONNECTION, temp.getMaxTotal());
        returnValue.put(ConnectionCount.MIN_IDLE_CONNECTION, temp.getMinIdle());
        returnValue.put(ConnectionCount.NUM_ACTIVE_CONNECTION, temp.getNumActive());
        returnValue.put(ConnectionCount.NUM_IDLE_CONNECTION, temp.getNumIdle());
        returnValue.put(ConnectionCount.MAX_OPEN_PREPARED_STATEMENTS, temp.getMaxOpenPreparedStatements());
        return returnValue;
    }

    void createDataSource(
            JndiInfo aJndiInfo,
            DataSourceConfig aDatabaseConfig)
    {
        final DBDataSource dataSource = new DBDataSource(aJndiInfo, aDatabaseConfig);
        addDataSource(aJndiInfo, dataSource);
    }

    void closeAllConnectionPool()
    {

        for (final Entry<JndiInfo, DBDataSource> entry : dataSourceHolder.entrySet())
        {
            final JndiInfo temp = entry.getKey();

            if (log.isInfoEnabled())
                log.info("Closing the DataSource for the Connection ID: " + temp.toString());

            try
            {
                final DBDataSource tempDataSource = entry.getValue();

                if (tempDataSource.isDataSourceCreated())
                {
                    tempDataSource.closeDataSource();

                    if (log.isDebugEnabled())
                        log.debug("DataSource closed for the Connection ID: " + temp.toString());
                }
                else
                    if (log.isDebugEnabled())
                        log.debug("DataSource not created for the Connection ID: " + temp.toString() + ". No need to close it.");
            }
            catch (final Exception e)
            {
                log.error("IGNORE: Exception while closing the DataSource: '" + temp.toString() + "'", e);
            }
        }
    }

}