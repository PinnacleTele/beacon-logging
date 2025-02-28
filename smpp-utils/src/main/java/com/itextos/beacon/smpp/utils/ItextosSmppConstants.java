package com.itextos.beacon.smpp.utils;

public class ItextosSmppConstants
{

    private ItextosSmppConstants()
    {}

    public static final String  DEFAULT_SYSTEMID                         = "ITEXTOS";
    public static final boolean DEFAULT_AUTO_NEGOTIATE_INTERFACE_VERSION = true;
    public static final double  DEFAULT_INTERFACE_VERSION                = 3.4;
    public static final boolean DEFAULT_SESSION_COUNTERS_ENABLED         = true;

    public static final String  TRANSMITTER                              = "TX";
    public static final String  RECEIVER                                 = "RX";
    public static final String  TRANSCEIVER                              = "TRX";

    public static final int     DCS_MINUS_16                             = -16;
    public static final int     DCS_INVALID                              = -1;
    public static final int     DCS_ZERO                                 = 0;
    public static final int     DCS_4                                    = 4;
    public static final int     DCS_8                                    = 8;
    public static final int     DCS_12                                   = 12;
    public static final int     DCS_16                                   = 16;
    public static final int     DCS_18                                   = 18;
    public static final int     DCS_24                                   = 24;

    public static final String  ESM_CLASS_40                             = "40";
    public static final String  ESM_CLASS_43                             = "43";

    public static final String  UDH_0500                                 = "0500";
    public static final String  UDH_0608                                 = "0608";
    public static final String  UDH_158_A                                = "158A";
    public static final String  UDH_CONCATENATE_1                        = "050003";
    public static final String  UDH_CONCATENATE_2                        = "060804";

}