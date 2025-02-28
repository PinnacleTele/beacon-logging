package com.itextos.beacon.inmemory.routeinfo.cache;

public class RouteConfigInfo
{

    private final String  mHeaderWhitelisted;
    private final String  mRouteType;
    private final String  mPrefix;
    private final boolean isPrefix;
    private final String  mSmscid;
    private final boolean isTxnRoute;
    private final boolean isPromoRoute;
    private final boolean isIntlRoute;
    private final boolean isDummyRoute;
    private final String  mDtimeFormat;
    private final String  mCarrierFullDn;
    private final String  mPromoHeaderType;
    private final String  mRouteId;
    private final String  mTelemartkerId;
    private final String  mTelemarketerTLVOption;
    private final String  mPromoHeader;

    public RouteConfigInfo(
            String aHeaderWhitelisted,
            String aRouteType,
            String aPrefix,
            boolean aIsPrefix,
            String aSmscid,
            boolean aIsTxnRoute,
            boolean aIsPromoRoute,
            boolean aIsIntlRoute,
            boolean aIsDummyRoute,
            String aDtimeFormat,
            String aCarrierFullDn,
            String aPromoHeaderType,
            String aRouteId,
            String aPromoHeader,
            String  aTelemartkerId,
            String  lTelemarketerTLVOption)
    {
        super();
        mHeaderWhitelisted = aHeaderWhitelisted;
        mRouteType         = aRouteType;
        mPrefix            = aPrefix;
        isPrefix           = aIsPrefix;
        mSmscid            = aSmscid;
        isTxnRoute         = aIsTxnRoute;
        isPromoRoute       = aIsPromoRoute;
        isIntlRoute        = aIsIntlRoute;
        isDummyRoute       = aIsDummyRoute;
        mDtimeFormat       = aDtimeFormat;
        mCarrierFullDn     = aCarrierFullDn;
        mPromoHeaderType   = aPromoHeaderType;
        mRouteId           = aRouteId;
        mPromoHeader       = aPromoHeader;
        mTelemartkerId	   = aTelemartkerId;
        mTelemarketerTLVOption			   = lTelemarketerTLVOption;
    }

    public String getHeaderWhitelisted()
    {
        return mHeaderWhitelisted;
    }

    public String getRouteType()
    {
        return mRouteType;
    }

    public String getPrefix()
    {
        return mPrefix;
    }

    public boolean isPrefix()
    {
        return isPrefix;
    }

    public String getSmscid()
    {
        return mSmscid;
    }

    public boolean isTxnRoute()
    {
        return isTxnRoute;
    }

    public boolean isPromoRoute()
    {
        return isPromoRoute;
    }

    public boolean isIntlRoute()
    {
        return isIntlRoute;
    }

    public boolean isDummyRoute()
    {
        return isDummyRoute;
    }

    public String getDtimeFormat()
    {
        return mDtimeFormat;
    }

    public String getCarrierFullDn()
    {
        return mCarrierFullDn;
    }

    public String getPromoHeaderType()
    {
        return mPromoHeaderType;
    }

    public String getRouteId()
    {
        return mRouteId;
    }
    
    public String getTelemartkerId()
    {
        return mTelemartkerId;
    }

    
    public String getTelemarketerTLVOption() {
    	
		return mTelemarketerTLVOption;
	}

	public String getPromoHeader()
    {
        return mPromoHeader;
    }

    @Override
    public String toString()
    {
        return "RouteConfigInfo [mHeaderWhitelisted=" + mHeaderWhitelisted + ", mRouteType=" + mRouteType + ", mPrefix=" + mPrefix + ", isPrefix=" + isPrefix + ", mSmscid=" + mSmscid + ", isTxnRoute="
                + isTxnRoute + ", isPromoRoute=" + isPromoRoute + ",  isIntlRoute=" + isIntlRoute + ", isDummyRoute=" + isDummyRoute + ", mDtimeFormat=" + mDtimeFormat + ", mCarrierFullDn="
                + mCarrierFullDn + ", mPromoHeaderType=" + mPromoHeaderType + ", mRouteId=" + mRouteId + ", mPromoHeader=" + mPromoHeader + "]";
    }

}
