package com.itextos.beacon.commonlib.shortcode.servlet;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.itextos.beacon.commonlib.apperrorhandler.servlets.BasicServlet;
import com.itextos.beacon.commonlib.shortcodeprovider.RedisShortCodeChecker;

/**
 * Servlet implementation class InitServlet
 */
@WebServlet(
        value = "/initservlet",
        loadOnStartup = 1)
public class InitServlet
        extends
        BasicServlet
{

    private static final long serialVersionUID = -5284455511324473556L;

    /**
     * @see BasicServlet#BasicServlet()
     */
    public InitServlet()
    {
        new RedisShortCodeChecker();
    }

    /**
     * @see Servlet#init(ServletConfig)
     */
    @Override
    public void init(
            ServletConfig config)
            throws ServletException
    {}

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
    {
        doGet(request, response);
    }

}