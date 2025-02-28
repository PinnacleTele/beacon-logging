package com.itextos.beacon.smpp.interfaces.util.counters;

public class SessionCounter
        implements
        Cloneable
{

    private final String mClientId;
    private final Long   mSessionId;
    private double       mAvgResponseTime;
    private double       mAvgWaitTime;
    private long         mExpired;
    private long         mRequest;
    private long         mResponse;

    public SessionCounter(
            String aClientId,
            Long aSessionId)
    {
        mClientId  = aClientId;
        mSessionId = aSessionId;
    }

    private SessionCounter(
            String aClientId,
            Long aSessionId,
            long aRequest,
            long aResponse,
            long aExpired,
            double aAvgResponseTime,
            double aAvgWaitTime)
    {
        mClientId        = aClientId;
        mSessionId       = aSessionId;
        mRequest         = aRequest;
        mResponse        = aResponse;
        mExpired         = aExpired;
        mAvgResponseTime = aAvgResponseTime;
        mAvgWaitTime     = aAvgWaitTime;
    }

    public void addExpired(
            long aExpiredCount)
    {
        mExpired += aExpiredCount;
    }

    public void addRequest(
            long aRequestsCount)
    {
        mRequest += aRequestsCount;
    }

    public void addResponse(
            long aResponseCount)
    {
        mResponse += aResponseCount;
    }

    @Override
    public SessionCounter clone()
    {
        return new SessionCounter(mClientId, mSessionId, mRequest, mResponse, mExpired, mAvgResponseTime, mAvgWaitTime);
    }

    public double getAvgResponseTime()
    {
        return mAvgResponseTime;
    }

    public double getAvgWaitTime()
    {
        return mAvgWaitTime;
    }

    public long getExpired()
    {
        return mExpired;
    }

    public long getRequest()
    {
        return mRequest;
    }

    public long getResponse()
    {
        return mResponse;
    }

    public Long getSessionId()
    {
        return mSessionId;
    }

    public void setAvgResponseTime(
            double aAvgResponseTime)
    {
        mAvgResponseTime = aAvgResponseTime;
    }

    public void setAvgWaitTime(
            double aAvgWaitTime)
    {
        mAvgWaitTime = aAvgWaitTime;
    }

    public void setExpired(
            long aExpired)
    {
        mExpired = aExpired;
    }

    public void setRequest(
            long aRequest)
    {
        mRequest = aRequest;
    }

    public void setResponse(
            long aResponse)
    {
        mResponse = aResponse;
    }

    @Override
    public String toString()
    {
        return "SessionCounter [mClientId=" + mClientId + ", mAvgResponseTime=" + mAvgResponseTime + ", mAvgWaitTime=" + mAvgWaitTime + ", mExpired=" + mExpired + ", mRequest=" + mRequest
                + ", mResponse=" + mResponse + ", mSessionId=" + mSessionId + "]";
    }

}