package com.itextos.beacon.commonlib.constants;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public enum Component
        implements
        ItextosEnum
{

    // MT Components
	MW("middleware"),
	BILLER("biller"),
	AX("ax"),
    AGIN("aging_insert"),
    CH("carrier_handover"),
    DCH("dummy_carrier_handover"),
    FBP("failback_poller"),
    SUBBC("submission_billing_consumer"),
    IC("interface_consumer"),
    ICRCS("interface_rcs_consumer"),
    
    PRC("platform_rejection_consumer"),
    R3C("visulaized_link_consumer"),
    RC("router_consumer"),
    RCH("retry_carrier_handover"),
    SBC("schedule_blockout_consumer"),
    SBCV("schedule_blockout_consumer_verify"),
    SBP("schedule_blockout_poller"),
    VC("verify_consumer"),
    VSMS("verified_sms"),
    WC("wallet_consumer"),
    DLTVC("dlt_verify_consumer"),
    BWC("blockout_wallet_consumer"),

    // DN COmponents
    ADNP("aging_dn_processor"),
    CDNH("client_dlr_handover"),
    DLRINTLP("dlr_internal_process"),
    DLRQDN("dlr_query_dn"),
    DLRQMT("dlr_query_mt"),
    DLRR("dlr_retry"),
    DNDP("dlr_del_processor"),
    DNP("dlr_processor"),
    DNR("dlr_receiver"),
    FPDN("final_process_dn"),
    HTTP_DLR("http_dlr_handover"),
    SDNP("single_dlr_processor"),
    SMPP_DLR("smpp_dlr_handover"),
    UADN("update_aging"),
    DLRFBP("dlr_failback_poller"),

    AVDN("aging_voice_dn"),
    DLRWR("dn_wait_retry"),
    VOICE_PROCESS("voice_process"),

    /** Use T2DB_ERROR_LOG instead of ERROR_LOG */
    WALLET_REFUND("wallet_refund"),

    // REDIS Related.
    ACCOUNT_SYNC("account_sync"),
    ADNG("againg_dlr_generate"), // To Store Aging records in redis
    AGING_DN("aging_dlr"),
    AGING_PROCESS("aging_process"),
    CLIENT_HANDOVER("client_handover"),
    DLR_GEN("dlr_gen"),
    DLR_POST_LOG("dlr_post_log"),
    DN_PAYLOAD("dn_payload"),
    DUPLICATE_CHK("duplicate_chk"),
    DUPLICATE_DN("duplicate_dn"),
    FAILLIST("faillist"),
    HANDOVER_DN("handover_dn"),
    INTERFACES("interfaces"),
    INTERFACE_ASYNC_PROCESS("interface_async_process"),
    INTRIM_DN("intrim_dlr"),
    KAFKA_SERVICE("kafka_service"),
    KANNEL_REDIS("kannel_redis"),
    MESSAGE_IDENTIFIER("messageid_generator"),
    OPT_IN_CHK("optin_chk"),
    PRI_DND_CHK("pri_dnd_chk"),
    PRI_OPT_OUT_CHK("pri_optout_chk"),
    SEC_DND_CHK("sec_dnd_chk"),
    SEC_OPT_OUT_CHK("sec_optout_chk"),
    SMPP_CONSUMER("smpp_consumer"),
    TIMEBOUND_CHK("timebound_chk"),
    SHORTCODE_PROVIDER("shortcode_provider"),
    DLR_WAIT_RETRY("dlr_wait_retry"),
    WALLET_CHK("wallet_chk"),
    SMPP_CLIENT_DN("smpp_client_dn"),
    PROMO_KANNEL_REDIS_CLEANER("promo_kannel_redis_cleaner"),
    CAPPING_CHK("capping_chk"),

    K2E_SUBMISSION("k2e_submission"),
    K2E_DELIVERIES("k2e_deliveries"),

    // Topic 2 DB Components
    T2DB_SUBMISSION("t2db_submission"),
    T2DB_DELIVERIES("t2db_deliveries"),
    T2DB_DELIVERIES_BKUP("t2db_deliveries_bkup"),
    T2DB_FULL_MESSAGE("t2db_full_message"),
    T2DB_INTERIM_FAILUERS("t2db_interim_failuers"),
    T2DB_INTERIM_DELIVERIES("t2db_interim_deliveries"),
    T2DB_ERROR_LOG("t2db_error_log"),
    T2DB_NO_PAYLOAD_DN("t2db_no_payload_dn"),
    T2DB_DN_ERROR_BASED_RETRY("t2db_dn_error_based_retry"),
    T2DB_CLIENT_HANDOVER_LOG("t2db_client_handover_log"),
    T2DB_CLIENT_HANDOVER_MASTER_LOG("t2db_client_handover_master_log"),
    T2DB_CLIENT_HANDOVER_RETRY_DATA("t2db_client_handover_retry_data"),
    T2DB_SMPP_POST_LOG("t2db_smpp_post_log"),

    // SMPP Components
    SMPP_CONCAT("smpp_concat"),
    SMPP_SESSION("smpp_session"),
    SMPP_THROTTLE("smpp_throttle"),

    CLOUD_ACCEPTOR("cloud_acceptor"),
    FTP("ftp"),

    ;

    private static final Log log = LogFactory.getLog(Component.class);

    private final String     key;

    Component(
            String aKey)
    {
        key = aKey;
    }

    private static final Map<String, Component> mAllTypes = new HashMap<>();

    static
    {
        final Component[] lValues = Component.values();

        for (final Component ip : lValues)
            mAllTypes.put(ip.key, ip);
    }

    @Override
    public String getKey()
    {
        return key;
    }

    public static Component getComponent(
            String aKey)
    {
        final Component lComponent = mAllTypes.get(aKey);
        if (lComponent == null)
            log.error("WARNING: >>> Unable to identify the component for '" + aKey + "'. Please check the DB configuration for component.");
        return lComponent;
    }

}