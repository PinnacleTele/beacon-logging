package com.itextos.beacon.commonlib.apperrorhandler.servlets;

import java.io.IOException;
import java.net.HttpURLConnection;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class BasicServlet
        extends
        HttpServlet
{

    private static final long serialVersionUID = -7625426500089875115L;

    private static final Log  log              = LogFactory.getLog(BasicServlet.class);

    public BasicServlet()
    {
        super();
    }

    
    @Override
    protected abstract void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException,
            IOException;

    @Override
    protected abstract void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException,
            IOException;

    @Override
    protected void doPut(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException,
            IOException
    {
        processErrorResponse("PUT", request, response);
    }

    @Override
    protected void doDelete(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException,
            IOException
    {
        processErrorResponse("DELETE", request, response);
    }

    @Override
    protected void doHead(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException,
            IOException
    {
        processErrorResponse("HEAD", request, response);
    }

    @Override
    protected void doOptions(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException,
            IOException
    {
        processErrorResponse("OPTIONS", request, response);
    }

    @Override
    protected void doTrace(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException,
            IOException
    {
        processErrorResponse("TRACE", request, response);
    }

    private static void processErrorResponse(
            String aMethodName,
            HttpServletRequest aRequest,
            HttpServletResponse aResponse)
            throws IOException,
            ServletException
    {
        if (log.isDebugEnabled())
            log.debug("Method called : '" + aRequest.getMethod() + "' Context path : '" + aRequest.getContextPath() + "' Requested from : '" + aRequest.getLocalAddr() + "' Path : '"
                    + aRequest.getPathInfo() + "' Query String : '" + aRequest.getQueryString() + "' ");
        aResponse.setStatus(HttpURLConnection.HTTP_BAD_METHOD);
        aRequest.setAttribute("javax.servlet.error.status_code", HttpURLConnection.HTTP_BAD_METHOD);
        final RequestDispatcher rd = aRequest.getRequestDispatcher("/errorservlet");
        rd.forward(aRequest, aResponse);
    }

    // ==============================================================================================================

    /*
     * *************************************
     * If required below methods can be
     * override by the sub classes. - START
     * *************************************
     */

    @Override
    public void init(
            ServletConfig config)
            throws ServletException
    {
        super.init(config);
    }

    @Override
    public void destroy()
    {
        super.destroy();
    }

    @Override
    public ServletConfig getServletConfig()
    {
        return super.getServletConfig();
    }

    @Override
    public String getServletInfo()
    {
        return super.getServletInfo();
    }

    /*
     * *************************************
     * If required below methods can be
     * override by the sub classes. - END
     * *************************************
     */

    // ==============================================================================================================

    /*
     * *****************************************************************************
     * Don't uncomment this below code. - START
     * Otherwise all the requests will fall into this method.
     * *****************************************************************************
     */

    // @Override
    // protected void service(HttpServletRequest request, HttpServletResponse
    // response) throws ServletException, IOException
    // {
    //
    // }

    /*
     * *****************************************************************************
     * Don't uncomment this above code. - END
     * *****************************************************************************
     */
}