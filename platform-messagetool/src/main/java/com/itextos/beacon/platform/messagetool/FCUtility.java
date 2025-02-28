package com.itextos.beacon.platform.messagetool;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.DCS;
import com.itextos.beacon.commonlib.constants.FeatureCode;
import com.itextos.beacon.commonlib.constants.MessageClass;
import com.itextos.beacon.commonlib.constants.MessageClassLength;
import com.itextos.beacon.commonlib.constants.SpecialCharacters;

public class FCUtility
{

    private static final Log log = LogFactory.getLog(FCUtility.class);

    private FCUtility()
    {}

    static FeatureCode getFeatureCodeBasedOnDcs(
            int aDataCodingValue,
            int aPlainMessageLengh,
            int aUdhInclude,
            String aUdh,
            String aMessage)
    {
        final DCS   dcs        = DCS.getDcs(aDataCodingValue);
        FeatureCode lFeaturecd = null;

        switch (dcs)
        {
            case PLAIN:
                lFeaturecd = getPlainMessageFeatureCode(aPlainMessageLengh, aUdh);
                break;

            case FLASH:
            case FLASH_PM:
            case FLASH_PM2:	
                lFeaturecd = getFlashPlainFeatureCode(aPlainMessageLengh, aUdhInclude, aUdh, aMessage);
                break;

            case FLASH_UNICODE1:
            case FLASH_UNICODE2:
                lFeaturecd = getFlashUnicodeFeatureCode(aPlainMessageLengh, aUdhInclude, aUdh, aMessage);
                break;

            case PDU_MSG:
                break;

            case SPECIAL_PORT_PM:
                lFeaturecd = getSpecialPortFeatureCode(aPlainMessageLengh, aUdhInclude, aMessage);
                break;

            case UNICODE:
                lFeaturecd = getUnicodeFeatureCode(aPlainMessageLengh, aUdhInclude, aUdh, aMessage);
                break;

            default:
                break;
        }
        return lFeaturecd;
    }

    static FeatureCode getFeatureCodeBasedOnMessageClass(
            MessageClass aMsgClass,
            int aPlainMessageLengh,
            int aUdhInclude,
            String aUdh,
            String aMessage)
    {
        FeatureCode lFeaturecd = null;

        switch (aMsgClass)
        {
            case PLAIN_MESSAGE:
                lFeaturecd = getPlainMessageFeatureCode(aPlainMessageLengh, aUdh);
                break;

            case BINARY_MESSAGE:
                break;

            case FLASH_PLAIN_MESSAGE:
                lFeaturecd = getFlashPlainFeatureCode(aPlainMessageLengh, aUdhInclude, aUdh, aMessage);
                break;

            case FLASH_UNICODE_MESSAGE:
                lFeaturecd = getFlashUnicodeFeatureCode(aPlainMessageLengh, aUdhInclude, aUdh, aMessage);
                break;

            case SP_PLAIN_MESSAGE:
            case SP_UNICODE_MESSAGE:
                lFeaturecd = getSpecialPortFeatureCode(aPlainMessageLengh, aUdhInclude, aMessage);
                break;

            case UNICODE_MESSAGE:
                lFeaturecd = getUnicodeFeatureCode(aPlainMessageLengh, aUdhInclude, aUdh, aMessage);
                break;

            default:
                break;
        }
        return lFeaturecd;
    }

    static FeatureCode getSpecialPortFeatureCode(
            int aMessageLength,
            int aUdhInclude,
            String aMessage)
    {

        if (aMessageLength > 0)
        {
            if (aMessageLength > MessageClassLength.MAX_LENGTH_SP_PLAIN_MESSAGE)
                return FeatureCode.SPECIAL_PORT_PLAIN_MESSAGE_MULTI;
            return FeatureCode.SPECIAL_PORT_PLAIN_MESSAGE_SINGLE;
        }

        if (aUdhInclude == 1)
            return FeatureCode.SPECIAL_PORT_UNICODE_MULTI;

        final int tempMessageLength = aMessage.length();
        if (tempMessageLength > MessageClassLength.MAX_LENGTH_SP_UNICODE_MESSAGE)
            return FeatureCode.SPECIAL_PORT_UNICODE_MULTI;
        return FeatureCode.SPECIAL_PORT_UNICODE_SINGLE;
    }

