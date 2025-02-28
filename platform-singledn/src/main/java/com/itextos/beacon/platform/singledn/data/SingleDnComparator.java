package com.itextos.beacon.platform.singledn.data;

import java.util.Comparator;

public class SingleDnComparator
        implements
        Comparator<SingleDnInfo>
{

    @Override
    public int compare(
            SingleDnInfo aO1,
            SingleDnInfo aO2)
    {
        if (aO1 == null)
            return 1;

        if (aO2 == null)
            return -1;

        if (aO1.equals(aO2))
            return 0;

        if (aO1.getDeliveredTime() > aO2.getDeliveredTime())
            return 1;

        if (aO1.getDeliveredTime() < aO2.getDeliveredTime())
            return -1;

        return 0;
    }

}