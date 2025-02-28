package com.itextos.beacon.smpp.interfaces.util;

import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cloudhopper.smpp.pdu.SubmitSmResp;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.InterfaceStatusCode;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.smpp.objects.SessionDetail;
import com.itextos.beacon.smpp.objects.request.SmppMessageRequest;
import com.itextos.beacon.smpp.utils.AccountDetails;
import com.itextos.beacon.smpp.utils.properties.SmppUtilConstants;

public class TraiBlockoutCheck
{

    private static Log log = LogFactory.getLog(TraiBlockoutCheck.class);

    private TraiBlockoutCheck()
    {}

    public static boolean isValidTraiBlockOut(
            Date aScheduleDate,
            SessionDetail aSessionDetail,
            SmppMessageRequest aSmppMessageRequest,
            SubmitSmResp aSubmitResponse)
    {
        boolean       status                 = true;

        final boolean isDomTraBlockoutReject = aSessionDetail.isDomesticTraBlockoutReject();

        if (log.isDebugEnabled())
            log.debug("Domestic TRA Blockout Reject flag :" + isDomTraBlockoutReject);

        if (!isDomTraBlockoutReject)
            return status;

        final String   lTraiStartTime  = AccountDetails.getConfigParamsValueAsString(ConfigParamConstants.TRAI_BLOCKOUT_START);
        final String   lTraiEndTime    = AccountDetails.getConfigParamsValueAsString(ConfigParamConstants.TRAI_BLOCKOUT_STOP);

        final Calendar lBlockStartTime = Calendar.getInstance();
        lBlockStartTime.setTime(aScheduleDate);
        final Calendar lBlockEndTime = Calendar.getInstance();
        lBlockEndTime.setTime(aScheduleDate);

        // st - start time
        final StringTokenizer lStartTime = new StringTokenizer(lTraiStartTime, ":");
        final StringTokenizer lEndTime   = new StringTokenizer(lTraiEndTime, ":");

        final int             lStartHour = Integer.parseInt(lStartTime.nextToken());
        final int             lStartMin  = Integer.parseInt(lStartTime.nextToken());
        final int             endHour    = Integer.parseInt(lEndTime.nextToken());
        final int             endMin     = Integer.parseInt(lEndTime.nextToken());

        final Calendar        date       = Calendar.getInstance();
        date.setTime(aScheduleDate);
        final int currentHour = date.get(Calendar.HOUR_OF_DAY);

        if (lStartHour > endHour)
        {

            if (lStartHour > currentHour)
            {
                lBlockStartTime.set(Calendar.DAY_OF_MONTH, (date.get(Calendar.DAY_OF_MONTH) - 1));
                lBlockEndTime.set(Calendar.DAY_OF_MONTH, date.get(Calendar.DAY_OF_MONTH));
            }
            else
            {
                lBlockStartTime.set(Calendar.DAY_OF_MONTH, date.get(Calendar.DAY_OF_MONTH));
                lBlockEndTime.set(Calendar.DAY_OF_MONTH, (date.get(Calendar.DAY_OF_MONTH) + 1));
            }
        }
        else
        {
            lBlockStartTime.set(Calendar.DAY_OF_MONTH, date.get(Calendar.DAY_OF_MONTH));
            lBlockEndTime.set(Calendar.DAY_OF_MONTH, date.get(Calendar.DAY_OF_MONTH));
        }

        lBlockStartTime.set(Calendar.HOUR_OF_DAY, lStartHour);
        lBlockStartTime.set(Calendar.MINUTE, lStartMin);
        lBlockEndTime.set(Calendar.HOUR_OF_DAY, endHour);
        lBlockEndTime.set(Calendar.MINUTE, endMin);

        final Calendar currentTime = Calendar.getInstance();
        currentTime.setTime(aScheduleDate);

        if (currentTime.after(lBlockStartTime) && currentTime.before(lBlockEndTime))
        {
            if (log.isDebugEnabled())
                log.debug("Check for TRA block out time / schedule time falls in TRA block out time");

            status = false;

            if (aSmppMessageRequest.getScheduleTime() == null)
            {
                aSubmitResponse.setCommandStatus(SmppUtilConstants.TRAI_BLOCKOUT_REJECT);
                aSmppMessageRequest.setInterfaceErrorCode(InterfaceStatusCode.TRA_BLOCKOUT_TIME.getStatusCode());
            }
            else
            {
                aSubmitResponse.setCommandStatus(SmppUtilConstants.TRAI_BLOCKOUT_REJECT);
                aSmppMessageRequest.setInterfaceErrorCode(InterfaceStatusCode.SCHEDULE_TRA_BLOCKOUT_TIME.getStatusCode());
            }
        }

        return status;
    }

