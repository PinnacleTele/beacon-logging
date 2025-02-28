package com.itextos.beacon.commonlib.constants;

public enum ConfigParamConstants
        implements
        ItextosEnum
{

    TRAI_BLOCKOUT_START("trai.blockout.start"),
    TRAI_BLOCKOUT_STOP("trai.blockout.end"),
    REQUIRE_UDH_VALIDATION("require.udh.validation"),
    DOMESTIC_GLOBAL_FAILLIST_CHK("domestic.global.faillist.chk"),
    MAX_SPLIT_PART_ALLOW("max.split.part.allow"),
    MSC_CODE_SPLIT_CHK("msc.code.split.chk"),
    VALIDATE_DLT_TEMPLATE("validate.dlt.template"),
    SUB_DND_STATUS("submission.dnd.status"),
    SUB_DND_ROUTE_ID("submission.dnd.route.id"),
    DELV_DND_STATUS("deliveries.dnd.status"),
    DELV_DND_ROUTE_ID("deliveries.dnd.route.id"),
    ACC_FAILLIST_DOMESTIC_ROUTE_ID("acc.faillist.domestic.route.id"),
    GLOBAL_FAILLIST_DOMESTIC_ROUTE_ID("global.faillist.domestic.route.id"),
    INTL_GLOBAL_FAILIST_ROUTE_ID("intl.global.faillist.route.id"),
    INTL_CLIENT_FAILLIST_ROUTE_ID("intl.client.faillist.route.id"),
    PROMO_FAILED_TEMPORARY_ERROR_CODE("promo.failed.temporary.error.code"),
    GLOBAL_MSG_MAX_VALIDITY_IN_SEC("global.msg.max.validity.insec"),
    DLR_URL_TEMPLATE("dlr.url.template"),
    KANNEL_CONN_RESP_TIME_IN_MILLIS("kannel.conn.resp.time.in.millis"),
    GLOBAL_DN_ADJUSTMENT_IN_SEC("global.dn.adjustment.in.sec"),
    KANNEL_CONN_TIMEOUT_IN_SEC("kannel.connection.timeout.in.sec"),
    KANNEL_RESP_TINEOUT_IN_SEC("kannel.resp.timeout.in.sec"),
    KANNEL_CONN_RESP_CHK("kannel.conn.resp.chk"),
    NOPAYLOAD_FOR_PROMO_MSG("nopayload.for.promo.msg"),
    DEFAULT_PAYLOAD_EXPIRY_IN_HR("default.payload.expiry.in.hr"),
    KANNEL_CONNECTION_RESPONSE_STATUS_DELETE_EXPIRED_STATUS("kannel.connection.response.status.delete.expired.record"),
    KANNEL_CONNECTION_RESPONSE_STATUS_EXPIRE_TIME_IN_SEC("kannel.connection.response.status.expire.time.in.sec"),
    UNKNOWN_ERROR_CODE("unknown.error.code"),
    CARRIER_SUCCESS_STATUS_ID("carrier.success.status"),
    VOICE_FAILUER_ERROR_CODE("voice.failure.error.code"),
    VOICE_MAX_RETRY_ATTEMPT("voice.max.retry.attempt"),
    PLATFORM_REJ_DLR_HANDOVER(".platform.reject.dlr.handover"),
    INTL_HEADER_MIN_LEN("intl.header.min.length"),
    INTL_HEADER_MAX_LEN("intl.header.max.length"),
    GLOBAL_TIMEBOUND_CHECK_ENBLED("global.timebound.enabled"),
    GLOBAL_TIMEBOUND_CHECK_INTERVAL("global.timebound.time.interval.sec"),
    GLOBAL_TIMEBOUND_MAXCOUNT("global.timebound.request.count"),
    MAX_REDIS_KEYS_FETCH_LEN("timebound.redis.keys.max.fetch.len"),
    MAX_DEL_REDIS_KEYS_FETCH_LEN("delete.redis.keys.max.fetch.len"),
    VL_URL_STARTS_WITH("vl.url.starts.with"),
    VL_URL_ENDS_WITH("vl.url.ends.with"),
    VL_EXCLUDE_URL_STARTS_WITH("vl.exclude.url.starts.with"),
    VL_EXCLUDE_URL_ENDS_WITH("vl.exclude.url.ends.with"),
    R3C_MAX_ELASTIC_RETRY_COUNT("max.r3c.elastic.retry.count"),
    R3C_DEFAULT_DOMAIN_URL("r3c.default.domain.url"),
    DLT_PARAM_MIN_LENGTH("dlt.praram.min.length"),
    DLT_PARAM_MAX_LENGTH("dlt.param.max.length"),
    SCHEDULE_MIN_TIME("schedule.min.time"),
    SCHEDULE_MAX_TIME("schedule.max.time"),
    SMS_TEMPLATE_MAX_PARAMS("sms.template.max.params"),
    DEFAULT_CARRIER_VALUE("default.carrier.value"),
    DEFAULT_CIRCLE_VALUE("default.circle.value"),
    SIX_DIGIT_SHORTCODE_SLA("six.digit.shortcode.sla"),
    FIVE_DIGIT_SHORTCODE_SLA("five.digit.shortcode.sla"),
    DLT_ENABLE("dlt_enable"),
    ALLOW_SPECIAL_CHAR("allow.special.chars"),
    CUSTOM_PATTERN_CHECK_ENABLE("custom.pattern.check"),
    ELASTIC_SEARCH_HOSTS("elastic.search.hosts"),
    ELASTIC_SEARCH_PORT("elastic.search.port"),
    ELASTIC_SEARCH_SCHEME("elastic.search.scheme"),
    ELASTIC_SEARCH_EXPIRY_TIME("elastic.search.expiry.time"),
    BILLING_TABLE_DATEWISE_ENABLED("billing.table.datewise.enabled"),
    BILLING_TABLE_DATE_FORMAT("billing.table.date.format"),
    BILLING_SCHEMA_DATE_FORMAT("billing.schema.date.format"),
    DLR_GEN_INTERVAL_FROM_PAYLOAD_IN_HRS("gen.dlr.from.payload.interval.in.hrs"),
    DLR_GEN_MYSQL_PAYLOAD_SIZE("dlr.gen.mysql.payload.size"),
    DLR_GEN_PAYLOAD_SIZE("dlr.gen.payload.size"),
    DLR_GEN_SUCCESS_STATUS_ID("payloadgen.success.status.id"),
    DLR_GEN_SUCCESS_STATUS_FLAG("payloadgen.success.status.flag"),
    DLR_GEN_FAIL_STATUS_ID("payloadgen.fail.status.id"),
    DLR_GEN_FAIL_STATUS_FLAG("payloadgen.fail.status.flag"),
    DEFAULT_COUNTRY_CODE("default.country.code"),
    SMPP_MAX_EXPIRY_MINUTES_ALLOW("smpp.max.expiry.minutes.allowed"),
    DN_DELV_STATUS_CODE("dn.delv.status.code"),
    R3C_JNDI_INFO("r3c.jndiinfo.id"),
    R3C_INSERT_TYPE("r3c.insert.type"),
    ACCESS_KEY_PARAMS("accesskey.params"),
    MOBILE_DEFAULT_MIN_LENGTH("dest.min.length"),
    MOBILE_DEFAULT_MAX_LENGTH("dest.max.length"),
    IS_KAFKA_AVAILABLE("is.kafka.available"),
    MAX_NO_PAYLOAD_RETRY_ATTEMPT_COUNT("max.no.payload.retry.attempt.count"),
    MAX_ROUTE_RETRY_ATTEMPT_COUNT("max.route.retry.attempt.count"),
    SMPP_CONCAT_MESSAGE_EXPIRY_IN_SEC("smpp.concat.message.expiry.in.sec"),
    NACK_ERROR_CODE("nack.error.code"),
    BASE_CURRENCY("base.currency"),
    INTL_BASE_CURRENCY("intl.base.currency"),
    SMPP_DN_MAX_EXPIRY_IN_SEC("smpp.dn.max.expiry.in.sec"),
    PLATFORM_DECIMAL_PRCECISION_DATA_STORE("platform.decimal.prcecision.data.store"),
    PLATFORM_DECIMAL_PRCECISION_DATA_PROCESS("platform.decimal.prcecision.data.process"),
    PLATFORM_DECIMAL_PRCECISION_ROUND_MODE("platform.decimal.prcecision.round.mode"),

    ;

    private String key;

    ConfigParamConstants(
            String aKey)
    {
        key = aKey;
    }

    @Override
    public String getKey()
    {
        return key;
    }

}