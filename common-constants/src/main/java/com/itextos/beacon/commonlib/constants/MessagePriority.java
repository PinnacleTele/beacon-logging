package com.itextos.beacon.commonlib.constants;

import java.util.HashMap;
import java.util.Map;

public enum MessagePriority
        implements
        ItextosEnum
{

    OTP(0),
    PRIORITY_0(0),
    PRIORITY_1(1),
    PRIORITY_2(2),
    PRIORITY_3(3),
    PRIORITY_4(4),
    PRIORITY_5(5);

    private int priority;

    MessagePriority(
            int aPriority)
    {
        priority = aPriority;
    }

    public int getPriority()
    {
        return priority;
    }

    private static final Map<Integer, MessagePriority> allPriorities = new HashMap<>();

    static
    {
        final MessagePriority[] temp = MessagePriority.values();

        for (final MessagePriority priority : temp)
            allPriorities.put(priority.getPriority(), priority);
    }

    public static MessagePriority getMessagePriority(
            String aPriority)
    {
        return getMessagePriority(Integer.parseInt(aPriority));
    }

    public static MessagePriority getMessagePriority(
            int aPriority)
    {
        return allPriorities.get(aPriority);
    }

    @Override
    public String getKey()
    {
        return Integer.toString(getPriority());
    }

}