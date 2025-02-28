package com.itextos.beacon.inmemory.errorinfo.data;

public class PlatformErrorInfo
        implements
        IErrorInfo
{

    private final ErrorCategory mCategory;
    private final String        mDisplayError;
    private final String        mErrorCode;
    private final String        mErrorDesc;
    private final int           mHandoverAllParts;
    private final ErrorStatus   mStatusFlag;
    private final boolean       isDomSmsRateRefundable;
    private final boolean       isDomDltRateRefundable;

    public PlatformErrorInfo(
            String aErrorCode,
            String aErrorDesc,
            String aDisplayError,
            String aCategory,
            String aStatusFlag,
            int aHandoverAllParts,
            boolean aIsDomSmsRateRefundable,
            boolean aIsDomDltRateRefundable)
    {
        super();
        mErrorCode             = aErrorCode;
        mErrorDesc             = aErrorDesc;
        mDisplayError          = aDisplayError;
        mCategory              = ErrorCategory.getErrorCategroy(aCategory);
        mStatusFlag            = ErrorStatus.getErrorStatus(aStatusFlag);
        mHandoverAllParts      = aHandoverAllParts;
        isDomSmsRateRefundable = aIsDomSmsRateRefundable;
        isDomDltRateRefundable = aIsDomDltRateRefundable;
    }

    public ErrorCategory getCategory()
    {
        return mCategory;
    }

    public String getDisplayError()
    {
        return mDisplayError;
    }

    @Override
    public String getErrorCode()
    {
        return mErrorCode;
    }

    @Override
    public String getErrorDesc()
    {
        return mErrorDesc;
    }

    public int getHandoverAllParts()
    {
        return mHandoverAllParts;
    }

    public ErrorStatus getStatusFlag()
    {
        return mStatusFlag;
    }

    public boolean isDomSmsRateRefundable()
    {
        return isDomSmsRateRefundable;
    }

    public boolean isDomDltRateRefundable()
    {
        return isDomDltRateRefundable;
    }

    @Override
    public String toString()
    {
        return "PlatformErrorInfo [mCategory=" + mCategory + ", mDisplayError=" + mDisplayError + ", mErrorCode=" + mErrorCode + ", mErrorDesc=" + mErrorDesc + ", mHandoverAllParts="
                + mHandoverAllParts + ", mStatusFlag=" + mStatusFlag + ", isDomSmsRateRefundable=" + isDomSmsRateRefundable + ", isDomDltRateRefundable=" + isDomDltRateRefundable + "]";
    }

}