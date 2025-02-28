package com.itextos.beacon.commonlib.constants.exception;

public class ItextosInterfaceException
        extends
        ItextosException
{

    private static final long serialVersionUID = 3361967752576135000L;

    public ItextosInterfaceException()
    {
        super();
    }

    public ItextosInterfaceException(
            String aMessage)
    {
        super(aMessage);
    }

    public ItextosInterfaceException(
            Throwable aCause)
    {
        super(aCause);
    }

    public ItextosInterfaceException(
            String aMessage,
            Throwable aCause)
    {
        super(aMessage, aCause);
    }

    public ItextosInterfaceException(
            String aMessage,
            Throwable aCause,
            boolean aEnableSuppression,
            boolean aWritableStackTrace)
    {
        super(aMessage, aCause, aEnableSuppression, aWritableStackTrace);
    }

}
