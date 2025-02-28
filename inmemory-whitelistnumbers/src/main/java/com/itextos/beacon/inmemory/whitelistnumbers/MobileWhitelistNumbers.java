package com.itextos.beacon.inmemory.whitelistnumbers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class MobileWhitelistNumbers
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log log                     = LogFactory.getLog(MobileWhitelistNumbers.class);
    private Set<String>      mMobileWhiteListNumbers = new HashSet<>();

    public MobileWhitelistNumbers(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public boolean isNumberWhitelisted(
            String aMobileNumber)
    {
        return mMobileWhiteListNumbers.contains(aMobileNumber);
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        final Set<String> lWhitelistNumbers = new HashSet<>();

        while (aResultSet.next())
            lWhitelistNumbers.add(aResultSet.getString("mnumber"));

        if (!lWhitelistNumbers.isEmpty())
            mMobileWhiteListNumbers = lWhitelistNumbers;
    }

}