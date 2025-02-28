package com.itextos.beacon.commonlib.messageprocessor.data.db;

public class KafkaTopicMap
{

    private final String mPlatformCluster;
    private final String mInterfaceGroup;
    private final String mMessageType;
    private final String mMessagePriority;
    private final String kafkaTopicPrefix;

    public KafkaTopicMap(
            String aPlatformCluster,
            String aInterfaceGroup,
            String aMessageType,
            String aMessagePriority,
            String aKafkaTopicPrefix)
    {
        super();
        mPlatformCluster = aPlatformCluster;
        mInterfaceGroup  = aInterfaceGroup;
        mMessageType     = aMessageType;
        mMessagePriority = aMessagePriority;
        kafkaTopicPrefix = aKafkaTopicPrefix;
    }

    public String getPlatformCluster()
    {
        return mPlatformCluster;
    }

    public String getInterfaceGroup()
    {
        return mInterfaceGroup;
    }

    public String getMessageType()
    {
        return mMessageType;
    }

    public String getMessagePriority()
    {
        return mMessagePriority;
    }

    public String getKafkaTopicPrefix()
    {
        return kafkaTopicPrefix;
    }

    @Override
    public String toString()
    {
        return "KafkaTopicMap [mPlatformCluster=" + mPlatformCluster + ", mInterfaceGroup=" + mInterfaceGroup + ", mMessageType=" + mMessageType + ", mMessagePriority=" + mMessagePriority
                + ", kafkaTopicPrefix=" + kafkaTopicPrefix + "]";
    }

}