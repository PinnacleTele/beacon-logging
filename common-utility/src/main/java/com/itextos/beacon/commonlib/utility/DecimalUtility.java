package com.itextos.beacon.commonlib.utility;

import org.apache.commons.math3.util.Precision;

public final class DecimalUtility
{

    private DecimalUtility()
    {}

    public static final int                 DEFAULT_FLOAT_PRECISION_DIGITS_COUNT           = 2;
    public static final int                 DEFAULT_FLOAT_PRECISION_DIGITS_FOR_DATA_STORE  = DEFAULT_FLOAT_PRECISION_DIGITS_COUNT;
    public static final int                 DEFAULT_FLOAT_PRECISION_DIGITS_FOR_PROCESS     = 4;
    public static final DecimalRoundingMode DEFAULT_FLOAT_PRECISION_ROUND_OFF_MODE         = DecimalRoundingMode.UP;

    public static final int                 DEFAULT_DOUBLE_PRECISION_DIGITS_COUNT          = 6;
    public static final int                 DEFAULT_DOUBLE_PRECISION_DIGITS_FOR_DATA_STORE = DEFAULT_DOUBLE_PRECISION_DIGITS_COUNT;
    public static final int                 DEFAULT_DOUBLE_PRECISION_DIGITS_FOR_PROCESS    = 12;
    public static final DecimalRoundingMode DEFAULT_DOUBLE_PRECISION_ROUND_OFF_MODE        = DecimalRoundingMode.UP;

    /**
     * Use {@link #round(float, int) or
     * {@link #round(float, int, DecimalRoundingMode) methods for more
     * clarity.
     *
     * @param aDoubleValue
     * @param aDefaultPrecisionDigitsCount
     *
     * @return
     *
     * @deprecated
     */
    @Deprecated
    public static float round(
            float aFloatValue)
    {
        return round(aFloatValue, DEFAULT_FLOAT_PRECISION_DIGITS_COUNT);
    }

    public static float round(
            float aFloatValue,
            int aDefaultPrecisionDigitsCount)
    {
        return round(aFloatValue, aDefaultPrecisionDigitsCount, DEFAULT_DOUBLE_PRECISION_ROUND_OFF_MODE);
    }

    public static float round(
            float aFloatValue,
            int aDefaultPrecisionDigitsCount,
            DecimalRoundingMode aDefaultPrecisionRoundOffMode)
    {
        return Precision.round(aFloatValue, aDefaultPrecisionDigitsCount,
                aDefaultPrecisionRoundOffMode == null ? DEFAULT_DOUBLE_PRECISION_ROUND_OFF_MODE.getRoundOffValue() : aDefaultPrecisionRoundOffMode.getRoundOffValue());
    }

    /**
     * Use {@link #round(double, int)} or
     * {@link #round(double, int, DecimalRoundingMode)} methods for more
     * clarity.
     *
     * @param aDoubleValue
     * @param aDefaultPrecisionDigitsCount
     *
     * @return
     *
     * @deprecated
     */
    @Deprecated
    public static double round(
            double aDoubleValue)
    {
        return round(aDoubleValue, DEFAULT_DOUBLE_PRECISION_DIGITS_COUNT);
    }

    public static double round(
            double aDoubleValue,
            int aDefaultPrecisionDigitsCount)
    {
        return round(aDoubleValue, aDefaultPrecisionDigitsCount, DEFAULT_DOUBLE_PRECISION_ROUND_OFF_MODE);
    }

    public static double round(
            double aDoubleValue,
            int aDefaultPrecisionDigitsCount,
            DecimalRoundingMode aDefaultPrecisionRoundOffMode)
    {
        return Precision.round(aDoubleValue, aDefaultPrecisionDigitsCount,
                aDefaultPrecisionRoundOffMode == null ? DEFAULT_DOUBLE_PRECISION_ROUND_OFF_MODE.getRoundOffValue() : aDefaultPrecisionRoundOffMode.getRoundOffValue());
    }

    public static void main(
            String[] args)
    {
        // final double d1 = 425.0255015300918d;
        // final double d2 = 340.02040122407345d;
        // final double smsCost = d1 + d2;
        //
        // final int msgCount = 2;
        // final double d3 = msgCount * smsCost;

        final float d1       = 425.0255115300918f;
        final float d2       = 340.02040122407345f;
        final float smsCost  = d1 + d2;

        final int   msgCount = 2;
        final float d3       = msgCount * smsCost;

        System.out.println(round(Float.MAX_VALUE));

        System.out.println("d1\t" + d1);
        System.out.println("d2\t" + d2);
        System.out.println("smscost\t" + smsCost);
        System.out.println("to smscost\t" + d3);

        System.out.println("---------------");

        System.out.println("d1\t" + round(d1));
        System.out.println("d2\t" + round(d2));
        System.out.println("smscost\t" + round(smsCost));
        System.out.println("to smscost\t" + round(d3));

        System.out.println("---------------");

        System.out.println("d1\t" + round(d1, 8));
        System.out.println("d2\t" + round(d2, 8));
        System.out.println("smscost\t" + round(smsCost, 8));
        System.out.println("to smscost\t" + round(d3, 8));

        System.out.println("---------------");

        System.out.println("d1\t" + round(d1, 8, DecimalRoundingMode.CEILING));
        System.out.println("d2\t" + round(d2, 8, DecimalRoundingMode.CEILING));
        System.out.println("smscost\t" + round(smsCost, 8, DecimalRoundingMode.CEILING));
        System.out.println("to smscost\t" + round(d3, 8, DecimalRoundingMode.CEILING));

        System.out.println("---------------");

        System.out.println("d1\t" + round(d1, 8, DecimalRoundingMode.FLOOR));
        System.out.println("d2\t" + round(d2, 8, DecimalRoundingMode.FLOOR));
        System.out.println("smscost\t" + round(smsCost, 8, DecimalRoundingMode.FLOOR));
        System.out.println("to smscost\t" + round(d3, 8, DecimalRoundingMode.FLOOR));
    }

}