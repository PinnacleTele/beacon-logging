package com.itextos.beacon.interfaces.generichttpapi.processor.request;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;

import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.Constants;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.InterfaceStatusCode;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.RouteType;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.http.generichttpapi.common.data.BasicInfo;
import com.itextos.beacon.http.generichttpapi.common.data.InterfaceMessage;
import com.itextos.beacon.http.generichttpapi.common.data.InterfaceRequestStatus;
import com.itextos.beacon.http.generichttpapi.common.data.xmlparser.Messagerequest;
import com.itextos.beacon.http.generichttpapi.common.data.xmlparser.Messagerequest.Messages;
import com.itextos.beacon.http.generichttpapi.common.data.xmlparser.Messagerequest.Messages.Destination;
import com.itextos.beacon.http.generichttpapi.common.data.xmlparser.Messagerequest.Messages.TemplateValues;
import com.itextos.beacon.http.generichttpapi.common.utils.APIConstants;
import com.itextos.beacon.http.generichttpapi.common.utils.Utility;
import com.itextos.beacon.http.interfaceutil.MessageSource;
import com.itextos.beacon.interfaces.generichttpapi.processor.handover.MiddlewareHandler;
import com.itextos.beacon.interfaces.generichttpapi.processor.response.GenerateXMLResponse;
import com.itextos.beacon.interfaces.generichttpapi.processor.validate.MessageValidater;

