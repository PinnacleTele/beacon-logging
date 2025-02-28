package com.itextos.beacon.smpp.interfaces.inmemdrainer;

import java.util.List;

import com.itextos.beacon.platform.smpputil.ISmppInfo;

public interface IInmemDrainer
{

    void processInMemObjects(
            List<ISmppInfo> aList)
            throws Exception;

}
