package com.itextos.beacon.platform.promokannelrediscleaner.process.redisprocess;

import java.util.Map;

import com.itextos.beacon.commonlib.utility.CommonUtility;

class DlrRedisData
{

    private static final String R_KEY_SMSC        = "smsc";
    private static final String R_KEY_SERVICE     = "service";
    private static final String R_KEY_BOXC        = "boxc";
    private static final String R_KEY_MASK        = "mask";
    private static final String R_KEY_SOURCE      = "source";
    private static final String R_KEY_DESTINATION = "destination";
    private static final String R_KEY_URL         = "url";
    private static final String R_KEY_STATUS      = "status";
    private static final String R_KEY_TS          = "ts";

    private final String        mSmsc;
    private final String        mService;
    private final String        mBoxc;
    private final String        mMask;
    private final String        mSource;
    private final String        mDestination;
    private final String        mUrl;
    private final String        mStatus;
    private final String        mOperatorMsgId;

    private final UrlObject     mUrlObject;

    DlrRedisData(
            Map<String, String> aRedisValues)
    {
        super();
        mSmsc          = CommonUtility.nullCheck(aRedisValues.get(R_KEY_SMSC), true);
        mService       = CommonUtility.nullCheck(aRedisValues.get(R_KEY_SERVICE), true);
        mBoxc          = CommonUtility.nullCheck(aRedisValues.get(R_KEY_BOXC), true);
        mMask          = CommonUtility.nullCheck(aRedisValues.get(R_KEY_MASK), true);
        mSource        = CommonUtility.nullCheck(aRedisValues.get(R_KEY_SOURCE), true);
        mDestination   = CommonUtility.nullCheck(aRedisValues.get(R_KEY_DESTINATION), true);
        mUrl           = CommonUtility.nullCheck(aRedisValues.get(R_KEY_URL), true);
        mStatus        = CommonUtility.nullCheck(aRedisValues.get(R_KEY_STATUS), true);
        mOperatorMsgId = CommonUtility.nullCheck(aRedisValues.get(R_KEY_TS), true);

        mUrlObject     = DlrParser.getUrlObject(mUrl);
        mUrlObject.setDest(mDestination);
        mUrlObject.setHeader(mSource);
        mUrlObject.setOperatorMsgId(mOperatorMsgId);
    }

    public String getSmsc()
    {
        return mSmsc;
    }

    public String getService()
    {
        return mService;
    }

    public String getBoxc()
    {
        return mBoxc;
    }

    public String getMask()
    {
        return mMask;
    }

    public String getSource()
    {
        return mSource;
    }

    public String getDestination()
    {
        return mDestination;
    }

    public String getUrl()
    {
        return mUrl;
    }

    public String getStatus()
    {
        return mStatus;
    }

    public String getOperatorMsgId()
    {
        return mOperatorMsgId;
    }

    public UrlObject getUrlObject()
    {
        return mUrlObject;
    }

    @Override
    public String toString()
    {
        return "DlrRedisData [mSmsc=" + mSmsc + ", mService=" + mService + ", mBoxc=" + mBoxc + ", mMask=" + mMask + ", mSource=" + mSource + ", mDestination=" + mDestination + ", mUrl=" + mUrl
                + ", mStatus=" + mStatus + ", mOperatorMsgId=" + mOperatorMsgId + ", mUrlObject=" + mUrlObject + "]";
    }

}
