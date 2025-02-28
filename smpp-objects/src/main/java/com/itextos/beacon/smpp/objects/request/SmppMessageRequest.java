package com.itextos.beacon.smpp.objects.request;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.itextos.beacon.commonlib.constants.FeatureCode;
import com.itextos.beacon.commonlib.constants.MessageClass;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.RouteType;
import com.itextos.beacon.commonlib.utility.CommonUtility;

public class SmppMessageRequest
        implements
        Serializable
{

    private static final Log  log                 = LogFactory.getLog(SmppMessageRequest.class);

    private static final long serialVersionUID    = 1858742649285075585L;

    private String            mAppInstanceId;
    private String            mAckid;
    private String            mClientId;
    private String            mClientIp;
    private String            mCountry;
    private int               mDcs                = -1;
    private int               mDestPort;
    private String            mDlrReqFromClient;
    private String            mDltEntityId;
    private String            mDltTemplateId;
    private String            mDltTelemarketerId;    
    private boolean           mDndCheckRequired;
    private String            mEsmClass;
    private FeatureCode       mFeatureCode;
    private String            mHeader;
    private boolean           mIsConcatMessage;
    private boolean           mIsHexMsg;
    private boolean           mIsSpecialSeriesNumber;
    private String            mMessage;
    private String            mMessageExpiry;
    private String            mMobileNumber;
    private MessageClass      mMsgClass;
    private String            mMsgDeliveryType;
    private int               mPartNumber;
    private long              mReceivedTime;
    private RouteType         mRouteType;
    private String            mScheduleTime;
    private String            mServicetype;
    private int               mTotalParts;
    private String            mUdh;
    private String            mUdhReferenceNumber;
    private String            mUdhi;
    private String            mCluster;
    private String            mInterfaceErrorCode = "";
    private String            mSmppInstance;
    private String            mSystemId;
    private String            mBindType;
    private String            mCustMid;

    public SmppMessageRequest()
    {}

    public SmppMessageRequest(
            String aJsonString)
    {
        log.debug("aJsonString : '" + aJsonString + "'");

        final JsonObject lJsonObject = parse(aJsonString);

        mAppInstanceId      = getStringFromJson(lJsonObject, MiddlewareConstant.MW_APP_INSTANCE_ID);
        mClientId           = getStringFromJson(lJsonObject, MiddlewareConstant.MW_CLIENT_ID);
        mMobileNumber       = getStringFromJson(lJsonObject, MiddlewareConstant.MW_MOBILE_NUMBER);
        mMessage            = getMessageStringFromJson(lJsonObject, MiddlewareConstant.MW_MSG);
        mHeader             = getStringFromJson(lJsonObject, MiddlewareConstant.MW_HEADER);
        mScheduleTime       = getStringFromJson(lJsonObject, MiddlewareConstant.MW_SCHE_DATE_TIME);
        mUdh                = getStringFromJson(lJsonObject, MiddlewareConstant.MW_UDH);
        mDestPort           = getIntFromJson(lJsonObject, MiddlewareConstant.MW_DESTINATION_PORT);
        mDlrReqFromClient   = getStringFromJson(lJsonObject, MiddlewareConstant.MW_DLR_REQ_FROM_CLI);
        mMessageExpiry      = getStringFromJson(lJsonObject, MiddlewareConstant.MW_MAX_VALIDITY_IN_SEC);
        mAckid              = getStringFromJson(lJsonObject, MiddlewareConstant.MW_FILE_ID);
        mReceivedTime       = getLongFromJson(lJsonObject, MiddlewareConstant.MW_MSG_RECEIVED_TIME);
        mClientIp           = getStringFromJson(lJsonObject, MiddlewareConstant.MW_CLIENT_SOURCE_IP);
        mServicetype        = getStringFromJson(lJsonObject, MiddlewareConstant.MW_SMPP_SERVICE_TYPE);
        mIsHexMsg           = getBooleanFromJson(lJsonObject, MiddlewareConstant.MW_IS_HEX_MSG);
        mUdhi               = getStringFromJson(lJsonObject, MiddlewareConstant.MW_UDHI);
        mEsmClass           = getStringFromJson(lJsonObject, MiddlewareConstant.MW_SMPP_ESM_CLASS);
        mDltEntityId        = getStringFromJson(lJsonObject, MiddlewareConstant.MW_DLT_ENTITY_ID);
        mDltTemplateId      = getStringFromJson(lJsonObject, MiddlewareConstant.MW_DLT_TEMPLATE_ID);
        mDltTelemarketerId  = getStringFromJson(lJsonObject, MiddlewareConstant.MW_CLIENT_MESSAGE_ID);

        mTotalParts         = getIntFromJson(lJsonObject, MiddlewareConstant.MW_MSG_TOTAL_PARTS);
        mPartNumber         = getIntFromJson(lJsonObject, MiddlewareConstant.MW_MSG_PART_NUMBER);
        mUdhReferenceNumber = getStringFromJson(lJsonObject, MiddlewareConstant.MW_CONCAT_REF_NUM);
        mFeatureCode        = FeatureCode.getFeatureCode(getStringFromJson(lJsonObject, MiddlewareConstant.MW_FEATURE_CODE));
        mDcs                = getIntFromJson(lJsonObject, MiddlewareConstant.MW_DCS);

        mMsgClass           = MessageClass.getMessageClass(getStringFromJson(lJsonObject, MiddlewareConstant.MW_MSG_CLASS));
        mRouteType          = RouteType.getRouteType(getStringFromJson(lJsonObject, MiddlewareConstant.MW_ROUTE_TYPE));
        mCluster            = getStringFromJson(lJsonObject, MiddlewareConstant.MW_PLATFORM_CLUSTER);
        mInterfaceErrorCode = getStringFromJson(lJsonObject, MiddlewareConstant.MW_SUB_ORI_STATUS_CODE);
        mSmppInstance       = getStringFromJson(lJsonObject, MiddlewareConstant.MW_SMPP_INSTANCE_ID);
        mSystemId           = getStringFromJson(lJsonObject, MiddlewareConstant.MW_SMPP_SYSTEM_ID);
        mBindType           = getStringFromJson(lJsonObject, MiddlewareConstant.MW_SMPP_BIND_TYPE);
        mCustMid            = getStringFromJson(lJsonObject, MiddlewareConstant.MW_CLIENT_MESSAGE_ID);
    }

    public boolean isConcatMessage()
    {
        return mIsConcatMessage;
    }

    public void setConcatMessage(
            boolean aIsConcatMessage)
    {
        mIsConcatMessage = aIsConcatMessage;
    }

    public boolean isSpecialSeriesNumber()
    {
        return mIsSpecialSeriesNumber;
    }

    public void setSpecialSeriesNumber(
            boolean aIsSpecialSeriesNumber)
    {
        mIsSpecialSeriesNumber = aIsSpecialSeriesNumber;
    }

    public String getAckid()
    {
        return mAckid;
    }

    public void setAckid(
            String aAckid)
    {
        mAckid = aAckid;
    }

    public String getClientId()
    {
        return mClientId;
    }

    public void setClientId(
            String aClientId)
    {
        mClientId = aClientId;
    }

    public String getClientIp()
    {
        return mClientIp;
    }

    public void setClientIp(
            String aClientIp)
    {
        mClientIp = aClientIp;
    }

    public String getCountry()
    {
        return mCountry;
    }

    public void setCountry(
            String aCountry)
    {
        mCountry = aCountry;
    }

    public int getDcs()
    {
        return mDcs;
    }

    public void setDcs(
            int aDcs)
    {
        mDcs = aDcs;
    }

    public int getDestPort()
    {
        return mDestPort;
    }

    public void setDestPort(
            int aDestPort)
    {
        mDestPort = aDestPort;
    }

    public String getDlrReqFromClient()
    {
        return mDlrReqFromClient;
    }

    public void setDlrReqFromClient(
            String aDlrReqFromClient)
    {
        mDlrReqFromClient = aDlrReqFromClient;
    }

    public String getDltEntityId()
    {
        return mDltEntityId;
    }

    public void setDltEntityId(
            String aDltEntityId)
    {
        mDltEntityId = aDltEntityId;
    }

    public String getDltTemplateId()
    {
        return mDltTemplateId;
    }

    public void setDltTemplateId(
            String aDltTemplateId)
    {
        mDltTemplateId = aDltTemplateId;
    }

    
    public String getDltTelemarketerId()
    {
        return mDltTelemarketerId;
    }

    public void setDltTelemarketerId(
            String aDltTelemarketerId)
    {
        mDltTelemarketerId = aDltTelemarketerId;
    }
    
    public boolean isDndCheckRequired()
    {
        return mDndCheckRequired;
    }

    public void setDndCheckRequired(
            boolean aDndCheckRequired)
    {
        mDndCheckRequired = aDndCheckRequired;
    }

    public String getEsmClass()
    {
        return mEsmClass;
    }

    public void setEsmClass(
            String aEsmClass)
    {
        mEsmClass = aEsmClass;
    }

    public FeatureCode getFeatureCode()
    {
        return mFeatureCode;
    }

    public void setFeatureCode(
            FeatureCode aFeatureCode)
    {
        mFeatureCode = aFeatureCode;
    }

    public String getHeader()
    {
        return mHeader;
    }

    public void setHeader(
            String aHeader)
    {
        mHeader = aHeader;
    }

    public boolean isHexMsg()
    {
        return mIsHexMsg;
    }

    public void setHexMsg(
            boolean aIsHexMsg)
    {
        mIsHexMsg = aIsHexMsg;
    }

    public String getMessage()
    {
        return mMessage;
    }

    public void setMessage(
            String aMessage)
    {
        mMessage = aMessage;
    }

    public String getMessageExpiry()
    {
        return mMessageExpiry;
    }

    public void setMessageExpiry(
            String aMessageExpiry)
    {
        mMessageExpiry = aMessageExpiry;
    }

    public String getMobileNumber()
    {
        return mMobileNumber;
    }

    public void setMobileNumber(
            String aMobileNumber)
    {
        mMobileNumber = aMobileNumber;
    }

    public MessageClass getMsgClass()
    {
        return mMsgClass;
    }

    public void setMsgClass(
            MessageClass aMsgClass)
    {
        mMsgClass = aMsgClass;
    }

    public String getMsgDeliveryType()
    {
        return mMsgDeliveryType;
    }

    public void setMsgDeliveryType(
            String aMsgDeliveryType)
    {
        mMsgDeliveryType = aMsgDeliveryType;
    }

    public int getPartNumber()
    {
        return mPartNumber;
    }

    public void setPartNumber(
            int aPartNumber)
    {
        mPartNumber = aPartNumber;
    }

    public long getReceivedTime()
    {
        return mReceivedTime;
    }

    public void setReceivedTime(
            long aReceivedTime)
    {
        mReceivedTime = aReceivedTime;
    }

    public RouteType getRouteType()
    {
        return mRouteType;
    }

    public void setRouteType(
            RouteType aRouteType)
    {
        mRouteType = aRouteType;
    }

    public String getScheduleTime()
    {
        return mScheduleTime;
    }

    public void setScheduleTime(
            String aScheduleTime)
    {
        mScheduleTime = aScheduleTime;
    }

    public String getServicetype()
    {
        return mServicetype;
    }

    public void setServicetype(
            String aServicetype)
    {
        mServicetype = aServicetype;
    }

    public int getTotalParts()
    {
        return mTotalParts;
    }

    public void setTotalParts(
            int aTotalParts)
    {
        mTotalParts = aTotalParts;
    }

    public String getUdh()
    {
        return mUdh;
    }

    public void setUdh(
            String aUdh)
    {
        mUdh = aUdh;
    }

    public String getUdhi()
    {
        return mUdhi;
    }

    public void setUdhi(
            String aUdhi)
    {
        mUdhi = aUdhi;
    }

    public String getUdhReferenceNumber()
    {
        return mUdhReferenceNumber;
    }

    public void setUdhReferenceNumber(
            String aUdhReferenceNumber)
    {
        mUdhReferenceNumber = aUdhReferenceNumber;
    }

    public String getJsonString()
    {
        final JsonObject lJsonObject = new JsonObject();

        lJsonObject.addProperty(MiddlewareConstant.MW_APP_INSTANCE_ID.getKey(), mAppInstanceId);
        lJsonObject.addProperty(MiddlewareConstant.MW_CLIENT_ID.getKey(), mClientId);
        lJsonObject.addProperty(MiddlewareConstant.MW_MOBILE_NUMBER.getKey(), mMobileNumber);
        lJsonObject.addProperty(MiddlewareConstant.MW_MSG.getKey(), mMessage);
        lJsonObject.addProperty(MiddlewareConstant.MW_HEADER.getKey(), mHeader);
        lJsonObject.addProperty(MiddlewareConstant.MW_SCHE_DATE_TIME.getKey(), CommonUtility.nullCheck(mScheduleTime, true));
        lJsonObject.addProperty(MiddlewareConstant.MW_UDH.getKey(), mUdh);
        lJsonObject.addProperty(MiddlewareConstant.MW_DESTINATION_PORT.getKey(), Integer.toString(mDestPort));
        lJsonObject.addProperty(MiddlewareConstant.MW_DLR_REQ_FROM_CLI.getKey(), mDlrReqFromClient);
        lJsonObject.addProperty(MiddlewareConstant.MW_MAX_VALIDITY_IN_SEC.getKey(), CommonUtility.nullCheck(mMessageExpiry, true));
        lJsonObject.addProperty(MiddlewareConstant.MW_FILE_ID.getKey(), mAckid);
        lJsonObject.addProperty(MiddlewareConstant.MW_BASE_MESSAGE_ID.getKey(), mAckid);
        lJsonObject.addProperty(MiddlewareConstant.MW_MESSAGE_ID.getKey(), mAckid);
        lJsonObject.addProperty(MiddlewareConstant.MW_MSG_RECEIVED_TIME.getKey(), Long.toString(mReceivedTime));
        lJsonObject.addProperty(MiddlewareConstant.MW_CLIENT_SOURCE_IP.getKey(), mClientIp);
        lJsonObject.addProperty(MiddlewareConstant.MW_MSG_CLASS.getKey(), mMsgClass.getKey());
        lJsonObject.addProperty(MiddlewareConstant.MW_IS_HEX_MSG.getKey(), Boolean.toString(mIsHexMsg));
        lJsonObject.addProperty(MiddlewareConstant.MW_UDHI.getKey(), mUdhi);
        lJsonObject.addProperty(MiddlewareConstant.MW_SMPP_ESM_CLASS.getKey(), mEsmClass);
        lJsonObject.addProperty(MiddlewareConstant.MW_DLT_ENTITY_ID.getKey(), CommonUtility.nullCheck(mDltEntityId, true));
        lJsonObject.addProperty(MiddlewareConstant.MW_DLT_TEMPLATE_ID.getKey(), CommonUtility.nullCheck(mDltTemplateId, true));
        lJsonObject.addProperty(MiddlewareConstant.MW_COUNTRY.getKey(), CommonUtility.nullCheck(mCountry, true));
        lJsonObject.addProperty(MiddlewareConstant.MW_FEATURE_CODE.getKey(), mFeatureCode.getKey());
        lJsonObject.addProperty(MiddlewareConstant.MW_MSG_TOTAL_PARTS.getKey(), Integer.toString(mTotalParts));
        lJsonObject.addProperty(MiddlewareConstant.MW_MSG_PART_NUMBER.getKey(), Integer.toString(mPartNumber));
        lJsonObject.addProperty(MiddlewareConstant.MW_CONCAT_REF_NUM.getKey(), mUdhReferenceNumber);
        lJsonObject.addProperty(MiddlewareConstant.MW_ROUTE_TYPE.getKey(), mRouteType.getKey());
        lJsonObject.addProperty(MiddlewareConstant.MW_TREAT_DOMESTIC_AS_SPECIAL_SERIES.getKey(), Boolean.toString(mIsSpecialSeriesNumber));
        lJsonObject.addProperty(MiddlewareConstant.MW_DCS.getKey(), Integer.toString(mDcs));
        lJsonObject.addProperty(MiddlewareConstant.MW_PLATFORM_CLUSTER.getKey(), mCluster);
        lJsonObject.addProperty(MiddlewareConstant.MW_SUB_ORI_STATUS_CODE.getKey(), mInterfaceErrorCode);
        lJsonObject.addProperty(MiddlewareConstant.MW_SMPP_INSTANCE_ID.getKey(), mSmppInstance);
        lJsonObject.addProperty(MiddlewareConstant.MW_SMPP_SYSTEM_ID.getKey(), mSystemId);
        lJsonObject.addProperty(MiddlewareConstant.MW_SMPP_BIND_TYPE.getKey(), mBindType);
        lJsonObject.addProperty(MiddlewareConstant.MW_CLIENT_MESSAGE_ID.getKey(), mCustMid);
        lJsonObject.addProperty(MiddlewareConstant.MW_DLT_TMA_ID.getKey(), mCustMid);

        if (log.isDebugEnabled())
            log.debug("Concat Json String : " + lJsonObject);

        return lJsonObject.toString();
    }

    private static String getMessageStringFromJson(
            JsonObject aJsonObject,
            MiddlewareConstant aMwConstant)
    {
        final JsonElement lJsonElement = aJsonObject.get(aMwConstant.getKey());

        if (lJsonElement != null)
            return lJsonElement.getAsString() == null ? "" : lJsonElement.getAsString();

        return "";
    }

    private static String getStringFromJson(
            JsonObject aJsonObject,
            MiddlewareConstant aMwConstant)
    {
        final JsonElement lJsonElement = aJsonObject.get(aMwConstant.getKey());

        if (lJsonElement != null)
            return CommonUtility.nullCheck(lJsonElement.getAsString(), true);

        return "";
    }

    private static long getLongFromJson(
            JsonObject aJsonObject,
            MiddlewareConstant aMwConstant)
    {
        final JsonElement lJsonElement = aJsonObject.get(aMwConstant.getKey());
        if (lJsonElement != null)
            return CommonUtility.getLong(CommonUtility.nullCheck(lJsonElement.getAsString(), true));

        return 0;
    }

    private static int getIntFromJson(
            JsonObject aJsonObject,
            MiddlewareConstant aMwConstant)
    {
        final JsonElement lJsonElement = aJsonObject.get(aMwConstant.getKey());
        if (lJsonElement != null)
            return CommonUtility.getInteger(CommonUtility.nullCheck(lJsonElement.getAsString(), true));

        return 0;
    }

    private static boolean getBooleanFromJson(
            JsonObject aJsonObject,
            MiddlewareConstant aMwConstant)
    {
        final JsonElement lJsonElement = aJsonObject.get(aMwConstant.getKey());
        if (lJsonElement != null)
            return CommonUtility.isTrue(lJsonElement.getAsString());

        return false;
    }

    private static JsonObject parse(
            String aJsonString)
    {
        return JsonParser.parseString(aJsonString).getAsJsonObject();
    }

    public String getCluster()
    {
        return mCluster;
    }

    public void setCluster(
            String aCluster)
    {
        mCluster = aCluster;
    }

    public String getAppInstanceId()
    {
        return mAppInstanceId;
    }

    public void setAppInstanceId(
            String aAppInstanceId)
    {
        mAppInstanceId = aAppInstanceId;
    }

    public String getInterfaceErrorCode()
    {
        return mInterfaceErrorCode;
    }

    public void setInterfaceErrorCode(
            String aInterfaceErrorCode)
    {
        mInterfaceErrorCode = aInterfaceErrorCode;
    }

    public String getSmppInstance()
    {
        return mSmppInstance;
    }

    public void setSmppInstance(
            String aSmppInstance)
    {
        mSmppInstance = aSmppInstance;
    }

    public String getSystemId()
    {
        return mSystemId;
    }

    public void setSystemId(
            String aSystemId)
    {
        mSystemId = aSystemId;
    }

    public String getBindType()
    {
        return mBindType;
    }

    public void setBindType(
            String aBindType)
    {
        mBindType = aBindType;
    }

    public String getCustMid()
    {
        return mCustMid;
    }

    public void setCustMid(
            String aCustMid)
    {
        mCustMid = aCustMid;
    }

    @Override
    public String toString()
    {
        return "SmppMessageRequest [mAppInstanceId=" + mAppInstanceId + ", mAckid=" + mAckid + ", mClientId=" + mClientId + ", mClientIp=" + mClientIp + ", mCountry=" + mCountry + ", mDcs=" + mDcs
                + ", mDestPort=" + mDestPort + ", mDlrReqFromClient=" + mDlrReqFromClient + ", mDltEntityId=" + mDltEntityId + ", mDltTemplateId=" + mDltTemplateId + ", mDndCheckRequired="
                + mDndCheckRequired + ", mEsmClass=" + mEsmClass + ", mFeatureCode=" + mFeatureCode + ", mHeader=" + mHeader + ", mIsConcatMessage=" + mIsConcatMessage + ", mIsHexMsg=" + mIsHexMsg
                + ", mIsSpecialSeriesNumber=" + mIsSpecialSeriesNumber + ", mMessage=" + mMessage + ", mMessageExpiry=" + mMessageExpiry + ", mMobileNumber=" + mMobileNumber + ", mMsgClass="
                + mMsgClass + ", mMsgDeliveryType=" + mMsgDeliveryType + ", mPartNumber=" + mPartNumber + ", mReceivedTime=" + mReceivedTime + ", mRouteType=" + mRouteType + ", mScheduleTime="
                + mScheduleTime + ", mServicetype=" + mServicetype + ", mTotalParts=" + mTotalParts + ", mUdh=" + mUdh + ", mUdhReferenceNumber=" + mUdhReferenceNumber + ", mUdhi=" + mUdhi
                + ", mCluster=" + mCluster + ", mInterfaceErrorCode=" + mInterfaceErrorCode + ", mSmppInstance=" + mSmppInstance + ", mSystemId=" + mSystemId + ", mBindType=" + mBindType
                + ", mCustMid=" + mCustMid + "]";
    }

}