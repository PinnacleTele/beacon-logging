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

public class RouteConfiguration
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static Log                   log               = LogFactory.getLog(RouteConfiguration.class);

    private Map<String, String>          mDefaultHeaderMap = new HashMap<>();
    private Map<String, RouteConfigInfo> mRouteConfigMap   = new HashMap<>();

    public RouteConfiguration(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public RouteConfigInfo getRouteConfig(
            String aRouteId)
    {
        return mRouteConfigMap.get(aRouteId);
    }

    public boolean isRoutePresentInRouteConfig(
            String aRouteId)
    {
        return mRouteConfigMap.containsKey(aRouteId);
    }

    public String getDefaultHeaderMap(
            String aRouteId)
    {
        return mDefaultHeaderMap.get(aRouteId);
    }

    public boolean isDefaultHeaderAvailable(
            String aRouteId)
    {
        return mDefaultHeaderMap.containsKey(aRouteId);
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // Select * from route_config

        // Table : route_configuration
        final Map<String, RouteConfigInfo> lTempRouteConfigMap   = new HashMap<>();
        final Map<String, String>          lTempRouteBasedHeader = new HashMap<>();

        while (aResultSet.next())
        {
            final String          lHeaderWhitelisted = aResultSet.getString("header_whitelisted");
            final String          lRouteType         = aResultSet.getString("route_type");
            final boolean         isPrefix           = CommonUtility.isEnabled(aResultSet.getString("isprefix"));
            final String          lPrefix            = aResultSet.getString("prefix");
            final String          lSmscId            = aResultSet.getString("smscid");
            final boolean         isTxnRoute         = CommonUtility.isEnabled(aResultSet.getString("is_txn_route"));
            final boolean         isPromoRoute       = CommonUtility.isEnabled(aResultSet.getString("is_promo_route"));
            final boolean         isIntlRoute        = CommonUtility.isEnabled(aResultSet.getString("is_intl_route"));
            final boolean         isDummyRoute       = CommonUtility.isEnabled(aResultSet.getString("is_dummy_route"));
            final String          lDtimeFormat       = aResultSet.getString("dtime_format");
            final String          lCarrierFullDN     = aResultSet.getString("carrier_full_dn");
            final String          lPromoHeaderType   = aResultSet.getString("promo_header_type");
            final String          lPromoHeader       = CommonUtility.nullCheck(aResultSet.getString("promo_header"), true);
            final String          lRouteId           = aResultSet.getString("route_id");
            final String          lTelemarketerId           = getValueAsString(aResultSet,"telemarkater_id");
            final String          lTelemarketerTLVOption           = getValueAsString(aResultSet,"telemarketer_tlv_option");

            final RouteConfigInfo lRouteInfo         = new RouteConfigInfo(lHeaderWhitelisted, lRouteType, lPrefix, isPrefix, lSmscId, isTxnRoute, isPromoRoute, isIntlRoute, isDummyRoute,
                    lDtimeFormat, lCarrierFullDN, lPromoHeaderType, lRouteId, lPromoHeader,lTelemarketerId,lTelemarketerTLVOption);

            if (!lPromoHeader.isEmpty() && (lPromoHeaderType != null) && lPromoHeaderType.equalsIgnoreCase("0"))
                lTempRouteBasedHeader.put(lRouteId, lPromoHeader);

            lTempRouteConfigMap.put(lRouteId, lRouteInfo);
        }
        mDefaultHeaderMap = lTempRouteBasedHeader;

        mRouteConfigMap   = lTempRouteConfigMap;
    }

	private String getValueAsString(ResultSet aResultSet,String columnName) {
		
		try {
		return aResultSet.getString(columnName);
		}catch(Exception e) {
		
			return null;
		}
		
	}

}
