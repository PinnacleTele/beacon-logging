package com.itextos.beacon.commonlib.utility;

import java.math.RoundingMode;

public enum DecimalRoundingMode
{

    UP(RoundingMode.UP),
    DOWN(RoundingMode.DOWN),
    CEILING(RoundingMode.CEILING),
    FLOOR(RoundingMode.FLOOR),
    HALF_UP(RoundingMode.HALF_UP),
    HALF_DOWN(RoundingMode.HALF_DOWN),
    HALF_EVEN(RoundingMode.HALF_EVEN),

    ;

    private RoundingMode mValue;

    DecimalRoundingMode(
            RoundingMode aValue)
    {
        mValue = aValue;
    }

    public int getRoundOffValue()
    {
        return mValue.ordinal();
    }

}