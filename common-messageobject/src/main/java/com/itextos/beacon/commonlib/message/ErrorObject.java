package com.itextos.beacon.commonlib.message;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.InterfaceGroup;
import com.itextos.beacon.commonlib.constants.InterfaceType;
import com.itextos.beacon.commonlib.constants.MessagePriority;
import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.commonlib.constants.RouteType;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;

public class ErrorObject
        extends
        BaseMessage
{

    private static final long serialVersionUID = -9195548432339659158L;

    ErrorObject(
            ClusterType aClusterType,
            InterfaceType aInterfaceType,
            InterfaceGroup aInterfaceGroup,
            MessageType aMessageType,
            MessagePriority aMessagePriority,
            RouteType aRouteType) throws ItextosRuntimeException
    {
        super(aClusterType, aInterfaceType, aInterfaceGroup, aMessageType, aMessagePriority, aRouteType, "ErrorObject");
    }

}