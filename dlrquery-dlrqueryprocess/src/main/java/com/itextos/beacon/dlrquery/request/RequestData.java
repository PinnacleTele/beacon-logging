package com.itextos.beacon.dlrquery.request;

import java.util.List;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.dlrquery.request.enums.DlrQueryStatus;
import com.itextos.beacon.dlrquery.request.enums.DlrQueryType;
import com.itextos.beacon.platform.elasticsearchutil.types.DlrQueryMulti;

public class RequestData
{

    final String   mAccessKey;
    final String   mRequestedIp;
    final String   mCustRefId;
    final String   mDest;
    final String   mFileId;
    final long     mRequestedTime;

    String         mCliId;
    List<String>   mDestList;
    List<String>   mFileIdList;
    List<String>   mCusrRefIdList;

    DlrQueryStatus mDlrQueryStatus = DlrQueryStatus.VALID_REQUEST;
    DlrQueryType   mDlrQueryType   = DlrQueryType.REGULAR;
    boolean        mIsMultiple     = false;
    boolean        mDestListBased;
    boolean        mFileIdListBased;
    boolean        mCustRefIdListBased;

    public RequestData(
            String aAccessKey,
            String aRequestedIp,
            String aFileId,
            String aCustRefId,
            String aDest)
    {
        mAccessKey     = CommonUtility.nullCheck(aAccessKey, true);
        mRequestedIp   = CommonUtility.nullCheck(aRequestedIp, true);
        mFileId        = CommonUtility.nullCheck(aFileId, true);
        mCustRefId     = CommonUtility.nullCheck(aCustRefId, true);
        mDest          = CommonUtility.nullCheck(aDest, true);

        mRequestedTime = System.currentTimeMillis();

        validate();
    }

    private void validate()
    {
        final ValidateRequest validateRequest = new ValidateRequest(this);
        validateRequest.validate();
    }

    public List<String> getDestList()
    {
        return mDestList;
    }

    public List<String> getFileIdList()
    {
        return mFileIdList;
    }

    public List<String> getCusrRefIdList()
    {
        return mCusrRefIdList;
    }

    public boolean isMultiple()
    {
        return mIsMultiple;
    }

    public String getCustRefId()
    {
        return mCustRefId;
    }

    public String getDest()
    {
        return mDest;
    }

    public String getFileId()
    {
        return mFileId;
    }

    public boolean isDestListBased()
    {
        return mDestListBased;
    }

    public boolean isFileIdListBased()
    {
        return mFileIdListBased;
    }

    public boolean isCustRefIdListBased()
    {
        return mCustRefIdListBased;
    }

    public DlrQueryMulti getDlrQueryRequest()
    {
        return new DlrQueryMulti(mCliId, mFileIdList, mCusrRefIdList, mDestList);
    }

    public DlrQueryStatus getDlrQueryStatus()
    {
        return mDlrQueryStatus;
    }

    public String getClientId()
    {
        return mCliId;
    }

    public void setClientId(
            String aClientId)
    {
        mCliId = aClientId;
    }

    @Override
    public String toString()
    {
        return "RequestData [mAccessKey=" + mAccessKey + ", mRequestedIp=" + mRequestedIp + ", mCustRefId=" + mCustRefId + ", mDest=" + mDest + ", mFileId=" + mFileId + ", mRequestedTime="
                + mRequestedTime + ", mCliId=" + mCliId + ", mDestList=" + mDestList + ", mFileIdList=" + mFileIdList + ", mCusrRefIdList=" + mCusrRefIdList + ", mDlrQueryStatus=" + mDlrQueryStatus
                + ", mDlrQueryType=" + mDlrQueryType + ", mIsMultiple=" + mIsMultiple + ", mDestListBased=" + mDestListBased + ", mFileIdListBased=" + mFileIdListBased + ", mCustRefIdListBased="
                + mCustRefIdListBased + "]";
    }

}