    static int findSplitMessageLength(
            MessageClass aMessageClass,
            boolean aIs16BitUdh)
    {

        switch (aMessageClass)
        {
            case BINARY_MESSAGE:
                return MessageClassLength.SPLIT_LENGTH_BINARY_MESSAGE;

            case PLAIN_MESSAGE:
            case FLASH_PLAIN_MESSAGE:
                return (aIs16BitUdh) ? MessageClassLength.SPLIT_LENGTH_PLAIN_MESSAGE_16_BIT : MessageClassLength.SPLIT_LENGTH_PLAIN_MESSAGE_8_BIT;

            case UNICODE_MESSAGE:
            case FLASH_UNICODE_MESSAGE:
                return (aIs16BitUdh) ? MessageClassLength.SPLIT_LENGTH_UNICODE_MESSAGE_16_BIT : MessageClassLength.SPLIT_LENGTH_UNICODE_MESSAGE;

            case SP_PLAIN_MESSAGE:
                return MessageClassLength.SPLIT_LENGTH_SP_PLAIN_MESSAGE;

            case SP_UNICODE_MESSAGE:
                return MessageClassLength.SPLIT_LENGTH_SP_UNICODE_MESSAGE;

            default:
                break;
        }
        return -1;
    }

    static int fetchMaxMessageLength(
            MessageClass aMessageClass)
    {

        switch (aMessageClass)
        {
            case BINARY_MESSAGE:
                return MessageClassLength.MAX_LENGTH_BINARY_MESSAGE;

            case PLAIN_MESSAGE:
            case FLASH_PLAIN_MESSAGE:
                return MessageClassLength.MAX_LENGTH_PLAIN_MESSAGE;

            case UNICODE_MESSAGE:
            case FLASH_UNICODE_MESSAGE:
                return MessageClassLength.MAX_LENGTH_UNICODE_MESSAGE;

            case SP_PLAIN_MESSAGE:
                return MessageClassLength.MAX_LENGTH_SP_PLAIN_MESSAGE;

            case SP_UNICODE_MESSAGE:
                return MessageClassLength.MAX_LENGTH_SP_UNICODE_MESSAGE;

            default:
                break;
        }
        return -1;
    }

    static String replaceISOCharacter(
            String aMessage)
    {
        final char[] lCharArr = aMessage.toCharArray();

        for (int i = 0; i < lCharArr.length; i++)
            lCharArr[i] = (char) CharacterAsciiMapping.getMappedByteIso(lCharArr[i]);

        return new String(lCharArr);
    }

    public static int getMessageLength(
            String aMessage)
    {
        int                   lMessageLen   = aMessage.length();
        final List<Character> specialCharLs = SpecialCharacters.getSpecialCharList();

        for (final Character specialChar : specialCharLs)
        {
            final int lSpecialCharMatchesCount = StringUtils.countMatches(aMessage, specialChar.toString());
            lMessageLen = lMessageLen + lSpecialCharMatchesCount;
        }
        return lMessageLen;
    }

    static String addPrefixSuffix(
            String aMessage,
            PrefixSuffix aPrefixaSuffixValue)
    {
        if (log.isDebugEnabled())
            log.debug("Prefix and Suffix value from db. PrefixSuffix : '" + aPrefixaSuffixValue + "'");

        if (!"".equals(aPrefixaSuffixValue.getPrefix()))
            aMessage = aPrefixaSuffixValue.getPrefix() + aMessage;

        if (!"".equals(aPrefixaSuffixValue.getSuffix()))
            aMessage = aMessage + aPrefixaSuffixValue.getSuffix();

        if (log.isDebugEnabled())
            log.debug("Message after adding Prefix Suffix :" + aMessage);
        return aMessage.trim();
    }

    private static FeatureCode getPlainMessageFeatureCode(
            int aMessageLength,
            String aUdh)
    {
        if (log.isDebugEnabled())
            log.debug("Udh Value :" + aUdh);

        if ((aMessageLength > MessageClassLength.MAX_LENGTH_PLAIN_MESSAGE) || (aUdh.length() > 0))
            return FeatureCode.PLAIN_MESSAGE_MULTI;

        return FeatureCode.PLAIN_MESSAGE_SINGLE;
    }

    private static FeatureCode getFlashUnicodeFeatureCode(
            int aMessageLength,
            int aUdhInclude,
            String aUdh,
            String aMessage)
    {
        if (aUdhInclude == 1)
            return FeatureCode.FLASH_UNICODE_MULTI;

        int tempMessageLength = aMessage.length();
        if (aMessageLength > 0)
            tempMessageLength = aMessageLength;

        if ((tempMessageLength > MessageClassLength.MAX_LENGTH_UNICODE_MESSAGE) || (aUdh.length() > 0))
            return FeatureCode.FLASH_UNICODE_MULTI;
        return FeatureCode.FLASH_UNICODE_SINGLE;
    }

