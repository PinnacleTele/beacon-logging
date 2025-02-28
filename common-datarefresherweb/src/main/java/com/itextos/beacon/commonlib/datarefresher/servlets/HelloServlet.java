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
@WebServlet("/hello")
public class HelloServlet
        extends
        BasicServlet
{

    /**
     *
     */
    private static final long serialVersionUID = 2733589991614429437L;
    // private static final long serialVersionUID = 1L;
    private static final Log  log              = LogFactory.getLog(HelloServlet.class);

    public HelloServlet()
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


        final PrintWriter lWriter = response.getWriter();
        lWriter.write("hello");
        lWriter.flush();
        lWriter.close();
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
