package com.itextos.beacon.commonlib.constants.exception;

public class ItextosException
        extends
        Exception
{

    private static final long serialVersionUID = -755738883904377432L;

    public ItextosException()
    {
        super();
    }

    public ItextosException(
            String aMessage)
    {
        super(aMessage);
    }

    public ItextosException(
            Throwable aCause)
    {
        super(aCause);
    }

    public ItextosException(
            String aMessage,
            Throwable aCause)
    {
        super(aMessage, aCause);
    }

    public ItextosException(
            String aMessage,
            Throwable aCause,
            boolean aEnableSuppression,
            boolean aWritableStackTrace)
    {
        super(aMessage, aCause, aEnableSuppression, aWritableStackTrace);
    }

}
