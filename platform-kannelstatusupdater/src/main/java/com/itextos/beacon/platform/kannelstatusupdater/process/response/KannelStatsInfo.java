package com.itextos.beacon.platform.kannelstatusupdater.process.response;

public class KannelStatsInfo
{

    private final String routeID;
    private final int    timetaken; // -1 indicates connection failed.

    public KannelStatsInfo(
            String aRouteID,
            int aTimetaken)
    {
        super();
        routeID   = aRouteID;
        timetaken = aTimetaken;
    }

    public String getRouteID()
    {
        return routeID;
    }

    public int getTimetaken()
    {
        return timetaken;
    }

    @Override
    public String toString()
    {
        return "StatsInfo [routeID=" + routeID + ", timetaken=" + timetaken + "]";
    }

}