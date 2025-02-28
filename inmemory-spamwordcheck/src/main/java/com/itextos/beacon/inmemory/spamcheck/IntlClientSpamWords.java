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

public class IntlClientSpamWords
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log                          log                  = LogFactory.getLog(IntlClientSpamWords.class);
    private Map<String, Map<String, List<SpamWords>>> mIntlClientSpamWords = new HashMap<>();

    public IntlClientSpamWords(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public Map<String, List<SpamWords>> getIntlClientWiseSpamWords(
            String aClientId)
    {
        return mIntlClientSpamWords.get(aClientId);
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // select cs.cli_id, cs.spam_word, sm.threashold_count, sm.action,
        // sm.spam_group_id from listing.spam_group_master sm,
        // listing.intl_client_spam_words
        // cs where sm.spam_group_id = cs.spam_group_id and sm.is_active=1 and
        // cs.is_active=1 and sm.action<>0 order by action asc

        final Map<String, Map<String, List<SpamWords>>> lIntlGroupIdClientSpamWords = new HashMap<>();

        while (aResultSet.next())
        {
            final String lClientId  = CommonUtility.nullCheck(aResultSet.getString("cli_id"), true);
            final String lSpamWord  = CommonUtility.nullCheck(aResultSet.getString("spam_word"), true).toLowerCase();
            final String lSpamGrpId = CommonUtility.nullCheck(aResultSet.getString("spam_group_id"), true);

            if ((lClientId.isEmpty()) || lSpamWord.isEmpty())
                continue;

            final Map<String, List<SpamWords>> lMsgTypeSpamWords = lIntlGroupIdClientSpamWords.computeIfAbsent(lClientId, k -> new HashMap<>());
            final List<SpamWords>              spamWordList      = lMsgTypeSpamWords.computeIfAbsent(lSpamGrpId, k -> new ArrayList<>());
            final SpamWords                    lSpamWords        = new SpamWords(lSpamWord, aResultSet.getInt("threashold_count"), aResultSet.getInt("action"));
            spamWordList.add(lSpamWords);
        }
        if (!lIntlGroupIdClientSpamWords.isEmpty())
            mIntlClientSpamWords = lIntlGroupIdClientSpamWords;
    }

}
