package com.itextos.beacon.platform.rc.util;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Constants;
import com.itextos.beacon.commonlib.constants.CustomFeatures;
import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.commonlib.constants.RouteLogic;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.message.utility.MessageUtil;
import com.itextos.beacon.commonlib.pattern.PatternCache;
import com.itextos.beacon.commonlib.pattern.PatternCheckCategory;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.RoundRobin;
import com.itextos.beacon.inmemory.customfeatures.InmemCustomFeatures;
import com.itextos.beacon.inmemory.governmentheaders.GovtMaskingCheck;
import com.itextos.beacon.inmemory.governmentheaders.GovtMaskingExcludeCheck;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.inmemory.routeinfo.util.RouteUtil;

public class RFinder
{

    private static Log           log = LogFactory.getLog(RFinder.class);

    private final MessageRequest mMessageRequest;

    public RFinder(
            MessageRequest aMessageRequest)
    {
        this.mMessageRequest = aMessageRequest;
    }

    public boolean findAndSetRoute()
    {
        final String lClientId = mMessageRequest.getClientId();

        isGovtHeader();

        final boolean isBypassMobileRoute         = CommonUtility.isEnabled(getCutomFeatureValue(lClientId, CustomFeatures.BYPASS_MOBILE_ROUTE));
        boolean       lSetRouteTryWithMobileRoute = false;

        if (!isBypassMobileRoute)
            lSetRouteTryWithMobileRoute = setRouteTryWithMobileRoute();

        if (lSetRouteTryWithMobileRoute)
            return true;

        final boolean lSpecialSeriesPresent = mMessageRequest.isTreatDomesticAsSpecialSeries();

        if (lSpecialSeriesPresent)
        {
            // Special series not configured in mobile_route_config
            log.error("Special series 15 digit number is not configured in mobile_route_config - rejecting it as INVALID ROUTEID");
            return false;
        }

        final String lRouteId = getSmsRouteByAccRouteTemplate();

        if ((lRouteId != null) && setRouteByTemplateRoute(lRouteId))
        {
            mMessageRequest.setClientTemplateMatch(true);
            return true;
        }

        return setRouteTryWithCustomRoute();
    }

    private void isGovtHeader()
    {

        try
        {
            final String lHeader = MessageUtil.getHeaderId(mMessageRequest);
            final String alpha   = RCUtil.getGovtRoute(lHeader);

            if (alpha != null)
            {
                if (log.isDebugEnabled())
                    log.debug("isGovtHeader() Alpha found in govt_headers******* Base Message Id:" + mMessageRequest.getBaseMessageId() + " alpha:" + alpha);
                mMessageRequest.setAlpha(alpha);
            }
        }
        catch (final Exception ignore)
        {}
    }

    private boolean setRouteTryWithMobileRoute()
    {

        final String lMobileNumber = mMessageRequest.getMobileNumber();

        try
        {

            if (log.isDebugEnabled())
                log.debug(mMessageRequest.getBaseMessageId()+" : setRouteTryWithMobileRoute() : "+lMobileNumber);
            String            lStrMsgType = "";

            final MessageType lMsgType    = mMessageRequest.getMessageType();

            // TODO : Why we have to do up to last.
            for (int i = lMobileNumber.length(); i > 0; i--)
            {
                String lMobileRange = lMobileNumber.substring(0, i);

                lMobileRange += "~" + lMsgType.getKey();
                lStrMsgType   = "" + lMsgType.getKey();

                String lMobileRoute = RouteUtil.getFirstAttemptMobileRoute(lMobileRange);

         
                if (lMobileRoute == null)
                    continue;

                if (StringUtils.isNumeric(lMobileRoute))
                {
                    final String       groupid    = lMobileRoute + lStrMsgType;

                    final List<String> lRoutelist = RouteUtil.getGroupRouteList(groupid);

                    if (log.isDebugEnabled())
                        log.debug("Route List for group id : '" + groupid + "', RouteList : '" + lRoutelist + "'");

                    if ((lRoutelist == null) || lRoutelist.isEmpty())
                        return false;

                    final int lIndex = RouteUtil.getRRPointer(groupid, lRoutelist.size());
                    lMobileRoute = lRoutelist.get(lIndex - 1);
                }
                mMessageRequest.setRouteId(lMobileRoute);
                mMessageRequest.setRouteLogicId(CommonUtility.getInteger(RouteLogic.LOGICID.getKey()));

                if (log.isDebugEnabled())
                    log.debug("Mobile route set " + lMobileRoute + " MessageRequest " + mMessageRequest.getBaseMessageId());

                return true;
            }
        }
        catch (final Exception e)
        {
            log.error("setRouteTryWithMobileRoute() exception ", e);
            e.printStackTrace();
        }
        if (log.isDebugEnabled())
            log.debug("Mobile route Not Avilable for MessageRequest " + mMessageRequest.getBaseMessageId()+" : lMobileNumber : "+lMobileNumber);


        return false;
    }

