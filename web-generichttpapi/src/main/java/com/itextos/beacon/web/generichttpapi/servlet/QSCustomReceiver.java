package com.itextos.beacon.web.generichttpapi.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.apperrorhandler.servlets.BasicServlet;
import com.itextos.beacon.commonlib.constants.InterfaceType;
import com.itextos.beacon.commonlib.prometheusmetricsutil.PrometheusMetrics;
import com.itextos.beacon.http.generichttpapi.common.utils.APIConstants;
import com.itextos.beacon.http.generichttpapi.common.utils.Utility;
import com.itextos.beacon.http.interfaceutil.MessageSource;
import com.itextos.beacon.interfaces.generichttpapi.processor.reader.QSRequestReader;
import com.itextos.beacon.interfaces.generichttpapi.processor.reader.RequestReader;
import com.itextos.beacon.smslog.TimeTakenInterfaceLog;

public class QSCustomReceiver
        extends
        BasicServlet
{

    private static final long serialVersionUID = 4251095559542995977L;

    private static final Log  log              = LogFactory.getLog(QSCustomReceiver.class);

    @Override
    protected void doGet(
            HttpServletRequest aRequest,
            HttpServletResponse aResponse)
            throws ServletException,
            IOException
    {
        if (log.isDebugEnabled())
            log.debug("QS request received in doGet");

        StringBuffer sb=new StringBuffer();
        final long start = System.currentTimeMillis();
        PrometheusMetrics.apiIncrementAcceptCount(InterfaceType.HTTP_JAPI, MessageSource.GENERIC_QS, APIConstants.CLUSTER_INSTANCE, aRequest.getRemoteAddr());

        final RequestReader reader = new QSRequestReader(aRequest, aResponse, getServletName(), "custom",sb);
        reader.processGetRequest();

        final long end = System.currentTimeMillis();

        TimeTakenInterfaceLog.log("Request Start time : '" + Utility.getFormattedDateTime(start) + "' End time : '" + Utility.getFormattedDateTime(end) + "' Processing time : '" + (end-start) + "' milliseconds");
   }

    @Override
    protected void doPost(
            HttpServletRequest aRequest,
            HttpServletResponse aResponse)
            throws ServletException,
            IOException
    {
        if (log.isDebugEnabled())
            log.debug("QS request received in doPost");

        final long lProcessStart = System.currentTimeMillis();

        doGet(aRequest, aResponse);

        final long lProcessEnd   = System.currentTimeMillis();
        final long lProcessTaken = lProcessEnd - lProcessStart;

        if (log.isInfoEnabled())
            log.info("Request Start time : '" + Utility.getFormattedDateTime(lProcessStart) + "' End time : '" + Utility.getFormattedDateTime(lProcessEnd) + "' Processing time : '" + lProcessTaken
                    + "' milliseconds");
    }

}
