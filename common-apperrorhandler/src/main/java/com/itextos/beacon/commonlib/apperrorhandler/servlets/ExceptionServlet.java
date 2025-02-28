package com.itextos.beacon.commonlib.apperrorhandler.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class ErrorServlet
 */
public class ExceptionServlet
        extends
        HttpServlet
{

    /**
     *
     */
    private static final long serialVersionUID = 4533936357233028490L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ExceptionServlet()
    {
        super();
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException,
            IOException
    {

        try (
                final PrintWriter writer = response.getWriter();)
        {
            final int statusCode = (int) request.getAttribute("javax.servlet.error.status_code");
            response.setStatus(statusCode);

            final StringBuilder sb = new StringBuilder();
            sb.append(ErrorCodeStatus.getHTMLHeader());
            sb.append(new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss.SSS z").format(new Date()));
            sb.append("</td></tr>");

            final Throwable exception = (Throwable) request.getAttribute("javax.servlet.error.exception");

            if (exception != null)
            {
                sb.append("<tr><td><p class='regular'>Exception</p></td><td align='center'>:</td><td><font color='red'><p class='regular'>").append(exception.getMessage())
                        .append("</p></font></td></tr>");
                sb.append("<tr><td><p class='regular'>Stack Trace</p></td><td align='center'>:</td><td><p class='regular'>").append(getErrorString(exception)).append("</p></td></tr>");
            }
            else
                sb.append("<tr><td><p class='regular'>Reason</p></td><td align='center'>:</td><td><font color='red'><p class='regular'>Unknown</p></font></td></tr>");
            sb.append("</table></Font></body></html>");
            response.setContentType("text/html");

            writer.print(sb.toString());
            writer.flush();
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

    private static String getErrorString(
            Throwable exception)
    {
        final StringWriter errors = new StringWriter();
        exception.printStackTrace(new PrintWriter(errors));
        String s = errors.toString();
        s = s.replace("\n", "<br>");
        s = s.replace("at ", "&nbsp;&nbsp;&nbsp;&nbsp;at ");
        return s;
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     *      response)
     */
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