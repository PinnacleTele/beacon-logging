package com.itextos.beacon.interfaces.generichttpapi.processor.reader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.InterfaceStatusCode;
import com.itextos.beacon.commonlib.constants.InterfaceType;
import com.itextos.beacon.commonlib.prometheusmetricsutil.PrometheusMetrics;
import com.itextos.beacon.http.generichttpapi.common.data.InterfaceMessage;
import com.itextos.beacon.http.generichttpapi.common.data.InterfaceRequestStatus;
import com.itextos.beacon.http.generichttpapi.common.interfaces.IRequestProcessor;
import com.itextos.beacon.http.generichttpapi.common.utils.APIConstants;
import com.itextos.beacon.http.generichttpapi.common.utils.InterfaceInputParameters;
import com.itextos.beacon.http.interfaceutil.MessageSource;
import com.itextos.beacon.interfaces.generichttpapi.processor.request.XMLRequestProcessor;
import com.itextos.beacon.interfaces.generichttpapi.processor.validate.XMLValidation;

import io.prometheus.client.Histogram.Timer;

public class XMLRequestReader
        extends
        AbstractReader
{

    private static final Log log = LogFactory.getLog(XMLRequestReader.class);

    StringBuffer sb;
    
    public XMLRequestReader(
            HttpServletRequest aRequest,
            HttpServletResponse aResponse,
            StringBuffer sb)
    {
        super("xml", aRequest, aResponse);
        
        this.sb=sb;
    }

    @Override
    public void doProcess(
            String aXmlString)
    {
        if (log.isDebugEnabled())
            log.debug(" XML process() Customer IP:  '" + mHttpRequest.getRemoteAddr() + "' Request Received Time: '" + System.currentTimeMillis() + "'");

        String lUserName      = "";

        Timer  xmlProcess     = null;
        Timer  overAllProcess = null;

        try
        {
            overAllProcess = PrometheusMetrics.apiStartTimer(InterfaceType.HTTP_JAPI, MessageSource.GENERIC_XML, APIConstants.CLUSTER_INSTANCE, mHttpRequest.getRemoteAddr(), OVERALL);

            XMLValidation.getInstance().isXMLValid(aXmlString);
            final IRequestProcessor requestProcessor = new XMLRequestProcessor(aXmlString, mHttpRequest.getRemoteAddr(), System.currentTimeMillis(),sb);

            requestProcessor.parseBasicInfo(mHttpRequest.getHeader(InterfaceInputParameters.AUTHORIZATION));
            final InterfaceRequestStatus reqStatus = requestProcessor.validateBasicInfo();

            lUserName  = getUserName(requestProcessor);

            xmlProcess = PrometheusMetrics.apiStartTimer(InterfaceType.HTTP_JAPI, MessageSource.GENERIC_XML, APIConstants.CLUSTER_INSTANCE, mHttpRequest.getRemoteAddr(), lUserName);

            /*
            if (requestProcessor.getBasicInfo().isIsAsync()) {
                doAsyncProcess(requestProcessor, reqStatus);
            }
            else {
                doSyncProcess(requestProcessor, reqStatus, aXmlString, lUserName);
            }
            */
            
            doSyncProcess(requestProcessor, reqStatus, aXmlString, lUserName);

        }
        catch (final Exception e)
        {
            log.error("Excception while processig XML request:" + aXmlString, e);
            handleException(aXmlString, e);
        }
        finally
        {
            PrometheusMetrics.apiEndTimer(InterfaceType.HTTP_JAPI, APIConstants.CLUSTER_INSTANCE, xmlProcess);
            PrometheusMetrics.apiEndTimer(InterfaceType.HTTP_JAPI, APIConstants.CLUSTER_INSTANCE, overAllProcess);
        }
    }

    private void doSyncProcess(
            IRequestProcessor aRequestProcessor,
            InterfaceRequestStatus aReqStatus,
            String aXmlString,
            String aUser)
    {
        final int aMessagesCount = aRequestProcessor.getMessagesCount();

        if (aReqStatus.getStatusCode() == InterfaceStatusCode.SUCCESS)
        {
            for (int i = 0; i < aMessagesCount; i++)
                PrometheusMetrics.apiIncrementAcceptCount(InterfaceType.HTTP_JAPI, MessageSource.GENERIC_XML, APIConstants.CLUSTER_INSTANCE, mHttpRequest.getRemoteAddr(), aUser);

            if (aMessagesCount == 0)
            {
                if (log.isDebugEnabled())
                    log.debug("messageCount==0:  '" + InterfaceStatusCode.INVALID_XML + "' Message Object Missing Invalid XML Req:  " + aXmlString);

                handleNoMessage(aRequestProcessor, aReqStatus);
            }
            else
            {
                if (log.isDebugEnabled())
                    log.debug("Process valid messages");

                if ((aRequestProcessor.getBasicInfo().getClusterType() == ClusterType.OTP) && (aMessagesCount > 1))
                {
                    if (log.isDebugEnabled())
                        log.debug("Requested Message Count is greather than  '" + aMessagesCount + "' for cluster '" + ClusterType.OTP + "', Heance rejecting the request.");

                    handleAccessViolationMessage(aRequestProcessor, aReqStatus);
                }
                else
                    if (aMessagesCount == 1)
                        processSingleMessage(aRequestProcessor, aReqStatus,sb);
                    else
                        processMultipleMessages(aRequestProcessor, aReqStatus);
            }
        }
        sendResponse(aRequestProcessor);
    }
/*
    private void doAsyncProcess(
            IRequestProcessor aRequestProcessor,
            InterfaceRequestStatus aReqStatus)
    {
        final int    aMessagesCount = aRequestProcessor.getMessagesCount();
        final String messageId      = aReqStatus.getMessageId();
        final String aUser          = getUserName(aRequestProcessor);

        if (aReqStatus.getStatusCode() == InterfaceStatusCode.SUCCESS)
        {
            for (int i = 0; i < aMessagesCount; i++)
                PrometheusMetrics.apiIncrementAcceptCount(InterfaceType.HTTP_JAPI, MessageSource.GENERIC_XML, APIConstants.CLUSTER_INSTANCE, mHttpRequest.getRemoteAddr(), aUser);

            aReqStatus = new InterfaceRequestStatus(InterfaceStatusCode.SUCCESS, "");
            aRequestProcessor.setRequestStatus(aReqStatus);

            aReqStatus.setMessageId(messageId);

            sendResponse(aRequestProcessor);

            aRequestProcessor.pushKafkaTopic(MessageSource.GENERIC_XML);
        }
        else
            sendResponse(aRequestProcessor);
    }
*/
    private static void handleNoMessage(
            IRequestProcessor aRequestProcessor,
            InterfaceRequestStatus aReqStatus)
    {
        aReqStatus = new InterfaceRequestStatus(InterfaceStatusCode.INVALID_XML, "Message Object Missing");
        aRequestProcessor.setRequestStatus(aReqStatus);
    }

    private static void handleAccessViolationMessage(
            IRequestProcessor aRequestProcessor,
            InterfaceRequestStatus aReqStatus)
    {
        aReqStatus = new InterfaceRequestStatus(InterfaceStatusCode.ACCESS_VIOLATION, "OTP account should not allow batch request");
        aRequestProcessor.setRequestStatus(aReqStatus);
    }

    private static void processMultipleMessages(
            IRequestProcessor aRequestProcessor,
            InterfaceRequestStatus aReqStatus)
    {
        final String messageId = aReqStatus.getMessageId();
        // multiple messages
        if (log.isDebugEnabled())
            log.debug(" process() MultipleMessage:  '" + InterfaceStatusCode.SUCCESS + "'");

        aReqStatus = aRequestProcessor.getMultipleMessages(false);

        if (aReqStatus == null)
        {
            aReqStatus = new InterfaceRequestStatus(InterfaceStatusCode.SUCCESS, "");
            aReqStatus.setMessageId(messageId);
        }
        aRequestProcessor.setRequestStatus(aReqStatus);
    }

    private static void processSingleMessage(
            IRequestProcessor aRequestProcessor,
            InterfaceRequestStatus aReqStatus,
            StringBuffer sb)
    {
        final InterfaceMessage message   = aRequestProcessor.getSingleMessage(sb);
        final String           messageId = aReqStatus.getMessageId();

        if (message == null)
        {
            aReqStatus = new InterfaceRequestStatus(InterfaceStatusCode.SUCCESS, "");
            aReqStatus.setMessageId(messageId);
            aRequestProcessor.setRequestStatus(aReqStatus);
        }
        else
        {
            if (log.isDebugEnabled())
                log.debug(" process() singleMessage:  '" + message.getRequestStatus() + "'");
            aReqStatus = message.getRequestStatus();

            aReqStatus.setMessageId(messageId);
            aRequestProcessor.setRequestStatus(aReqStatus);
        }
    }

    private void handleException(
            String aXmlString,
            Exception aException)
    {
        final IRequestProcessor      requestProcessor = new XMLRequestProcessor(aXmlString, mHttpRequest.getRemoteAddr(), System.currentTimeMillis(),sb);
        final InterfaceRequestStatus status           = new InterfaceRequestStatus(InterfaceStatusCode.INVALID_XML, aException.getMessage());
        requestProcessor.setRequestStatus(status);
        sendResponse(requestProcessor);
    }

    @Override
    public void setContentType()
    {
        mHttpResponse.setContentType(InterfaceInputParameters.RES_CONTENT_TYPE_XML);
    }

    @Override
    public void setContentLength(
            String aResponse)
    {
        mHttpResponse.setContentLength(aResponse.length());
    }

    @Override
    public void doProcess(
            JSONObject aJsonObj)
    {
        // TODO Auto-generated method stub
    }

}