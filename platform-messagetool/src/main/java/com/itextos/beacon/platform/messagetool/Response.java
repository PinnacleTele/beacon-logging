package com.itextos.beacon.platform.messagetool;

public class Response
{

    private boolean mIsUniCode;
    private String  mMessage;

    public void setUnicode(
            boolean aIsUnicode)
    {
        mIsUniCode = aIsUnicode;
    }

    public void setMessage(
            String aMessage)
    {
        mMessage = aMessage;
    }

    public boolean isIsUniCode()
    {
        return mIsUniCode;
    }

    public String getMessage()
    {
        return mMessage;
    }

    @Override
    public String toString()
    {
        return "Response [mIsUniCode=" + mIsUniCode + ", mMessage=" + mMessage + "]";
    }

}