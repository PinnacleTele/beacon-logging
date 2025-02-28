package com.itextos.beacon.platform.elasticsearchutil.types;

import java.util.ArrayList;
import java.util.List;

import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.platform.elasticsearchutil.data.R3Info;
import com.itextos.beacon.platform.elasticsearchutil.utility.EsFieldDefinition;

public final class EsConstant
{

    private EsConstant()
    {}

    public static final MiddlewareConstant[] DNQUERY_KEYWORDS          = getDnQueryKeywords();
    public static final EsFieldDefinition[]  DNQUERY_SUBMISSION_FIELDS = getDnQuerySubFields();
    public static final EsFieldDefinition[]  DNQUERY_DELIVERIES_FIELDS = getDnQueryDnFields();
    public static final EsFieldDefinition[]  DNQUERY_RESPONSE_FIELDS   = getDnQueryResponseFields();

    public static final MiddlewareConstant[] AGING_KEYWORDS            = getAgingKeywords();
    public static final EsFieldDefinition[]  AGING_INSERT_FIELDS       = getAgingInsertFields();
    public static final EsFieldDefinition[]  AGING_UPDATE_FIELDS       = getAgingUdpdateFields();

    public static final MiddlewareConstant[] SINGLE_DN_KEYWORDS        = getSingleDnKeywords();
    public static final EsFieldDefinition[]  SINGLE_DN_INSERT_FIELDS   = getSingleDnFields();

    public static final String[]             VL_KEYWORDS               = R3Info.INDEX_KEYS;

    private static EsFieldDefinition[] getSingleDnFields()
    {
        return new EsFieldDefinition[]
        { //
                new EsFieldDefinition(MiddlewareConstant.MW_FILE_ID, EsFieldDataType.STRING),//
                new EsFieldDefinition(MiddlewareConstant.MW_BASE_MESSAGE_ID, EsFieldDataType.STRING), //
                new EsFieldDefinition(MiddlewareConstant.MW_MESSAGE_ID, EsFieldDataType.STRING), //
                new EsFieldDefinition(MiddlewareConstant.MW_CLIENT_ID, EsFieldDataType.STRING), //
                new EsFieldDefinition(MiddlewareConstant.MW_CLIENT_MESSAGE_ID, EsFieldDataType.STRING), //
                new EsFieldDefinition(MiddlewareConstant.MW_MOBILE_NUMBER, EsFieldDataType.LONG), //
                new EsFieldDefinition(MiddlewareConstant.MW_MSG_PART_NUMBER, EsFieldDataType.INTEGER), //
                new EsFieldDefinition(MiddlewareConstant.MW_MSG_TOTAL_PARTS, EsFieldDataType.INTEGER), //
                new EsFieldDefinition(MiddlewareConstant.MW_MSG, EsFieldDataType.STRING), //
                new EsFieldDefinition(MiddlewareConstant.MW_DELIVERY_TIME, EsFieldDataType.DATE_AS_LONG, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS.getFormat()), //
                new EsFieldDefinition(MiddlewareConstant.MW_MSG_RECEIVED_TIME, EsFieldDataType.DATE_AS_LONG, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS.getFormat()), //
                new EsFieldDefinition(MiddlewareConstant.MW_MSG_RECEIVED_DATE, EsFieldDataType.DATE_AS_LONG, DateTimeFormat.DEFAULT_DATE_ONLY.getFormat()), //
                new EsFieldDefinition(MiddlewareConstant.MW_UDH, EsFieldDataType.STRING), //
        };
    }

    private static MiddlewareConstant[] getAgingKeywords()
    {
        // TODO Auto-generated method stub
        return null;
    }

    private static MiddlewareConstant[] getDnQueryKeywords()
    {
        return new MiddlewareConstant[]
        { MiddlewareConstant.MW_CLIENT_ID, //
                MiddlewareConstant.MW_FILE_ID,//
                MiddlewareConstant.MW_CLIENT_MESSAGE_ID,//
                MiddlewareConstant.MW_MOBILE_NUMBER };
    }

