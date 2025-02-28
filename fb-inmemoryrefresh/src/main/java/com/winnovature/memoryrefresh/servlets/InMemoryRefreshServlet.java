package com.winnovature.memoryrefresh.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.utils.singletons.ConfigParamsTon;

public class InMemoryRefreshServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	static Log log = LogFactory.getLog("MemoryRefreshLogger");
	private static final String className = "InMemoryRefreshServlet";

	public InMemoryRefreshServlet() {
		super();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String logName = className + " [doGet] ";

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		String reqtype = request.getParameter("reqtype");
		if (log.isDebugEnabled()) {
			log.debug(logName + " begin, reqtype = " + reqtype);
		}
		if (StringUtils.isBlank(reqtype)) {
			displayMenu(response);
		} else {
			reqtype = reqtype.trim();
			if (reqtype.equalsIgnoreCase("CP")) {
				try {
					ConfigParamsTon.getInstance().reload();
					out.println("<font color='green'>Config params reloaded</font>");
				} catch (Exception e) {
					log.error(logName + "Exception: ", e);
					out.println("<font color='red'>Exception while reloading Config Params table.</font>");
				}
			} else {
				displayMenu(response);
			}

			out.flush();
			out.close();
		}

		if (log.isDebugEnabled()) {
			log.debug(logName + "end ..");
		}
	}

	private void displayMenu(HttpServletResponse response) {
		PrintWriter out = null;

		try {
			out = response.getWriter();
			response.setContentType("text/html");
			out.print(
					"<B> http://IP:PORT/FP-InMemoryRefresh-0.0.1/memoryrefresh?reqtype={here use any one of below keys....}</B>");
			out.print("<br><ul>");
			out.print("<li>CP - Config Param &nbsp;&nbsp;(TABLE NAME = CONFIG_PARAMS)</li></ul>");
			// out.print("<li>RC - Redis Connection &nbsp;&nbsp;(TABLE NAME =
			// REDIS_INFO_UPFP)</li>");
			out.print("</ul>");

		} catch (IOException e) {
			log.error(className + "[displayMenu]", e);
		} finally {
			if (out != null)
				out.close();
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

}
