package com.itextos.beacon.inmemory.carrierhandover;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.ItextosClient;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class MessageReplaceKeywords
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log                 log                       = LogFactory.getLog(MessageReplaceKeywords.class);

    private Map<String, Map<String, String>> mClientReplaceKeywordInfo = new HashMap<>();

    public MessageReplaceKeywords(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public String getReplacedMessage(
            String aClientId,
            String aMessage)
    {

        try
        {
            final Map<String, String> accMsgReplaceKeywords = getClientMsgReplaceKeywords(aClientId);
            final String              lMessage              = aMessage;

            if ((accMsgReplaceKeywords != null) && (!lMessage.isEmpty()))
            {
                final Set<String> lSearchKeys    = accMsgReplaceKeywords.keySet();
                String[]          lSearchKeyList = new String[lSearchKeys.size()];
                lSearchKeyList = lSearchKeys.toArray(lSearchKeyList);

                final Collection<String> lReplacementCollection = accMsgReplaceKeywords.values();
                String[]                 lReplacementLs         = new String[lReplacementCollection.size()];
                lReplacementLs = lReplacementCollection.toArray(lReplacementLs);

                final String lAlterdMessage = StringUtils.replaceEach(lMessage, lSearchKeyList, lReplacementLs);

                if (!lMessage.equals(lAlterdMessage))
                {
                    if (log.isDebugEnabled())
                        log.debug("After repalce keywords" + lMessage);
                    return lAlterdMessage;
                }
            }
        }
        catch (final Exception exp)
        {
            log.error("problem in message replace due to...", exp);
        }
        return null;
    }

    public Map<String, String> getClientMsgReplaceKeywords(
            String lClientId)
    {
        if (log.isInfoEnabled())
            log.info("msg replace check for ClientId=" + lClientId);

        if (!mClientReplaceKeywordInfo.isEmpty())
        {
            final ItextosClient lClient = new ItextosClient(lClientId);

            if (mClientReplaceKeywordInfo.get(lClient.getClientId()) != null)
                return mClientReplaceKeywordInfo.get(lClient.getClientId());

            if (mClientReplaceKeywordInfo.get(lClient.getAdmin()) != null)
                return mClientReplaceKeywordInfo.get(lClient.getAdmin());

            if (mClientReplaceKeywordInfo.get(lClient.getSuperAdmin()) != null)
                return mClientReplaceKeywordInfo.get(lClient.getSuperAdmin());
        }

        return null;
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // SELECT * FROM acc_msg_replace_keywords

        // Table : msg_replace_keywords

        final HashMap<String, Map<String, String>> lClientReplaceKeyowrdMap = new HashMap<>();

        while (aResultSet.next())
        {
            final String              lClientId       = CommonUtility.nullCheck(aResultSet.getString("cli_id"), true);
            final String              lReplaceKeyword = CommonUtility.nullCheck(aResultSet.getString("replace_keyword"), true);
            final String              lKeyword        = CommonUtility.nullCheck(aResultSet.getString("keyword"), true);

            final Map<String, String> lTempMap        = lClientReplaceKeyowrdMap.computeIfAbsent(lClientId, k -> new HashMap<>());

            lTempMap.put(lKeyword, lReplaceKeyword);
        }
        mClientReplaceKeywordInfo = lClientReplaceKeyowrdMap;
    }

}
