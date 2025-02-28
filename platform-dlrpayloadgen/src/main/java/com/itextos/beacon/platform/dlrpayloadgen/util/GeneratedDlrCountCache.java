package com.itextos.beacon.platform.dlrpayloadgen.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GeneratedDlrCountCache
{

    private static Log                        log              = LogFactory.getLog(GeneratedDlrCountCache.class);

    private Map<String, Map<String, Integer>> mDlrGeneratedMap = null;

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final GeneratedDlrCountCache INSTANCE = new GeneratedDlrCountCache();

    }

    public static GeneratedDlrCountCache getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    public void incrementAndPutToMap(
            String aKey,
            String aRouteId,
            int aCount)
    {
        if (mDlrGeneratedMap == null)
            resetDnGenerateMap();

        if (log.isDebugEnabled())
            log.debug("key:" + aKey + " routeid:" + aRouteId);

        if (mDlrGeneratedMap.get(aKey) == null)
        {
            final Map<String, Integer> innerMap = new HashMap<>();
            innerMap.put(aRouteId, aCount);
            mDlrGeneratedMap.put(aKey, innerMap);
        }
        else
        {
            final Map<String, Integer> innerMap   = mDlrGeneratedMap.get(aKey);
            final Integer              routeCount = innerMap.get(aRouteId) == null ? aCount : innerMap.get(aRouteId) + aCount;
            innerMap.put(aRouteId, routeCount);
            mDlrGeneratedMap.put(aKey, innerMap);
        }
    }

    public Map<String, Integer> getGeneratedDnCountByKey(
            String key)
    {
        if (mDlrGeneratedMap != null)
            return mDlrGeneratedMap.get(key);
        return null;
    }

    public Map<String, Integer> removeGeneratedDnCountByKey(
            String key)
    {
        return mDlrGeneratedMap.remove(key);
    }

    public void resetDnGenerateMap()
    {
        mDlrGeneratedMap = new HashMap<>();
    }

}
