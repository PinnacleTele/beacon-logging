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

public class GlobalSpamWords
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log             log              = LogFactory.getLog(GlobalSpamWords.class);
    private Map<String, List<SpamWords>> mGlobalSpamWords = new HashMap<>();

    public GlobalSpamWords(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public Map<String, List<SpamWords>> getGlobalSpamWords()
    {
        return mGlobalSpamWords;
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // select gs.spam_word, sm.threashold_count, sm.action, sm.spam_group_id from
        // listing.spam_group_master sm, listing.global_spam_words gs where
        // sm.spam_group_id = gs.spam_group_id and sm.is_active=1 and gs.is_active=1 and
        // sm.action<>0 order by action asc

        final Map<String, List<SpamWords>> lGlobalSpamWords = new HashMap<>();

        while (aResultSet.next())
        {
            final String lSpamWord  = CommonUtility.nullCheck(aResultSet.getString("spam_word"), true).toLowerCase();
            final String lSpamGrpId = CommonUtility.nullCheck(aResultSet.getString("spam_group_id"), true);

            if (lSpamWord.isEmpty())
                continue;

            final List<SpamWords> lGlobalSpamWordList = lGlobalSpamWords.computeIfAbsent(lSpamGrpId, k -> new ArrayList<>());
            lGlobalSpamWordList.add(new SpamWords(lSpamWord, aResultSet.getInt("threashold_count"), aResultSet.getInt("action")));
        }

        if (!lGlobalSpamWords.isEmpty())
            mGlobalSpamWords = lGlobalSpamWords;
    }

}
