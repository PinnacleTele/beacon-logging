package com.itextos.beacon.platform.singledn.data;

import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.platform.singledn.enums.DnStatus;

public class SingleDnInfo
{

    private final String   mMsgId;
    private final int      mPartNo;
    private final int      mTotalPartNos;
    private final long     mReceivedTime;
    private final long     mDeliveredTime;
    private final DnStatus mDnStatus;
    private String         mDNObject;

    public SingleDnInfo(
            String aMsgId,
            int aPartNo,
            int aTotalPartNos,
            long aReceivedTime,
            long aDeliveredTime,
            DnStatus aDnStatus)
    {
        super();
        mMsgId         = aMsgId;
        mPartNo        = aPartNo;
        mTotalPartNos  = aTotalPartNos;
        mReceivedTime  = aReceivedTime;
        mDeliveredTime = aDeliveredTime;
        mDnStatus      = aDnStatus;
    }

    public String getMsgId()
    {
        return mMsgId;
    }

    public int getPartNo()
    {
        return mPartNo;
    }

    public int getTotalPartNos()
    {
        return mTotalPartNos;
    }

    public long getReceivedTime()
    {
        return mReceivedTime;
    }

    public long getDeliveredTime()
    {
        return mDeliveredTime;
    }

    public DnStatus getDnStatus()
    {
        return mDnStatus;
    }

    public void setDNObject(
            String aDNObject)
    {
        mDNObject = aDNObject;
    }

    public String getDNObject()
    {
        return mDNObject;
    }

    @Override
    public String toString()
    {
        return "SingleDnInfo [mMsgId=" + mMsgId + ", mPartNo=" + mPartNo + ", mTotalPartNos=" + mTotalPartNos + ", mReceivedTime="
                + DateTimeUtility.getFormattedDateTime(mReceivedTime, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS) + ", mDeliveredTime="
                + DateTimeUtility.getFormattedDateTime(mDeliveredTime, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS) + ", mDnStatus=" + mDnStatus + "]";
    }

}