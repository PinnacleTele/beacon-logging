package com.itextos.beacon.commonlib.messageprocessor.data.db;

public class KafkaClusterInfo
{

    private final String mKafkaClusterName;
    private final String mKafkaProducerProperties;
    private final String mKafkaConsumerProperties;

    public KafkaClusterInfo(
            String aKafkaClusterName,
            String aKafkaProducerProperties,
            String aKafkaConsumerProperties)
    {
        super();
        mKafkaClusterName        = aKafkaClusterName;
        mKafkaProducerProperties = aKafkaProducerProperties;
        mKafkaConsumerProperties = aKafkaConsumerProperties;
    }

    public String getKafkaClusterName()
    {
        return mKafkaClusterName;
    }

    public String getKafkaProducerProperties()
    {
        //return mKafkaProducerProperties;
        return "/kafka-producer.properties_"+System.getenv("profile");
    }

    public String getKafkaConsumerProperties()
    {
    //    return mKafkaConsumerProperties;
        return "/kafka-consumer.properties_"+System.getenv("profile");

    }

    @Override
    public String toString()
    {
        return "KafkaClusterInfo [mKafkaClusterName=" + mKafkaClusterName + ", mKafkaProducerProperties=" + mKafkaProducerProperties + ", mKafkaConsumerProperties=" + mKafkaConsumerProperties + "]";
    }

}