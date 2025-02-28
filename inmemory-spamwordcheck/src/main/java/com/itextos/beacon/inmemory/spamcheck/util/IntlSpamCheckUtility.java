package com.itextos.beacon.inmemory.spamcheck.util;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.inmemory.spamcheck.IntlClientSpamWords;
import com.itextos.beacon.inmemory.spamcheck.IntlGlobalSpamWords;
import com.itextos.beacon.inmemory.spamcheck.IntlMsgTypeSpamWords;

public class IntlSpamCheckUtility
{

    private static final Log log = LogFactory.getLog(IntlSpamCheckUtility.class);

    private IntlSpamCheckUtility()
    {}

    public static boolean validateRequest(
            String aClientId,
            String aMessage,
            MessageType aMsgType,
            String aAccSpamFilter,
            String aMid)
    {
        final IntlSpamCheckObject lIntlSpamCheckObject = new IntlSpamCheckObject(aClientId, aMid, aMessage, aAccSpamFilter, aMsgType);
        return lIntlSpamCheckObject.checkSpam();
    }

    public static SpamAction spamCheckGlobal(
            String aMessage)
    {
        final IntlGlobalSpamWords          lIntlGlobalSpamWords = (IntlGlobalSpamWords) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.INTL_GLOBAL_SPAM_BLOCK);
        final Map<String, List<SpamWords>> lIntlSpamkWordMap    = lIntlGlobalSpamWords.getIntlGlobalSpamWords();
        SpamAction                         lSpamAction          = null;

        if (lIntlSpamkWordMap != null)
            for (final Map.Entry<String, List<SpamWords>> entry : lIntlSpamkWordMap.entrySet())
            {
                final List<SpamWords> lSpamkWordList = entry.getValue();

                lSpamAction = SpamUtil.isThreasHoldReached(aMessage.toLowerCase(), lSpamkWordList);
                if (lSpamAction != null)
                    return lSpamAction;
            }
        return lSpamAction;
    }

    /*
     * public static boolean spamCheckExceptional(
     * String aMessage,
     * String aClientId)
     * {
     * final IntlExceptionalSpamWords lIntlExceptionalSW =
     * (IntlExceptionalSpamWords)
     * InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.
     * INTL_EXCEPTIONS_SPAM_BLOCK);
     * final List<String> lSpamList = lIntlExceptionalSW.getSpamWordList(aClientId);
     * if (log.isDebugEnabled())
     * log.debug("checkSpamExceptional Spam List:" + lSpamList);
     * if (lSpamList != null)
     * for (final String data : lSpamList)
     * try
     * {
     * final boolean isPatternMatched =
     * PatternCache.getInstance().isPatternMatch(PatternCheckCategory.
     * INTL_SPAM_CHECK, data, aMessage);
     * if (log.isDebugEnabled())
     * log.debug("checkSpamExceptional msg=" + aMessage + " match status " +
     * isPatternMatched);
     * if (isPatternMatched)
     * return false; // means process request further
     * }
     * catch (final Exception ignore)
     * {}
     * return true;
     * }
     */

    public static SpamAction spamCheckBasedonMessageType(
            MessageType aMsgType,
            String aMessage)
    {
        final IntlMsgTypeSpamWords         lIntlMsgTypeSpamWords = (IntlMsgTypeSpamWords) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.INTL_MSG_TYPE_SPAM_WORDS_BLOCK);
        final Map<String, List<SpamWords>> lSpamWordsMap         = lIntlMsgTypeSpamWords.getIntlMsgTypeSpamWords(aMsgType);
        SpamAction                         lSpamAction           = null;

        if (lSpamWordsMap != null)
            for (final Map.Entry<String, List<SpamWords>> entry : lSpamWordsMap.entrySet())
            {
                final List<SpamWords> lSpamkWordList = entry.getValue();

                lSpamAction = SpamUtil.isThreasHoldReached(aMessage.toLowerCase(), lSpamkWordList);
                if (lSpamAction != null)
                    return lSpamAction;
            }
        return lSpamAction;
    }

    public static SpamAction spamCheckBasedonClient(
            String aClient,
            String aMessage)
    {
        final IntlClientSpamWords          lIntlClientSpamWords = (IntlClientSpamWords) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.INTL_CLIENT_SPAM_BLOCK);
        final Map<String, List<SpamWords>> lSpamWordsMap        = lIntlClientSpamWords.getIntlClientWiseSpamWords(aClient);
        SpamAction                         lSpamAction          = null;

        if (lSpamWordsMap != null)
            for (final Map.Entry<String, List<SpamWords>> entry : lSpamWordsMap.entrySet())
            {
                final List<SpamWords> lSpamkWordList = entry.getValue();

                lSpamAction = SpamUtil.isThreasHoldReached(aMessage.toLowerCase(), lSpamkWordList);
                if (lSpamAction != null)
                    return lSpamAction;
            }
        return lSpamAction;
    }

}
