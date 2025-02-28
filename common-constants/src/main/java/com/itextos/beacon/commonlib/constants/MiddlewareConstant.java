package com.itextos.beacon.commonlib.constants;

import java.util.HashMap;
import java.util.Map;

public enum MiddlewareConstant
{

    MW_MESSAGE_CREATED_TIMESTAMP("m_cts", "msg_create_ts", "Message created time"),
    MW_INTERFACE_TYPE("if_ty", "intf_type", "Interface Type"),
    MW_INTERFACE_GROUP_TYPE("if_gp_ty", "intf_grp_type", "Interface Group Type"),
    MW_INTL_MESSAGE("itl_m", "intl_msg", "To indicate the international message"),

    MW_CLIENT_ID("c_id", "cli_id", "account_view.cli_id"),
    MW_USER("usr", "user", "account_view.user"),
    MW_UI_PASS("ui_pass", "ui_pass", "account_view.ui_pass"),
    MW_API_PASS("api_pass", "api_pass", "account_view.api_pass"),
    MW_SMPP_PASS("smpp_pass", "smpp_pass", "account_view.smpp_pass"),
    MW_USER_TYPE("usr_ty", "user_type", "account_view.user_type"),
    MW_PU_ID("pu_id", "pu_id", "account_view.pu_id"),
    MW_SU_ID("su_id", "su_id", "account_view.su_id"),
    MW_ACC_STATUS("acc_sts", "acc_status", "account_view.acc_status"),
    MW_MSG_TYPE("m_ty", "msg_type", "account_view.msg_type"),
    MW_ACC_DEFAULT_ROUTE_ID("acc_rute", "acc_route_id", "account_view.acc_route_id"),
    MW_PLATFORM_CLUSTER("pf_clsr", "platform_cluster", "account_view.platform_cluster"),
    MW_SMS_PRIORITY("sms_pri", "sms_priority", "account_view.sms_priority"),
    MW_NEWLINE_REPLACE_CHAR("nl_repl_char", "newline_replace_char", "account_view.newline_replace_char"),
    MW_IS_16BIT_UDH("16_udh", "is_16bit_udh", "account_view.is_16bit_udh"),
    MW_ACKNOWLEDGE_ID_LENGTH("ack_id_len", "acknowledge_id_length", "account_view.acknowledge_id_length"),
    MW_CLIENT_ENCRYPT_ENABLED("cli_encp", "client_encrypt", "account_view.client_encrypt"),
    MW_BILLING_ENCRYPT_TYPE("bill_encp_type", "bill_encrypt_type", "account_view.bill_encrypt_type"),
    MW_DOMESTIC_PROMO_TRAI_BLOCKOUT_PURGE("dom_pro_trai_bout_purge", "domestic_promo_trai_blockout_purge", "account_view.domestic_promo_trai_blockout_purge"),
    MW_CLIENT_DOMESTIC_SMS_BLOCKOUT_ENABLED("dom_sms_bout", "domestic_sms_blockout", "account_view.domestic_sms_blockout"),
    MW_CLIENT_DOMESTIC_SMS_BLOCKOUT_START("dom_sms_bout_str", "domestic_sms_blockout_start", "account_view.domestic_sms_blockout_start"),
    MW_CLIENT_DOMESTIC_SMS_BLOCKOUT_STOP("dom_sms_bout_stp", "domestic_sms_blockout_stop", "account_view.domestic_sms_blockout_stop"),
    MW_DLT_CHECK_ENABLED("dlt_enb", "dlt_enabled", "account_view.dlt_enabled"),
    MW_DLT_TEMPL_GRP_ID("dlt_tmpl_gp_id", "dlt_templ_grp_id", "account_view.dlt_templ_grp_id"),
    MW_DUPLICATE_CHK_REQ("dup_chk_req", "dup_chk_req", "account_view.dup_chk_req"),
    MW_DUPLICATE_CHK_INTERVAL("dup_chk_int", "dup_chk_interval", "account_view.dup_chk_interval"),
    MW_INTL_SMS_BLOCKOUT_ENABLED("itl_sms_bout", "intl_sms_blockout", "account_view.intl_sms_blockout"),
    MW_INTL_SMS_BLOCKOUT_START("itl_sms_bout_str", "intl_sms_blockout_start", "account_view.intl_sms_blockout_start"),
    MW_INTL_SMS_BLOCKOUT_STOP("itl_sms_bout_stp", "intl_sms_blockout_stop", "account_view.intl_sms_blockout_stop"),
    MW_RETRY_ALTER_ROUTE_LOOKUP("rety_alter_rute_lkup", "retry_alter_route_lookup", "account_view.retry_alter_route_lookup"),
    MW_TIME_ZONE("tz", "time_zone", "account_view.time_zone"),
    MW_TIME_OFFSET("tz_off", "time_offset", "account_view.time_offset"),
    MW_IP_VALIDATION("ip_valid", "ip_validation", "account_view.ip_validation"),
    MW_IP_LIST("ip_lst", "ip_list", "account_view.ip_list"),
    MW_MT_ADJUST_ENABLED("mt_adjust", "mt_adjust", "account_view.mt_adjust"),
    MW_DN_ADJUST_ENABLED("dn_adjust", "dn_adjust", "account_view.dn_adjust"),
    MW_DND_REJECT_YN("dnd_rej_yn", "dnd_reject_yn", "account_view.dnd_reject_yn"),
    MW_VL_SHORTNER("vl_short", "vl_shortner", "account_view.vl_shortner"),
    MW_BILL_TYPE("bill_ty", "bill_type", "account_view.bill_type"),
    MW_DND_PREF("dnd_pref", "dnd_pref", "account_view.dnd_pref"),
    MW_DND_CHK("dnd_chk", "dnd_chk", "account_view.dnd_chk"),
    MW_SPAM_CHK("spam_chk", "spam_chk", "account_view.spam_chk"),
    MW_BLACKLIST_CHK("blklist_chk", "blklist_chk", "account_view.blklist_chk"),
    MW_SMS_RETRY_ENABLED("sms_rety_avail", "sms_retry_available", "account_view.sms_retry_available"),
    MW_IS_SCHEDULE_ALLOW("is_sch_allow", "is_schedule_allow", "account_view.is_schedule_allow"),
    MW_UC_IDEN_ALLOW("uc_iden_allow", "uc_iden_allow", "account_view.uc_iden_allow"),
    MW_UC_IDEN_CHAR_LEN("uc_iden_char_len", "uc_iden_char_len", "account_view.uc_iden_char_len"),
    MW_UC_IDEN_OCCUR("uc_iden_occur", "uc_iden_occur", "account_view.uc_iden_occur"),
    MW_IS_REMOVE_UC_CHARS("is_rem_uc_chars", "is_remove_uc_chars", "account_view.is_remove_uc_chars"),
    MW_USE_DEFAULT_HEADER_ENABLED("u_df_hdr", "use_default_header", "account_view.use_default_header"),
    MW_ACC_DEFAULT_HEADER("acc_hdr", "acc_default_header", "account_view.acc_default_header"),
    MW_USE_DEFAULT_HEADER_FAIL_ENABLED("u_df_hdr_fail", "use_default_on_header_fail", "account_view.use_default_on_header_fail"),
    MW_CONSIDER_DEFAULTLENGTH_AS_DOMESTIC("c_len_dom", "considerdefaultlength_as_domestic", "account_view.considerdefaultlength_as_domestic"),
    MW_DOMESTIC_TRA_BLOCKOUT_REJECT("dom_tra_bout_rej", "domestic_tra_blockout_reject", "account_view.domestic_tra_blockout_reject"),
    MW_TIMEBOUND_CHK_ENABLED("tb_chk", "timebound_chk_enable", "account_view.timebound_chk_enable"),
    MW_TIMEBOUND_INTERVAL("tb_interval", "timebound_interval", "account_view.interval"),
    MW_TIMEBOUND_MAX_REQ_COUNT("tb.max.req", "timebound_max_count_allow", "account_view.timebound_max_count_allow"),
    MW_SMS_RATE("s_rate", "sms_rate", "account_view.sms_rate"),
    MW_DLT_RATE("d_rate", "dlt_rate", "account_view.dlt_rate"),
    MW_DOMESTIC_SPECIAL_SERIES_ALLOW("dom_sps_allow", "domestic_special_series_allow", "account_view.domestic_special_series_allow"),
    MW_REQ_HEX_MSG("r_hex_m", "req_hex_msg", "accout_view.req_hex_msg"),
    MW_BILLING_CURRENCY("bill_cury", "billing_currency", "account_view.billing_currency"),
    MW_BILLING_CURRENCY_CONVERSION_TYPE("b_curr_conv_type", "billing_currency_conv_type", "account_view.billing_currency_conv_type"),
    MW_IS_IDLO("is_ildo", "is_ildo", "account_view.is_ildo"),
    MW_BASE_SMS_RATE("b_s_rate", "base_sms_rate", "account_view.base_sms_rate"),
    MW_BASE_ADD_FIXED_RATE("b_af_rate", "base_add_fixed_rate", "account_view.base_add_fixed_rate"),
    MW_FORCE_DND_CHK("f_dnd_chk", "force_dnd_check", "account_view.force_dnd_check"),
    MW_INVOICE_BASED_ON("inv_b_on", "invoice_based_on", "account_view.invoice_based_on"),
    MW_MSG_RETRY_ENABLED("m_retry_avail", "msg_retry_available", "account_view.msg_retry_available"),
    MW_CAPPING_CHK_ENABLED("cp_chk", "capping_chk_enable", "account_view.capping_chk_enable"),
    MW_CAPPING_INTERVAL_TYPE("cp_interval_type", "capping_interval_type", "account_view.capping_interval_type"),
    MW_CAPPING_INTERVAL("cp_interval", "capping_interval", "account_view.capping_interval"),
    MW_CAPPING_MAX_REQ_COUNT("cp_max_req", "capping_max_count_allow", "account_view.capping_max_count_allow"),
    MW_CREDIT_CHECK("c_chk", "credit_check", "account_view.credit_check"),

