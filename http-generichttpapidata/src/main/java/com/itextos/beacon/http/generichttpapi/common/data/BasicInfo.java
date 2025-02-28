package com.itextos.beacon.http.generichttpapi.common.data;

import java.io.Serializable;

import org.json.simple.JSONObject;

import com.itextos.beacon.commonlib.constants.AccountStatus;
import com.itextos.beacon.commonlib.constants.ClusterType;

public class BasicInfo
        implements
        Serializable
{

    private static final long      serialVersionUID = -6168378355035775726L;

    private final String           mVersion;
    private final String           mAccessKey;
    private final String           mEncrypt;
    private final String           mCustIp;
    private final long             mRequestedTime;
    private String                 mFileId;
    private JSONObject             mUserAccountInfo;
    private InterfaceRequestStatus mRequestStatus;
    private String                 mClientId;
    private String                 mReportingKey;
    private String                 mBatchNo;
    private String                 mScheduleTime;
    private AccountStatus          mAccountStatus;
    private boolean                mIsAsync;
    private ClusterType            mClusterType;

    public BasicInfo(
            String aVersion,
            String aAccessKey,
            String aEncrypt,
            String aScheduleTime,
            String aCustomerIP,
            long aRequestedTime)
    {
        super();
        mVersion       = aVersion;
        mAccessKey     = aAccessKey;
        mEncrypt       = aEncrypt;
        mScheduleTime  = aScheduleTime;
        mCustIp        = aCustomerIP;
        mRequestedTime = aRequestedTime;
    }

    public String getAccessKey()
    {
        return mAccessKey;
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

    public JSONObject getUserAccountInfo()
    {
        return mUserAccountInfo;
    }

    public void setUserAccountInfo(
            JSONObject aUserAccountInfo)
    {
        mUserAccountInfo = aUserAccountInfo;
    }

    public InterfaceRequestStatus getRequestStatus()
    {
        return mRequestStatus;
    }

    public void setRequestStatus(
            InterfaceRequestStatus aRequestStatus)
    {
        mRequestStatus = aRequestStatus;
    }

    public String getReportingKey()
    {
        return mReportingKey;
    }

    public void setReportingKey(
            String aReportingKey)
    {
        mReportingKey = aReportingKey;
    }

    public String getBatchNo()
    {
        return mBatchNo;
    }

    public void setBatchNo(
            String aBatchNo)
    {
        mBatchNo = aBatchNo;
    }

    public String getVersion()
    {
        return mVersion;
    }

    public String getEncrypt()
    {
        return mEncrypt;
    }

    public String getCustIp()
    {
        return mCustIp;
    }

    public long getRequestedTime()
    {
        return mRequestedTime;
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

    public String getScheduleTime()
    {
        return mScheduleTime;
    }

    public void setScheduleTime(
            String aScheduleTime)
    {
        mScheduleTime = aScheduleTime;
    }

    public AccountStatus getAccountStatus()
    {
        return mAccountStatus;
    }

    public void setAccountStatus(
            AccountStatus aAccountStatus)
    {
        mAccountStatus = aAccountStatus;
    }

    public void setIsAsync(
            boolean aIsAsync)
    {
        mIsAsync = aIsAsync;
    }

    public boolean isIsAsync()
    {
        return mIsAsync;
    }

    public ClusterType getClusterType()
    {
        return mClusterType;
    }

    public void setClusterType(
            ClusterType aClusterType)
    {
        mClusterType = aClusterType;
    }

    @Override
    public String toString()
    {
        return "BasicInfo [mVersion=" + mVersion + ", mAccessKey=" + mAccessKey + ", mEncrypt=" + mEncrypt + ", mCustIp=" + mCustIp + ", mRequestedTime=" + mRequestedTime + ", mFileId=" + mFileId
                + ", mUserAccountInfo=" + mUserAccountInfo + ", mRequestStatus=" + mRequestStatus + ", mClientId=" + mClientId + ", mReportingKey=" + mReportingKey + ", mBatchNo=" + mBatchNo
                + ", mScheduleTime=" + mScheduleTime + ", mAccountStatus=" + mAccountStatus + ", mIsAsync=" + mIsAsync + ", mClusterType=" + mClusterType + "]";
    }

}