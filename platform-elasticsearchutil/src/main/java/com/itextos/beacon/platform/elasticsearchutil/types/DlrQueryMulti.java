package com.itextos.beacon.platform.elasticsearchutil.types;

import java.util.List;

public class DlrQueryMulti
{

    private final String       mClientId;
    private final List<String> mFileIdList;
    private final List<String> mCustRefIdList;
    private final List<String> mDestList;

    public DlrQueryMulti(
            String aClientId,
            List<String> aFileIdList,
            List<String> aCustRefIdList,
            List<String> aDestList)
    {
        super();
        mClientId      = aClientId;
        mFileIdList    = aFileIdList;
        mCustRefIdList = aCustRefIdList;
        mDestList      = aDestList;
    }

    public String getClientId()
    {
        return mClientId;
    }

    public List<String> getFileIdList()
    {
        return mFileIdList;
    }

    public List<String> getCliMsgIdList()
    {
        return mCustRefIdList;
    }

    public List<String> getDestList()
    {
        return mDestList;
    }

    public boolean isFileIdBased()
    {
        return !mFileIdList.isEmpty();
    }

    public boolean isDestBased()
    {
        return !mDestList.isEmpty();
    }

    public boolean isCliMsgIdBased()
    {
        return !mCustRefIdList.isEmpty();
    }

    @Override
    public String toString()
    {
        return "DlrQueryMulti [mClientId=" + mClientId + ", mFileIdList=" + mFileIdList + ", mCustRefIdList=" + mCustRefIdList + ", mDestList=" + mDestList + "]";
    }

}