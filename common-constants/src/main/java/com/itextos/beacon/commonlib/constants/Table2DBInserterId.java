package com.itextos.beacon.commonlib.constants;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public enum Table2DBInserterId
        implements
        ItextosEnum
{

    SUBMISSION("submission"),
    DELIVERIES("deliveries"),
    DELIVERIES_BKUP("deliveries_bkup"),
    FULL_MESSAGE("full_message"),
    INTERIM_FAILUERS("interim_failuers"),
    INTERIM_DELIVERIES("interim_deliveries"),
    ERROR_LOG("error_log"),
    NO_PAYLOAD_DN("no_payload_dn"),
    DN_ERROR_BASED_RETRY("dn_error_based_retry"),
    CLIENT_HANDOVER_MASTER_LOG("client_handover_master_log"),
    CLIENT_HANDOVER_RETRY_DATA("client_handover_retry_data"),
    CLIENT_HANDOVER_LOG("client_handover_log"),
    SMPP_POST_LOG("smpp_post_log"),

    ;

    private static final Log log = LogFactory.getLog(Table2DBInserterId.class);

    private final String     key;

    Table2DBInserterId(
            String aKey)
    {
        key = aKey;
    }

    private static final Map<String, Table2DBInserterId> mAllTypes = new HashMap<>();

    static
    {
        final Table2DBInserterId[] lValues = Table2DBInserterId.values();

        for (final Table2DBInserterId ip : lValues)
            mAllTypes.put(ip.key, ip);
    }

    @Override
    public String getKey()
    {
        return key;
    }

    public static Table2DBInserterId getTableInserterId(
            String aKey)
    {
        final Table2DBInserterId lInserterId = mAllTypes.get(aKey);
        if (lInserterId == null)
            log.error("WARNING: >>> Unable to identify the TableInserterId for '" + aKey + "'. Please check the DB configuration for TableInsertId.",
                    new Exception("Invalid TableInserter key " + aKey));
        return lInserterId;
    }

}
