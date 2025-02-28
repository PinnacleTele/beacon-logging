package com.itextos.beacon.httpclienthandover.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.httpclient.HttpResult;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.httpclienthandover.clientinfo.HttpCustomerInfoCollection;
import com.itextos.beacon.httpclienthandover.data.ClientHandoverData;
import com.itextos.beacon.httpclienthandover.data.ClientHandoverMaster;
import com.itextos.beacon.httpclienthandover.data.ClientHandoverParams;
import com.itextos.beacon.httpclienthandover.data.HttpMethod;
import com.itextos.beacon.httpclienthandover.data.ParamDataType;
import com.itextos.beacon.httpclienthandover.drools.validator.DroolsValidator;
import com.itextos.beacon.httpclienthandover.validator.DataValidatorImpl;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;

public class ClientHandoverUtils
{

    private static final Log           log                  = LogFactory.getLog(ClientHandoverUtils.class);

    private static final int           RETRY_DISABLED       = 0;
    private static final int           TIME_BASED           = 1;
    private static final int           COUNT_BASED          = 2;
    private static final int           TIME_AND_COUNT_BASED = 3;
    private static final Pattern       HEXADECIMAL_PATTERN  = Pattern.compile("\\p{XDigit}+");
    private static final String        DEFAULT_ENCODING     = StandardCharsets.UTF_8.name();
    private static final List<Integer> HTTP_STATUS_SUCCESS  = new ArrayList<>();

    static
    {
        HTTP_STATUS_SUCCESS.add(200);
        HTTP_STATUS_SUCCESS.add(201);
        HTTP_STATUS_SUCCESS.add(202);
    }

    private ClientHandoverUtils()
    {}

    public static boolean checkIfEncodingRequired(
            ClientHandoverMaster aEndPointInfo)
    {
        final HttpMethod urlMethod = aEndPointInfo.getHttpMethod();

        return urlMethod == HttpMethod.GET;
    }

    public static boolean isReryEnabled(
            ClientHandoverData clientConfiguration)
    {
        final var retryLogic = clientConfiguration.getRetryExpiryLogic();
        return retryLogic > 0;
    }

    public static String getValueFromTheConstant(
            ClientHandoverParams handoverParams,
            BaseMessage aMessage)
    {
        final ParamDataType      paramDataType            = handoverParams.getDataType();
        final String             dataFormat               = handoverParams.getDataFormat();
        final String             dataValidation           = handoverParams.getDataValidation();
        final String             droolsValidationFilePath = handoverParams.getDroolsValidationFilePath();
        final String             constantName             = handoverParams.getMwConstantName();

        final MiddlewareConstant constant                 = MiddlewareConstant.getMiddlewareConstantByName(constantName);
        final String             value                    = aMessage.getValue(constant);

        boolean                  isValidated              = false;

        if (!"".equals(dataValidation))
            isValidated = DataValidatorImpl.getInstance().validate(paramDataType, dataValidation, value, dataFormat);
        else
            if (!"".equals(droolsValidationFilePath))
                isValidated = DroolsValidator.getInstance().validate(droolsValidationFilePath, value);

        return getParsedValue(isValidated, value, aMessage, handoverParams);
    }

    private static String getParsedValue(
            boolean aIsValidated,
            String aValue,
            BaseMessage aMessage,
            ClientHandoverParams aHandoverParams)
    {
        String finalValue = aValue;

        if (aIsValidated)
        {
            final MiddlewareConstant alternativeConstantKeyName = MiddlewareConstant.getMiddlewareConstantByName(aHandoverParams.getMwAlternativeConstantName());

            if (alternativeConstantKeyName == null)
            {
                if (!"".equals(aHandoverParams.getDefaultValue()))
                    finalValue = aHandoverParams.getDefaultValue();
            }
            else
                finalValue = aMessage.getValue(alternativeConstantKeyName);
        }

        return "".equals(aHandoverParams.getDataFormat()) ? finalValue : getFormattedValue(finalValue, aHandoverParams.getDataType(), aHandoverParams.getDataFormat());
    }

