package com.itextos.beacon.platform.singledn.data;

import com.itextos.beacon.inmemory.customfeatures.pojo.DNDeliveryMode;
import com.itextos.beacon.inmemory.customfeatures.pojo.SingleDnProcessType;

/**
 * This class has to move to inmemory package.
 *
 * @author KS
 */
public class CustomerSingleDnInfo
{

    private final String              mClientId;
    private final SingleDnProcessType mSingleDnProcessType;
    private final boolean             mWaitForAllParts;
    private final DNDeliveryMode      mSingleDnResultSuccess;
    private final DNDeliveryMode      mSingleDnResultFailure;

    public CustomerSingleDnInfo(
            String aClientId,
            SingleDnProcessType aSingleDnProcessType,
            boolean aWaitForAllParts,
            DNDeliveryMode aSingleDnResultSuccess,
            DNDeliveryMode aSingleDnResultFailure)
    {
        super();
        mClientId              = aClientId;
        mSingleDnProcessType   = aSingleDnProcessType;
        mWaitForAllParts       = aWaitForAllParts;
        mSingleDnResultSuccess = aSingleDnResultSuccess;
        mSingleDnResultFailure = aSingleDnResultFailure;
    }

    public String getClientId()
    {
        return mClientId;
    }

    public SingleDnProcessType getSingleDnProcessType()
    {
        return mSingleDnProcessType;
    }

    public boolean isWaitForAllParts()
    {
        return mWaitForAllParts;
    }

    public DNDeliveryMode getSingleDnResultSuccess()
    {
        return mSingleDnResultSuccess;
    }

    public DNDeliveryMode getSingleDnResultFailure()
    {
        return mSingleDnResultFailure;
    }

    @Override
    public String toString()
    {
        return "CustomerSingleDnInfo [mClientId=" + mClientId + ", mSingleDnProcessType=" + mSingleDnProcessType + ", mWaitForAllParts=" + mWaitForAllParts + ", mSingleDnResultSuccess="
                + mSingleDnResultSuccess + ", mSingleDnResultFailure=" + mSingleDnResultFailure + "]";
    }

}