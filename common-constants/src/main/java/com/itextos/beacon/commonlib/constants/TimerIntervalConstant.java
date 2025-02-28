package com.itextos.beacon.commonlib.constants;

public enum TimerIntervalConstant
        implements
        IntervalConstants
{

    KANNEL_STATUS_REFRESH("kannel.status.refresh", _30_SECS),
    KANNEL_RESPONSE_REFRESH("kannel.response.refresh", _30_SECS),
    KANNEL_AVALIABILITY_REFRESH("kannel.availability.refresh", _30_SECS),
    PAYLOAD_UPDATE_TASK_RELOAD("payload.update.reload", _5_MINS),
    PAYLOAD_DELETE_TASK_RELOAD("payload.delete.reload", _5_MINS),
    DLR_PAYLOAD_GEN_REFRESH("dlr.payload.refresh", _5_MINS),
    SINGLE_DN_DELETE_PROCESS("single.dn.delete.process", _1_SEC),
    SINGLE_DN_DUPCHECK_PROCESS("single.dn.dupcheck.process", _30_SECS),
    SINGLE_DN_EXPIRY_PROCESS("single.dn.expiry.process", _30_SECS),
    DLR_HTTP_HANDOVER_EXPIRED_MESSAGE_LOG_INTERVAL("dlr.http.handover.expired.message.log.interval", _30_SECS),
    DLR_HTTP_HANDOVER_HANDOVER_RETRY_REAPER("dlr.http.handover.handover.retry.reaper", _1_SEC),
    DLR_HTTP_HANDOVER_REDIS_PUSH_INTERVAL("dlr.http.handover.redis.push.interval", _30_SECS),
    DLR_HTTP_HANDOVER_REDIS_RETRY_PUSH_INTERVAL("dlr.http.handover.redis.retry.push.interval", _30_SECS),
    DATA_REFRESHER_RELOAD_INTERVAL("data.refresh.reload.interval", _30_SECS),
    ELASTIC_SEARCH_INMEMORY_PUSH("elastic.search.inmemory.push", _5_SECS),
    ELASTIC_SEARCH_CONNECTION_REAPER("elastic.search.connection.reaper", _10_SECS),
    REDIS_STATISTICS_READER("redis.statistics.reader", _30_SECS),
    INTERFACE_FILE_POLLER_LOOKUP_INTERVAL("interface.file.poller.lookup.interval", _10_SECS),
    INTERFACE_FALLBACK_TABLE_INSERTER("interface.fallback.table.inserter", _30_SECS),
    INTERFACE_FALLBACK_TABLE_READER("interface.fallback.table.reader", _30_SECS),
    INTERFACE_PARAMETER_LOADER("interface.parameter.loader", _30_SECS),
    TIMEBOUND_MESSAGE_REAPER("timebound.message.reaper", _1_SEC),
    SCHEDULE_MESSAGE_TABLE_INSERTER("schedule.message.table.inserter", _10_SECS),
    SCHEDULE_MESSAGE_TABLE_READER("schedule.message.table.reader", _1_SEC),
    MESSAGE_REMOVE_PROPERTY_UPDATER("message.remove.property.updater", _30_SECS),
    SHORT_CODE_COUNT_CHECER("shortcode.count.checker", _30_SECS),
    PROMETHEUS_CONTROL_REFRESH("prometheus.control.checker", _30_SECS),
    DUPLICATE_CHECK_EXPIRY_TASK_INTERVAL("duplicate.check.expiry.task.interval", _1_SEC),
    KAFKA_CUSTOM_PROPERTIES_RELOAD("kafka.custom.properties.reload", _30_SECS),
    ONNET_TABLE_INFO_REFRESH("onnet.table.reload.interval", _30_SECS),
    CARRIER_ERROR_INFO_REFRESH("carrier.error.info.reload.interval", _30_SECS),
    KAFKA_PRODUCER_FALLBACK_DATA("kafka.producer.fallback.data.interval", _5_SECS),
    KAFKA_PARTITION_INFO_INSERT("kafka.partition.info.insert.interval", _5_SECS),
    SMPP_CONCAT_MESSAGE_CHECKER_INTERVAL("smpp.concat.message.checker.interval", _5_SECS),
    SMPP_CONCAT_MESSAGE_EXPIRY_INTERVAL("smpp.concat.message.expiry.interval", _30_SECS),
    SMPP_DLR_INMEM_PROCESS_INTERVAL("smpp.dlr.inmem.process.interval", _1_SEC),
    SMPP_DLR_FALLBACK_TABLE_READER("smpp.dlr.fallback.table.reader", _5_SECS),
    NO_PAYLOAD_RETRY_TABLE_READER("no.payload.retry.table.reader", _5_SECS),
    PROMO_KANNEL_REDIS_CLEANER_INTERVAL("promo.kannel.redis.cleaner.interval", _30_SECS),
    CURRENT_DATE_CURRENCY_CONVERSION_RELOAD_INTERVAL("current.date.currency.conversion.reload.interval", _1_MIN),
    CURRENT_MONTH_CURRENCY_CONVERSION_RELOAD_INTERVAL("current.month.currency.conversion.reload.interval", _1_MIN),
    SMPP_CONCAT_ORPHAN_MESSAGE_EXPIRY_INTERVAL("smpp.concat.orphan.message.expiry.interval", _15_MINS),

    ;

    private String intervalName          = null;
    private int    defaultDurationInSecs = INVALID;

    TimerIntervalConstant(
            String aIntervalName,
            int aDefaultDurationInSecs)
    {
        this.intervalName          = aIntervalName;
        this.defaultDurationInSecs = aDefaultDurationInSecs;
    }

    public String getIntervalName()
    {
        return intervalName;
    }

    public int getDurationInSecs()
    {
        return defaultDurationInSecs;
    }

}
