package com.itextos.beacon.commonlib.messageprocessor.data;

public class KafkaDBConstants
{

    public static final char      TOPIC_SEPARATOR                                         = '-';
    public static final String    DEFAULT                                                 = "DEFAULT";
    public static final String    OTP_SUFFIX                                              = "otp";
    public static final String    HIGH_SUFFIX                                             = "high";

    protected static final String TABLE_NAME_PLATFORM_CLUSTER                             = "cluster_type";
    protected static final String TABLE_NAME_KAFKA_CLUSTER                                = "kafka_cluster";
    protected static final String TABLE_NAME_KAFKA_TOPIC                                  = "kafka_topic";
    protected static final String TABLE_NAME_KAFKA_COMPONENT                              = "kafka_component";
    protected static final String TABLE_NAME_KAFKA_CONSUMER_GROUP                         = "kafka_consumer_group";
    public static final String    TABLE_NAME_CLIENT_SPECIFIC_COMPONENT                    = "client_specific_component";
    public static final String    TABLE_NAME_PLATFORM_CLUSTER_KAFKA_TOPIC_MAP             = "platform_cluster_kafka_topic_map";
    public static final String    TABLE_NAME_PLATFORM_CLUSTER_COMPONENT_KAFKA_CLUSTER_MAP = "platform_cluster_component_kafka_cluster_map";

    // kafka_cluster
    protected static final int    COL_INDEX_KC_KAFKA_CLUSTER_NAME                         = 1;
    protected static final int    COL_INDEX_KC_PRODUCER_PROPERTIES_FILE_PATH              = 2;
    protected static final int    COL_INDEX_KC_CONSUMER_PROPERTIES_FILE_PATH              = 3;

    // kafka_component
    protected static final int    COL_INDEX_KCM_COMPONENT                                 = 1;
    protected static final int    COL_INDEX_KCM_PROCESSOR_CLASS_NAME                      = 2;

    // platform_cluster_component_kafka_cluster_map
    protected static final int    COL_INDEX_PCCKCM_COMPONENT_NAME                         = 1;
    protected static final int    COL_INDEX_PCCKCM_PLATFORM_CLUSTER_NAME                  = 2;
    protected static final int    COL_INDEX_PCCKCM_KAFKA_CLUSTER_PRODUCER                 = 3;
    protected static final int    COL_INDEX_PCCKCM_KAFKA_CLUSTER_CONSUMER                 = 4;
    protected static final int    COL_INDEX_PCCKCM_CONSUMER_GROUP_NAME                    = 5;
    protected static final int    COL_INDEX_PCCKCM_KAFKA_CLIENT_CONSUMER_COUNT            = 6;
    protected static final int    COL_INDEX_PCCKCM_SLEEP_TIME_IN_MILLIS                   = 7;
    protected static final int    COL_INDEX_PCCKCM_THREADS_COUNT                          = 8;
    protected static final int    COL_INDEX_PCCKCM_INTL_THREADS_COUNT                     = 9;
    protected static final int    COL_INDEX_PCCKCM_MAX_PRODUCERS_PER_TOPIC                = 10;

    // client_specific_component
    protected static final int    COL_INDEX_CPC_COMPONENT_NAME                            = 1;
    protected static final int    COL_INDEX_CPC_CLIENT_ID                                 = 2;

    // platform_cluster_kafka_topic_map
    protected static final int    COL_INDEX_PCKTM_PLATFORM_CLUSTER_NAME                   = 1;
    protected static final int    COL_INDEX_PCKTM_INTERFACE_GROUP_NAME                    = 2;
    protected static final int    COL_INDEX_PCKTM_MSG_TYPE                                = 3;
    protected static final int    COL_INDEX_PCKTM_MSG_PRIORITY                            = 4;
    protected static final int    COL_INDEX_PCKTM_KAFKA_TOPIC_PREFIX                      = 5;

}