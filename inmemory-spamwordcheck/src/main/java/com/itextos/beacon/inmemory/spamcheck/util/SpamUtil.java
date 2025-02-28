package com.itextos.beacon.inmemory.spamcheck.util;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SpamUtil
{

    private static final Log log = LogFactory.getLog(SpamUtil.class);

    private SpamUtil()
    {}

    public static SpamAction isThreasHoldReached(
            String aMessage,
            List<SpamWords> aGlobalSpamWords)
    {
        SpamAction returnValue = null;

        try
        {
            int counter = 0;

            if (!aGlobalSpamWords.isEmpty())
            {
                final int lThresholdCount = aGlobalSpamWords.get(0).getThresholdCount();
                final int mAction         = aGlobalSpamWords.get(0).getAction();

                for (final SpamWords spamWords : aGlobalSpamWords)
                {
                    if (log.isDebugEnabled())
                        log.debug("Spam Keyword : " + spamWords.getSpamWord());

                    if (aMessage.contains(spamWords.getSpamWord()))
                        counter++;
                }

                if (log.isDebugEnabled())
                    log.debug("ThreasHold Count : '" + lThresholdCount + "' and matches count : '" + counter + "'");

                if (counter >= lThresholdCount)
                    returnValue = new SpamAction(mAction, counter);
            }
        }
        catch (final Exception e)
        {
            log.error("Problem counting spam words", e);
        }
        return returnValue;
    }

}
