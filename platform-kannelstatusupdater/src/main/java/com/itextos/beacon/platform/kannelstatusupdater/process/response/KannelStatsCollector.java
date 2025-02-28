package com.itextos.beacon.platform.kannelstatusupdater.process.response;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.platform.kannelstatusupdater.utility.Utility;

public class KannelStatsCollector
{

    private static final Log log                     = LogFactory.getLog(KannelStatsCollector.class);
    public static final int  DEFAULT_TOLLARABLE_TIME = 2000;

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final KannelStatsCollector INSTANCE = new KannelStatsCollector();

    }

    public static KannelStatsCollector getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final BlockingQueue<KannelStatsInfo> mKannelResponseQ = new LinkedBlockingQueue<>();

    private KannelStatsCollector()
    {
        // To start the RedisUpdater.
        KannelStatsRedisUpdater.getInstance();
    }

    public void verifyKannelResponseTime(
            String aKannelUrl,
            String aRouteId,
            long aKannelHitStartTime,
            long aKannelHitEndTime,
            boolean aKannelHitStatus)
    {
        if (log.isDebugEnabled())
            log.debug("Kannel Url : '" + aKannelUrl + "', Route Id : '" + aRouteId + "', Kannel Hit Start Time : '"
                    + DateTimeUtility.getFormattedDateTime(new Date(aKannelHitStartTime), DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS) + "', Kannel Hit End Time : '"
                    + DateTimeUtility.getFormattedDateTime(new Date(aKannelHitEndTime), DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS) + "', Kannel Hit Status : '" + aKannelHitStatus + "'");

        try
        {
            int lTimeTaken = -1;

            if (aKannelHitStatus)
            {
                final int lMaxTolerableTime = CommonUtility.getInteger(Utility.getAppConfigValueAsString(ConfigParamConstants.KANNEL_CONN_RESP_TIME_IN_MILLIS), DEFAULT_TOLLARABLE_TIME);

                if (lMaxTolerableTime == DEFAULT_TOLLARABLE_TIME)
                    log.error("Kannel connection tolerate limit not set properly.");

                lTimeTaken = (int) (aKannelHitEndTime - aKannelHitStartTime);
                if (lTimeTaken > lMaxTolerableTime)
                    log.error("Kannel URL hit more time than expected for Route Id : '" + aRouteId + "' Kannel Url : '" + aKannelUrl + "'. Expected Time : '" + lMaxTolerableTime + "', Time taken : '"
                            + lTimeTaken + "' milliseconds.");
            }

            addKannelStatsInfo(new KannelStatsInfo((aRouteId != null ? aRouteId.toLowerCase() : aRouteId), lTimeTaken));
        }
        catch (final Exception e)
        {
            log.error("Excception occured in calculating the response time and sending to queue.", e);
        }
    }

    private void addKannelStatsInfo(
            KannelStatsInfo aStatsInfo)
    {

        try
        {
            final boolean lOffer = mKannelResponseQ.offer(aStatsInfo, 100, TimeUnit.MILLISECONDS);

            if (log.isDebugEnabled())
                log.debug("addStats() - kannelResponseQueue size : " + mKannelResponseQ.size() + " Added ? :" + lOffer);
        }
        catch (final Exception e)
        {
            log.error("Exception while adding the message in the inmemory queue.", e);
        }
    }

    public boolean isDataAvailable()
    {
        return !mKannelResponseQ.isEmpty();
    }

    public List<KannelStatsInfo> getKannelStatsInfo(
            int aSize)
    {
        final List<KannelStatsInfo> lStatsInfoList = new ArrayList<>();
        mKannelResponseQ.drainTo(lStatsInfoList, aSize);

        if (log.isDebugEnabled())
            log.debug("kannelResponseQ size : " + mKannelResponseQ.size());
        return lStatsInfoList;
    }

}
