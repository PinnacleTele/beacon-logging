package com.itextos.beacon.commonlib.constants.exception;

public class InternationalSMSRateNotAvailableRuntimeException
        extends
        Exception
{

    private static final long serialVersionUID = 5501361566335613837L;

    public InternationalSMSRateNotAvailableRuntimeException()
    {
        super();
    }

    public InternationalSMSRateNotAvailableRuntimeException(
            String aMessage)
    {
        super(aMessage);
    }

    public InternationalSMSRateNotAvailableRuntimeException(
            Throwable aCause)
    {
        super(aCause);
    }

    public InternationalSMSRateNotAvailableRuntimeException(
            String aMessage,
            Throwable aCause)
    {
        super(aMessage, aCause);
    }

    public InternationalSMSRateNotAvailableRuntimeException(
            String aMessage,
            Throwable aCause,
            boolean aEnableSuppression,
            boolean aWritableStackTrace)
    {
        super(aMessage, aCause, aEnableSuppression, aWritableStackTrace);
    }

}
