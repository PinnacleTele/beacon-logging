package com.itextos.beacon.platform.r3c.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.MessageConvertionUtility;
import com.itextos.beacon.inmemory.visualizelink.util.VLUtil;

public class SmartlinkIdReplacer
{

    private static final Log     log       = LogFactory.getLog(SmartlinkIdReplacer.class);

    private final MessageRequest mMessageRequest;
    boolean                      isUnicode = false;
    String                       mMessage;

    public SmartlinkIdReplacer(
            MessageRequest aMessageRequest)
    {
        mMessageRequest = aMessageRequest;
        checkMessage();
    }

    private void checkMessage()
    {
        mMessage = CommonUtility.nullCheck(mMessageRequest.getLongMessage());
        final boolean lIsHexMsg = mMessageRequest.isHexMessage();

        if (log.isDebugEnabled())
            log.debug("Is Hex Message : " + lIsHexMsg);

        if (lIsHexMsg)
        {

            if (!mMessage.contains(R3CUtil.URL_TRACK_PATTERN_PREFIX) && !mMessage.contains(R3CUtil.URL_TRACK_PATTERN_SUFFIX))
            {
                mMessage = MessageConvertionUtility.convertHex2String(mMessage);

                if (log.isDebugEnabled())
                    log.debug("Converted Hex2String message -> " + mMessage);
            }

            isUnicode = true;
        }
    }

    public boolean process()
    {
        if (log.isDebugEnabled())
            log.debug("--------- Executing smartlink process ----------- ");

        try
        {
            final List<String> smartLinkIds = getSmartLinkIds();

            if (smartLinkIds.isEmpty())
            {
                if (log.isDebugEnabled())
                    log.debug("No Smartlink id specified in the message.");
                return true;
            }

            final Map<String, String> shortUrls = getShortCodeUrls(smartLinkIds);

            replaceShortUrlInMessage(shortUrls);
        }
        catch (final ItextosException e)
        {
            log.error("Exception while processing for the short code.", e);
            return false;
        }
        finally
        {
            replaceTheMessage();
        }
        return true;
    }

    private void replaceShortUrlInMessage(
            Map<String, String> aShortUrls)
    {

        for (final Entry<String, String> entry : aShortUrls.entrySet())
        {
            final String toLookUp     = R3CUtil.URL_TRACK_PATTERN_PREFIX + "VL:" + entry.getKey() + R3CUtil.URL_TRACK_PATTERN_SUFFIX;
            final String urlToReplace = entry.getValue();
            mMessage = mMessage.replace(toLookUp, urlToReplace);
        }
    }

    private Map<String, String> getShortCodeUrls(
            List<String> aSmartLinkIds)
            throws ItextosException
    {
        final Map<String, String> returnValue = new LinkedHashMap<>();
        final String              lClientId   = mMessageRequest.getClientId();

        for (final String smartLinkId : aSmartLinkIds)
        {
            String         lSmartLinkId      = "";
            final String[] lTempSmartLinkIds = smartLinkId.split(":");

            if ((lTempSmartLinkIds != null) && (lTempSmartLinkIds.length > 1))
                lSmartLinkId = CommonUtility.nullCheck(lTempSmartLinkIds[1], true);

            if (lSmartLinkId.isEmpty())
                throw new ItextosException("Invalid Smartlink id " + lSmartLinkId + " and Client Id:" + lClientId);

            final String lRedirectAndDomainUrl = VLUtil.getVLInfo(CommonUtility.combine(lClientId, lSmartLinkId));

            if (log.isDebugEnabled())
                log.debug("smartlinkid " + lSmartLinkId + " Client Id:" + lClientId + " RedirectAndDomainUrl - " + lRedirectAndDomainUrl);

            if (lRedirectAndDomainUrl == null)
                throw new ItextosException("Redirect and Domain Url not found for Smartlink id " + lSmartLinkId + " and Client Id:" + lClientId);

            updateUrl(returnValue, lSmartLinkId, lRedirectAndDomainUrl);
        }
        return returnValue;
    }

