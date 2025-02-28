package com.itextos.beacon.commonlib.kafkaservice.consumer.partitionlogger;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.utility.CommonUtility;

public class KafkaParititionInfo
{

    private final Component          mComponent;
    private final PartitionEventType mParitionType;
    private final int                mPrometheusServerPort;
    private final String             mProcessId;
    private final boolean            mIsOnStartup;
    private final String             mTopic;
    private final int                mPartition;
    private final long               mKafkaOffset;
    private final long               mRedisOffset;
    private final long               mCreatedTime;

    public KafkaParititionInfo(
            Component aComponent,
            PartitionEventType aParitionType,
            int aPrometheusServerPort,
            boolean aIsOnStartup,
            String aTopic,
            int aPartition,
            long aKafkaOffset,
            long aRedisOffset)
    {
        super();
        mComponent            = aComponent;
        mParitionType         = aParitionType;
        mPrometheusServerPort = aPrometheusServerPort;
        mIsOnStartup          = aIsOnStartup;
        mTopic                = aTopic;
        mPartition             = aPartition;
        mKafkaOffset          = aKafkaOffset;
        mRedisOffset          = aRedisOffset;
        mProcessId            = CommonUtility.getJvmProcessId();
        mCreatedTime          = System.currentTimeMillis();
    }

    Component getComponent()
    {
        return mComponent;
    }

    PartitionEventType getParitionType()
    {
        return mParitionType;
    }

    int getPrometheusServerPort()
    {
        return mPrometheusServerPort;
    }

    boolean isOnStartup()
    {
        return mIsOnStartup;
    }

    String getTopic()
    {
        return mTopic;
    }

    int getPartition()
    {
        return mPartition;
    }

    long getKafkaOffset()
    {
        return mKafkaOffset;
    }

    long getRedisOffset()
    {
        return mRedisOffset;
    }

    long getCreatedTime()
    {
        return mCreatedTime;
    }

    String getProcessId()
    {
        return mProcessId;
    }

    @Override
    public String toString()
    {
        return "KafkaParititionInfo [mComponent=" + mComponent + ", mParitionType=" + mParitionType + ", mPrometheusServerPort=" + mPrometheusServerPort + ", mProcessId=" + mProcessId
                + ", mIsOnStartup=" + mIsOnStartup + ", mTopic=" + mTopic + ", mPartition=" + mPartition + ", mKafkaOffset=" + mKafkaOffset + ", mRedisOffset=" + mRedisOffset + ", mCreatedTime="
                + mCreatedTime + "]";
    }

}