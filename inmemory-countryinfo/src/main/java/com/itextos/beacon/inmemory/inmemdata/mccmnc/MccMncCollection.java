package com.itextos.beacon.inmemory.inmemdata.mccmnc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class MccMncCollection
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private final Map<String, MccMncInfo> mMccMncData = new HashMap<>();

    public MccMncCollection(
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
            final MccMncInfo ci = getMccMncInfoFromDB(aResultSet);
            mMccMncData.put(ci.getPrefix(), ci);
        }
    }

    private static MccMncInfo getMccMncInfoFromDB(
            ResultSet aResultSet)
            throws SQLException
    {
      
        return new MccMncInfo(CommonUtility.nullCheck(aResultSet.getString("mcc"), true), CommonUtility.nullCheck(aResultSet.getString("mnc"), true),
                CommonUtility.nullCheck(aResultSet.getString("prefix"), true));
    }

    public MccMncInfo getMccMncData(
            String prefix)
    {
        return mMccMncData.get(prefix);
    }

}
