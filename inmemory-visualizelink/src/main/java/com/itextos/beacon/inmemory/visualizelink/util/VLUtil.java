package com.itextos.beacon.inmemory.visualizelink.util;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.inmemory.visualizelink.ExcludeVisualizeLinks;
import com.itextos.beacon.inmemory.visualizelink.IncludeVisualizeLinks;
import com.itextos.beacon.inmemory.visualizelink.VisualizeLinkUtil;

public class VLUtil
{

    private static final Log log = LogFactory.getLog(VLUtil.class);

    private VLUtil()
    {}

    public static List<String> getExcludeUrls(
            String aClientId)
    {
        final ExcludeVisualizeLinks lExcludeVLInfo = (ExcludeVisualizeLinks) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.EXCLUDE_VISUALIZE_LINK);

        return lExcludeVLInfo.getExcludeDomainUrls(aClientId);
    }

    public static Map<String, String> getIncludeUrls(
            String aClientId)
    {
        final IncludeVisualizeLinks lIncludeVLInfo = (IncludeVisualizeLinks) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.INCLUDE_VISUALIZE_LINK);

        return lIncludeVLInfo.getIncludeVLInfo(aClientId);
    }

    public static String getEmptyIncludeUrls(
            String aClientId)
    {
        final IncludeVisualizeLinks lIncludeVLInfo = (IncludeVisualizeLinks) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.INCLUDE_VISUALIZE_LINK);

        return lIncludeVLInfo.getEmptyIncludeVLInfo(aClientId);
    }

    public static String getVLInfo(
            String aKey)
    {
        final VisualizeLinkUtil lVLInfo = (VisualizeLinkUtil) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.VISUALIZE_LINK);

        return lVLInfo.getVisualizeLinkIds(aKey);
    }

}
