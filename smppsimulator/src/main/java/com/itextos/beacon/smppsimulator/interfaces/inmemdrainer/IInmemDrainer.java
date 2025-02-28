package com.itextos.beacon.smppsimulator.interfaces.inmemdrainer;

import java.util.List;

import com.itextos.beacon.platform.smppsimulator.util.ISmppInfo;

public interface IInmemDrainer
{

    void processInMemObjects(
            List<ISmppInfo> aList)
            throws Exception;

}
