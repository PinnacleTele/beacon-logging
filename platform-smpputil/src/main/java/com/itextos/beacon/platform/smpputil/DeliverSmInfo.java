package com.itextos.beacon.platform.smpputil;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

public class DeliverSmInfo
        extends
        AbstractSmppInfo
{

    private static final long INVALID_RETRY_INIT_TIME = -1;
    private static final int  DN_RES_FUT_NULL         = 99;
    public static final int   TIME_EXPIRED            = 50;

    public long getRetryInitTime()
    {
        return mRetryInitTime;
    }

    public void setRetryInitTime(
            long aRetryInitTime)
    {
        mRetryInitTime = aRetryInitTime;
    }

    public long getSubmiTs()
    {
        return mSubmiTs;
    }

    public void setSubmiTs(
            long aSubmiTs)
    {
        mSubmiTs = aSubmiTs;
    }

    public void setResponseTime(
            long aResponseTime)
    {
        mResponseTime = aResponseTime;
    }

    public void setRetryAttempt(
            int aRetryAttempt)
    {
        mRetryAttempt = aRetryAttempt;
    }

    public static final int VC_EXP_B4_SENT = 60;

    private String          mClientId;
    private SmppDnStatus    mDnStatus;
    private long            mResponseTime;
    private int             mRetryAttempt;
    private long            mRetryInitTime;
    private String          mSourceAddress;
    private int             mDataCoding;
    private String          mDestinationAddress;
    private String          mServiceType;
    private String          mEsmClass;
    private String          mShortMessage;
    private String          mMsgId;
    private long            mSubmiTs;
    private long            mReceivedTs;
    private String          mReason;
    private long            mCarrierSubmitTs;
    private long            mDNReceivedTs;

    public void setClientId(
            String aClientId)
    {
        mClientId = aClientId;
    }

    public String getClientId()
    {
        return mClientId;
    }

    public SmppDnStatus getDnStatus()
    {
        return mDnStatus;
    }

    public long getResponseTime()
    {
        return mResponseTime;
    }

    public int getRetryAttempt()
    {
        return mRetryAttempt;
    }

    private void resetDnStatus()
    {
        setDnStatus(null);
    }

    public void setDnStatus(
            SmppDnStatus aDnStatus)
    {
        mDnStatus = aDnStatus;
    }

    public void setResponseTime()
    {
        mResponseTime = System.currentTimeMillis();
    }

    public void setRetryInitTime()
    {
        if (mRetryInitTime == INVALID_RETRY_INIT_TIME)
            mRetryInitTime = System.currentTimeMillis();
    }

    public void incRetryAttempt()
    {
        mRetryAttempt++;
    }

    public boolean updateDnStatus(
            Integer aStatus)
    {
        boolean postLog = false;

        if (aStatus == null)
            resetDnStatus();
        else
            switch (aStatus)
            {
                case 0:
                    setDnStatus(SmppDnStatus.SUCCESS);
                    setResponseTime();
                    postLog = true;
                    break;

                case -2:
                    setDnStatus(SmppDnStatus.EXPIRED);
                    break;

                case DN_RES_FUT_NULL:
                    // Skip
                    break;

                case TIME_EXPIRED:
                    setDnStatus(SmppDnStatus.SMPP_API_TIME_EXPIRED);
                    break;

                case VC_EXP_B4_SENT:
                    setDnStatus(SmppDnStatus.VC_EXP_B4_SENT);
                    break;

                default:
                    setDnStatus(SmppDnStatus.FAILED);
                    setResponseTime();
                    postLog = true;
                    break;
            }
        return postLog;
    }

    public boolean isExpired(
            long aWaitTime)
    {

        if ((mRetryInitTime != INVALID_RETRY_INIT_TIME) && ((System.currentTimeMillis() - mRetryInitTime) > aWaitTime))
        {
            setDnStatus(SmppDnStatus.VC_EXP_B4_SENT);
            return true;
        }
        return false;
    }

    public void updateRetryInfo(
            Integer aStatus)
    {

        if ((aStatus != null) && !aStatus.equals(0))
        {
            setRetryInitTime();
            incRetryAttempt();
        }
    }

    public String getSourceAddress()
    {
        return mSourceAddress;
    }

    public void setSourceAddress(
            String aSourceAddress)
    {
        mSourceAddress = aSourceAddress;
    }

    public int getDataCoding()
    {
        return mDataCoding;
    }

    public void setDataCoding(
            int aDataCoding)
    {
        mDataCoding = aDataCoding;
    }

    public String getDestinationAddress()
    {
        return mDestinationAddress;
    }

    public void setDestinationAddress(
            String aDestinationAddress)
    {
        mDestinationAddress = aDestinationAddress;
    }

    public String getServiceType()
    {
        return mServiceType;
    }

    public void setServiceType(
            String aServiceType)
    {
        mServiceType = aServiceType;
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

    public String getShortMessage()
    {
        return mShortMessage;
    }

    public void setShortMessage(
            String aShortMessage)
    {
        mShortMessage = aShortMessage;
    }

    public void setMsgId(
            String aMsgId)
    {
        mMsgId = aMsgId;
    }

    public String getMsgId()
    {
        return mMsgId;
    }

    public void setSubmitTs()
    {
        mSubmiTs = System.currentTimeMillis();
    }

    public void resetDnRts()
    {}

    public void resetDnSts()
    {}

    public JsonObject getJson()
    {
        final JsonObject obj = new JsonObject();
        obj.addProperty("mClientId", mClientId);
        obj.addProperty("mResponseTime", mResponseTime);
        obj.addProperty("mRetryAttempt", mRetryAttempt);
        obj.addProperty("mRetryInitTime", mRetryInitTime);
        obj.addProperty("mDataCoding", mDataCoding);
        obj.addProperty("mMsgId", mMsgId);
        obj.addProperty("mSubmiTs", mSubmiTs);
        obj.addProperty("mEsmClass", mEsmClass);
        obj.addProperty("mShortMessage", mShortMessage);
        obj.addProperty("mServiceType", mServiceType);
        obj.addProperty("mSourceAddress", mSourceAddress);
        obj.addProperty("mReceivedTs", mReceivedTs);
        obj.addProperty("mDestinationAddress", mDestinationAddress);
        obj.addProperty("mReason", mReason);
        obj.addProperty("mCarrierSubmitTs", mCarrierSubmitTs);
        obj.addProperty("mDNReceivedTs", mDNReceivedTs);

        return obj;
    }

    public static void main(
            String[] args)
    {
        final JsonArray jsonArray      = new JsonArray();
        DeliverSmInfo   lDeliverSmInfo = new DeliverSmInfo();
        lDeliverSmInfo.setClientId("6000000200000000");
        lDeliverSmInfo.setMsgId("2692110291811120000400");
        jsonArray.add(lDeliverSmInfo.getJson());
        lDeliverSmInfo = new DeliverSmInfo();
        lDeliverSmInfo.setClientId("60000002000000001");
        lDeliverSmInfo.setMsgId("26921102918111200004001");
        jsonArray.add(lDeliverSmInfo.getJson());

        // final String lJson = new Gson().toJson(lDeliverSmInfo, DeliverSmInfo.class);
        System.out.println("Json :" + jsonArray.toString());

        final Type                type      = new TypeToken<List<DeliverSmInfo>>()
                                            {}.getType();
        final List<DeliverSmInfo> lFromJson = new Gson().fromJson(jsonArray.toString(), type);
        System.out.println(lFromJson);

        for (final DeliverSmInfo lDelv : lFromJson)
            System.out.println("Client ID: " + lDelv.getClientId());
    }

    public long getReceivedTs()
    {
        return mReceivedTs;
    }

    public void setReceivedTs(
            long aReceivedTs)
    {
        mReceivedTs = aReceivedTs;
    }

    public String getReason()
    {
        return mReason;
    }

    public void setReason(
            String aReason)
    {
        mReason = aReason;
    }

    public long getCarrierSubmitTs()
    {
        return mCarrierSubmitTs;
    }

    public void setCarrierSubmitTs(
            long aCarrierSubmitTs)
    {
        mCarrierSubmitTs = aCarrierSubmitTs;
    }

    public long getDNReceivedTs()
    {
        return mDNReceivedTs;
    }

    public void setDNReceivedTs(
            long aDNReceivedTs)
    {
        mDNReceivedTs = aDNReceivedTs;
    }

    @Override
    public String toString()
    {
        return "DeliverSmInfo [mClientId=" + mClientId + ", mDnStatus=" + mDnStatus + ", mResponseTime=" + mResponseTime + ", mRetryAttempt=" + mRetryAttempt + ", mRetryInitTime=" + mRetryInitTime
                + ", mSourceAddress=" + mSourceAddress + ", mDataCoding=" + mDataCoding + ", mDestinationAddress=" + mDestinationAddress + ", mServiceType=" + mServiceType + ", mEsmClass=" + mEsmClass
                + ", mShortMessage=" + mShortMessage + ", mMsgId=" + mMsgId + ", mSubmiTs=" + mSubmiTs + ", mReceivedTs=" + mReceivedTs + ", mReason=" + mReason + ", mCarrierSubmitTs="
                + mCarrierSubmitTs + ", mDNReceivedTs=" + mDNReceivedTs + "]";
    }

}
