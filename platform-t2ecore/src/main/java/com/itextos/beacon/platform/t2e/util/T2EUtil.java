package com.itextos.beacon.platform.t2e.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.CustomFeatures;
import com.itextos.beacon.inmemory.customfeatures.InmemCustomFeatures;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;

public class T2EUtil
{

    private static final Log log = LogFactory.getLog(T2EUtil.class);

    public static boolean isFastDnEnabled(
            String lClientId)
    {
        final String lFeactureEnabled = getCutomFeatureValue(lClientId, CustomFeatures.IS_FASTDN_ENABLE);
        if ((lFeactureEnabled != null) && lFeactureEnabled.trim().equals("1"))
            return true;

        return false;
    }

    public static String getCutomFeatureValue(
            String aClientId,
            CustomFeatures aCustomFeature)
    {
        final InmemCustomFeatures lCustomFeatures = (InmemCustomFeatures) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.CUSTOM_FEATURES);
        return lCustomFeatures.getValueOfCustomFeature(aClientId, aCustomFeature.getKey());
    }

}
