package com.itextos.beacon.inmemory.loader.process;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public enum InmemoryId
{

    ACCOUNT_INFO("account_info"),
    ELASTIC_SEARCH_COLUMN("elastic_search_column"),
    APPLICATION_CONFIG("application_config"),
    CUSTOM_FEATURES("custom_features"),
    SMPP_PARENT_CHILD_ACC_MAP("smpp_parent_child_acc_map"),
    BLOCK_LIST_NUMBERS("block_list_numbers"),
    COMMON_MSG_VALIDITY("common_msg_validity"),
    GLOBAL_SPAM_BLOCK("global_spam_block"),
    INTL_GLOBAL_SPAM_BLOCK("intl_global_spam_block"),
    EXCEPTIONS_SPAM_BLOCK("exceptions_spam_block"),
    CLIENT_SPAM_BLOCK("client_spam_block"),
    INTL_EXCEPTIONS_SPAM_BLOCK("intl_exceptions_spam_block"),
    MSG_TYPE_SPAM_WORDS_BLOCK("msg_type_spam_words_block"),
    INTL_MSG_TYPE_SPAM_WORDS_BLOCK("intl_msg_type_spam_words_block"),
    INTL_CLIENT_SPAM_BLOCK("intl_client_spam_block"),
    COMMON_HEADERS("common_headers"),
    GOVT_HEADER_BLOCK("govt_header_block"),
    GOVT_HEADER_MASKING("govt_header_masking"),
    GOVT_HEADER_EXCLUDE("govt_header_exclude"),
    MESSAGE_TEMPLATES("message_templates"),
    DLT_MSG_TEMPLATES("dlt_msg_templates"),
    DLT_MSG_PREFIX_SUFFIX("dlt_msg_prefix_suffix"),
    VISUALIZE_LINK("visualize_link"),
    EXCLUDE_VISUALIZE_LINK("exclude_visualize_link"),
    INCLUDE_VISUALIZE_LINK("include_visualize_link"),
    VSMS_USER_HEADERS("vsms_user_headers"),
    MOBILE_WHITELIST("mobile_whitelist"),
    JAPI_CLIENT_ACCESS_DETAILS("japi_client_access_details"),
    JAPI_GENERIC_RESPONSE("japi_generic_response"),
    INTERFACE_MSG_TEMPLATE("interface_msg_template"),
    COUNTRY_INFO("country_info"),
    MCC_MNC("mcc_mnc"),
    ACCOUNT_MSG_PREFIX_SUFFIX("account_msg_prefix_suffix"),
    CLIENT_INTL_RATES("client_intl_rates"),
    MCC_MNC_RATES("mcc_mnc_rates"),
    MCC_MNC_ROUTES("mcc_mnc_routes"),
    INTL_COUNTRY_RATES("intl_country_rates"),
    SPECIFIC_BLOCKOUT("specific_blockout"),
    RANDOM_HEADER("random_header"),
    CUSTOM_PROMO_HEADER("custom_promo_header"),
    ALTERNATE_HEADER_ROUTE("alternate_header_route"),
    FIXED_HEADER_ROUTE("fixed_header_route"),
    PRIORITY_HEADER_ROUTE("priority_header_route"),
    ROUTE_CONFIGURATION("route_configuration"),
    ROUTE_GROUPS("route_groups"),
    CUSTOM_ROUTES("custom_routes"),
    DEFAULT_ROUTES("default_routes"),
    CLIENT_ALLOW_HEADERS("client_allow_headers"),
    CUSTOM_ROUTE_TEMPLATE("custom_route_template"),
    MASKED_HEADER_POOL("masked_header_pool"),
    MOBILE_ROUTES("mobile_routes"),
    HEADER_ROUTE_STATUS("header_route_status"),
    TABLE_INSERTER_INFO("table_inserter_info"),
    INTL_GLOBAL_HEADER_KEYWORD("intl_global_header_keyword"),
    INTL_MOBILE_ROUTES("intl_mobile_routes"),
    INTL_ROUTE_CONFIGURATION("intl_route_configuration"),
    INTL_PRIORITY_ROUTE("intl_priority_route"),
    INTL_CLIENT_KEYWORD_HEADER("intl_client_keyword_header"),
    INTL_CLIENT_COUNTRY_SERIES_HEADER("intl_client_country_series_header"),
    INTL_ROUTE_HEADERS("intl_route_headers"),
    INTL_CARRIER_SUPPORT_HEADERS("carrier_support_headers"),
    AGING_DN_INFO("aging_dn_info"),
    EXCLUDE_CIRCLE("exclude_circle"),
    CUSTOM_SLABS("custom_slabs"),
    CARRIER_CIRCLE("carrier_circle"),
    MSG_REPLACE_RULES("msg_replace_rules"),
    MSG_REPLACE_KEYWORDS("msg_replace_keywords"),
    KANNEL_INFO("kannel_info"),
    KANNEL_STORE_INFO("kannel_store_info"),
    ALTERNATE_ROUTE("alternate_route"),
    DN_RECEIVER_CONN_INFO("dn_receiver_conn_info"),
    CLUSTER_DN_RECEIVER_INFO("cluster_dn_receiver_info"),
    KANNEL_CONFIG_INFO("kannel_config_info"),
    CLIENT_DLR_PREF("client_dlr_pref"),
    INTL_USER_HEADERS("intl_user_headers"),
    DOMESTIC_USER_HEADERS("domestic_user_headers"),

    CARRIER_ERROR_INFO("carrier_error_info"),
    PLATFORM_ERROR_INFO("platform_error_info"),
    CLIENT_ERROR_INFO("client_error_info"),

    DN_PROCESS_TYPE_CONFIG("dn_process_type_config"),
    DLR_PERCENTAGE_INFO("dlr_percentage_info"),
    DLR_CLIENT_PERCENTAGE_INFO("dlr_client_percentage_info"),
    DLR_CLIENT_EXCLUDE("dlr_client_exclude"),
    ALERT_WAIT_CONFIG("alert_wait_config"),
    RETRY_MSG_VALIDITY("retry_msg_validity"),
    CUSTOM_RETRY_ROUTES("custom_retry_routes"),
    GLOBAL_RETRY_INFO("global_retry_info"),
    GLOBAL_RETRY_ROUTES("global_retry_routes"),
    GLOBAL_RETRY_INTERVAL("global_retry_interval"),
    VOICE_ACC_INFO("voice_acc_info"),
    VOICE_TEMPLATE_INFO("voice_template_info"),
    DN_SLAB_ADJUST_CUSTOMER("dn_slab_adjust_customer"),
    IGNORE_DLR_GEN("ignore_dlr_gen"),
    HTTPCLIENTHANDOVER_CUSTOMER_INFO("httpclienthandover_customer_info"),
    BILL_LOG_MAPPING("bill_log_mapping"),
    CLIENT_IGNORE_DN_GEN("client_ignore_dn_gen"),
    ENCRYPT_INFO("encrypt_info"),
    CLIENT_BLOCK_LIST_NUMBERS("client_block_list_numbers"),

    CLOUD_INTERFACE_CONFIGURATION("cloud_interface_configuration"),
    INTL_HEADER_INFO("intl_header_info"),
    CURRENCY_CONVERSION_DATE_BASED("currency_conversion_date_based"),
    CURRENCY_CONVERSION_MONTH_BASED("currency_conversion_month_based"),
    SMPP_ACCOUNT_INFO("smpp_account_info"),
    CURRENCY_MASTER("currency_master"),
    DLR_QUERY("dlr_query"),
    CLIENT_MSG_VALIDITY("client_msg_validity"),

    OTP_CONFIG("otp_config"),;

    private static final Log log = LogFactory.getLog(InmemoryId.class);

    private final String     key;

    InmemoryId(
            String aKey)
    {
        key = aKey;
    }

    public String getKey()
    {
        return key;
    }

    private static final Map<String, InmemoryId> allValues = new HashMap<>();

    static
    {
        final InmemoryId[] lValues = InmemoryId.values();

        for (final InmemoryId id : lValues)
            allValues.put(id.getKey(), id);
    }

    public static InmemoryId getInmemoryId(
            String aKey)
    {
        final InmemoryId lInmemoryId = allValues.get(aKey);

        if (lInmemoryId == null)
            log.error("WARNING: >>> Unable to identify the inmemoryid for '" + aKey + "'. Please check the DB configuration for inmemoryid..");
        return lInmemoryId;
    }

}