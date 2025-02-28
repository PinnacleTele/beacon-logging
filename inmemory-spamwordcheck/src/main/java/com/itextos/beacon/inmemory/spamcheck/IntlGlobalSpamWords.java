package com.itextos.beacon.inmemory.spamcheck;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;
import com.itextos.beacon.inmemory.spamcheck.util.SpamWords;

public class IntlGlobalSpamWords
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log             log                  = LogFactory.getLog(IntlGlobalSpamWords.class);
    private Map<String, List<SpamWords>> mIntlGlobalSpamWords = new HashMap<>();

    public IntlGlobalSpamWords(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public Map<String, List<SpamWords>> getIntlGlobalSpamWords()
    {
        return mIntlGlobalSpamWords;
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // select gs.spam_word, sm.threashold_count, sm.action, sm.spam_group_id from
        // listing.spam_group_master sm, listing.intl_global_spam_words gs where
        // sm.spam_group_id = gs.spam_group_id and sm.is_active=1 and gs.is_active=1 and
        // sm.action<>0 order by action asc

        final Map<String, List<SpamWords>> lIntlGlobalSpamWords = new HashMap<>();

        while (aResultSet.next())
        {
            final String lSpamWord  = CommonUtility.nullCheck(aResultSet.getString("spam_word"), true).toLowerCase();
            final String lSpamGrpId = CommonUtility.nullCheck(aResultSet.getString("spam_group_id"), true);

            if (lSpamWord.isEmpty())
                continue;

            final List<SpamWords> lGlobalSpamWordList = lIntlGlobalSpamWords.computeIfAbsent(lSpamGrpId, k -> new ArrayList<>());
            lGlobalSpamWordList.add(new SpamWords(lSpamWord, aResultSet.getInt("threashold_count"), aResultSet.getInt("action")));
        }

        if (!lIntlGlobalSpamWords.isEmpty())
            mIntlGlobalSpamWords = lIntlGlobalSpamWords;
    }

}