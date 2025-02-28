package com.itextos.beacon.smpp.utils.properties;

import java.util.Arrays;
import java.util.List;

public class SmppPropertiesConstant
{

    static final String       SMPP_API_ADMIN_PORT                                = "smpp.interface.admin.server.port";
    static final String       SMPP_API_CLIENT_SOCKET_TIMEOUT                     = "smpp.interface.as.client.socket.timeout";
    static final String       SMPP_API_LISTEN_PORT                               = "smpp.interface.listen.port";
    static final String       SMPP_API_BIND_TIMEOUT                              = "smpp.bind.timeout";
    static final String       SMPP_API_INSTANCE_ALLOW_MAX_BINDS                  = "smpp.max.binds";
    static final String       SMPP_API_WINDOW_SIZE                               = "smpp.window.size";
    static final String       SMPP_API_REQ_TIMEOUT                               = "smpp.request.timeout";
    static final String       SMPP_API_DN_REQ_TIMEOUT                            = "smpp.dnrequest.timeout";
    static final String       SMPP_API_MSG_EXPIRE_IN_MINUTES                     = "expire.message.in.minutes";
    static final String       SMPP_API_SESSION_WINDOW_SIZE                       = "smpp.session.window.size";
    static final String       SMPP_API_ALLOW_BIND_TYPE                           = "bind.type.allowed";
    static final String       INVALID_BINDINFO_LOG_FILE_PATH                     = "invalid.bindinfo.log.file.path";
    static final String       ALLOW_DLR_SESSIONS                                 = "dn.session.thread.enabled";
    static final String       STORE_INVALID_BINDINFO_LOG                         = "store.invalid.bindinfolog";
    static final String       STRING_INSTANCE_ID                                 = "instance.id";
    static final String       DB_INSERT_REQUIRED                                 = "db.insert.required";
    static final String       VSMS_DN_REQUEST_TIMEOUT                            = "smpp.dnrequest.timeout";
    static final String       SMPP_DN_WAITING_MS                                 = "smpp.dn.waiting.ms";
    static final String       SMPP_IDLE_SESSION_ALLOWED_TIME                     = "smpp.idle.session.allowed.time";
    static final String       SMPP_DISABLED_SCCOUNT_CHECK_SEC                    = "smpp.disabled.account.check.sec";
    static final String       SMPP_MAX_BINDS_PER_INSTANCE                        = "smpp.max.binds";
    static final String       SMPP_BIND_DB_INSERT_REQ                            = "bind.db.insert.req";
    static final String       SMPP_INSTANCE_CLUSTER                              = "instance.cluster";
    static final String       CONCAT_REDIS_ALLOWED_MEMORY_BYTES                  = "redis.allowed.memory.bytes";
    static final String       TRACE_MONITOR_CLIENT                               = "trace.monitor.client";
    static final String       CONCAT_MESSAGE_PROCESS_ENABLE                      = "concat.message.proess.enable";
    static final String       CONCAT_MESSAGE_POOLER_REDIS_CONSUMER_COUNT         = "concat.message.pooler.redis.consumer.count";
    static final String       SMPP_CLUSTER_INSTANCE_ALLOW                        = ".interface.allow";

    static final int          DEFAULT_ADMIN_PORT                                 = 2021;
    static final int          DEFAULT_CLIENT_SOCKET_TIMEOUT                      = 5000;
    static final int          DEFAULT_API_LISTEN_PORT                            = 2775;
    static final int          DEFAULT_API_BIND_TIMEOUT                           = 30 * 1000; // 30 Seconds
    static final int          DEFAULT_API_INSTANCE_ALLOW_MAX_BINDS               = 500;
    static final int          DEFAULT_API_WINDOW_SIZE                            = 100;
    static final int          DEFAULT_API_REQ_TIMEOUT                            = 5000;
    static final int          DEFAULT_API_MSG_EXPIRE_MINUTES                     = 24 * 60; // one day
    static final int          DEFAULT_API_DN_REQ_TIMEOUT                         = 250;
    static final int          DEFAULT_API_SESSION_WINDOW_SIZE                    = 1;
    static final List<String> DEFAULT_BIND_TYPE                                  = Arrays.asList("TX,RX,TRX");
    static final String       DEFAULT_ALLOW_DLR_SESSIONS                         = "0";
    static final String       DEFAULT_INVALID_BINDINFO_LOG                       = "0";
    static final String       DEFAULT_DB_INSERT_REQUIRED                         = "1";
    static final int          DEFAULT_VSMS_DN_REQUEST_TIMEOUT                    = 250;
    static final long         DEFAULT_SMPP_DN_WAITING_MS                         = 1800000;
    static final int          DEFAULT_SMPP_IDLE_SESSION_ALLOWED_TIME             = 180;
    static final int          DEFAULT_SMPP_DISABLED_SCCOUNT_CHECK_SEC            = 180;
    static final int          DEFAULT_SMPP_MAX_BINDS_PER_INSTANCE                = 500;
    static final long         DEFAULT_ALLOWED_LIMIT                              = 1024 * 1024 * 500L;
    static final int          DEFAULT_CONCAT_MESSAGE_POOLER_REDIS_CONSUMER_COUNT = 1;

    private SmppPropertiesConstant()
    {}

}