    private static FeatureCode getFlashPlainFeatureCode(
            int aMessageLength,
            int aUdhInclude,
            String aUdh,
            String aMessage)
    {
        if ((aMessageLength > MessageClassLength.MAX_LENGTH_PLAIN_MESSAGE) || (aUdh.length() > 0))
            return FeatureCode.FLASH_PLAIN_MESSAGE_MULTI;
        return FeatureCode.FLASH_PLAIN_MESSAGE_SINGLE;
    }

    private static FeatureCode getUnicodeFeatureCode(
            int aMessageLength,
            int aUdhInclude,
            String aUdh,
            String aMessage)
    {
        int lMessageLength = aMessage.length();
        if (aMessageLength > 0)
            lMessageLength = aMessageLength;

        if ((aUdhInclude == 1) || (!aUdh.isBlank() && (aUdh.length() > 0)) || (lMessageLength > MessageClassLength.MAX_LENGTH_UNICODE_MESSAGE))
            return FeatureCode.UNICODE_MULTI;
        return FeatureCode.UNICODE_SINGLE;
    }

    static MessageClass findMessageClass(
            FeatureCode aFeatureCode)
    {

        switch (aFeatureCode)
        {
            case BINARY_MSG:
                return MessageClass.BINARY_MESSAGE;

            case FLASH_PLAIN_MESSAGE_MULTI:
            case FLASH_PLAIN_MESSAGE_SINGLE:
                return MessageClass.FLASH_PLAIN_MESSAGE;

            case FLASH_UNICODE_MULTI:
            case FLASH_UNICODE_SINGLE:
                return MessageClass.FLASH_UNICODE_MESSAGE;

            case PLAIN_MESSAGE_MULTI:
            case PLAIN_MESSAGE_SINGLE:
                return MessageClass.PLAIN_MESSAGE;

            case SPECIAL_PORT_PLAIN_MESSAGE_MULTI:
            case SPECIAL_PORT_PLAIN_MESSAGE_SINGLE:
                return MessageClass.SP_PLAIN_MESSAGE;

            case SPECIAL_PORT_UNICODE_MULTI:
            case SPECIAL_PORT_UNICODE_SINGLE:
                return MessageClass.SP_UNICODE_MESSAGE;

            case UNICODE_MULTI:
            case UNICODE_SINGLE:
                return MessageClass.UNICODE_MESSAGE;

            default:
                break;
        }
        return MessageClass.BINARY_MESSAGE;
    }

    public static int getSingleMessageLength(
            MessageClass aMessageClass)
    {

        switch (aMessageClass)
        {
            case BINARY_MESSAGE:
                return MessageClassLength.MAX_LENGTH_BINARY_MESSAGE;

            case PLAIN_MESSAGE:
            case FLASH_PLAIN_MESSAGE:
                return MessageClassLength.MAX_LENGTH_PLAIN_MESSAGE;

            case UNICODE_MESSAGE:
            case FLASH_UNICODE_MESSAGE:
                return MessageClassLength.MAX_LENGTH_UNICODE_MESSAGE;

            case SP_PLAIN_MESSAGE:
                return MessageClassLength.MAX_LENGTH_SP_PLAIN_MESSAGE;

            case SP_UNICODE_MESSAGE:
                return MessageClassLength.MAX_LENGTH_SP_UNICODE_MESSAGE;

            default:
                return MessageClassLength.MAX_LENGTH_PLAIN_MESSAGE;
        }
    }

    public static MessageClass getMessageClassBasedonDCS(
            int aDcs)
    {
        final DCS lDcs = DCS.getDcs(aDcs);

        switch (lDcs)
        {
            case FLASH:
            case FLASH_PM:
            case FLASH_PM2:	
                return MessageClass.FLASH_PLAIN_MESSAGE;

            case FLASH_UNICODE1:
            case FLASH_UNICODE2:
                return MessageClass.FLASH_UNICODE_MESSAGE;

            case PLAIN:
                return MessageClass.PLAIN_MESSAGE;

            case SPECIAL_PORT_PM:
                return MessageClass.SP_PLAIN_MESSAGE;

            case UNICODE:
                return MessageClass.UNICODE_MESSAGE;

            case PDU_MSG:
                return MessageClass.PLAIN_MESSAGE; // Yet to verify

            default:
                return MessageClass.BINARY_MESSAGE;
        }
    }

}