package com.itextos.beacon.smpp.objects.bind;

import com.itextos.beacon.platform.smpputil.ISmppInfo;

public class UnbindInfoRedis
        implements
        ISmppInfo
{

    private final String  mClientId;
    private final String  mInstanceId;
    private final String  mSystemId;
    private final boolean mIsDn;

    public UnbindInfoRedis(
            String aClientId,
            String aInstanceId,
            String aSystemId,
            boolean aIsDn)
    {
        super();
        mClientId   = aClientId;
        mInstanceId = aInstanceId;
        mSystemId   = aSystemId;
        mIsDn       = aIsDn;
    }

    public String getClientId()
    {
        return mClientId;
    }

    public String getInstanceId()
    {
        return mInstanceId;
    }

    public String getSystemId()
    {
        return mSystemId;
    }

    public boolean isDn()
    {
        return mIsDn;
    }

    @Override
    public String toString()
    {
        return "UnbindInfoRedis [mClientId=" + mClientId + ", mInstanceId=" + mInstanceId + ", mSystemId=" + mSystemId + ", mIsDn=" + mIsDn + "]";
    }

}