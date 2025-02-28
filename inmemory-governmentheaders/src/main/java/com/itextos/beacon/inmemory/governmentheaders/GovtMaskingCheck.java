package com.itextos.beacon.inmemory.governmentheaders;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Constants;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.ItextosClient;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;

public class GovtMaskingCheck
{

    private static final Log log = LogFactory.getLog(GovtMaskingCheck.class);

    private GovtMaskingCheck()
    {}

    public static Map<String, String> getGovtHeaderMasking(
            String aClientId,
            String aCircle,
            String aHeader)
    {
        if (log.isDebugEnabled())
            log.debug("getGovtHeaderMasking() clientId : " + aClientId + " circle:" + aCircle + " header:" + aHeader);

        aCircle = aCircle != null ? aCircle.trim().toUpperCase() : "";
        aHeader = aHeader != null ? aHeader.trim().toUpperCase() : "";

        final GovtHeaderMasking lGovtHeaderMasking = (GovtHeaderMasking) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.GOVT_HEADER_MASKING);

        for (int logicIndex = 1; logicIndex < 13; logicIndex++)
        {
            final String              key                 = generateMapKey(logicIndex, aClientId, aCircle, aHeader);
            final Map<String, String> lGovtMaskingDetails = lGovtHeaderMasking.getGovrMaskingDetails(key);

            if (lGovtMaskingDetails != null)
            {
                if (log.isDebugEnabled())
                    log.debug("getGovtHeaderMasking() found for createListKey : " + key + " govtMaskingDetails:" + lGovtMaskingDetails);
                return lGovtMaskingDetails;
            }
        }
        return null;
    }

    // headerid masking logic
    private static String generateMapKey(
            int aLogicid,
            String aClientId,
            String aCircle,
            String aHeader)
    {
        final ItextosClient lClient   = new ItextosClient(aClientId);

        String              lClientId = lClient.getClientId();

        switch (aLogicid)
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
                aHeader = Constants.NULL_STRING;
                break;

            case 5:
                lClientId = lClient.getAdmin();
                aHeader = Constants.NULL_STRING;
                break;

            case 6:
                lClientId = lClient.getSuperAdmin();
                aHeader = Constants.NULL_STRING;
                break;

            case 7:
                aCircle = Constants.NULL_STRING;
                break;

            case 8:
                lClientId = lClient.getAdmin();
                aCircle = Constants.NULL_STRING;
                break;

            case 9:
                lClientId = lClient.getSuperAdmin();
                aCircle = Constants.NULL_STRING;
                break;

            case 10:
                aHeader = Constants.NULL_STRING;
                aCircle = Constants.NULL_STRING;
                break;

            case 11:
                lClientId = lClient.getAdmin();
                aHeader = Constants.NULL_STRING;
                aCircle = Constants.NULL_STRING;
                break;

            case 12:
                lClientId = lClient.getSuperAdmin();
                aHeader = Constants.NULL_STRING;
                aCircle = Constants.NULL_STRING;
                break;

            default:
                return null;
        }
        return CommonUtility.combine(lClientId, aCircle, aHeader);
    }

}