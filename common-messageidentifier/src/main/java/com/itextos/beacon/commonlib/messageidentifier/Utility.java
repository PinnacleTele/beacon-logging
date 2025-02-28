package com.itextos.beacon.commonlib.messageidentifier;

final class Utility
{

    private Utility()
    {}

    static String nullCheck(
            Object aObject)
    {
        return nullCheck(aObject, false);
    }

    static String nullCheck(
            Object aObject,
            boolean aTrimIt)
    {
        return (aObject == null) ? (aTrimIt ? "" : " ") : (aTrimIt ? aObject.toString().trim() : aObject.toString());
    }

    static int getInteger(
            String aString,
            int aDefaultValue)
    {
        int returnValue = aDefaultValue;

        try
        {
            returnValue = Integer.parseInt(aString);
        }
        catch (final Exception e)
        {}
        return returnValue;
    }

}