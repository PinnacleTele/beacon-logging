package com.itextos.beacon.commonlib.constants.exception;

public class ItextosMiddlewareException
        extends
        ItextosException
{

    private static final long serialVersionUID = -5538919477447897505L;

    public ItextosMiddlewareException()
    {
        super();
    }

    public ItextosMiddlewareException(
            String aMessage)
    {
        super(aMessage);
    }

    public ItextosMiddlewareException(
            Throwable aCause)
    {
        super(aCause);
    }

    public ItextosMiddlewareException(
            String aMessage,
            Throwable aCause)
    {
        super(aMessage, aCause);
    }

    public ItextosMiddlewareException(
            String aMessage,
            Throwable aCause,
            boolean aEnableSuppression,
            boolean aWritableStackTrace)
    {
        super(aMessage, aCause, aEnableSuppression, aWritableStackTrace);
    }

}