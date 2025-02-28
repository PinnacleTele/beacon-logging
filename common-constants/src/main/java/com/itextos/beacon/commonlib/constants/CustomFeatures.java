package com.itextos.beacon.commonlib.constants;

public enum CustomFeatures
        implements
        ItextosEnum
{

    REJECT_INVALID_DOMESTIC_SERIES("reject.invalid.domestic.series"),
    TEMPLATE_HEADER_FAIL("template.header.fail"),
    BYPASS_MOBILE_ROUTE("bypass.mobile.route"),
    ACC_ROUTING_TEMPLATE_CHK("acc.routing.template.chk"),
    DOMESTIC_CLIENT_FAILLIST_CHK("domestic.acc.faillist.chk"),
    DOMESTIC_GLOBAL_FAILLIST_CHK("domestic.global.faillist.chk"),
    INTL_GLOBAL_FAILLIST_CHK("intl_global_faillist_chk"),
    INTL_CLIENT_FAILLIST_CHK("intl_acc_faillist_chk"),
    IS_AGING_ENABLE("is.aging.enabled"),
    IS_FASTDN_ENABLE("is.fastdn.enabled"),
    FASTDN_GEN_IN_SEC("fastdn.gen.in.sec"),
    DN_SLAB_ENABLE("dn.slab.enabled"),
    PAYLOAD_LOGMSG_YN("payload.longmsg.yn"),
    PARENT_CHILD_ACC_SUB_ENABLE("parent.child.acc.sub.enabled"),
    LEGACY_MESSAGE_EXPIRY_TIME_ZONE("legacy.msg.expiry.time.zone"),
    STANDARD_TON_NPI_ENABLE("standard.ton.npi.enabled"),
    IGNORE_JAPI_VERSION("ignore.japi.version"),
    CUSTOM_BLACKLIST_ENABLE("custom.blacklist.enabled"),
    EXPLICIT_URL_SHORTNER_YN("explicit.url.shortner.yn"),
    USE_DEFAULT_INTL_PRICE("use.default.intl.price"),
    UDH_VALIDATE_IN_SMPP("udh.validate.in.smpp"),
    USER_DEBUG_LOG("user.debug.log");

    private String key;

    CustomFeatures(
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