    private static String getFormattedValue(
            String aFinalValue,
            ParamDataType aParamDataType,
            String aDataFormat)
    {
        if (StringUtils.isEmpty(aFinalValue))
            return aFinalValue;

        try
        {

            switch (aParamDataType)
            {
                case NUMBER:
                    final DecimalFormat format = new DecimalFormat(aDataFormat);
                    return format.format(Double.valueOf(aFinalValue));

                case STRING:
                    return String.format(aDataFormat, aFinalValue);

                case DATE_TIME:
                    // TOOD need to ad one more column to parse
                    Date formattedDate = DateTimeUtility.getDateFromString(aFinalValue, DateTimeFormat.DEFAULT);
                    if (formattedDate == null)
                        formattedDate = DateTimeUtility.getDateFromString(aFinalValue, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS);
                    final String formattedDateString = DateTimeUtility.getFormattedDateTime(formattedDate, aDataFormat);
                    if (formattedDateString == null)
                        return aFinalValue;
                    return formattedDateString;

                default:
                    return aFinalValue;
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while formatting the data | Final Value : " + aFinalValue + " | DataFormat : " + aDataFormat, e);
        }
        return aFinalValue;
    }

    public static StringBuilder getTemPlateBuilder(
            ClientHandoverMaster aCustomerEndPoint)
    {
        final String template = CommonUtility.nullCheck(aCustomerEndPoint.getHandoverTemplate(), true);
        return new StringBuilder(template);
    }

    public static String convertHexToString(
            String aHexString)
    {
        String result = "";

        if (aHexString == null)
            return result;

        try
        {
            final byte[] bytes = Hex.decodeHex(aHexString.toCharArray());
            result = new String(bytes, StandardCharsets.UTF_16);
        }
        catch (final Exception e)
        {
            throw new IllegalArgumentException("Invalid Hex format!");
        }
        return result;
    }

    public static boolean isHexadecimal(
            String input)
    {
        final Matcher matcher = HEXADECIMAL_PATTERN.matcher(input);
        return matcher.matches();
    }

    public static String getCompleteURL(
            String aUrl,
            String toAppend)
    {
        String url = aUrl;

        if (!url.endsWith("?"))
            url = url + "?";
        url = url + toAppend;
        return url;
    }

    public static StringBuilder getReplacedStringBuffer(
            StringBuilder stringToReplace,
            String checkString,
            String recordValue,
            boolean aEncodeRequired)
            throws UnsupportedEncodingException
    {
        final int checkStringindex = stringToReplace.indexOf(checkString);

        if (checkStringindex > -1)
        {
            if (aEncodeRequired)
                recordValue = URLEncoder.encode(recordValue, DEFAULT_ENCODING);

            if(checkString.equals("{7}")) {
            	
            	 if (!"".equals(CommonUtility.nullCheck(recordValue, true)))
                     stringToReplace.replace(checkStringindex, checkStringindex + checkString.length(), recordValue.toLowerCase());
                 else
                     stringToReplace.replace(checkStringindex, checkStringindex + checkString.length(), "");
         
            	 
            }else {
    
            	 if (!"".equals(CommonUtility.nullCheck(recordValue, true)))
                     stringToReplace.replace(checkStringindex, checkStringindex + checkString.length(), recordValue);
                 else
                     stringToReplace.replace(checkStringindex, checkStringindex + checkString.length(), "");
         
            	 
            }
        }
        return stringToReplace;
    }

    public static boolean checkIfExpired(
            BaseMessage message,
            ClientHandoverData clientConfiguration,
            boolean aOnlyLog)
    {
        final int retryLogic = clientConfiguration.getRetryExpiryLogic();
        final int expiryTime = clientConfiguration.getExpiryTimeSeconds();

        if (log.isDebugEnabled())
            log.debug("Retry Logic: '" + retryLogic + "' | ExpiryTime: '" + expiryTime + "' | Client ID: '" + clientConfiguration.getClientId() + "'");

        switch (retryLogic)
        {
            case TIME_BASED:
                return checkTimeBasedExpiry(expiryTime, message, aOnlyLog, false);

            case COUNT_BASED:
                return checkCountBasedExpiry(message, clientConfiguration, aOnlyLog);

            case TIME_AND_COUNT_BASED:
                return checkTimeAndCountBasedExpiry(expiryTime, message, clientConfiguration, aOnlyLog);

            case RETRY_DISABLED:
            default:
                return true;
        }
    }

    private static boolean checkCountBasedExpiry(
            BaseMessage message,
            ClientHandoverData aClientConfiguration,
            boolean aOnlyLog)
    {
        final int maxRetryCount  = aClientConfiguration.getMaxRetryCount();
        int       attemptedCount = CommonUtility.getInteger(message.getValue(MiddlewareConstant.MW_CLIENT_HANDOVER_MAX_RETRY_COUNT));

        if (aOnlyLog)
            attemptedCount += 1;
        message.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_MAX_RETRY_COUNT, "" + attemptedCount);

        return attemptedCount >= maxRetryCount;
    }