    MW_APP_TYPE("app_type", "app_type", "Application Type like SMS, WhatsApp, Email, Voice"),
    MW_MSG("m", "msg", "interface.msg Message request from client"),
    MW_LONG_MSG("long_m", "long_msg", "All parts message"),
    MW_UDHI("udhi", "udhi", "interface.udhi UDH include notifier in message"),
    MW_UDH("udh", "udh", "interface.udh UDH value from client"),
    MW_DCS("dcs", "dcs", "interface.dcs Data coding from client"),
    MW_HEADER("hdr", "hdr", "interface.header Header from client"),
    MW_MOBILE_NUMBER("dest", "dest", "Mobile number"),
    MW_MESSAGE_ID("m_id", "msg_id", "interface.mid Unique Message Id from Interfaces"),
    MW_FILE_ID("f_id", "file_id", "interface.file_id Acknowledgement Id for Client Request"),
    MW_BASE_MESSAGE_ID("b_m_id", "base_msg_id", "interface.longmsg_base_mid Long message id"),
    MW_CLIENT_MESSAGE_ID("cli_m_id", "cli_msg_id", "interface.client_mid Unique Client generated number"),
    MW_MSG_RECEIVED_TIME("recv_ts", "recv_time", "interface.stime Request Received time"),
    MW_MSG_RECEIVED_DATE("recv_dt", "recv_date", "interface.sdate Request Received date"),
    MW_MSG_ACTUAL_RECEIVED_TIME("a_recv_ts", "act_recv_time", "interface.stime Actual Request Received time"),
    MW_MSG_ACTUAL_RECEIVED_DATE("a_recv_dt", "act_recv_date", "interface.sdate Actual Request Received date"),
    MW_CLIENT_MAX_SPLIT("cli_max_split", "cli_max_split", "interface.max_split Maximum split part for given request from client"),
    MW_MSG_TAG("m_tag", "msg_tag", "interface.msg_tag Message Tag from client"),
    MW_IS_HEX_MSG("is_hex_m", "is_hex_msg", "interface.hexmsg indicator to consider the message as Binary messages"),
    MW_FAIL_REASON("fail_reason", "fail_reason", "interface.reason Interface Rejection Reason"),
    MW_DESTINATION_PORT("dest_port", "dest_port", "interface.destination_port Special Port from client"),
    MW_MAX_VALIDITY_IN_SEC("max_valid_in_sec", "max_validity_in_sec", "interface.max_validity_in_sec Message Validity Period in Seconds"),
    MW_CLIENT_SOURCE_IP("cli_ip", "cli_ip", "interface.cust_ip Client Source IP"),
    MW_MSG_CLASS("m_class", "msg_class", "interface.msg_class Request Type"),
    MW_COUNTRY("cntry", "country", "interface.country Country"),
    MW_PARAM_1("p1", "param1", "interface.param1 Addition Parmas"),
    MW_PARAM_2("p2", "param2", "interface.param2 Addition Parmas"),
    MW_PARAM_3("p3", "param3", "interface.param3 Addition Parmas"),
    MW_PARAM_4("p4", "param4", "interface.param4 Addition Parmas"),
    MW_PARAM_5("p5", "param5", "interface.param5 Addition Parmas"),
    MW_PARAM_6("p6", "param6", "interface.param6 Addition Parmas"),
    MW_PARAM_7("p7", "param7", "interface.param7 Addition Parmas"),
    MW_PARAM_8("p8", "param8", "interface.param8 Addition Parmas"),
    MW_PARAM_9("p9", "param9", "interface.param9 Addition Parmas"),
    MW_PARAM_10("p10", "param10", "interface.param10 Addition Parmas"),
    MW_SCHE_DATE_TIME("sch_ts", "sch_date_time", "interface.sche_date_time Schedule Message Time"),
    MW_DLT_TEMPLATE_ID("dlt_tmpl_id", "dlt_tmpl_id", "dlt template id from client"),
    MW_DLT_ENTITY_ID("dlt_enty_id", "dlt_entity_id", "dlt entity id from client"),
    MW_DLT_TMA_ID("dlt_tma_id", "dlt_tma_id", "dlt Telemarketer id from client"),
    MW_APP_INSTANCE_ID("app_ins_id", "app_ins_id", "Application Instance Id"),
    MW_INTERFACE_REJECTED("if_reject", "intf_reject", "Interface Reject flag"),
    MW_CARRIER_DATE_TIME_FORMAT("car_ts_format", "carrier_datetime_format", "Date Time format used to parse the carrier date and time"),
    MW_IS_GOVT_HEADER("is_govt_hdr", "is_govt_hdr", "Govt Header present"),
    MW_FROM_SCHD_BLOCKOUT("frm_sch_bout", "from_schd_blkout", "Schedule Blockout from IC or Kannel Handover"),
    MW_BLOCKOUT_TYPE("bout_type", "blkout_type", "Blockout Type"),
    MW_PROCESS_BLOCKOUT_TIME("process_bout_ts", "process_blkout_time", "Process Blockout Time"),
    MW_IS_SPECIFIC_DROP("is_specific_drop", "is_specific_drop", "Specific Blockout Drop"),
    MW_TRA_TRANSPROMO_CHK("tra_transpromo_chk", "tra_transpromo_chk", "TRA TransPromo Check"),
    MW_TREAT_DOMESTIC_AS_SPECIAL_SERIES("treat_dom_as_spl_srs", "treat_dom_as_special_series", "Treat Domestic Special Series Number"),
    MW_IS_DND_SCRUBBED("is_dnd_scrub", "is_dnd_scrubbed", "The Number is DND"),
    MW_DND_ENABLE("dnd_en", "dnd_enable", "DND flag is enabled"),
    MW_MSG_PART_NUMBER("m_prt_no", "msg_part_no", "Message part number"),
    MW_MSG_TOTAL_PARTS("tot_m_prts", "total_msg_parts", "Total Number of Message Parts"),
    MW_MSG_SPLIT_LENGTH("split_m_len", "split_msg_length", "Split Message part Length"),
    MW_FEATURE_CODE("ft_cd", "feature_code", "Feature Code"),
    MW_CLIENT_TEMPLATE_ID("cli_tmpl_id", "cli_tmpl_id", "Client Template ID"),
    MW_RETRY_ATTEMPT("rty_atmpt", "retry_attempt", "Retry Attempt Count"),
    MW_CONCAT_REF_NUM("concat_ref_num", "concat_ref_num", "Concat SMS Refrence Number"),
    MW_VL_TRACK_INFO("vl_trck_info", "vl_track_info", "VL Track Information"),
    MW_SHORTNER_ID("shrt_id", "shortner_id", "URL Shortner Id"),
    MW_IS_MSG_SHORTNED("m_shrted", "msg_shortned", "The request message is shortned"),
    MW_ROUTE_ID("rute_id", "route_id", "Last attempted Route Id"),
    MW_ACTUAL_ROUTE_ID("a_rute_id", "act_route_id", "First identified Route Id"),
    MW_PLATFROM_REJECTED("pf_reject", "plat_reject", "Platform Reject Flag"),
    MW_CIRCLE("cir", "circle", "Carrier Circle"),
    MW_CARRIER("car", "carrier", "Carrier Name"),
    MW_MCC("mcc", "mcc", "mcc"),
    MW_MNC("mnc", "mnc", "mnc"),
    MW_SEGMENT("segment", "segment", "segment"),
    MW_DLR_FROM_INTERNAL("dlr_from_intl", "dlr_from_internal", "DLR from Internal"),
    MW_CARRIER_SUBMIT_TIME("car_sub_ts", "carrier_sub_time", "Carrier Submission Time"),
    MW_ACTUAL_CARRIER_SUBMIT_TIME("a_car_sub_ts", "act_carrier_sub_time", "Actual Carrier Submission Time"),
    MW_AALPHA("aplha_value", "aplha_value", "Aplha Value"),
    MW_CONCAT_TEMPLATE_STATUS("concate_tmpl_sts", "concate_tmpl_sts", "Concate Template Status"),
    MW_CLIENT_HEADER("cli_hdr", "cli_hdr", "Client Passed Header"),
    MW_INTF_COUNTRY_CODE("if_cntry_cd", "intf_country_code", "Default Country Code passed in Interface"),
    MW_IS_HEADER_MASKED("is_hdr_msk", "is_hdr_masked", "Is Header Masked"),
    MW_IS_CLIENT_TEMPLATE_MATCH("is_cli_tmpl_mtch", "is_cli_tmpl_matched", "Is Client Template Matched"),
    MW_ROUTE_LOGIC_ID("rute_logic_id", "route_logic_id", "Route Logic Id identified"),
    MW_MASKED_HEADER("mask_hdr", "masked_hdr", "Masked Header"),
    MW_ROUTE_TYPE("rute_type", "route_type", "Route Type"),
    MW_SMSC_ID("smsc_id", "smsc_id", "SMSC Id"),
    MW_SPECIFIC_BLOCKOUT_CHK_ENABLED("specific_bout_chk", "specific_blkout_chk", "Is Specific Blockout Check Enabled"),
    MW_URL_TRACK_ENABLED("url_trck_ebl", "url_track_enabled", "URL Track enabled"),
    MW_INTL_STANDARD_ROUTE_ID("intl_std_rute_id", "intl_std_route_id", "International Standard Route ID"),
    MW_INTL_ECONOMIC_ROUTE_ID("intl_eco_rute_id", "intl_eco_route_id", "International Economy Route ID"),
    MW_INTL_DEFAULT_HEADER("intl_def_hdr", "intl_def_hdr", "International Default Header"),
    MW_INTL_CARRIER_NW("car_nw", "carrier_network", "International Network"),
    MW_INTL_DEFAULT_HEADER_TYPE("intl_def_hdr_type", "intl_def_hdr_type", "International default header type"),
    MW_INTL_CLIENT_HEADER("intl_cli_hdr", "intl_cli_hdr", "International client_header"),
    MW_INTL_CLIENT_FAILLIST_CHK("intl_cli_flst_chk", "intl_cli_faillist_chk", "International client faillist check"),
    MW_INTL_GLOBAL_FAILLIST_CHK("intl_glo_flist_chk", "intl_global_faillist_chk", "International Global faillist check"),
    MW_PAYLOAD_EXPIRY("pl_exp", "payload_expiry", "Payload Expiry"),
    MW_PAYLOAD_REDIS_ID("pl_rds_id", "payload_redis_id", "Payload Redis Id"),
    MW_CALLBACK_PARAMS("cb_params", "callback_params", "Callback Params"),
    MW_CALLBACK_URL("cb_url", "callback_url", "Callback URL"),
    MW_ALTER_MSG("alter_m", "alter_msg", "Alternate Message"),
    MW_CARRIER_FULL_DN("car_full_dn", "carrier_full_dn", "Carrier Full DN"),
    MW_AGING_TYPE("age_type", "aging_type", "Aging Type F: FastDn, A-AgingDN, B-Both, Null"),
    MW_AGING_SCHE_TIME("age_sch_ts", "aging_sch_time", "Aging Scheduled Time"),
    MW_DELIVERY_TIME("dly_ts", "dly_time", "Message Delivered Time"),
    MW_ACTUAL_DELIVERY_TIME("a_dly_ts", "act_dly_time", "Actual Message Delivered Time"),
    MW_CARRIER_RECEIVED_TIME("car_rcvd_ts", "carrier_rcvd_time", "Carrier Received Time"),
    MW_MSG_ALTER_CHK("m_replace_chk", "msg_replace_chk", "Message Replace Account level Check"),
    MW_CARRIER_ACKNOWLEDGE_ID("car_ack_id", "carrier_ack_id", "Carrier Acknowledge Id"),
    MW_CARRIER_SYSTEM_ID("car_sys_id", "carrier_sys_id", "Carrier System Id"),
    MW_IS_SCHEDULE_BLOCKOUT_MSG("is_sch_m", "is_sch_msg", "Is message coming from schedule / blockout"),
    MW_DN_PAYLOAD_STATUS("dn_pl_sts", "dn_payload_sts", "DN Payload Redis Status"),
    MW_DN_FAILURE_TYPE("dn_fail_type", "dn_fail_type", "DN Failuer Type"),
    MW_TERM_CARRIER("term_car", "term_carrier", "Terminated Carrier"),
    MW_TERM_CIRCLE("term_cir", "term_circle", "Terminated Circle"),
    MW_DELV_LATENCY_ORG_IN_MILLIS("delv_lat_ori_in_millis", "delv_lat_ori_in_millis", "Delivery Latency Original In Milliseconds"),
    MW_DELV_LATENCY_SLA_IN_MILLIS("delv_lat_sla_in_millis", "delv_lat_sla_in_millis", "Delivery Latency SLA In Milliseconds"),
    MW_OVERALL_LATENCY_IN_MILLIS("overall_lat_in_millis", "overall_lat_in_millis", "Overall Latency In Milliseconds"),
    MW_DLR_REQ_FROM_CLI("dn_req_cli", "dn_req_cli", "Dlr request from Client"),
    MW_SMPP_SERVICE_TYPE("svc_type", "service_type", "SMPP Service Type"),
    MW_SMPP_ESM_CLASS("esm_cls", "esm_class", "SMPP Esm class"),
    MW_SMPP_SM_LENGTH("sm_len", "sm_length", "SMPP Message Length"),
    MW_SMPP_PRIORITY_FLAG("pri_flag", "priority_flag", "SMPP Priority Flag"),
    MW_SMPP_REGISTERED_DELIVERY("reg_dly", "registered_delivery", "SMPP Registered Delivery"),
    MW_SMPP_SOURCE_ADDR_TON("src_addr_ton", "src_addr_ton", "SMPP Source Address TON value"),
    MW_SMPP_SOURCE_ADDR_NPI("src_addr_npi", "src_addr_npi", "SMPP Source Address NPI value"),
    MW_SMPP_DEST_ADDR_TON("dest_addr_ton", "dest_addr_ton", "SMPP Destination Address TON value"),
    MW_SMPP_DEST_ADDR_NPI("dest_addr_npi", "dest_addr_npi", "SMPP Destination Address NPI value"),
    MW_SMPP_LAST_SENT("last_sent", "last_sent", "SMPP last_sent"),
    MW_SMPP_MESSAGE_PAYLOAD("m_pl", "msg_payload", "SMPP Message Payload"),
    MW_VOICE_CONFIG_ID("vc_con_id", "voice_config_id", "Voice configuration unique id"),
    MW_ENCRYPTED_MESSAGE("encry_m", "encrypted_msg", "Encrypted message for storing in db"),
    MW_ENCRYPTED_MOBILENUMBER("encry_dest", "encrypted_dest", "Encrypted mobile for storing in db"),
    MW_ENCRYPTED_LONG_MSG("encry_long_m", "encrypted_long_msg", "Encrypted All Parts message for storing in db"),
    MW_SUBMISSION_LATENCY_ORG_IN_MILLIS("sub_lat_ori_in_millis", "sub_lat_ori_in_millis", "Submission Latency Original In Milliseconds"),
    MW_SUBMISSION_LATENCY_SLA_IN_MILLIS("sub_lat_sla_in_millis", "sub_lat_sla_in_millis", "Submission Latency SLA In Milliseconds"),
    MW_ERROR_STACKTRACE("err_stack", "error_stacktrace", "Error stack trace"),
    MW_COMPONENT_NAME("cmpt", "component", "component"),
    MW_DB_BILLING_INSERT_DATABASE_SUFFIX("db_bill_ins_db_suf", "db_bill_ins_db_suf", "Database name suffix to insert if datewise enabled."),
    MW_DB_BILLING_INSERT_TABLE_SUFFIX("db_bill_ins_tab_suf", "db_bill_ins_tab_suf", "Table name suffix to insert if datewise enabled."),
    MW_DB_BILLING_INSERT_JNDI("db_bill_ins_jndi", "db_bill_ins_jndi", "Jndi Info id to insert the data."),
    MW_DB_BILLING_INSERT_CLIENT_SUFFIX("db_bill_ins_cli_suf", "db_bill_ins_cli_suf", "Table suffix to insert if client specific enabled in bill_log_map table."),
    MW_CLIENT_HANDOVER_ATTEMPTED_COUNT("cli_hover_atmpt_cnt", "cli_handover_attempted_count", "Number of attempted counts for the client handover"),
    MW_CLIENT_HANDOVER_RETRY_COUNT("cli_hover_rty_cnt", "cli_handover_retry_count", "Number of retry counts for the client handover"),
    MW_CLIENT_HANDOVER_RETRY_TIME("cli_hover_rty_ts", "cli_handover_retry_time", "Retry time for the client handover"),
    MW_CLIENT_HANDOVER_START_TIME("cli_hover_sta_ts", "cli_handover_start_time", "Start time for the client handover"),
    MW_FILE_NAME("file_name", "file_name", "FTP / UI file name"),
    MW_SUB_ORI_STATUS_CODE("sub_ori_sts_code", "sub_ori_sts_code", "Submission Original Status Code"),
    MW_SUB_CLI_STATUS_CODE("sub_cli_sts_code", "sub_cli_sts_code", "Submission Client Status Code"),
    MW_SUB_ORI_STATUS_DESC("sub_ori_sts_desc", "sub_ori_sts_desc", "Submission Original Status Description"),
    MW_SUB_CLI_STATUS_DESC("sub_cli_sts_desc", "sub_cli_sts_desc", "Submission Client Status Description"),
    MW_DN_ORI_STATUS_CODE("dn_ori_sts_code", "dn_ori_sts_code", "Delivery Original Status Code"),
    MW_DN_CLI_STATUS_CODE("dn_cli_sts_code", "dn_cli_sts_code", "Delivery Client Status Code"),
    MW_DN_ORI_STATUS_DESC("dn_ori_sts_desc", "dn_ori_sts_desc", "Delivery Original Status Description"),
    MW_DN_CLI_STATUS_DESC("dn_cli_sts_desc", "dn_cli_sts_desc", "Delivery Client Status Description"),
    MW_CARRIER_ORI_STATUS_CODE("car_ori_sts_code", "car_ori_sts_code", "Carrier Original Status Code"),
    MW_CARRIER_ORI_STATUS_DESC("car_ori_sts_desc", "car_ori_sts_desc", "Carrier Original Status Description"),
    MW_CARRIER_STATUS_CODE("car_sts_code", "car_sts_code", "Carrier Status Code"),
    MW_CARRIER_STATUS_DESC("car_sts_desc", "car_sts_desc", "Carrier Status Description"),
    MW_RETRY_MSG_REJECT("rty_m_reject", "retry_msg_reject", "To indicate whether the message rejected in Retry"),
    MW_DLT_TEMPLATE_TYPE("dlt_tmpl_type", "dlt_template_type", "To indicate the type of the DLT Template"),
    MW_ATTEMPT_COUNT("atmpt_cnt", "attempt_count", "To indicate the attempt count"),
    MW_MT_MSGRETRY_IDENTIFIER("mt_rty_idtifier", "mt_retry_identifier", "To indicate message is retried in MT"),
    MW_INDICATE_DN_FINAL("is_dn_final", "indicate_dn_final", "To indicate message already processed for DN"),
    MW_IS_VOICE_DLR("is_voice_dlr", "is_voice_dlr", "To indicate whether the DLR is from voice"),
    MW_OTP_RETRY_CHANNEL("otp_rty_chnl", "otp_retry_channel", "To store the OTP retry channel"),
    MW_RETRY_ORG_ROUTE_ID("rty_ori_rute_id", "retry_ori_route_id", "On retry to store the original route id"),
    MW_RETRY_ALE_ROUTE_ID("rty_alt_rute_id", "retry_alt_route_id", "On retry to store the Alternate route id"),
    MW_IS_CURRENT("is_cur_m", "is_current_mesage", "To indicate the message as current message"),
    MW_DELIVERY_STATUS("dly_sts", "delivery_status", "To indicate the delivery status"),
    MW_RETRY_CURRENT_TIME("rty_cur_ts", "retry_current_time", "To indicate the retry current time"),
    MW_RETRY_TIME("rty_ts", "retry_time", "To indicate the retry time"),
    MW_RETRY_INTERVAL("rty_in", "retry_interval", "To indicate the retry interval"),
    MW_ADD_SUB_CLIENT_HEADER("add_sub_cli_hdr", "add_sub_client_header", "To indicate the add client headrr in submission billing"),
    MW_DN_CAME_FROM("age_dn_from", "aging_dn_came_from", "To specify the DN came from"),
    MW_FULL_ITEXTO_MESSAGE("full_itexto_m", "msg_json", "to holde the full message"),
    MW_SINGLE_DN_INSERT_REDIS_KEY("sgle_dn_ins_rds_key", "single_dn_insert_redis_key", "store the single dn insert redis key"),
    MW_DN_HANDOVER_BASED_ON("dn_hover_base", "dn_handover_based_on", "Dn handover based on"),
    MW_ERROR_SERVER_IP("s_ip", "server_ip", "Exception occured server ip"),
    MW_ERROR_GENERATED_TIME("err_gen_tim", "error_gen_time", "Error generated Time"),

