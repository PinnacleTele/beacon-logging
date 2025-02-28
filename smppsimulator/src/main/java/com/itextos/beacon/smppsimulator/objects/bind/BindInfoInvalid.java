package com.itextos.beacon.smppsimulator.objects.bind;

import com.cloudhopper.smpp.SmppBindType;
import com.itextos.beacon.smppsimulator.objects.SmppRequestType;

public class BindInfoInvalid
        extends
        BindInfo
{

    BindInfoInvalid(
            String aInstanceId,
            String aClientId,
            SmppRequestType aRequestType,
            SmppBindType aBindType,
            String aBindId,
            String aServerIp,
            int aServerPort,
            String aSystemId,
            String aSourceIp,
            String aThreadName)
    {
        super(aInstanceId, aClientId, aRequestType, aBindType, aBindId, aServerIp, aServerPort, aSystemId, aSourceIp, aThreadName);
    }

}