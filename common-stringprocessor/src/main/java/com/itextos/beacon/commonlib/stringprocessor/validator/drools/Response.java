package com.itextos.beacon.commonlib.stringprocessor.validator.drools;

class Response
{

    private final String value;
    private boolean      isValidated;

    Response(
            String aValue)
    {
        value = aValue;
    }

    String getValue()
    {
        return value;
    }

    boolean isValidated()
    {
        return isValidated;
    }

    void setValidated(
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