    MW_CLIENT_HANDOVER_UNIQUE_ID("ch_uni_id", "request_id", "Unique Id for the HTTP request"),
    MW_CLIENT_HANDOVER_MASTER_RECORD("ch_master_rec", "master_rec", "Indicater for the Client Handover Master Record"),
    MW_CLIENT_HANDOVER_MAX_RETRY_COUNT("ch_max_rety_cnt", "max_retry_count", "Maximim Retry Count for a single request"),
    MW_CLIENT_HANDOVER_INITIAL_TIME("ch_initial_time", "ch_start_time", "Time started for the initial request"),
    MW_CLIENT_HANDOVER_CLIENT_URL("ch_cli_url", "cli_url", "URL for the client handover"),
    MW_CLIENT_HANDOVER_HTTP_STATUS_CODE("ch_http_sts_code", "ch_sts_code", "Http Status Code of the URL call"),
    MW_CLIENT_HANDOVER_HTTP_RESPONSE_CONTENT("ch_res", "ch_res", "Response content after URL hit"),
    MW_CLIENT_HANDOVER_HTTP_RESPONSE_TIME("ch_res_time", "ch_res_time", "Response received time for the URL hit"),
    MW_CLIENT_HANDOVER_IS_BATCH("ch_is_batch", "ch_is_batch", "Is the request is made as batch."),
    MW_DELIVERY_HEADER("d_hdr", "dn_hdr", "Delivery Header"),
    MW_URL_SMARTLINK_ENABLE("is_slink", "url_smartlink_enable", "Url SmartLink Enable"),
    MW_CLIENT_HANDOVER_END_TIME("cli_hover_end_time", "cli_handover_end_time", "Client Handover End Time"),
    MW_CLIENT_HANDOVER_REQUEST_CONTENT("cli_hover_request_content", "cli_hover_req_content", "Client Handover Request Content"),
    MW_CLIENT_HANDOVER_RETRY_DATA_MESSAGE("cli_hover_retry_data_msg", "cli_hover_message", "Client Handover retry datat message"),
    MW_URL_TRACKING_ENABLE("is_url_trk", "is_url_track", "Url Tracking Enable"),
    MW_URL_SHORTCODE_LENGTH("sc_len", "sc_len", "Short Code Length"),
    MW_IS_SYNC_REQUEST("is_sync", "is_sync", "Request process type is sync or async"),

