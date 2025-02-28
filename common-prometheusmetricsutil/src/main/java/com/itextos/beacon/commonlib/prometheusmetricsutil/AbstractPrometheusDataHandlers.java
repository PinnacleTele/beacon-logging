package com.itextos.beacon.commonlib.prometheusmetricsutil;

abstract class AbstractPrometheusDataHandlers
        implements
        IPrometheusDataHandlers
{

    protected final MetricType mType;
    protected final String     mMetricName;
    protected final String     mHelpInfo;
    protected final String[]   mLabels;
    protected final String     mLabelString;
    protected final String     mUniqueName;
    protected final boolean    mIsHavingLabels;

    AbstractPrometheusDataHandlers(
            MetricType aMetricType,
            String aMetricName,
            String aHelpInfo,
            String... aLabels)
    {
        mType           = aMetricType;
        mMetricName     = aMetricName;
        mHelpInfo       = aHelpInfo;
        mLabels         = aLabels;

        mIsHavingLabels = ((mLabels != null) && (mLabels.length > 0));

        if (mIsHavingLabels)
            mLabelString = Utility.getConcatenatedLabels(mLabels);
        else
            mLabelString = "NO_LABEL";
        mUniqueName = Utility.getUniqueName(mType, mMetricName);
    }

    @Override
    public String getUniqueName()
    {
        return mUniqueName;
    }

}