package com.itextos.beacon.platform.elasticsearchutil.utility;

public class Kafka2ESConstants
{

    public static final String subMode           = "submission";
    public static final String delMode           = "deliveries";

    public static final String subUpdTmColumn    = "sub_update_ts";
    public static final String delUpdTmColumn    = "dn_update_ts";

    public static final String colInitNullValue  = "__NULL__";

    public static final String colTypeText       = "text";
    // public static final String colTypeNumeric = "numeric";
    public static final String colTypeInteger    = "integer";
    public static final String colTypeFloat      = "float";
    public static final String colTypeLong      = "long";

    public static final String colTypeDateTime   = "datetime";
    public static final String colTypeDate       = "date";

    public static final String subStsCodeSuccess = "400";
    public static final String delStsCodeSuccess = "600";
    
    public static final int                       ESRetryConflictCount =5;

    public static final int                       FlushLimit = 2000;


    /*
     es.index.name=
es.index.uidcolumn=msg_id
es.fmsg.index.name=sub_del_t2_fmsg_info
es.fmsg.index.uidcolumn=base_msg_id
es.index.flush.limit=2000
es.update.retry.count=5
es.connection.timeout=5000
es.socket.timeout=60000
     */
    
    public static String                                  ESIndexName             = "sub_del_t2";
    public static String                                  ESIndexUniqueColumn     =  "msg_id";

    public static String                                  ESFmsgIndexName         = "sub_del_t2_fmsg_info";
    public static String                                  ESFmsgIndexUniqueColumn = "base_msg_id";


}
