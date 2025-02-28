package com.itextos.beacon.commonlib.commondbpool;

public enum DatabaseSchema
{

    CM("cm"),
    ACCOUNTS("accounts"),
    // Configuration should not be read from property file.
    // It is always be 1 and it was hard coded in code.
    CONFIGURATION("configuration"),
    BILLING("billing"),
    BILLINGBKUP("billingbkup"),
    CARRIER_HANDOVER("carrier_handover"),
    SMART_LINK("smartlink"),
    LOGGING("logging"),
    LISTING("listing"),
    PAYLOAD("payload"),
    MESSAGING("messaging"),
    R3C("r3c"),
    CLIENT_HANDOVER("httpclienthandover");

    private String key;

    DatabaseSchema(
            String aKey)
    {
        key = aKey;
    }

    public String getKey()
    {
        return key;
    }

}
