package com.itextos.beacon.http.cloudacceptor.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.itextos.beacon.http.cloudacceptor.common.CloudAcceptorUtility;

public class CloudAcceptorJsonServlet
        extends
        HttpServlet
{

    private static final long serialVersionUID = -5883454141554893293L;

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
        CloudAcceptorUtility.processJsonRequest(request, response);
    }

}
