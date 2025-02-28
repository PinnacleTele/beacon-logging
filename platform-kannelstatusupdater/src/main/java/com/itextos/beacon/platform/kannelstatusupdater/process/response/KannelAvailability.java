package com.itextos.beacon.platform.kannelstatusupdater.process.response;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;
import com.itextos.beacon.platform.kannelstatusupdater.utility.KannelRedisConstants;
import com.itextos.beacon.platform.kannelstatusupdater.utility.Utility;

public class KannelAvailability
        implements
        ITimedProcess
{

    private static final Log log                         = LogFactory.getLog(KannelAvailability.class);

    private static final int FROM_CARRIER_HANDOVER       = 0;
    private static final int FROM_RETRY_CARRIER_HANDOVER = 1;

    private static class SingletonHolder
    {

        static final KannelAvailability INSTANCE = new KannelAvailability();

    }

    public static KannelAvailability getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

  private TimedProcessor                   mTimedProcessor = null;
    private boolean                          canContinue     = true;

    private Map<String, Map<String, String>> kannelRedisData = new HashMap<>();

    private KannelAvailability()
    {
    	
        mTimedProcessor = new TimedProcessor("KannelAvailability", this, TimerIntervalConstant.KANNEL_AVALIABILITY_REFRESH);
      
        ExecutorSheduler.getInstance().addTask(mTimedProcessor, "KannelAvailability");
    }

    /** Request from - 0-CarrierHandover, 1-RetryCarrierHandover */
    public boolean isKannelAvailable(
            String aRouteId,
            int aReqFrom)
    {

        try
        {
            aRouteId = aRouteId.toLowerCase();

            if (kannelRedisData.containsKey(aRouteId))
            {
                final Map<String, String> lKannelInfo = kannelRedisData.get(aRouteId);

                if (log.isDebugEnabled())
                    log.debug("Routeid available in redis: " + aRouteId + " valueMap:" + lKannelInfo + " msgcomeFrom:" + aReqFrom);

                final boolean lAvailableStatus = CommonUtility.isTrue(lKannelInfo.get(KannelRedisConstants.KANNEL_KEY_AVAILABLE));
                final String  lRedisStoreSize  = lKannelInfo.get(KannelRedisConstants.KANNEL_KEY_STORESIZE);

                if (!lAvailableStatus)
                {
                    if (log.isDebugEnabled())
                        log.debug("Kannel is down routeid: " + aRouteId + " available:" + lAvailableStatus);
                    return false;
                }

                final long lKannelStoreSize = Long.parseLong(lRedisStoreSize);
                final long lTableStoreSize  = Utility.getKannelStoreSize(aRouteId);

                if ((lTableStoreSize > 0) && (lKannelStoreSize > lTableStoreSize))
                {
                    if (log.isDebugEnabled())
                        log.debug("Storesize is reached threshold routeid: " + aRouteId + " tableStoreSize:" + lTableStoreSize + " redisStoreSize:" + lRedisStoreSize);
                    return false;
                }

                if (aReqFrom == FROM_CARRIER_HANDOVER)
                {
                    if (!Utility.isFailedRecordsAvailable(lKannelInfo))
                        return Utility.checkKannelLatency(aRouteId, lKannelInfo);

                    return false;
                }
            }
            else
                if (log.isDebugEnabled())
                    log.debug("Routeid is not available in redis: " + aRouteId);
        }
        catch (final Exception e)
        {
            log.error("Exception:", e);
        }
        return true;
    }

    @Override
    public boolean canContinue()
    {
        return canContinue;
    }

    @Override
    public boolean processNow()
    {
        kannelRedisData = Utility.getKannelConfig();
        return false;
    }

    @Override
    public void stopMe()
    {
        canContinue = false;

        if (mTimedProcessor != null)
            mTimedProcessor.stopReaper();
    	
          }

}