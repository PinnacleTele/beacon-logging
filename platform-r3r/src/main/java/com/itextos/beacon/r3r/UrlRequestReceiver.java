package com.itextos.beacon.r3r;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.apperrorhandler.servlets.BasicServlet;
import com.itextos.beacon.r3r.data.R3CUserInfo;
import com.itextos.beacon.r3r.process.UrlRedirectProcessor;

public class UrlRequestReceiver
        extends
        BasicServlet

{

    private static final long serialVersionUID = 298748163795929626L;
    private static final Log  log              = LogFactory.getLog(UrlRequestReceiver.class);

    @Override
    public void init(
            ServletConfig config)
            throws ServletException
    {}

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException,
            IOException
    {
        doPost(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException,
            IOException
    {
        response.setContentType("text/html");

        final String      lRequestUrl  = request.getRequestURI();
        final String      lUserAgent   = request.getHeader("User-Agent");
        final String      lIpAddress   = request.getRemoteAddr();

        final R3CUserInfo lR3cUserInfo = new R3CUserInfo(lRequestUrl, lUserAgent, lIpAddress, System.currentTimeMillis());

        if (log.isDebugEnabled())
            log.debug("Request Url is : -  '" + lRequestUrl + "' User Agent is : - '" + lUserAgent + "' IpAddress is : - '" + lIpAddress + "'");

        String lRedirectUrl = UrlRedirectProcessor.processRequestInfo(lR3cUserInfo);

        if (!lRedirectUrl.startsWith("http"))
        {
            final String errorCode = lRedirectUrl;
            if (log.isDebugEnabled())
                log.debug("Error Code is : " + errorCode);

            lRedirectUrl = request.getContextPath() + "/index.jsp";
            final HttpSession lSession = request.getSession();
            lSession.setAttribute("errorcode", errorCode);
            lSession.setAttribute("receivedurl", lRequestUrl);

            if (log.isDebugEnabled())
                log.debug("Redirecting to '" + lRedirectUrl + "'");

            final RequestDispatcher despatcher = request.getRequestDispatcher(lRedirectUrl);
            despatcher.forward(request, response);
        }
        else
        {
            if (log.isDebugEnabled())
                log.debug("URL going to Redirect : -  '" + lRedirectUrl);
            response.sendRedirect(lRedirectUrl);
        }
    }

}