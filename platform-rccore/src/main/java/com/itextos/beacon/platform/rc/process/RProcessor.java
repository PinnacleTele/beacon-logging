package com.itextos.beacon.platform.rc.process;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.PlatformStatusCode;
import com.itextos.beacon.commonlib.constants.RouteLogic;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.Name;
import com.itextos.beacon.inmemory.routeinfo.cache.RouteConfigInfo;
import com.itextos.beacon.inmemory.routeinfo.util.HeaderValidation;
import com.itextos.beacon.inmemory.routeinfo.util.RouteFinder;
import com.itextos.beacon.inmemory.routeinfo.util.RouteUtil;
import com.itextos.beacon.platform.rc.util.RCProducer;
import com.itextos.beacon.platform.rc.util.RCUtil;
import com.itextos.beacon.platform.rc.util.RFinder;

public class RProcessor
{

  //  private static final Log     log = LogFactory.getLog(RProcessor.class);
    private final MessageRequest mMessageRequest;

    public RProcessor(
            MessageRequest aMessageRequest)
    {
        this.mMessageRequest = aMessageRequest;
    }

    public void doRCProcess()
    {
        if ((mMessageRequest.getCarrier() == null) || (mMessageRequest.getCircle() == null))
            RCUtil.findAndSetCarrierCircle(mMessageRequest);

        PlatformStatusCode lError           = null;

        final String       lFailListRouteId = CommonUtility.nullCheck(RCUtil.checkDomesticFailList(mMessageRequest), true);
        final boolean      isSpecialSeries  = mMessageRequest.isTreatDomesticAsSpecialSeries();

        findNonFailListRoute(lFailListRouteId);

        lError = validateRoutes(mMessageRequest);

        if (lError == null)
        {
        	mMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(mMessageRequest.getFileId()+" :: To Validate Govt Route...");

            lError = doGovtRouteIdentifying(lFailListRouteId, isSpecialSeries);
        }

        if (lError != null)
            sendToBillerQueue(mMessageRequest, lError);
        else
            sendToQueue(mMessageRequest);
    }

    private PlatformStatusCode doGovtRouteIdentifying(
            String aFailListRouteId,
            boolean aIsSpecialSeries)
    {
        PlatformStatusCode lErrorCode = null;

        if ((aFailListRouteId.isEmpty()) && !aIsSpecialSeries)
        {
            // This line will be removed later - just added it
            final String lRouteLogicId = Integer.toString(mMessageRequest.getRouteLogicId());

            if (!RouteLogic.GOVT_LOGIC_ID.getKey().equals(lRouteLogicId))
            {
                lErrorCode = validateHeader(mMessageRequest, lRouteLogicId);

                if (lErrorCode != null)
                    return lErrorCode;

                lErrorCode = validateRoutes(mMessageRequest);

                if (lErrorCode != null)
                    return lErrorCode;

                // final boolean isInValidHeader =
                // HeaderValidation.isInvalidHeader(mMessageRequest);
                final String lBaseMessageId = CommonUtility.nullCheck(mMessageRequest.getBaseMessageId(), true);

                // govt header masking - do only for non dnd
                // numbers, if dnd numbers comes from validator process
                // them with received header (masking not needed)

                if (!mMessageRequest.isDndScrubbed())
                {
                    final RFinder lRFinder        = new RFinder(mMessageRequest);
                    final boolean lMaskGovtHeader = lRFinder.maskGovtHeader();

                    mMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(mMessageRequest.getFileId()+" :: mapping found in govt_header_masking****** mid:"+ lMaskGovtHeader );

                 
                
                }

                final String lRouteId = CommonUtility.nullCheck(mMessageRequest.getRouteId(), true);

                // check if the routeid is configured in route_config table
                if (!RCUtil.isAbsoluteRoute(lRouteId))
                    return PlatformStatusCode.INVALID_ROUTE_ID;

                /*
                 * if ((mMessageRequest.getIsHeaderMasked() == null) && isInValidHeader)
                 * return PlatformStatusCode.CUSTOMER_HEADER_POOL_FAILED;
                 */

                HeaderValidation.prefixDND(mMessageRequest);
            }

            final String  lMessageId              = mMessageRequest.getBaseMessageId();
            final String  lHeader                 = mMessageRequest.getHeader();
            final String  lClientId               = mMessageRequest.getClientId();
            final boolean isGlobalHeaderBlocklist = RCUtil.isGlobalHeaderBlocklist(lMessageId, lHeader);

            if (isGlobalHeaderBlocklist)
            {
                final boolean isSenderidAllowedForClient = RCUtil.isClientAllowedHeader(lMessageId, lHeader, lClientId);

                if (!isSenderidAllowedForClient)
                    return PlatformStatusCode.GLOBAL_HEADER_BLOCK_FAILED;
            }
        }
        return null;
    }

