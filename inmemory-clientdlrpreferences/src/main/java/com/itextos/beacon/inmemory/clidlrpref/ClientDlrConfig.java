package com.itextos.beacon.inmemory.clidlrpref;

import com.itextos.beacon.commonlib.constants.DlrEnable;
import com.itextos.beacon.commonlib.constants.DlrHandoverMode;
import com.itextos.beacon.commonlib.constants.InterfaceType;
import com.itextos.beacon.commonlib.utility.CommonUtility;

public class ClientDlrConfig
{

    private final String                 mClientId;
    private final String                 mApp;
    private final InterfaceType          mRequestInterface;
    private final DlrEnable              mDlrEnabled;
    private final DlrHandoverMode        mDlrHandoverMode;
    private final boolean                mDlrQueryEnabled;
    private final boolean                mDlrOnSuccess;
    private final boolean                mDlrOnCarrierFailure;
    private final boolean                mDlrOnPlatformFail;
    private final ClientDlrAdminDelivery mDlrToSu;
    private final boolean                mClientSpecificHttpTopic;

    public ClientDlrConfig(
            String aClientId,
            String aApp,
            InterfaceType aRequestInterface,
            DlrEnable aDlrEnabled,
            DlrHandoverMode aDlrHandoverMode,
            boolean aDlrQueryEnabled,
            boolean aDlrOnSuccess,
            boolean aDlrOnCarrierFailure,
            boolean aDlrOnPlatformFail,
            ClientDlrAdminDelivery aDlrToSu,
            boolean aClientSpecificHttpTopic)
    {
        super();
        mClientId                = aClientId;
        mApp                     = aApp;
        mRequestInterface        = aRequestInterface;
        mDlrEnabled              = aDlrEnabled;
        mDlrHandoverMode         = aDlrHandoverMode;
        mDlrQueryEnabled         = aDlrQueryEnabled;
        mDlrOnSuccess            = aDlrOnSuccess;
        mDlrOnCarrierFailure     = aDlrOnCarrierFailure;
        mDlrOnPlatformFail       = aDlrOnPlatformFail;
        mDlrToSu                 = aDlrToSu;
        mClientSpecificHttpTopic = aClientSpecificHttpTopic;
    }

    public String getKey()
    {
        return CommonUtility.combine(mClientId, mApp, (mRequestInterface != null ? mRequestInterface.getKey() : ""));
    }

    public String getQueryKey()
    {
        return CommonUtility.combine(mClientId, mApp);
    }

    public String getClientId()
    {
        return mClientId;
    }

    public String getApp()
    {
        return mApp;
    }

    public InterfaceType getRequestInterface()
    {
        return mRequestInterface;
    }

    public DlrEnable getDlrEnabled()
    {
        return mDlrEnabled;
    }

    public DlrHandoverMode getDlrHandoverMode()
    {
        return mDlrHandoverMode;
    }

    public boolean isDlrQueryEnabled()
    {
        return mDlrQueryEnabled;
    }

    public boolean isDlrOnSuccess()
    {
        return mDlrOnSuccess;
    }

    public boolean isDlrOnCarrierFailure()
    {
        return mDlrOnCarrierFailure;
    }

    public boolean isDlrOnPlatformFail()
    {
        return mDlrOnPlatformFail;
    }

    public ClientDlrAdminDelivery getDlrToSu()
    {
        return mDlrToSu;
    }

    public boolean isClientSpecificHttpTopic()
    {
        return mClientSpecificHttpTopic;
    }

    @Override
    public String toString()
    {
        return "ClientDlrConfig [mClientId=" + mClientId + ", mApp=" + mApp + ", mRequestInterface=" + mRequestInterface + ", mDlrEnabled=" + mDlrEnabled + ", mDlrHandoverMode=" + mDlrHandoverMode
                + ", mDlrQueryEnabled=" + mDlrQueryEnabled + ", mDlrOnSuccess=" + mDlrOnSuccess + ", mDlrOnCarrierFailure=" + mDlrOnCarrierFailure + ", mDlrOnPlatformFail=" + mDlrOnPlatformFail
                + ", mDlrToSu=" + mDlrToSu + ", mClientSpecificHttpTopic=" + mClientSpecificHttpTopic + "]";
    }

}