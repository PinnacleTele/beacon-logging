package com.itextos.beacon.platform.agingdn.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.platform.msgflowutil.util.PlatformUtil;

public class AgingUtil
{

    private static final Log log = LogFactory.getLog(AgingUtil.class);

    private AgingUtil()
    {}

    public static void sendToErrorLog(
            BaseMessage aBaseMessage,
            Exception aErrorMsg)
    {

        try
        {
            PlatformUtil.sendToErrorLog(Component.DLRR, aBaseMessage, aErrorMsg);
        }
        catch (final Exception e)
        {
            log.error("Exception while sending request to error log. " + aBaseMessage, e);
        }
    }

}
