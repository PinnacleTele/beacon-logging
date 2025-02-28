package com.itextos.beacon.http.interfaceparameters;

import java.util.HashMap;
import java.util.Map;

import com.itextos.beacon.commonlib.constants.ItextosEnum;

public enum InterfaceParameter
        implements
        ItextosEnum
{

    MESSAGES_LIST("Messages List"),
    MESSAGE("Message"),
    MOBILE_NUMBER("Mobile Number"),
    SIGNATURE("Signature"),
    MESSAGE_TYPE("Message Type"),
    UDH_INCLUDE("UDHI"),
    UDH("UDH"),
    DATA_CODING("Data Coding"),
    DESTINATION_PORT("Destination Port"),
    COUNTRY_CODE("Country Code"),
    DLR_REQUIRED("DLR Required"),
    MESSAGE_EXPIRY("Message Expiry"),
    CUSTOMER_MSSAGE_ID("Customer Message Id"),
    TEMPLATE_ID("Template Id"),
    TEMPLATE_VALUES("Template Values"),
    SCHEDULE_TIME("Schedule Time"),
    APPEND_COUNTRY_CODE("Append Country Code"),
    URL_TRACKING("Url Tracking"),
    MSG_TAG("Message Tag"),
    DLT_ENTITY_ID("Dlt EntityId"),
    DLT_TEMPLATE_ID("Dlt TemplateId"),
    DLT_TMA_ID("Dlt TMA Id"),
    PARAM1("param1"),
    PARAM2("param2"),
    PARAM3("param3"),
    PARAM4("param4"),
    PARAM5("param5"),
    PARAM6("param6"),
    PARAM7("param7"),
    PARAM8("param8"),
    PARAM9("param9"),
    PARAM10("param10"),
    MAX_SPLIT("Max Split"),
    URL_SHORTNER("Url Shortner");

    private final String key;

    InterfaceParameter(
            String aKey)
    {
        key = aKey;
    }

    private static final Map<String, InterfaceParameter> mAllParameters = new HashMap<>();

    static
    {
        final InterfaceParameter[] values = InterfaceParameter.values();

        for (final InterfaceParameter ip : values)
            mAllParameters.put(ip.getKey(), ip);
    }

    @Override
    public String getKey()
    {
        return key;
    }

    public static InterfaceParameter getParameter(
            String aKey)
    {
        return mAllParameters.get(aKey);
    }

}