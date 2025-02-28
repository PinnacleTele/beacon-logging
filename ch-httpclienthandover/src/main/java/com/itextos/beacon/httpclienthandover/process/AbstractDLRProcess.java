package com.itextos.beacon.httpclienthandover.process;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.httpclient.HTTPRequestUtility;
import com.itextos.beacon.commonlib.httpclient.HttpHeader;
import com.itextos.beacon.commonlib.httpclient.HttpResult;
import com.itextos.beacon.commonlib.httpclient.helper.HttpConstants;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.httpclienthandover.data.ClientHandoverData;
import com.itextos.beacon.httpclienthandover.data.ClientHandoverHeaderParams;
import com.itextos.beacon.httpclienthandover.data.ClientHandoverMaster;
import com.itextos.beacon.httpclienthandover.data.ClientHandoverParams;
import com.itextos.beacon.httpclienthandover.data.HttpMethod;
import com.itextos.beacon.httpclienthandover.data.URLResult;
import com.itextos.beacon.httpclienthandover.retry.RedisPusher;
import com.itextos.beacon.httpclienthandover.utils.ClientHandoverConstatnts;
import com.itextos.beacon.httpclienthandover.utils.ClientHandoverUtils;
import com.itextos.beacon.httpclienthandover.utils.LogStatusEnum;
import com.itextos.beacon.httpclienthandover.utils.TopicSenderUtility;
import com.itextos.beacon.inmemory.configvalues.ApplicationConfiguration;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.smslog.DNHttpPostLog;

