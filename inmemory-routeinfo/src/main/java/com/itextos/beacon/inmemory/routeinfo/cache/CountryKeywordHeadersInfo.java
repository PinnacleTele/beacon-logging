package com.itextos.beacon.inmemory.routeinfo.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;
import com.itextos.beacon.inmemory.routeinfo.util.IntlRUtils;

public class CountryKeywordHeadersInfo
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log                                    log                      = LogFactory.getLog(CountryKeywordHeadersInfo.class);

    private Map<String, Map<String, CountryKeywordHeaderRoute>> mCountryKeywordHeaderMap = new HashMap<>();

    public CountryKeywordHeadersInfo(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public CountryKeywordHeaderRoute getPriorityRouteID(
            String aCountry,
            String aMessage,
            String aHeader)
    {
        aCountry = CommonUtility.nullCheck(aCountry, true).toLowerCase();
        aHeader  = CommonUtility.nullCheck(aHeader, true).toLowerCase();

        String                    lKey  = CommonUtility.combine(aCountry, aHeader);
        CountryKeywordHeaderRoute value = (CountryKeywordHeaderRoute) IntlRUtils.verifyKeywordInMessage(mCountryKeywordHeaderMap.get(lKey), aMessage, false);

        if (value != null)
            return getClonedObject(value, -1, lKey);

        lKey  = CommonUtility.combine(aCountry, CommonUtility.ANY_VALUE);
        value = (CountryKeywordHeaderRoute) IntlRUtils.verifyKeywordInMessage(mCountryKeywordHeaderMap.get(lKey), aMessage, false);

        if (value != null)
            return getClonedObject(value, -2, lKey);

        lKey  = CommonUtility.combine(aCountry, aHeader);
        value = (CountryKeywordHeaderRoute) IntlRUtils.verifyKeywordInMessage(mCountryKeywordHeaderMap.get(lKey), aMessage, true);

        if (value != null)
            return getClonedObject(value, -3, lKey);

        lKey  = CommonUtility.combine(CommonUtility.ANY_VALUE, aHeader);
        value = (CountryKeywordHeaderRoute) IntlRUtils.verifyKeywordInMessage(mCountryKeywordHeaderMap.get(lKey), aMessage, false);

        if (value != null)
            return getClonedObject(value, -4, lKey);

        lKey  = CommonUtility.combine(CommonUtility.REST_OF_THE_WORLD, aHeader);
        value = (CountryKeywordHeaderRoute) IntlRUtils.verifyKeywordInMessage(mCountryKeywordHeaderMap.get(lKey), aMessage, false);

        if (value != null)
            return getClonedObject(value, -5, lKey);

        return null;
    }

    private static CountryKeywordHeaderRoute getClonedObject(
            CountryKeywordHeaderRoute aValue,
            int aLogicId,
            String aKey)
    {
        final CountryKeywordHeaderRoute returnValue = aValue.clone();
        returnValue.setLogicId(aLogicId);
        returnValue.setDerivedKey(aKey);
        return returnValue;
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // select * from intl_country_template

        // Table : intl_country_header_template

        final Map<String, Map<String, CountryKeywordHeaderRoute>> lKeywordHeaderMap = new HashMap<>();

        while (aResultSet.next())
        {
            final String lCountry  = CommonUtility.nullCheck(aResultSet.getString("country"), true);
            final String lKeywords = CommonUtility.nullCheck(aResultSet.getString("keywords"), true);
            final String lHeader   = CommonUtility.nullCheck(aResultSet.getString("header"), true);
            final String lPriority = CommonUtility.nullCheck(aResultSet.getString("priority"), true);
            final String lRouteId  = CommonUtility.nullCheck(aResultSet.getString("route_id"), true);

            if (!"".equals(lRouteId))
            {
                final CountryKeywordHeaderRoute              value    = new CountryKeywordHeaderRoute(lCountry, lKeywords, lHeader, lPriority, lRouteId);
                final String                                 lKey     = CommonUtility.combine(lCountry.toLowerCase(), lHeader.toLowerCase());
                final Map<String, CountryKeywordHeaderRoute> lTempMap = lKeywordHeaderMap.computeIfAbsent(lKey, k -> new HashMap<>());
                lTempMap.put(lKeywords, value);
            }
        }

        mCountryKeywordHeaderMap = lKeywordHeaderMap;
    }

}
