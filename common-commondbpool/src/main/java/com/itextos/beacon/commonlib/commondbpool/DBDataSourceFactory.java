package com.itextos.beacon.commonlib.commondbpool;

import java.sql.Connection;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;

public class DBDataSourceFactory
{

    private static final Log log = LogFactory.getLog(DBDataSourceFactory.class);

    static
    {
        InitializeConnectionPool.getInstance();
    }

    public static Connection getConnection(
            JndiInfo aDBConID)
            throws Exception
    {
        waitForJndiLoad();
        final Connection con = DataSourceCollection.getInstance().getConnection(aDBConID);
        con.setAutoCommit(true);
        return con;
    }
    
    public static Connection getConnectionFromThin(
            JndiInfo aDBConID)
            throws Exception
    {
        waitForJndiLoad();
        final Connection con = DataSourceCollection.getInstance().getConnection(aDBConID);
        con.setAutoCommit(true);
        return con;
    }

    
    public static Connection getConnectionFromPool(
            JndiInfo aDBConID)
            throws Exception
    {
        waitForJndiLoad();
        final Connection con = DataSourceCollection.getInstance().getConnection(aDBConID);
        con.setAutoCommit(true);
        return con;
    }

    public static Map<ConnectionCount, Integer> getDataSourceStatistics(
            JndiInfo aConnectionID)
    {
        waitForJndiLoad();
        return DataSourceCollection.getInstance().getDataSourceStatistics(aConnectionID);
    }

    public static void closeAllConnectionPool()
    {
        waitForJndiLoad();
        DataSourceCollection.getInstance().closeAllConnectionPool();
    }

    public static boolean isTrackerEnabled()
    {
        waitForJndiLoad();
        return DataSourceCollection.getInstance().isTrackerEnabled();
    }

    private DBDataSourceFactory()
    {}

    private static void waitForJndiLoad()
    {

        while (!InitializeConnectionPool.getInstance().isJndiInfoLoadCompleted())
        {
            if (log.isDebugEnabled())
                log.debug("Waiting for JNDI_INFO load to complete.");

            CommonUtility.sleepForAWhile(1);
        }
    }

}