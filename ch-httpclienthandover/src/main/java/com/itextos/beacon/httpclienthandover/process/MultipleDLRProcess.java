package com.itextos.beacon.httpclienthandover.process;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.httpclient.HttpResult;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.httpclienthandover.data.ClientHandoverData;
import com.itextos.beacon.httpclienthandover.data.ClientHandoverMaster;
import com.itextos.beacon.httpclienthandover.data.URLResult;
import com.itextos.beacon.httpclienthandover.utils.ClientHandoverUtils;
import com.itextos.beacon.httpclienthandover.utils.TopicSenderUtility;
import com.itextos.beacon.smslog.payLoadReceiverLog;

public class MultipleDLRProcess
        extends
        AbstractDLRProcess
{
	
	private static final String className="com.itextos.beacon.httpclienthandover.process.MultipleDLRProcess";

    private static final Log log = LogFactory.getLog(MultipleDLRProcess.class);

    private final String     customerId;

    public MultipleDLRProcess(
            String aCustomerId)
    {
        customerId = aCustomerId;
    }

    @Override
    public void processDLR(
            List<BaseMessage> aMessageList)
    {
        final ClientHandoverData clientHandoverData = ClientHandoverUtils.getClientHandoverData(customerId);

        if (clientHandoverData == null)
        {
            log.error("Client Handover Configuration Missing for customer ID : '" + customerId + "'");

            sendToMasterLogWithError(aMessageList);
            return;
        }

        final int batchsize = clientHandoverData.getBatchSize();
        startBatchProcess(aMessageList, batchsize, clientHandoverData);
    }

    private static void sendToMasterLogWithError(
            List<BaseMessage> aMessageList)
    {
        final UUID        uniqueId     = UUID.randomUUID();

        final BaseMessage masterRecord = aMessageList.get(0).getClonedObject();
        masterRecord.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_UNIQUE_ID, "" + uniqueId);
        masterRecord.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_MASTER_RECORD, "" + 1);

        final HttpResult failResult = ClientHandoverUtils.getCustomResult("No Client Configuration found for " + masterRecord.getValue(MiddlewareConstant.MW_CLIENT_ID), -999);

        ClientHandoverUtils.setResultInMessage(masterRecord, failResult);
        TopicSenderUtility.sendToMasterLogQueue(masterRecord);

        for (final BaseMessage message : aMessageList)
        {
            message.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_UNIQUE_ID, "" + uniqueId);
            TopicSenderUtility.sendToMasterLogQueue(message);
        }

        aMessageList.clear();
    }

    private void startBatchProcess(
            List<BaseMessage> aMessageList,
            int aBatchsize,
            ClientHandoverData aClientConfiguration)
    {
        final List<ClientHandoverMaster> customerEndPointInfos = aClientConfiguration.getClientHandoverMaster();
        final List<BaseMessage>          processedMessage      = new ArrayList<>();

        for (final ClientHandoverMaster customerEndPoint : customerEndPointInfos)
        {
            StringJoiner sJoiner = new StringJoiner(customerEndPoint.getBatchBodyDelimiter(), customerEndPoint.getBodyHeader(), customerEndPoint.getBodyFooter());

            for (final BaseMessage message : aMessageList)
            {
            	StringBuffer sb=new StringBuffer();
            	
            	payLoadReceiverLog.log(message.getJsonString());
            	
                message.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_IS_BATCH, "1");

                doWeNeedToCheckLatency(message);

                final String processedTemplate = processTemplate(customerEndPoint, message,sb);
                
                sb.append("processedTemplate : ").append(processedTemplate).append("\t").append(className).append("\n");
               
                sJoiner.add(processedTemplate);

                processedMessage.add(message);

                if (processedMessage.size() >= aBatchsize)
                {
                    processBatch(processedMessage, aClientConfiguration, customerEndPoint, sJoiner);
                    processedMessage.clear();

                    sJoiner = new StringJoiner(customerEndPoint.getBatchBodyDelimiter(), customerEndPoint.getBodyHeader(), customerEndPoint.getBodyFooter());
                }
            }

            if (!processedMessage.isEmpty())
                processBatch(processedMessage, aClientConfiguration, customerEndPoint, sJoiner);
        }
    }

    private static void processBatch(
            List<BaseMessage> processedMessage,
            ClientHandoverData aClientConfiguration,
            ClientHandoverMaster customerEndPoint,
            StringJoiner sJoiner)
    {
        long      httpProcessStartTime = System.currentTimeMillis();
        URLResult httpResult           = processHTTPRequest(sJoiner.toString(), customerEndPoint, customerEndPoint.getPrimaryUrl());
        long      httpProcessEndTime   = System.currentTimeMillis();

        long      totalHttpProcessTime = httpProcessEndTime - httpProcessStartTime;

        String    startTime            = DateTimeUtility.getFormattedDateTime(httpProcessStartTime, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS);
        String    endTime              = DateTimeUtility.getFormattedDateTime(httpProcessEndTime, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS);

        if (log.isDebugEnabled())
            log.debug("HttpResult : '" + httpResult + "' | Request Start time: '" + startTime + "' | EndTime: '" + endTime + "'");

        final boolean retrySecondaryUrl = (!ClientHandoverUtils.isHttpProcessSuccess(httpResult.getHttpResult()) && StringUtils.isNotEmpty(customerEndPoint.getSecondaryUrl()));

        if (retrySecondaryUrl)
        {
            processRecordBasedOnResult(httpResult, processedMessage, aClientConfiguration, totalHttpProcessTime, startTime, endTime, sJoiner.toString(), true);

            httpProcessStartTime = System.currentTimeMillis();
            httpResult           = processHTTPRequest(sJoiner.toString(), customerEndPoint, customerEndPoint.getSecondaryUrl());
            httpProcessEndTime   = System.currentTimeMillis();

            totalHttpProcessTime = httpProcessEndTime - httpProcessStartTime;

            startTime            = DateTimeUtility.getFormattedDateTime(httpProcessStartTime, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS);
            endTime              = DateTimeUtility.getFormattedDateTime(httpProcessEndTime, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS);

            processRecordBasedOnResult(httpResult, processedMessage, aClientConfiguration, totalHttpProcessTime, startTime, endTime, sJoiner.toString(), false);
        }
        else
            processRecordBasedOnResult(httpResult, processedMessage, aClientConfiguration, totalHttpProcessTime, startTime, endTime, sJoiner.toString(), false);

        processedMessage.clear();
    }

}