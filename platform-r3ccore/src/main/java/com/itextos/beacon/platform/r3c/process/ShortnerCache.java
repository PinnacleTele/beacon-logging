package com.itextos.beacon.platform.r3c.process;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.visualizelink.util.VLUtil;
import com.itextos.beacon.platform.r3c.util.R3CUtil;

public class ShortnerCache
{

    private static final Log log = LogFactory.getLog(ShortnerCache.class);

    private ShortnerCache()
    {}

    public static String getVLSmartlinkId(
            String aClientId,
            String aPayloadUrl,
            int aVLShortner)
    {
        String lSmartLinkId = null;

        if (log.isDebugEnabled())
            log.debug("getVLSmartlinkId() - url_shortner flag value - " + aVLShortner);

        switch (aVLShortner)
        {
            case 0:
            {
                log.info("This case should not occure as check is already is in place.");
                break;
            }

            case 1:
            {
                lSmartLinkId = getSmartLinkIdDummayUrl(aClientId);
                break;
            }

            case 2:
            {
                lSmartLinkId = getSmartLinkIdWithUrlMatch(aClientId, aPayloadUrl);
                break;
            }

            default:
                log.debug("This case should not come, please investigate. Got value of url_shortner as:" + aVLShortner + ", for Client Id:" + aClientId);
        }

        return lSmartLinkId;
    }

    private static String getSmartLinkIdDummayUrl(
            String aClientId)
    {
        if (log.isDebugEnabled())
            log.debug("getSmartLinkIdToDummayUrl() - Process Dummy Url to get the smartlink ids...");
        return VLUtil.getEmptyIncludeUrls(aClientId);
    }

    private static String getSmartLinkIdWithUrlMatch(
            String aClientId,
            String aUrlToConvert)
    {
        final Map<String, String> lIncludeUrls = getVLIncludeInfo(aClientId);

        if (log.isDebugEnabled())
            log.debug("Include Url present for the client : " + aClientId);

        if (lIncludeUrls == null)
            return null;

        String returnValue = null;

        for (final Map.Entry<String, String> lTempEntry : lIncludeUrls.entrySet())
        {
            final String   lSmartLinkIdWithCheck = lTempEntry.getKey();
            final String   lIncludeUrl           = lTempEntry.getValue();
            final String[] lTempSmartLinkId      = lSmartLinkIdWithCheck.split("~");
            final String   lPartial              = lTempSmartLinkId[1];

            if (log.isDebugEnabled())
                log.debug("Smartlink Id is Partial : '" + lTempEntry.getKey() + "' URL : '" + lTempEntry.getValue() + "'");

            if (CommonUtility.isTrue(lPartial))
            {
                if (R3CUtil.isUrlPartialMatch(aUrlToConvert, lIncludeUrl))
                    returnValue = lTempSmartLinkId[0];
            }
            else
                if (R3CUtil.isUrlExactMatch(aUrlToConvert, lIncludeUrl))
                    returnValue = lTempSmartLinkId[0];

            if (returnValue != null)
                break;
        }

        return returnValue;
    }

    private static Map<String, String> getVLIncludeInfo(
            String aClientId)
    {
        Map<String, String> lVLIncludeInfo = null;

        try
        {
            lVLIncludeInfo = VLUtil.getIncludeUrls(aClientId);

            if (log.isDebugEnabled())
                log.debug("ClientId (" + aClientId + ") - " + lVLIncludeInfo);
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the VL Urls for client " + aClientId, e);
        }
        return lVLIncludeInfo;
    }

}