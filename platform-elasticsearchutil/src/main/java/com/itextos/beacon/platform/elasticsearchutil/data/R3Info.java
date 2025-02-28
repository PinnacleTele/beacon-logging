package com.itextos.beacon.platform.elasticsearchutil.data;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import com.itextos.beacon.commonlib.constants.ClusterType;

public class R3Info
{

    private static final int     DEFAULT_EXPIRY_DAYS = 45;
    public static final String   CLUSTER_TYPE        = "cluster_type";
    public static final String   ID                  = "id";
    public static final String   URL                 = "url";
    public static final String   SHORT_CODE          = "short_code";
    public static final String   MESSAGE_ID          = "message_id";
    public static final String   CLIENT_ID           = "client_id";
    public static final String   MOBILE_NUMBER       = "mobile_number";
    public static final String   SMART_LINK_ID       = "smart_link_id";
    public static final String   STATUS              = "status";
    public static final String   REASON              = "reason";
    public static final String   FILE_ID             = "file_id";
    public static final String   SHORTEN_URL         = "shorten_url";
    public static final String   EXPIRY_DATE         = "expiry_date7";
    public static final String   ADDITIONAL_INFO     = "add_info";
    public static final String   CREATE_TIME         = "created_time";

    public static final String[] INDEX_KEYS          =
    { CLIENT_ID, SHORT_CODE, MOBILE_NUMBER };

    private final ClusterType    mClusterType;
    private final String         mShortCode;
    private final Date           mExpiryDate;

    private String               mUrl;
    private String               mMessageId;
    private String               mClientId;
    private String               mMobileNumber;
    private Long                 mSmartLinkId;
    private String               mStatus;
    private String               mReason;

    private String               mFileId;
    private String               mShortenUrl;
    private Map<String, String>  additionalInfo;
    private final Date           mCreatedTs          = new Date();

    public R3Info(
            ClusterType aClusterType,
            String aShortCode,
            Date aExpiryDate)
    {
        mClusterType = aClusterType;
        mShortCode   = aShortCode;
        mExpiryDate  = getProperExpiryDate(aExpiryDate);
    }

    private static Date getProperExpiryDate(
            Date aExpiryDate)
    {
        if (aExpiryDate != null)
            return aExpiryDate;

        final Calendar c = Calendar.getInstance();
        c.setLenient(false);
        c.add(Calendar.DATE, DEFAULT_EXPIRY_DAYS);
        return c.getTime();
    }

    public String getUrl()
    {
        return mUrl;
    }

    public void setUrl(
            String aUrl)
    {
        mUrl = aUrl;
    }

    public String getShortCode()
    {
        return mShortCode;
    }

    public String getMessageId()
    {
        return mMessageId;
    }

    public void setMessageId(
            String aMessageId)
    {
        mMessageId = aMessageId;
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

    public String getMobileNumber()
    {
        return mMobileNumber;
    }

    public void setMobileNumber(
            String aMobileNumber)
    {
        mMobileNumber = aMobileNumber;
    }

    public Long getSmartLinkId()
    {
        return mSmartLinkId;
    }

    public void setSmartLinkId(
            Long aSmartLinkId)
    {
        mSmartLinkId = aSmartLinkId;
    }

    public String getStatus()
    {
        return mStatus;
    }

    public void setStatus(
            String aStatus)
    {
        mStatus = aStatus;
    }

    public String getReason()
    {
        return mReason;
    }

    public void setReason(
            String aReason)
    {
        mReason = aReason;
    }

    public String getFileId()
    {
        return mFileId;
    }

    public void setFileId(
            String aFileId)
    {
        mFileId = aFileId;
    }

    public String getShortenUrl()
    {
        return mShortenUrl;
    }

    public void setShortenUrl(
            String aShortenUrl)
    {
        mShortenUrl = aShortenUrl;
    }

    public Date getCreatedTs()
    {
        return mCreatedTs;
    }

    public Map<String, String> getAdditionalInfo()
    {
        return additionalInfo;
    }

    public void setAdditionalInfo(
            Map<String, String> aAdditionalInfo)
    {
        additionalInfo = aAdditionalInfo;
    }

    public ClusterType getClusterType()
    {
        return mClusterType;
    }

    public Date getExpiryDate()
    {
        return mExpiryDate;
    }

    @Override
    public String toString()
    {
        return "R3Info [mClusterType=" + mClusterType + ", mShortCode=" + mShortCode + ", mExpiryDate=" + mExpiryDate + ", mUrl=" + mUrl + ", mMessageId=" + mMessageId + ", mClientId=" + mClientId
                + ", mMobileNumber=" + mMobileNumber + ", mSmartLinkId=" + mSmartLinkId + ", mStatus=" + mStatus + ", mReason=" + mReason + ", mFileId=" + mFileId + ", mShortenUrl=" + mShortenUrl
                + ", additionalInfo=" + additionalInfo + ", mCreatedTs=" + mCreatedTs + "]";
    }

}