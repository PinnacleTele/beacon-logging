package com.itextos.beacon.queryprocessor.commonutils;

public class CommonVariables
{

    public static final String MASTER_DB_JNDI_ID            = "1";
    public static final String MASTER_DB                    = "MASTER";
    public static final String MASTER_PG_DB                 = "MASTER_PG";
    public static final String MARIA_DB                     = "MARIADB";
    public static final String PG_DB                        = "POSTGRESQL";

    public static final String CFG_MARIA_DB_BILLNIG_JNDI    = "mdb_billing_jndi";
    public static final String CFG_PG_DB_BILLNIG_JNDI       = "pgdb_billing_jndi";

    public static final String MARIA_DB_PREFIX              = "billing";
    public static final String MARIA_DB_SUB_TABLE_PREFIX    = "submission";
    public static final String MARIA_DB_DEL_TABLE_PREFIX    = "deliveries";
    public static final String MARIA_DB_FMSG_TABLE_PREFIX   = "full_message";
    public static final String PG_DB_SUB_DEL_TABLE_PREFIX   = "sub_del_log";
    public static final String PG_DB_FMSG_TABLE_PREFIX      = "full_message";
    public static final String PG_DB_FMSG_INFO_TABLE_PREFIX = "sub_del_log_fmsg_info";

    public static final String GET_DATA_API                 = "GET_DATA";
    public static final String LOG_QUEUE_CREATE_API         = "LOG_DATA_QUEUE_CREATE";
    public static final String LOG_QUEUE_STATUS_API         = "LOG_DATA_QUEUE_STATUS";
    public static final String LOG_QUEUE_LIST_API           = "LOG_DATA_QUEUE_LIST";

    public static final String COL_TEXT_TYPE                = "text";
    public static final String COL_NUMERIC_TYPE             = "numeric";
    public static final String COL_DATETIME_TYPE            = "datetime";

    public static final String MDB_IS_HEX_MSG               = "is_hex_msg";
    public static final String PGDB_IS_HEX_MSG              = "sub_is_hex_msg";

    public static final String USER_COL_NAME                = "user";
    public static final String USER_COL_TYPE                = "text";

    public static final String MSG_ID_COL_NAME              = "msg_id";
    public static final String CLI_ID_COL_NAME              = "cli_id";
    public static final String MDB_DLY_TIME_COL_NAME        = "dly_time";
    public static final String PGDB_DLY_TIME_COL_NAME       = "del_dly_time";
    public static final String MDB_MSG_COL_NAME             = "msg";
    public static final String PGDB_MSG_COL_NAME            = "sub_msg";
    public static final String MDB_BILL_CURR_COL_NAME       = "billing_currency";
    public static final String MDB_SMS_RATE_COL_NAME        = "sms_rate";
    public static final String MDB_DLT_RATE_COL_NAME        = "dlt_rate";
    public static final int    MDB_SMS_RATE_DECIMALS        = 6;
    public static final int    MDB_DLT_RATE_DECIMALS        = 6;

    public static final String DUP_MSGID_FOUND              = "DUP_MSGID_FOUND";

    public static final String SUB_STS_CODE_SUCCESS         = "400";
    public static final String DEL_STS_CODE_SUCCESS         = "600";

    public static final String MDB_ERR_CODE_COL_NAME        = "sub_ori_sts_code";
    public static final String PGDB_ERR_CODE_COL_NAME       = "sub_sub_ori_sts_code";

    public static final String LOG_USERNAME                 = "Username";
    public static final String LOG_STATUS_COL               = "status";
    public static final String LOG_REASON_COL               = "reason";

    public static final String FMSG_SUB_STATUS_SUCCESS      = "Success";
    public static final String FMSG_SUB_REASON_SUCCESS      = "Success";

    public static final String FMSG_DEL_STATUS_SUCCESS      = "Delivered";
    public static final String FMSG_DEL_REASON_SUCCESS      = "DELIVRD";

    public static final String FMSG_STATUS_FAILED           = "Failed";
    public static final String FMSG_REASON_FAILED           = "Failed";

    public static final String QUERY_PARAM                  = "query_parameters";

    public static final String R_FULL_MESSAGE               = "full_message";
    public static final String R_CLI_ID                     = "cli_id";
    public static final String R_PARAM                      = "r_param";
    public static final String R_APP                        = "r_app";
    public static final String R_APP_VERSION                = "r_app_version";
    public static final String R_USERNAME                   = "r_username";
    public static final String R_CURRENT_STATUS             = "current_status";
    public static final String R_COMPLETION_STATUS          = "completed_status";

    public static final String STATUS_CODE                  = "status_code";
    public static final String STATUS_MESSAGE               = "message";
    public static final String SERVER_TIMESTAMP             = "server_timestamp";

    public static final String QUERY_TYPE                   = "LOG_DATA";
    public static final String SUCCESS                      = "success";
    public static final String START_DATE                   = "recv_date_from";
    public static final String END_DATE                     = "recv_date_to";

    public static final String COMPLETION_SUCCESS           = "SUCCESS";
    public static final String COMPLETION_FAILURE           = "FAIL";
    public static final String COMPLETION_ERROR             = "ERROR";

    public static final String QUEUE_STARTED                = "FETCHING_DATA";
    public static final String QUEUE_SORTING                = "SORTING_DATA";
    public static final String QUEUE_COMPLETED              = "COMPLETED";
    public static final String QUEUED                       = "QUEUED";

    public static final String ERROR                        = "ERROR";
    public static final String INFO                         = "INFO";

    public static final String QUEUE_ID                     = "queue_id";
    public static final String PAGE_NO                      = "page_no";

    public static final String SQLT_TABLE_NAME              = "query_result";
    public static final String SQLT_CSV_COLUMN_NAME         = "csv_record";
    public static final String SQLT_FMSG_DATA_TABLE_NAME    = "fmsg_data";
    public static final String SQLT_FMSG_1PART_TABLE_NAME   = "fmsg_1part";
    public static final int    SQLT_FMSG_1PART_BATCH_COUNT  = 5000;

    public static final String SQLT_FMSG_INFO_TABLE_NAME    = "fmsg_info";
    public static final int    SQLT_FMSG_INFO_BATCH_COUNT   = 1000;

}
