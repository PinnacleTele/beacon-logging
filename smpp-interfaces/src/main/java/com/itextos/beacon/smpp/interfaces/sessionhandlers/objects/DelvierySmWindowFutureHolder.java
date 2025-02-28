package com.itextos.beacon.smpp.interfaces.sessionhandlers.objects;

import com.cloudhopper.commons.util.windowing.WindowFuture;
import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import com.itextos.beacon.platform.smpputil.DeliverSmInfo;
import com.itextos.beacon.smpp.interfaces.event.handlers.ItextosSmppSessionHandler;

public class DelvierySmWindowFutureHolder
{

    private final DeliverSmInfo                                  mDelvierySmInfo;
    private final ItextosSmppSessionHandler                      mEventHandler;
    private final WindowFuture<Integer, PduRequest, PduResponse> mWindowfuture;

    public DelvierySmWindowFutureHolder(
            DeliverSmInfo aDelvierySmInfo,
            ItextosSmppSessionHandler aEventHandler,
            WindowFuture<Integer, PduRequest, PduResponse> aWindowfuture)
    {
        super();
        mDelvierySmInfo = aDelvierySmInfo;
        mEventHandler   = aEventHandler;
        mWindowfuture   = aWindowfuture;
    }

    public DeliverSmInfo getDelvierySmInfo()
    {
        return mDelvierySmInfo;
    }

    public ItextosSmppSessionHandler getEventHandler()
    {
        return mEventHandler;
    }

    public WindowFuture<Integer, PduRequest, PduResponse> getWindowfuture()
    {
        return mWindowfuture;
    }

    @Override
    public String toString()
    {
        return "DelvierySmWindowFutureHolder [mDelvierySmInfo=" + mDelvierySmInfo + ", mEventHandler=" + mEventHandler + ", mWindowfuture=" + mWindowfuture + "]";
    }

}