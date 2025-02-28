package com.itextos.beacon.platform.carrierhandoverutility.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.regex.Pattern;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commonpropertyloader.PropertiesPath;
import com.itextos.beacon.commonlib.commonpropertyloader.PropertyLoader;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.Constants;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.MsgRetry;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.message.SubmissionObject;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.configvalues.ApplicationConfiguration;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;

public class CHUtil
{

    private static final Log     log          = LogFactory.getLog(CHUtil.class);

    private static final String  PERCENTAGE   = "%";
    private static final Pattern HEXA_PATTERN = Pattern.compile("^[0-9A-Fa-f]+$");

    private CHUtil()
    {}

    public static boolean isValidHexMessage(
            String aHexMessage)
    {

        if (!aHexMessage.isBlank())
        {
            if (aHexMessage.indexOf(" ") != -1)
                return false;
            if (HEXA_PATTERN.matcher(aHexMessage).matches())
                return (aHexMessage.length() % 2) == 0;
            return false;
        }
        return true;
    }

    public static boolean isValidUDH(
            String aUdh)
    {
        final String lUDHHeader = aUdh.substring(2, aUdh.length());
        final int    lUDHLength = Integer.parseInt(aUdh.substring(0, 2), 16);
        return lUDHHeader.length() == (lUDHLength * 2);
    }

    public static String addKannelSpecCharToHex(
            String aHexString)
            throws KannelPercentagePrefixException
    {
        final StringBuilder lTempValue = new StringBuilder();

        try
        {

            for (int i = 0; i < aHexString.length(); i = i + 2)
            {
                lTempValue.append(PERCENTAGE);
                lTempValue.append(aHexString.substring(i, i + 2));
            }
        }
        catch (final Exception e)
        {
            throw new KannelPercentagePrefixException(e);
        }

        return lTempValue.toString();
    }

    public static String generateCallBackUrl(
            String aDnIpInfo,
            String aCallBackParams)
    {

        try
        {
            if (log.isInfoEnabled())
                log.info("Attempting Genrate the DN Call back Url ...........");

            final String lDnUrlTemplate = getAppConfigValueAsString(ConfigParamConstants.DLR_URL_TEMPLATE);
            final String lDnIPInfo[]    = aDnIpInfo.split(":");
            final String formattedUrl   = MessageFormat.format(lDnUrlTemplate, lDnIPInfo[0], lDnIPInfo[1], aCallBackParams);
            if (log.isInfoEnabled())
                log.info("encoded URL===>" + formattedUrl);
            return formattedUrl;
        }
        catch (final Exception e)
        {
            log.error("problem framing url generateCallBackUrl()...", e);
        }
        return null;
    }

