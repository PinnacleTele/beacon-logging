package com.itextos.beacon.r3r.utils;

public class R3RConstants
{

    private R3RConstants()
    {}

    public static final String REQUEST_KEY                       = "key";
    public static final String IPADDRESSKEY                      = "ip";

    public static final String ACCESSKEY                         = "r3r.device.location.access.key";
    public static final String ACCESSURL                         = "r3r.device.location.access.url";
    public static final String DEFAULT_URL                       = "r3c.default.domain.url";

    public static final String REQUEST_HEADER_CONTENT_TYPE_KEY   = "Content-type";
    public static final String REQUEST_HEADER_CONTENT_TYPE_VALUE = "text/xml; charset=ISO-8859-1";

    public static final String REDIRECT_URL_NOT_VALID            = "100";
    public static final String REDIRECT_URL_NOT_AVAILABLE        = "200";
    public static final String REQUEST_SHORTCODE_NOT_AVAILABLE   = "300";
    public static final String SHORTCODE_NOT_PROVIDED            = "400";
    public static final String REDIRECT_URL_EXCEPTION            = "500";

    public static final String DEFAULT_CLIENT_ID                 = "Nil";
    public static final String DEFAULT_DEST                      = "Nil";
    public static final String DEFAULT_MID                       = "Nil";

    public static final int    LOCATION_INFO_RESPONSE_LENGTH     = 9;
    public static final int    COUNTRY_CODE_INDEX                = 3;
    public static final int    COUNTRY_NAME_INDEX                = 4;
    public static final int    REGION_INDEX                      = 5;
    public static final int    CITY_INDEX                        = 6;
    public static final int    LONGITUDE_INDEX                   = 8;
    public static final int    LATITUDE_INDEX                    = 9;

}
