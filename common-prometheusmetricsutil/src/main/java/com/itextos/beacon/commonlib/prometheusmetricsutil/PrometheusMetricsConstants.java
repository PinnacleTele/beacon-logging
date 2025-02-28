package com.itextos.beacon.commonlib.prometheusmetricsutil;

class PrometheusMetricsConstants
{

    protected static final String KAFKA_PRODUCER_COUNT       = "KafkaProducerCount";
    protected static final String KAFKA_CONSUMER_COUNT       = "KafkaConsumerCount";

    protected static final String COMPONENT_MESSAGE_COUNT    = "ComponentMessageCount";
    protected static final String COMPONENT_PROCESS_LATENCY  = "ComponentProcessLatency";

    protected static final String API_USER_ACCEPT_COUNT      = "ApiAcceptCount";
    protected static final String API_USER_STATUS_COUNT      = "ApiStatusCount";
    protected static final String API_USER_PROCESS_LATENCY   = "ApiProcessLatency";

    protected static final String UI_MESSAGE_COUNT           = "UiMessageCount";
    protected static final String FTP_MESSAGE_COUNT          = "FtpMessageCount";

    protected static final String COMPONENT_METHOD_LATENCY   = "ComponentMethodLatency";

    // SMPP
    protected static final String SMPP_BIND_REQUEST          = "SmppBindRequest";
    protected static final String SMPP_BIND_RESPONSE         = "SmppBindResponse";
    protected static final String SMPP_BIND_LATENCY          = "SmppBindLatency";

    protected static final String SMPP_BIND_ERROR            = "SmppBindError";
    protected static final String SMPP_ACTIVE_BINDS          = "SmppActiveBinds";

    protected static final String SMPP_ENQUIRE_LINK_REQUEST  = "SmppEnquireLinkRequest";
    protected static final String SMPP_ENQUIRE_LINK_RESPONSE = "SmppEnquireLinkResponse";
    protected static final String SMPP_ENQUIRE_LINK_LATENCY  = "SmppEnquireLinkLatency";

    protected static final String SMPP_SUBMITSM_REQUEST      = "SmppSubmitSmRequest";
    protected static final String SMPP_SUBMITSM_RESPONSE     = "SmppSubmitSmResponse";
    protected static final String SMPP_SUBMITSM_LATENCY      = "SmppSubmitSmLatency";

    protected static final String SMPP_DELIVERSM_REQUEST     = "SmppDeliverSmRequest";
    protected static final String SMPP_DELIVERSM_RESPONSE    = "SmppDeliverSmResponse";
    protected static final String SMPP_DELIVERSM_LATENCY     = "SmppDeliverSmLatency";
    protected static final String SMPP_DELIVERSM_FAILURE     = "SmppDeliverSmFailure";

    protected static final String SMPP_UNBIND_REQUEST        = "SmppUnbindRequest";
    protected static final String SMPP_UNBIND_RESPONSE       = "SmppUnbindResponse";
    protected static final String SMPP_UNBIND_LATENCY        = "SmppUnbindLatency";

    protected static final String SMPP_UNBIND_COUNTS         = "SmppUnbindCounts";
    protected static final String SMPP_FAILURE_COUNTS        = "SmppFailureCounts";

    protected static final String PLATFORM_REJECTION_COUNTS  = "PlatformRejectionCounts";
    protected static final String GENERIC_ERROR_COUNTS       = "GenericErrorCounts";

    protected static final String MESSAGE_SOURCE             = "MessageSource";
    protected static final String CLUSTER_NAME               = "ClusterName";
    protected static final String COMPONENT_NAME             = "ComponentName";
    protected static final String IP                         = "IP";
    protected static final String API                        = "Api";
    protected static final String USER                       = "User";
    protected static final String TOPIC                      = "Topic";
    protected static final String METHOD_NAME                = "MethodName";
    protected static final String STATUS_CODE                = "StatusCode";
    protected static final String PLATFORM_ERROR_CODE        = "PlatformErrorCode";
    protected static final String PLATFORM_ERROR_DESC        = "PlatformErrorDesc";
    protected static final String GENERIC_ERROR_CODE         = "GenericErrorCode";
    protected static final String GENERIC_ERROR_DESC         = "GenericErrorDesc";

    protected static final String INSTANCE_ID                = "InstanceId";
    protected static final String SYSTEM_ID                  = "SystemId";
    protected static final String CLIENT_IP                  = "ClientIp";
    protected static final String BIND_TYPE                  = "BindType";
    protected static final String ERROR_CODE                 = "ErrorCode";
    protected static final String REASON                     = "Reason";

}