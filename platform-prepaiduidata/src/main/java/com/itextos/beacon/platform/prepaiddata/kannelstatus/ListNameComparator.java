package com.itextos.beacon.platform.prepaiddata.kannelstatus;

import java.util.Comparator;

public class ListNameComparator
        implements
        Comparator<KannelInfo>
{

    @Override
    public int compare(
            KannelInfo aO1,
            KannelInfo aO2)
    {
        if (aO1 == null)
            return -1;

        if (aO2 == null)
            return -1;

        final int nameCompare = aO1.getOperator().compareTo(aO2.getOperator());
        return nameCompare == 0 ? aO1.getRoute().compareTo(aO2.getRoute()) : nameCompare;
    }

}