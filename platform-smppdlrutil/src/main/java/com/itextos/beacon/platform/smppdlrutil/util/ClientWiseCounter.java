package com.itextos.beacon.platform.smppdlrutil.util;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.inmemory.smpp.account.SmppAccInfo;
import com.itextos.beacon.inmemory.smpp.account.util.SmppAccUtil;
import com.itextos.beacon.platform.msgflowutil.util.PlatformUtil;

public class ClientWiseCounter
{

    private static final Log log = LogFactory.getLog(ClientWiseCounter.class);

    private ClientWiseCounter()
    {}

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final ClientWiseCounter INSTANCE = new ClientWiseCounter();

    }

    public static ClientWiseCounter getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private static final long   DEFAULT_EXPIRE_MESSAGE_TIME_INMILLIS = CommonUtility.getLong(PlatformUtil.getAppConfigValueAsString(ConfigParamConstants.SMPP_CONCAT_MESSAGE_EXPIRY_IN_SEC));
    private final static String SUBMIT_DATE                          = "submit date";

    public static boolean canProcessMessage(
            DeliveryObject aDeliveryObject)
    {
        boolean returnValue = false;

        try
        {
            long              mExpireMessageTimeInMillis = DEFAULT_EXPIRE_MESSAGE_TIME_INMILLIS * 1000;

            final String      lShortMessage              = aDeliveryObject.getValue(MiddlewareConstant.MW_SHORT_MESSAGE);
            final SmppAccInfo lSmAccInfo                 = SmppAccUtil.getSmppAccountInfo(aDeliveryObject.getClientId());

            if ((lSmAccInfo != null) && (lSmAccInfo.getDnExpiryInSec() > 0))
            {
                if (log.isDebugEnabled())
                    log.debug("Max DN Expiry In Seconds ..:'" + lSmAccInfo.getDnExpiryInSec() + "'");

                mExpireMessageTimeInMillis = lSmAccInfo.getDnExpiryInSec() * 1000;
            }

            if (log.isDebugEnabled())
                log.debug("Short Message : " + lShortMessage);

            final int            lSubmitTimeStart = lShortMessage.indexOf(SUBMIT_DATE) + SUBMIT_DATE.length() + 1;
            final int            lDTimeEnd        = lShortMessage.indexOf(" ", lSubmitTimeStart);
            final String         lDate            = lShortMessage.substring(lSubmitTimeStart, lDTimeEnd);

            // submit date:161228155600 OR submit date:1612281556
            final DateTimeFormat lDateFormat      = (lDate.length() == 12) ? DateTimeFormat.NO_SEPARATOR_YY_MM_DD_HH_MM_SS : DateTimeFormat.NO_SEPARATOR_YY_MM_DD_HH_MM;

            final Date           stime            = DateTimeUtility.getDateFromString(lDate, lDateFormat);

            final long           diff             = System.currentTimeMillis() - stime.getTime();
            if (log.isInfoEnabled())
                log.info("expireMessageTimeInMillis=" + mExpireMessageTimeInMillis + " diff=" + diff + "  diff < expireMessageTimeInMillis=" + (diff < mExpireMessageTimeInMillis));
            returnValue = diff < mExpireMessageTimeInMillis;

            if (log.isDebugEnabled())
                log.debug("STime : '" + lDate + "', Diff : '" + diff + "' milliseconds, Result : '" + returnValue + "'" + " expireMessageTimeInMillis:" + mExpireMessageTimeInMillis);
        }
        catch (final Exception e)
        {
            log.error("Problem in validating Message Expiry. " + aDeliveryObject, e);
        }
        if (log.isInfoEnabled())
            log.info("can process message==>" + returnValue);
        return returnValue;
    }

}
