package com.itextos.beacon.commonlib.componentconsumer.processor;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.message.IMessage;

class FallbackMessage
{

    private final IMessage  mMessage;
    private final Component mNextComponent;

    public FallbackMessage(
            IMessage aMessage,
            Component aNextComponent)
    {
        super();
        mMessage       = aMessage;
        mNextComponent = aNextComponent;
    }

    public IMessage getMessage()
    {
        return mMessage;
    }

    public Component getNextComponent()
    {
        return mNextComponent;
    }

    @Override
    public String toString()
    {
        return "FallbackMessage [mMessage=" + mMessage + ", mNextComponent=" + mNextComponent + "]";
    }

}