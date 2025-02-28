package com.itextos.beacon.interfaces.generichttpapi.processor.request;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.InterfaceStatusCode;
import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.messageidentifier.MessageIdentifier;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.http.generichttpapi.common.data.BasicInfo;
import com.itextos.beacon.http.generichttpapi.common.data.InterfaceRequestStatus;
import com.itextos.beacon.http.generichttpapi.common.data.QueueObject;
import com.itextos.beacon.http.generichttpapi.common.data.xmlparser.Messagerequest;
import com.itextos.beacon.http.generichttpapi.common.interfaces.IRequestProcessor;
import com.itextos.beacon.http.generichttpapi.common.interfaces.IResponseProcessor;
import com.itextos.beacon.http.generichttpapi.common.utils.FileGenUtil;
import com.itextos.beacon.http.generichttpapi.common.utils.Utility;
import com.itextos.beacon.http.interfaceutil.MessageSource;
import com.itextos.beacon.interfaces.generichttpapi.processor.async.AsyncRequestHandler;
import com.itextos.beacon.interfaces.generichttpapi.processor.validate.BasicValidation;

public abstract class AbstractRequestProcessor
        implements
        IRequestProcessor
{

    private static Log               log                   = LogFactory.getLog(AbstractRequestProcessor.class);

    protected int                    mDomesticMobileLength = 0;
    protected long                   mRequestedTime        = 0;
    protected String                 mDefaultCountryCode   = null;
    protected BasicInfo              mBasicInfo            = null;
    protected JSONObject             mParsedJson           = null;
    protected Messagerequest         mParsedXML            = null;
    protected String                 mRequestString        = null;
    protected String                 mCustIp               = null;
    protected IResponseProcessor     mResponseProcessor    = null;
    protected InterfaceRequestStatus mReqStatus            = null;
    protected String                 mReqType              = null;

    protected AbstractRequestProcessor(
            String aRequestString,
            String aCustomerIP,
            long aRequestedTime,
            String aReqType,
            String aResponseType)
    {
        mRequestString = aRequestString;
        mCustIp        = aCustomerIP;
        mRequestedTime = aRequestedTime;
        mReqType       = aReqType;
    }

    @Override
    public BasicInfo getBasicInfo()
    {
        return mBasicInfo;
    }

    @Override
    public InterfaceRequestStatus validateBasicInfo()
    {

        try
        {
            mReqStatus = BasicValidation.validateBasicData(mBasicInfo);
            mReqStatus.setBatchNo(mBasicInfo.getBatchNo());

            if (log.isDebugEnabled())
                log.debug("commonInfo status-  - " + mReqStatus);

            if (mReqStatus.getStatusCode() == InterfaceStatusCode.SUCCESS)
            {
                final String lMessageId    = MessageIdentifier.getInstance().getNextId();
                final String lUserName     = CommonUtility.nullCheck(mBasicInfo.getUserAccountInfo().get(MiddlewareConstant.MW_USER.getName()), true);
                boolean      IsAccountSync = CommonUtility.isEnabled(CommonUtility.nullCheck(mBasicInfo.getUserAccountInfo().get(MiddlewareConstant.MW_ACC_IS_ASYNC.getName())));
                ClusterType  lClusterType  = Utility.getClusterType(CommonUtility.nullCheck(mBasicInfo.getUserAccountInfo().get(MiddlewareConstant.MW_PLATFORM_CLUSTER.getName())));

                if (lClusterType == null)
                    lClusterType = ClusterType.BULK;

                if (lClusterType == ClusterType.OTP)
                    IsAccountSync = false;

                mResponseProcessor.setUname(lUserName);
                mBasicInfo.setFileId(lMessageId);
                mReqStatus.setMessageId(lMessageId);
                mBasicInfo.setIsAsync(IsAccountSync);
                mBasicInfo.setClusterType(lClusterType);

                if (log.isDebugEnabled())
                    log.debug("Message Id  - " + lMessageId);
            }

            if (mBasicInfo.getClientId() == null)
                mResponseProcessor.setStatusObject(mReqStatus, mBasicInfo.getClientId(), mReqType, null);
            else
                mResponseProcessor.setStatusObject(mReqStatus, mBasicInfo.getClientId(), mReqType, (String) mBasicInfo.getUserAccountInfo().get(MiddlewareConstant.MW_TIME_ZONE.getName()));
        }
        catch (final Exception e)
        {
            log.error("Exception while validate commonInfo  - ", e);

            mReqStatus = new InterfaceRequestStatus(InterfaceStatusCode.INTERNAL_SERVER_ERROR, "");
            mResponseProcessor.setStatusObject(mReqStatus, mBasicInfo.getClientId(), mReqType, null);
        }
        return mReqStatus;
    }

    @Override
    public void setRequestStatus(
            InterfaceRequestStatus aRequestStatus)
    {

        try
        {
            if (mBasicInfo == null)
                mResponseProcessor.setStatusObject(aRequestStatus, null, mReqType, null);
            else
                mResponseProcessor.setStatusObject(aRequestStatus, mBasicInfo.getClientId(), mReqType, (String) mBasicInfo.getUserAccountInfo().get(MiddlewareConstant.MW_TIME_ZONE.getName()));
        }
        catch (final Exception e)
        {
            log.error("Exception while giving object to response ", e);
        }
    }

    /*
     * @Override
     * public String appendCountryCode(
     * InterfaceMessage aMessage,
     * String aMobileNum)
     * {
     * final boolean isAppendCountryCode =
     * CommonUtility.isEnabled(aMessage.getAppendCountry());
     * if (log.isDebugEnabled())
     * log.debug("Before append country code destination  - " + aMobileNum);
     * if (isAppendCountryCode)
     * {
     * if (log.isDebugEnabled())
     * log.debug("append country code  ");
     * final String countryCode = CommonUtility.nullCheck(aMessage.getCountryCode(),
     * true);
     * aMobileNum = countryCode + aMobileNum;
     * }
     * if (log.isDebugEnabled())
     * log.debug("After append country code destination  - " + aMobileNum);
     * aMessage.setMobileNumber(aMobileNum);
     * return aMobileNum;
     * }
     */
    @Override
    public void pushKafkaTopic(
            String aReqType)
    {
        if (log.isDebugEnabled())
            log.debug("Request Type : " + aReqType);

        final String      lClientId   = CommonUtility.nullCheck(mBasicInfo.getUserAccountInfo().get(MiddlewareConstant.MW_CLIENT_ID.getName()), true);
        final String      lCluster    = CommonUtility.nullCheck(mBasicInfo.getUserAccountInfo().get(MiddlewareConstant.MW_PLATFORM_CLUSTER.getName()));
        final String      lMsgType    = mBasicInfo.getUserAccountInfo().get(MiddlewareConstant.MW_MSG_TYPE.getName()).toString();

        final QueueObject queueObject = new QueueObject(mBasicInfo.getFileId(), mCustIp, mRequestString, aReqType, mRequestedTime, lClientId, lCluster, lMsgType);

        pushRRQueue(queueObject, "Queue");
    }

    @Override
    public boolean pushRRQueue(
            QueueObject aQueueObj,
            String type)
    {
        boolean       queueSucess      = false;

        final boolean isKafkaAvailable = CommonUtility.isEnabled(Utility.getConfigParamsValueAsString(ConfigParamConstants.IS_KAFKA_AVAILABLE));

        if (isKafkaAvailable)
            queueSucess = pushQueue(aQueueObj);

        if (!queueSucess)
        {
            if (log.isDebugEnabled())
                log.debug("Unable to push the request to Kafka ..., Hence storing into file...");

            if (type.equals("Queue"))
                queueSucess = FileGenUtil.storeInFile(aQueueObj);
        }

        return queueSucess;
    }

    private boolean pushQueue(
            QueueObject aQueueObj)
    {

        try
        {
            if (log.isDebugEnabled())
                log.debug("PushQueue - Request Type : " + aQueueObj.getReqType());

            if (MessageSource.GENERIC_XML.equalsIgnoreCase(aQueueObj.getReqType()))
                aQueueObj.setXmlMessageObj(mParsedXML);
            else
                aQueueObj.setJsonMessageObj(mParsedJson.toString());

            final ClusterType lCluster        = Utility.getClusterType(aQueueObj.getCluster());
            final MessageType lMsgType        = MessageType.getMessageType(aQueueObj.getMsgType());

            String            lMessageContent = "";

            if (MessageSource.GENERIC_XML.equalsIgnoreCase(aQueueObj.getReqType()))
                lMessageContent = mRequestString;
            else
                lMessageContent = aQueueObj.getJsonMessageObj();

            try
            {
                AsyncRequestHandler.writeToKafka(lCluster, aQueueObj.getReqType(), lMsgType, MessageIdentifier.getInstance().getAppInstanceId(), aQueueObj.getClientId(), aQueueObj.getMid(),
                        aQueueObj.getCustIp(), lMessageContent, aQueueObj.getRequestedTime());
            }
            catch (final ItextosException e)
            {
                log.error("Exception occer while pushing the Request to Kafka to process in Async .... " + e);
                return false;
            }
            return true;
        }
        catch (final Exception e)
        {
            e.printStackTrace();
            log.error("Exception occer while pushing the Request to Kafka .... " + e);
        }
        return false;
    }

    @Override
    public void setRequestString(
            String aRequestString)
    {
        mRequestString = aRequestString;
    }

    protected static InterfaceStatusCode getMiddlewareStatus(
            InterfaceStatusCode aClientAccessStatus,
            InterfaceStatusCode aMessageValidationStatus)
    {
        InterfaceStatusCode middlewareStaus = null;

        if (aClientAccessStatus != null)
            middlewareStaus = aClientAccessStatus;
        else
            if (aMessageValidationStatus != InterfaceStatusCode.SUCCESS)
                middlewareStaus = aMessageValidationStatus;
            else
                middlewareStaus = InterfaceStatusCode.SUCCESS;
        return middlewareStaus;
    }

}