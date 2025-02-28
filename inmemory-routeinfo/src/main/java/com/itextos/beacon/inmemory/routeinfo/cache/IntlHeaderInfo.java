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

public class IntlHeaderInfo
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static Log  log             = LogFactory.getLog(IntlHeaderInfo.class);

    Map<String, String> mIntlHeaderInfo = new HashMap<>();

    public IntlHeaderInfo(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public String getIntlHeaderInfo(
            String aCountry)
    {
        return mIntlHeaderInfo.get(aCountry.toUpperCase());
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // Table : intl_country_header_info

        final Map<String, String> lTempHeaderInfoMap = new HashMap<>();

        while (aResultSet.next())
        {
            final String lCountry = CommonUtility.nullCheck(aResultSet.getString("country"), true).toUpperCase();
            final String lRegEx   = CommonUtility.nullCheck(aResultSet.getString("regex"), true);

            lTempHeaderInfoMap.put(lCountry, lRegEx);
        }

        mIntlHeaderInfo = lTempHeaderInfoMap;
    }

}
