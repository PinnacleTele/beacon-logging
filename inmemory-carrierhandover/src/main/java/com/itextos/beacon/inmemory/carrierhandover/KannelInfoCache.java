package com.itextos.beacon.inmemory.carrierhandover;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class KannelInfoCache
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log             log            = LogFactory.getLog(KannelInfoCache.class);

    private Map<String, RouteKannelInfo> mKannelInfoMap = new HashMap<>();
    private Map<String, String>          mDefaultHeader = new HashMap<>(); // TODO What is the use?

    public KannelInfoCache(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public RouteKannelInfo getDeliveryRouteInfo(
            String aRouteId,
            String aFeatureCode)
    {
        return mKannelInfoMap.get(CommonUtility.combine(aRouteId, aFeatureCode));
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // SELECT
        // promo_senderid_type,promo_senderid,a.route,Kannel_ip,Kannel_Port,smscid,isprefix,prefix,feature_cd,url_template,response,route_type,kannel_type,is_dlt_route,is_dummy_route,carrier_full_dn,connect_tanla_yn
        // FROM kannel_template a,route_config b WHERE a.route=b.route

        // Table : kannel_url_config, route_configuration

        final Map<String, RouteKannelInfo> lKannelInfoMap = new HashMap<>();
        final Map<String, String>          lDefaultHeader = new HashMap<>();

        while (aResultSet.next())
        {
            final String          lRouteId         = aResultSet.getString("route_id");
            final String          lFeatureCode     = aResultSet.getString("feature_cd");
            final RouteKannelInfo rki              = new RouteKannelInfo(lRouteId, aResultSet.getString("kannel_ip"), aResultSet.getString("kannel_port"), lFeatureCode,
                    aResultSet.getString("kannel_url"), aResultSet.getString("response"), aResultSet.getString("smscid"), CommonUtility.isTrue(aResultSet.getString("isprefix")),
                    aResultSet.getString("prefix"), aResultSet.getString("route_type"), CommonUtility.isTrue(aResultSet.getString("is_dlt_route")),
                    CommonUtility.isTrue(aResultSet.getString("is_dummy_route")), CommonUtility.nullCheck(aResultSet.getString("carrier_full_dn"), true));

            final String          lPromoHeaderType = aResultSet.getString("promo_header_type");
            final String          lPromoHeader     = aResultSet.getString("promo_header");

            if ((lPromoHeaderType != null) && lPromoHeaderType.equals("0") && (lPromoHeader != null))
                lDefaultHeader.put(lRouteId, lPromoHeader);
            lKannelInfoMap.put(CommonUtility.combine(lRouteId, lFeatureCode), rki);
        }

        mKannelInfoMap = lKannelInfoMap;
        mDefaultHeader = lDefaultHeader;
    }

}
