package com.itextos.beacon.queryprocessor.requestreceiver;

import java.io.IOException;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.http.HttpStatus;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.itextos.beacon.errorlog.QPRLog;
import com.itextos.beacon.queryprocessor.commonutils.CommonVariables;
import com.itextos.beacon.queryprocessor.commonutils.Utility;
import com.itextos.beacon.queryprocessor.databaseconnector.ConnectionPoolSingleton;
import com.itextos.beacon.queryprocessor.databaseconnector.DBConnectionProvider;
import com.itextos.beacon.queryprocessor.databaseconnector.SQLStatementExecutor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * log data from MARIADB or POSTGRES
 */
public class CreateLogDataQueue
        extends
        HttpServlet
{

    private static final QPRLog log           = QPRLog.getInstance();
    public static String                   API_NAME = CommonVariables.LOG_QUEUE_CREATE_API;
    private static ConnectionPoolSingleton connPool = null;

    @SuppressWarnings("unchecked")
    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException,
            IOException
    {
        final JSONObject resJson      = new JSONObject();
        Connection       masterDBConn = null;

        try
        {
            if (connPool == null)
                connPool = ConnectionPoolSingleton.getInstance();
            // add timestamp of the server
            resJson.put(CommonVariables.SERVER_TIMESTAMP, System.currentTimeMillis());
            final JSONParser parser  = new JSONParser();
            JSONObject       reqJson = null;

            try
            {
                reqJson = (JSONObject) parser.parse(IOUtils.toString(req.getReader()));
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

            final JSONObject        paramJson      = (JSONObject) reqJson.get(CommonVariables.R_PARAM);

            final String            DATE_FORMAT_S  = "yyyy-M-dd HH:mm:ss";
            final DateTimeFormatter formatWithS    = DateTimeFormatter.ofPattern(DATE_FORMAT_S);
            final LocalDateTime     paramStartTime = LocalDateTime
                    .parse(paramJson.get(CommonVariables.START_DATE).toString(), formatWithS);
            final LocalDateTime     paramEndTime   = LocalDateTime
                    .parse(paramJson.get(CommonVariables.END_DATE).toString(), formatWithS);

            final LocalDate         ldt_cur_dt     = LocalDate.now();

            if (paramStartTime.isAfter(paramEndTime))
            {
                log.info(String.format("Param Start Time: %s", Utility.formatDateTime(paramStartTime)));
                log.info(String.format("Param End Time: %s", Utility.formatDateTime(paramEndTime)));
                log.error("Start Time greater than End Time");
                resp.setStatus(HttpStatus.BAD_REQUEST_400);
                resJson.put(CommonVariables.STATUS_MESSAGE, "Start Time greater than End Time");
                resp.getWriter().println(resJson.toJSONString());
                return;
            }

            final LocalDate paramStartDate = paramStartTime.toLocalDate();
            final LocalDate paramEndDate   = paramEndTime.toLocalDate();

            final int       max_past_days  = Utility.getInteger(QueryEngine.mySQL_cfg_val.getProperty("maxPastDays"));
            final LocalDate ldt_max_old_dt = paramStartDate.plusDays(-max_past_days);

            if (ldt_max_old_dt.isAfter(paramStartDate))
            {
                log.info(String.format("Param Start Time: %s", Utility.formatDateTime(paramStartTime)));
                log.info(String.format("Param End Time: %s", Utility.formatDateTime(paramEndTime)));
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

            if (paramJson.containsKey("sort_by"))
            {
                final String sort_field = Utility.nullCheck(paramJson.get("sort_by"), true);

                if (!connPool.hmLog_DL_Col_Map.containsKey(sort_field))
                {
                    log.error("Invalid Sort Field: " + sort_field);
                    resp.setStatus(HttpStatus.BAD_REQUEST_400);
                    resJson.put(CommonVariables.STATUS_MESSAGE, "Invalid Sort Field: " + sort_field);
                    resp.getWriter().println(resJson.toJSONString());
                    return;
                }
            }

            masterDBConn = DBConnectionProvider.getMasterDBConnection();

            final SQLStatementExecutor dml = new SQLStatementExecutor();

            log.info("Creating queue");
            final String queue_id = dml.CreateQueue(masterDBConn, reqJson.get(CommonVariables.R_PARAM).toString(),
                    reqJson.get(CommonVariables.R_APP).toString(),
                    reqJson.get(CommonVariables.R_APP_VERSION).toString(),
                    reqJson.get(CommonVariables.R_USERNAME).toString(), req.getHeader("HOST").toString(),
                    CommonVariables.QUEUED);

            resJson.put("queue_id", queue_id);
            resp.getWriter().println(resJson.toJSONString());
            log.info("Queue created successfully. Queueu ID: " + queue_id);
        }
        catch (final Exception e)
        {
            resJson.put(CommonVariables.STATUS_MESSAGE, "Problem when creating the queue");
            resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
            resp.getWriter().println(resJson.toJSONString());
            log.error("Error Occurred", e);
            e.printStackTrace();
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
