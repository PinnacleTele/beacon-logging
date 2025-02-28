package com.itextos.beacon.smpp.interfaces.inmemdrainer;

import java.util.List;

import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.platform.smpputil.ISmppInfo;
import com.itextos.beacon.smpp.dboperations.DbBindOperation;
import com.itextos.beacon.smpp.objects.SmppObjectType;

public class BindInfoValidInmemDrainer
        extends
        AbstractInmemDrainer
{

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final BindInfoValidInmemDrainer INSTANCE = new BindInfoValidInmemDrainer();

    }

    public static BindInfoValidInmemDrainer getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private BindInfoValidInmemDrainer()
    {
        // This has to be updated.
        super(SmppObjectType.BIND_INFO_VALID, 1000, TimerIntervalConstant.CARRIER_ERROR_INFO_REFRESH);
    }

    @Override
    public void processInMemObjects(
            List<ISmppInfo> aList)
            throws Exception
    {
        DbBindOperation.insertBindInfo(aList);
    }

}
