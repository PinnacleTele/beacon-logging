package com.itextos.beacon.queryprocessor.databaseconnector;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.queryprocessor.commonutils.CommonVariables;
import com.itextos.beacon.queryprocessor.commonutils.Utility;

/*
 * This class is used to store all connection objects used by the
 * queue processor
 */
public class ConnectionPoolSingleton
{

    private static final Log                                log                        = LogFactory
            .getLog(ConnectionPoolSingleton.class);
    // static variable s of type Singleton
    private static ConnectionPoolSingleton                  ConnPoolObj                = null;

    private final HashMap<String, DatabaseInformationModel> databaseID                 = new HashMap<>();

    public HashMap<String, String>                          hostID                     = new HashMap<>();

    public String                                           defaultMDBCliJNDI_ID       = null;
    public String                                           defaultPGCliJNDI_ID        = null;
    public Set<String>                                      hsJNDICliIdMap             = new HashSet<>();
    public HashMap<String, List<String>>                    hmMDBCliJNDI               = new HashMap<>();
    public HashMap<String, List<String>>                    hmPGCliJNDI                = new HashMap<>();

    public HashMap<String, String>                          hmCliUserMap               = new HashMap<>();

    public HashMap<String, String>                          hmMapSubPG_MYSQL           = new HashMap<>();
    public HashMap<String, String>                          hmMapDelPG_MYSQL           = new HashMap<>();
    public HashMap<String, String>                          hmMapCol_DataType          = new HashMap<>();

    public HashMap<String, String>                          hmPG_MDB_Col_Map           = new HashMap<>();

    public HashMap<String, String>                          hmLog_DL_Col_Map           = new HashMap<>();
    public HashMap<String, String>                          hmLog_DL_Col_DataType      = new HashMap<>();

    public HashMap<String, String>                          hmFMSG_Log_DL_Col_Map      = new HashMap<>();
    public HashMap<String, String>                          hmFMSG_Log_DL_Col_DataType = new HashMap<>();

    public Set<String>                                      setSubCols                 = new HashSet<>();
    public Set<String>                                      setDelCols                 = new HashSet<>();

    public HashMap<String, String>                          hmErrDesc                  = new HashMap<>();
    public HashMap<String, String>                          hmErrStatusFlag            = new HashMap<>();

    public boolean                                          isDBMonthSuffix            = false;
    public String                                           CfgMDBBillingJNDI          = null;
    public String                                           CfgPGDBBillingJNDI         = null;

    // public HashMap<String, String> hmCliJNDI_ID_PG = new HashMap<>();

    // public HashMap<String, Connection> dbConnections = new HashMap<>();

    private ConnectionPoolSingleton()
    {}

    public static ConnectionPoolSingleton createInstance(
            Properties MASTER_DB_CFG)
            throws Exception
    {

        if (ConnPoolObj == null)
        {
            log.info("Creating Connection Pool Singleton Object");

            final Connection conn = DBConnectionProvider.getDriectConnection(CommonVariables.MARIA_DB, MASTER_DB_CFG);
            ConnPoolObj = new ConnectionPoolSingleton();

            final String dbMonthSuffix = Utility.nullCheck(MASTER_DB_CFG.getProperty("db_month_suffix"), true)
                    .toLowerCase();
            if (dbMonthSuffix.equals("true"))
                ConnPoolObj.isDBMonthSuffix = true;

            if (MASTER_DB_CFG.containsKey(CommonVariables.CFG_MARIA_DB_BILLNIG_JNDI))
                ConnPoolObj.CfgMDBBillingJNDI = MASTER_DB_CFG.getProperty(CommonVariables.CFG_MARIA_DB_BILLNIG_JNDI);

            if (MASTER_DB_CFG.containsKey(CommonVariables.CFG_PG_DB_BILLNIG_JNDI))
                ConnPoolObj.CfgPGDBBillingJNDI = MASTER_DB_CFG.getProperty(CommonVariables.CFG_PG_DB_BILLNIG_JNDI);

            ConnPoolObj.loadAll(conn);
            conn.close();

            DBConnectionProvider.setConnPoolObject(ConnPoolObj);
            DBConnectionProvider.createDBConnPool(CommonVariables.MASTER_DB_JNDI_ID);
            SQLStatementExecutor.ConnPoolObj = ConnPoolObj;
            ResultSetConverter.connPool      = ConnPoolObj;
        }
        return ConnPoolObj;
    }

