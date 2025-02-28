package com.itextos.beacon.commonlib.constants;

import java.util.HashMap;

public enum PlatformStatusCode
        implements
        IStatusCode
{

    BAD_REQUEST("304", "Bad Request"),
    MOBILE_NUMBER_IS_NOT_VALID("312", "Mobile Number is not valid"),
    SUCCESS("400", "Success"),
    SYSTEM_ERROR("401", "System Error"),
    DND_REJECT("402", "DND Rejected"),
    PROMOTIONAL_MESSAGE_NOT_ALLOW("403", "Promotional message is not allowed"),
    INVALID_HEADER("404", "Invalid Header"),
    EMPTY_FEATURE_CODE("405", "Empty Feature Code"),
    KANNEL_TEMPLATE_NOT_FOUND("406", "Kannel Template Not Found"),
    EXPIRED_MESSAGE("407", "Expired Message"),
    DUPLICATE_CHECK_FAILED("408", "Duplicate Check failed"),
    GLOBAL_HEADER_BLOCK_FAILED("409", "Global Header block failed"),
    GLOBAL_MOBILE_NUMBER_BLOCK_FAILED("410", "Global Mobile Number Block failed"),
    ROUTE_BASED_HEADER_FAILED("411", "Route Based Header failed"),
    SPECIFIC_BLOCKOUT_FAILED("412", "Specific Blockout failed"),
    TRAI_BLOCKOUT_FAILED("413", "TRA Blockout failed"),
    MESSAGE_SPAM_FILTER_FAILED("414", "Message Spam Filter failed"),
    SMS_BLOCKOUT_FAILED("415", "SMS Blockout failed"),
    URL_SHORTNER_PROCESS_FAILED("416", "URL Shortner process failed"),
    PREPAID_CHECK_FAILED("417", "Wallet Check Failed"),
    INTL_CREDIT_NOT_SPECIFIED("418", "Intl Credit Not Specified"),
    INVALID_HEX_MESSAGE("419", "Invalid HEX message"),
    INVALID_UDH("420", "Invalid UDH"),
    INVALID_MESSAGE("421", "Invalid message"),
    EMPTY_ROUTE_ID("422", "Empty Route id"),
    INVALID_ROUTE_ID("423", "Invalid Route id"),
    INTL_COUNTRY_CODE_RANGE_NOT_AVAILABLE("424", "International Country Code Range not available"),
    INTL_INVALID_MOBILE_LENGTH("425", "International Invalid Mobile Number length"),
    INTL_ROUTE_EXPIRED("426", "International Route Expired"),
    CONCAT_MESSAGE_PARTS_NOT_RECEIVED("427", "All parts of Concat message not received"),
    INTL_INVALID_ROUTE("428", "International Invalid Route"),
    INTL_SERVICE_DISABLED("429", "International Service Disabled"),
    HEADER_PATTERN_CHECK_FAILED("430", "Header pattern check failed"),
    REJECT_TIMEBOUND_CHECK("431", "Reject Timebound check"),
    REJECTED_IN_DLT_TEMPLATE_CHECK("432", "Rejected in DLT Template check"),
    INVALID_DLT_ENTITY_ID("433", "Invalid DLT Entity id"),
    ACCOUNT_DEACTIVATED("434", "Account Deactivated"),
    ACCOUNT_SUSPENDED("435", "Account Suspended"),
    CARRIER_HANDOVER_FAILED("436", "Carrier Handover Failed"),
    SUBMISSION_DND_STATUS_ID("437", "Submission DND Status"),
    DELIVERIES_DND_STATUS_ID("438", "Deliveries DND Status"),
    DEFAULT_AGING_ERROR_CODE("439", "Default Aging Error Code"),
    CUSTOMER_HEADER_POOL_FAILED("440", "Custom Header Pool Failed"),
    PROMO_TEMPORARY_FAILED("441", "Promotional message is not allowed"),
    PLATFORM_UNKNOWN_ERROR_CODE("499", "Unknown Error Code"),
    EXCEED_MAX_SPLIT_PARTS("442", "Exceed Max Split Parts"),
    PARTIALLY_CARRIER_HANDOVER_FAILED("443", "Partially Carrier Handover Failed"),
    INVALID_DLT_TEMPLATE_GROUP_ID("444", "Invalid DLT Template Group Id"),
    INSUFFICIENT_WALLET_BALANCE("445", "Insufficient Wallet Balance"),

    OPERATOR_UNKNOWN_ERROR_CODE("699", "Unknown Error Code"),
    DEFAULT_CARRIER_STATUS_ID("EEE", "Default Carrier Status"),

    CUSTOM_MOBILE_NUMBER_BLACK_FAILED("446", "Custom Mobile Number Black failed"),
    PRICE_CONVERSION_FAILED("447", "Price Conversion Failed"),
    SMPP_SAME_UDH("448", "Same UDH for All Parts"),
    SMPP_UDH_TOTAL_PART_MISMATCH("449", "UDH Total Part Mismatch"),
    CONCATE_ORPHAN_EXPIRY("450", "Orphan Concat Expiry"),
    INVALID_DATA_CODING_SCHEME("451", "Invalid Data Coding Scheme"),
    REJECT_CAPPING_CHECK("452", "Exceed Capping Limit"),

    ;

    private final String mStatusCode;
    private final String mStatusDesc;

    PlatformStatusCode(
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

    private static HashMap<String, PlatformStatusCode> mAllStatusCodes = new HashMap<>();

    static
    {
        final PlatformStatusCode[] temp = PlatformStatusCode.values();

        for (final PlatformStatusCode type : temp)
            mAllStatusCodes.put(type.getStatusCode(), type);
    }

    public static PlatformStatusCode getStatusDesc(
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