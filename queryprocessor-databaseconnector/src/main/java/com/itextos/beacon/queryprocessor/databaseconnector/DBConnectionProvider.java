package com.itextos.beacon.queryprocessor.databaseconnector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.BasicDataSourceFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.queryprocessor.commonutils.CommonVariables;

public class DBConnectionProvider
{

    private static final Log                          log        = LogFactory.getLog(DBConnectionProvider.class);
    private static ConnectionPoolSingleton            connPool   = null;
    protected static HashMap<String, BasicDataSource> hmConnPool = new HashMap<>();

    public static Connection getMasterDBConnection()
            throws Exception
    {
        final String masterDBID = CommonVariables.MASTER_DB_JNDI_ID;

        return getDBConnection(masterDBID);
    }

    public static Connection getDBConnection(
            String dbID)
            throws Exception
    {
        if (!hmConnPool.containsKey(dbID))
            createDBConnPool(dbID);
        return hmConnPool.get(dbID).getConnection();
    }

    protected static void setConnPoolObject(
            ConnectionPoolSingleton ConnPoolObj)
    {
        connPool = ConnPoolObj;
    }

    protected static void createDBConnPool(
            String dbID)
            throws Exception
    {
        if (connPool == null)
            connPool = ConnectionPoolSingleton.getInstance();

        if (hmConnPool.containsKey(dbID))
            return;

        log.info(String.format("Creating Connection Pool for JNDI ID: %s", dbID));

        final DatabaseInformationModel DB_CFG = connPool.getDatabaseID(dbID);
        if (DB_CFG == null)
            throw new Exception("JNDI Info not found for the DB ID: " + dbID);

        final String     JDBC_Url                  = DB_CFG.url;

        final String     DB_User                   = DB_CFG.username.trim();
        final String     DB_Pass                   = DB_CFG.password.trim();
        final String     DB_JDBC_Driver            = DB_CFG.driver_class_name.trim();
        final String     DB_CP_Test_SQL            = DB_CFG.validation_query.trim();

        final int        DB_CP_Num_Tests_Per_Evict = DB_CFG.num_tests_per_eviction_run;
        final int        DB_CP_Min_Evict_Idle_Tm   = DB_CFG.min_evictable_idle_time_millis;
        final int        maxThreads                = DB_CFG.max_total;
        final int        DB_CP_Init_Size           = maxThreads;
        final int        DB_CP_Max_Active          = maxThreads * 2;
        final int        DB_CP_Max_Idle            = DB_CP_Max_Active;
        final int        DB_CP_Min_Idle            = maxThreads;

        final Properties DB_CP_Props               = new Properties();

        DB_CP_Props.put("url", JDBC_Url);
        DB_CP_Props.put("driverClassName", DB_JDBC_Driver);
        DB_CP_Props.put("username", DB_User);
        DB_CP_Props.put("password", DB_Pass);
        DB_CP_Props.put("initialSize", DB_CP_Init_Size);
        DB_CP_Props.put("maxActive", DB_CP_Max_Active);
        DB_CP_Props.put("maxIdle", DB_CP_Max_Idle);
        DB_CP_Props.put("minIdle", DB_CP_Min_Idle);
        DB_CP_Props.put("minEvictableIdleTimeMillis", DB_CP_Min_Evict_Idle_Tm);
        DB_CP_Props.put("timeBetweenEvictionRunsMillis", "-1");
        DB_CP_Props.put("numTestsPerEvictionRun", DB_CP_Num_Tests_Per_Evict);
        DB_CP_Props.put("validationQuery", DB_CP_Test_SQL);
        DB_CP_Props.put("defaultAutoCommit", Boolean.FALSE);
        DB_CP_Props.put("testOnBorrow", Boolean.TRUE);

        hmConnPool.put(dbID, BasicDataSourceFactory.createDataSource(DB_CP_Props));
    }

    public static Connection getDriectConnection(
            String DB_TYPE,
            Properties DB_CFG_val)
            throws Exception
    {
        Connection dbConn = null;

        if (DB_TYPE.equals(CommonVariables.MARIA_DB))
        {
            final String MariaDBHost     = DB_CFG_val.getProperty("mysql.host");
            final String MariaDBPort     = DB_CFG_val.getProperty("mysql.port");
            final String MariaDBDatabase = DB_CFG_val.getProperty("mysql.db");
            final String MariaDBUser     = DB_CFG_val.getProperty("mysql.user");
            final String MysqlPassword   = DB_CFG_val.getProperty("mysql.password");

            final String MariaDBJDBCURL  = "jdbc:mariadb://" + MariaDBHost + ":" + MariaDBPort + "/" + MariaDBDatabase;
            dbConn = DriverManager.getConnection(MariaDBJDBCURL, MariaDBUser, MysqlPassword);
        }
        else
            if (DB_TYPE.equals(CommonVariables.PG_DB))
            {
                final String PGDBHost     = DB_CFG_val.getProperty("pgsql.host");
                final String PGDBPort     = DB_CFG_val.getProperty("pgsql.port");
                final String PGDBDatabse  = DB_CFG_val.getProperty("pgsql.db");
                final String PGDBUser     = DB_CFG_val.getProperty("pgsql.user");
                final String PGDBPassword = DB_CFG_val.getProperty("pgsql.password");

                final String PGDBJDBCURL  = "jdbc:postgresql://" + PGDBHost + ":" + PGDBPort + "/" + PGDBDatabse;
                dbConn = DriverManager.getConnection(PGDBJDBCURL, PGDBUser, PGDBPassword);
            }
        return dbConn;
    }

}
