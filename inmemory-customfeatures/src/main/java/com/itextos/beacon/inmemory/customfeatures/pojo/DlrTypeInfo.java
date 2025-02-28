package com.itextos.beacon.inmemory.customfeatures.pojo;

public class DlrTypeInfo
{

    String              mClientId;
    String              mDnType;
    String              mExpiryInSec;
    SingleDnProcessType mSingleDnProcessType;
    DNDeliveryMode      mDnHandoverMode;
    DNDeliveryMode      mAltHandoverMode;
    String              mHandoverStatus;
    String              mAltHandoverStatus;
    boolean             isWaitForAllParts;

    public DlrTypeInfo(
            String aClientId,
            String aDnType,
            String aExpiryInSec,
            SingleDnProcessType aSingleDnProcessType,
            DNDeliveryMode aDnHandoverBasedOn,
            String aHandoveRStatus,
            DNDeliveryMode aDnAltHandoverBasedOn,
            String aAltHandoverStatus,
            boolean aWaitForAllParts)
    {
        super();
        mClientId            = aClientId;
        mDnType              = aDnType;
        mExpiryInSec         = aExpiryInSec;
        mSingleDnProcessType = aSingleDnProcessType;
        mDnHandoverMode      = aDnHandoverBasedOn;
        mHandoverStatus      = aHandoveRStatus;
        mAltHandoverMode     = aDnAltHandoverBasedOn;
        mAltHandoverStatus   = aAltHandoverStatus;
        isWaitForAllParts    = aWaitForAllParts;
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

    public String getDnType()
    {
        return mDnType;
    }

    public void setDnType(
            String aDnType)
    {
        mDnType = aDnType;
    }

    public String getExpiryInSec()
    {
        return mExpiryInSec;
    }

    public void setExpiryInSec(
            String aExpiryInSec)
    {
        mExpiryInSec = aExpiryInSec;
    }

    public SingleDnProcessType getSingleDnProcessType()
    {
        return mSingleDnProcessType;
    }

    public void setSingleDnProcessType(
            SingleDnProcessType aSingleDnProcessType)
    {
        mSingleDnProcessType = aSingleDnProcessType;
    }

    public DNDeliveryMode getDnHandoverMode()
    {
        return mDnHandoverMode;
    }

    public void setDnHandoverMode(
            DNDeliveryMode aDnHandoverMode)
    {
        mDnHandoverMode = aDnHandoverMode;
    }

    public DNDeliveryMode getAltHandoverMode()
    {
        return mAltHandoverMode;
    }

    public void setAltHandoverMode(
            DNDeliveryMode aAltHandoverMode)
    {
        mAltHandoverMode = aAltHandoverMode;
    }

    public String getHandoverStatus()
    {
        return mHandoverStatus;
    }

    public void setHandoverStatus(
            String aHandoverStatus)
    {
        mHandoverStatus = aHandoverStatus;
    }

    public String getAltHandoverStatus()
    {
        return mAltHandoverStatus;
    }

    public void setAltHandoverStatus(
            String aAltHandoverStatus)
    {
        mAltHandoverStatus = aAltHandoverStatus;
    }

    public boolean isWaitForAllParts()
    {
        return isWaitForAllParts;
    }

    public void setWaitForAllParts(
            boolean aIsWaitForAllParts)
    {
        isWaitForAllParts = aIsWaitForAllParts;
    }

    @Override
    public String toString()
    {
        return "DlrTypeInfo [mClientId=" + mClientId + ", mDnType=" + mDnType + ", mExpiryInSec=" + mExpiryInSec + ", mSingleDnProcessType=" + mSingleDnProcessType + ", mDnHandoverMode="
                + mDnHandoverMode + ", mAltHandoverMode=" + mAltHandoverMode + ", mHandoverStatus=" + mHandoverStatus + ", mAltHandoverStatus=" + mAltHandoverStatus + ", isWaitForAllParts="
                + isWaitForAllParts + "]";
    }

}
