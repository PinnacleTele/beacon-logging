package com.itextos.beacon.smpp.interfaces.inmemdrainer;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.platform.smpputil.ISmppInfo;
import com.itextos.beacon.smpp.objects.SmppObjectType;
import com.itextos.beacon.smpp.objects.bind.UnbindInfoRedis;
import com.itextos.beacon.smpp.objects.inmem.InfoCollection;
import com.itextos.beacon.smpp.redisoperations.RedisBindOperation;

public class UnbindInfoRedisInmemDrainer
        extends
        AbstractInmemDrainer
{

    private static final Log log = LogFactory.getLog(UnbindInfoRedisInmemDrainer.class);

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final UnbindInfoRedisInmemDrainer INSTANCE = new UnbindInfoRedisInmemDrainer();

    }

    public static UnbindInfoRedisInmemDrainer getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private UnbindInfoRedisInmemDrainer()
    {
        // This has to be updated.
        super(SmppObjectType.UNBIND_INFO_REDIS, 1000, TimerIntervalConstant.CARRIER_ERROR_INFO_REFRESH);
    }

    @Override
    public void processInMemObjects(
            List<ISmppInfo> aList)
            throws Exception
    {

        for (final ISmppInfo smppInfo : aList)
        {
            final UnbindInfoRedis unbindInfoRedis = (UnbindInfoRedis) smppInfo;

            try
            {
                RedisBindOperation.decreaseBindCount(unbindInfoRedis.getClientId(), unbindInfoRedis.getInstanceId());
            }
            catch (final Exception exp)
            {
                InfoCollection.getInstance().addInfoObject(SmppObjectType.UNBIND_INFO_REDIS, unbindInfoRedis);
            }
        }
    }

}