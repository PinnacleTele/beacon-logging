package com.itextos.beacon.platform.agingdn.processor;

import java.util.ArrayList;
import java.util.List;

public class AgingDlrGenHolder
{

    List<AgingDNGenerator> allReaders = new ArrayList<>();

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final AgingDlrGenHolder INSTANCE = new AgingDlrGenHolder();

    }

    public static AgingDlrGenHolder getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    @SuppressWarnings("unused")
    private void startAgingDlrGen()
    {
        allReaders.add(new AgingDNGenerator());
    }

    public void stopMe()
    {
        for (final AbstractAgingDlrGen agingDlrGen : allReaders)
            agingDlrGen.stopMe();
    }

}
