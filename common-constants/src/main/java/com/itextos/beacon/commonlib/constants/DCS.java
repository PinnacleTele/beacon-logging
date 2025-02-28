package com.itextos.beacon.commonlib.constants;

import java.util.HashMap;
import java.util.Map;

public enum DCS
        implements
        ItextosEnum
{
	 PLAIN("0"),
	    FLASH_UNICODE1("24"),
	    FLASH_UNICODE2("18"),
	    FLASH_PM("16"),
	    FLASH_PM2("-16"),
	    UNICODE("8"),
	    PEM("12"),
	    BM_2("-11"),
	    BM_3("11"),

    FLASH("240"),
   
    SPECIAL_PORT_PM("4"),
    PDU_MSG("247");

    private String key;

    DCS(
            String aValue)
    {
        key = aValue;
    }

    @Override
    public String getKey()
    {
        return key;
    }

    private static final Map<String, DCS> DCS_VALUES = new HashMap<>();

    static
    {
        final DCS[] lValues = DCS.values();
        for (final DCS dcs : lValues)
            DCS_VALUES.put(dcs.getKey(), dcs);
    }

    public static DCS getDcs(
            String aKey)
    {
        return DCS_VALUES.get(aKey);
    }

    public static DCS getDcs(
            int aKey)
    {
        return getDcs(Integer.toString(aKey));
    }

}