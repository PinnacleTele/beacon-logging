package com.itextos.beacon.commonlib.constants;

import java.util.HashMap;
import java.util.Map;

public enum DateTimeFormat
        implements
        ItextosEnum
{

    DEFAULT("yyyy-MM-dd HH:mm:ss"),
    DEFAULT_WITH_MILLI_SECONDS("yyyy-MM-dd HH:mm:ss.SSS"),
    DEFAULT_DATE_ONLY("yyyy-MM-dd"),
    DEFAULT_TIME_ONLY("HH:mm:ss"),
    DEFAULT_YYYY_MM_DD_HH_MM("yyyy/MM/dd/HH/mm"),

    // Custom formats with yyyy
    NO_SEPARATOR_YYYY_MM_DD_HH_MM_SS("yyyyMMddHHmmss"),
    NO_SEPARATOR_YYYY_MM_DD_HH_MM_SS_SSS("yyyyMMddHHmmssSSS"),
    NO_SEPARATOR_YYYY_MM_DD_HH("yyyyMMddHH"),
    NO_SEPARATOR_YYYY_MM_DD("yyyyMMdd"),

    // Custom formats with yy
    NO_SEPARATOR_YY_MM_DD_HH_MM("yyMMddHHmm"),
    NO_SEPARATOR_YY_MM_DD_HH_MM_SS("yyMMddHHmmss"),
    NO_SEPARATOR_YY_MM_DD_HH("yyMMddHH");

    private String format;

    DateTimeFormat(
            String aFormat)
    {
        format = aFormat;
    }

    public String getFormat()
    {
        return format;
    }

    @Override
    public String getKey()
    {
        return getFormat();
    }

    private static final Map<String, DateTimeFormat> allFormats = new HashMap<>();

    static
    {
        final DateTimeFormat[] lValues = DateTimeFormat.values();

        for (final DateTimeFormat format : lValues)
            allFormats.put(format.getFormat(), format);
    }

    public static DateTimeFormat getDateTimeFormat(
            String aFormat)
    {
        return allFormats.get(aFormat);
    }

}
