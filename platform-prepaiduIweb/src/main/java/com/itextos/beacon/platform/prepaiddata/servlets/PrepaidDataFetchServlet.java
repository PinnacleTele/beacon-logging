package com.itextos.beacon.platform.prepaiddata.servlets;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.itextos.beacon.commonlib.apperrorhandler.servlets.BasicServlet;
import com.itextos.beacon.platform.prepaiddata.PrepaidData;
import com.itextos.beacon.platform.prepaiddata.ReadRedisData;

/**
 * Servlet implementation class PrepaidDataFetchServlet
 */
@WebServlet("/prepaiddatafetchservlet")
public class PrepaidDataFetchServlet
        extends
        BasicServlet
{

    private static final long   serialVersionUID   = 1L;
    private static final String CLI_ID_OR_USERNAME = "cliid_user";
    private static final String RESULT             = "result";
    private static final String DESTINATION        = "jsp/prepaiddata.jsp";

    /**
     * @see BasicServlet#BasicServlet()
     */
    public PrepaidDataFetchServlet()
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
        final String                        cliIdUserName  = request.getParameter(CLI_ID_OR_USERNAME);
        final ReadRedisData                 lReadRedisData = new ReadRedisData(cliIdUserName);
        final Map<String, Set<PrepaidData>> lData          = lReadRedisData.getData();
        final HttpSession                   session        = request.getSession();
        session.setAttribute(RESULT, lData);
        session.setAttribute(CLI_ID_OR_USERNAME, cliIdUserName);

        final RequestDispatcher dispatcher = request.getRequestDispatcher(DESTINATION);
        dispatcher.forward(request, response);
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
