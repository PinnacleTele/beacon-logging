package com.itextos.beacon.inmemory.spamcheck;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;
import com.itextos.beacon.inmemory.spamcheck.util.SpamWords;

public class MsgTypeSpamWords
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log                               log               = LogFactory.getLog(MsgTypeSpamWords.class);
    private Map<MessageType, Map<String, List<SpamWords>>> mMsgTypeSpamWords = new EnumMap<>(MessageType.class);

    public MsgTypeSpamWords(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public Map<String, List<SpamWords>> getMsgTypeSpamWords(
            MessageType aMsgType)
    {
        return mMsgTypeSpamWords.get(aMsgType);
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // select ms.msg_type, ms.spam_word, sm.threashold_count, sm.action,
        // sm.spam_group_id from listing.spam_group_master sm,
        // listing.msg_type_spam_words ms where sm.spam_group_id = ms.spam_group_id and
        // sm.is_active=1 and ms.is_active=1 and sm.action<>0 order by action asc

        final Map<MessageType, Map<String, List<SpamWords>>> lGroupIdMsgTypeSpamWords = new EnumMap<>(MessageType.class);

        while (aResultSet.next())
        {
            final MessageType lMsgType   = MessageType.getMessageType(CommonUtility.nullCheck(aResultSet.getString("msg_type"), true));
            final String      lSpamWord  = CommonUtility.nullCheck(aResultSet.getString("spam_word"), true).toLowerCase();
            final String      lSpamGrpId = CommonUtility.nullCheck(aResultSet.getString("spam_group_id"), true);

            if ((lMsgType == null) || lSpamWord.isEmpty())
                continue;

            final Map<String, List<SpamWords>> lMsgTypeSpamWords = lGroupIdMsgTypeSpamWords.computeIfAbsent(lMsgType, k -> new HashMap<>());
            final List<SpamWords>              spamWordList      = lMsgTypeSpamWords.computeIfAbsent(lSpamGrpId, k -> new ArrayList<>());
            final SpamWords                    lSpamWords        = new SpamWords(lSpamWord, aResultSet.getInt("threashold_count"), aResultSet.getInt("action"));
            spamWordList.add(lSpamWords);
        }
        if (!lGroupIdMsgTypeSpamWords.isEmpty())
            mMsgTypeSpamWords = lGroupIdMsgTypeSpamWords;
    }

}