public class XMLRequestProcessor
        extends
        AbstractRequestProcessor
{

    private static final Log log          = LogFactory.getLog(XMLRequestProcessor.class);
    private List<Messages>   mMessageList = null;

    StringBuffer sb=null;
    public XMLRequestProcessor(
            String aRequestString,
            String aCustomerIP,
            long aRequestedTime,
            StringBuffer sb)
    {
        super(aRequestString, aCustomerIP, aRequestedTime, MessageSource.GENERIC_XML, MessageSource.GENERIC_XML);
        mResponseProcessor = new GenerateXMLResponse(aCustomerIP);
        mResponseProcessor.setServletContext("XMLReceiver");
        this.sb=sb;
    }

    @Override
    public void parseBasicInfo(
            String aAuthorization)
            throws ItextosException
    {

        try
        {
            final JAXBContext  jaxbContext      = JAXBContext.newInstance(Messagerequest.class);
            final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            final StringReader reader           = new StringReader(mRequestString);
            mParsedXML = (Messagerequest) jaxbUnmarshaller.unmarshal(reader);

            if (CommonUtility.nullCheck(mParsedXML.getAccesskey(), true).isBlank())
                if (aAuthorization != null)
                {
                    String key[] = null;

                    try
                    {
                        key = Utility.getAccessKey(aAuthorization);
                    }
                    catch (final Exception e)
                    {
                        log.error("Invalid authorization value");
                    }

                    if ((key != null) && (key.length == 2))
                    {
                        mParsedXML.setAccesskey(key[1]);
                        log.info("Username " + key[0]);
                    }
                }

            final StringWriter sw             = new StringWriter();
            final Marshaller   jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            jaxbMarshaller.marshal(mParsedXML, sw);
            mRequestString = sw.toString();
            xmlParser();
        }
        catch (final Exception e)
        {
            final String err = "Exception while parsing the XML. XMLString : '" + mRequestString + "'";
            log.error(err, e);
            throw new ItextosException(err, e);
        }
    }

    private void xmlParser()
    {
        final String lVersion      = CommonUtility.nullCheck(mParsedXML.getVersion(), true);
        final String lAccessKey    = CommonUtility.nullCheck(mParsedXML.getAccesskey(), true);
        final String lBatchNo      = CommonUtility.nullCheck(mParsedXML.getBatchno(), true);
        final String lEncrypt      = CommonUtility.nullCheck(mParsedXML.getEncrypt(), true);
        final String lScheduleTime = CommonUtility.nullCheck(mParsedXML.getScheduleTime(), true);

        mBasicInfo = new BasicInfo(lVersion, lAccessKey, lEncrypt, lScheduleTime, mCustIp, mRequestedTime);

        mBasicInfo.setBatchNo(lBatchNo);

        if (log.isDebugEnabled())
            log.debug("basicInfo:  '" + mBasicInfo + "'");
    }

    public void continueFromQueue(
            String aXmlString,
            String aFileID,
            String aClientId)
    {

        try
        {
            parseBasicInfo(null);

            mRequestString = aXmlString;

            xmlParser();

            Utility.setAccInfo(mBasicInfo, aClientId);

            final String lUserName = CommonUtility.nullCheck(mBasicInfo.getUserAccountInfo().get(MiddlewareConstant.MW_USER.getName()));

            mResponseProcessor.setUname(lUserName);

            final String lTimeZone   = CommonUtility.nullCheck(mBasicInfo.getUserAccountInfo().get(MiddlewareConstant.MW_TIME_ZONE.getName()));
            final String lScheduleAT = CommonUtility.nullCheck(mBasicInfo.getScheduleTime());

            if (!"".equals(lScheduleAT) && !"".equals(lTimeZone))
                Utility.changeScheduleTimeToGivenOffset(lTimeZone, mBasicInfo);

            mBasicInfo.setFileId(aFileID);

            getMessagesCount();

            getMultipleMessages(true);
        }
        catch (final Exception e)
        {
            log.error("Error While continuing from queue", e);
            pushKafkaTopic(MessageSource.GENERIC_XML);
        }
    }

    @Override
    public InterfaceMessage getSingleMessage(StringBuffer sb)
    {
        InterfaceMessage lMessage  = null;
        Messages         lMessages = null;

        try
        {
            lMessages = mMessageList.get(0);

            final Destination  lDestination      = lMessages.getDestination();
            final List<String> mMobileNumberList = lDestination.getDest();

            if (log.isDebugEnabled())
                log.debug("Destination List:  '" + mMobileNumberList + "'");

            if (mMobileNumberList.isEmpty())
            {
                if (log.isDebugEnabled())
                    log.debug("Destination list is empty:  '" + mMobileNumberList.size() + "' status '" + InterfaceStatusCode.DESTINATION_EMPTY + "'");

                final InterfaceRequestStatus status = new InterfaceRequestStatus(InterfaceStatusCode.DESTINATION_EMPTY, null);
                lMessage = getMessage(lMessages);
                lMessage.setRouteType(RouteType.DOMESTIC);
                lMessage.setRequestStatus(status);
            }
            else
                if (mMobileNumberList.size() == 1)
                {
                    final String lMobileNumber = mMobileNumberList.get(0);
                    lMessage = getMessage(lMessages);

                    final InterfaceStatusCode    lMessageValidationStatus = validateSingleMessage(lMessage, lMobileNumber);
                    final InterfaceRequestStatus requestStatus            = new InterfaceRequestStatus(lMessageValidationStatus, null);

                    if (log.isDebugEnabled())
                        log.debug("single message status:  '" + requestStatus + "'");

                    lMessage.setRequestStatus(requestStatus);
                }
                else
                {
                    if (log.isDebugEnabled())
                        log.debug("- Single message multiple destination  ");

                    final InterfaceRequestStatus reqStatus = getMultipleMessages(false);
                    lMessage = new InterfaceMessage();

                    if (reqStatus == null)
                        lMessage.setRequestStatus(new InterfaceRequestStatus(InterfaceStatusCode.SUCCESS, null));
                    else
                        lMessage.setRequestStatus(reqStatus);
                }
        }
        catch (final Exception e)
        {
            log.error("Exception while parsing messages: ", e);
            final InterfaceRequestStatus status = new InterfaceRequestStatus(InterfaceStatusCode.INVALID_XML, null);
            lMessage = new InterfaceMessage();
            lMessage.setRequestStatus(status);
        }

        return lMessage;
    }

    private InterfaceStatusCode validateSingleMessage(
            InterfaceMessage lMessage,
            String aMobileNumber)
            throws Exception
    {

        if (log.isDebugEnabled())
        {
            log.debug("mobile Number: '" + aMobileNumber + "'");
            log.debug("parse message:  '" + lMessage + "'");
        }

        final MessageValidater lMsgValidater            = new MessageValidater(lMessage, mBasicInfo,sb);

        // aMobileNumber = appendCountryCode(lMessage, aMobileNumber);

        InterfaceStatusCode    lMessageValidationStatus = lMsgValidater.validate();

        InterfaceStatusCode    lDestValidationStatus    = InterfaceStatusCode.SUCCESS;

        if (lMessageValidationStatus == InterfaceStatusCode.SUCCESS)
        {
            lDestValidationStatus = lMsgValidater.validateDest(aMobileNumber,sb);
            if (log.isDebugEnabled())
                log.debug("Mobile Number validation Status:  '" + lDestValidationStatus + "'");

            if (lDestValidationStatus != InterfaceStatusCode.SUCCESS)
                lMessage.setRouteType(RouteType.DOMESTIC);

            if (log.isDebugEnabled())
                log.debug("message object validation:  '" + lDestValidationStatus + "' lMessageValidationStatus '" + lMessageValidationStatus + "'");

            lMessageValidationStatus = lDestValidationStatus;
        }

        if (lMessageValidationStatus == InterfaceStatusCode.SUCCESS)
        {
            final String lScheduleTime = mBasicInfo.getScheduleTime();
            final Date   toCheck       = "".equals(lScheduleTime) ? new Date() : DateTimeUtility.getDateFromString(lScheduleTime, DateTimeFormat.DEFAULT);

            if (log.isDebugEnabled())
                log.debug("Date to check trai blockout:  '" + toCheck + "'");

            lMessageValidationStatus = lMsgValidater.validateTraiBlockOut(toCheck);
        }

        if (lMessageValidationStatus != InterfaceStatusCode.SUCCESS)
            lMessage.setRouteType(RouteType.DOMESTIC);

        Utility.setMessageId(lMessage);

        final MiddlewareHandler middlewareHandler = new MiddlewareHandler(lMessage, mBasicInfo, lMessageValidationStatus, lDestValidationStatus);
        middlewareHandler.middleWareHandover(false, mResponseProcessor, mReqType,sb);

        return lMessageValidationStatus;
    }

    @Override
    public InterfaceRequestStatus getMultipleMessages(
            boolean isAsync)
    {
        InterfaceRequestStatus reqStatus = null;

        try
        {
            InterfaceStatusCode clientAccessStatus = null;

            final String        cluster            = (String) mBasicInfo.getUserAccountInfo().get(MiddlewareConstant.MW_PLATFORM_CLUSTER.getName());

            if (("otp".equalsIgnoreCase(cluster)))
            {
                log.error("For OTP cluster, Multiplt message is not applicable.");

                if (isAsync)
                    clientAccessStatus = InterfaceStatusCode.ACCESS_VIOLATION;
                else
                    return reqStatus = new InterfaceRequestStatus(InterfaceStatusCode.ACCESS_VIOLATION, null);
            }

            final String schAt   = mBasicInfo.getScheduleTime();
            final Date   toCheck = "".equals(schAt) ? new Date() : DateTimeUtility.getDateFromString(schAt, DateTimeFormat.DEFAULT);

            if (log.isDebugEnabled())
                log.debug("Date to check trai blockout:  '" + toCheck + "'");

            for (final Messages messages : mMessageList)
            {

                /*
                 * if (mBasicInfo.getBatchNo() != null)
                 * messages.setMsgtag(mBasicInfo.getBatchNo());
                 */
                if (log.isDebugEnabled())
                {
                    log.debug("Mmessage list Length:  '" + mMessageList.size() + "'");
                    log.debug("xml message:  '" + messages + "'");
                }

                final InterfaceMessage message = getMessage(messages);

                if (log.isDebugEnabled())
                    log.debug("message Object:  '" + message + "'");

                final MessageValidater    validater             = new MessageValidater(message, mBasicInfo,sb);
                final InterfaceStatusCode messageValidateStatus = validater.validate();
                final InterfaceStatusCode lMiddlewareStatus     = getMiddlewareStatus(clientAccessStatus, messageValidateStatus);

                if (log.isDebugEnabled())
                    log.debug("ClientAccessStatus: '" + clientAccessStatus + " Message Validation : '" + messageValidateStatus + "' MiddlewareStatus : '" + lMiddlewareStatus + "'");

                final Destination  dest              = messages.getDestination();
                final List<String> mMobileNumberList = dest.getDest();
                handleForSingleMobileNumber(message, mMobileNumberList, toCheck, validater, lMiddlewareStatus, isAsync);
            }
        }
        catch (final Exception e)
        {
            log.error("Exception in multple message:  ", e);
            reqStatus = new InterfaceRequestStatus(InterfaceStatusCode.INTERNAL_SERVER_ERROR, null);
        }
        return reqStatus;
    }

    private void handleForSingleMobileNumber(
            InterfaceMessage aMessage,
            List<String> aMobileNumberList,
            Date aToCheck,
            MessageValidater aValidater,
            InterfaceStatusCode aMiddlewareStatus,
            boolean aIsAsync)
            throws Exception
    {

        for (final String number : aMobileNumberList)
        {
            if (log.isDebugEnabled())
                log.debug("mobile number:  '" + number + "'");

            InterfaceStatusCode destStatus = InterfaceStatusCode.SUCCESS;

            /*
             * if ((aMiddlewareStatus == InterfaceStatusCode.SUCCESS)) {
             */
            // number = appendCountryCode(aMessage, number);

            destStatus = aValidater.validateDest(number,sb);

            if (log.isDebugEnabled())
                log.debug("mobile number validation status:  '" + destStatus + "'");

            if (destStatus == InterfaceStatusCode.SUCCESS)
            {
                destStatus = aValidater.validateTraiBlockOut(aToCheck);

                if (log.isDebugEnabled())
                    log.debug("validate trai blockout status:  '" + destStatus + "'");
            }
            else
            {
                // Mobile Validation fail case setting RouteType is Domestic.
                aMessage.setRouteType(RouteType.DOMESTIC);

                aMessage.setMobileNumber(APIConstants.DEFAULT_DEST);
            }

            /*
             * }
             * else
             * aMessage.setMobileNumber(number);
             */
            if (log.isDebugEnabled())
            {
                log.debug("Send to Kafka");
                log.debug("Multiple message " + aMessage);
            }

            Utility.setMessageId(aMessage);

            final MiddlewareHandler middlewareHandler = new MiddlewareHandler(aMessage, mBasicInfo, aMiddlewareStatus, destStatus);
            middlewareHandler.middleWareHandover(aIsAsync, mResponseProcessor, mReqType,sb);
        }
    }

    private static InterfaceMessage getMessage(
            Messages aMessage)
    {
        final InterfaceMessage message = new InterfaceMessage();

        message.setMessage(aMessage.getMsg());
        message.setHeader(aMessage.getHeader());
        message.setMsgType(aMessage.getMsgtype());
        message.setDcs(CommonUtility.getInteger(aMessage.getDcs(), Constants.DEFAULT_ENTRY));
        // message.setUdhi(aMessage.getUdhi());
        // message.setUdh(aMessage.getUdh());
        message.setDestinationPort(CommonUtility.getInteger(aMessage.getPort()));
        message.setExpiry(aMessage.getExpiry());
        message.setAppendCountry(aMessage.getAppCountry());
        message.setCountryCode(aMessage.getCountryCd());
        message.setUrlTrack(aMessage.getUrltrack());
        message.setCustRef(aMessage.getCustRef());
        message.setTemplateId(aMessage.getTemplateId());

        if (message.getTemplateId() != null)
            setTemplateValues(aMessage, message);

        if (log.isDebugEnabled())
            log.debug("Message Tag value : " + aMessage.getMsgtag());

        message.setMsgTag(aMessage.getMsgtag());
        message.setDlrReq(CommonUtility.nullCheck(aMessage.getDlrReq()));
        message.setDltEntityId(aMessage.getDltentityid());
        message.setDltTemplateId(aMessage.getDlttemplateid());
   //     message.setDltTelemarketerId(aMessage.getDltTelemarketerid());

        message.setUrlShortner(aMessage.getUrlShortner());

        message.setParam1(aMessage.getParam1());
        message.setParam2(aMessage.getParam2());
        message.setParam3(aMessage.getParam3());
        message.setParam4(aMessage.getParam4());
        message.setParam5(aMessage.getParam5());
        message.setParam6(aMessage.getParam6());
        message.setParam7(aMessage.getParam7());
        message.setParam8(aMessage.getParam8());
        message.setParam9(aMessage.getParam9());
        message.setParam10(aMessage.getParam10());

        // message.setMessageScheduleTime(aMessage.);

        message.setRequestSource(MessageSource.GENERIC_XML);
        return message;
    }

    private static void setTemplateValues(
            Messages aMessages,
            InterfaceMessage aMessage)
    {

        try
        {
            final TemplateValues templateValues = aMessages.getTemplateValues();

            if (templateValues != null)
            {
                final List<String> templateList = templateValues.getValues();
                String[]           values       = null;

                if (templateList != null)
                {
                    final int size         = templateList.size();

                    final int templateSize = Utility.getConfigParamsValueAsInt(ConfigParamConstants.SMS_TEMPLATE_MAX_PARAMS);

                    values = new String[templateSize];

                    for (int i = 0; i < templateSize; i++)
                        if (i >= size)
                            values[i] = "";
                        else
                            values[i] = templateList.get(i);
                    aMessage.setTemplateValues(values);
                }
            }
        }
        catch (final Exception e)
        {
            log.info("Template values are empty..");
        }
    }

    @Override
    public String generateResponse()
    {
        return mResponseProcessor.generateResponse();
    }

    @Override
    public int getMessagesCount()
    {

        try
        {
            mMessageList = mParsedXML.getMessages();

            if (mMessageList != null)
                return mMessageList.size();
        }
        catch (final Exception e)
        {
            log.error("Message object is missing");
        }
        return 0;
    }

    @Override
    public int getNumbersCount(
            int aIndex)
    {

        try
        {
            return mMessageList.get(aIndex).getDestination().getDest().size();
        }
        catch (final Exception e)
        {
            // nothing to do
        }

        return 0;
    }

    @Override
    public void resetRequestJson(
            JSONObject aRequestJson)
    {
        // final JSONObject lJsonObject = new JSONObject(m_RequestString);
        mParsedJson = aRequestJson;
    }

    @Override
    public int getHttpStatus()
    {
        return HttpServletResponse.SC_OK;
    }

}