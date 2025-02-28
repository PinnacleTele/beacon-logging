package com.itextos.beacon.queryprocessor.threadpoolexecutor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.queryprocessor.commonutils.CommonVariables;
import com.itextos.beacon.queryprocessor.commonutils.Utility;
import com.itextos.beacon.queryprocessor.databaseconnector.ConnectionPoolSingleton;
import com.itextos.beacon.queryprocessor.databaseconnector.DBConnectionProvider;
import com.itextos.beacon.queryprocessor.databaseconnector.ResultSetConverter;
import com.itextos.beacon.queryprocessor.databaseconnector.SQLStatementExecutor;
import com.itextos.beacon.queryprocessor.databaseconnector.SqlLiteConnectionProvider;

public class ProcessQueueThreadPool
{

    private static final Log log           = LogFactory.getLog(ProcessQueueThreadPool.class);
    static int               MAX_T         = 1;
    static final Properties  mySQL_cfg_val = new Properties();

    public static void main(
            String[] args)
    {
        Connection masterDBConn = null;
        String     queue_id     = null;

        try
        {
            final String cfg_fn = args[0];
            log.info("Reading values from config file: " + cfg_fn);
            final FileReader file = new FileReader(args[0]);
            mySQL_cfg_val.load(file);
            file.close();
            final ConnectionPoolSingleton connPool = ConnectionPoolSingleton.createInstance(mySQL_cfg_val);
            masterDBConn = DBConnectionProvider.getMasterDBConnection();

            MAX_T        = Utility.getInteger(mySQL_cfg_val.getProperty("maxExecutorThreads").trim());
            log.info("Queue Processor Executor Max Threads: " + MAX_T);
            // creates a thread pool with MAX_T no. of
            // threads as the fixed pool size(Step 2)
            final ExecutorService pool = Executors.newFixedThreadPool(MAX_T);
            log.info("Queue Processor Executor started");

            while (true)
            {
                if (log.isDebugEnabled())
                    log.debug("Checking pending items on queue");
                queue_id = null;
                final ResultSet rs_pending_queue = SQLStatementExecutor.getPendingQueue(masterDBConn);

                if (rs_pending_queue != null)
                {

                    if (rs_pending_queue.next())
                    {
                        queue_id = rs_pending_queue.getString("queue_id");

                        log.info(String.format("Processing queue - QUEUE ID : %s", queue_id));

                        SQLStatementExecutor.logQueueProcessingInfo(masterDBConn, queue_id,
                                String.format("Queue Processing Started"), CommonVariables.INFO);

                        // update the status of the picked record
                        final String            updSQL = "update query_async_queue "
                                + " set current_status=?, started_ts=?, modified_ts=? " + " where queue_id = ?";
                        final PreparedStatement pstmt  = masterDBConn.prepareStatement(updSQL);
                        pstmt.setString(1, CommonVariables.QUEUE_STARTED);
                        pstmt.setString(2, DateTimeUtility.getFormattedCurrentDateTime(DateTimeFormat.DEFAULT));
                        pstmt.setString(3, DateTimeUtility.getFormattedCurrentDateTime(DateTimeFormat.DEFAULT));
                        pstmt.setString(4, queue_id);
                        pstmt.executeUpdate();
                        pstmt.close();
                        masterDBConn.commit();

                        SQLStatementExecutor.logQueueProcessingInfo(masterDBConn, queue_id,
                                String.format("Starting background task"), CommonVariables.INFO);
                        log.info("Starting background task");
                        // passes the Task objects to the pool to execute (Step 3)
                        final Runnable queue_task = new GenerateRequestedData(connPool, queue_id);

                        pool.execute(queue_task);
                    }

                    final Statement stmt = rs_pending_queue.getStatement();
                    rs_pending_queue.close();
                    stmt.close();
                    Thread.sleep(10000);
                }
                else
                    // nothing pending to process, make the thread sleep for 10 secs
                    Thread.sleep(10000);
            }
        }
        catch (final Exception e)
        {
            log.error(e.getMessage(), e);
            e.printStackTrace();
            if (queue_id != null)
                try
                {
                    SQLStatementExecutor.logQueueProcessingInfo(masterDBConn, queue_id, e.getMessage(),
                            CommonVariables.ERROR);
                }
                catch (final Exception e1)
                {
                    log.error("Error while updating Queue Status", e1);
                    e1.printStackTrace();
                }
        }
        finally
        {
            if (masterDBConn != null)
                try
                {
                    if (!masterDBConn.isClosed())
                        masterDBConn.close();
                }
                catch (final Exception ex)
                {
                    log.error("Error while closing Master DB Connection", ex);
                    ex.printStackTrace();
                }
        }
    }

}

