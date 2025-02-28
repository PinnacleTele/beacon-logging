package com.itextos.beacon.platform.templatefinder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.MessageConvertionUtility;
import com.itextos.beacon.platform.templatefinder.data.DltTemplatesDataLoader;
import com.itextos.beacon.platform.templatefinder.data.TemplateDataHolder;

public class TemplateScrubber
{

    private static final Log log = LogFactory.getLog(TemplateScrubber.class);

    static
    {
        final int  secondsToWait = 100;
        final long start         = System.currentTimeMillis();

        while (!DltTemplatesDataLoader.getInstance().isFirstLoadCompleted())
        {
            log.warn("Waiting for the DLT Templates load completion. Sleeping for " + secondsToWait + " milliseconds");

            try
            {
                Thread.sleep(secondsToWait);
            }
            catch (final InterruptedException e)
            {}
        }

        if (log.isInfoEnabled())
            log.info("Time taken to load all the templates " + (System.currentTimeMillis() - start) + " millis");
    }
    private static Component   mComponent   = null;
    private static ClusterType mClusterType = null;

    private TemplateScrubber()
    {}

    public static TemplateResult getTemplateHexMessage(
            String aTemplateGroupId,
            String aHeader,
            String aMessage)
    {
        final String msg = MessageConvertionUtility.convertHex2String(aMessage);
        return getTemplate(aTemplateGroupId, aHeader, msg);
    }

    public static TemplateResult getTemplate(
            String aTemplateGroupId,
            String aHeader,
            String aMessage)
    {
        final String lTemplateGroupId = CommonUtility.nullCheck(aTemplateGroupId, true).toLowerCase();
        final String lHeader          = CommonUtility.nullCheck(aHeader, true).toLowerCase();
        final String lMessage         = CommonUtility.nullCheck(aMessage, true).toLowerCase();

        return TemplateDataHolder.getInstance().getTemplates(lTemplateGroupId, lHeader, lMessage);
    }

    public static Component getComponent()
    {
        return mComponent;
    }

    public static void setComponent(
            Component aComponent)
    {
        mComponent = aComponent;
    }

    public static ClusterType getClusterType()
    {
        return mClusterType;
    }

    public static void setClusterType(
            ClusterType aClusterType)
    {
        mClusterType = aClusterType;
    }

}