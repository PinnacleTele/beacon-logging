package com.itextos.beacon.platform.prepaiddata.kannelstatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;

public class ReadKannelInfo
{

    private static final Log    log                 = LogFactory.getLog(ReadKannelInfo.class);

    public static final String  UNAVAILABLE_KANNELS = "UNAVAILABLE";
    public static final String  AVAILABLE_KANNELS   = "AVAILABLE";

    private static final String STORE_SIZE          = "storesize";
    private static final String IP_PORT             = "ip:port:statusport";
    private static final String AVAILABLE           = "available";
    private static final String LASTUPDATED         = "lastupdated";

    private ReadKannelInfo()
    {}

    public static Map<String, List<KannelInfo>> getRouteStatusFromInmemory(
            List<String> aOperator,
            List<String> aRouteList)
    {

        try
        {
            if (aOperator.isEmpty() && aRouteList.isEmpty())
                return getAllRoutes();
            return getSpecificRoutes(aOperator, aRouteList);
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the data from Redis.", e);
        }
        return new HashMap<>();
    }

    private static Map<String, List<KannelInfo>> getAllRoutes()
    {
        final Map<String, List<KannelInfo>> returnValue         = new HashMap<>();

        final List<KannelInfo>              lUnAvailableKannels = new ArrayList<>();
        final List<KannelInfo>              lAvailableKannels   = new ArrayList<>();

        final Set<String>                   lAllOperators       = KannelInfoLoader.getInstance().getAllOperators();

        for (final String operator : lAllOperators)
        {
            final List<String> allRoutes = KannelInfoLoader.getInstance().getRoutesForOperator(operator);

            for (final String routeId : allRoutes)
                try
                {
                    final Map<String, String> lKannelStatus = KannelInfoLoader.getInstance().getKannelStatus(routeId.toUpperCase());

                    final KannelInfo          ki            = getKannelInfo(operator, routeId, lKannelStatus);

                    if (!ki.isAvailable())
                    {
                        lUnAvailableKannels.add(ki);
                        continue;
                    }
                    lAvailableKannels.add(ki);
                }
                catch (final Exception e)
                {
                    log.error("Exeption while getting the route information for Operator '" + operator + "' Route '" + routeId + "'", e);
                }
        }

        Collections.sort(lUnAvailableKannels, new ListNameComparator());
        Collections.sort(lAvailableKannels, new ListStoresizeComparator());

        returnValue.put(UNAVAILABLE_KANNELS, lUnAvailableKannels);
        returnValue.put(AVAILABLE_KANNELS, lAvailableKannels);
        return returnValue;
    }

    private static Map<String, List<KannelInfo>> getSpecificRoutes(
            List<String> aOperator,
            List<String> aRouteList)
    {
        final Map<String, List<KannelInfo>> returnValue         = new HashMap<>();

        final List<KannelInfo>              lUnAvailableKannels = new ArrayList<>();
        final List<KannelInfo>              lAvailableKannels   = new ArrayList<>();

        final Set<String>                   lAllOperators       = KannelInfoLoader.getInstance().getAllOperators();

        for (final String operator : lAllOperators)
            if (aOperator.contains(operator))
            {
                final List<String> allRoutes = KannelInfoLoader.getInstance().getRoutesForOperator(operator);

                for (final String routeId : allRoutes)
                    try
                    {

                        if (aRouteList.isEmpty() || aRouteList.contains(routeId))
                        {
                            final Map<String, String> lKannelStatus = KannelInfoLoader.getInstance().getKannelStatus(routeId.toUpperCase());

                            final KannelInfo          ki            = getKannelInfo(operator, routeId, lKannelStatus);

                            if (!ki.isAvailable())
                            {
                                lUnAvailableKannels.add(ki);
                                continue;
                            }
                            lAvailableKannels.add(ki);
                        }
                    }
                    catch (final Exception e)
                    {
                        log.error("Exeption while getting the route information for Operator '" + operator + "' Route '" + routeId + "'", e);
                    }
            }

        Collections.sort(lUnAvailableKannels, new ListNameComparator());
        Collections.sort(lAvailableKannels, new ListStoresizeComparator());

        returnValue.put(UNAVAILABLE_KANNELS, lUnAvailableKannels);
        returnValue.put(AVAILABLE_KANNELS, lAvailableKannels);
        return returnValue;
    }

    private static KannelInfo getKannelInfo(
            String aOperator,
            String aRouteId,
            Map<String, String> aKannelStatus)
    {
        String[] lIpData =
        { "-1", "-1", "-1", "-1" };

        if (aKannelStatus.isEmpty())
            return new KannelInfo(aOperator, aRouteId, false, -1, lIpData[0], lIpData[1], lIpData[3], "Unavailable");

        try
        {
            lIpData = getIpData(aKannelStatus.get(IP_PORT));
            return new KannelInfo(aOperator, aRouteId, CommonUtility.isEnabled(aKannelStatus.get(AVAILABLE)), Integer.parseInt(aKannelStatus.get(STORE_SIZE)), lIpData[0], lIpData[1], lIpData[3],
                    aKannelStatus.get(LASTUPDATED));
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the IP Port details for Operator '" + aOperator + "' Route '" + aRouteId + "'");
            return new KannelInfo(aOperator, aRouteId, false, -1, lIpData[0], lIpData[1], lIpData[3], "Unavailable");
        }
    }

    private static String[] getIpData(
            String aString)
    {
        return aString.split(":");
    }

}
