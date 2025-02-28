package com.itextos.beacon.commonlib.constants;

import java.util.HashMap;
import java.util.Map;

public enum InterfaceStatusCode
        implements
        IStatusCode
{

    SUCCESS("200", "Application Accepted"),

    INVALID_XML("301", "Invalid XML Request"),
    INVALID_JSON("302", "Invalid JSON Request"),
    API_VERSION_INVALID("303", "Invalid Version"),
    INVALID_REQUEST("304", "Bad Request"),
    ACCOUNT_DEACTIVATED("305", "Account Deactivated"),
    ACCOUNT_INVALID_CREDENTIALS("306", "Invalid Credentials"),
    ACCOUNT_EXPIRED("307", "Account expired"),
    ACCOUNT_SUSPENDED("308", "Account Suspended"),
    ACCOUNT_INACTIVE("309", "Account Inactive"),
    ENCRYPTION_OPTION_INVALID("310", "Invalid Encrypt value"),
    DESTINATION_EMPTY("311", "Empty Mobile Number"),
    DESTINATION_INVALID("312", "Mobile Number is not valid"),
    MESSAGE_EMPTY("313", "Empty Message"),
    SCHEDULE_OPTION_DISABLE("314", "Schedule Feature Disabled"),
    SCHEDULE_INVALID_TIME("315", "Invalid schedule time"),
    EXCEED_SCHEDULE_TIME("316", "Exceed Schedule Time"),
    INVALID_MSGTYPE("317", "Message Type is not valid"),
    PORT_INVALID("318", "Destination Port is not valid"),
    EXPIRY_MINUTES_INVALID("319", "Expiry Value is not valid"),
    EXPIRY_MINUTES_BEYOUND_TIME_BOUNDRY("320", "Invalid Expiry Time"),
    COUNTRY_CODE_INVALID_APPEND("321", "Append Country code is not valid"),
    URLTRACK_INVALID_OPTION("322", "URL Track is not valid"),
    CUST_REFERENCE_ID_INVALID("323", "Customer Reference Id is not valid"),
    CUST_REFERENCE_ID_INVALID_LENGTH("324", "Customer Reference Id exceeds length"),
    TRA_BLOCKOUT_TIME("325", "Message Send failed - TRA Blockout Time //"),
    SCHEDULE_TRA_BLOCKOUT_TIME("326", "Message Send failed - Schedule Time is with in TRA Blockout Time"),
    UDHI_INVALID("327", "UDHI is not valid"),
    DCS_INVALID("328", "Invalid Data Coding Value"),
    SENDER_ID_EMPTY("329", "Invalid Header - Empty Header"),
    INVALID_SENDERID("330", "Invalid Header"),
    ACCESS_VIOLATION("331", "Unauthorized Request"),
    INVALID_TEMPLATEID("332", "Template Id is not valid"),
    TEMPLATE_VALUES_EMPTY("333", "Empty Template Values"),
    INVALID_UDH("334", "UDH is not valid"),
    EMPTY_REPORTING_KEY("335", "Empty Reporting Key"),
    INVALID_DLT_ENTITY_ID("336", "DLT Entity Id is not valid"),
    INVALID_DLT_TEMPLATE_ID("337", "DLT Template Id is not valid"),
    IP_RESTRICTED("338", "Invalid source IP"),
    API_SERVICE_DISABLED("339", "API Service Disabled"),
    INTL_SERVICE_DISABLED("340", "International Service Disabled"),
    INVALID_COUNTRY_CODE("341", "Inavalid Country Code"),
    INTERNAL_SERVER_ERROR("398", "Internal Server Error"),

    UNKNOWN_ERROR_CODE("399", "Unknown Error Code"),
    SMPP_THROTTLE_LIMIT_EXCEED("342", "Smpp Throttle Limit Exceed"),
    SMPP_PARTNO_EXCEED_TOTAL_PART_COUNT("343", "Smpp Part Number Exceed Total Part Count"),

    ;

    private final String mStatusCode;
    private final String mStatusDesc;

    InterfaceStatusCode(
            String aStatusCode,
            String aStatudDesc)
    {
        mStatusCode = aStatusCode;
        mStatusDesc = aStatudDesc;
    }

    public String getStatusCode()
    {
        return mStatusCode;
    }

    @Override
    public String getStatusDesc()
    {
        return mStatusDesc;
    }

    private static final Map<String, InterfaceStatusCode> mAllStatusCodes = new HashMap<>();

    static
    {
        final InterfaceStatusCode[] temp = InterfaceStatusCode.values();

        for (final InterfaceStatusCode type : temp)
            mAllStatusCodes.put(type.getStatusCode(), type);
    }

    public static InterfaceStatusCode getStatusDesc(
            String aStatusCode)
    {
        return mAllStatusCodes.get(aStatusCode);
    }

    @Override
    public String getKey()
    {
        return getStatusCode();
    }

}