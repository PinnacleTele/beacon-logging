package com.itextos.beacon.commonlib.messageprocessor.data.db;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;

public class KafkaClusterComponentMap
{

    private final Component   mComponent;
    private final ClusterType mPlatformClusterType;
    private final String      mKafkaProducerClusterName;
    private final String      mKafkaConsumerClusterName;
    private final String      mKafkaConsumerGroupName;
    private final int         mKafkaClientConsumerCount;
    private final int         mSleepTimeInMillis;
    private final int         mThreadsCount;
    private final int         mIntlThreadCount;
    private final int         mMaxProducersPerTopic;

    public KafkaClusterComponentMap(
            Component aComponent,
            ClusterType aPlatformClusterType,
            String aKafkaProducerClusterName,
            String aKafkaConsumerClusterName,
            String aKafkaConsumerGroupName,
            int aKafkaClientConsumerCount,
            int aSleepTimeInMillis,
            int aThreadsCount,
            int aIntlThreadCount,
            int aMaxProducersPerTopic)
    {
        super();
        mComponent                = aComponent;
        mPlatformClusterType      = aPlatformClusterType;
        mKafkaProducerClusterName = aKafkaProducerClusterName;
        mKafkaConsumerClusterName = aKafkaConsumerClusterName;
        mKafkaConsumerGroupName   = aKafkaConsumerGroupName;
        mKafkaClientConsumerCount = aKafkaClientConsumerCount;
        mSleepTimeInMillis        = aSleepTimeInMillis;
        mThreadsCount             = aThreadsCount;
        mIntlThreadCount          = aIntlThreadCount;
        mMaxProducersPerTopic     = aMaxProducersPerTopic;
    }

    public ClusterType getPlatformClusterType()
    {
        return mPlatformClusterType;
    }

    public Component getComponent()
    {
        return mComponent;
    }

    public String getKafkaProducerClusterName()
    {
        return mKafkaProducerClusterName;
    }

    public String getKafkaConsumerClusterName()
    {
        return mKafkaConsumerClusterName;
    }

    public String getKafkaConsumerGroupName()
    {
        return mKafkaConsumerGroupName;
    }

    public int getKafkaClientConsumerCount()
    {
        return mKafkaClientConsumerCount;
    }

    public int getSleepTimeInMillis()
    {
        return mSleepTimeInMillis;
    }

    public int getThreadsCount()
    {
        return mThreadsCount;
    }

    public int getIntlThreadsCount()
    {
        return mIntlThreadCount;
    }

    public int getMaxProducersPerTopic()
    {
        return mMaxProducersPerTopic;
    }

    @Override
    public String toString()
    {
        return "KafkaClusterComponentMap [mComponent=" + mComponent + ", mPlatformClusterType=" + mPlatformClusterType + ", mKafkaProducerClusterName=" + mKafkaProducerClusterName
                + ", mKafkaConsumerClusterName=" + mKafkaConsumerClusterName + ", mKafkaConsumerGroupName=" + mKafkaConsumerGroupName + ", mKafkaClientConsumerCount=" + mKafkaClientConsumerCount
                + ", mSleepTimeInMillis=" + mSleepTimeInMillis + ", mThreadsCount=" + mThreadsCount + ", mIntlThreadCount=" + mIntlThreadCount + ", mMaxProducersPerTopic=" + mMaxProducersPerTopic
                + "]";
    }

}