    private void updateUrl(
            Map<String, String> aReturnValue,
            String aSmartLinkId,
            String aRedirectAndDomainUrl)
            throws ItextosException
    {
        final String[] lTemp = aRedirectAndDomainUrl.split("~");
        if (log.isDebugEnabled())
            log.debug("lTempVal length : " + lTemp.length);

        String       lDomainUrl   = "";
        final String lRedirectUrl = CommonUtility.nullCheck(lTemp[0], true);
        if (lTemp.length > 1)
            lDomainUrl = CommonUtility.nullCheck(lTemp[1], true);

        if ((mMessageRequest.getVlShortner() == 0) || !R3CUtil.isItextosDomain(lDomainUrl))
        {
            if (log.isDebugEnabled())
                log.debug("Calling to process Redirect Url...");

            if (mMessageRequest.getVlShortner() == 0 || lDomainUrl.isEmpty())
            {
                if (lRedirectUrl.isEmpty())
                    throw new ItextosException("Unable to process the message for Shorten URL. Redirect URL is empty");
                aReturnValue.put(aSmartLinkId, lRedirectUrl);
            }
            else
                aReturnValue.put(aSmartLinkId, lDomainUrl);
        }
        else
        {
            final String newUrl = getShortCodeForUrl(aSmartLinkId, lRedirectUrl, lDomainUrl);
            if (newUrl != null)
                aReturnValue.put(aSmartLinkId, newUrl);
            else
                aReturnValue.put(aSmartLinkId, lRedirectUrl);
        }
    }

    private String getShortCodeForUrl(
            String aSmartLinkId,
            String aRedirectUrl,
            String aDomainUrl)
    {
        final VLRepository lRepository = R3CUtil.createVlRepoObject(mMessageRequest, aRedirectUrl, aSmartLinkId, aDomainUrl, true);

        if ((lRepository != null) && (lRepository.getShortCode() != null))
        {
            if (!mMessageRequest.isUrlShortned())
                mMessageRequest.setUrlShortned(true);

            R3CUtil.addSmartLinkId(mMessageRequest, aSmartLinkId);

            return lRepository.getShortenUrl();
        }

        return null;
    }

    private void replaceTheMessage()
    {
        mMessageRequest.setLongMessage(mMessage);

        if (isUnicode)
        {
            mMessage = MessageConvertionUtility.convertString2HexString(mMessage);
            mMessageRequest.setIsHexMessage(true);
            mMessageRequest.setLongMessage(mMessage);
        }
    }

    private List<String> getSmartLinkIds()
    {
        final List<String> smartIdlist   = new ArrayList<>();
        int                startPosition = mMessage.indexOf(R3CUtil.URL_TRACK_PATTERN_PREFIX);

        if (startPosition > 0)
        {
            int endPosition = mMessage.indexOf(R3CUtil.URL_TRACK_PATTERN_SUFFIX, startPosition);
            if (log.isDebugEnabled())
                log.debug("Smart link Start position : " + startPosition + " :: End Position : " + endPosition);

            while ((startPosition > 0) && (endPosition > 0))
            {
                if (log.isDebugEnabled())
                    log.debug("In loop Smart link Start position : " + startPosition + " :: End Position : " + endPosition);

                final String smartLinkId = mMessage.substring(startPosition + R3CUtil.LENGTH_URL_TRACK_PATTERN_PREFIX, endPosition);
                smartIdlist.add(smartLinkId);

                startPosition = mMessage.indexOf(R3CUtil.URL_TRACK_PATTERN_PREFIX, startPosition + 1);
                if (startPosition <= 0)
                    break;

                endPosition = mMessage.indexOf(R3CUtil.URL_TRACK_PATTERN_SUFFIX, startPosition);
            }
        }
        return smartIdlist;
    }

}
