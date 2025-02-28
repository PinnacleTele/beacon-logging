package com.itextos.beacon.queryprocessor.requestreceiver;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.http.HttpStatus;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.itextos.beacon.errorlog.QPRLog;
import com.itextos.beacon.queryprocessor.commonutils.CommonVariables;
import com.itextos.beacon.queryprocessor.commonutils.Utility;
import com.itextos.beacon.queryprocessor.databaseconnector.ConnectionPoolSingleton;
import com.itextos.beacon.queryprocessor.databaseconnector.DBConnectionProvider;
import com.itextos.beacon.queryprocessor.databaseconnector.ResultSetConverter;
import com.itextos.beacon.queryprocessor.databaseconnector.SQLStatementExecutor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Get data from MARIADB or POSTGRES
 */
public class GetData
        extends
        HttpServlet
{

    private static final QPRLog log           = QPRLog.getInstance();
    private static ConnectionPoolSingleton connPool = null;
    public static String                   API_NAME = CommonVariables.GET_DATA_API;

    private static String validateColumns(
            List<String> lstParamColumns)
    {
        final Set<String> setIVcols = lstParamColumns.stream()
                .filter(val -> (!val.startsWith("s.") && !val.startsWith("d."))).collect(Collectors.toSet());

        final Set<String> ivSubCols = lstParamColumns.stream().filter(val -> val.startsWith("s."))
                .collect(Collectors.toSet());
        ivSubCols.removeAll(connPool.setSubCols);

        final Set<String> ivDelCols = lstParamColumns.stream().filter(val -> val.startsWith("d."))
                .collect(Collectors.toSet());
        ivDelCols.removeAll(connPool.setDelCols);

        setIVcols.addAll(ivSubCols);
        setIVcols.addAll(ivDelCols);

        if (setIVcols.size() > 0)
            return String.join(", ", setIVcols);

        return null;
    }

    /**
     *
     */
    @SuppressWarnings(
    { "unchecked", "resource" })
    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException,
            IOException
    {
        final JSONObject resJson            = new JSONObject();

        Connection       clientConnection   = null;
        Connection       clientPGConnection = null;

        try
        {
            if (connPool == null)
                connPool = ConnectionPoolSingleton.getInstance();

            // add timestamp of the server

            int              maxlimit  = Utility.getInteger(QueryEngine.mySQL_cfg_val.getProperty("recordLimit"));

            final JSONParser parser    = new JSONParser();

            JSONObject       reqJson   = null;
            JSONObject       paramJson = null;

            try
            {
                reqJson   = (JSONObject) parser.parse(IOUtils.toString(req.getReader()));
                paramJson = (JSONObject) parser.parse(reqJson.get(CommonVariables.R_PARAM).toString());
            }
            catch (final Exception e)
            {
                // TODO Auto-generated catch block
                log.error("JSON Parsing Error", e);
                resp.setStatus(HttpStatus.BAD_REQUEST_400);
                resJson.put(CommonVariables.STATUS_MESSAGE, "Invalid JSON");
                resp.getWriter().println(resJson.toJSONString());
                return;
            }
            log.info("Parameter list");
            log.info(paramJson.toJSONString());

            final String            DATE_FORMAT_S  = "yyyy-M-dd HH:mm:ss";
            final DateTimeFormatter formatWithS    = DateTimeFormatter.ofPattern(DATE_FORMAT_S);
            final LocalDateTime     paramStartTime = LocalDateTime
                    .parse(paramJson.get(CommonVariables.START_DATE).toString(), formatWithS);
            final LocalDateTime     paramEndTime   = LocalDateTime
                    .parse(paramJson.get(CommonVariables.END_DATE).toString(), formatWithS);

            final LocalDate         ldt_cur_dt     = LocalDate.now();

            log.info(String.format("Param Start Time: %s", Utility.formatDateTime(paramStartTime)));
            log.info(String.format("Param End Time: %s", Utility.formatDateTime(paramEndTime)));

            if (paramStartTime.isAfter(paramEndTime))
            {
                log.error("Start Time greater than End Time");
                resp.setStatus(HttpStatus.BAD_REQUEST_400);
                resJson.put(CommonVariables.STATUS_MESSAGE, "Start Time greater than End Time");
                resp.getWriter().println(resJson.toJSONString());
                return;
            }

            final LocalDate paramStartDate = paramStartTime.toLocalDate();

            final int       max_past_days  = Utility.getInteger(QueryEngine.mySQL_cfg_val.getProperty("maxPastDays"));
            final LocalDate ldt_max_old_dt = paramStartDate.plusDays(-max_past_days);

            if (ldt_max_old_dt.isAfter(paramStartDate))
            {
                log.error("Start Time cannot be Older than " + max_past_days + " Days");
                resp.setStatus(HttpStatus.BAD_REQUEST_400);
                resJson.put(CommonVariables.STATUS_MESSAGE,
                        "Start Time cannot be Older than " + max_past_days + " Days");
                resp.getWriter().println(resJson.toJSONString());
                return;
            }
            /***************************************************
             * else
             * if (paramStartDate.isAfter(ldt_cur_dt))
             * {
             * log.info(String.format("Param Start Time: %s",
             * Utility.formatDateTime(paramStartTime)));
             * log.info(String.format("Param End Time: %s",
             * Utility.formatDateTime(paramEndTime)));
             * log.error("Start Time cannot be Future Date");
             * resp.setStatus(HttpStatus.BAD_REQUEST_400);
             * resJson.put(CommonVariables.STATUS_MESSAGE, "Start Time cannot be Future
             * Date");
             * resp.getWriter().println(resJson.toJSONString());
             * return;
             * }
             * if (paramEndDate.isAfter(ldt_cur_dt))
             * {
             * log.info(String.format("Param Start Time: %s",
             * Utility.formatDateTime(paramStartTime)));
             * log.info(String.format("Param End Time: %s",
             * Utility.formatDateTime(paramEndTime)));
             * log.error("End Time cannot be Future Date");
             * resp.setStatus(HttpStatus.BAD_REQUEST_400);
             * resJson.put(CommonVariables.STATUS_MESSAGE, "End Time cannot be Future
             * Date");
             * resp.getWriter().println(resJson.toJSONString());
             * return;
             * }
             *************************************************************/
            final String paramColumns = Utility.nullCheck(paramJson.get("columns"), true);

            if ("".equals(paramColumns))
            {
                log.error("Columns parameter is empty");
                resp.setStatus(HttpStatus.BAD_REQUEST_400);
                resJson.put(CommonVariables.STATUS_MESSAGE, "Columns parameter is empty");
                resp.getWriter().println(resJson.toJSONString());
                return;
            }

            final List<String> lstParamColumns = Arrays.asList(paramColumns.trim().split("\\s*,\\s*"));

            final String       invalidColumns  = GetData.validateColumns(lstParamColumns);

            if (invalidColumns != null)
            {
                log.error("Invalid Columns: " + invalidColumns);
                resp.setStatus(HttpStatus.BAD_REQUEST_400);
                resJson.put(CommonVariables.STATUS_MESSAGE, "Invalid Columns: " + invalidColumns);
                resp.getWriter().println(resJson.toJSONString());
                return;
            }

            final List<String> lstJSONCols = new ArrayList<>();

            for (final String pCol : lstParamColumns)
            {
                final String colName = pCol.substring(2);
                if (pCol.startsWith("s."))
                    lstJSONCols.add(connPool.hmMapSubPG_MYSQL.get(colName));
                else
                    if (pCol.startsWith("d."))
                        lstJSONCols.add(connPool.hmMapDelPG_MYSQL.get(colName));
            }
            log.info("JSON Columns List: " + String.join(",", lstJSONCols));

            final List<Long>   client_ids = (List<Long>) paramJson.get(CommonVariables.R_CLI_ID);
            final List<String> lstCli     = client_ids.stream().map(s -> String.valueOf(s))
                    .collect(Collectors.toList());

            int                limit      = Utility.getInteger(String.valueOf(paramJson.get("limit")));

            if (limit == 0)
                limit = maxlimit;
            maxlimit = Math.min(limit, maxlimit);

            final String        timeZoneName = Utility.nullCheck(paramJson.get("zone_name"), true);

            final LocalDate     startDate    = paramStartTime.toLocalDate();
            final LocalDate     endDate      = paramEndTime.toLocalDate();
            List<LocalDateTime> listOfDates  = null;

            final long          totalDays    = ChronoUnit.DAYS.between(startDate, endDate);

            if (totalDays == 0)
                listOfDates = Collections.singletonList(startDate.atStartOfDay());
            else
                listOfDates = startDate.datesUntil(endDate.plusDays(1)).map(d -> d.atStartOfDay())
                        .collect(Collectors.toList());

            log.info(String.format("Total requested days : %d", (totalDays + 1)));

            int             recordCount = 0;
            final JSONArray dataList    = new JSONArray();

            for (final LocalDateTime date : listOfDates)
            {
                LocalDateTime qStartDate = null;
                LocalDateTime qEndDate   = null;
                ResultSet     rsRecords  = null;

                final String  dateStr    = Utility.formatDateTime(date.toLocalDate());

                if (startDate.isEqual(date.toLocalDate()))
                    qStartDate = paramStartTime;
                else
                    qStartDate = date;

                // if requested for same day or last day
                if (endDate.isEqual(date.toLocalDate()))
                    // where condition has < end time
                    qEndDate = paramEndTime.plusSeconds(1);
                else
                    qEndDate = date.plusDays(1);

                log.info(String.format("Query Param Start Time : %s", Utility.formatDateTime(qStartDate)));
                log.info(String.format("Query Param End Time : %s", Utility.formatDateTime(qEndDate)));

                if (date.toLocalDate().isAfter(LocalDate.now().plusDays(-2)))
                {
                    log.info("Fetching Data from MariaDB");

                    if (clientConnection == null)
                        clientConnection = DBConnectionProvider.getDBConnection(connPool.defaultMDBCliJNDI_ID);

                    rsRecords = SQLStatementExecutor.getMariaDBDataForAPI(clientConnection, lstCli, qStartDate,
                            qEndDate, paramJson, maxlimit);
                }
                else
                {
                    log.info("Fetching Data from Postgresql");
                    if (clientPGConnection == null)
                        clientPGConnection = DBConnectionProvider.getDBConnection(connPool.defaultPGCliJNDI_ID);

                    rsRecords = SQLStatementExecutor.getPGDataForAPI(clientPGConnection, lstCli, qStartDate, qEndDate,
                            paramJson, maxlimit);
                }

                if (rsRecords != null)
                {
                    int                   dataCount = 0;
                    final HashSet<String> hsMsgId   = new HashSet<>();
                    final JSONArray       ja        = ResultSetConverter.ConvertRsToJSONArray(rsRecords, lstJSONCols,
                            timeZoneName);

                    if ((ja != null) && (ja.size() > 0))
                    {

                        for (final Object oe : ja)
                        {
                            final JSONObject jo = (JSONObject) oe;

                            if (jo.containsKey(CommonVariables.MSG_ID_COL_NAME))
                            {
                                final String msgId = (String) jo.get(CommonVariables.MSG_ID_COL_NAME);

                                if ((msgId != null))
                                {
                                    if (hsMsgId.contains(msgId))
                                        continue;
                                    hsMsgId.add(msgId);
                                }
                                jo.remove(CommonVariables.MSG_ID_COL_NAME);
                            }
                            dataList.add(jo);
                            recordCount++;
                            dataCount++;
                            if (maxlimit == recordCount)
                                break;
                        }
                        log.info(String.format("Date: %s, Count: %d", dateStr, dataCount));
                    }

                    if (dataCount == 0)
                        log.info(String.format("No Data available for the Date: %s", dateStr));

                    final Statement stmt = rsRecords.getStatement();
                    rsRecords.close();
                    stmt.close();

                    if (maxlimit == recordCount)
                        break;
                }
                else
                {
                    log.error(String.format("Unable to get Data for the Date: %s", dateStr));
                    resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
                    resJson.put(CommonVariables.STATUS_MESSAGE, "Unable to retrieve the data");
                    resp.getWriter().println(resJson.toJSONString());
                    return;
                }
            }

            log.info(String.format("Get data completed, Record Count: %d", recordCount));

            resJson.put(CommonVariables.SERVER_TIMESTAMP, System.currentTimeMillis());
            resJson.put("record-count", recordCount);
            resJson.put("records", dataList);
            resp.setStatus(HttpStatus.OK_200);
            // resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().println(resJson.toJSONString());
        }
        catch (final Exception e)
        {
            log.error("Error Occurred", e);
            resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
            resJson.put(CommonVariables.STATUS_MESSAGE, "Unable to retrieve the data");
            resp.getWriter().println(resJson.toJSONString());
        }
        finally
        {
            if ((clientConnection != null))
                try
                {
                    if (!clientConnection.isClosed())
                        clientConnection.close();
                }
                catch (final Exception ex)
                {
                    log.error("Error while closing MariaDB Connection", ex);
                    ex.printStackTrace();
                }

            if ((clientPGConnection != null))
                try
                {
                    if (!clientPGConnection.isClosed())
                        clientPGConnection.close();
                }
                catch (final Exception ex2)
                {
                    log.error("Error while closing Posgresql DB Connection", ex2);
                    ex2.printStackTrace();
                }
        }
    }

}