public abstract class AbstractDLRProcess
        implements
        IDLRProcess
{

    private static final Log log = LogFactory.getLog(AbstractDLRProcess.class);
    private static final Logger logger = LoggerFactory.getLogger(AbstractDLRProcess.class);

    public static String processTemplate(
            ClientHandoverMaster aCustomerEndPoint,
            BaseMessage aMessage,StringBuffer sb)
    {
        final List<ClientHandoverParams> aClientHandoverParams = aCustomerEndPoint.getClientHandoverParams();
        final boolean                    isEncodingRequired    = ClientHandoverUtils.checkIfEncodingRequired(aCustomerEndPoint);
        final StringBuilder              template              = ClientHandoverUtils.getTemPlateBuilder(aCustomerEndPoint);

        sb.append("template : ").append(template).append("\t").append("\n");
        
        for (final ClientHandoverParams handoverParams : aClientHandoverParams)
        {
            final String defaultValue = handoverParams.getDefaultValue();
            final String replaceKey   = "{" + handoverParams.getParamSeqNo() + "}";
            String       finalValue   = defaultValue;

            try
            {
                finalValue = ClientHandoverUtils.getValueFromTheConstant(handoverParams, aMessage);
            }
            catch (final Exception e)
            {
                log.error("Exception while getting the http param value from constant. Handover Params " + handoverParams + " template " + template + " Message " + aMessage, e);
            }

            try
            {
                ClientHandoverUtils.getReplacedStringBuffer(template, replaceKey, finalValue, isEncodingRequired);
            }
            catch (final Exception e)
            {
                log.error("Exception parsing the template. Handover Params " + handoverParams + " template " + template + " Message " + aMessage, e);
            }
        }
        return template.toString();
    }

    public void doWeNeedToCheckLatency(
            BaseMessage message)
    {}

    public static void processHex2StringConvert(
            BaseMessage message)
    {

        try
        {

            if (((Boolean.parseBoolean(message.getValue(MiddlewareConstant.MW_IS_HEX_MSG)) //
                    && (message.getValue(MiddlewareConstant.MW_LONG_MSG) != null)) //
                    && "UC".equalsIgnoreCase(message.getValue(MiddlewareConstant.MW_MSG_CLASS))) //
                    || ClientHandoverUtils.isHexadecimal(message.getValue(MiddlewareConstant.MW_LONG_MSG)))
            {
                final String convertedMsg = ClientHandoverUtils.convertHexToString(message.getValue(MiddlewareConstant.MW_LONG_MSG));
                message.putValue(MiddlewareConstant.MW_LONG_MSG, convertedMsg);
            }
        }
        catch (final Exception e)
        {
            log.error("Invalid Hex String for fullMsg : " + message.getValue(MiddlewareConstant.MW_LONG_MSG), e);
        }
    }

    public static URLResult processHTTPRequest(
            String aTemplate,
            ClientHandoverMaster aCustomerEndPoint,
            String aUrl)
    {
        DNHttpPostLog.log(AbstractDLRProcess.class + " processHTTPRequest() incoming  - " + aTemplate + ", " + aCustomerEndPoint + ", " + aUrl);
        logger.debug("{} processHTTPRequest() incoming  - {}, {}, {}", AbstractDLRProcess.class, aTemplate, aCustomerEndPoint, aUrl);

        final List<ClientHandoverHeaderParams> headerParams = aCustomerEndPoint.getClientHandoverHeaderParams();
        final HttpMethod                       requestType  = aCustomerEndPoint.getHttpMethod();
        final HttpHeader<String, String>       headerMap    = generateHeaderMap(headerParams, aCustomerEndPoint);

        final URLResult                        httpResult   = hitUrl(requestType, aUrl, aTemplate, headerMap);

        DNHttpPostLog.log(httpResult.toString());
        
        return httpResult;
    }

    public static URLResult hitUrl(
            HttpMethod requestType,
            String aUrl,
            String aTemplate,
            HttpHeader<String, String> aHeaderMap)
    {
        String     completeUrl = null;
        HttpResult result;

        switch (requestType)
        {
            case GET:
                completeUrl = ClientHandoverUtils.getCompleteURL(aUrl, aTemplate);
                DNHttpPostLog.log(AbstractDLRProcess.class + " hitUrl() GET complete url  - " + completeUrl);
                logger.debug(AbstractDLRProcess.class + " hitUrl() GET complete url  - " + completeUrl);

                result = HTTPRequestUtility.processGetRequest(aUrl, completeUrl, aHeaderMap);
                break;

            case POST:
                completeUrl = aTemplate;
                DNHttpPostLog.log(AbstractDLRProcess.class + " hitUrl() POST complete url  - " + completeUrl);
                logger.debug(AbstractDLRProcess.class + " hitUrl() POST complete url  - " + completeUrl);

                result = HTTPRequestUtility.doPostRequest(aUrl, aHeaderMap, aTemplate);
                break;

            case POSTQS:
                completeUrl = ClientHandoverUtils.getCompleteURL(aUrl, aTemplate);
                result = HTTPRequestUtility.doPostRequestQueryString(aUrl, completeUrl, aHeaderMap);
                break;

            default:
                final HttpResult defaultResult = new HttpResult();
                defaultResult.setSuccess(false);
                defaultResult.setStatusCode(-999);
                defaultResult.setErrorString("Unknown RequestType");
                defaultResult.setException(new ItextosException("Unknows RequestType"));
                defaultResult.setResponseString("Unknow Request Type");
                result = defaultResult;
                break;
        }

        return new URLResult(aUrl, completeUrl, result);
    }

    public static void addResultAndTimeToBaseMessage(
            BaseMessage aMessage,
            URLResult httpResult,
            long aTotalHttpProcessTime,
            boolean aIsBatch,
            String aStartTime,
            String aEndTime,
            String aTemplate)
    {
        aMessage.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_CLIENT_URL, httpResult.getUrl());

        aMessage.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_HTTP_STATUS_CODE, "" + httpResult.getHttpResult().getStatusCode());
        aMessage.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_HTTP_RESPONSE_CONTENT, httpResult.getHttpResult().getResponseString());
        aMessage.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_REQUEST_CONTENT, aTemplate);
        aMessage.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_HTTP_RESPONSE_TIME, "" + aTotalHttpProcessTime);
        aMessage.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_IS_BATCH, "" + (aIsBatch ? 1 : 0));
        aMessage.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_START_TIME, aStartTime);
        aMessage.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_END_TIME, aEndTime);
    }

    public static void processRecordBasedOnResult(
            URLResult aHttpResult,
            List<BaseMessage> aMessageList,
            ClientHandoverData aClientConfiguration,
            long aTotalHttpProcessTime,
            String aStartTime,
            String aEndTime,
            String aTemplate,
            boolean aIsOnlyLog)
    {
        final UUID  uniqueId     = UUID.randomUUID();
        BaseMessage masterRecord = null;

        try
        {
            masterRecord = aMessageList.get(0).getClonedObject();
        }
        catch (final Exception e)
        {
            //
        }

        if (log.isDebugEnabled())
            log.debug("Log Table UniqueID '" + uniqueId + "'");

        if (masterRecord != null)
        {
            masterRecord.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_UNIQUE_ID, "" + uniqueId);
            masterRecord.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_MASTER_RECORD, "" + 1);

            addResultAndTimeToBaseMessage(masterRecord, aHttpResult, aTotalHttpProcessTime, true, aStartTime, aEndTime, aTemplate);
            processRecordBasedOnResult(aHttpResult, masterRecord, aClientConfiguration, true, true, aIsOnlyLog);
        }

        for (final BaseMessage message : aMessageList)
        {
            message.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_UNIQUE_ID, "" + uniqueId);
            if (masterRecord.getValue(MiddlewareConstant.MW_CLIENT_HANDOVER_RETRY_CHECK) != null)
                message.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_RETRY_CHECK, masterRecord.getValue(MiddlewareConstant.MW_AALPHA));
            processRecordBasedOnResult(aHttpResult, message, aClientConfiguration, true, false, aIsOnlyLog);
            message.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_RETRY_CHECK, null);
        }
    }

    public static void processRecordBasedOnResult(
            URLResult aHttpResult,
            BaseMessage aMessage,
            ClientHandoverData aClientConfiguration,
            boolean isCustomerSpecific,
            boolean isMainTable,
            boolean aOnlyLog)
    {
        final ApplicationConfiguration applicationConfiguration = (ApplicationConfiguration) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.APPLICATION_CONFIG);

        final boolean                  logRetryAttempt          = aClientConfiguration.isLogRetryAttempt()
                && "1".equals(applicationConfiguration.getConfigValue(ClientHandoverConstatnts.GLOBAL_LOG_RETRY_ATTEMPT));

        // TODO KP sir need to check in the HttpClient | change to isSuccess
        if (ClientHandoverUtils.isHttpProcessSuccess(aHttpResult.getHttpResult()))
        {
            if (CommonUtility.getInteger(aMessage.getValue(MiddlewareConstant.MW_CLIENT_HANDOVER_MAX_RETRY_COUNT)) > 0)
                aMessage.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_LOG_STATUS, LogStatusEnum.RETRY_SUCCESS.name());
            else
                aMessage.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_LOG_STATUS, LogStatusEnum.SUCCESS.name());
            if (log.isDebugEnabled())
                log.debug("Http Status Success | sending to Log Queue | ClientID: ''" + aClientConfiguration.getClientId() + "'");
            sendToLogQueue(true, aMessage, isCustomerSpecific, isMainTable);
        }
        else
            if (ClientHandoverUtils.isReryEnabled(aClientConfiguration))
            {
                final boolean doNotUpdateCount = (aOnlyLog || !(StringUtils.isNotEmpty(aClientConfiguration.getClientHandoverMaster().get(0).getSecondaryUrl())));// TODO change logic

                final boolean isExpired        = ClientHandoverUtils.checkIfExpired(aMessage, aClientConfiguration, doNotUpdateCount);
                if (log.isDebugEnabled())
                    log.debug("Retry  enabled for this customer | ClientID: ''" + aClientConfiguration.getClientId() + "' | Message Expired: '" + isExpired + "' | isMainTable: '" + isMainTable + "'");

                if (isExpired
                        && ((aMessage.getValue(MiddlewareConstant.MW_CLIENT_HANDOVER_RETRY_CHECK) == null) || (aMessage.getValue(MiddlewareConstant.MW_CLIENT_HANDOVER_RETRY_CHECK) != "notExpired")))
                {
                    if (log.isDebugEnabled())
                        log.debug(" ClientID: ''" + aClientConfiguration.getClientId() + "' | Message expired sending to log queue");
                    aMessage.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_LOG_STATUS, LogStatusEnum.RETRY_EXPIRED.name());
                    aMessage.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_RETRY_CHECK, "Expired");

                    sendToLogQueue(true, aMessage, isCustomerSpecific, isMainTable);
                    return;
                }
                aMessage.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_LOG_STATUS, LogStatusEnum.RETRY_FAILED.name());

                if (isMainTable || aOnlyLog)
                {
                    aMessage.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_RETRY_CHECK, "notExpired");

                    sendToLogQueue(logRetryAttempt, aMessage, isCustomerSpecific, isMainTable);
                    return;
                }

                if (log.isDebugEnabled())
                    log.debug("Adding into Retry Queue | ClientID: ''" + aClientConfiguration.getClientId() + "'");

                aMessage.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_RETRY_DATA_MESSAGE, aMessage.getJsonString());

                TopicSenderUtility.sendToRetryQueue(aMessage);

                if (isCustomerSpecific)
                    RedisPusher.getInstance().addCustomerQueue(aMessage);
                else
                    RedisPusher.getInstance().add(aMessage);

                sendToLogQueue(logRetryAttempt, aMessage, isCustomerSpecific, isMainTable);
            }
            else
            {
                if (log.isDebugEnabled())
                    log.debug("Http Status Failed |Retry Not enabled for this customer | sending to Log Queue | ClientID: ''" + aClientConfiguration.getClientId() + "'");

                aMessage.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_LOG_STATUS, LogStatusEnum.FAILED.name());
                sendToLogQueue(true, aMessage, isCustomerSpecific, isMainTable);
            }
    }

    private static void sendToLogQueue(
            boolean logRetryAttempt,
            BaseMessage aMessage,
            boolean aIsCustomerSpecific,
            boolean aIsMainTable)
    {

        try
        {
            if (log.isDebugEnabled())
                log.debug("LogRetry Attempt is: " + logRetryAttempt + "Message Object Going to Kafka : " + aMessage.toString() + " Customer Specific Topic: " + aIsCustomerSpecific + " Main Table is "
                        + aIsMainTable);

            if (logRetryAttempt)

                if (aIsMainTable)
                    TopicSenderUtility.sendToMasterLogQueue(aMessage);
                else
                    TopicSenderUtility.sendToChildLogQueue(aMessage);
        }
        catch (final Exception exec)
        {
            log.error("Exception while send to Topic: " + exec);
        }
    }

    private static void setTimeout(
            HttpHeader<String, String> aHeaders,
            ClientHandoverMaster aCustomerEndPoint)
    {
        aHeaders.put(HttpConstants.CONNECTION_TIMEOUT, "" + aCustomerEndPoint.getConWaitTimeoutMills());
        aHeaders.put(HttpConstants.SOCKET_TIMEOUT, "" + aCustomerEndPoint.getReadTimeoutMills());
    }

    private static HttpHeader<String, String> generateHeaderMap(
            List<ClientHandoverHeaderParams> aHeaderParams,
            ClientHandoverMaster aCustomerEndPoint)
    {
        final HttpHeader<String, String> headers = new HttpHeader<>();

        if (aHeaderParams != null)
            for (final ClientHandoverHeaderParams headerParam : aHeaderParams)
                headers.put(headerParam.getHeaderParamName(), headerParam.getHeaderParamValue());
        setTimeout(headers, aCustomerEndPoint);

        return headers;
    }

    protected static void updateMetaData(
            BaseMessage aMessage)
    {
        final String retryCount     = ClientHandoverUtils.getIntegerWithInc(aMessage, MiddlewareConstant.MW_CLIENT_HANDOVER_RETRY_COUNT, "0");
        final String retryTime      = CommonUtility.nullCheck(aMessage.getValue(MiddlewareConstant.MW_CLIENT_HANDOVER_RETRY_TIME), true);
        final String startTime      = CommonUtility.nullCheck(aMessage.getValue(MiddlewareConstant.MW_CLIENT_HANDOVER_START_TIME), true);
        final String attemptedCount = ClientHandoverUtils.getIntegerWithInc(aMessage, MiddlewareConstant.MW_CLIENT_HANDOVER_ATTEMPTED_COUNT, "1");

        aMessage.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_RETRY_COUNT, retryCount);
        aMessage.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_RETRY_TIME, retryTime);

        aMessage.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_ATTEMPTED_COUNT, attemptedCount);
        aMessage.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_START_TIME, "".equals(startTime) ? DateTimeUtility.getFormattedCurrentDateTime(DateTimeFormat.DEFAULT) : startTime);
    }

}
