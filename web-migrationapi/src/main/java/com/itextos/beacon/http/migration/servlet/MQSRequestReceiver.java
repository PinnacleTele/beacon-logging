package com.itextos.beacon.http.migration.servlet;

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
import com.itextos.beacon.interfaces.migration.processor.reader.MJsonRequestReader;
import com.itextos.beacon.interfaces.migration.processor.reader.MRequestReader;

public class MQSRequestReceiver
        extends
        BasicServlet
{

    private static final long serialVersionUID = -4880462426737762823L;

    private static final Log  log              = LogFactory.getLog(MQSRequestReceiver.class);

    public MQSRequestReceiver()
    {}

    @Override
    protected void doGet(
            HttpServletRequest aRequest,
            HttpServletResponse aResponse)
            throws ServletException,
            IOException
    {
    	StringBuffer sb=new StringBuffer();

        if (log.isDebugEnabled())
            log.debug("QS request received in doGet");

        final long lProcessStart = System.currentTimeMillis();
        PrometheusMetrics.apiIncrementAcceptCount(InterfaceType.MIGRATION_API, MessageSource.GENERIC_QS, APIConstants.CLUSTER_INSTANCE, aRequest.getRemoteAddr());
        final MRequestReader lMRequestReader = new MJsonRequestReader(aRequest, aResponse, MessageSource.GENERIC_QS, MessageSource.GENERIC_QS,sb);
        lMRequestReader.processGetRequest();

        final long lProcessEnd   = System.currentTimeMillis();
        final long lProcessTaken = lProcessEnd - lProcessStart;

        if (log.isInfoEnabled())
            log.info("Request Start time : '" + Utility.getFormattedDateTime(lProcessStart) + "' End time : '" + Utility.getFormattedDateTime(lProcessEnd) + "' Processing time : '" + lProcessTaken
                    + "' milliseconds");
    }

    @Override
    protected void doPost(
            HttpServletRequest aRequest,
            HttpServletResponse aResponse)
            throws ServletException,
            IOException
    {
        doGet(aRequest, aResponse);
    }

}