    public static ConnectionPoolSingleton getInstance()
            throws Exception
    {
        if (ConnPoolObj == null)
            throw new Exception("ConnectionPoolSingleton is not created");

        return ConnPoolObj;
    }

    public DatabaseInformationModel getMasterDatabaseID()
    {
        return databaseID.get(CommonVariables.MASTER_DB_JNDI_ID);
    }

    public DatabaseInformationModel getDatabaseID(
            String dbID)
    {
        return databaseID.get(dbID);
    }

    public void loadAll(
            Connection mDBConnection)
            throws Exception
    {
        loadMariaDBtoPGDBColMap(mDBConnection, true);
        loadMariaDBErrorCodes(mDBConnection, true);
        loadDBJNDIInfo(mDBConnection, true);
        loadCliJNDI(mDBConnection, true);
        loadCliUserMap(mDBConnection, true);
        loadLogDownloadColMap(mDBConnection, true);
        loadFMSGLogDownloadColMap(mDBConnection, true);
    }

    public void loadMariaDBtoPGDBColMap(
            Connection mDBConnection,
            boolean forceReload)
            throws Exception
    {
        if (!forceReload && (ConnPoolObj.hmMapSubPG_MYSQL.size() > 0))
            return;

        hmMapCol_DataType.clear();
        hmMapSubPG_MYSQL.clear();
        hmMapDelPG_MYSQL.clear();
        hmPG_MDB_Col_Map.clear();

        final Statement stmt = mDBConnection.createStatement();
        final String    ssql = "SELECT  mdb_table_name, mdb_column_name, pg_column_name, data_type FROM mdb_to_pg_log_col_map";

        log.info("Fetching MariaDB & Postgresql Column Mapping");
        // read client table to get connection strings
        final ResultSet rsMDBPGColMap = stmt.executeQuery(ssql);

        while (rsMDBPGColMap.next())
        {
            final String tablePrefix = rsMDBPGColMap.getString(1);
            final String mdbColName  = rsMDBPGColMap.getString(2);
            final String pgColName   = rsMDBPGColMap.getString(3);
            final String colDataType = rsMDBPGColMap.getString(4);

            hmPG_MDB_Col_Map.put(pgColName, mdbColName);
            hmMapCol_DataType.put(pgColName, colDataType);

            if (tablePrefix.equals(CommonVariables.MARIA_DB_SUB_TABLE_PREFIX))
            {
                hmMapSubPG_MYSQL.put(mdbColName, pgColName);
                setSubCols.add("s." + mdbColName);
            }
            else
                if (tablePrefix.equals(CommonVariables.MARIA_DB_DEL_TABLE_PREFIX))
                {
                    ConnPoolObj.hmMapDelPG_MYSQL.put(mdbColName, pgColName);
                    setDelCols.add("d." + mdbColName);
                }
        }
        rsMDBPGColMap.close();
        stmt.close();
    }

    public void loadMariaDBErrorCodes(
            Connection mDBConnection,
            boolean forceReload)
            throws Exception
    {
        if (!forceReload && (ConnPoolObj.hmErrDesc.size() > 0))
            return;

        hmErrDesc.clear();
        hmErrStatusFlag.clear();

        final Statement stmt = mDBConnection.createStatement();
        final String    ssql = "select error_code, display_error, status_flag from error_code_mapping";

        log.info("Fetching error code mapping data");
        final ResultSet rsErrorCcode = stmt.executeQuery(ssql);

        while (rsErrorCcode.next())
        {
            final String errorCode     = rsErrorCcode.getString(1);
            final String errDesc       = rsErrorCcode.getString(2);
            final String errStatusFlag = rsErrorCcode.getString(3);
            ConnPoolObj.hmErrDesc.put(errorCode, errDesc);
            ConnPoolObj.hmErrStatusFlag.put(errorCode, errStatusFlag);
        }
        rsErrorCcode.close();
        stmt.close();
    }

