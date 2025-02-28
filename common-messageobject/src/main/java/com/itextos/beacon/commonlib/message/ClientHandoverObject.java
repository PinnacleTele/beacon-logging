package com.itextos.beacon.commonlib.message;

import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;

public class ClientHandoverObject
        extends
        BaseMessage
{

    private static final long serialVersionUID = -4398747152379260881L;

    public ClientHandoverObject(
            DeliveryObject aDeliveryObject) throws ItextosRuntimeException
    {
        super(aDeliveryObject.getClusterType(), aDeliveryObject.getInterfaceType(), aDeliveryObject.getInterfaceGroupType(), aDeliveryObject.getMessageType(), aDeliveryObject.getMessagePriority(),
                aDeliveryObject.getMessageRouteType(), "ClientHandoverObject");
    }

    public ClientHandoverObject(
            String aCompleteJsonString)
            throws Exception
    {
        super(aCompleteJsonString, "ClientHandoverObject");
    }

    public ClientHandoverObject getClonedDeliveryObject()
    {
        return (ClientHandoverObject) super.getClonedObject();
    }

}