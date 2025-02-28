package com.itextos.beacon.dlrquery.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.apperrorhandler.servlets.BasicServlet;
import com.itextos.beacon.dlrquery.reqresp.DlrQueryReqProcessor;

/**
 * Servlet implementation class DlrQueryRequest
 */
public class DlrQueryRequest
        extends
        BasicServlet
{

    private static final long serialVersionUID = 1L;

    private static Log        log              = LogFactory.getLog(DlrQueryRequest.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public DlrQueryRequest()
    {
        super();
    }

    

 
	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
        DlrQueryReqProcessor.processRequest(request, response);

		
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		doGet(request,response);
		
	}

}
