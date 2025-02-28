package com.itextos.beacon.platform.dnr.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.apperrorhandler.servlets.BasicServlet;
import com.itextos.beacon.platform.dnr.process.DlrProcess;

public class DLRReceiver
        extends
        BasicServlet
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final Log  log              = LogFactory.getLog(DLRReceiver.class);

    @Override
    protected void doGet(
            HttpServletRequest aRequest,
            HttpServletResponse aResponse)
            throws ServletException,
            IOException
    {

        try
        {
            DlrProcess.doProcess(aRequest);
        }
        catch (final Exception e)
        {
            log.error("DLRReceiver : doProcess()", e);
            throw new ServletException(e.getMessage());
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest aRequest,
            HttpServletResponse aResponse)
            throws ServletException,
            IOException
    {
        doGet(aRequest, aResponse);
    }

}