    private String getSmsRouteByAccRouteTemplate()
    {

        try
        {
            final String  lClientId             = mMessageRequest.getClientId();

            final boolean isAccRouteTemplateChk = CommonUtility.isEnabled(getCutomFeatureValue(lClientId, CustomFeatures.ACC_ROUTING_TEMPLATE_CHK));

            if (isAccRouteTemplateChk)
            {
                final Map<String, String> lCustomRouteTemplateMap = RouteUtil.getCustomRouteTemplates(lClientId);

                if ((lCustomRouteTemplateMap != null) && !lCustomRouteTemplateMap.isEmpty())
                {
                    if (log.isDebugEnabled())
                        log.debug("getSmsRouteByAccRouteTemplate checking pattern match");
                    final String lLongMessage = mMessageRequest.getLongMessage();

                    for (final Entry<String, String> pattern : lCustomRouteTemplateMap.entrySet())
                        try
                        {

                            if (PatternCache.getInstance().isPatternMatch(PatternCheckCategory.TEMPLATE_CHECK, pattern.getKey(), lLongMessage))
                            {
                                final String lRouteId = pattern.getValue();
                                if (log.isDebugEnabled())
                                    log.debug("getSmsRouteByAccRouteTemplate pattern matched: " + pattern.getKey() + " Rouet ID:" + lRouteId);
                                return lRouteId;
                            }
                        }
                        catch (final Exception ignore)
                        {}
                }
            }
        }
        catch (final Exception e)
        {
            log.error("getSmsRouteByAccRouteTemplate", e);
        }

        return null;
    }

    private boolean setRouteByTemplateRoute(
            String aSMSRoute)
    {

        try
        {
            if (log.isDebugEnabled())
                log.debug("setRouteByTemplateRoute check");

            for (int logicid = 14; logicid < 17; logicid++)
            {
                final String  lKey               = RouteUtil.createMapKey(mMessageRequest, logicid, aSMSRoute, true);

                final boolean lCheckForSomething = checkForCustomRoutes(logicid, lKey);
                if (lCheckForSomething)
                    return true;
            }
        }
        catch (final Exception e)
        {
            log.error("Exception in setRouteByTemplateRoute " + mMessageRequest, e);
        }
        return false;
    }

    private boolean setRouteTryWithCustomRoute()
    {

        try
        {
            if (log.isDebugEnabled())
                log.debug("Second Attempt");

            for (int logicid = 2; logicid < 18; logicid++)
            {
                final String  lKey               = RouteUtil.createMapKey(mMessageRequest, logicid, "", true);

                final boolean lCheckForSomething = checkForCustomRoutes(logicid, lKey);
                if (lCheckForSomething)
                    return true;
            }
        }
        catch (final Exception e)
        {
            log.error("Exception in setRouteTryWithCustomRoute " + mMessageRequest, e);
        }
        return false;
    }

    private boolean checkForCustomRoutes(
            int aLogicid,
            String aKey)
    {
    	
    	/*
        if (log.isDebugEnabled())
            log.debug(" mapkey " + aLogicid + " : " + aKey);
    	 */
        String lRouteId = CommonUtility.nullCheck(RouteUtil.getSecondAttemptCustomRoute(aKey), true);

        if (lRouteId.isEmpty())
            return false;

        
        if (StringUtils.isNumeric(lRouteId))
        {
            final MessageType lMsgType = mMessageRequest.getMessageType();
            final String      lGroupId = lRouteId + lMsgType.getKey();

            if (RouteUtil.isRouteGroupAvailable(lGroupId))
            {
                final List<String> routelist = RouteUtil.getGroupRouteList(lGroupId);

                if ((routelist == null) || routelist.isEmpty())
                    return false;

                final int lIndex = RouteUtil.getRRPointer(aKey, routelist.size());
                lRouteId = routelist.get(lIndex - 1);
            }
        }else {
        

            if (log.isDebugEnabled())
                log.debug(" checkForCustomRoutes Matched Logicid " + aLogicid + " : " + aKey+ " : lRouteId : "+lRouteId);
        
        }

        mMessageRequest.setRouteId(lRouteId);
        mMessageRequest.setRouteLogicId(aLogicid);
        return true;
    }