    public void loadCliJNDI(
            Connection mDBConnection,
            boolean forceReload)
            throws Exception
    {
        if (!forceReload && (ConnPoolObj.hmMDBCliJNDI.size() > 0))
            return;
        hmMDBCliJNDI.clear();
        hmPGCliJNDI.clear();
        hsJNDICliIdMap.clear();
        defaultMDBCliJNDI_ID = null;
        defaultPGCliJNDI_ID  = null;

        log.info("Reading JNDI information for Maria DB");
        final Statement stmt = mDBConnection.createStatement();

        String          ssql = null;

        if (ConnPoolObj.CfgMDBBillingJNDI != null)
            ssql = "select id from jndi_info where description='" + ConnPoolObj.CfgMDBBillingJNDI + "'";
        else
            ssql = "select jndi_info_id from bill_log_map_default";

        log.info("SQL to fetch default MariaDB Billing DB: " + ssql);
        final ResultSet rsDefaultMDBJNDI = stmt.executeQuery(ssql);
        String          jndi_id          = null;

        if (rsDefaultMDBJNDI.next())
        {
            jndi_id = Utility.nullCheck(rsDefaultMDBJNDI.getString(1), true);
            if (!rsDefaultMDBJNDI.wasNull() && !"".equals(jndi_id))
                ConnPoolObj.defaultMDBCliJNDI_ID = jndi_id;
        }
        rsDefaultMDBJNDI.close();

        if (ConnPoolObj.defaultMDBCliJNDI_ID == null)
            throw new Exception("No Data Found in the table configuration.bill_log_map_default");

        if (ConnPoolObj.CfgPGDBBillingJNDI != null)
            ssql = "select id from jndi_info where description='" + ConnPoolObj.CfgPGDBBillingJNDI + "'";
        else
            ssql = "select jndi_info_id from pg_bill_log_map_default";

        log.info("SQL to fetch default Postgresql Billing DB: " + ssql);
        final ResultSet rsDefaultPGJNDI = stmt.executeQuery(ssql);
        jndi_id = null;

        if (rsDefaultPGJNDI.next())
        {
            jndi_id = Utility.nullCheck(rsDefaultPGJNDI.getString(1), true);
            if (!rsDefaultPGJNDI.wasNull() && !"".equals(jndi_id))
                ConnPoolObj.defaultPGCliJNDI_ID = jndi_id;
        }
        rsDefaultPGJNDI.close();

        if (ConnPoolObj.defaultPGCliJNDI_ID == null)
            throw new Exception("No Data Found in the table configuration.pg_bill_log_map_default");

        ssql = "SELECT a.id as jndi_info_id, group_concat(b.cli_id) as cli_id "
                + "FROM jndi_info a INNER JOIN bill_log_map b ON b.jndi_info_id = a.id group by a.id";

        log.info("Fetching MariaDB bill log map details");
        // read client table to get connection strings
        final ResultSet rsClientId = stmt.executeQuery(ssql);

        // map the db id with the host id - currently description is used
        while (rsClientId.next())
        {
            final String       JNDI_ID      = rsClientId.getString(1);
            final String       Client_ID    = rsClientId.getString(2);
            final List<String> lstClient_ID = Arrays.asList(Client_ID.split(","));
            // hmCliJNDI_ID
            ConnPoolObj.hmMDBCliJNDI.put(JNDI_ID, lstClient_ID);
            ConnPoolObj.hsJNDICliIdMap.addAll(lstClient_ID);
        }
        rsClientId.close();

        log.info("Fetching Postgres bill log map details");
        ssql = "SELECT a.id as jndi_info_id,group_concat(b.cli_id) as cli_id "
                + "FROM jndi_info a  INNER JOIN pg_bill_log_map b ON b.jndi_info_id = a.id group by a.id";
        // read client table to get connection strings
        final ResultSet rsPGClientId = stmt.executeQuery(ssql);

        // map the db id with the host id - currently description is used
        while (rsPGClientId.next())
        {
            final String       JNDI_ID      = rsPGClientId.getString(1);
            final String       Client_ID    = rsPGClientId.getString(2);
            final List<String> lstClient_ID = Arrays.asList(Client_ID.split(","));
            // hmCliJNDI_ID
            ConnPoolObj.hmPGCliJNDI.put(JNDI_ID, lstClient_ID);
            ConnPoolObj.hsJNDICliIdMap.addAll(lstClient_ID);
        }
        rsPGClientId.close();
        stmt.close();
    }

