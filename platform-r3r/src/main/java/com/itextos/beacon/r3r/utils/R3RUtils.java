package com.itextos.beacon.r3r.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.configvalues.ApplicationConfiguration;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;

public class R3RUtils
{

    private static final Log log = LogFactory.getLog(R3RUtils.class);

    private R3RUtils()
    {}

    public static String getAppConfigValueAsString(
            ConfigParamConstants aConfigParamConstant)
    {
        final ApplicationConfiguration lAppConfiguration = (ApplicationConfiguration) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.APPLICATION_CONFIG);
        return lAppConfiguration.getConfigValue(aConfigParamConstant.getKey());
    }

    public static Map<String, String> getSmartLinkData(
            String aJSonData)
    {
        final Map<String, String> lSmartLinkDataMap = new HashMap<>();

        try
        {
            final JsonObject shortCodeJson = JsonParser.parseString(aJSonData).getAsJsonObject();
            lSmartLinkDataMap.put("smartlink_id", CommonUtility.nullCheck(shortCodeJson.get("smartlink_id").getAsString(), true));
            lSmartLinkDataMap.put("shortner_url", CommonUtility.nullCheck(shortCodeJson.get("shortner_url").getAsString(), true));
            lSmartLinkDataMap.put("url", CommonUtility.nullCheck(shortCodeJson.get("url").getAsString(), true));
        }
        catch (final Exception e)
        {
            log.error("Exception while get the Smartlink Data ", e);
        }
        return lSmartLinkDataMap;
    }

}