    private static EsFieldDefinition[] getDnQueryResponseFields()
    {
        return new EsFieldDefinition[]
        { //
                new EsFieldDefinition(MiddlewareConstant.MW_CLIENT_ID, EsFieldDataType.STRING), //
                new EsFieldDefinition(MiddlewareConstant.MW_CLIENT_MESSAGE_ID, EsFieldDataType.STRING), //
                new EsFieldDefinition(MiddlewareConstant.MW_MOBILE_NUMBER, EsFieldDataType.LONG), //
                new EsFieldDefinition(MiddlewareConstant.MW_FILE_ID, EsFieldDataType.STRING), //
                new EsFieldDefinition(MiddlewareConstant.MW_BASE_MESSAGE_ID, EsFieldDataType.STRING), //
                new EsFieldDefinition(MiddlewareConstant.MW_MESSAGE_ID, EsFieldDataType.STRING), //
                new EsFieldDefinition(MiddlewareConstant.MW_MSG_RECEIVED_DATE, EsFieldDataType.DATE_AS_LONG, DateTimeFormat.DEFAULT_DATE_ONLY.getFormat()),//
                new EsFieldDefinition(MiddlewareConstant.MW_MSG_RECEIVED_TIME, EsFieldDataType.DATE_AS_LONG, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS.getFormat()), //
                new EsFieldDefinition(MiddlewareConstant.MW_HEADER, EsFieldDataType.STRING), //
                new EsFieldDefinition(MiddlewareConstant.MW_CARRIER_SUBMIT_TIME, EsFieldDataType.DATE_AS_LONG, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS.getFormat()), //
                new EsFieldDefinition(MiddlewareConstant.MW_MSG_TOTAL_PARTS, EsFieldDataType.INTEGER), //
                new EsFieldDefinition(MiddlewareConstant.MW_SUB_CLI_STATUS_CODE, EsFieldDataType.STRING), //
                new EsFieldDefinition(MiddlewareConstant.MW_SUB_CLI_STATUS_DESC, EsFieldDataType.STRING), //
                new EsFieldDefinition(MiddlewareConstant.MW_CARRIER_RECEIVED_TIME, EsFieldDataType.DATE_AS_LONG, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS.getFormat()), //
                new EsFieldDefinition(MiddlewareConstant.MW_DN_CLI_STATUS_CODE, EsFieldDataType.STRING), //
                new EsFieldDefinition(MiddlewareConstant.MW_DN_CLI_STATUS_DESC, EsFieldDataType.STRING), //
                new EsFieldDefinition(MiddlewareConstant.MW_DELIVERY_HEADER, EsFieldDataType.STRING),
                new EsFieldDefinition(MiddlewareConstant.MW_DELIVERY_TIME, EsFieldDataType.DATE_AS_LONG, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS.getFormat()),

        };
    }

    private static EsFieldDefinition[] getDnQuerySubFields()
    {
        final List<EsFieldDefinition> returnValue = getDnQueryCommonInsertFields();

        returnValue.add(new EsFieldDefinition(MiddlewareConstant.MW_CARRIER_SUBMIT_TIME, EsFieldDataType.DATE_AS_LONG, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS.getFormat()));
        returnValue.add(new EsFieldDefinition(MiddlewareConstant.MW_HEADER, EsFieldDataType.STRING));
        returnValue.add(new EsFieldDefinition(MiddlewareConstant.MW_MSG_PART_NUMBER, EsFieldDataType.INTEGER));
        returnValue.add(new EsFieldDefinition(MiddlewareConstant.MW_MSG_TOTAL_PARTS, EsFieldDataType.INTEGER));
        returnValue.add(new EsFieldDefinition(MiddlewareConstant.MW_SUB_CLI_STATUS_CODE, EsFieldDataType.STRING));
        returnValue.add(new EsFieldDefinition(MiddlewareConstant.MW_SUB_CLI_STATUS_DESC, EsFieldDataType.STRING));
        final EsFieldDefinition[] returns = new EsFieldDefinition[returnValue.size()];
        return returnValue.toArray(returns);
    }

    private static EsFieldDefinition[] getDnQueryDnFields()
    {
        final List<EsFieldDefinition> returnValue = getDnQueryCommonInsertFields();
        returnValue.add(new EsFieldDefinition(MiddlewareConstant.MW_CARRIER_RECEIVED_TIME, EsFieldDataType.DATE_AS_LONG, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS.getFormat()));
        returnValue.add(new EsFieldDefinition(MiddlewareConstant.MW_DN_CLI_STATUS_CODE, EsFieldDataType.STRING));
        returnValue.add(new EsFieldDefinition(MiddlewareConstant.MW_DN_CLI_STATUS_DESC, EsFieldDataType.STRING));
        returnValue.add(new EsFieldDefinition(MiddlewareConstant.MW_DELIVERY_HEADER, EsFieldDataType.STRING));
        returnValue.add(new EsFieldDefinition(MiddlewareConstant.MW_DELIVERY_TIME, EsFieldDataType.DATE_AS_LONG, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS.getFormat()));

        final EsFieldDefinition[] returns = new EsFieldDefinition[returnValue.size()];
        return returnValue.toArray(returns);
    }

