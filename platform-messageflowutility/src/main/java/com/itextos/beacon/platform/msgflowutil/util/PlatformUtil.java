package com.itextos.beacon.platform.msgflowutil.util;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commonpropertyloader.PropertiesPath;
import com.itextos.beacon.commonlib.commonpropertyloader.PropertyLoader;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.Constants;
import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.ErrorObject;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.message.utility.MessageUtil;
import com.itextos.beacon.commonlib.messageprocessor.process.MessageProcessor;
import com.itextos.beacon.commonlib.pattern.PatternCache;
import com.itextos.beacon.commonlib.pattern.PatternCheckCategory;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.Name;
import com.itextos.beacon.inmemory.configvalues.ApplicationConfiguration;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;

public abstract class PlatformUtil
{

    private PlatformUtil()
    {}

    private static final Log                     log               = LogFactory.getLog(PlatformUtil.class);

    private static final PropertiesConfiguration mProps;
    private static String                        mPromoHeaderRegex = ".*";
    private static String                        mTransHeaderRegex = ".*";

    static
    {
        // TODO Need to take these below three values from Config-params
        mProps = PropertyLoader.getInstance().getPropertiesConfiguration(PropertiesPath.COMMON_PROPERTIES, false);

        final String lPromoHeaderRegex = getPropertyConfigValue(Constants.PROMO_HEADER_REGEX);
        if (!lPromoHeaderRegex.isBlank())
            mPromoHeaderRegex = lPromoHeaderRegex;

        final String lTransHeaderRegex = getPropertyConfigValue(Constants.TRANS_HEADER_REGEX);
        if (!lTransHeaderRegex.isBlank())
            mTransHeaderRegex = lTransHeaderRegex;
    }

    public static boolean isHeaderPatternFailed(
            MessageRequest aMessageRequest)
    {
        final String lHeader = MessageUtil.getHeaderId(aMessageRequest);

         aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getFileId()+" Pattern check for Header : " + lHeader + " PromoHeaderRegex : " + mPromoHeaderRegex + " :: TransHeaderRegex : " + mTransHeaderRegex);


        if (aMessageRequest.getMessageType() == MessageType.PROMOTIONAL)
            return PatternCache.getInstance().isPatternMatch(PatternCheckCategory.HEADER_CHECK, mPromoHeaderRegex, lHeader);

        return PatternCache.getInstance().isPatternMatch(PatternCheckCategory.HEADER_CHECK, mTransHeaderRegex, lHeader);
    }

    public static String getPropertyConfigValue(
            String aPropConfigKey)
    {
        return CommonUtility.nullCheck(mProps.getProperty(aPropConfigKey), true);
    }

    public static void sendToErrorLog(
            Component aComponent,
            BaseMessage aBaseMessage,
            Exception aException)
    {

        try
        {
            final ErrorObject errorObject = aBaseMessage.getErrorObject(aComponent, aException);
            MessageProcessor.writeMessage(aComponent, Component.T2DB_ERROR_LOG, errorObject);
        } catch (final ItextosRuntimeException e)
        {
            log.error("Exception while sending the message to error log topic for the component '" + aComponent + "' Message '" + aBaseMessage + "' Error [[[" + CommonUtility.getStackTrace(aException)
                    + "]]]", e);
        }
        catch (final ItextosException e)
        {
            log.error("Exception while sending the message to error log topic for the component '" + aComponent + "' Message '" + aBaseMessage + "' Error [[[" + CommonUtility.getStackTrace(aException)
                    + "]]]", e);
        }
    }

    public static String getAppConfigValueAsString(
            ConfigParamConstants aKey)
    {
        if (aKey == null)
            return null;
        final ApplicationConfiguration lAppConfiguration = (ApplicationConfiguration) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.APPLICATION_CONFIG);
        return lAppConfiguration.getConfigValue(aKey.getKey());
    }

}