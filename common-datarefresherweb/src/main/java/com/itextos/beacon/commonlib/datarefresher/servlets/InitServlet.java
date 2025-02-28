package com.itextos.beacon.commonlib.datarefresher.servlets;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.itextos.beacon.commonlib.apperrorhandler.servlets.BasicServlet;
import com.itextos.beacon.commonlib.datarefresher.DataRefresher;

//@WebServlet(value = "/initservlet",        loadOnStartup = 1)
@WebServlet(loadOnStartup = 1)

public class InitServlet
        extends
        BasicServlet
{

    private static final long serialVersionUID = -896811591722186822L;

    public InitServlet()
    {
        super();
    }

    @Override
    public void init(
            ServletConfig config)
            throws ServletException
    {
        DataRefresher.getInstance();
    }

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		
	}

}