package com.itextos.beacon.http.generichttpapi.common.data;

import java.io.Serializable;

import com.itextos.beacon.http.generichttpapi.common.data.xmlparser.Messagerequest;

public class QueueObject
        implements
        Serializable
{

    private static final long      serialVersionUID = 9036202400866427339L;

    private final String           mMid;
    private final String           mCustIp;
    private final long             mRequestedTime;
    private final transient String mRequestMag; // TODO KP Why it is transient?
    private final String           mReqType;
    private final String           mClientId;
    private final String           mCluster;
    private final String           mMsgType;
    private Messagerequest         mXmlMessageObj;
    private String                 mJsonMessageObj;

    public QueueObject(
            String aMid,
            String aIP,
            String aRequest,
            String aReqType,
            long aTime,
            String aClientId,
            String aCluster,
            String aMsgType)
    {
        mMid           = aMid;
        mCustIp        = aIP;
        mRequestedTime = aTime;
        mRequestMag    = aRequest;
        mReqType       = aReqType;
        mClientId      = aClientId;
        mCluster       = aCluster;
        mMsgType       = aMsgType;
    }

    public Messagerequest getXmlMessageObj()
    {
        return mXmlMessageObj;
    }

    public void setXmlMessageObj(
            Messagerequest aXmlMessageObj)
    {
        mXmlMessageObj = aXmlMessageObj;
    }

    public String getJsonMessageObj()
    {
        return mJsonMessageObj;
    }

    public void setJsonMessageObj(
            String aJsonMessageObj)
    {
        mJsonMessageObj = aJsonMessageObj;
    }

    public String getMid()
    {
        return mMid;
    }

    public String getCustIp()
    {
        return mCustIp;
    }

    public long getRequestedTime()
    {
        return mRequestedTime;
    }

    public String getRequestMag()
    {
        return mRequestMag;
    }

    public String getReqType()
    {
        return mReqType;
    }

    public String getClientId()
    {
        return mClientId;
    }

    public String getCluster()
    {
        return mCluster;
    }

    public String getMsgType()
    {
        return mMsgType;
    }

    @Override
    public String toString()
    {
        return "QueueObject [mMid=" + mMid + ", mCustIp=" + mCustIp + ", mRequestedTime=" + mRequestedTime + ", mReqType=" + mReqType + ", mClientId=" + mClientId + ", mCluster=" + mCluster
                + ", mMsgType=" + mMsgType + ", mXmlMessageObj=" + mXmlMessageObj + ", mJsonMessageObj=" + mJsonMessageObj + "]";
    }

}