package com.itextos.beacon.commonlib.constants;

public enum UdhHeaderInfo
        implements
        ItextosEnum
{

    CONCAT_8BIT_HEADER("050003"),
    CONCAT_16BIT_HEADER("060804"),
    CONCAT_PORT_HEADER_PREFIX("060504"),
    CONCAT_PORT_HEADER_SUFFIX("0000"),
    CONCAT_PORT_MULTI_HEADER_PREFIX("0B0504"),
    CONCAT_PORT_MULTI_HEADER_SUFFIX("00000003");

    private String key;

    UdhHeaderInfo(
            String aValue)
    {
        key = aValue;
    }

    @Override
    public String getKey()
    {
        return key;
    }

}