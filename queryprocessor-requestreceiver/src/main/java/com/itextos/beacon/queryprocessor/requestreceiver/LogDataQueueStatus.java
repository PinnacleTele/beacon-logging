package com.itextos.beacon.queryprocessor.requestreceiver;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.http.HttpStatus;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.itextos.beacon.errorlog.QPRLog;
import com.itextos.beacon.queryprocessor.commonutils.CommonVariables;
import com.itextos.beacon.queryprocessor.commonutils.Utility;
import com.itextos.beacon.queryprocessor.databaseconnector.DBConnectionProvider;
import com.itextos.beacon.queryprocessor.databaseconnector.ResultSetConverter;
import com.itextos.beacon.queryprocessor.databaseconnector.SQLStatementExecutor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * log data from MARIADB or POSTGRES
 */
public class LogDataQueueStatus
        extends
        HttpServlet
{

    private static final QPRLog log           = QPRLog.getInstance();
    public static String     API_NAME      = CommonVariables.LOG_QUEUE_STATUS_API;
    static Pattern           queid_pattern = Pattern
            .compile("\\b[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}\\b");

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
        // add timestamp of the server
        resJson.put(CommonVariables.SERVER_TIMESTAMP, System.currentTimeMillis());

        try
        {
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

            final String queue_id = Utility.nullCheck(reqJson.get(CommonVariables.QUEUE_ID), true);

            if ("".equals(queue_id))
            {
                final String errMsg = "Parameter " + CommonVariables.QUEUE_ID + " not found";
                log.error(errMsg);
                resp.setStatus(HttpStatus.BAD_REQUEST_400);
                resJson.put(CommonVariables.STATUS_MESSAGE, errMsg);
                resp.getWriter().println(resJson.toJSONString());
                return;
            }

            if (!queid_pattern.matcher(queue_id).matches())
            {
                final String errMsg = "Invalid format of " + CommonVariables.QUEUE_ID + ": " + queue_id;
                log.error(errMsg);
                resp.setStatus(HttpStatus.BAD_REQUEST_400);
                resJson.put(CommonVariables.STATUS_MESSAGE, errMsg);
                resp.getWriter().println(resJson.toJSONString());
                return;
            }

            masterDBConn = DBConnectionProvider.getMasterDBConnection();
            final ResultSet  rs_queue_status = SQLStatementExecutor.getQueueStatusInfo(masterDBConn, queue_id);

            final JSONObject que_status      = ResultSetConverter.ConvertRsToJSONObject(rs_queue_status, null, "");

            final Statement  stmt            = rs_queue_status.getStatement();
            rs_queue_status.close();
            stmt.close();

            if (que_status == null)
            {
                final String errMsg = CommonVariables.QUEUE_ID + ": " + queue_id + " not found";
                log.info(errMsg);
                resJson.put(CommonVariables.STATUS_MESSAGE, errMsg);
                resp.setStatus(HttpStatus.BAD_REQUEST_400);
            }
            else
            {
                resJson.put("queue_status", que_status);
                resp.setStatus(HttpStatus.OK_200);
            }
            resp.getWriter().println(resJson.toJSONString());
        }
        catch (

        final Exception e)
        {
            log.error("Error Occurred", e);
            resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
            resJson.put(CommonVariables.STATUS_MESSAGE, "Unable to retrieve Queue information");
            resp.getWriter().println(resJson.toJSONString());
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
