package com.itextos.beacon.smppsimulator.interfaces.inmemdrainer;

import java.util.List;

import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.platform.smppsimulator.util.ISmppInfo;
import com.itextos.beacon.smppsimulator.dboperations.DbBindOperation;
import com.itextos.beacon.smppsimulator.objects.SmppObjectType;

public class BindInfoInvalidInmemDrainer
        extends
        AbstractInmemDrainer
{

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final BindInfoInvalidInmemDrainer INSTANCE = new BindInfoInvalidInmemDrainer();

    }

    public static BindInfoInvalidInmemDrainer getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private BindInfoInvalidInmemDrainer()
    {
        // This has to be updated.
        super(SmppObjectType.BIND_INFO_INVALID, 1000, TimerIntervalConstant.CARRIER_ERROR_INFO_REFRESH);
    }

    @Override
    public void processInMemObjects(
            List<ISmppInfo> aList)
            throws Exception
    {
        DbBindOperation.insertInvalidUnBindInfo(aList);
    }

}
