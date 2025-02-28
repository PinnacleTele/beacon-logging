package com.itextos.beacon.clienthandover.accept;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.apperrorhandler.servlets.BasicServlet;

/**
 * Servlet implementation class HandoverRequestAccepter
 */
@WebServlet("/handoverrequest")
public class HandoverRequestAccepter
        extends
        BasicServlet
{

    private static Log        log              = LogFactory.getLog(HandoverRequestAccepter.class);
    private static final long serialVersionUID = -4405192610434694240L;

    /**
     * @see BasicServlet#BasicServlet()
     */
    public HandoverRequestAccepter()
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
        if (log.isDebugEnabled())
            log.debug("Received Request : '" + request.getQueryString() + "'");

        int                 status  = HttpServletResponse.SC_ACCEPTED;
        final StringBuilder sb      = new StringBuilder();

        String              isError = request.getParameter("iserror");
        isError = isError == null ? "null" : isError.toUpperCase();

        if ("TRUE1YES".contains(isError))
        {
            status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            sb.append("Internal server error occured by " + new Date());
        }
        else
            sb.append("Served at: ").append(request.getContextPath()).append(" Request : ").append(request.getQueryString());

        response.setStatus(status);

        try (
                final PrintWriter lWriter = response.getWriter();)
        {
            lWriter.append(sb.toString());
        }
        catch (final IOException ioe)
        {}
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
