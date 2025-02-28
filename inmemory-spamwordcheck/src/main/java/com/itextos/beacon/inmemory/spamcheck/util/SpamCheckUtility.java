package com.itextos.beacon.inmemory.spamcheck.util;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.inmemory.spamcheck.ClientSpamWords;
import com.itextos.beacon.inmemory.spamcheck.GlobalSpamWords;
import com.itextos.beacon.inmemory.spamcheck.MsgTypeSpamWords;

public class SpamCheckUtility
{

    private static final Log log = LogFactory.getLog(SpamCheckUtility.class);

    private SpamCheckUtility()
    {}

    public static boolean validateRequest(
            String aClientId,
            String aMessage,
            MessageType aMsgType,
            String aAccSpamFilter,
            String aMid)
    {
        final SpamCheckObject lSpamCheckObject = new SpamCheckObject(aClientId, aMid, aMessage, aAccSpamFilter, aMsgType);
        return lSpamCheckObject.checkSpam();
    }

    public static SpamAction spamCheckGlobal(
            String aMessage)
    {
        final GlobalSpamWords              lGlobalSpamWords = (GlobalSpamWords) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.GLOBAL_SPAM_BLOCK);
        final Map<String, List<SpamWords>> lSpamkWordMap    = lGlobalSpamWords.getGlobalSpamWords();
        SpamAction                         lSpamAction      = null;

        if (lSpamkWordMap != null)
            for (final Map.Entry<String, List<SpamWords>> entry : lSpamkWordMap.entrySet())
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
     * final ExceptionalSpamWords lExceptionalSW = (ExceptionalSpamWords)
     * InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.
     * EXCEPTIONS_SPAM_BLOCK);
     * final List<String> lSpamList = lExceptionalSW.getSpamWordList(aClientId);
     * if (log.isDebugEnabled())
     * log.debug("checkSpamExceptional Spam List:" + lSpamList);
     * if (lSpamList != null)
     * for (final String data : lSpamList)
     * try
     * {
     * final boolean isPatternMatched =
     * PatternCache.getInstance().isPatternMatch(PatternCheckCategory.SPAM_CHECK,
     * data, aMessage);
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
        final MsgTypeSpamWords             lMsgTypeSpamWords = (MsgTypeSpamWords) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.MSG_TYPE_SPAM_WORDS_BLOCK);
        final Map<String, List<SpamWords>> lSpamkWordMap     = lMsgTypeSpamWords.getMsgTypeSpamWords(aMsgType);
        SpamAction                         lSpamAction       = null;

        if (lSpamkWordMap != null)
            for (final Map.Entry<String, List<SpamWords>> entry : lSpamkWordMap.entrySet())
            {
                final List<SpamWords> lSpamkWordList = entry.getValue();

                lSpamAction = SpamUtil.isThreasHoldReached(aMessage.toLowerCase(), lSpamkWordList);
                if (lSpamAction != null)
                    return lSpamAction;
            }
        return lSpamAction;
    }

    public static SpamAction spamCheckBasedonClient(
            String aClientId,
            String aMessage)
    {
        final ClientSpamWords              lClientSpamWords = (ClientSpamWords) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.CLIENT_SPAM_BLOCK);
        final Map<String, List<SpamWords>> lSpamkWordMap    = lClientSpamWords.getClientWiseSpamWords(aClientId);
        SpamAction                         lSpamAction      = null;

        if (lSpamkWordMap != null)
            for (final Map.Entry<String, List<SpamWords>> entry : lSpamkWordMap.entrySet())
            {
                final List<SpamWords> lSpamkWordList = entry.getValue();

                lSpamAction = SpamUtil.isThreasHoldReached(aMessage.toLowerCase(), lSpamkWordList);
                if (lSpamAction != null)
                    return lSpamAction;
            }
        return lSpamAction;
    }

}