package com.itextos.beacon.platform.dnrfallback;

import com.itextos.beacon.commonlib.message.IMessage;
import com.itextos.beacon.platform.dnrfallback.inmem.DlrFallbackQ;

public class DlrFallbackProcessor
{

    private DlrFallbackProcessor()
    {}

    public static boolean sendToFallBack(
            IMessage aIMessage)
    {
        boolean isDone = false;
        while (!isDone)
            try
            {
                DlrFallbackQ.getInstance().addMessage(aIMessage);
                isDone = true;
            }
            catch (final Exception e)
            {
                isDone = false;
            }

        return isDone;
    }

}