    public static String getAppConfigValueAsString(
            ConfigParamConstants aConfigParamConstant)
    {
        final ApplicationConfiguration lAppConfiguration = (ApplicationConfiguration) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.APPLICATION_CONFIG);
        return lAppConfiguration.getConfigValue(aConfigParamConstant.getKey());
    }

    public static String getMessage(
            SubmissionObject aSubmissionObject)
            throws UnsupportedEncodingException
    {
        final boolean isHexMsg = aSubmissionObject.isHexMessage();
        if (isHexMsg)
            try
            {
                String    lMessage = aSubmissionObject.getMessage();
                final int lUdhi    = aSubmissionObject.getUdhi();

                if (lUdhi == 1)
                {
                    final String  lUdhLength = lMessage.substring(0, 2);
                    final Integer lIndex     = Integer.parseInt(lUdhLength, 16);
                    final int     lUdhLen    = (lIndex.intValue() * 2) + 2;
                    lMessage = lMessage.substring(lUdhLen);
                }

                return CHUtil.addKannelSpecCharToHex(lMessage);
            }
            catch (final Exception ignore)
            {}
        else
            if (aSubmissionObject.getAlterMessage() != null)
                return URLEncoder.encode(aSubmissionObject.getAlterMessage(), Constants.ENCODER_FORMAT);
            else
                if (aSubmissionObject.getMessage() != null)
                    return URLEncoder.encode(aSubmissionObject.getMessage(), Constants.ENCODER_FORMAT);
        return "";
    }

    public static boolean isExpired(
            MessageRequest aMessageRequest)
    {
        final Date lScheduleTime = aMessageRequest.getScheduleDateTime();

        if (lScheduleTime != null)
            return false;

        // The ValidityPeriod is greater then GlobalMaxValidityPeriod then we are
        // resetting
        final int lGlobalMaxMsgExpiredInSec = CommonUtility.getInteger(getAppConfigValueAsString(ConfigParamConstants.GLOBAL_MSG_MAX_VALIDITY_IN_SEC), -1);

        if (log.isDebugEnabled())
            log.debug("GlobalMaxMsgExpiredInSec :" + lGlobalMaxMsgExpiredInSec);

        if (lGlobalMaxMsgExpiredInSec != -1)
        {
            final int lRecValidityPeriod = aMessageRequest.getMaxValidityInSec();

            if (log.isDebugEnabled())
                log.debug("Received Validity Period :" + lRecValidityPeriod);

            if (lRecValidityPeriod > lGlobalMaxMsgExpiredInSec)
                aMessageRequest.setMaxValidityInSec(lGlobalMaxMsgExpiredInSec);
        }

        final int lFinalValidityPeriod = aMessageRequest.getMaxValidityInSec();
        if (log.isDebugEnabled())
            log.debug("Final Validity Period :" + lFinalValidityPeriod);

        final long lExpiry            = lFinalValidityPeriod * 1000L;

        final long lFirstReceivedTime = aMessageRequest.getFirstReceivedTime().getTime();
        final long lTimedifference    = System.currentTimeMillis() - lFirstReceivedTime;

        return lTimedifference > lExpiry;
    }

    public static String getCallBackParams(
            SubmissionObject aSubmissionObject)
    {

        try
        {
            final PropertiesConfiguration lProps    = PropertyLoader.getInstance().getPropertiesConfiguration(PropertiesPath.DN_CALLBACK_PARAMS_PROPERTIES, false);
            final Iterator<String>        lIterator = lProps.getKeys();
            return aSubmissionObject.getOperatorJson(lIterator);
        }
        catch (final Exception exp)
        {
            log.error("Problem framing call back map for smpp...", exp);
        }

        return null;
    }

    public static String getPriority(
            MessageRequest aMessageRequest) // TODO Hope this is not equal.
    {
        int lIntSMSPriority = Integer.parseInt(aMessageRequest.getMessagePriority().getKey());
        if (lIntSMSPriority < 3)
            lIntSMSPriority = 3;
        else
            if (lIntSMSPriority == 4)
                lIntSMSPriority = 2;
            else
                lIntSMSPriority = 1;

        return "" + lIntSMSPriority;
    }

    public static boolean canMsgRetry(
            MessageRequest aMessageRequest)
    {
        final String lMsgRetryAvailable = CommonUtility.nullCheck(aMessageRequest.getValue(MiddlewareConstant.MW_MSG_RETRY_ENABLED), true);

        if (log.isDebugEnabled())
            log.debug("Message Retry Enabled :'" + lMsgRetryAvailable + "'");

        MsgRetry lMsgRetry = MsgRetry.getMsgRetry(lMsgRetryAvailable);

        boolean  canRetry  = false;

        if (lMsgRetry == null)
            lMsgRetry = MsgRetry.SINGLE_PART_RETRY;

        if (log.isDebugEnabled())
            log.debug("Final Message Retry Enabled :'" + lMsgRetry + "'");

        switch (lMsgRetry)
        {
            case SINGLE_PART_RETRY:
                canRetry = (aMessageRequest.getMessageTotalParts() <= 1) ? true : false;
                break;

            case SINGLE_AND_MULTIPART_PART_RETRY:
                canRetry = ((aMessageRequest.getMessageTotalParts() <= 1) || (aMessageRequest.getMessageTotalParts() > 1)) ? true : false;
                break;

            case PARTIAL_RETRY:
                canRetry = true;
                break;

            case NO_RETRY:
                canRetry = false;
                break;

            default:
                canRetry = false;
                break;
        }

        return canRetry;
    }

}