    MW_TOPIC_RETRY_ATTEMPT("t_rety_count", "topic_retry_count", "To specify the topic retry attempt"),
    MW_LAST_TOPIC_RETRY_ATTEMPT_TIME("t_rety_att_time", "topic_retry_attempt_time", "To specify the topic last retry attempt time."),
    MW_UI_DUP_CHK("ui_dup_chk", "ui_dup_chk_req", "Duplicate check request from UI"),
    MW_CAMP_ID("camp_id", "campaign_id", "Campaign Id"),
    MW_CAMP_NAME("camp_name", "campaign_name", "Campaign name"),
    MW_UI_VL_SHORTNER_REQ("ui_vl_short_req", "ui_vl_shortner_req", "VL Shortner request from UI"),
    MW_MSG_TAG1("m_tag1", "msg_tag1", "Message Tag1"),
    MW_MSG_TAG2("m_tag2", "msg_tag2", "Message Tag2"),
    MW_MSG_TAG3("m_tag3", "msg_tag3", "Message Tag3"),
    MW_MSG_TAG4("m_tag4", "msg_tag4", "Message Tag4"),
    MW_MSG_TAG5("m_tag5", "msg_tag5", "Message Tag5"),
    MW_ACC_IS_ASYNC("acc_is_async", "acc_is_async", "Account Enabled Async"),
    MW_NEXT_COMPONENT("nxt_comp", "next_component", "Next component for this message to be processed."),
    MW_FROM_COMPONENT("from_comp", "from_component", "From component for this message to be processed."),
    MW_PRCOESSOR_COMPONENT("proc_comp", "processor_component", "Processor component for this message to be processed."),
    MW_PROGRAM_MESSAGE_TYPE("pro_msg_ty", "program_message_type", "To specify the program message type"),
    MW_BYPASS_DLT_TEMPLATE_CHECK("bp_dlt_chk", "bypass_dlt_chk", "By pass DLR template Check"),
    MW_ADD_ERROR_INFO("add_err_info", "add_error_info", "add_error_info"),
    MW_SUB_STATUS("sub_status", "sub_status", "Submission Status"),