    private static boolean checkTimeAndCountBasedExpiry(
            int aExpiryTime,
            BaseMessage aMessage,
            ClientHandoverData aClientConfiguration,
            boolean aOnlyLog)
    {
        final boolean isCountExpired = checkCountBasedExpiry(aMessage, aClientConfiguration, aOnlyLog);

        if (isCountExpired)
            return isCountExpired;

        return checkTimeBasedExpiry(aExpiryTime, aMessage, aOnlyLog, true);
    }

    private static boolean checkTimeBasedExpiry(
            int aExpiryTime,
            BaseMessage message,
            boolean aOnlyLog,
            boolean both)
    {
        int attemptedCount = CommonUtility.getInteger(message.getValue(MiddlewareConstant.MW_CLIENT_HANDOVER_MAX_RETRY_COUNT));

        if (aOnlyLog && !both)
            attemptedCount += 1;
        message.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_MAX_RETRY_COUNT, "" + attemptedCount);

        long chTime = CommonUtility.getLong(message.getValue(MiddlewareConstant.MW_CLIENT_HANDOVER_INITIAL_TIME));

        if (log.isDebugEnabled())
            log.debug("chTime: '" + chTime + "'");

        if (chTime == 0)
        {
            chTime = System.currentTimeMillis();
            message.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_INITIAL_TIME, "" + chTime);
        }

        final long diff            = System.currentTimeMillis() - chTime;
        final long retryMaxTimeInt = (long) aExpiryTime * 1000;

        if (log.isDebugEnabled())
            log.debug("Diff between current time and recorded time : '" + diff + "' | Retry Max Milliseconds: '" + retryMaxTimeInt + "'");
        return diff > retryMaxTimeInt;
    }

    public static HttpResult getCustomResult(
            String aErrorString,
            int aStatusCode)
    {
        final HttpResult result = new HttpResult();
        result.setSuccess(false);
        result.setErrorString(aErrorString);
        result.setException(new ItextosException(aErrorString));
        result.setResponseString(aErrorString);
        result.setStatusCode(aStatusCode);
        return result;
    }

    public static void setResultInMessage(
            BaseMessage aMessage,
            HttpResult httpResult)
    {
        aMessage.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_HTTP_STATUS_CODE, "" + httpResult.getStatusCode());
        aMessage.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_HTTP_RESPONSE_CONTENT, httpResult.getResponseString());
        aMessage.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_CLIENT_URL, "NO URL CONFIGURED");
    }

    public static String getIntegerWithInc(
            BaseMessage BaseMessage,
            MiddlewareConstant key,
            String startWith)
    {
        final String value = CommonUtility.nullCheck(BaseMessage.getValue(key), true);

        return "".equals(value) ? startWith : String.valueOf(Integer.parseInt(value) + 1);
    }

    public static ClientHandoverData getClientHandoverData(
            String aCustomerId)
    {
        final HttpCustomerInfoCollection clientConfigurationInfo = (HttpCustomerInfoCollection) InmemoryLoaderCollection.getInstance()
                .getInmemoryCollection(InmemoryId.HTTPCLIENTHANDOVER_CUSTOMER_INFO);
        return clientConfigurationInfo.getClientHandoverInfo(aCustomerId);
    }

    public static boolean isHttpProcessSuccess(
            HttpResult aHttpResult)
    {
        return HTTP_STATUS_SUCCESS.contains(aHttpResult.getStatusCode());
    }

}
