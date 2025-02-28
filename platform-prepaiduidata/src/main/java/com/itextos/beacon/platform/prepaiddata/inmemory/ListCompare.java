package com.itextos.beacon.platform.prepaiddata.inmemory;

import java.util.Comparator;

import com.itextos.beacon.platform.prepaiddata.PrepaidData;

public class ListCompare
        implements
        Comparator<PrepaidData>
{

    @Override
    public int compare(
            PrepaidData aPrepaid1,
            PrepaidData aPrepaid2)
    {
        final int returnValue = 0;

        if ((aPrepaid1 != null) && (aPrepaid2 != null) && (aPrepaid1.getUserName() != null) && (aPrepaid2.getUserName() != null))
            return aPrepaid1.getUserName().compareTo(aPrepaid2.getUserName());
        return returnValue;
    }

}