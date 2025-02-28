package com.itextos.beacon.inmemory.intlrouteinfo.util;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.PlatformStatusCode;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.Name;
import com.itextos.beacon.inmemory.intlrouteinfo.cache.IntlRouteConfigInfo;
import com.itextos.beacon.inmemory.intlrouteinfo.cache.IntlRouteConfiguration;
import com.itextos.beacon.inmemory.intlrouteinfo.cache.MccMncRoutes;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;

public class IntlRouteUtil
{

    private static Log log = LogFactory.getLog(IntlRouteUtil.class);

    private IntlRouteUtil()
    {}

    public static PlatformStatusCode checkAndUpdateRouteBasedOnIntlRoute(
            MessageRequest aMessageRequest)
    {
        final String              lMnumber         = aMessageRequest.getMobileNumber();

        IntlRouteConfigInfo lIntlRouteConfig = getRouteIntlInfo(lMnumber);
        
        if (lIntlRouteConfig == null)
            return PlatformStatusCode.INTL_COUNTRY_CODE_RANGE_NOT_AVAILABLE;
        
        if (!isValidMobileLengh(lMnumber, lIntlRouteConfig))
        {
            // Setting country here to send to billing tables.
            if (lIntlRouteConfig.getCountry() != null)
                aMessageRequest.setCountry(lIntlRouteConfig.getCountry());
            return PlatformStatusCode.INTL_INVALID_MOBILE_LENGTH;
        }

        aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getBaseMessageId()+" mccmnc route : "+lIntlRouteConfig); 

     



      
       

        setCarrierInfo(aMessageRequest, lIntlRouteConfig);
        return null;
    }

  
	private static IntlRouteConfigInfo getRouteIntlInfo(
            String aMNumber)
    {

        for (int index = aMNumber.length(); index > 0; index--)
        {
            final String                           lCounteryCode    = aMNumber.substring(0, index);
            final Map<String, IntlRouteConfigInfo> lIntlRouteConfig = getIntlMobileRoute();

            if (lIntlRouteConfig.containsKey(lCounteryCode))
                return lIntlRouteConfig.get(lCounteryCode);
        }
        return null;
    }

    private static boolean isValidMobileLengh(
            String aMobileNumber,
            IntlRouteConfigInfo aCarrierInfo)
    {
        final int lMinlength = CommonUtility.getInteger(aCarrierInfo.getMinMnumberLength());
        final int lMaxlength = CommonUtility.getInteger(aCarrierInfo.getMaxMnumberLength());

        return ((aMobileNumber.length() >= lMinlength) && (aMobileNumber.length() <= lMaxlength));
    }

    private static void setCarrierInfo(
            MessageRequest aMesaageRequest,
            IntlRouteConfigInfo ccinfo)
    {
        aMesaageRequest.setCountry(CommonUtility.nullCheck(ccinfo.getCountry()));
        aMesaageRequest.setCarrier(CommonUtility.nullCheck(ccinfo.getCarrier()));
        aMesaageRequest.setIntlCarrierNetwork(CommonUtility.nullCheck(ccinfo.getCarrierNetwork()));

        if (ccinfo.getDefaultHeader() != null)
            aMesaageRequest.setIntlDefaultHeader(CommonUtility.nullCheck(ccinfo.getDefaultHeader()));

        aMesaageRequest.setIntlDefaultHeaderType(CommonUtility.nullCheck(ccinfo.getHeaderType()));
        aMesaageRequest.setIntlEconomicRouteId(CommonUtility.nullCheck(ccinfo.getEconomyRouteId()));
        aMesaageRequest.setIntlStandardRouteId(CommonUtility.nullCheck(ccinfo.getRouteId()));
        aMesaageRequest.setIntlHeaderSubType(ccinfo.getHeaderSubType());
    }

    private static Map<String, IntlRouteConfigInfo> getIntlMobileRoute()
    {
        final IntlRouteConfiguration lIntlRouteConfig = (IntlRouteConfiguration) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.INTL_ROUTE_CONFIGURATION);
        return lIntlRouteConfig.getIntlRouteConfig();
    }

}
