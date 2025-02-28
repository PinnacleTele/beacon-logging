package com.itextos.beacon.platform.prepaiddata.kannelstatus;

import java.util.Comparator;

public class ListStoresizeComparator
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

        return aO1.getStoreSize() < aO2.getStoreSize() ? 1 : (aO1.getStoreSize() == aO2.getStoreSize() ? 0 : -1);
    }

}
