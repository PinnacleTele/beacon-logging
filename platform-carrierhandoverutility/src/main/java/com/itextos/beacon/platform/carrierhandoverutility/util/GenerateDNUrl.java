package com.itextos.beacon.platform.carrierhandoverutility.util;

import java.util.Date;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.message.SubmissionObject;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.configvalues.ApplicationConfiguration;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.inmemory.whitelistnumbers.MobileWhitelistNumbers;
import com.itextos.beacon.platform.dnpayloadutil.common.PayloadUtil;

public class GenerateDNUrl
{

    private static Log log = LogFactory.getLog(GenerateDNUrl.class);

    private GenerateDNUrl()
    {}

    public static void setDlrUrl(
            SubmissionObject aSubmissionObject,
            int aRetryAttempt)
    {
        if (log.isDebugEnabled())
            log.debug("Submission Object : " + aSubmissionObject);

        final long lCurretnSystemTime = System.currentTimeMillis();
        long       lSts               = lCurretnSystemTime;
        long       lActualTs          = lCurretnSystemTime;

        aSubmissionObject.setRetryAttempt(aRetryAttempt);

        if (aRetryAttempt != 0)
        {
            lSts      = aSubmissionObject.getCarrierSubmitTime().getTime();
            lActualTs = aSubmissionObject.getActualCarrierSubmitTime().getTime();
        }

        final Date lScheduleTime = aSubmissionObject.getScheduleDateTime();
        Date       lStime        = aSubmissionObject.getMessageReceivedTime();

        if ((lScheduleTime != null) && (aSubmissionObject.getAttemptCount() == null) && (aRetryAttempt == 0))
            lStime = lScheduleTime;

        final String lDNTimeAdjust = CommonUtility.nullCheck(aSubmissionObject.getDnAdjustEnabled(), true);
        final long   lSTimeLnng    = lStime.getTime();
        final Random lRandomGen    = new Random();

        if (lSts < lSTimeLnng)
        {
            log.error("Looks MT time is less than the time received from client (stime) so adjusting sts with random number");

            int maxRandomSeed = CommonUtility.getInteger(getAppConfigValueAsString(ConfigParamConstants.GLOBAL_DN_ADJUSTMENT_IN_SEC));
            if (maxRandomSeed == 0)
                maxRandomSeed = 10;

         //   lSts = lSTimeLnng + (lRandomGen.nextInt(maxRandomSeed) * 1000);
            lSts = lSTimeLnng + lRandomGen.nextInt(1500);
            if (log.isDebugEnabled())
                log.debug("Adjusted sts:" + lSts + " for stime:" + lSTimeLnng);
        }

        final String lMNumber  = aSubmissionObject.getMobileNumber();
        final String lClientId = aSubmissionObject.getClientId();
        final String lCircle   = aSubmissionObject.getCircle();

        if (!lDNTimeAdjust.isEmpty() && !lDNTimeAdjust.equals("0"))
        {
            final long    lDNAdjustTimeMill   = CommonUtility.getLong(lDNTimeAdjust) * 1000;
            final boolean isWhiteListed       = checkNumberWhiteListed(lMNumber);
            final boolean isCircleExcludeList = PayloadUtil.isCircleInExcludeList(lClientId, lCircle);
            if (log.isDebugEnabled())
                log.debug("isWhiteListed:" + isWhiteListed + " circleInExcludeList:" + isCircleExcludeList);
            if (((lSts - lSTimeLnng) > lDNAdjustTimeMill) && !isWhiteListed && !isCircleExcludeList)
                lSts = lSTimeLnng + (lRandomGen.nextInt(CommonUtility.getInteger(lDNTimeAdjust) + 1) * 1000);
        }

        aSubmissionObject.setCarrierSubmitTime(new Date(lSts));
        aSubmissionObject.setActualCarrierSubmitTime(new Date(lActualTs));
    }

    public static String getAppConfigValueAsString(
            ConfigParamConstants aConfigParamConstant)
    {
        final ApplicationConfiguration lAppConfiguration = (ApplicationConfiguration) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.APPLICATION_CONFIG);
        return lAppConfiguration.getConfigValue(aConfigParamConstant.getKey());
    }

    public static boolean checkNumberWhiteListed(
            String aMobileNumber)
    {
        final MobileWhitelistNumbers lWhiteListNumber = (MobileWhitelistNumbers) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.MOBILE_WHITELIST);
        return lWhiteListNumber.isNumberWhitelisted(aMobileNumber);
    }

}