class GenerateRequestedData
        implements
        Runnable
{

    private static final Log        log      = LogFactory.getLog(GenerateRequestedData.class);
    String                          queue_id;
    private ConnectionPoolSingleton connPool = null;

    // get db connection object to this thread
    public GenerateRequestedData(
            ConnectionPoolSingleton p_conn_pool,
            String p_queue_id)
    {
        connPool = p_conn_pool;
        queue_id = p_queue_id;
    }

    @Override
    @SuppressWarnings(
    { "unchecked", "resource" })
    public void run()
    {
        Connection                masterDBConn       = null;
        Connection                clientConnection   = null;
        Connection                clientPGConnection = null;
        SqlLiteConnectionProvider sqliteCP           = null;

        try
        {
            final String serverIP = Utility.getApplicationServerIp();
            log.info(String.format("Queue Task Started - QUEUE ID: %s, Host: %s", queue_id, serverIP));
            masterDBConn = DBConnectionProvider.getMasterDBConnection();
            SQLStatementExecutor.logQueueProcessingInfo(masterDBConn, queue_id,
                    "Queue Task Started in the Host:" + serverIP, CommonVariables.INFO);

            // getting queue request information
            final ResultSet rs_pending_queue = SQLStatementExecutor.getQueryRequestInfo(masterDBConn, queue_id);

            if (rs_pending_queue.next())
            {
                log.info(String.format("Request parameter info available - QUEUE ID: %s", queue_id));

                final JSONParser parser       = new JSONParser();
                final JSONObject paramJson    = (JSONObject) parser
                        .parse(rs_pending_queue.getString(CommonVariables.QUERY_PARAM));

                boolean          full_message = false;
                if (paramJson.containsKey(CommonVariables.R_FULL_MESSAGE))
                    full_message = (boolean) paramJson.get(CommonVariables.R_FULL_MESSAGE);

                final List<Long>        client_ids     = (List<Long>) paramJson.get(CommonVariables.R_CLI_ID);
                final List<String>      lstCli         = client_ids.stream().map(s -> String.valueOf(s))
                        .collect(Collectors.toList());

                final String            DATE_FORMAT_S  = "yyyy-MM-dd HH:mm:ss";
                final DateTimeFormatter formatWithS    = DateTimeFormatter.ofPattern(DATE_FORMAT_S);

                final LocalDateTime     paramStartTime = LocalDateTime
                        .parse(paramJson.get(CommonVariables.START_DATE).toString(), formatWithS);
                final LocalDateTime     paramEndTime   = LocalDateTime
                        .parse(paramJson.get(CommonVariables.END_DATE).toString(), formatWithS);

                log.info(String.format("Request Start Time: %s", Utility.formatDateTime(paramStartTime)));
                log.info(String.format("Request End Time: %s", Utility.formatDateTime(paramEndTime)));

                final LocalDate     startDate   = paramStartTime.toLocalDate();
                final LocalDate     endDate     = paramEndTime.toLocalDate();
                List<LocalDateTime> listOfDates = null;

                final long          totalDays   = ChronoUnit.DAYS.between(startDate, endDate);

                if (totalDays == 0)
                    listOfDates = Collections.singletonList(startDate.atStartOfDay());
                else
                    listOfDates = startDate.datesUntil(endDate.plusDays(1)).map(d -> d.atStartOfDay())
                            .collect(Collectors.toList());

                log.info(String.format("Total requested days : %d", (totalDays + 1)));

                final String     timeZoneName   = Utility.nullCheck(paramJson.get("zone_name"), true);
                final JSONObject paramFilters   = (JSONObject) paramJson.get("filters");

                final boolean    sort_available = !Utility.nullCheck(paramJson.get("sort_by"), true).equals("");
                final String     sqliteFilePath = ProcessQueueThreadPool.mySQL_cfg_val.getProperty("sqlLiteFilePath")
                        .trim();

                String           sort_field     = null;
                String           sort_order     = null;
                sqliteCP = null;

                if (full_message || sort_available)
                {

                    if (sort_available)
                    {
                        sort_field = Utility.nullCheck(paramJson.get("sort_by"), true);
                        sort_order = Utility.nullCheck(paramJson.get("sort_order"), true);
                        log.info(String.format("Sort Field: %s, Order: %s", sort_field, sort_order));
                    }

                    final String sqliteDBFile = sqliteFilePath + File.separator + queue_id + ".db";
                    log.info(String.format("Creating SQLite DB: %s", sqliteDBFile));

                    try
                    {
                        final boolean result = Files.deleteIfExists(Paths.get(sqliteDBFile));
                    }
                    catch (final Exception e)
                    {
                        log.error("Error while deleting SQLite DB File: " + sqliteDBFile, e);
                        SQLStatementExecutor.updateQueueStatus(masterDBConn, queue_id, CommonVariables.COMPLETION_ERROR,
                                -1, CommonVariables.COMPLETION_FAILURE);
                        return;
                    }

                    sqliteCP = new SqlLiteConnectionProvider(connPool, sqliteDBFile, sort_field, sort_order,
                            full_message);
                    sqliteCP.GetDBConnection();
                }

                final String         csvFilePath    = ProcessQueueThreadPool.mySQL_cfg_val.getProperty("csvFilePath")
                        .trim();
                final String         csvFilename    = csvFilePath + File.separator + queue_id + ".csv";
                boolean              isColumnsPrint = false;

                final BufferedWriter bwCSV          = new BufferedWriter(new FileWriter(csvFilename));

                String               dbType         = null;
                int                  recordCount    = 0;

                for (final LocalDateTime date : listOfDates)
                {
                    final LocalDate procDate    = date.toLocalDate();
                    final String    procDateStr = Utility.formatDateTime(procDate);
                    LocalDateTime   qStartDate  = null;
                    LocalDateTime   qEndDate    = null;
                    ResultSet       rsRecords   = null;

                    if (startDate.isEqual(procDate))
                        qStartDate = paramStartTime;
                    else
                        qStartDate = date;

                    // if requested for same day or last day
                    if (endDate.isEqual(procDate))
                        // where condition has < end time
                        qEndDate = paramEndTime.plusSeconds(1);
                    else
                        qEndDate = date.plusDays(1);

                    log.info(String.format("Query Param Start Time : %s", Utility.formatDateTime(qStartDate)));
                    log.info(String.format("Query Param End Time : %s", Utility.formatDateTime(qEndDate)));
                    SQLStatementExecutor.logQueueProcessingInfo(masterDBConn, queue_id,
                            String.format("Processing Date: %s", procDateStr), CommonVariables.INFO);

                    if (date.toLocalDate().isAfter(LocalDate.now().plusDays(-2)))
                    {
                        log.info("Loading last 2 days data");
                        dbType = CommonVariables.MARIA_DB;

                        if (clientConnection == null)
                        {
                            clientConnection = DBConnectionProvider.getDBConnection(connPool.defaultMDBCliJNDI_ID);

                            if (clientConnection == null)
                            {
                                log.error("Unable to get MariaDB Connection for the JNDI ID: "
                                        + connPool.defaultMDBCliJNDI_ID);
                                SQLStatementExecutor.updateQueueStatus(masterDBConn, queue_id,
                                        CommonVariables.COMPLETION_ERROR, -1, CommonVariables.COMPLETION_FAILURE);
                                return;
                            }
                        }

                        if (full_message)
                        {
                            final int sqltFMSGCount = sqliteCP.insertFMSGData(dbType, clientConnection, lstCli,
                                    qStartDate, qEndDate, paramFilters);
                            log.info(String.format("Date: %s, Full Message SQLite Count: %d", procDateStr,
                                    sqltFMSGCount));
                            rsRecords = sqliteCP.getFMSGRecords();
                        }
                        else
                            rsRecords = SQLStatementExecutor.getLogRecords(clientConnection, lstCli, qStartDate,
                                    qEndDate, paramFilters, full_message);
                    }
                    else
                    {
                        dbType = CommonVariables.PG_DB;

                        if (clientPGConnection == null)
                        {
                            clientPGConnection = DBConnectionProvider.getDBConnection(connPool.defaultPGCliJNDI_ID);

                            if (clientPGConnection == null)
                            {
                                log.error("Unable to get PGDB Connection for the JNDI ID: "
                                        + connPool.defaultPGCliJNDI_ID);
                                SQLStatementExecutor.updateQueueStatus(masterDBConn, queue_id,
                                        CommonVariables.COMPLETION_ERROR, -1, CommonVariables.COMPLETION_FAILURE);
                                return;
                            }
                        }

                        if (full_message)
                        {
                            final int sqltFMSGCount = sqliteCP.insertFMSGData(dbType, clientPGConnection, lstCli,
                                    qStartDate, qEndDate, paramFilters);
                            log.info(String.format("Date: %s, Full Message SQLite Count: %d", procDateStr,
                                    sqltFMSGCount));
                            rsRecords = sqliteCP.getFMSGRecords();
                        }
                        else
                            rsRecords = SQLStatementExecutor.getPGLogRecords(clientPGConnection, lstCli, qStartDate,
                                    qEndDate, paramFilters, full_message);
                    }

                    if (rsRecords == null)
                    {
                        log.error(String.format("Unable to get Data for the Date: %s", procDateStr));
                        SQLStatementExecutor.updateQueueStatus(masterDBConn, queue_id, CommonVariables.COMPLETION_ERROR,
                                -1, CommonVariables.COMPLETION_FAILURE);
                        return;
                    }

                    if ((!isColumnsPrint) && (!rsRecords.isClosed()))
                    {
                        final ResultSetMetaData rsmd = rsRecords.getMetaData();
                        bwCSV.write(CommonVariables.LOG_USERNAME);

                        for (int cidx = 4; cidx <= rsmd.getColumnCount(); cidx++)
                        {
                            final String colName = rsmd.getColumnLabel(cidx);
                            if (colName.equals(CommonVariables.MDB_BILL_CURR_COL_NAME))
                                continue;

                            String colHdr = "";
                            if (full_message)
                                colHdr += connPool.hmFMSG_Log_DL_Col_Map.get(colName);
                            else
                                colHdr += connPool.hmLog_DL_Col_Map.get(colName);
                            bwCSV.write("," + colHdr);
                        }
                        bwCSV.newLine();
                        isColumnsPrint = true;
                    }

                    int dataCount = 0;

                    if (sort_available)
                    {
                        SQLStatementExecutor.logQueueProcessingInfo(masterDBConn, queue_id,
                                "Writing Data into SQLite for sorting", "INFO");
                        dataCount = sqliteCP.insertCSVRecords(rsRecords, timeZoneName);
                        log.info("Written Data into SQLite for sorting, Count: " + dataCount);
                    }
                    else
                    {
                        SQLStatementExecutor.logQueueProcessingInfo(masterDBConn, queue_id,
                                "Writing Data into CSV File", CommonVariables.INFO);
                        dataCount = ResultSetConverter.writeRStoLogCSVFile(rsRecords, bwCSV, timeZoneName);
                        log.info("Written Data into CSV File, Count: " + dataCount);
                    }

                    final Statement stmt = rsRecords.getStatement();
                    rsRecords.close();
                    stmt.close();

                    if (dataCount > 0)
                    {
                        log.info(String.format("Date: %s, Count: %d", procDateStr, dataCount));
                        recordCount += dataCount;
                    }
                    else
                        log.info(String.format("No Data available for the Date: %s", procDateStr));
                }

                if (full_message || sort_available)
                {

                    if (sort_available && (recordCount > 0))
                    {
                        SQLStatementExecutor.logQueueProcessingInfo(masterDBConn, queue_id,
                                "Writing sorted data from SQLite into CSV File", CommonVariables.INFO);
                        final int sort_count = sqliteCP.writeSortLogCSVFile(bwCSV);
                        log.info("Written sorted data from SQLite into CSV File, Count: " + sort_count);
                    }

                    sqliteCP.closeConnection();
                    sqliteCP = null;
                }

                bwCSV.close();

                log.info(String.format("CSV Created successfully, Record Count: %d", recordCount));
                SQLStatementExecutor.logQueueProcessingInfo(masterDBConn, queue_id,
                        String.format("CSV Created successfully, Record Count: %d", recordCount), CommonVariables.INFO);
                SQLStatementExecutor.updateQueueStatus(masterDBConn, queue_id, CommonVariables.QUEUE_COMPLETED,
                        recordCount, CommonVariables.COMPLETION_SUCCESS);
            }
            else
            {
                SQLStatementExecutor.updateQueueStatus(masterDBConn, queue_id, CommonVariables.COMPLETION_ERROR, -1,
                        CommonVariables.COMPLETION_FAILURE);
                log.error(String.format("No request parameter info available - QUEUE ID: %s", queue_id));
            }
            final Statement stmt = rs_pending_queue.getStatement();
            rs_pending_queue.close();
            stmt.close();
        }
        catch (final Exception e)
        {
            log.error(e.getMessage(), e);
            e.printStackTrace();

            try
            {
                SQLStatementExecutor.updateQueueStatus(masterDBConn, queue_id, CommonVariables.COMPLETION_ERROR, -1,
                        CommonVariables.COMPLETION_FAILURE);
            }
            catch (final Exception e1)
            {
                log.error("Error while updating Queue Status", e1);
                e1.printStackTrace();
            }
        }
        finally
        {
            if (masterDBConn != null)
                try
                {
                    if (!masterDBConn.isClosed())
                        masterDBConn.close();
                }
                catch (final Exception ex)
                {
                    log.error("Error while closing Master DB Connection", ex);
                    ex.printStackTrace();
                }

            if ((clientConnection != null))
                try
                {
                    if (!clientConnection.isClosed())
                        clientConnection.close();
                }
                catch (final Exception ex2)
                {
                    log.error("Error while closing MariaDB Connection", ex2);
                    ex2.printStackTrace();
                }

            if ((clientPGConnection != null))
                try
                {
                    if (!clientPGConnection.isClosed())
                        clientPGConnection.close();
                }
                catch (final Exception ex3)
                {
                    log.error("Error while closing Posgresql DB Connection", ex3);
                    ex3.printStackTrace();
                }

            if ((sqliteCP != null))
                try
                {
                    sqliteCP.closeConnection();
                }
                catch (final Exception ex4)
                {
                    log.error("Error while closing SQLite DB File: " + sqliteCP.dbFilePath, ex4);
                    ex4.printStackTrace();
                }
        }
    }

}