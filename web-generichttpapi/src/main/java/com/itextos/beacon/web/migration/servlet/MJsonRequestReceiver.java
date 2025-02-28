package com.itextos.beacon.web.migration.servlet;

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

public class MJsonRequestReceiver
        extends
        BasicServlet
{

    private static final long serialVersionUID = -4880462426737762823L;

    private static final Log  log              = LogFactory.getLog(MJsonRequestReceiver.class);

    public MJsonRequestReceiver()
    {}

    @Override
    protected void doGet(
            HttpServletRequest aRequest,
            HttpServletResponse aResponse)
            throws ServletException,
            IOException
    {
        final long start = System.currentTimeMillis();

        try
        {
            if (log.isDebugEnabled())
                log.debug("Generic JSON  request received in doGet");

            PrometheusMetrics.apiIncrementAcceptCount(InterfaceType.HTTP_JAPI, MessageSource.GENERIC_JSON, APIConstants.CLUSTER_INSTANCE, aRequest.getRemoteAddr());
        	StringBuffer sb=new StringBuffer();

            final MRequestReader lMRequestReader = new MJsonRequestReader(aRequest, aResponse, MessageSource.GENERIC_JSON, MessageSource.GENERIC_JSON,sb);
            lMRequestReader.processPostRequest();
        }
        catch (final Exception e)
        {
            log.error("Exception while processing request in Post method.", e);
        }
        finally
        {
            final long end    = System.currentTimeMillis();
            final long result = end - start;

            if (log.isInfoEnabled())
                log.info("Request Start time : '" + Utility.getFormattedDateTime(start) + "' End time : '" + Utility.getFormattedDateTime(end) + "' Processing time : '" + result + "' milliseconds");
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest aRequest,
            HttpServletResponse aResponse)
            throws ServletException,
            IOException
    {
        final long lProcessStart = System.currentTimeMillis();

        try
        {
        	StringBuffer sb=new StringBuffer();

            if (log.isDebugEnabled())
                log.debug("M JSON request received in doPost");

            PrometheusMetrics.apiIncrementAcceptCount(InterfaceType.HTTP_JAPI, MessageSource.GENERIC_JSON, APIConstants.CLUSTER_INSTANCE, aRequest.getRemoteAddr());

            final MRequestReader lMRequestReader = new MJsonRequestReader(aRequest, aResponse, MessageSource.GENERIC_JSON, MessageSource.GENERIC_JSON,sb);
            lMRequestReader.processPostRequest();
        }
        catch (final Exception e)
        {
            log.error("Exception while processing request in Post method.", e);
        }
        finally
        {
            final long lProcessEnd = System.currentTimeMillis();

            if (log.isInfoEnabled())
                log.info("Request Start time : '" + Utility.getFormattedDateTime(lProcessStart) + "' End time : '" + Utility.getFormattedDateTime(lProcessEnd) + "' Processing time : '"
                        + (lProcessEnd - lProcessStart) + "' milliseconds");
        }
    }

}
