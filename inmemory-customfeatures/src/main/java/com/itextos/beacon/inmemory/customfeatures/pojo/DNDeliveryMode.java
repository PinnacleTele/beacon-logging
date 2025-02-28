package com.itextos.beacon.inmemory.customfeatures.pojo;

import java.util.HashMap;
import java.util.Map;

import com.itextos.beacon.commonlib.constants.ItextosEnum;

public enum DNDeliveryMode
        implements
        ItextosEnum
{

    FIRST_PART("1"),
    LAST_PART("2"),

    AVAILABLE_FIRST_SUCCESS_PART("3"),
    AVAILABLE_FIRST_FAILURE_PART("4"),

    AVAILABLE_LAST_SUCCESS_PART("5"),
    AVAILABLE_LAST_FAILURE_PART("6"),

    EARLIEST_DELIVERED("7"),
    EARLIEST_SUCCESS_DELIVERED("8"),
    EARLIEST_FAILURE_DELIVERED("9"),

    LATEST_DELIVERED("10"),
    LATEST_SUCCESS_DELIVERED("11"),
    LATEST_FAILURE_DELIVERED("12");

    private static Map<String, DNDeliveryMode> allStatus = new HashMap<>();

    static
    {
        final DNDeliveryMode[] temp = DNDeliveryMode.values();

        for (final DNDeliveryMode type : temp)
            allStatus.put(type.getKey(), type);
    }

    public static DNDeliveryMode getDNDeliveryMode(
            String aDNDeliveryMode)
    {
        return allStatus.get(aDNDeliveryMode);
    }

    private String mDNDeliveryMode;

    DNDeliveryMode(
            String aDNDeliveryMode)
    {
        mDNDeliveryMode = aDNDeliveryMode;
    }

    @Override
    public String getKey()
    {
        return mDNDeliveryMode;
    }

}
