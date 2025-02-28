package com.itextos.beacon.inmemory.carrierhandover;

public class RouteKannelInfo
{

    private String  routeId;
    private String  kannelIp;
    private String  kannelPort;
    private String  featureCode;
    private String  urlTemplate;
    private String  response;
    private String  smscId;
    private boolean isPrefix;
    private String  prefix;
    private String  routeType;
    private boolean isDltRoute;
    private boolean isDummyRoute;
    private String  carrierFullDn;

    public RouteKannelInfo(
            String aRouteId,
            String aKannelIp,
            String aKannelPort,
            String aFeatureCode,
            String aUrlTemplate,
            String aResponse,
            String aSmscId,
            boolean aIsPrefix,
            String aPrefix,
            String aRouteType,
            boolean aIsDltRoute,
            boolean aIsDummyRoute,
            String aCarrierFullDn)
    {
        super();
        routeId       = aRouteId;
        kannelIp      = aKannelIp;
        kannelPort    = aKannelPort;
        featureCode   = aFeatureCode;
        urlTemplate   = aUrlTemplate;
        response      = aResponse;
        smscId        = aSmscId;
        isPrefix      = aIsPrefix;
        prefix        = aPrefix;
        routeType     = aRouteType;
        isDltRoute    = aIsDltRoute;
        isDummyRoute  = aIsDummyRoute;
        carrierFullDn = aCarrierFullDn;
    }

    public String getRouteId()
    {
        return routeId;
    }

    public void setRouteId(
            String aRouteId)
    {
        routeId = aRouteId;
    }

    public String getKannelIp()
    {
        return kannelIp;
    }

    public void setKannelIp(
            String aKannelIp)
    {
        kannelIp = aKannelIp;
    }

    public String getKannelPort()
    {
        return kannelPort;
    }

    public void setKannelPort(
            String aKannelPort)
    {
        kannelPort = aKannelPort;
    }

    public String getFeatureCode()
    {
        return featureCode;
    }

    public void setFeatureCode(
            String aFeatureCode)
    {
        featureCode = aFeatureCode;
    }

    public String getUrlTemplate()
    {
        return urlTemplate;
    }

    public void setUrlTemplate(
            String aUrlTemplate)
    {
        urlTemplate = aUrlTemplate;
    }

    public String getResponse()
    {
        return response;
    }

    public void setResponse(
            String aResponse)
    {
        response = aResponse;
    }

    public String getSmscId()
    {
        return smscId;
    }

    public void setSmscId(
            String aSmscId)
    {
        smscId = aSmscId;
    }

    public boolean isPrefix()
    {
        return isPrefix;
    }

    public void setPrefix(
            boolean aIsPrefix)
    {
        isPrefix = aIsPrefix;
    }

    public String getPrefix()
    {
        return prefix;
    }

    public void setPrefix(
            String aPrefix)
    {
        prefix = aPrefix;
    }

    public String getRouteType()
    {
        return routeType;
    }

    public void setRouteType(
            String aRouteType)
    {
        routeType = aRouteType;
    }

    public boolean isDltRoute()
    {
        return isDltRoute;
    }

    public void setDltRoute(
            boolean aIsDltRoute)
    {
        isDltRoute = aIsDltRoute;
    }

    public boolean isDummyRoute()
    {
        return isDummyRoute;
    }

    public void setDummyRoute(
            boolean aIsDummyRoute)
    {
        isDummyRoute = aIsDummyRoute;
    }

    public String getCarrierFullDn()
    {
        return carrierFullDn;
    }

    public void setCarrierFullDn(
            String aCarrierFullDn)
    {
        carrierFullDn = aCarrierFullDn;
    }

    @Override
    public String toString()
    {
        return "RouteKannelInfo [routeId=" + routeId + ", kannelIp=" + kannelIp + ", kannelPort=" + kannelPort + ", featureCode=" + featureCode + ", urlTemplate=" + urlTemplate + ", response="
                + response + ", smscId=" + smscId + ", isPrefix=" + isPrefix + ", prefix=" + prefix + ", routeType=" + routeType + ", isDltRoute=" + isDltRoute + ", isDummyRoute=" + isDummyRoute
                + ", carrierFullDn=" + carrierFullDn + "]";
    }

}