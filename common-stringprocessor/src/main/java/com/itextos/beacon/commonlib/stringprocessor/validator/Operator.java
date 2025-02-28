package com.itextos.beacon.commonlib.stringprocessor.validator;

enum Operator
{

    EQUAL_TO("="),
    NOT_EQUAL_TO("!="),
    LESS_THAN("<"),
    LESS_THAN_EQUAL_TO("<="),
    GREATER_THAN(">"),
    GREATER_THAN_EQUAL_TO(">=");

    private String key;

    Operator(
            String aKey)
    {
        key = aKey;
    }

    public String getKey()
    {
        return key;
    }

    public static Operator getEnum(
            String value)
    {
        for (final Operator v : values())
            if (v.getKey().equalsIgnoreCase(value))
                return v;
        throw new IllegalArgumentException();
    }

}
