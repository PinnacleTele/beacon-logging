package com.itextos.beacon.inmemory.dlrquery.objects;

public class DlrConfigDetail
{

    private final String mBodyTemplate;
    private final String mBodyHeader;
    private final String mBodyFooter;
    private final String mBatchBodyDelimiter;

    public DlrConfigDetail(
            String aBodyTemplate,
            String aBodyHeader,
            String aBodyFooter,
            String aBatchBodyDelimiter)
    {
        super();
        mBodyTemplate       = aBodyTemplate;
        mBodyHeader         = aBodyHeader;
        mBodyFooter         = aBodyFooter;
        mBatchBodyDelimiter = aBatchBodyDelimiter;
    }

    public String getBodyTemplate()
    {
        return mBodyTemplate;
    }

    public String getBodyHeader()
    {
        return mBodyHeader;
    }

    public String getBodyFooter()
    {
        return mBodyFooter;
    }

    public String getBatchBodyDelimiter()
    {
        return mBatchBodyDelimiter;
    }

    @Override
    public String toString()
    {
        return "DlrConfigDetail [mBodyTemplate=" + mBodyTemplate + ", mBodyHeader=" + mBodyHeader + ", mBodyFooter=" + mBodyFooter + ", mBatchBodyDelimiter=" + mBatchBodyDelimiter + "]";
    }

}