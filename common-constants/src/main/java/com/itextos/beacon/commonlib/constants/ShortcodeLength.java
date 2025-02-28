package com.itextos.beacon.commonlib.constants;

public enum ShortcodeLength
{

    LENGTH_5(5),
    LENGTH_6(6);

    private final int mLength;

    ShortcodeLength(
            int aLength)
    {
        mLength = aLength;
    }

    public int getLength()
    {
        return mLength;
    }

    public static ShortcodeLength getShortcode(
            int aLength)
    {

        switch (aLength)
        {
            case 5:
                return LENGTH_5;

            case 6:
                return LENGTH_6;

            default:
                break;
        }
        return null;
    }

}