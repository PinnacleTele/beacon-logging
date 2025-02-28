package com.itextos.beacon.http.generichttpapi.common.data;

import java.io.Serializable;
import java.util.Arrays;

import com.itextos.beacon.commonlib.constants.RouteType;

public class InterfaceMessage
        implements
        Serializable
{

    private static final long      serialVersionUID = -8881822394459509447L;

    private String                 mFileId;
    private String                 mMsgId;
    private String                 mBaseMessageId;
    private String                 mMessage;
    private String                 mMobileNumber;
    private String                 mHeader;
    private String                 mMsgType;
    private int                    mDcs;
    // private String mUdhi;
    // private String mUdh;
    private int                    mDestinationPort;
    private String                 mExpiry;
    private String                 mAppendCountry;
    private String                 mCountryCode;
    private String                 mMessageScheduleTime;
    private String                 mUrlTrack;
    private String                 mCustRef;
    private String                 mTemplateId;
    private String                 mTelemarketerId;
    private String[]               mTemplateValues;
    private String                 mMsgTag;
    private String                 mParam1;
    private String                 mParam2;
    private String                 mParam3;
    private String                 mParam4;
    private String                 mParam5;
    private String                 mParam6;
    private String                 mParam7;
    private String                 mParam8;
    private String                 mParam9;
    private String                 mParam10;
    private String                 mSplitMax;
    private InterfaceRequestStatus mRequestStatus;
    private String                 mRequestSource;
    private String                 mDltTemplateId;
    private String                 mDltTelemarketerId;

    private String                 mDltEntityId;
    private RouteType              mRouteType;
    private String                 mIsSpecialSeriesNumber;
    private String                 mDlrReq;
    private String                 mMegTypeHex;
    private String                 mUrlShortner;
    
    

    public String getFileId() {
		return mFileId;
	}

	public void setFileId(String aFileId) {
		this.mFileId = aFileId;
	}

	public String getMsgId()
    {
        return mMsgId;
    }

	public void setMsgId(
            String aMsgId)
    {
        mMsgId = aMsgId;
    }

    public String getBaseMessageId()
    {
        return mBaseMessageId;
    }

    public void setBaseMessageId(
            String aLongMessageId)
    {
        mBaseMessageId = aLongMessageId;
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

    public String getMobileNumber()
    {
        return mMobileNumber;
    }

    public void setMobileNumber(
            String aMobileNumber)
    {
        mMobileNumber = aMobileNumber;
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

    public String getMsgType()
    {
        return mMsgType;
    }

    public void setMsgType(
            String aMsgType)
    {
        mMsgType = aMsgType;
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

    // public String getUdhi()
    // {
    // return mUdhi;
    // }
    //
    // public void setUdhi(
    // String aUdhi)
    // {
    // mUdhi = aUdhi;
    // }
    //
    // public String getUdh()
    // {
    // return mUdh;
    // }
    //
    // public void setUdh(
    // String aUdh)
    // {
    // mUdh = aUdh;
    // }

    public int getDestinationPort()
    {
        return mDestinationPort;
    }

    public void setDestinationPort(
            int aDestinationPort)
    {
        mDestinationPort = aDestinationPort;
    }

    public String getExpiry()
    {
        return mExpiry;
    }

    public void setExpiry(
            String aExpiry)
    {
        mExpiry = aExpiry;
    }

    public String getAppendCountry()
    {
        return mAppendCountry;
    }

    public void setAppendCountry(
            String aAppendCountry)
    {
        mAppendCountry = aAppendCountry;
    }

    public String getCountryCode()
    {
        return mCountryCode;
    }

    public void setCountryCode(
            String aCountryCode)
    {
        mCountryCode = aCountryCode;
    }

    public String getMessageScheduleTime()
    {
        return mMessageScheduleTime;
    }

    public void setMessageScheduleTime(
            String aMessageScheduleTime)
    {
        mMessageScheduleTime = aMessageScheduleTime;
    }

    public String getUrlTrack()
    {
        return mUrlTrack;
    }

    public void setUrlTrack(
            String aUrlTrack)
    {
        mUrlTrack = aUrlTrack;
    }

    public String getCustRef()
    {
        return mCustRef;
    }

    public void setCustRef(
            String aCustRef)
    {
        mCustRef = aCustRef;
    }

    public String getTemplateId()
    {
        return mTemplateId;
    }

    public void setTemplateId(
            String aTemplateId)
    {
        mTemplateId = aTemplateId;
    }
    
    public String getTelemarketerId()
    {
        return mTelemarketerId;
    }

    public void setTelemarketerId(
            String aTelemarketerId)
    {
        mTelemarketerId = aTelemarketerId;
    }

    public String[] getTemplateValues()
    {
        return mTemplateValues;
    }

    public void setTemplateValues(
            String[] aTemplateValues)
    {
        mTemplateValues = aTemplateValues;
    }

    public String getMsgTag()
    {
        return mMsgTag;
    }

    public void setMsgTag(
            String aMsgTag)
    {
        mMsgTag = aMsgTag;
    }

    public String getParam1()
    {
        return mParam1;
    }

    public void setParam1(
            String aParam1)
    {
        mParam1 = aParam1;
    }

    public String getParam2()
    {
        return mParam2;
    }

    public void setParam2(
            String aParam2)
    {
        mParam2 = aParam2;
    }

    public String getParam3()
    {
        return mParam3;
    }

    public void setParam3(
            String aParam3)
    {
        mParam3 = aParam3;
    }

    public String getParam4()
    {
        return mParam4;
    }

    public void setParam4(
            String aParam4)
    {
        mParam4 = aParam4;
    }

    public String getParam5()
    {
        return mParam5;
    }

    public void setParam5(
            String aParam5)
    {
        mParam5 = aParam5;
    }

    public String getParam6()
    {
        return mParam6;
    }

    public void setParam6(
            String aParam6)
    {
        mParam6 = aParam6;
    }

    public String getParam7()
    {
        return mParam7;
    }

    public void setParam7(
            String aParam7)
    {
        mParam7 = aParam7;
    }

    public String getParam8()
    {
        return mParam8;
    }

    public void setParam8(
            String aParam8)
    {
        mParam8 = aParam8;
    }

    public String getParam9()
    {
        return mParam9;
    }

    public void setParam9(
            String aParam9)
    {
        mParam9 = aParam9;
    }

    public String getParam10()
    {
        return mParam10;
    }

    public void setParam10(
            String aParam10)
    {
        mParam10 = aParam10;
    }

    public String getSplitMax()
    {
        return mSplitMax;
    }

    public void setSplitMax(
            String aSplitMax)
    {
        mSplitMax = aSplitMax;
    }

    public InterfaceRequestStatus getRequestStatus()
    {
        return mRequestStatus;
    }

    public void setRequestStatus(
            InterfaceRequestStatus aRequestStatus)
    {
        mRequestStatus = aRequestStatus;
    }

    public String getRequestSource()
    {
        return mRequestSource;
    }

    public void setRequestSource(
            String aRequestSource)
    {
        mRequestSource = aRequestSource;
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
    
    public String getDltTemplateId()
    {
        return mDltTemplateId;
    }

    public void setDltTemplateId(
            String aDltTemplateId)
    {
        mDltTemplateId = aDltTemplateId;
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

    public RouteType getRouteType()
    {
        return mRouteType;
    }

    public void setRouteType(
            RouteType aRouteType)
    {
        mRouteType = aRouteType;
    }

    public String getIsSpecialSeriesNumber()
    {
        return mIsSpecialSeriesNumber;
    }

    public void setIsSpecialSeriesNumber(
            String aIsSpecialSeriesNumber)
    {
        mIsSpecialSeriesNumber = aIsSpecialSeriesNumber;
    }

    public String getDlrReq()
    {
        return mDlrReq;
    }

    public void setDlrReq(
            String aDlrReq)
    {
        mDlrReq = aDlrReq;
    }

    public String getMegTypeHex()
    {
        return mMegTypeHex;
    }

    public void setMegTypeHex(
            String aMegTypeHex)
    {
        mMegTypeHex = aMegTypeHex;
    }

    public String getUrlShortner()
    {
        return mUrlShortner;
    }

    public void setUrlShortner(
            String aUrlShortner)
    {
        mUrlShortner = aUrlShortner;
    }

    @Override
    public String toString()
    {
        return "InterfaceMessage [mMsgId=" + mMsgId + ", mBaseMessageId=" + mBaseMessageId + ", mMessage=" + mMessage + ", mMobileNumber=" + mMobileNumber + ", mHeader=" + mHeader + ", mMsgType="
                + mMsgType + ", mDcs=" + mDcs + ", mDestinationPort=" + mDestinationPort + ", mExpiry=" + mExpiry + ", mAppendCountry=" + mAppendCountry + ", mCountryCode=" + mCountryCode
                + ", mMessageScheduleTime=" + mMessageScheduleTime + ", mUrlTrack=" + mUrlTrack + ", mCustRef=" + mCustRef + ", mTemplateId=" + mTemplateId + ", mTemplateValues="
                + Arrays.toString(mTemplateValues) + ", mMsgTag=" + mMsgTag + ", mParam1=" + mParam1 + ", mParam2=" + mParam2 + ", mParam3=" + mParam3 + ", mParam4=" + mParam4 + ", mParam5=" + mParam5
                + ", mParam6=" + mParam6 + ", mParam7=" + mParam7 + ", mParam8=" + mParam8 + ", mParam9=" + mParam9 + ", mParam10=" + mParam10 + ", mSplitMax=" + mSplitMax + ", mRequestStatus="
                + mRequestStatus + ", mRequestSource=" + mRequestSource + ", mDltTemplateId=" + mDltTemplateId + ", mDltEntityId=" + mDltEntityId + ", mRouteType=" + mRouteType
                + ", mIsSpecialSeriesNumber=" + mIsSpecialSeriesNumber + ", mDlrReq=" + mDlrReq + ", mMegTypeHex=" + mMegTypeHex + ", mUrlShortner=" + mUrlShortner + "]";
    }

}