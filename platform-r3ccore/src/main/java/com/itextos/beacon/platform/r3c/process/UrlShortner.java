package com.itextos.beacon.platform.r3c.process;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.MessageConvertionUtility;
import com.itextos.beacon.platform.r3c.util.R3CUtil;

public class UrlShortner
{

    private static final Log     log                    = LogFactory.getLog(UrlShortner.class);

    private final MessageRequest mMessageRequest;
    private String               mMessage;
    private boolean              mIsUnicode             = false;
    private boolean              mIsSkipUrlShortnerFlow = false;

    public UrlShortner(
            MessageRequest aMessageRequest)
    {
        mMessageRequest = aMessageRequest;
        checkMessages();
    }

    private void checkMessages()
    {
        mMessage = CommonUtility.nullCheck(mMessageRequest.getLongMessage());
        final boolean lIsHexMsg = mMessageRequest.isHexMessage();

        if (lIsHexMsg && !mMessage.isBlank())
        {
            mIsUnicode = true;

            if (mMessage.contains(R3CUtil.URL_TRACK_PATTERN_PREFIX) && mMessage.contains(R3CUtil.URL_TRACK_PATTERN_SUFFIX))
            {
                mIsSkipUrlShortnerFlow = true;
                log.info("urlShortnerCheckProcess() - Unicode Message contains [~ XXX ~] smartlink Id. Hence Skip the URL_SHORTNER..");
            }
            else
                mMessage = MessageConvertionUtility.convertHex2String(mMessage);
        }
    }

    public void process()
    {

        try
        {
            if (mIsSkipUrlShortnerFlow || mMessage.isEmpty())
                return;

            List<String> lUrlsToConvert = R3CUtil.getUrlsWithPrefix(mMessage, false);
            List<String> lExcludeUrls   = null;

            if (lUrlsToConvert.isEmpty())
                lUrlsToConvert = R3CUtil.getNonPrefixedUrls(mMessage);

            lExcludeUrls = R3CUtil.getUrlsWithPrefix(mMessage, true);
            if (!lExcludeUrls.isEmpty())
                lUrlsToConvert = R3CUtil.removeExcludeUrls(lUrlsToConvert, lExcludeUrls);

            if (log.isDebugEnabled())
            {
                log.debug("urlShortnerCheckProcess() - Extract VLinks from Message :" + lUrlsToConvert);
                log.debug("urlShortnerCheckProcess() - Exclude VLinks from Message :" + lExcludeUrls);
            }

            if (lUrlsToConvert.isEmpty())
                replaceOriginalMessageAndSentToNextLevel(lExcludeUrls);
            else
                callUrlShortner(lUrlsToConvert, lExcludeUrls);
        }
        catch (final Exception e)
        {
            log.error("Exception occer in Vl-Processor -", e);
            // R3CProcess.sendToNextProducer(mMessageRequest);
        }
    }

    private void callUrlShortner(
            List<String> aUrlsToConvert,
            List<String> aExcludeUrls)
    {
        mMessage = processUrl(aUrlsToConvert);
        mMessage = R3CUtil.replace(mMessage, false);

        if (log.isDebugEnabled())
            log.debug("After replace the URL Starts/Ends characters in Message Object : " + mMessage);

        if (log.isDebugEnabled())
            log.debug("Exclude Urls list : " + aExcludeUrls);

        if ((aExcludeUrls != null) && !aExcludeUrls.isEmpty())
        {
            mMessage = R3CUtil.replace(mMessage, true);
            if (log.isDebugEnabled())
                log.debug("After replace the URL ExcludeStarts/ExcludeEnds characters in Message Object : " + mMessage);
        }

        if (log.isDebugEnabled())
            log.debug("Final Formatted Domain Url in Message Object : " + mMessage);

        setBackMessage();
    }

    private void setBackMessage()
    {

        if (mIsUnicode)
        {
            final String lConvertedHexMsg = MessageConvertionUtility.convertString2HexString(mMessage);
            if (log.isDebugEnabled())
                log.debug("HEX String " + lConvertedHexMsg);

            mMessageRequest.setLongMessage(lConvertedHexMsg);
        }
        else
            mMessageRequest.setLongMessage(mMessage);
    }

    private void replaceOriginalMessageAndSentToNextLevel(
            List<String> aExcludeUrls)
    {
        mMessage = R3CUtil.replace(mMessage, false);

        if (log.isDebugEnabled())
            log.debug("After replace the URL Starts/Ends characters in Message Object : " + mMessage);

        if ((aExcludeUrls != null) && (!aExcludeUrls.isEmpty()))
        {
            mMessage = R3CUtil.replace(mMessage, true);
            if (log.isDebugEnabled())
                log.debug("After replace the URL ExcludeStarts/ExcludeEnds characters in Message Object : " + mMessage);
        }

        setBackMessage();
    }

    private String processUrl(
            List<String> aUrls)
    {
        String lTempMessage = mMessage;

        for (final String lUrlToConvert : aUrls)
        {
            if (log.isDebugEnabled())
                log.debug("Payload url :" + lUrlToConvert);

            final boolean isExcludeUrl = R3CUtil.isExcludeUrl(mMessageRequest.getClientId(), lUrlToConvert);

            if (!isExcludeUrl)
            {
                final String lShortenUrl = VLShortnerProcess.doVLShortner(mMessageRequest, lUrlToConvert);

                if (log.isDebugEnabled())
                    log.debug("Formatted Domain Url : " + lShortenUrl);

                if (lShortenUrl != null)
                    lTempMessage = lTempMessage.replace(lUrlToConvert, lShortenUrl);

                if (log.isDebugEnabled())
                    log.debug("After replace the Formatted Domain Url in Message Object : " + lTempMessage);
            }
        }

        return lTempMessage;
    }

}