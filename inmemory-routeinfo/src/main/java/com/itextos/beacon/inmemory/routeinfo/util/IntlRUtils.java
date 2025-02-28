package com.itextos.beacon.inmemory.routeinfo.util;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.message.utility.MessageUtil;
import com.itextos.beacon.commonlib.pattern.PatternCache;
import com.itextos.beacon.commonlib.pattern.PatternCheckCategory;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.Name;
import com.itextos.beacon.inmemory.intlrouteinfo.cache.IntlRouteConfigInfo;
import com.itextos.beacon.inmemory.intlrouteinfo.cache.MccMncRoutes;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.inmemory.routeinfo.cache.ClientKeywordHeaderInfo;
import com.itextos.beacon.inmemory.routeinfo.cache.CountryKeywordHeaderRoute;
import com.itextos.beacon.inmemory.routeinfo.cache.CountryKeywordHeadersInfo;
import com.itextos.beacon.inmemory.routeinfo.cache.CountrySeriesHeaderInfo;
import com.itextos.beacon.inmemory.routeinfo.cache.IntlGlobalKeywordHeaderInfo;
import com.itextos.beacon.inmemory.routeinfo.cache.IntlHeaderInfo;
import com.itextos.beacon.inmemory.routeinfo.cache.IntlRouteHeader;
import com.itextos.beacon.inmemory.routeinfo.cache.LoadCarrierSupportHeaders;
import com.itextos.beacon.inmemory.routeinfo.cache.MobileNumberRouteInfo;

public class IntlRUtils
{

    private static final int INTL_ROUTE_LOGIC_ID = -11;
    private static Log       log                 = LogFactory.getLog(IntlRUtils.class);

    private IntlRUtils()
    {}

    public static boolean canProcessMessageByGlobalTemplate(
            MessageRequest aMessageRequest)
    {
        final String lKeyword = aMessageRequest.getLongMessage();
        final String lHeader  = MessageUtil.getHeaderId(aMessageRequest);

        try
        {
            if (log.isDebugEnabled())
                log.debug("Keyword : '" + lKeyword + "', Header : '" + lHeader + "'");

            return getIntlGlobalHeaderKeywordMatchs(lKeyword, lHeader);
        }
        catch (final Exception e)
        {
            log.error("Exception while checking whether to process the message or not. Keyword : '" + lKeyword + "', Header : '" + lHeader + "'", e);
            return true;
        }
    }

    public static boolean setRouteUsingMobileNumber(
            MessageRequest aMessageRequest)
    {
        final String lClientId     = aMessageRequest.getClientId();
        final String lMobileNumber = aMessageRequest.getMobileNumber();

        try
        {
            if (log.isDebugEnabled())
                log.debug("Mobile Number : '" + lMobileNumber + "'");

            final DerivedRoute derivedRoute = getIntlMobileRoute(lClientId, lMobileNumber);

            if (log.isDebugEnabled())
                log.debug("Mobile Number : '" + lMobileNumber + "' Derived Route : '" + derivedRoute + "'");

            return updateRouteDetails(aMessageRequest, derivedRoute);
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the Route based on the Mobile. Mobile : '" + lMobileNumber + "'", e);
        }
        return false;
    }

    public static boolean[] checkForSeriesBasedLookup(
            MessageRequest aMessageRequest)
    {
        final boolean isRouteFound              = true;
        final boolean isInvalidIntlMobileLength = false;
        final boolean isCCNotInRange            = false;
        /*
         * final PlatformStatusCode lStatusError =
         * IntlRouteUtil.checkAndUpdateRouteBasedOnIntlRoute(aMessageRequest);
         * if (log.isDebugEnabled())
         * log.
         * debug("Error Desc while getting the route based on route_intl errorDesc _1 : '"
         * + lStatusError + "'");
         * if (lStatusError != null)
         * {
         * isRouteFound = false;
         * if (lStatusError == PlatformStatusCode.INTL_INVALID_MOBILE_LENGTH)
         * isInvalidIntlMobileLength = true;
         * else
         * if (lStatusError == PlatformStatusCode.INTL_COUNTRY_CODE_RANGE_NOT_AVAILABLE)
         * isCCNotInRange = true;
         * }
         */

        aMessageRequest.setRouteLogicId(INTL_ROUTE_LOGIC_ID);
        if (log.isDebugEnabled())
            log.debug("Route found based on route_intl : '" + isRouteFound + "', isInvalidIntlMobileLength : '" + isInvalidIntlMobileLength + "', isCCNotInRange : '" + isCCNotInRange + "'");

        return new boolean[]
        { isRouteFound, isInvalidIntlMobileLength, isCCNotInRange };
    }

