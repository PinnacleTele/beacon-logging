package com.itextos.beacon.inmemory.smpp.account;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SmppAccInfo
{

    private final List<String> mBindTypes;
    private final int          mMaxConnAllowed;
    private final int          mMaxSpeed;
    private final String       mCharSet;
    private final String       mDltEntityIdTag;
    private final String       mDltTemplateIdTag;
    private final int          mDnExpiryInSec;
    private final String       mDnDateFormat;
    private final String       mClientMidTag;

    public SmppAccInfo(
            String aBindType,
            int aMaxConnAllowed,
            int aMaxSpeed,
            String aCharSet,
            String aDltEntityIdTag,
            String aDltTemplateIdTag,
            int aDnExpiryInSec,
            String aDnDateFormat,
            String aClientMidTag)
    {
        super();
        mBindTypes        = new ArrayList<>(Arrays.asList(aBindType.toUpperCase().split("~")));
        mMaxConnAllowed   = aMaxConnAllowed;
        mMaxSpeed         = aMaxSpeed;
        mCharSet          = aCharSet;
        mDltEntityIdTag   = aDltEntityIdTag;
        mDltTemplateIdTag = aDltTemplateIdTag;
        mDnExpiryInSec    = aDnExpiryInSec;
        mDnDateFormat     = aDnDateFormat;
        mClientMidTag     = aClientMidTag;
    }

    public List<String> getBindTypes()
    {
        return mBindTypes;
    }

    public int getMaxConnAllowed()
    {
        return mMaxConnAllowed;
    }

    public int getMaxSpeed()
    {
        return mMaxSpeed;
    }

    public String getCharSet()
    {
        return mCharSet;
    }

    public String getDltEntityIdTag()
    {
        return mDltEntityIdTag;
    }

    public String getDltTemplateIdTag()
    {
        return mDltTemplateIdTag;
    }

    public int getDnExpiryInSec()
    {
        return mDnExpiryInSec;
    }

    public String getDnDateFormat()
    {
        return mDnDateFormat;
    }

    public String getClientMidTag()
    {
        return mClientMidTag;
    }

    @Override
    public String toString()
    {
        return "SmppAccInfo [mBindTypes=" + mBindTypes + ", mMaxConnAllowed=" + mMaxConnAllowed + ", mMaxSpeed=" + mMaxSpeed + ", mCharSet=" + mCharSet + ", mDltEntityIdTag=" + mDltEntityIdTag
                + ", mDltTemplateIdTag=" + mDltTemplateIdTag + ", mDnExpiryInSec=" + mDnExpiryInSec + ", mDnDateFormat=" + mDnDateFormat + ", mClientMidTag=" + mClientMidTag + "]";
    }

}
