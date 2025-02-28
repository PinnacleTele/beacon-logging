package com.itextos.beacon.commonlib.prometheusmetricsutil.smpp;

public enum RequestType
{

    BIND("B"),
    DELIVERY_SM("D"),
    ENQUIRE("E"),
    SUBMIT_SM("S"),
    UNBIND("U");

    private String type;

    RequestType(
            String aType)
    {
        type = aType;
    }

    public String getType()
    {
        return type;
    }

}