package com.itextos.beacon.r3r.data;

import java.util.Map;

public class R3rRequestData
{

    private String              mRequestUrl;
    private String              mUserAgent;
    private String              mRequestIpAddress;
    private Map<String, String> mShortCodeDataMap;
    private long                mRequestTime;
    private String              mShortCode;
    private String              mRequestStatus;

    public String getRequestUrl()
    {
        return mRequestUrl;
    }

    public void setRequestUrl(
            String aRequestUrl)
    {
        mRequestUrl = aRequestUrl;
    }

    public String getUserAgent()
    {
        return mUserAgent;
    }

    public void setUserAgent(
            String aUserAgent)
    {
        mUserAgent = aUserAgent;
    }

    public String getRequestIpAddress()
    {
        return mRequestIpAddress;
    }

    public void setRequestIpAddress(
            String aRequestIpAddress)
    {
        mRequestIpAddress = aRequestIpAddress;
    }

    public Map<String, String> getShortCodeDataMap()
    {
        return mShortCodeDataMap;
    }

    public void setShortCodeDataMap(
            Map<String, String> aShortCodeDataMap)
    {
        mShortCodeDataMap = aShortCodeDataMap;
    }

    public long getRequestTime()
    {
        return mRequestTime;
    }

    public void setRequestTime(
            long aRequestTime)
    {
        mRequestTime = aRequestTime;
    }

    public String getShortCode()
    {
        return mShortCode;
    }

    public void setShortCode(
            String aShortCode)
    {
        mShortCode = aShortCode;
    }

    public String getRequestStatus()
    {
        return mRequestStatus;
    }

    public void setRequestStatus(
            String aRequestStatus)
    {
        mRequestStatus = aRequestStatus;
    }

    @Override
    public String toString()
    {
        return "R3rRequestData [mRequestUrl=" + mRequestUrl + ", mUserAgent=" + mUserAgent + ", mRequestIpAddress=" + mRequestIpAddress + ", mShortCodeDataMap=" + mShortCodeDataMap + ", mRequestTime="
                + mRequestTime + ", mShortCode=" + mShortCode + ", mRequestStatus=" + mRequestStatus + "]";
    }

}