package com.itextos.beacon.dlrquery.response;

final class ResponseConstants
{

    public static final String JSON_TOP              = "{\"records\":[";
    public static final String JSON_BOTTOM           = "]}";
    public static final String JSON_RECORD           = "{" //
            + "\"file_id\":\"$[file_id]\"," //
            + "\"dest\":\"$[dest]\"," //
            + "\"long_id\":\"$[base_msg_id]\"," //
            + "\"received_time\":\"$[recv_time]\"," //
            + "\"header\":\"$[cli_hdr]\"," //
            + "\"ref_id\":\"$[cli_msg_id]\"," //
            + "\"total_parts\":\"$[total_msg_parts]\"," //
            + "\"msg_id\":\"$[msg_id]\"," //
            + "\"sub\":{" //
            + "\"submit_time\":\"$[carrier_sub_time]\"," //
            + "\"status_code\":\"$[sub_cli_sts_code]\"," //
            + "\"status_desc\":\"$[sub_cli_sts_desc]\"}," //
            + "\"del\":{" //
            + "\"delivery_time\":\"$[dly_time]\"," //
            + "\"status_code\":\"$[dn_cli_sts_code]\"," //
            + "\"status_desc\":\"$[dn_cli_sts_desc]\"}" //
            + "}";
    public static final String JSON_RECORD_SEPARATOR = ",";

    public static final String XML_TOP               = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><records>";
    public static final String XML_BOTTOM            = "</records></root>";
    public static final String XML_RECORD            = "<element>" //
            + "<file_id>$[file_id]</file_id>" //
            + "<dest>$[dest]</dest>" //
            + "<long_id>$[base_msg_id]</long_id>" //
            + "<received_time>$[recv_time]</received_time>" //
            + "<header>$[cli_hdr]</header>" //
            + "<ref_id>$[cli_msg_id]</ref_id>" //
            + "<total_parts>$[total_msg_parts]</total_parts>" //
            + "<msg_id>$[msg_id]</msg_id>" //
            + "<sub>" //
            + "<status_code>$[sub_cli_sts_code]</status_code>" //
            + "<status_desc>$[sub_cli_sts_desc]</status_desc>" //
            + "<submit_time>$[carrier_sub_time]</submit_time>" //
            + "</sub>" //
            + "<del>" //
            + "<delivery_time>$[dly_time]</delivery_time>" //
            + "<status_code>$[dn_cli_sts_code]</status_code>" //
            + "<status_desc>$[dn_cli_sts_desc]</status_desc>" //
            + "</del>" //
            + "</element>";
    public static final String XML_RECORD_SEPARATOR  = "";

}