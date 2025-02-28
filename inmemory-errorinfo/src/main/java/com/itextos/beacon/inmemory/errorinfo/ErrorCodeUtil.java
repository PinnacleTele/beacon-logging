package com.itextos.beacon.inmemory.errorinfo;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.ItextosClient;
import com.itextos.beacon.inmemory.errorinfo.data.ErrorCategory;
import com.itextos.beacon.inmemory.errorinfo.data.IErrorInfo;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;

public class ErrorCodeUtil
{

    public static IErrorInfo getCarrierErrorCode(
            String aRouteId,
            String aErrorCode,
            String aErrorStatus)
    {
        final CarrierErrorInfoCollection carrierErrorInfoCollection = (CarrierErrorInfoCollection) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.CARRIER_ERROR_INFO);
        return carrierErrorInfoCollection.getErrorInfo(aRouteId, aErrorCode, aErrorStatus);
    }

    public static IErrorInfo getClientErrorCode(
            String aClientId,
            String aPlatformErrorCode)
    {
        final String clientId = CommonUtility.nullCheck(aClientId, true);

        if (!clientId.isEmpty())
        {
            final ItextosClient             client                    = new ItextosClient(aClientId);
            final ClientErrorInfoCollection clientErrorInfoCollection = (ClientErrorInfoCollection) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.CLIENT_ERROR_INFO);
            IErrorInfo                      lErrorInfo                = clientErrorInfoCollection.getErrorInfo(client.getClientId(), aPlatformErrorCode);

            if (lErrorInfo != null)
                return lErrorInfo;

            lErrorInfo = clientErrorInfoCollection.getErrorInfo(client.getAdmin(), aPlatformErrorCode);
            if (lErrorInfo != null)
                return lErrorInfo;

            lErrorInfo = clientErrorInfoCollection.getErrorInfo(client.getSuperAdmin(), aPlatformErrorCode);
            if (lErrorInfo != null)
                return lErrorInfo;
        }
        return getPlatformErrorCode(ErrorCategory.PLATFORM, aPlatformErrorCode);
    }

    public static IErrorInfo getPlatformErrorCode(
            ErrorCategory aErrorCategory,
            String aErrorCode)
    {
        final PlatformErrorInfoCollection platformErrorInfoCollection = (PlatformErrorInfoCollection) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.PLATFORM_ERROR_INFO);
        return platformErrorInfoCollection.getErrorInfo(aErrorCategory, aErrorCode);
    }

    private ErrorCodeUtil()
    {}

}