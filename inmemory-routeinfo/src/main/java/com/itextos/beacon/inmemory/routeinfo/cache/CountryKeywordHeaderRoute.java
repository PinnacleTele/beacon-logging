package com.itextos.beacon.inmemory.routeinfo.cache;

public class CountryKeywordHeaderRoute
{

    private final String mCountry;
    private final String mKeywords;
    private String       mDerivedKey;
    private int          mLogicId;
    private final String mHeader;
    private final String mPriority;
    private final String mRouteId;

    public CountryKeywordHeaderRoute(
            String aCountry,
            String aKeywords,
            String aHeader,
            String aPriority,
            String aRouteid)
    {
        super();
        mCountry  = aCountry;
        mKeywords = aKeywords;
        mHeader   = aHeader;
        mPriority = aPriority;
        mRouteId  = aRouteid;
    }

    public String getDerivedKey()
    {
        return mDerivedKey;
    }

    public void setDerivedKey(
            String aDerivedKey)
    {
        mDerivedKey = aDerivedKey;
    }

    public int getLogicId()
    {
        return mLogicId;
    }

    public void setLogicId(
            int aLogicId)
    {
        mLogicId = aLogicId;
    }

    public String getCountry()
    {
        return mCountry;
    }

    public String getKeywords()
    {
        return mKeywords;
    }

    public String getHeader()
    {
        return mHeader;
    }

    public String getPriority()
    {
        return mPriority;
    }

    public String getRouteId()
    {
        return mRouteId;
    }

    @Override
    public CountryKeywordHeaderRoute clone()
    {
        return new CountryKeywordHeaderRoute(mCountry, mKeywords, mHeader, mPriority, mRouteId);
    }

    @Override
    public String toString()
    {
        return "CountryKeywordSenderIDRoute [mCountry=" + mCountry + ", mKeywords=" + mKeywords + ", mDerivedKey=" + mDerivedKey + ", mLogicId=" + mLogicId + ", mHeader=" + mHeader + ", mPriority="
                + mPriority + ", mRouteId=" + mRouteId + "]";
    }

}