    private static List<EsFieldDefinition> getDnQueryCommonInsertFields()
    {
        final List<EsFieldDefinition> returnValue = new ArrayList<>();

        returnValue.add(new EsFieldDefinition(MiddlewareConstant.MW_CLIENT_ID, EsFieldDataType.STRING));
        returnValue.add(new EsFieldDefinition(MiddlewareConstant.MW_CLIENT_MESSAGE_ID, EsFieldDataType.STRING));
        returnValue.add(new EsFieldDefinition(MiddlewareConstant.MW_MOBILE_NUMBER, EsFieldDataType.LONG));
        returnValue.add(new EsFieldDefinition(MiddlewareConstant.MW_FILE_ID, EsFieldDataType.STRING));
        returnValue.add(new EsFieldDefinition(MiddlewareConstant.MW_BASE_MESSAGE_ID, EsFieldDataType.STRING));
        returnValue.add(new EsFieldDefinition(MiddlewareConstant.MW_MESSAGE_ID, EsFieldDataType.STRING));
        returnValue.add(new EsFieldDefinition(MiddlewareConstant.MW_MSG_RECEIVED_TIME, EsFieldDataType.DATE_AS_LONG, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS.getFormat()));
        returnValue.add(new EsFieldDefinition(MiddlewareConstant.MW_MSG_RECEIVED_DATE, EsFieldDataType.DATE_AS_LONG, DateTimeFormat.DEFAULT_DATE_ONLY.getFormat()));

        return returnValue;
    }

    private static EsFieldDefinition[] getAgingUdpdateFields()
    {
        return new EsFieldDefinition[]
        { //
                new EsFieldDefinition(MiddlewareConstant.MW_DN_CAME_FROM, EsFieldDataType.STRING),//
                new EsFieldDefinition(MiddlewareConstant.MW_AGING_SCHE_TIME, EsFieldDataType.DATE_AS_LONG, DateTimeFormat.DEFAULT_DATE_ONLY.getFormat()), //
        };
    }

    private static EsFieldDefinition[] getAgingInsertFields()
    {
        return new EsFieldDefinition[]
        { //
                new EsFieldDefinition(MiddlewareConstant.MW_CLIENT_ID, EsFieldDataType.STRING),//
                new EsFieldDefinition(MiddlewareConstant.MW_MESSAGE_ID, EsFieldDataType.STRING),//
                new EsFieldDefinition(MiddlewareConstant.MW_MOBILE_NUMBER, EsFieldDataType.LONG),//
                new EsFieldDefinition(MiddlewareConstant.MW_DN_ORI_STATUS_CODE, EsFieldDataType.STRING),//
                new EsFieldDefinition(MiddlewareConstant.MW_ROUTE_ID, EsFieldDataType.STRING),//
                new EsFieldDefinition(MiddlewareConstant.MW_DN_CAME_FROM, EsFieldDataType.STRING),//
                new EsFieldDefinition(MiddlewareConstant.MW_AGING_TYPE, EsFieldDataType.STRING),//
                new EsFieldDefinition(MiddlewareConstant.MW_AGING_SCHE_TIME, EsFieldDataType.DATE_AS_LONG, DateTimeFormat.DEFAULT_DATE_ONLY.getFormat()), //
                new EsFieldDefinition(MiddlewareConstant.MW_AGING_TYPE, EsFieldDataType.STRING),//
                new EsFieldDefinition(MiddlewareConstant.MW_MSG_RECEIVED_TIME, EsFieldDataType.DATE_AS_LONG, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS.getFormat()), //
                new EsFieldDefinition(MiddlewareConstant.MW_MSG_RECEIVED_DATE, EsFieldDataType.DATE_AS_LONG, DateTimeFormat.DEFAULT_DATE_ONLY.getFormat()), //
                new EsFieldDefinition(MiddlewareConstant.MW_FULL_ITEXTO_MESSAGE, EsFieldDataType.STRING),//
                new EsFieldDefinition(MiddlewareConstant.MW_RETRY_ATTEMPT, EsFieldDataType.INTEGER), //
        };
    }

    private static MiddlewareConstant[] getSingleDnKeywords()
    {
        return new MiddlewareConstant[]
        { //
                MiddlewareConstant.MW_CLIENT_ID, //
                MiddlewareConstant.MW_BASE_MESSAGE_ID, //
                MiddlewareConstant.MW_MESSAGE_ID, //
                MiddlewareConstant.MW_MOBILE_NUMBER, //
        };
    }

}