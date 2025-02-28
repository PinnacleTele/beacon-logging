package com.itextos.beacon.smpp.utils.generator;

import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.smpp.utils.properties.SmppProperties;

public class BindIdGenerator
{

    private static final int    DEFAULT_VALUE = 1;
    private static final int    MAX_VALUE     = 99;
    private static final String instanceId    = SmppProperties.getInstance().getInstanceId();

    private static class SingletonHolder
    {

        static final BindIdGenerator INSTANCE = new BindIdGenerator();

    }

    public static BindIdGenerator getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private int counter = 0;

    private BindIdGenerator()
    {}

    public synchronized String getNextBindId()
    {
        return CommonUtility.combine(instanceId, DateTimeUtility.getFormattedCurrentDateTime(DateTimeFormat.NO_SEPARATOR_YYYY_MM_DD_HH_MM_SS_SSS), getCount());
    }

    private String getCount()
    {
        if (counter >= MAX_VALUE)
            counter = DEFAULT_VALUE;
        else
            counter++;

        return String.format("%02d", counter);
    }

    public static void main(
            String[] args)
    {
        System.out.println("Hi :" + new BindIdGenerator().getNextBindId());
    }

}