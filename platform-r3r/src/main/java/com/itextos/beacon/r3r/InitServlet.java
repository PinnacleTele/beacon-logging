package com.itextos.beacon.r3r;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.itextos.beacon.commonlib.apperrorhandler.servlets.BasicServlet;
import com.itextos.beacon.commonlib.prometheusmetricsutil.PrometheusMetrics;
import com.itextos.beacon.r3r.process.DataProcessor;

/**
 * Servlet implementation class InitServlet
 */
public class InitServlet
        extends
        BasicServlet
{

    private static final long serialVersionUID = 4450981324098534891L;

    public InitServlet()
    {
        super();
    }

    @Override
    public void init(
            ServletConfig config)
            throws ServletException
    {
        PrometheusMetrics.registerServer();
        PrometheusMetrics.registerApiMetrics();
  
        DataProcessor.getInstance();
    }

    @Override
    public void destroy()
    {
        DataProcessor.getInstance().stopMe();
    }

    @Override
    protected void doGet(
            HttpServletRequest aRequest,
            HttpServletResponse aResponse)
            throws ServletException,
            IOException
    {}

    @Override
    protected void doPost(
            HttpServletRequest aRequest,
            HttpServletResponse aResponse)
            throws ServletException,
            IOException
    {}

}