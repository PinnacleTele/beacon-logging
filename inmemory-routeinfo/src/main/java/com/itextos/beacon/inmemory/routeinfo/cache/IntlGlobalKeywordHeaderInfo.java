package com.itextos.beacon.inmemory.routeinfo.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Constants;
import com.itextos.beacon.commonlib.pattern.PatternCache;
import com.itextos.beacon.commonlib.pattern.PatternCheckCategory;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class IntlGlobalKeywordHeaderInfo
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static Log   log                = LogFactory.getLog(IntlGlobalKeywordHeaderInfo.class);

    private List<String> mKeywordHeaderList = new ArrayList<>();

    public IntlGlobalKeywordHeaderInfo(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public boolean isKeywordHeaderMatchs(
            String aMessage,
            String aHeader)
    {
        final String lMessage = aMessage;
        final String lHeader  = aHeader;

        for (final String key : mKeywordHeaderList)
        {
            final String[] splittedKeys = StringUtils.split(key, Constants.DEFAULT_CONCATENATE_CHAR);

            if (log.isDebugEnabled())
                log.debug("Keyword from DB:" + splittedKeys[0] + " Header from DB:" + splittedKeys[1]);
            // both should match

            if (PatternCache.getInstance().isPatternMatch(PatternCheckCategory.TEMPLATE_CHECK, splittedKeys[0], lMessage))
                return true;
        }

        for (final String key : mKeywordHeaderList)
        {
            final String[] splittedKeys = StringUtils.split(key, Constants.DEFAULT_CONCATENATE_CHAR);

            if (PatternCache.getInstance().isPatternMatch(PatternCheckCategory.TEMPLATE_CHECK, splittedKeys[0], lMessage) && (splittedKeys[1].isBlank()))
            {
                if (log.isDebugEnabled())
                    log.debug("keyword and any header - true");
                return true;
            }
        }

        for (final String key : mKeywordHeaderList)
        {
            final String[] splittedKeys = StringUtils.split(key, Constants.DEFAULT_CONCATENATE_CHAR);
            // any keyword and header
            if ((splittedKeys[0].equalsIgnoreCase(CommonUtility.DOT_STAR) && splittedKeys[1].equalsIgnoreCase(lHeader)))
                return true;
        }

        return false;
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // select * from intl_global_template

        // Table : intl_global_header_template

        final List<String> lTempKeywordHeaderList = new ArrayList<>();

        while (aResultSet.next())
        {
            final String lKeyword = CommonUtility.getDotStarString(aResultSet.getString("keywords"));
            final String lHeader  = CommonUtility.nullCheck(aResultSet.getString("header"), true);

            lTempKeywordHeaderList.add(CommonUtility.combine(lKeyword, lHeader));
        }

        mKeywordHeaderList = lTempKeywordHeaderList;
    }

}