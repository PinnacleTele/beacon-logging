package com.itextos.beacon.smpp.utils.enums;

import java.util.HashMap;
import java.util.Map;

import com.itextos.beacon.commonlib.constants.ItextosEnum;

public enum SmppCharset
        implements
        ItextosEnum
{

    ISO_8859_1("ISO-8859-1"),
    ISO_8859_15("ISO-8859-15"),
    UTF_8("UTF-8"),
    UCS_2("UCS-2"),
    GSM("GSM"),
    GSM7("GSM7"),
    GSM8("GSM8");

    SmppCharset(
            String aKey)
    {
        key = aKey;
    }

    private final String                          key;
    private static final Map<String, SmppCharset> mAllCharsets = new HashMap<>();

    @Override
    public String getKey()
    {
        return key;
    }

    public static SmppCharset getCharset(
            String aKey)
    {

        if (mAllCharsets.size() == 0)
        {
            final SmppCharset[] lValues = SmppCharset.values();

            for (final SmppCharset ip : lValues)
                mAllCharsets.put(ip.key, ip);
        }
        return mAllCharsets.get(aKey);
    }

}
