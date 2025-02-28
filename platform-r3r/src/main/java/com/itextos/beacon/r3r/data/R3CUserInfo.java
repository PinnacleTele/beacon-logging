package com.itextos.beacon.r3r.data;

import java.util.Date;

public class R3CUserInfo
{

    private final String mRequestUrl;
    private final String mUserAgent;
    private final String mUserIp;
    private final long   mRequestedTime;

    /**
     * @param aRequestUrl
     * @param aUserAgent
     * @param aUserIp
     * @param aRequestedTime
     */
    public R3CUserInfo(
            String aRequestUrl,
            String aUserAgent,
            String aUserIp,
            long aRequestedTime)
    {
        super();
        mRequestUrl    = aRequestUrl;
        mUserAgent     = aUserAgent;
        mUserIp        = aUserIp;
        mRequestedTime = aRequestedTime;
    }

    public String getRequestUrl()
    {
        return mRequestUrl;
    }

    public String getUserAgent()
    {
        return mUserAgent;
    }

    public String getUserIp()
    {
        return mUserIp;
    }

    public long getRequestedTime()
    {
        return mRequestedTime;
    }

    @Override
    public String toString()
    {
        return "R3CUserInfo [mRequestUrl=" + mRequestUrl + ", mUserAgent=" + mUserAgent + ", mUserIp=" + mUserIp + ", mRequestedTime=" + new Date(mRequestedTime) + "]";
    }

}