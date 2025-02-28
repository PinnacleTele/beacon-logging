package com.itextos.beacon.dlrquery.request.enums;

import java.util.HashMap;
import java.util.Map;

public enum DlrQueryStatus
{

    VALID_REQUEST("000", "SUCCESS"),
    INTERNAL_ERROR("500", "Internal Error"),
    INVALID_ACCESS_KEY("301", "Invalid Access Key"),
    INVALID_NO_VALID_INPUT_PARAMETERS("302", "No Valid Parameters"),
    INVALID_MORE_THAN_ONE_MULTIPLE_PARAMETERS("303", "More than one multiple parameters"),
    IP_RESTRICTED("304", "Invalid source IP"),
    DLR_QUERY_FEATURE_DISABLED("305", "Dlr Query Feature Disabled"),

    ;

    private final String mStatusCode;
    private final String mStatusDesc;

    DlrQueryStatus(
            String aStatusCode,
            String aStatusDesc)
    {
        mStatusCode = aStatusCode;
        mStatusDesc = aStatusDesc;
    }

    public String getStatusCode()
    {
        return mStatusCode;
    }

    public String getStatusDesc()
    {
        return mStatusDesc;
    }

    private static final Map<String, DlrQueryStatus> mAllTypes = new HashMap<>();
    static
    {
        final DlrQueryStatus[] values = DlrQueryStatus.values();

        for (final DlrQueryStatus dqs : values)
            mAllTypes.put(dqs.getStatusCode(), dqs);
    }

    public static DlrQueryStatus getType(
            String aStatusCode)
    {
        return mAllTypes.get(aStatusCode);
    }

}