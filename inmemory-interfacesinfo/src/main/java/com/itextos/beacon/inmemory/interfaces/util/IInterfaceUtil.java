package com.itextos.beacon.inmemory.interfaces.util;

import com.itextos.beacon.inmemory.interfaces.cache.GenericResponse;
import com.itextos.beacon.inmemory.interfaces.cache.InterfaceSMSTemplate;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;

public class IInterfaceUtil
{

    private IInterfaceUtil()
    {}

    public static String getInterfaceSMSTeamplate(
            String aClientId,
            String aTemplateId)
    {
        final InterfaceSMSTemplate lSmsTemplate = (InterfaceSMSTemplate) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.INTERFACE_MSG_TEMPLATE);
        return lSmsTemplate.getInterfaceMsgTemplate(aClientId, aTemplateId);
    }

    public static GenericResponse getGenericResponse()
    {
        final GenericResponse lGenericResp = (GenericResponse) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.JAPI_GENERIC_RESPONSE);
        return lGenericResp;
    }

}
