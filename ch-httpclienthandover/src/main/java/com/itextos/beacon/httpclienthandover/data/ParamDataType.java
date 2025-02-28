package com.itextos.beacon.httpclienthandover.data;

public enum ParamDataType
{

    STRING("1"),
    NUMBER("2"),
    DATE_TIME("3");

    private String key;

    ParamDataType(
            String aKey)
    {
        key = aKey;
    }

    public String getKey()
    {
        return key;
    }

    public static ParamDataType getParamDataType(
            String aType)
    {

        switch (aType)
        {
            case "1":
                return STRING;

            case "2":
                return NUMBER;

            case "3":
                return DATE_TIME;

            default:
                return STRING;
        }
    }

}