    MW_LOG_BUFFER("log_buff", "log_buffer", "Log Buffer for this message to be processed."),

    MW_R3C_URL("url", "url", "URL"),
    MW_R3C_SHORTNER_URL("s_url", "shortner_url", " Shortner Url"),
    MW_R3C_SMARTLINK_ID("smt_id", "smartlink_id", "Smartlink Id"),
    MW_IS_WC_DEDUCT("is_wc", "is_wc_deduct", "is_wc_deduct"),
    MW_CARRIER_DELIVERY_STATUS("car_dly_sts", "car_delivery_status", "To indicate the Carrier delivery status"),
    MW_CLIENT_HANDOVER_LOG_STATUS("status", "status", "client handover status"),
    MW_CLIENT_HANDOVER_RETRY_CHECK("ch_rtry_chk", "client_handover_retry_check", "Client handover Retry check"),
    MW_MSG_SOURCE("m_src", "msg_source", "Message Source"),
    MW_CLI_DLT_TEMPLATE_ID("cli_dlt_tmpl_id", "cli_dlt_tmpl_id", "Client dlt template id from client"),
    MW_CLI_DLT_ENTITY_ID("cli_dlt_enty_id", "cli_dlt_entity_id", "Client dlt entity id from client"),

    MW_RETRY_INIT_TIME("rty_init", "retry_init_time", "Retry init Time"),
    MW_SHORT_MESSAGE("s_msg", "short_msg", "Short Message"),
    MW_NO_PAYLOD_RETRY_EXPIRY_COUNT("npr_exp_cnt", "nopayload_exp_cnt", "No Payload Expiry Count"),
    MW_SNO("sno", "sno", "sno"),
    MW_ROUTE_RETRY_ATTEMPT("rr_atmpt", "route_retry_attempt", "route_retry_attempt"),
    MW_URL_SHORTNER_REQ("url_short_req", "url_shortner_req", "Url Shortner from client"),
    MW_INTL_HEADER_SUB_TYPE("intl_hdr_sub_type", "intl_hdr_sub_type", "Intl Header Sub Type"),
    MW_INTL_HEADER("intl_hdr", "intl_header", "intl_header"),

