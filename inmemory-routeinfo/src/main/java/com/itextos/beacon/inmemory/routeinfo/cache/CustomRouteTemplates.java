package com.itextos.beacon.inmemory.routeinfo.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.ItextosClient;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class CustomRouteTemplates
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static Log                             log                     = LogFactory.getLog(CustomRouteTemplates.class);

    private final Map<String, Map<String, String>> mCustomRouteTemplateMap = new HashMap<>();

    public CustomRouteTemplates(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public Map<String, String> getCustomTemplateRouteInfo(
            String aClientId)
    {
        final ItextosClient lClient = new ItextosClient(aClientId);

        if (mCustomRouteTemplateMap.get(lClient.getClientId()) != null)
            return mCustomRouteTemplateMap.get(lClient.getClientId());

        if (mCustomRouteTemplateMap.get(lClient.getAdmin()) != null)
            return mCustomRouteTemplateMap.get(lClient.getAdmin());

        if (mCustomRouteTemplateMap.get(lClient.getSuperAdmin()) != null)
            return mCustomRouteTemplateMap.get(lClient.getSuperAdmin());

        return null;
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // SELECT sno,esmeaddr,pattern,most_used_pattern,sms_route FROM
        // acc_template_routing ORDER BY most_used_pattern

        // Table: template_based_routing

        final Map<String, Map<String, String>> lTempCustomRouteTemplates = new HashMap<>();

        while (aResultSet.next())
        {
            final String lClientId = CommonUtility.nullCheck(aResultSet.getString("cli_id"), true);
            final String lTemplate = CommonUtility.nullCheck(aResultSet.getString("template"), true);
            final String lRouteId  = CommonUtility.nullCheck(aResultSet.getString("route_id"), true);

            if ((lClientId.isEmpty()) || (lTemplate.isEmpty()) || (lRouteId.isEmpty()))
                continue;

            final Map<String, String> lTempMap = lTempCustomRouteTemplates.computeIfAbsent(lClientId, k -> new HashMap<>());

            lTempMap.put(lTemplate, lRouteId.toUpperCase());
        }
    }

}
