package com.itextos.beacon.inmemory.governmentheaders;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Constants;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class GovtHeaderExcludes
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log log                 = LogFactory.getLog(GovtHeaderExcludes.class);

    private Set<String>      mGovtExcludeHeaders = new HashSet<>();

    public GovtHeaderExcludes(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public boolean isRouteExclude(
            String aKey)
    {
        return mGovtExcludeHeaders.contains(aKey);
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        final Set<String> loadGovtExcludeHeaders = new HashSet<>();

        while (aResultSet.next())
        {
            String lClientId = CommonUtility.nullCheck(aResultSet.getString("cli_id"), true);
            String lCarrier  = CommonUtility.nullCheck(aResultSet.getString("carrier"), true);
            String lCircle   = CommonUtility.nullCheck(aResultSet.getString("circle"), true);

            lClientId = ("".equals(lClientId) ? Constants.NULL_STRING : lClientId);
            lCarrier  = ("".equals(lCarrier) ? Constants.NULL_STRING : lCarrier.toUpperCase());
            lCircle   = ("".equals(lCircle) ? Constants.NULL_STRING : lCircle.toUpperCase());

            loadGovtExcludeHeaders.add(CommonUtility.combine(lClientId, lCarrier, lCircle));
        }

        if (!loadGovtExcludeHeaders.isEmpty())
            mGovtExcludeHeaders = loadGovtExcludeHeaders;
    }

}