    private static boolean updateRouteDetails(
            MessageRequest aMessageRequest,
            DerivedRoute aDerivedRoute)
    {

        if (aDerivedRoute != null)
        {
            /**
             * Route BasedOn RouteGroup will be updated after the splitting of the routes.
             */

            aMessageRequest.setRouteLogicId(aDerivedRoute.getLogicId());
            aMessageRequest.setRouteId(aDerivedRoute.getRouteId());

            /*
            if (!CommonUtility.nullCheck(aDerivedRoute.getDefaultHeader(), true).isEmpty())
                aMessageRequest.setIntlClientHeader(aDerivedRoute.getDefaultHeader());

			*/
            if (log.isDebugEnabled())
                log.debug("Updating the Route : '" + aDerivedRoute.getRouteId() + "', Logic ID : '" + aDerivedRoute.getLogicId() + "' Default Header : '" + aDerivedRoute.getDefaultHeader()
                        + "' for the MID : '" + aMessageRequest.getBaseMessageId() + "'");
            return true;
        }
        return false;
    }

    private static boolean getIntlGlobalHeaderKeywordMatchs(
            String aHeader,
            String aMessage)
    {
        final IntlGlobalKeywordHeaderInfo lGlobalKeywordHeaderInfo = (IntlGlobalKeywordHeaderInfo) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.INTL_GLOBAL_HEADER_KEYWORD);
        return (lGlobalKeywordHeaderInfo == null) ? false : lGlobalKeywordHeaderInfo.isKeywordHeaderMatchs(aMessage, aHeader);
    }

    private static DerivedRoute getIntlMobileRoute(
            String aClientId,
            String aMnumber)
    {
        final MobileNumberRouteInfo lMobileNumberRouteInfo = (MobileNumberRouteInfo) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.INTL_MOBILE_ROUTES);
        return lMobileNumberRouteInfo.getDerivedRoute(aClientId, aMnumber);
    }

    private static CountryKeywordHeaderRoute getIntlPriorityRoute(
            String aCountry,
            String aMessage,
            String aHeader)
    {
        final CountryKeywordHeadersInfo lCountryKeywordHeadersInfo = (CountryKeywordHeadersInfo) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.INTL_PRIORITY_ROUTE);
        return lCountryKeywordHeadersInfo.getPriorityRouteID(aCountry, aMessage, aHeader);
    }

    private static DerivedRoute getDerivedRoute(
            String aClientId,
            String aCountry,
            String aMessage,
            String aHeader)
    {
        final ClientKeywordHeaderInfo lClientKeywordHeaderInfo = (ClientKeywordHeaderInfo) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.INTL_CLIENT_KEYWORD_HEADER);
        return lClientKeywordHeaderInfo.getDerivedRoute(aClientId, aCountry, aMessage, aHeader);
    }

    private static DerivedRoute getClientCountrySeriesHeaderDerivedRoute(
            String aClientId,
            String aCountry,
            String aCarrier,
            String aHeader)
    {
        final CountrySeriesHeaderInfo lCountrySeriesHeaderInfo = (CountrySeriesHeaderInfo) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.INTL_CLIENT_COUNTRY_SERIES_HEADER);
        return lCountrySeriesHeaderInfo.getClientCountrySeriesHeaderDerivedRoute(aClientId, aCountry, aCarrier, aHeader);
    }

    public static Map<String, String> getIntlRouteHeaderInfo(
            String aKey)
    {
        final IntlRouteHeader lIntlRouteHeader = (IntlRouteHeader) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.INTL_ROUTE_HEADERS);
        return lIntlRouteHeader.getIntlRouteHeaderInfo().get(aKey);
    }

    public static Map<String, String> getCarrierSupportHeaders()
    {
        final LoadCarrierSupportHeaders lCarrierSupportHeaders = (LoadCarrierSupportHeaders) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.INTL_CARRIER_SUPPORT_HEADERS);
        return lCarrierSupportHeaders.getCarrierSupportHeaders();
    }

    public static void setRouteBasesdOnOtherCriteria(
            MessageRequest aMessageRequest)
    {

        try
        {
        	
            
            
            IntlRouteConfigInfo lIntlRouteConfigTemp=getMccMncRouteInfo(aMessageRequest);
            
            boolean isRouteFound=false;
            if(lIntlRouteConfigTemp!=null) {
            	
            	isRouteFound=true;
            	
            	aMessageRequest.setRouteId(lIntlRouteConfigTemp.getRouteId());
            }
         

            if (!isRouteFound)
            {

            	isRouteFound = setRouteUsingCountryKeywordTemplate(aMessageRequest);

            }
            if (log.isDebugEnabled())
                log.debug("Route found based on intl_country_header_template: '" + isRouteFound + "'");

            if (!isRouteFound)
            {
                isRouteFound = setRouteUsingAccountKeywordHeader(aMessageRequest);

                if (log.isDebugEnabled())
                    log.debug("Route found based on intl_client_header_template: '" + isRouteFound + "'");
            }

            if (!isRouteFound)
            {
                isRouteFound = setRouteUsingCountrySeriesHeader(aMessageRequest);

                if (log.isDebugEnabled())
                    log.debug("Route found based on intl_client_route_info: '" + isRouteFound + "'");
            }

            if (!isRouteFound)
            {
                if (log.isDebugEnabled())
                    log.debug("Internation standard route was set here. Route ID : '" + aMessageRequest.getIntlStandardRouteId() + "'");
                aMessageRequest.setRouteId(aMessageRequest.getIntlStandardRouteId());
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the route.", e);
        }
    }
    
    
    
  private static IntlRouteConfigInfo getMccMncRouteInfo(MessageRequest aMessageRequest) {
		
        final MccMncRoutes lMccMnceRoutes = (MccMncRoutes) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.MCC_MNC_ROUTES);

        aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getBaseMessageId()+" IntlRouteConfigInfo lMccMnceRoutes : "+lMccMnceRoutes); 

        if(lMccMnceRoutes!=null) {
        	
        	

        	IntlRouteConfigInfo lIntlRouteConfigInfo= lMccMnceRoutes.getMccMncRoute(aMessageRequest.getClientId(), aMessageRequest.getCountry(), aMessageRequest.getMcc(), aMessageRequest.getMnc());
        	
        	if(lIntlRouteConfigInfo==null) {
        		
        		lIntlRouteConfigInfo= lMccMnceRoutes.getMccMncRoute(aMessageRequest.getClientId(), aMessageRequest.getCountry(),  CommonUtility.nullCheck(null, true), CommonUtility.nullCheck(null, true));
            	
        	}
        	
        	
        	if(lIntlRouteConfigInfo==null) {
        		
        		lIntlRouteConfigInfo= lMccMnceRoutes.getMccMncRoute(CommonUtility.nullCheck(null, true), aMessageRequest.getCountry(), CommonUtility.nullCheck(null, true), CommonUtility.nullCheck(null, true));
            	
        	}
        	
        	
        	return lIntlRouteConfigInfo;
        }
        return null;
       }


    public static boolean setRouteUsingCountryKeywordTemplate(
            MessageRequest aMessageRequest)
    {
        final String lCountry = CommonUtility.nullCheck(aMessageRequest.getCountry(), true);
        final String lMessage = aMessageRequest.getLongMessage();
        final String lHeader  = MessageUtil.getHeaderId(aMessageRequest);

        try
        {
            if (log.isDebugEnabled())
                log.debug("Country : '" + lCountry + "' keyword : '" + lMessage + "', Header : '" + lHeader + "'");

            final CountryKeywordHeaderRoute lCountryKeywordHeader = getIntlPriorityRoute(lCountry, lMessage, lHeader);

            if (log.isDebugEnabled())
                log.debug("Country : '" + lCountry + "' keyword : '" + lMessage + "',Header : '" + lHeader + "' priorityRouteID : '" + lCountryKeywordHeader + "'");

            if (lCountryKeywordHeader != null)
            {
                /**
                 * Route BasedOn RouteGroup will be updated after the splitting of the routes.
                 */

                aMessageRequest.setSmsPriority(lCountryKeywordHeader.getPriority());
                aMessageRequest.setRouteLogicId(lCountryKeywordHeader.getLogicId());
                aMessageRequest.setRouteId(lCountryKeywordHeader.getRouteId());

                if (log.isDebugEnabled())
                    log.debug("Updating the Route : '" + lCountryKeywordHeader.getRouteId() + "', Logic ID : '" + lCountryKeywordHeader.getLogicId() + "', Priority : '"
                            + lCountryKeywordHeader.getPriority() + "'" + lCountryKeywordHeader.getRouteId() + " for the MID : '" + aMessageRequest.getBaseMessageId() + "'");
                return true;
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the Route based on the country wise keyword template. Country : '" + lCountry + "' keyword : '" + lMessage + "', Header : '" + lHeader + "'", e);
        }
        return false;
    }

    public static boolean setRouteUsingAccountKeywordHeader(
            MessageRequest aMessageRequest)
    {
        final String lClientId = aMessageRequest.getClientId();
        final String lCountry  = CommonUtility.nullCheck(aMessageRequest.getCountry(), true);
        final String lKeyword  = aMessageRequest.getLongMessage();
        final String lHeader   = MessageUtil.getHeaderId(aMessageRequest);

        try
        {
            if (log.isDebugEnabled())
                log.debug("ClientId : '" + lClientId + "', Country : '" + lCountry + "', Keyword : '" + lKeyword + "', Header : '" + lHeader + "'");

            final DerivedRoute derivedRoute = getDerivedRoute(lClientId, lCountry, lKeyword, lHeader);

            if (log.isDebugEnabled())
                log.debug("ClientId : '" + lClientId + "', Country : '" + lCountry + "', Keyword : '" + lKeyword + "', Header : '" + lHeader + "', derivedRoute : '" + derivedRoute + "'");

            return updateRouteDetails(aMessageRequest, derivedRoute);
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the Route based on the Account, country wise keyword template. ClietId: '" + lClientId + "', Country : '" + lCountry + "', Keyword : '" + lKeyword
                    + "', Header : '" + lHeader + "'", e);
        }
        return false;
    }

    public static boolean setRouteUsingCountrySeriesHeader(
            MessageRequest aMessageRequest)
    {
        final String lClientId = aMessageRequest.getClientId();
        final String lCountry  = CommonUtility.nullCheck(aMessageRequest.getCountry(), true);
        final String lCarrier  = CommonUtility.nullCheck(aMessageRequest.getCarrier(), true);
        final String lHeader   = MessageUtil.getHeaderId(aMessageRequest);

        try
        {
            if (log.isDebugEnabled())
                log.debug("Client Id : '" + lClientId + "', Country : '" + lCountry + "', Carrier : '" + lCarrier + "', Header : '" + lHeader + "'");

            final DerivedRoute derivedRoute = getClientCountrySeriesHeaderDerivedRoute(lClientId, lCountry, lCarrier, lHeader);

            if (log.isDebugEnabled())
                log.debug("Client Id : '" + lClientId + "', Country : '" + lCountry + "', Carrier : '" + lCarrier + "', Header : '" + lHeader + "', derivedRoute : '" + derivedRoute + "'");

            return updateRouteDetails(aMessageRequest, derivedRoute);
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the Route based on the Account Country Series and Header. Client Id : '" + lClientId + "', Country : '" + lCountry + "', Carrier : '" + lCarrier
                    + "', Header : '" + lHeader + "'", e);
        }
        return false;
    }

    public static Object verifyKeywordInMessage(
            Map<String, ?> aKeywordsMap,
            String aMessage,
            boolean aLookForDotStart)
    {
        Object lRouteId = null;

        if (log.isDebugEnabled())
            log.debug("Keywords Map : " + aKeywordsMap);

        if (aKeywordsMap != null)
        {
            if (aLookForDotStart)
                lRouteId = aKeywordsMap.get(CommonUtility.DOT_STAR);

            if (lRouteId != null)
                return lRouteId;

            for (final String lEntry : aKeywordsMap.keySet())
            {
                /** This check is not required as we checked before the for loop. */
                /**
                 * Overriding the previous comment. As 'aLookForDotStart' as coming as false. we
                 * should not use that
                 */
                if (CommonUtility.DOT_STAR.equals(lEntry))
                    continue;

                if (PatternCache.getInstance().isPatternMatch(PatternCheckCategory.TEMPLATE_CHECK, lEntry, aMessage))
                {
                    lRouteId = aKeywordsMap.get(lEntry);
                    break;
                }
            }
        }

        return lRouteId;
    }

    public static String getRouteBasedOnRouteGroupID(
            MessageRequest aMessageRequest)
    {
        final String aRouteId = aMessageRequest.getRouteId();
        final int    logicId  = aMessageRequest.getRouteLogicId();

        if (log.isDebugEnabled())
            log.debug("Looking for the routes based on the route group for the route id : '" + aRouteId + "'");

        if (StringUtils.isNumeric(aRouteId))
        {
            final List<String> lRoutelist = RouteUtil.getGroupRouteList(aRouteId);

            if ((lRoutelist != null) && !lRoutelist.isEmpty())
            {
                final int lIndex = RouteUtil.getRRPointer(Integer.toString(logicId), lRoutelist.size());
                return lRoutelist.get(lIndex - 1);
            }
        }

        return aRouteId;
    }

    public static String getIntlHeaderInfo(
            String aCountry)
    {
        String               lHeaderRegEx    = null;
        final IntlHeaderInfo IIntlHeaderInfo = (IntlHeaderInfo) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.INTL_HEADER_INFO);

        if (IIntlHeaderInfo.getIntlHeaderInfo(aCountry) != null)
            lHeaderRegEx = IIntlHeaderInfo.getIntlHeaderInfo(aCountry);
        else
            lHeaderRegEx = IIntlHeaderInfo.getIntlHeaderInfo("ROW");

        return lHeaderRegEx;
    }

}
