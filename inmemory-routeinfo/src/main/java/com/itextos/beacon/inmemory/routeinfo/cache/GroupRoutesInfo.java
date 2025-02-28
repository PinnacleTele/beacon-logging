package com.itextos.beacon.inmemory.routeinfo.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.commonlib.constants.RouteConstants;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;
import com.itextos.beacon.inmemory.routeinfo.util.RouteUtil;

public class GroupRoutesInfo
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log          log                = LogFactory.getLog(GroupRoutesInfo.class);

    private Map<String, List<String>> mGroupRouteInfoMap = new HashMap<>();

    public GroupRoutesInfo(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public boolean isRouteGroupAvailable(
            String aRouteGroup)
    {
        return mGroupRouteInfoMap.containsKey(aRouteGroup);
    }

    public List<String> getRouteListFromGroup(
            String aRouteGroup)
    {
        return mGroupRouteInfoMap.get(aRouteGroup);
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // SELECT a.group,a.route,ratio,b.msg_type,is_intl_group FROM group_routes_ratio
        // a,group_route_config b,route_config c WHERE a.group=b.group AND
        // a.route=c.route";

        // Table : route_group_config, group_routes_ratio, route_configuration
        final Map<String, List<String>> lTempGroupRouteMap = new HashMap<>();

        while (aResultSet.next())
        {
            final String lRouteGroup  = aResultSet.getString("group");
            final String lRouteId     = aResultSet.getString("route_id");
            final String lMsgType     = aResultSet.getString("msg_type");
            final String lIsintlgroup = aResultSet.getString("is_intl_group");
            final int    lRouteRatio  = aResultSet.getInt("ratio");

            processRouteGroups(lRouteId, lIsintlgroup, lRouteGroup, lRouteRatio, lMsgType, lTempGroupRouteMap);
        }

        mGroupRouteInfoMap = lTempGroupRouteMap;
    }

    private static void processRouteGroups(
            String aRouteId,
            String aIsIntlGroup,
            String aRouteGroup,
            int aRouteRatio,
            String aMsgType,
            Map<String, List<String>> aTempGroupRouteMap)
    {
        List<String>          lRoutelist              = null;
        final RouteConfigInfo lRouteinfo              = RouteUtil.getRouteConfiguration(aRouteId);
        boolean               isNeedtoAddRoutetoGroup = true;

        if (aRouteId.equalsIgnoreCase(RouteConstants.EXPIRED) || aRouteId.equalsIgnoreCase(RouteConstants.ECONOMY) || aRouteId.equalsIgnoreCase(RouteConstants.DUMMY)
                || aRouteId.equalsIgnoreCase(RouteConstants.VOICE) || !StringUtils.isAlphanumeric(aRouteId) || !RouteUtil.isRouteAvailable(aRouteId))
            isNeedtoAddRoutetoGroup = false;

        if (isNeedtoAddRoutetoGroup)
        {

            if ("1".equals(aIsIntlGroup))
            {
                final boolean isINTLRoute = lRouteinfo.isIntlRoute();

                if (isINTLRoute)
                {

                    if (aTempGroupRouteMap.containsKey(aRouteGroup))
                        lRoutelist = aTempGroupRouteMap.get(aRouteGroup);
                    else
                    {
                        lRoutelist = new ArrayList<>();
                        aTempGroupRouteMap.put(aRouteGroup, lRoutelist);
                    }

                    for (int i = 0; i < aRouteRatio; i++)
                        lRoutelist.add(aRouteId);
                }
            }

            if (aMsgType.equals(MessageType.TRANSACTIONAL.getKey()))
            {
                final boolean isTransRoute = lRouteinfo.isTxnRoute();
                aRouteGroup += MessageType.TRANSACTIONAL.getKey();

                if (isTransRoute)
                {

                    if (aTempGroupRouteMap.containsKey(aRouteGroup))
                        lRoutelist = aTempGroupRouteMap.get(aRouteGroup);
                    else
                    {
                        lRoutelist = new ArrayList<>();
                        aTempGroupRouteMap.put(aRouteGroup, lRoutelist);
                    }

                    for (int i = 0; i < aRouteRatio; i++)
                        lRoutelist.add(aRouteId);
                }
            }

            if (aMsgType.equals(MessageType.PROMOTIONAL.getKey()))
            {
                aRouteGroup += MessageType.PROMOTIONAL.getKey();

                final boolean isPromoRoute = lRouteinfo.isPromoRoute();

                if (isPromoRoute)
                {

                    if (aTempGroupRouteMap.containsKey(aRouteGroup))
                        lRoutelist = aTempGroupRouteMap.get(aRouteGroup);
                    else
                    {
                        lRoutelist = new ArrayList<>();
                        aTempGroupRouteMap.put(aRouteGroup, lRoutelist);
                    }

                    for (int i = 0; i < aRouteRatio; i++)
                        lRoutelist.add(aRouteId);
                }
            }
        }
    }

}