    MW_BASE_CURRENCY("b_cury", "base_currency", "base_currency"),
    MW_REF_CURRENCY("r_cury", "ref_currency", "ref_currency"),
    MW_BILLING_EXCHANGE_RATE("b_ex_rate", "billing_exchange_rate", "billing_exchange_rate"),
    MW_REF_EXCHANGE_RATE("r_ex_rate", "ref_exchange_rate", "ref_exchange_rate"),
    MW_REF_SMS_RATE("r_sms_rate", "ref_sms_rate", "ref_sms_rate"),
    MW_REF_ADD_FIXED_RATE("r_af_rate", "ref_add_fixed_rate", "ref_add_fixed_rate"),
    MW_BILLING_SMS_RATE("bill_s_rate", "billing_sms_rate", "billing_sms_rate"),
    MW_BILLING_ADD_FIXED_RATE("bill_af_rate", "billing_add_fixed_rate", "billing_add_fixed_rate"),

    MW_SMPP_PROTOCAL("s_protocal", "protocol", "Smpp protocal"),
    MW_SMPP_INSTANCE_ID("s_id", "instanceid", "Smpp Instance Id"),
    MW_SMPP_STATUS("s_status", "status", "Smpp Status"),
    MW_SMPP_MSG_RECEIVED_TIME("smpp_recv_ts", "smpp_recv_time", "Smpp Request Received time"),
    MW_SMPP_SYSTEM_ID("s_sys_id", "smpp_system_id", "Smpp System Id"),
    MW_SMPP_BIND_TYPE("s_b_type", "smpp_bind_type", "Smpp Bind Type"),
    MW_SMPP_DN_SUBMIT_TIME("dn_sub_ts", "submit_ts", "Smpp Dn Submit Time"),
    MW_SMPP_DN_RESPONSE_TIME("dn_resp_ts", "response_ts", "Smpp Dn Response Time"),
    MW_SMPP_REASON("s_reason", "reason", "Smpp Dn Reason"),

