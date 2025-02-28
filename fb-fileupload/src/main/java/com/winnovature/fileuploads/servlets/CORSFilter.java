package com.winnovature.fileuploads.servlets;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.fileuploads.utils.Constants;

@WebFilter(urlPatterns = { "/*" })
public class CORSFilter implements Filter {

	static Log logger = LogFactory.getLog(Constants.FileUploadLogger);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {

		HttpServletResponse response = (HttpServletResponse) res;
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, OPTIONS, DELETE");
		response.setHeader("Access-Control-Max-Age", "3600");
		response.setHeader("Access-Control-Allow-Headers", "*");
		//response.setHeader("Access-Control-Expose-Headers", "Authorization");
		final HttpServletRequest request = (HttpServletRequest) req;
		
		if (!request.getMethod().equals("OPTIONS")) {
            chain.doFilter(req, res);
        }

	}

	@Override
	public void destroy() {
	}

}
