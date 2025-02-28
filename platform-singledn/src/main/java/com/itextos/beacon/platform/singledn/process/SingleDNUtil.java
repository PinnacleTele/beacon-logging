package com.itextos.beacon.platform.singledn.process;

import java.util.Map;

import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.configvalues.ApplicationConfiguration;
import com.itextos.beacon.inmemory.customfeatures.InmemAccountDnTypeMappingInfo;
import com.itextos.beacon.inmemory.customfeatures.pojo.DlrTypeInfo;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;

public class SingleDNUtil
{

    private SingleDNUtil()
    {}

    public static DlrTypeInfo getDnTypeInfo(
            String aClientId)
    {
        final InmemAccountDnTypeMappingInfo lDnTypeInfo = (InmemAccountDnTypeMappingInfo) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.DN_PROCESS_TYPE_CONFIG);
        return lDnTypeInfo.getDnTypeInfo(aClientId);
    }

    public static Map<String, DlrTypeInfo> getDnTypeInfoMap()
    {
        final InmemAccountDnTypeMappingInfo lDnTypeInfo = (InmemAccountDnTypeMappingInfo) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.DN_PROCESS_TYPE_CONFIG);
        return lDnTypeInfo.getDnTypeInfoMap();
    }

    public static void goToSleep()
    {

        try
        {
            Thread.sleep(100L);
        }
        catch (final InterruptedException e)
        {
            // ignore
        }
    }

    private static String getAppConfigValueAsString(
            ConfigParamConstants aConfigParamConstant)
    {
        final ApplicationConfiguration lAppConfiguration = (ApplicationConfiguration) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.APPLICATION_CONFIG);
        return lAppConfiguration.getConfigValue(aConfigParamConstant.getKey());
    }

    public static int getMaxRedisRecordsFetchLen()
    {
        return CommonUtility.getInteger(getAppConfigValueAsString(ConfigParamConstants.MAX_DEL_REDIS_KEYS_FETCH_LEN), 500);
    }

}
