package com.itextos.beacon.commonlib.stringprocessor.process;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.stringprocessor.dto.ParamDataType;
import com.itextos.beacon.commonlib.stringprocessor.dto.ValidatorMaster;
import com.itextos.beacon.commonlib.stringprocessor.dto.ValidatorParams;
import com.itextos.beacon.commonlib.stringprocessor.validator.DataValidatorImpl;
import com.itextos.beacon.commonlib.stringprocessor.validator.drools.DroolsValidator;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;

public class ValidatorUtil
{

    private static final Log    log              = LogFactory.getLog(ValidatorUtil.class);
    private static final String DEFAULT_ENCODING = StandardCharsets.UTF_8.name();

    private ValidatorUtil()
    {}

    public static StringBuilder getTemplateBuilder(
            ValidatorMaster aCustomerEndPoint)
    {
        final String template = CommonUtility.nullCheck(aCustomerEndPoint.getBodyTemplate(), true);
        return new StringBuilder(template);
    }

    public static String getValueFromTheConstant(
            ValidatorParams handoverParams,
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
            ValidatorParams aHandoverParams)
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

            if (!"".equals(CommonUtility.nullCheck(recordValue, true)))
                stringToReplace.replace(checkStringindex, checkStringindex + checkString.length(), recordValue);
            else
                stringToReplace.replace(checkStringindex, checkStringindex + checkString.length(), "");
        }
        return stringToReplace;
    }

}