package com.itextos.beacon.inmemory.inmemdata.country;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class CountryInfoCollection
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private final Map<String, CountryInfo> mCountryData = new HashMap<>();

    public CountryInfoCollection(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {

        while (aResultSet.next())
        {
            final CountryInfo ci = getCountryInfoFromDB(aResultSet);
            mCountryData.put(ci.getCountryCode(), ci);
        }
    }

    private static CountryInfo getCountryInfoFromDB(
            ResultSet aResultSet)
            throws SQLException
    {
        final String[] lSplit       = CommonUtility.split(aResultSet.getString("other_mobile_length"), ",");
        int[]          otherLengths = null;

        if (lSplit != null)
        {
            otherLengths = new int[lSplit.length];
            int index = 0;
            for (final String s : lSplit)
                otherLengths[index++] = Integer.valueOf(s);
        }
        return new CountryInfo(CommonUtility.nullCheck(aResultSet.getString("country_code_iso_3"), true), CommonUtility.nullCheck(aResultSet.getString("country"), true),
                CommonUtility.nullCheck(aResultSet.getString("country_short_name"), true), CommonUtility.nullCheck(aResultSet.getString("country_code_iso_2"), true),
                CommonUtility.getInteger(aResultSet.getString("country_code_iso_numeric")), CommonUtility.getInteger(aResultSet.getString("dial_in_code")),
                CommonUtility.nullCheck(aResultSet.getString("dial_in_code_full"), true), CommonUtility.getInteger(aResultSet.getString("default_mobile_length")), otherLengths,
                CommonUtility.getInteger(aResultSet.getString("min_mobile_length")), CommonUtility.getInteger(aResultSet.getString("max_mobile_length")),
                CommonUtility.nullCheck(aResultSet.getString("country_currency"),true));
    }

    public CountryInfo getCountryData(
            String aCountryCode)
    {
        return mCountryData.get(aCountryCode);
    }

}
