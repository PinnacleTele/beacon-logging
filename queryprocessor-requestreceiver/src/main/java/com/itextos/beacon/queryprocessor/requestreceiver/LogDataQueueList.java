package com.itextos.beacon.queryprocessor.requestreceiver;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.http.HttpStatus;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.itextos.beacon.errorlog.QPRLog;
import com.itextos.beacon.queryprocessor.commonutils.CommonVariables;
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
public class LogDataQueueList
        extends
        HttpServlet
{

    private static final QPRLog log           = QPRLog.getInstance();
    public static String     API_NAME = CommonVariables.LOG_QUEUE_LIST_API;

    /**
     *
     */
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
            int pageNo = 1;

            if (reqJson.get(CommonVariables.PAGE_NO) != null)
                pageNo = Integer.parseInt(String.valueOf(reqJson.get(CommonVariables.PAGE_NO)));

            final JSONArray queList = new JSONArray();

            log.info("Getting queue list");
            masterDBConn = DBConnectionProvider.getMasterDBConnection();
            final ResultSet rs_pending_queue = SQLStatementExecutor.getQueueStatusList(masterDBConn, pageNo);
            final JSONArray ja               = ResultSetConverter.ConvertRsToJSONArray(rs_pending_queue, null, "");
            if (ja != null)
                queList.addAll(ja);

            resJson.put("queue_list", queList);

            final Statement stmt = rs_pending_queue.getStatement();
            rs_pending_queue.close();
            stmt.close();

            // add timestamp of the server
            resJson.put(CommonVariables.SERVER_TIMESTAMP, System.currentTimeMillis());
            resp.setStatus(HttpStatus.OK_200);
            resp.getWriter().println(resJson.toJSONString());
        }
        catch (final Exception e)
        {
            log.error("Error Occurred", e);
            resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
            resJson.put(CommonVariables.STATUS_MESSAGE, "Unable to retrieve Pending Queue List information");
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
