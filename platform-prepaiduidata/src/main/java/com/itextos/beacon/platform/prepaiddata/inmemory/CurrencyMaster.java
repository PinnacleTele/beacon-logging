package com.itextos.beacon.platform.prepaiddata.inmemory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class CurrencyMaster
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private Map<String, CurrencyData> mCurrencyData = new HashMap<>();

    public CurrencyMaster(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public CurrencyData getCurrencyInfo(
            String aCurCode)
    {
        if (aCurCode != null)
            return mCurrencyData.get(aCurCode);
        return null;
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        final Map<String, CurrencyData> tempCurrencyData = new HashMap<>();
        while (aResultSet.next())
            tempCurrencyData.put(aResultSet.getString(1), new CurrencyData(aResultSet.getString(1), aResultSet.getString(2)));

        if (!tempCurrencyData.isEmpty())
            mCurrencyData = tempCurrencyData;
    }

}