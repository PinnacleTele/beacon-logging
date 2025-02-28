package com.itextos.beacon.httpclienthandover.drools.validator;

public class Response
{

    private final String value;
    private boolean      isValidated;

    public Response(
            String aValue)
    {
        value = aValue;
    }

    public String getValue()
    {
        return value;
    }

    public boolean isValidated()
    {
        return isValidated;
    }

    public void setValidated(
            boolean aIsValidated)
    {
        isValidated = aIsValidated;
    }

    @Override
    public String toString()
    {
        return "Response [value=" + value + ", isValidated=" + isValidated + "]";
    }

}