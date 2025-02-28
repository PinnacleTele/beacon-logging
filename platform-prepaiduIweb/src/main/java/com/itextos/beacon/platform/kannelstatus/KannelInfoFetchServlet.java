package com.itextos.beacon.platform.kannelstatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.apperrorhandler.servlets.BasicServlet;
import com.itextos.beacon.platform.prepaiddata.kannelstatus.KannelInfo;
import com.itextos.beacon.platform.prepaiddata.kannelstatus.KannelInfoLoader;
import com.itextos.beacon.platform.prepaiddata.kannelstatus.ReadKannelInfo;

@WebServlet("/kannelinfofetchservlet")
public class KannelInfoFetchServlet
        extends
        BasicServlet
{

    private static final Log    log         = LogFactory.getLog(KannelInfoFetchServlet.class);

    private static final String DESTINATION = "jsp/kannelinfo.jsp";

    public KannelInfoFetchServlet()
    {
        super();
        KannelInfoLoader.getInstance();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException,
            IOException
    {
        log.debug("QS : '" + request.getQueryString() + "'");

        String                        refresh              = request.getParameter("refresh");
        String[]                      lRequestOperator     = request.getParameterValues("operator");
        String[]                      lRequestRoutes       = request.getParameterValues("route");

        List<String>                  lRequestOperatorList = null;
        List<String>                  lRequestRoutesList   = null;
        Map<String, List<KannelInfo>> kannelResult         = null;

        try
        {
            refresh          = request.getParameter("refresh");
            lRequestOperator = request.getParameterValues("operator");
            lRequestRoutes   = request.getParameterValues("route");

            if (lRequestOperator != null)
                lRequestOperatorList = Arrays.asList(lRequestOperator);

            if (lRequestRoutes != null)
                lRequestRoutesList = Arrays.asList(lRequestRoutes);

            if (log.isDebugEnabled())
            {
                log.debug("refresh            '" + (refresh != null ? refresh : "<NULL>") + "'");
                log.debug("Selected Operators '" + (lRequestOperator != null ? Arrays.asList(lRequestOperator) : "<NULL>") + "'");
                log.debug("Selected Routes    '" + (lRequestRoutes != null ? Arrays.asList(lRequestRoutes) : "<NULL>") + "'");
            }

            lRequestOperatorList = lRequestOperatorList == null ? new ArrayList<>() : lRequestOperatorList;
            lRequestRoutesList   = lRequestRoutesList == null ? new ArrayList<>() : lRequestRoutesList;

            kannelResult         = ReadKannelInfo.getRouteStatusFromInmemory(lRequestOperatorList, lRequestRoutesList);
        }
        finally
        {
            final HttpSession lSession = request.getSession(true);

            request.setAttribute("refresh", refresh);
            lSession.setAttribute("operators", lRequestOperatorList);
            lSession.setAttribute("routes", lRequestRoutesList);
            lSession.setAttribute("kannelinfo", kannelResult);

            final RequestDispatcher dispatcher = request.getRequestDispatcher(DESTINATION);
            dispatcher.forward(request, response);
        }
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
