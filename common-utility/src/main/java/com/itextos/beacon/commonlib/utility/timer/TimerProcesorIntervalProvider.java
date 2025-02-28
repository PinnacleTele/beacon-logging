package com.itextos.beacon.commonlib.utility.timer;

import org.apache.commons.configuration.PropertiesConfiguration;

import com.itextos.beacon.commonlib.commonpropertyloader.PropertiesPath;
import com.itextos.beacon.commonlib.commonpropertyloader.PropertyLoader;
import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;

public class TimerProcesorIntervalProvider
{

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final TimerProcesorIntervalProvider INSTANCE = new TimerProcesorIntervalProvider();

    }

    public static TimerProcesorIntervalProvider getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    PropertiesConfiguration propConf = null;

    private TimerProcesorIntervalProvider()
    {

        try
        {
            propConf = PropertyLoader.getInstance().getPropertiesConfiguration(PropertiesPath.TIMER_PROCESSOR_INTERVAL_PROPERTIES, true);
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

    public long getTimerIntervalInSecs(
            TimerIntervalConstant aIntervalConstant)
    {
        return propConf.getLong(aIntervalConstant.getIntervalName(), aIntervalConstant.getDurationInSecs());
    }

}