    private void findNonFailListRoute(
            String aFailListRouteId)
    {

        if (aFailListRouteId.isEmpty())
        {
       
        	mMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(mMessageRequest.getFileId()+" :: Going to execute normal routing logics" );

            final RFinder lRFinder = new RFinder(mMessageRequest);
            lRFinder.findAndSetRoute();
        }
    }

    private static PlatformStatusCode validateHeader(
            MessageRequest aMessageRequest,
            String lLogicId)
    {
        String       lRouteId      = CommonUtility.nullCheck(aMessageRequest.getRouteId(), true);
        final String lHeader       = CommonUtility.nullCheck(aMessageRequest.getHeader(), true);
        boolean      isValidHeader = HeaderValidation.isValidHeader(lHeader, lRouteId);

        // isValidHeader = true means routeid is open
        // route/header is whitelisted in header_route_status
        if (!isValidHeader)
        {
            
        	aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getFileId()+" ::"+lHeader + " Header blocked for route " + lRouteId );

            // checking in
            // header_fixed_routes/header_alternate_routes/header_priority_open_routes
            // for alternative routes
            boolean isHeaderFailedRoute = RouteFinder.setRouteTryWithHeaderFailedRoute(aMessageRequest);

            if (!isHeaderFailedRoute)
            {
                if (lLogicId.equalsIgnoreCase(RouteLogic.DEAULT.getKey()))
                    return PlatformStatusCode.ROUTE_BASED_HEADER_FAILED;

                // checking in priority_routes
                final boolean RouteTryWithDefaultRoute = RouteFinder.setRouteTryWithDefaultRoute(aMessageRequest);
                lRouteId = CommonUtility.nullCheck(aMessageRequest.getRouteId(), true);

                if (!RouteTryWithDefaultRoute)
                    return PlatformStatusCode.INVALID_ROUTE_ID;

                isValidHeader = HeaderValidation.isValidHeader(lHeader, lRouteId);

                // since routeid found in priority_routes,
                // going to check header for this route
                if (!isValidHeader)
                {
                    isHeaderFailedRoute = RouteFinder.setRouteTryWithHeaderFailedRoute(aMessageRequest);

                    if (!isHeaderFailedRoute)
                        return PlatformStatusCode.ROUTE_BASED_HEADER_FAILED;
                }
            }
        }
        return null;
    }

    private static PlatformStatusCode validateRoutes(
            MessageRequest aMessageRequest)
    {
        final String lRouteId = CommonUtility.nullCheck(aMessageRequest.getRouteId(), true);



        aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getFileId()+" :: validateRoutes() - Route Id : " + lRouteId);

        if (RCUtil.isExpiredRoute(lRouteId))
            return PlatformStatusCode.INTL_ROUTE_EXPIRED;

        if (!RCUtil.isAbsoluteRoute(lRouteId))
            return PlatformStatusCode.INVALID_ROUTE_ID;
        return null;
    }

    private static void sendToBillerQueue(
            MessageRequest aMessageRequest,
            PlatformStatusCode aPlatformStatusCode)
    {
        aMessageRequest.setSubOriginalStatusCode(aPlatformStatusCode.getStatusCode());
        RCProducer.sendToPlatformRejection(aMessageRequest);


        aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getFileId()+" :: Sent To Biller ");

       
    }

    private static void sendToQueue(
            MessageRequest aMessageRequest)
    {
        final String lRouteId = CommonUtility.nullCheck(aMessageRequest.getRouteId(), true);

        aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getFileId()+" :: sendToQueue() - Route Id : " + lRouteId);

        final RouteConfigInfo lRouteConfigInfo = RouteUtil.getRouteConfiguration(lRouteId);

  
        aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getFileId()+" :: sendToQueue() - Route Configuration : " + lRouteConfigInfo);

        final String lRouteType = lRouteConfigInfo.getRouteType();

        aMessageRequest.setRouteType(lRouteType);

        /*
         * if (lRouteConfigInfo.isPrefix() && (lRouteConfigInfo.getPrefix() != null))
         * aNunMessage.putValue(MiddlewareConstant.MW_PREFIX,
         * lRouteConfigInfo.getPrefix());
         */
        if (lRouteConfigInfo.getSmscid() != null)
            aMessageRequest.setSmscId(lRouteConfigInfo.getSmscid());
        if (lRouteConfigInfo.getDtimeFormat() != null)
            aMessageRequest.setCarrierDateTimeFormat(lRouteConfigInfo.getDtimeFormat());

        RCProducer.sendToCarrierHandover(aMessageRequest);
    
             
        aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getFileId()+" :: Sent to Carrier Handover " + lRouteId + " Success. ");

       
    }

}
