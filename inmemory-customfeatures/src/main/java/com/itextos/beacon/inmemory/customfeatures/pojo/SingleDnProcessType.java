package com.itextos.beacon.inmemory.customfeatures.pojo;

import java.util.HashMap;
import java.util.Map;

import com.itextos.beacon.commonlib.constants.ItextosEnum;

public enum SingleDnProcessType
        implements
        ItextosEnum
{

    ALL_SUCCESS("1"),
    ALL_FAILURE("2"),
    PARTIAL_SUCCESS("3"),
    PARTIAL_FAILURE("4"),
    ANY_FAILURE("5"),
    ATLEAST_ONE_SUCCESS("6");

    private static Map<String, SingleDnProcessType> allStatus = new HashMap<>();

    static
    {
        final SingleDnProcessType[] temp = SingleDnProcessType.values();

        for (final SingleDnProcessType type : temp)
            allStatus.put(type.getKey(), type);
    }

    public static SingleDnProcessType getSingleDnProcessType(
            String aStatusCategory)
    {
        return allStatus.get(aStatusCategory);
    }

    private String mStatusCategory;

    SingleDnProcessType(
            String aStatusCategory)
    {
        mStatusCategory = aStatusCategory;
    }

    @Override
    public String getKey()
    {
        return mStatusCategory;
    }

}
