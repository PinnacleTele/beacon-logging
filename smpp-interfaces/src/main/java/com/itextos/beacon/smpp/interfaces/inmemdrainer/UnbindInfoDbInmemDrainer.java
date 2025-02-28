package com.itextos.beacon.smpp.interfaces.inmemdrainer;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.platform.smpputil.ISmppInfo;
import com.itextos.beacon.smpp.dboperations.DbBindOperation;
import com.itextos.beacon.smpp.objects.SmppObjectType;

public class UnbindInfoDbInmemDrainer
        extends
        AbstractInmemDrainer
{

    private static final Log log = LogFactory.getLog(UnbindInfoDbInmemDrainer.class);

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final UnbindInfoDbInmemDrainer INSTANCE = new UnbindInfoDbInmemDrainer();

    }

    public static UnbindInfoDbInmemDrainer getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private UnbindInfoDbInmemDrainer()
    {
        // This has to be updated.
        super(SmppObjectType.UNBIND_INFO_DB, 1000, TimerIntervalConstant.CARRIER_ERROR_INFO_REFRESH);
    }

    @Override
    public void processInMemObjects(
            List<ISmppInfo> aList)
            throws Exception
    {
        DbBindOperation.insertUnBindInfo(aList);
    }

}