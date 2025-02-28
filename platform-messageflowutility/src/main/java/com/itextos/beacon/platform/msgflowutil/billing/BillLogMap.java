package com.itextos.beacon.platform.msgflowutil.billing;

public class BillLogMap
{

    public static final String      DEFAULT_ESME_ADDRESS    = "0";
    public static final String      DEFAULT_BILLING_JNDI_ID = "";

    private static final BillLogMap DEFAULT_TRACE_MAP       = new BillLogMap(DEFAULT_ESME_ADDRESS, DEFAULT_BILLING_JNDI_ID, "");

    private final String            clientId;
    private final String            jndiID;
    private final String            tableSuffix;

    public static BillLogMap getDefaultMap()
    {
        return DEFAULT_TRACE_MAP;
    }

    public BillLogMap(
            String aClientId,
            String aJndiInfo,
            String aTableSuffix)
    {
        clientId    = aClientId;
        jndiID      = aJndiInfo;
        tableSuffix = aTableSuffix.equals("") ? "" : "_" + aTableSuffix;
    }

    public String getClientId()
    {
        return clientId;
    }

    public String getJndiID()
    {
        return jndiID;
    }

    public String getTableSuffix()
    {
        return tableSuffix;
    }

    @Override
    public String toString()
    {
        return "BillLogMap [clientId=" + clientId + ", jndiID=" + jndiID + ", tableSuffix=" + tableSuffix + "]";
    }

}