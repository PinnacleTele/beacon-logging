package com.itextos.beacon.inmemory.msgutil.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.ItextosClient;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class MessageSuffixPrefix
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log                 log                           = LogFactory.getLog(MessageSuffixPrefix.class);

    // TODO below country should be taken from config params.
    private static final String              DEFAULT_COUNTRY               = "india";
    private static final String              ALL_HEADERS                   = "all";
    private static final String              MSG_PREFIX                    = "msg_prefix";
    private static final String              MSG_SUFFIX                    = "msg_suffix";

    private Map<String, Map<String, String>> mClientWiseMsgPrefixSuffixMap = new HashMap<>();

    public MessageSuffixPrefix(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public String getClientMsgPrefix(
            String aClientId,
            String aCountry,
            String aHeader)
    {
        return getPrefixSufix(aClientId, aCountry, aHeader, true);
    }

    public String getClientMsgSuffix(
            String aClientId,
            String aCountry,
            String aHeader)
    {
        return getPrefixSufix(aClientId, aCountry, aHeader, false);
    }

    private String getPrefixSufix(
            String aClientId,
            String aCountry,
            String aHeader,
            boolean sIsPrefix)
    {
        final Map<String, String> lPrefixSuffixMap = getPrefixSuffixMap(aClientId, aCountry, aHeader);

        if (lPrefixSuffixMap == null)
            return null;

        if (sIsPrefix)
            return lPrefixSuffixMap.get(MSG_PREFIX);

        return lPrefixSuffixMap.get(MSG_SUFFIX);
    }

    private Map<String, String> getPrefixSuffixMap(
            String aClientId,
            String aCountry,
            String aHeader)
    {

        try
        {
            final ItextosClient lClient      = new ItextosClient(aClientId);
            String              lTempCountry = CommonUtility.nullCheck(aCountry, true).toLowerCase();
            final String        lTempHeader  = CommonUtility.nullCheck(aHeader, true).toLowerCase();

            lTempCountry = "".equals(lTempCountry) ? DEFAULT_COUNTRY : lTempCountry;

            Map<String, String> lPrefixSuffix = getPrefixSuffix(lClient.getClientId(), lTempCountry, lTempHeader);

            if (lPrefixSuffix != null)
                return lPrefixSuffix;

            lPrefixSuffix = getPrefixSuffix(lClient.getAdmin(), lTempCountry, lTempHeader);
            if (lPrefixSuffix != null)
                return lPrefixSuffix;

            return getPrefixSuffix(lClient.getSuperAdmin(), lTempCountry, lTempHeader);
        }
        catch (final Exception ignore)
        {
            log.warn("Exception while getting the Message Suffix ", ignore);
        }
        return null;
    }

    private Map<String, String> getPrefixSuffix(
            String aClientId,
            String aCountry,
            String aHeader)
    {
        final String              lKey = CommonUtility.combine(aClientId, aCountry, aHeader);
        final Map<String, String> lMap = mClientWiseMsgPrefixSuffixMap.get(lKey);

        if (lMap == null)
            return mClientWiseMsgPrefixSuffixMap.get(CommonUtility.combine(aClientId, aCountry, ALL_HEADERS));
        return lMap;
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        final Map<String, Map<String, String>> lClientWisePrefixSuffixMap = new HashMap<>();

        while (aResultSet.next())
        {
            final String lClientId  = CommonUtility.nullCheck(aResultSet.getString("cli_id"), true).toLowerCase();
            final String lCountery  = CommonUtility.nullCheck(aResultSet.getString("country"), true).toLowerCase();
            final String lHeader    = CommonUtility.nullCheck(aResultSet.getString("header"), true).toLowerCase();
            final String lMsgPrefix = CommonUtility.nullCheck(aResultSet.getString(MSG_PREFIX));
            final String lMsgSuffix = CommonUtility.nullCheck(aResultSet.getString(MSG_SUFFIX));

            if ((lClientId.isBlank()) || (lCountery.isBlank()) || (lHeader.isBlank()))
                continue;

            final Map<String, String> lSuffixPrefixMap = new HashMap<>();

            if (lMsgPrefix.length() != 0)
                lSuffixPrefixMap.put(MSG_PREFIX, lMsgPrefix);

            if (lMsgSuffix.length() != 0)
                lSuffixPrefixMap.put(MSG_SUFFIX, lMsgSuffix);

            lClientWisePrefixSuffixMap.put(CommonUtility.combine(lClientId, lCountery, lHeader), lSuffixPrefixMap);
        }

        if (!lClientWisePrefixSuffixMap.isEmpty())
            mClientWiseMsgPrefixSuffixMap = lClientWisePrefixSuffixMap;
    }

}