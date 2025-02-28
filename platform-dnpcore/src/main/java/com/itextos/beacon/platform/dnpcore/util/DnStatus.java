package com.itextos.beacon.platform.dnpcore.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.errorinfo.data.ErrorStatus;

public enum DnStatus
{

    DELIVRD,
    ACCEPTD,
    EXPIRED,
    DELETED,
    UNDELIV,
    UNKNOWN,
    REJECTD;

    private static final Log log = LogFactory.getLog(DnStatus.class);

    public static String getDnStatus(
            String aStatusString)
    {
        final ErrorStatus lErrorStat   = ErrorStatus.getErrorStatus(CommonUtility.nullCheck(aStatusString, true).toUpperCase());
        String            returnString = null;

        if (lErrorStat == null)
        {
            returnString = UNKNOWN.name();
            log.error("Invalid Status String '" + aStatusString + "'", new Throwable("Invalid Status String '" + aStatusString + "'"));
        }
        else
            switch (lErrorStat)
            {
                case SUCCESS:
                    returnString = ACCEPTD.name();
                    break;

                case REJECTED:
                    returnString = REJECTD.name();
                    break;

                case EXPIRED:
                    returnString = EXPIRED.name();
                    break;

                case FAILED:
                    returnString = UNDELIV.name();
                    break;

                case DELIVERED:
                    returnString = DELIVRD.name();
                    break;

                case PENDING:
                    returnString = UNKNOWN.name();
                    break;

                default:
                    returnString = UNKNOWN.name();
                    break;
            }

        if (log.isDebugEnabled())
            log.debug("Status string '" + aStatusString + "' DN STATUS '" + returnString + "'");
        return returnString;
    }

}
