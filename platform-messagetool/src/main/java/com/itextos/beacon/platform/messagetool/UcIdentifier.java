package com.itextos.beacon.platform.messagetool;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.Constants;
import com.itextos.beacon.inmemory.configvalues.ApplicationConfiguration;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;

public class UcIdentifier
{

    private static final Log log = LogFactory.getLog(UcIdentifier.class);

    private UcIdentifier()
    {}

    public static Response checkForUnicode(
            Request aRequest)
    {
        final Response     response             = new Response();

        // If the Special char word count is > 3 we should consider as UC message.
        boolean            isMsgUnicode         = false;
        int                lOccurenceCount      = 0;
        int                lSpecialCharSeqCount = 0;
        final List<String> lRemoveSpecialChars  = new ArrayList<>();

        String             lMessage             = aRequest.getMessage();

        if (log.isDebugEnabled())
            log.debug("Request Object " + aRequest);

        for (final char lChar : lMessage.toCharArray())
        {
            if (log.isDebugEnabled())
                log.debug("Total number of characters " + lMessage.length());

            try
            {
                final String lEncodedString = URLEncoder.encode(String.valueOf(lChar), Constants.ENCODER_FORMAT);

                if (log.isDebugEnabled())
                    log.debug("Encoded String :" + lEncodedString);

                if (lEncodedString.length() > 3)
                {
                    if (isAllowSpecialCharacter(lEncodedString))
                        continue;

                    lSpecialCharSeqCount++;

                    if (lSpecialCharSeqCount >= aRequest.getAccountLevelSplCharLength())
                    {
                        lOccurenceCount++;
                        lSpecialCharSeqCount = 0;
                    }

                    if (log.isDebugEnabled())
                        log.debug("Special Character Seq Count : '" + lSpecialCharSeqCount + "' Special Character Occurance count : '" + lOccurenceCount + "'");

                    lRemoveSpecialChars.add(String.valueOf(lChar));
                }
                else
                    lSpecialCharSeqCount = 0;

                if (lOccurenceCount >= aRequest.getAccountLevelOccuranceCount())
                {
                    isMsgUnicode = true;
                    break;
                }
            }
            catch (final Exception e)
            {
                log.error("Ingore the exception .", e);
            }
        }

        if (aRequest.isRemoveUcCharsInPlainMessage() && !isMsgUnicode)
            lMessage = removeSpecialCharInMsg(lMessage, lRemoveSpecialChars);

        response.setUnicode(isMsgUnicode);
        response.setMessage(lMessage);

        return response;
    }

    private static String removeSpecialCharInMsg(
            String aMsg,
            List<String> aRemoveChrs)
    {
        for (final String s : aRemoveChrs)
            aMsg = aMsg.replaceAll(s, "");

        return aMsg;
    }

    private static boolean isAllowSpecialCharacter(
            String aSpecialChar)
    {
        return (getAllowSpecialCharacters().contains(aSpecialChar.replace("%", "").toLowerCase()));
    }

    private static List<String> getAllowSpecialCharacters()
    {
        final String lSpecialCharacters = getAppConfigValueAsString(ConfigParamConstants.ALLOW_SPECIAL_CHAR);

        if (log.isDebugEnabled())
            log.debug("Configured Specialcharacters : " + lSpecialCharacters);

        final List<String> lAllowChars = new ArrayList<>();

        if (lSpecialCharacters != null)
        {
            final String[] lSpecialChar = lSpecialCharacters.split(",");
            Collections.addAll(lAllowChars, lSpecialChar);
        }

        if (log.isDebugEnabled())
            log.debug("Allow SpecialCharacters List : " + lAllowChars);

        return lAllowChars;
    }

    private static String getAppConfigValueAsString(
            ConfigParamConstants aConfigParamConstant)
    {
        final ApplicationConfiguration lAppConfiguration = (ApplicationConfiguration) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.APPLICATION_CONFIG);
        return lAppConfiguration.getConfigValue(aConfigParamConstant.getKey());
    }

}