    ;

    private final String key;
    private final String name;
    private final String desc;

    MiddlewareConstant(
            String aKey,
            String aName,
            String aDesc)
    {
        key  = aKey;
        name = aName;
        desc = aDesc;
    }

    public String getName()
    {
        return name;
    }

    public String getKey()
    {
        return key;
    }

    public String getDesc()
    {
        return desc;
    }

    private static final Map<String, MiddlewareConstant> allConstantsByKey  = new HashMap<>();
    private static final Map<String, MiddlewareConstant> allConstantsByName = new HashMap<>();

    static
    {
        final MiddlewareConstant[] keyValues = MiddlewareConstant.values();

        for (final MiddlewareConstant mc : keyValues)
        {
            allConstantsByKey.put(mc.key, mc);
            allConstantsByName.put(mc.name, mc);
        }

        final MiddlewareConstant[] nameValues = MiddlewareConstant.values();

        for (final MiddlewareConstant mc : nameValues)
        {
            allConstantsByKey.put(mc.key, mc);
            allConstantsByName.put(mc.name, mc);
        }
    }

    public static MiddlewareConstant getMiddlewareConstantByKey(
            String aKey)
    {
        return allConstantsByKey.get(aKey);
    }

    public static MiddlewareConstant getMiddlewareConstantByName(
            String aName)
    {
        return allConstantsByName.get(aName);
    }

}