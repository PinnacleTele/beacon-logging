package com.itextos.beacon.platform.elasticsearchutil.utility;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.json.simple.JSONObject;

import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.platform.elasticsearchutil.data.R3Info;
import com.itextos.beacon.platform.elasticsearchutil.types.EsConstant;
import com.itextos.beacon.platform.elasticsearchutil.types.EsCreateTimeStamp;
import com.itextos.beacon.platform.elasticsearchutil.types.EsFieldDataType;
import com.itextos.beacon.platform.elasticsearchutil.types.EsTypeParent;

public class EsUtility
{

    private static final Log    log           = LogFactory.getLog(EsUtility.class);

    private static final String SINGLE_DN_ILM = "single_dn_ilm";

    private EsUtility()
    {}

    public static Map<MiddlewareConstant, String> getGenericResponseReader(
            EsFieldDefinition[] aEsFieldDefinition,
            Map<String, Object> aSource)
    {
        final Map<MiddlewareConstant, String> returnValue = new EnumMap<>(MiddlewareConstant.class);

        if (log.isTraceEnabled())
            log.trace("Response from ES : '" + aSource + "'");

        if ((aSource != null) && !aSource.isEmpty())
            for (final EsFieldDefinition fieldDefinition : aEsFieldDefinition)
            {
                final MiddlewareConstant middleConst = fieldDefinition.getFieldName();
                String                   value       = null;

                if (fieldDefinition.getDataType() == EsFieldDataType.DATE_AS_LONG)
                    value = getDateFromString(aSource, middleConst, fieldDefinition);
                else
                    value = CommonUtility.nullCheck(aSource.get(middleConst.getName()));

                returnValue.put(middleConst, value);
            }

        return returnValue;
    }

    public static String getDateFromString(
            Map<String, Object> aSource,
            MiddlewareConstant aMc,
            EsFieldDefinition aFieldDefinition)
    {
        final long longDate  = CommonUtility.getLong(CommonUtility.nullCheck(aSource.get(aMc.getName())));
        Date       dateValue = null;

        if (longDate > 0)
            dateValue = new Date(longDate);

        if (dateValue == null)
            return null;

        return getStringFromDate(aFieldDefinition.getDateFormat(), dateValue);
    }

    public static Map<String, Object> getMappingInfo(
            EsTypeParent aEsType)
    {
        if (log.isDebugEnabled())
            log.debug("Es Type : '" + aEsType + "'");

        String[] keyWords = null;

        if (aEsType == EsTypeParent.R3)
            keyWords = EsConstant.VL_KEYWORDS;
        else
        {
            final MiddlewareConstant[] keywords = getKeyWords(aEsType);

            if (keywords != null)
            {
                keyWords = new String[keywords.length];
                int count = 0;
                for (final MiddlewareConstant mc : keywords)
                    keyWords[count++] = mc.getName();
            }
        }

        if (log.isDebugEnabled())
            log.debug("Es Index Keywords " + Arrays.asList(keyWords));

        final Map<String, Object> properties = new HashMap<>();
        final Map<String, Object> mapping    = new HashMap<>();
        mapping.put("properties", properties);

        if (keyWords != null)
            for (final String s : keyWords)
            {
                final Map<String, Object> typeDefinition = new HashMap<>();
                typeDefinition.put("type", "keyword");
                properties.put(s, typeDefinition);
            }

        if (log.isDebugEnabled())
            log.debug("Es Index return mappings " + mapping);

        return mapping;
    }

    public static MiddlewareConstant[] getKeyWords(
            EsTypeParent aIndexType)
    {

        switch (aIndexType)
        {
            case AGING:
                return EsConstant.AGING_KEYWORDS;

            case DLR_QUERY:
                return EsConstant.DNQUERY_KEYWORDS;

            case SINGLE_DN:
                return EsConstant.SINGLE_DN_KEYWORDS;

            case R3:
            default:
                break;
        }
        return null;
    }

    public static IndexRequest getR3InsertRequest(
            String aEsIndexName,
            R3Info aR3Info)
    {
        final String       id           = aR3Info.getShortCode();
        final IndexRequest indexRequest = new IndexRequest(aEsIndexName);
        indexRequest.id(id);

        final String commonJson = getJsonContent(aR3Info, EsCreateTimeStamp.R3_CTIME);
        indexRequest.source(commonJson, XContentType.JSON);
        return indexRequest;
    }

    public static IndexRequest getSingleDnInsertRequest(
            String aEsIndexName,
            BaseMessage aMessage)
    {
        final String       messageId    = aMessage.getValue(MiddlewareConstant.MW_MESSAGE_ID);
        final IndexRequest indexRequest = new IndexRequest(aEsIndexName);
        indexRequest.id(messageId);

        final String commonJson = getJsonContent(aMessage, EsConstant.SINGLE_DN_INSERT_FIELDS, EsCreateTimeStamp.SINGLE_DN_CTIME);
        indexRequest.source(commonJson, XContentType.JSON);
        return indexRequest;
    }

    public static String getStringFromDate(
            String aDateFormat,
            Date aDate)
    {
        if (aDate == null)
            return null;
        final DateTimeFormat dateTimeFormat = DateTimeFormat.getDateTimeFormat(aDateFormat);
        return (dateTimeFormat != null) ? DateTimeUtility.getFormattedDateTime(aDate, dateTimeFormat) : DateTimeUtility.getFormattedDateTime(aDate, aDateFormat);
    }

