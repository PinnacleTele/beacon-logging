package com.itextos.beacon.inmemory.clidlrpref;

import com.itextos.beacon.commonlib.constants.InterfaceType;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;

public class ClientDlrConfigUtil
{

    private ClientDlrConfigUtil()
    {}

    public static ClientDlrConfig getDlrQueryConfig(
            String aClientId,
            String aApp,
            InterfaceType aInterfaceType,
            String aExplitRequest)
    {
        final ClientDlrConfigCollection lClientDlrConfig = (ClientDlrConfigCollection) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.CLIENT_DLR_PREF);
        return lClientDlrConfig.getDlrQueryConfig(aClientId, aApp, aInterfaceType, aExplitRequest);
    }

    public static boolean getDlrQueryConfig(
            String aClientId,
            String aApp)
    {
        final ClientDlrConfigCollection lClientDlrConfig = (ClientDlrConfigCollection) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.CLIENT_DLR_PREF);
        return lClientDlrConfig.getDlrQueryConfig(aClientId, aApp);
    }

    public static ClientDlrConfig getDlrHandoverConfig(
            String aClientId,
            String aApp,
            InterfaceType aInterfaceType,
            boolean aExplitRequest)
    {
        final ClientDlrConfigCollection lClientDlrConfig = (ClientDlrConfigCollection) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.CLIENT_DLR_PREF);
        return lClientDlrConfig.getDlrHandoverConfig(aClientId, aApp, aInterfaceType, aExplitRequest);
    }

}
