package com.itextos.beacon.commonlib.commonpropertyloader;

public enum PropertiesPath
{

    COMMON_K2ES_PROPERTIES("commmon.k2es.properties.key"),
    COMMON_DATABASE_PROPERTIES("commmon.database.properties.key"),
    COMMON_REDIS_PROPERTIES("common.redis.properties.key"),
    MESSAGE_ID_GENERATOR_PROPERTIES("messageid.generator.properties.key"),
    TIMER_PROCESSOR_INTERVAL_PROPERTIES("timer.processor.interval.properties.key"),
    GENERICAPI_PROPERTIES("genericapi.properties.key"),
    SMPPAPI_PROPERTIES("smppapi.properties.key"),
    JNDI_PROPERTIES("jndi.properties.key"),
    GSM_REPLACE_PROPERTIES("gsm.replace.charset.properties.key"),
    ISO_REPLACE_PROPERTIES("iso.replace.charset.properties.key"),
    COMMON_PROPERTIES("common.properties.key"),
    DLT_TEMPLATES_CONFIG_PROPERTIES("dlt.template.config.properties.key"),
    FAILLIST_PROPERTTIES("faillist.properties.key"),
    DN_PAYLOAD_PARAMS_PROPERTIES("dn.payload.parms.properties.key"),
    DN_CALLBACK_PARAMS_PROPERTIES("dn.callback.params.properties.key"),
    HTTP_CONNECTOR_PROPERTIES("http.connector.properties.key"),
    DLR_PAYLOAD_GEN_PROPERTIES("dlr.payload.gen.properties.key"),
    TIME_ZONE_PROPERTIES("time.zone.properties.key"),
    MESSAGE_KEY_REMOVE_PROPERTIES("message.keys.remove.properties.key"),
    EXCEPTION_CASE_PROPERTIES("exception.case.properties.key"),
    R3C_ELASTIC_ADDINFO_PATH("r3c.elastic.addinfo.properties.key"),
    SHORTCODE_PROVIDER_PATH("shortcode.provider.properties.key"),
    DND_PROPERTIES("dnd.properties.key"),
    PROMETHEUS_CONTROL_PROPERTIES("prometheus.control.properties.key"),
    KAFKA_CUSTOM_PROPERTIES("kafka.custom.properties.key"),
    KAFKA_2_ELASTICSEARCH_PROPERTIES("kafka.2.elasticsearch.properties.key"),
    ELASTICSEARCH_SERVER_PROPERTIES("elasticsearch.server.properties.key"),
    WALLET_KAFKA_PRODUCER_PROPERTIES("wallet.kafka.producer.properties.key"),
    WALLET_KAFKA_CONSUMER_PROPERTIES("wallet.kafka.consumer.properties.key"),
    WALLET_HISTORY_PROPERTIES("wallet.history.properties.key"),
    CLOUD_INTERFACE_PROPERTIES("cloud.interface.properties.key"),
    PREPAID_MIGRATION_PROPERTIES("prepaid.migration.properties.key"),
    FTP_APPLICATION_PROPERTIES("ftp.application.properties.key"),
    WALLET_BALANCE_REMINDER_PROPERTIES("wallet.balance.reminder.properties.key"),
    SCHEDULER_PROPERTIES("scheduler.properties.key"),

    ;

    private final String propertiesKey;

    PropertiesPath(
            String aPropertiesKey)
    {
        propertiesKey = aPropertiesKey;
    }

    public String getKey()
    {
        return propertiesKey;
    }

}