    public static Date getDate(
            MiddlewareConstant aFieldName,
            String aDateFormat,
            String aDateString)
    {
        if (CommonUtility.nullCheck(aDateString, true).isEmpty())
            return null;
        final DateTimeFormat dateTimeFormat = DateTimeFormat.getDateTimeFormat(aDateFormat);
        final Date           parsedDate     = (dateTimeFormat != null) ? DateTimeUtility.getDateFromString(aDateString, dateTimeFormat) : DateTimeUtility.getDateFromString(aDateString, aDateFormat);

        if (parsedDate == null)
            log.error("Unable to get a valid date for '" + aDateString + "' in the format '" + aDateFormat + "' for field '" + aFieldName + "'");

        return parsedDate;
    }

    public static String getEsIndexName(
            EsTypeParent aSearchType)
    {
        final String returnValue = null;

        switch (aSearchType)
        {
            case AGING:
                return EsTypeParent.AGING.toString().toLowerCase();

            case DLR_QUERY:
                return EsTypeParent.DLR_QUERY.toString().toLowerCase();

            case SINGLE_DN:
                return SINGLE_DN_ILM;

            case R3:
                return EsTypeParent.R3.toString().toLowerCase();

            default:
                break;
        }
        return returnValue;
    }

    public static String getJsonContent(
            BaseMessage aMessage,
            EsFieldDefinition[] aMwConstants,
            EsCreateTimeStamp aCreateTimestamp)
    {

        try
        {
            final JSONObject jsonObj = new JSONObject();

            for (final EsFieldDefinition fieldDefinition : aMwConstants)
            {
                final MiddlewareConstant lFieldName  = fieldDefinition.getFieldName();
                final String             key         = lFieldName.getName();
                final String             lFieldValue = aMessage.getValue(lFieldName);

                switch (fieldDefinition.getDataType())
                {
                    case DATE_AS_LONG:
                        final Date d = getDate(lFieldName, fieldDefinition.getDateFormat(), lFieldValue);
                        if (d != null)
                            jsonObj.put(key, d.getTime());
                        break;

                    case DOUBLE:
                        jsonObj.put(key, CommonUtility.getDouble(lFieldValue));
                        break;

                    case FLOAT:
                        jsonObj.put(key, CommonUtility.getFloat(lFieldValue));
                        break;

                    case INTEGER:
                        jsonObj.put(key, CommonUtility.getInteger(lFieldValue));
                        break;

                    case LONG:
                        jsonObj.put(key, CommonUtility.getLong(lFieldValue));
                        break;

                    case STRING:
                    default:
                        jsonObj.put(key, CommonUtility.nullCheck(lFieldValue, true));
                        break;
                }
            }

            if (aCreateTimestamp != EsCreateTimeStamp.NONE)
                jsonObj.put(aCreateTimestamp.getKey(), DateTimeUtility.getFormattedCurrentDateTime(DateTimeFormat.DEFAULT));

            final String json = jsonObj.toJSONString();

            if (log.isDebugEnabled())
                log.debug("Generated JSON: '" + json + "'");

            return json;
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the content for the Elasticsearch " + aMessage, e);
        }
        return null;
    }

    private static String getJsonContent(
            R3Info aR3Info,
            EsCreateTimeStamp aCreateTimestamp)
    {

        try (
                XContentBuilder contentBuilder = XContentFactory.jsonBuilder();)
        {
            contentBuilder.startObject();

            contentBuilder.field(R3Info.ID, CommonUtility.nullCheck(aR3Info.getShortCode(), true));
            contentBuilder.field(R3Info.FILE_ID, CommonUtility.nullCheck(aR3Info.getFileId(), true));
            contentBuilder.field(R3Info.URL, aR3Info.getUrl());
            contentBuilder.field(R3Info.CREATE_TIME, aR3Info.getCreatedTs().getTime());
            contentBuilder.field(R3Info.MESSAGE_ID, CommonUtility.nullCheck(aR3Info.getMessageId(), true));
            contentBuilder.field(R3Info.CLIENT_ID, CommonUtility.nullCheck(aR3Info.getClientId(), true));
            contentBuilder.field(R3Info.MOBILE_NUMBER, CommonUtility.nullCheck(aR3Info.getMobileNumber(), true));
            contentBuilder.field(R3Info.SMART_LINK_ID, CommonUtility.nullCheck(aR3Info.getSmartLinkId(), true));
            contentBuilder.field(R3Info.SHORTEN_URL, CommonUtility.nullCheck(aR3Info.getShortenUrl(), true));
            contentBuilder.field(R3Info.SHORTEN_URL, CommonUtility.nullCheck(aR3Info.getShortenUrl(), true));
            contentBuilder.field(R3Info.EXPIRY_DATE, aR3Info.getExpiryDate().getTime());

            final Map<String, String> lAdditionalInfo = aR3Info.getAdditionalInfo();

            if ((lAdditionalInfo != null) && !lAdditionalInfo.isEmpty())
                for (final Entry<String, String> entry : lAdditionalInfo.entrySet())
                    contentBuilder.field(entry.getKey(), entry.getValue());

            if (aCreateTimestamp != EsCreateTimeStamp.NONE)
                contentBuilder.field(aCreateTimestamp.getKey(), DateTimeUtility.getFormattedCurrentDateTime(DateTimeFormat.DEFAULT));

            contentBuilder.endObject();

            final String json = Strings.toString(contentBuilder);

            if (log.isDebugEnabled())
                log.debug("Generated JSON: '" + json + "'");

            return json;
        }
        catch (final IOException e)
        {
            log.error("Exception while getting the content for the Elasticsearch " + aR3Info, e);
        }
        return null;
    }

}