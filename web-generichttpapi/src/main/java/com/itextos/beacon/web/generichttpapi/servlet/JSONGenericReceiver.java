package com.itextos.beacon.web.generichttpapi.servlet;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.apperrorhandler.servlets.BasicServlet;
import com.itextos.beacon.commonlib.constants.InterfaceType;
import com.itextos.beacon.commonlib.prometheusmetricsutil.PrometheusMetrics;
import com.itextos.beacon.http.generichttpapi.common.utils.APIConstants;
import com.itextos.beacon.http.generichttpapi.common.utils.Utility;
import com.itextos.beacon.http.interfaceutil.MessageSource;
import com.itextos.beacon.interfaces.generichttpapi.processor.reader.JSONRequestReader;
import com.itextos.beacon.interfaces.generichttpapi.processor.reader.RequestReader;
import com.itextos.beacon.smslog.JSONReceiverLog;
import com.itextos.beacon.smslog.TimeTakenInterfaceLog;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class JSONGenericReceiver
        extends
        BasicServlet
{

    private static final long serialVersionUID = -4880462426737762823L;

    private static final Log  log              = LogFactory.getLog(JSONGenericReceiver.class);

    public JSONGenericReceiver()
    {}

    @Override
    protected void doGet(
            HttpServletRequest aRequest,
            HttpServletResponse aResponse)
            throws ServletException,
            IOException
    {
       

        StringBuffer sb=new StringBuffer();
        sb.append("\n##########################################\n");
        sb.append("JSONGenericReceiver request received in doGet").append("\n");
    
        final long start = System.currentTimeMillis();
        PrometheusMetrics.apiIncrementAcceptCount(InterfaceType.HTTP_JAPI, MessageSource.GENERIC_JSON, APIConstants.CLUSTER_INSTANCE, aRequest.getRemoteAddr());

        final RequestReader reader = new JSONRequestReader(aRequest, aResponse, MessageSource.GENERIC_JSON, null,sb);
        reader.processGetRequest();

        final long end    = System.currentTimeMillis();
        final long lProcessTaken = end - start;

        TimeTakenInterfaceLog.log("Request Start time : '" + Utility.getFormattedDateTime(start) + "' End time : '" + Utility.getFormattedDateTime(end) + "' Processing time : '" + lProcessTaken + "' milliseconds");

        sb.append("Request Start time : '" + Utility.getFormattedDateTime(start) + "' End time : '" + Utility.getFormattedDateTime(end) + "' Processing time : '" + lProcessTaken
                + "' milliseconds").append("\n");

        sb.append("\n##########################################\n");
        
        JSONReceiverLog.log(sb.toString());

    }

    @Override
    protected void doPost(
            HttpServletRequest aRequest,
            HttpServletResponse aResponse)
            throws ServletException,
            IOException
    {
        final long lProcessStart = System.currentTimeMillis();
        StringBuffer sb=new StringBuffer();

        try
        {

            sb.append("\n##########################################\n");
            sb.append("JSONGenericReceiver request received in doPost").append("\n");
       

            PrometheusMetrics.apiIncrementAcceptCount(InterfaceType.HTTP_JAPI, MessageSource.GENERIC_JSON, APIConstants.CLUSTER_INSTANCE, aRequest.getRemoteAddr());

            final RequestReader reader = new JSONRequestReader(aRequest, aResponse, MessageSource.GENERIC_JSON, null,sb);

            reader.processPostRequest();
        }
        catch (final Exception e)
        {
            log.error("Exception while processing request in Post method.", e);
        }
        finally
        {
            final long lProcessEnd = System.currentTimeMillis();
            final long lProcessTaken = lProcessEnd - lProcessStart;

            TimeTakenInterfaceLog.log("Request Start time : '" + Utility.getFormattedDateTime(lProcessStart) + "' End time : '" + Utility.getFormattedDateTime(lProcessEnd) + "' Processing time : '" + (lProcessEnd - lProcessStart)  + "' milliseconds");
            sb.append("Request Start time : '" + Utility.getFormattedDateTime(lProcessStart) + "' End time : '" + Utility.getFormattedDateTime(lProcessEnd) + "' Processing time : '" + lProcessTaken
                    + "' milliseconds").append("\n");

            sb.append("\n##########################################\n");
            
            JSONReceiverLog.log(sb.toString());
          
        }
    }

}
