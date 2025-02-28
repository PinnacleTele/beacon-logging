package com.itextos.beacon.platform.elasticsearchutil.types;

import java.util.HashMap;
import java.util.Map;

import com.itextos.beacon.commonlib.constants.ItextosEnum;

public enum EsOperation
        implements
        ItextosEnum
{

    DLR_QUERY_SUB_INSERT("dlr_query_sub_insert", EsTypeParent.DLR_QUERY),
    DLR_QUERY_DN_INSERT("dlr_query_dn_insert", EsTypeParent.DLR_QUERY),
    DLR_QUERY_QUERY("dlr_query", EsTypeParent.DLR_QUERY),
    //
    AGING_INSERT("aging_insert", EsTypeParent.AGING),
    AGING_UPDATE("aging_update", EsTypeParent.AGING),
    AGING_DELETE("aging_delete", EsTypeParent.AGING),
    AGING_QUERY("aging_query", EsTypeParent.AGING),
    //
    SINGLE_DN_INSERT("single_dn_insert", EsTypeParent.SINGLE_DN),
    SINGLE_DN_QUERY("single_dn_query", EsTypeParent.SINGLE_DN),
    SINGLE_DN_DELETE("single_dn_delete", EsTypeParent.SINGLE_DN),
    //
    R3_INSERT("r3_insert", EsTypeParent.R3);

    private final String       key;
    private final EsTypeParent parent;

    EsOperation(
            String aKey,
            EsTypeParent aParent)
    {
        key    = aKey;
        parent = aParent;
    }

    @Override
    public String getKey()
    {
        return key;
    }

    public EsTypeParent getParent()
    {
        return parent;
    }

    private static Map<String, EsOperation> mAllTypes = new HashMap<>();

    public static EsOperation getEsType(
            String aKey)
    {

        if (!mAllTypes.isEmpty())
        {
            final EsOperation[] lValues = EsOperation.values();

            for (final EsOperation ip : lValues)
                mAllTypes.put(ip.key, ip);
        }
        return mAllTypes.get(aKey);
    }

}