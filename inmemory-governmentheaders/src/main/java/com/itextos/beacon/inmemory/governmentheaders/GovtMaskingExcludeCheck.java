package com.itextos.beacon.inmemory.governmentheaders;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Constants;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.ItextosClient;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;

public class GovtMaskingExcludeCheck
{

    private static final Log log = LogFactory.getLog(GovtMaskingExcludeCheck.class);

    private GovtMaskingExcludeCheck()
    {}

    public static boolean getGovtHeaderExcluded(
            String aClientId,
            String aCarrier,
            String aCircle)
    {
        if (log.isDebugEnabled())
            log.debug("getGovtHeaderExcluded() esmeaddr : " + aClientId + " carrier:" + aCarrier + " circle:" + aCircle);

        aCarrier = aCarrier != null ? aCarrier.trim().toUpperCase() : "";
        aCircle  = aCircle != null ? aCircle.trim().toUpperCase() : "";

        final GovtHeaderExcludes lGovtHeaderExcludes = (GovtHeaderExcludes) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.GOVT_HEADER_EXCLUDE);

        for (int logicIndex = 1; logicIndex < 10; logicIndex++)
        {
            final String key = generateMapKey(logicIndex, aClientId, aCarrier, aCircle);

            if (lGovtHeaderExcludes.isRouteExclude(key))
            {
                if (log.isDebugEnabled())
                    log.debug("getGovtHeaderExcluded() found for ListKey : " + key);
                return true;
            }
        }
        return false;
    }

    // govt_senderid_carr_cir_exclude
    private static String generateMapKey(
            int aLogicId,
            String aClientId,
            String aCarrier,
            String aCircle)
    {
        final String        lKey      = null;

        final ItextosClient lClient   = new ItextosClient(aClientId);
        String              lClientId = lClient.getClientId();

        switch (aLogicId)
        {
            case 1:
                break;

            case 2:
                lClientId = lClient.getAdmin();
                break;

            case 3:
                lClientId = lClient.getSuperAdmin();
                break;

            case 4:
                aCircle = Constants.NULL_STRING;
                break;

            case 5:
                lClientId = lClient.getAdmin();
                aCircle = Constants.NULL_STRING;
                break;

            case 6:
                lClientId = lClient.getSuperAdmin();
                aCircle = Constants.NULL_STRING;
                break;

            case 7:
                aCarrier = Constants.NULL_STRING;
                break;

            case 8:
                lClientId = lClient.getAdmin();
                aCarrier = Constants.NULL_STRING;
                break;

            case 9:
                lClientId = lClient.getSuperAdmin();
                aCarrier = Constants.NULL_STRING;
                break;

            default:
                return null;
        }

        return CommonUtility.combine(lClientId, aCarrier, aCircle);
    }

}