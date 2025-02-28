package com.itextos.beacon.commonlib.messageprocessor.request;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;

public class ConsumerKafkaRequest
{

    protected final Component   mComponent;
    protected final ClusterType mClusterType;
    protected final String      mTopicName;

    public ConsumerKafkaRequest(
            Component aComponent,
            ClusterType aClusterType,
            String aTopicName)
    {
        super();
        mComponent   = aComponent;
        mClusterType = aClusterType;
        mTopicName   = aTopicName;
    }

    public Component getComponent()
    {
        return mComponent;
    }

    public ClusterType getClusterType()
    {
        return mClusterType;
    }

    public String getTopicName()
    {
        return mTopicName;
    }

    @Override
    public String toString()
    {
        return "ConsumerKafkaRequest [mComponent=" + mComponent + ", mClusterType=" + mClusterType + ", mTopicName=" + mTopicName + "]";
    }

}