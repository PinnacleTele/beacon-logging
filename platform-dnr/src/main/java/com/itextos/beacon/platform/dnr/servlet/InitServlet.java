package com.itextos.beacon.platform.dnr.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.itextos.beacon.commonlib.apperrorhandler.servlets.BasicServlet;
import com.itextos.beacon.commonlib.prometheusmetricsutil.PrometheusMetrics;
import com.itextos.beacon.platform.dnrfallback.inmem.DlrFallbackQReaper;

/**
 * Servlet implementation class InitServlet
 */
public class InitServlet
        extends
        BasicServlet
{

    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     */
    public InitServlet()
    {
        super();
    }

    @Override
    public void init(
            ServletConfig config)
            throws ServletException
    {
    	com.itextos.beacon.App.createfolder();

        PrometheusMetrics.registerServer();
        PrometheusMetrics.registerApiMetrics();
        DlrFallbackQReaper.getInstance();
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
    {}

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
    {}

}