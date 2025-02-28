package com.itextos.beacon.platform.prepaiddata.inmemory;

public class CurrencyData
{

    private final String code;
    private final String desc;

    /**
     * @param aCode
     * @param aDesc
     */
    public CurrencyData(
            String aCode,
            String aDesc)
    {
        super();
        code = aCode;
        desc = aDesc;
    }

    public String getCode()
    {
        return code;
    }

    public String getDesc()
    {
        return desc;
    }

    @Override
    public String toString()
    {
        return "CurrencyData [code=" + code + ", desc=" + desc + "]";
    }

}