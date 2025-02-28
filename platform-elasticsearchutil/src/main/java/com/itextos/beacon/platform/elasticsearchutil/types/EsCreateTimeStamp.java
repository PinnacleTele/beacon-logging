package com.itextos.beacon.platform.elasticsearchutil.types;

import com.itextos.beacon.commonlib.constants.ItextosEnum;

public enum EsCreateTimeStamp
        implements
        ItextosEnum
{

    NONE("none"),
    SINGLE_DN_CTIME("single_ctime"),
    AGING_DN_CTIME("aging_ctime"),
    AGING_DN_UTIME("aging_utime"),
    DLR_QUERY_SUB_CTIME("sub_ctime"),
    DLR_QUERY_DN_CTIME("dn_ctime"),
    R3_CTIME("r3_ctime"),
    //
    ;

    private String key;

    EsCreateTimeStamp(
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