    public static boolean isBlockoutTraiTime(
            SmppMessageRequest aMessageRequest,
            String aStartTime,
            String aEndTime,
            Date aScheduleDate)
    {
        boolean        lClientStatus      = true;

        final Calendar lCalBlockStartTime = Calendar.getInstance();
        lCalBlockStartTime.setTime(aScheduleDate);
        final Calendar lCalBlockEndTime = Calendar.getInstance();
        lCalBlockEndTime.setTime(aScheduleDate);
        // st - start time
        final StringTokenizer lStartTime       = new StringTokenizer(aStartTime, ":");
        final StringTokenizer lEndTime         = new StringTokenizer(aEndTime, ":");

        final int             lStartHour       = Integer.parseInt(lStartTime.nextToken());
        final int             lStartMin        = Integer.parseInt(lStartTime.nextToken());
        final int             lEndHour         = Integer.parseInt(lEndTime.nextToken());
        final int             lEndMin          = Integer.parseInt(lEndTime.nextToken());

        final Calendar        lCalScheduleDate = Calendar.getInstance();
        lCalScheduleDate.setTime(aScheduleDate);
        final int currentHour = lCalScheduleDate.get(Calendar.HOUR_OF_DAY);

        if (lStartHour > lEndHour)
        {

            if (lStartHour > currentHour)
            {
                lCalBlockStartTime.set(Calendar.DAY_OF_MONTH, (lCalScheduleDate.get(Calendar.DAY_OF_MONTH) - 1));
                lCalBlockEndTime.set(Calendar.DAY_OF_MONTH, lCalScheduleDate.get(Calendar.DAY_OF_MONTH));
            }
            else
            {
                lCalBlockStartTime.set(Calendar.DAY_OF_MONTH, lCalScheduleDate.get(Calendar.DAY_OF_MONTH));
                lCalBlockEndTime.set(Calendar.DAY_OF_MONTH, (lCalScheduleDate.get(Calendar.DAY_OF_MONTH) + 1));
            }
        }
        else
        {
            lCalBlockStartTime.set(Calendar.DAY_OF_MONTH, lCalScheduleDate.get(Calendar.DAY_OF_MONTH));
            lCalBlockEndTime.set(Calendar.DAY_OF_MONTH, lCalScheduleDate.get(Calendar.DAY_OF_MONTH));
        }

        lCalBlockStartTime.set(Calendar.HOUR_OF_DAY, lStartHour);
        lCalBlockStartTime.set(Calendar.MINUTE, lStartMin);
        lCalBlockEndTime.set(Calendar.HOUR_OF_DAY, lEndHour);
        lCalBlockEndTime.set(Calendar.MINUTE, lEndMin);

        final Calendar lCalScheduleTime = Calendar.getInstance();
        lCalScheduleTime.setTime(aScheduleDate);

        if (lCalScheduleTime.after(lCalBlockStartTime) && lCalScheduleTime.before(lCalBlockEndTime))
        {
            final String str = DateTimeUtility.getFormattedDateTime(lCalBlockEndTime.getTime(), DateTimeFormat.DEFAULT);
            aMessageRequest.setScheduleTime(str);

            // TRAI_BLOCKOUT_REJECT
            lClientStatus = false;
        }

        return lClientStatus;
    }

}
