package com.itextos.beacon.commonlib.apperrorhandler.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class ErrorServlet
 */
public class ErrorServlet
        extends
        HttpServlet
{

    private static final long serialVersionUID = -1524473640435853560L;

    public ErrorServlet()
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
            sb.append("</p></td></tr>");
            sb.append("<tr><td><p class='regular'>Status Code</p></td><td align='center'>:</td><td><font color='red'><p class='regular'>").append(statusCode).append("</p></font></td></tr>");
            sb.append("<tr><td><p class='regular'>Description</p></td><td align='center'>:</td><td><p class='regular'>").append(ErrorCodeStatus.getErrorDescription(statusCode + ""))
                    .append("</p></td></tr>");
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