    public boolean maskGovtHeader()
    {
        final String lClientId      = mMessageRequest.getClientId();
        final String lBaseMessageId = mMessageRequest.getBaseMessageId();
        final String lMobileNumber  = mMessageRequest.getMobileNumber();
        final String lCircle        = CommonUtility.nullCheck(mMessageRequest.getCircle(), true);
        final String lCarrier       = CommonUtility.nullCheck(mMessageRequest.getCarrier(), true);
        final String lHeader        = MessageUtil.getHeaderId(mMessageRequest);

        try
        {
            final boolean isWhiteListed = RCUtil.checkNumberWhiteListed(lMobileNumber);
            if (log.isDebugEnabled())
                log.debug("  isWhiteListed:" + isWhiteListed + " Message Id:" + lBaseMessageId);

            if (isWhiteListed)
                return false;

            final Map<String, String> lGovtMaskingHeader = GovtMaskingCheck.getGovtHeaderMasking(lClientId, lCircle, lHeader);
            if (log.isDebugEnabled())
                log.debug("maskGovtHeader() Client ID:" + lClientId + " carrier:" + lCarrier + " circle:" + lCircle + " maskedsHeader:" + lGovtMaskingHeader + " mid:" + lBaseMessageId);

            if (lGovtMaskingHeader == null)
                return false;

            if (GovtMaskingExcludeCheck.getGovtHeaderExcluded(lClientId, lCarrier, lCircle))
            {
                if (log.isDebugEnabled())
                    log.debug("maskGovtHeader() Found exclude in govt_header_carr_cir_exclude for Client ID:" + lClientId + " operator:" + lCarrier + " circle:" + lCircle + " mid:" + lBaseMessageId);
                return false;
            }

            String        lMaskedHeader  = lGovtMaskingHeader.get("masked_header");
            String        lGovtRouteId   = lGovtMaskingHeader.get("route_id");
            final boolean isRotateHeader = CommonUtility.isEnabled(lGovtMaskingHeader.get("rotate_header"));
            final String  lEntityId      = lGovtMaskingHeader.get("entity_id");

            mMessageRequest.setAlpha(Constants.GOVT_HEADER_MASKING_ALPHA);
            mMessageRequest.setAddSubClientHeader(false);
            mMessageRequest.setMaskedHeader(lHeader);

            if (isRotateHeader)
            {
                final List<String> lMaskedHeaderPool = RouteUtil.getMaskedHeaderPool();
                if (log.isDebugEnabled())
                    log.debug("lMaskedHeaderPool:" + lMaskedHeaderPool);

                if (!lMaskedHeaderPool.isEmpty())
                {
                    final int index = RoundRobin.getInstance().getCurrentIndex("global_header_mask_rotaion_pool", lMaskedHeaderPool.size());
                    lMaskedHeader = lMaskedHeaderPool.get(index - 1);
                }
            }

            lGovtRouteId = setGovtGroupRoute(lGovtRouteId, lBaseMessageId);

            if (mMessageRequest.isDltCheckEnabled() && !isRotateHeader && !"".equals(lEntityId))
                mMessageRequest.setDltEntityId(lEntityId);

            MessageUtil.setHeaderId(mMessageRequest, lMaskedHeader);
            mMessageRequest.setRouteId(lGovtRouteId);
            mMessageRequest.setRouteLogicId(CommonUtility.getInteger(RouteLogic.GOVT_LOGIC_ID.getKey()));

            if (!mMessageRequest.isAddSubClientHeader())
                mMessageRequest.setClientHeader(mMessageRequest.getMaskedHeader());

            mMessageRequest.setIsHeaderMasked(Constants.ENABLED);
            return true;
        }
        catch (final Exception e)
        {
            log.error("maskGovtHeader() exception", e);
        }
        return false;
    }

    private static String setGovtGroupRoute(
            String aGovtRouteId,
            String aMessageId)
    {

        try
        {

            if (StringUtils.isNumeric(aGovtRouteId))
            {
                String       lGroupid  = aGovtRouteId + MessageType.TRANSACTIONAL.getKey();
                List<String> routelist = RouteUtil.getGroupRouteList(lGroupid);

                if (routelist == null)
                {
                    lGroupid  = aGovtRouteId + MessageType.PROMOTIONAL.getKey();
                    routelist = RouteUtil.getGroupRouteList(lGroupid);
                }

                if ((routelist != null) && !routelist.isEmpty())
                {
                    final int lIndex = RouteUtil.getRRPointer(lGroupid, routelist.size());
                    return routelist.get(lIndex - 1);
                }

                if (log.isDebugEnabled())
                    log.debug("maskGovtHeader() group is not enabled for Message Id :" + aMessageId + " groupid:" + aGovtRouteId);
            }
        }
        catch (final Exception e)
        {
            log.error("maskGovtHeader() exception while finding routeid from group", e);
        }
        return aGovtRouteId;
    }

    public static String getCutomFeatureValue(
            String aClientId,
            CustomFeatures aCustomFeature)
    {
        final InmemCustomFeatures lCustomFeatures = (InmemCustomFeatures) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.CUSTOM_FEATURES);
        return lCustomFeatures.getValueOfCustomFeature(aClientId, aCustomFeature.getKey());
    }

}
