package com.itextos.beacon.platform.voice.util;

import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;

public class VUtil
{

    private static final Log log = LogFactory.getLog(VUtil.class);

    private VUtil()
    {}

    public static Object[] generateDNParams(
            DeliveryObject aDeliveryObject)
            throws Exception
    {
        final Object[] dnParams =
        { URLEncoder.encode(aDeliveryObject.getMessageId(), "US-ASCII"), URLEncoder.encode(aDeliveryObject.getClientId(), "US-ASCII"), URLEncoder.encode(aDeliveryObject.getMobileNumber(), "US-ASCII"),
                URLEncoder.encode(DateTimeUtility.getFormattedDateTime(aDeliveryObject.getMessageReceivedTime(), DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS), "US-ASCII"),
                URLEncoder.encode(DateTimeUtility.getFormattedDateTime(aDeliveryObject.getMessageReceivedDate(), DateTimeFormat.DEFAULT_DATE_ONLY), "US-ASCII")

        };
        return dnParams;
    }

    public static String[] voiceMTRequestParams(
            Map<String, Object> aVoiceAccInfo,
            String aMNumber,
            String aTTSInfo,
            String aCampaignId,
            String aFormattedDnUrl)
            throws Exception
    {
        final String[] resultArray =
        { (String) aVoiceAccInfo.get("V_IP"), aVoiceAccInfo.get("V_PORT").toString(), (String) aVoiceAccInfo.get("V_ACCOUNT"), (String) aVoiceAccInfo.get("V_PIN"), aMNumber,
                URLEncoder.encode(aTTSInfo, "US-ASCII"), aCampaignId, aFormattedDnUrl };

        return resultArray;
    }

    public static String applyParams(
            String aVoicePlatformURL,
            Object[] aParams)
    {
        String urlFormatted = null;

        try
        {
            if (log.isDebugEnabled())
                log.debug("Before Encode - params[7]  " + aParams[7]);
            aParams[7] = URLEncoder.encode(aParams[7].toString(), "US-ASCII");
            if (log.isDebugEnabled())
                log.debug("After Encode - params[7]  " + aParams[7]);
            urlFormatted = MessageFormat.format(aVoicePlatformURL, aParams);
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
        return urlFormatted;
    }

}
