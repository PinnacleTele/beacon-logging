package com.itextos.beacon.inmemory.intlrouteinfo.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class MccMncRoutes
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    // CUSTOMER_INTL_CREDITS
    private static final Log          log                     = LogFactory.getLog(MccMncRoutes.class);

    private Map<String, IntlRouteConfigInfo> mMccMncRoutes = new HashMap<>();

    public MccMncRoutes(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public IntlRouteConfigInfo getMccMncRoute(
            String aClientId,
            String aCountry,
            String mcc,
            String mnc)
    {
    	log.debug("IntlRouteConfigInfo Searching Key : "+ CommonUtility.combine(aClientId, aCountry,mcc,mnc));
        return mMccMncRoutes.get(CommonUtility.combine(aClientId, aCountry,mcc,mnc));
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // Table Name : client_intl_rates
        final Map<String, IntlRouteConfigInfo> lMccMncRoutes = new HashMap<>();

        while (aResultSet.next())
        {
            final String       lClientId          = CommonUtility.nullCheck(aResultSet.getString("cli_id"), true);
            final String       lCountry           = CommonUtility.nullCheck(aResultSet.getString("country"), true);
            final String       lMcc           = CommonUtility.nullCheck(aResultSet.getString("mcc"), true);
            final String       lMnc           = CommonUtility.nullCheck(aResultSet.getString("mnc"), true);
            final String       lrouteid           = CommonUtility.nullCheck(aResultSet.getString("route_id"), true);

         
            final IntlRouteConfigInfo lMccMncRoute      = new IntlRouteConfigInfo("Other", lCountry, "-1", null, "15", "15", "EC", lrouteid, "", "");

            lMccMncRoutes.put(CommonUtility.combine(lClientId, lCountry.toUpperCase(),lMcc,lMnc), lMccMncRoute);
        }

        if (!lMccMncRoutes.isEmpty())
            mMccMncRoutes = lMccMncRoutes;
    }

    
    @Override
    public String toString() {
    	
    	return mMccMncRoutes.toString();
    }
}