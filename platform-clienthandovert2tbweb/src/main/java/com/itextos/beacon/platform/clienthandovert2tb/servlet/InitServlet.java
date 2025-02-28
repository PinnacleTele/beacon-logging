package com.itextos.beacon.platform.clienthandovert2tb.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.apperrorhandler.servlets.BasicServlet;
import com.itextos.beacon.platform.clienthandovert2tb.StartApplication;

/**
 * Servlet implementation class InitServlet
 */
public class InitServlet
        extends
        BasicServlet
{

    private static final Log  log              = LogFactory.getLog(InitServlet.class);

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
        StartApplication.main(null);
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