    public void loadDBJNDIInfo(
            Connection mDBConnection,
            boolean forceReload)
            throws Exception
    {
        if (!forceReload && (ConnPoolObj.databaseID.size() > 0))
            return;

        databaseID.clear();
        log.info("Fetching all JNDI Information");
        final Statement stmt           = mDBConnection.createStatement();
        final String    ssql           = "select id, url, description, username, password, driver_class_name, "
                + "validation_query, num_tests_per_eviction_run, min_evictable_idle_time_millis,max_total "
                + " from sysconfig.jndi_info";
        final ResultSet rsDatabaseInfo = stmt.executeQuery(ssql);

        // map the db id with the host id - currently description is used
        while (rsDatabaseInfo.next())
        {
            final String dbID = rsDatabaseInfo.getString(1);
            log.info(String.format("Storing database info to hashmap - DATABASE ID : %s", dbID));

            final DatabaseInformationModel dbInfoModel = new DatabaseInformationModel();
            dbInfoModel.url                            = rsDatabaseInfo.getString(2);
            dbInfoModel.description                    = rsDatabaseInfo.getString(3);
            dbInfoModel.username                       = rsDatabaseInfo.getString(4);
            dbInfoModel.password                       = rsDatabaseInfo.getString(5);
            dbInfoModel.driver_class_name              = rsDatabaseInfo.getString(6);
            dbInfoModel.validation_query               = rsDatabaseInfo.getString(7);
            dbInfoModel.num_tests_per_eviction_run     = rsDatabaseInfo.getInt(8);
            dbInfoModel.min_evictable_idle_time_millis = rsDatabaseInfo.getInt(9);
            dbInfoModel.max_total                      = rsDatabaseInfo.getInt(10);

            // for now result set is used
            databaseID.put(dbID, dbInfoModel);
        }
        rsDatabaseInfo.close();
        stmt.close();
    }

    public void loadCliUserMap(
            Connection mDBConnection,
            boolean forceReload)
            throws Exception
    {
        if (!forceReload && (ConnPoolObj.hmCliUserMap.size() > 0))
            return;

        hmCliUserMap.clear();
        log.info("Fetching Client Id & Account User mapping");
        final Statement stmt      = mDBConnection.createStatement();
        final String    ssql      = "select cli_id, user from accounts.accounts_view";
        final ResultSet rsCliUser = stmt.executeQuery(ssql);

        while (rsCliUser.next())
        {
            final String cliId = rsCliUser.getString(1);
            final String user  = rsCliUser.getString(2);
            hmCliUserMap.put(cliId, user);
        }
        rsCliUser.close();
        stmt.close();
    }

    public void loadLogDownloadColMap(
            Connection mDBConnection,
            boolean forceReload)
            throws Exception
    {
        if (!forceReload && (ConnPoolObj.hmLog_DL_Col_Map.size() > 0))
            return;

        hmLog_DL_Col_Map.clear();
        hmLog_DL_Col_DataType.clear();

        log.info("Fetching Log Download Column mapping");
        final Statement stmt        = mDBConnection.createStatement();
        final String    ssql        = "select column_name, mapped_name, data_type from configuration.log_download_col_map";
        final ResultSet rsLogColMap = stmt.executeQuery(ssql);

        while (rsLogColMap.next())
        {
            final String colName  = rsLogColMap.getString(1);
            final String mapName  = rsLogColMap.getString(2);
            final String dataType = rsLogColMap.getString(3);
            hmLog_DL_Col_Map.put(colName, mapName);
            hmLog_DL_Col_DataType.put(colName, dataType);
        }
        rsLogColMap.close();
        stmt.close();
    }

    public void loadFMSGLogDownloadColMap(
            Connection mDBConnection,
            boolean forceReload)
            throws Exception
    {
        if (!forceReload && (ConnPoolObj.hmFMSG_Log_DL_Col_Map.size() > 0))
            return;

        hmFMSG_Log_DL_Col_Map.clear();
        hmFMSG_Log_DL_Col_DataType.clear();

        log.info("Fetching Full Message Log Download Column mapping");
        final Statement stmt        = mDBConnection.createStatement();
        final String    ssql        = "select column_name, mapped_name, data_type from configuration.fmsg_log_download_col_map";
        final ResultSet rsLogColMap = stmt.executeQuery(ssql);

        while (rsLogColMap.next())
        {
            final String colName  = rsLogColMap.getString(1);
            final String mapName  = rsLogColMap.getString(2);
            final String dataType = rsLogColMap.getString(3);
            hmFMSG_Log_DL_Col_Map.put(colName, mapName);
            hmFMSG_Log_DL_Col_DataType.put(colName, dataType);
        }
        rsLogColMap.close();
        stmt.close();
    }

}