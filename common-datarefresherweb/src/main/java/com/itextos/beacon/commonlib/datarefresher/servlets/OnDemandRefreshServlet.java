package com.itextos.beacon.commonlib.datarefresher.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.apperrorhandler.servlets.BasicServlet;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.datarefresher.DataRefresher;
import com.itextos.beacon.commonlib.datarefresher.dataobjects.DataOperation;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;

/**
 * Servlet implementation class OnDemandRefreshServlet
 */
@WebServlet("/ondemandrefresh")
public class OnDemandRefreshServlet
        extends
        BasicServlet
{

    /**
     *
     */
    private static final long serialVersionUID = 2733589991614429437L;
    // private static final long serialVersionUID = 1L;
    private static final Log  log              = LogFactory.getLog(OnDemandRefreshServlet.class);

    public OnDemandRefreshServlet()
    {
        super();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException,
            IOException
    {
        final String lParameter = request.getParameter("tablename");

        if (log.isDebugEnabled())
            log.debug("Data to be refreshed for the table '" + lParameter + "'");

        final Map<String, Map<DataOperation, Integer>> lCheckForDataChange = DataRefresher.getInstance().checkForDataChange(lParameter);

        if (log.isDebugEnabled())
            log.debug("Result for the table '" + lCheckForDataChange + "'");

        final StringBuilder sb = new StringBuilder();
        sb.append("<html><title>Data Refresh Result</title>");
        sb.append("<br><Table align='center' border ='1'>");
        sb.append("<tr><th align='center'>Table Name</th><th align='center'>Insert</th><th align='center'>Update</th><th align='center'>Delete</th></tr>");

        if (lCheckForDataChange.isEmpty())
            sb.append("<tr><td align='center' colspan='4'>Nothing to refresh</td></th>");
        else
        {
            final Set<String>  lKeySet = lCheckForDataChange.keySet();
            final List<String> keys    = new ArrayList<>(lKeySet);
            Collections.sort(keys);

            for (final String tableName : keys)
            {
                sb.append("<tr><td>").append(tableName).append("</td>");

                final Map<DataOperation, Integer> counts = lCheckForDataChange.get(tableName);

                Integer                           value  = counts.get(DataOperation.INSERT);
                addColumnValue(sb, value);

                value = counts.get(DataOperation.UPDATE);
                addColumnValue(sb, value);

                value = counts.get(DataOperation.DELETE);
                addColumnValue(sb, value);
            }
        }
        sb.append("</table>");
        sb.append("<br><br><body>Data Refreshed for the table '").append(lParameter).append("' at ").append(DateTimeUtility.getFormattedCurrentDateTime(DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS));
        sb.append("</body></html>");

        final PrintWriter lWriter = response.getWriter();
        lWriter.write(sb.toString());
        lWriter.flush();
        lWriter.close();
    }

    private static void addColumnValue(
            StringBuilder aStringBuilder,
            Integer aValue)
    {
        if ((aValue == null) || (aValue < 0))
            aStringBuilder.append("<td align='center'>").append(aValue == null ? "-" : "Failed");
        else
            aStringBuilder.append("<td align='Right'>").append(aValue);
        aStringBuilder.append("</td>");
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException,
            IOException
    {
        doGet(request, response);
    }

}
