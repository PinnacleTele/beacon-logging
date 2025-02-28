package com.itextos.beacon.platform.ic.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.Constants;
import com.itextos.beacon.commonlib.constants.CustomFeatures;
import com.itextos.beacon.commonlib.constants.InterfaceStatusCode;
import com.itextos.beacon.commonlib.constants.InterfaceType;
import com.itextos.beacon.commonlib.constants.MessageClass;
import com.itextos.beacon.commonlib.constants.MessageClassLength;
import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.commonlib.constants.PlatformStatusCode;
import com.itextos.beacon.commonlib.constants.exception.InternationalSMSRateNotAvailableRuntimeException;
import com.itextos.beacon.commonlib.dndchecker.DNDCheck;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.message.utility.MessageUtil;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.MessageConvertionUtility;
import com.itextos.beacon.inmemory.configvalues.ApplicationConfiguration;
import com.itextos.beacon.inmemory.customfeatures.InmemCustomFeatures;
import com.itextos.beacon.inmemory.inmemdata.mccmnc.MCCMNCFinder;
import com.itextos.beacon.inmemory.inmemdata.mccmnc.MccMncInfo;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.inmemory.msgutil.cache.CarrierCircle;
import com.itextos.beacon.inmemory.msgutil.util.IndiaNPFinder;
import com.itextos.beacon.inmemory.spamcheck.util.IntlSpamCheckUtility;
import com.itextos.beacon.inmemory.spamcheck.util.SpamCheckUtility;
import com.itextos.beacon.inmemory.userheader.DomesticUserHeaderInfo;
import com.itextos.beacon.platform.intlprice.CalculateBillingPrice;
import com.itextos.beacon.platform.intlprice.CurrencyUtil;
import com.itextos.beacon.platform.msgflowutil.util.PlatformUtil;

public class ICUtility
{

    private static final Log    log                            = LogFactory.getLog(ICUtility.class);
    private static final String VL_UPPER                       = "[~VL:";
    private static final String VL_LOWER                       = "[~vl:";
    private static final String TILDA                          = "~]";

    private static String       mMobileSeriesLookupValueString = null;
    private static int[]        mMobileSeriesLookupValue       = null;

    private ICUtility()
    {}

    public static void doInterfaceValidation(
            MessageRequest aMessageRequest)
    {
        aMessageRequest.setAppType("sms");

        if (!aMessageRequest.isInterfaceRejected())
        {
            final boolean lMessageEmpty = isMessageEmpty(aMessageRequest);
            if (lMessageEmpty)
                return;

            stripMaxMessageParts(aMessageRequest);
        }
    }

    private static boolean isMessageEmpty(
            MessageRequest aMessageRequest)
    {
        final boolean lEmpty = CommonUtility.nullCheck(aMessageRequest.getLongMessage()).isEmpty();

        if (lEmpty)
        {
            if (log.isDebugEnabled())
                log.debug(aMessageRequest.getBaseMessageId()+" Empty Message cannot processed. ");
            aMessageRequest.setInterfaceRejected(true);
            aMessageRequest.setSubOriginalStatusCode(InterfaceStatusCode.MESSAGE_EMPTY.getStatusCode());
        }
        
        if (log.isDebugEnabled())
            log.debug(aMessageRequest.getBaseMessageId()+" Message content PASS ");

        return lEmpty;
    }

    private static void stripMaxMessageParts(
            MessageRequest aMessageRequest)
    {

        try
        {
            final String lLongMsg          = CommonUtility.nullCheck(aMessageRequest.getLongMessage());

            int          lMaxMsgPartsAllow = aMessageRequest.getClientMaxSplit();

            if (lMaxMsgPartsAllow == 0)
                lMaxMsgPartsAllow = CommonUtility.getInteger(getAppConfigValueAsString(ConfigParamConstants.MAX_SPLIT_PART_ALLOW), -1);

            if (lMaxMsgPartsAllow == -1)
            {
                log.error(aMessageRequest.getBaseMessageId()+" : Max Split Size was not set in the configuration parameters.");
                return;
            }

            if (lMaxMsgPartsAllow >= 1)
            {
                final int lMessageLength     = lLongMsg.length();

                int       lMaxMsgLengthAllow = getMaxMessageLengthAllow(aMessageRequest.getMessageClass(), aMessageRequest.getClientMaxSplit());

                if (log.isDebugEnabled())
                    log.debug(aMessageRequest.getBaseMessageId()+" Max Message Length allow - > " + lMaxMsgLengthAllow);

                final StringBuilder lTempMessage = new StringBuilder();

                lMaxMsgLengthAllow = (lMaxMsgLengthAllow * lMaxMsgPartsAllow);

                if (log.isDebugEnabled())
                    log.debug(aMessageRequest.getBaseMessageId()+" Base Message id :  Total Message Length : " + lMessageLength + " Max Allow Message Length :" + lMaxMsgPartsAllow);

                if (lMessageLength > lMaxMsgLengthAllow)
                {
                    lTempMessage.append(lLongMsg.substring(0, lMaxMsgLengthAllow));

                    aMessageRequest.setLongMessage(lTempMessage.toString());
                }
            }
        }
        catch (final Exception e)
        {
            log.error(aMessageRequest.getBaseMessageId()+" Exception while checking max split size.", e);
        }
    }

    public static boolean doCommonValidation(
            MessageRequest aMessageRequest)
    {
        replaceNewLineCharacter(aMessageRequest);

        final String lDltTemplateGroupId = CommonUtility.nullCheck(aMessageRequest.getDltTemplateGroupId(), true);

        if (lDltTemplateGroupId.isBlank())
        {
        	if(log.isDebugEnabled()) {
        		
        		log.debug(aMessageRequest.getBaseMessageId()+ " lDltTemplateGroupId is Blank Platform Rejected : with :  "+PlatformStatusCode.INVALID_DLT_TEMPLATE_GROUP_ID.getStatusCode());
        	}
            aMessageRequest.setPlatfromRejected(true);
            aMessageRequest.setSubOriginalStatusCode(PlatformStatusCode.INVALID_DLT_TEMPLATE_GROUP_ID.getStatusCode());
            return false;
        }

    	if(log.isDebugEnabled()) {
    		
    		log.debug(aMessageRequest.getBaseMessageId()+ " lDltTemplateGroupId is NOT Blank, Platform Validation PASS : "+lDltTemplateGroupId);
    	}
        final boolean lMsgFilteredInSpam = isMsgFilteredInSpam(aMessageRequest);

        if (lMsgFilteredInSpam)
        {
        	if(log.isDebugEnabled()) {
        		
        		log.debug(aMessageRequest.getBaseMessageId()+ " lMsgFilteredInSpam Platform Rejected : with :  "+PlatformStatusCode.MESSAGE_SPAM_FILTER_FAILED.getStatusCode());
        	}
     
            aMessageRequest.setPlatfromRejected(true);
            aMessageRequest.setSubOriginalStatusCode(PlatformStatusCode.MESSAGE_SPAM_FILTER_FAILED.getStatusCode());
            return false;
        }

        if(log.isDebugEnabled()) {
    		
    		log.debug(aMessageRequest.getBaseMessageId()+ " lMsgFilteredInSpam Platform Validation PASS : ");
    	}
	
        final boolean lBlacklistMobileNumber = MobileBlockListCheck.validateMobileBlockList(aMessageRequest);
      
        if(log.isDebugEnabled()) {
    		
    		log.debug(aMessageRequest.getBaseMessageId()+ " lBlacklistMobileNumber Platform Validation STATUS : "+lBlacklistMobileNumber);
    	}
        if (lBlacklistMobileNumber)
            return false;

        if(log.isDebugEnabled()) {
    		
    		log.debug(aMessageRequest.getBaseMessageId()+ " aMessageRequest.isIsIntl() : "+aMessageRequest.isIsIntl());
    	}
        if (!aMessageRequest.isIsIntl())
        {
            if (log.isDebugEnabled())
                log.debug(aMessageRequest.getBaseMessageId()+ " DND check process for domastic numbers....");

            if (doCompleteDNDProcess(aMessageRequest))
            {
            	  if (log.isDebugEnabled()) {
                      log.debug(aMessageRequest.getBaseMessageId()+ " DND_REJECT with STATUS "+PlatformStatusCode.DND_REJECT.getStatusCode());
            	  }
                aMessageRequest.setPlatfromRejected(true);
                aMessageRequest.setSubOriginalStatusCode(PlatformStatusCode.DND_REJECT.getStatusCode());
                return false;
            }else {
            	
            	  if (log.isDebugEnabled()) {
                      log.debug(aMessageRequest.getBaseMessageId()+ " DND CHECK PASS");
            	  }
            }
        }else {
        

            if(log.isDebugEnabled()) {
        		
        		log.debug(aMessageRequest.getBaseMessageId()+ " SKIP DND CHECK : ");
        	}
            
        }
        return true;
    }

    private static void replaceNewLineCharacter(
            MessageRequest aMessageRequest)
    {

        try
        {
            final String  lCustomerNewLineChar = CommonUtility.nullCheck(aMessageRequest.getNewLineReplaceChars(), true);
            String        lLongMessage         = CommonUtility.nullCheck(aMessageRequest.getLongMessage());
            final boolean isHexMessage         = aMessageRequest.isHexMessage();

            if (lCustomerNewLineChar.length() > 0)
            {
                if (log.isDebugEnabled())
                    log.debug(aMessageRequest.getBaseMessageId()+" New Line Character is '" + lCustomerNewLineChar + "'");

                if (isHexMessage)
                {
                    final String customerNewLineHex = MessageConvertionUtility.convertString2HexString(lCustomerNewLineChar);
                    final String platformNewLineHex = MessageConvertionUtility.convertString2HexString(Constants.PLATFORM_NEW_LINE_CHAR);
                    lLongMessage = StringUtils.replace(lLongMessage, customerNewLineHex, platformNewLineHex);
                }
                else
                    lLongMessage = StringUtils.replace(lLongMessage, lCustomerNewLineChar, Constants.PLATFORM_NEW_LINE_CHAR);

                aMessageRequest.setLongMessage(lLongMessage);
            }
        }
        catch (final Exception e)
        {
            log.error(aMessageRequest.getBaseMessageId()+" : Exception while checking new line replacement. Message " , e);
        }
    }

    public static boolean isMsgFilteredInSpam(
            MessageRequest aMessageRequest)
    {
        if (log.isDebugEnabled())
            log.debug("Spam Check Base Message Id : " + aMessageRequest.getBaseMessageId());

        final String lSpamFilterChk = CommonUtility.nullCheck(aMessageRequest.getSpamCheckEnabled(), true);

        final String lLongMsg       = aMessageRequest.getLongMessage();
        final String lClientId      = aMessageRequest.getClientId();
        final String lBaseMssageId  = aMessageRequest.getBaseMessageId();

        if (aMessageRequest.isIsIntl())
            return IntlSpamCheckUtility.validateRequest(lClientId, lLongMsg, aMessageRequest.getMessageType(), lSpamFilterChk, lBaseMssageId);

        return SpamCheckUtility.validateRequest(lClientId, lLongMsg, aMessageRequest.getMessageType(), lSpamFilterChk, lBaseMssageId);
    }

    public static void setMccMnc(MessageRequest aMessageRequest) {
    	
        final String  lMobileNo             = aMessageRequest.getMobileNumber();

    	
    	for(int i=8;i>0;i--) {
    		
    		final String searchPrefix=lMobileNo.substring(0, i);
    		MccMncInfo mccmnc=MCCMNCFinder.getMccMnc(searchPrefix);
    		
    		if(mccmnc!=null) {
    			
    			log.debug("lMobileNo : "+lMobileNo+" searchPrefix : "+searchPrefix+" mccmnc : "+mccmnc);
    		
    			aMessageRequest.setMcc(mccmnc.getMcc());
    			aMessageRequest.setMnc(mccmnc.getMnc());
    			
    			return ;
    		}
    		
    	}
    }
    public static boolean isValidDomesticNumber(
            MessageRequest aMessageRequest)
    {

        try
        {
            // Make sure accept only 91+13 digit series as per conf and bypass the msc_codes
            // lookup.
            final boolean lSpecialSeriesPresent = aMessageRequest.isTreatDomesticAsSpecialSeries();

            if (lSpecialSeriesPresent)
                return true;

            final String  lClientId             = aMessageRequest.getClientId();
            final String  lMobileNo             = aMessageRequest.getMobileNumber();

            final boolean isRejectInvalidSeries = CommonUtility.isEnabled(getCutomFeatureValue(lClientId, CustomFeatures.REJECT_INVALID_DOMESTIC_SERIES));

            if (log.isDebugEnabled())
                log.debug("Invalid Series Reject feature is Enabled? - " + isRejectInvalidSeries);

            if (isRejectInvalidSeries)
            {
                final int[] lMobileSeriesLookupValues = getMobileSeriesLookupValues();

                for (final int seriesValue : lMobileSeriesLookupValues)
                {
                    CarrierCircle lCarrierCircle = null;

                    try
                    {
                        lCarrierCircle = IndiaNPFinder.getCarrierCircle(lMobileNo.substring(0, seriesValue));
                    }
                    catch (final Exception e)
                    {
                        log.error("Invalid series Mobile length .., '" + lMobileNo + "', Series Value:'" + seriesValue + "'", e);
                        continue;
                    }

                    if (log.isDebugEnabled())
                        log.debug("Carrier & Circle for the Mobile : '" + lMobileNo + "', Carrier & Circle :" + lCarrierCircle);

                    if (lCarrierCircle != null)
                    {
                        aMessageRequest.setCarrier(lCarrierCircle.getCarrier());
                        aMessageRequest.setCircle(lCarrierCircle.getCircle());
                        return true;
                    }
                }
            }
            else
                return true;
        }
        catch (final Exception e)
        {
            log.error("Exception while check the msc_codes ." + e);
        }
        return false;
    }

    private static int[] getMobileSeriesLookupValues()
    {
        String temp = CommonUtility.nullCheck(getAppConfigValueAsString(ConfigParamConstants.MSC_CODE_SPLIT_CHK), true);

        if (temp.isBlank())
            temp = "7~6";

        if (temp.equals(mMobileSeriesLookupValueString))
            return mMobileSeriesLookupValue;

        final String[] lSplitArr = temp.split("~");
        mMobileSeriesLookupValue = new int[lSplitArr.length];

        int index = 0;
        for (final String s : lSplitArr)
            mMobileSeriesLookupValue[index++] = CommonUtility.getInteger(s);

        mMobileSeriesLookupValueString = temp;
        return mMobileSeriesLookupValue;
    }

    public static boolean isVLFeatureEnabled(
            MessageRequest aMessageRequest)
    {
        boolean isAllowVLShortner = false;

        try
        {

            // VL Not process for SMPP Case.
            if (aMessageRequest.getInterfaceType() == InterfaceType.SMPP)
            {
                aMessageRequest.setVlShortner(0);
                aMessageRequest.setUrlSmartLink(0);
                aMessageRequest.setAdditionalErrorInfo("Bypass VL Process for SMPP");
                return false;
            }

            int           lVLShortner = aMessageRequest.getVlShortner();
            final boolean lIsHexMsg   = aMessageRequest.isHexMessage();

            if (InterfaceType.GUI == aMessageRequest.getInterfaceType())
            {
                lVLShortner = aMessageRequest.getVlShortnerFromUI();

                aMessageRequest.setVlShortner(lVLShortner);
                aMessageRequest.setUrlSmartLink(lVLShortner == 0 ? 0 : aMessageRequest.getUrlSmartlinkEnable());
            }
            else
            {
                final boolean isVLShortnerCustomCheck = isVLShortnerEnable(aMessageRequest);
                if (log.isDebugEnabled())
                    log.debug("VL Shortner Customer Check :" + isVLShortnerCustomCheck);

                if (!isVLShortnerCustomCheck)
                    return false;

                lVLShortner = aMessageRequest.getVlShortner();
            }

            boolean       isMsgContainsUrl    = false;
            final boolean isVlTrackingEnabled = isMessageContainsVLTracking(aMessageRequest);

            if (lIsHexMsg)
            {
                String lMessage = aMessageRequest.getLongMessage();
                lMessage = MessageConvertionUtility.convertHex2String(lMessage);
                if (log.isDebugEnabled())
                    log.debug("Unicode Message Unicode String :" + lMessage);
                isMsgContainsUrl = containsURL(lMessage);
            }
            else
                isMsgContainsUrl = containsURL(aMessageRequest.getLongMessage());

            if (((lVLShortner > 0) && isMsgContainsUrl) || isVlTrackingEnabled)
                isAllowVLShortner = true;
            else
            {
                aMessageRequest.setVlShortner(0);
                aMessageRequest.setUrlSmartLink(0);
            }
        }
        catch (final Exception e)
        {
            // Ignore
        }
        return isAllowVLShortner;
    }

    private static boolean isMessageContainsVLTracking(
            MessageRequest aMessageRequest)
    {
        if (log.isDebugEnabled())
            log.debug("Vl Tracking checker........");

        boolean isVLTrackingEnabled = false;

        try
        {
            String        lMessage     = CommonUtility.nullCheck(aMessageRequest.getLongMessage());
            final boolean isHexMessage = aMessageRequest.isHexMessage();

            if (log.isDebugEnabled())
                log.debug("Calling VL Tracking check. Message : " + lMessage);

            if ((lMessage.contains(VL_UPPER) || lMessage.contains(VL_LOWER)) && lMessage.contains(TILDA))
                isVLTrackingEnabled = true;

            if (isHexMessage)
            {
                lMessage = MessageConvertionUtility.convertHex2String(lMessage);
                if ((lMessage.contains(VL_UPPER) || lMessage.contains(VL_LOWER)) && lMessage.contains(TILDA))
                    isVLTrackingEnabled = true;
            }

            // Force to disable the UrlSmartlinkCheck flag when message not contains VL
            // Smartlink id.
            if (!isVLTrackingEnabled)
                aMessageRequest.setUrlSmartLink(0);
        }
        catch (final Exception e)
        {
            log.error("Exception while checking VL Tracking " + aMessageRequest, e);
        }
        return isVLTrackingEnabled;
    }

    private static int getMaxMessageLengthAllow(
            String aMsgClass,
            int aClinetMaxSplitAllow)
    {
        final MessageClass lMsgClass     = MessageClass.getMessageClass(aMsgClass);
        int                lMaxMsgLength = 0;

        switch (lMsgClass)
        {
            case PLAIN_MESSAGE:
            case FLASH_PLAIN_MESSAGE:
                lMaxMsgLength = (aClinetMaxSplitAllow == 1 ? MessageClassLength.MAX_LENGTH_PLAIN_MESSAGE : MessageClassLength.SPLIT_LENGTH_PLAIN_MESSAGE_8_BIT);
                break;

            case UNICODE_MESSAGE:
            case FLASH_UNICODE_MESSAGE:
                lMaxMsgLength = (aClinetMaxSplitAllow == 1 ? MessageClassLength.MAX_LENGTH_UNICODE_MESSAGE : MessageClassLength.SPLIT_LENGTH_UNICODE_MESSAGE);
                break;

            case SP_PLAIN_MESSAGE:
                lMaxMsgLength = (aClinetMaxSplitAllow == 1 ? MessageClassLength.MAX_LENGTH_SP_PLAIN_MESSAGE : MessageClassLength.SPLIT_LENGTH_SP_PLAIN_MESSAGE);
                break;

            case SP_UNICODE_MESSAGE:
                lMaxMsgLength = (aClinetMaxSplitAllow == 1 ? MessageClassLength.MAX_LENGTH_SP_UNICODE_MESSAGE : MessageClassLength.SPLIT_LENGTH_SP_UNICODE_MESSAGE);
                break;

            case BINARY_MESSAGE:
                lMaxMsgLength = MessageClassLength.SPLIT_LENGTH_BINARY_MESSAGE;
                break;

            default:
                break;
        }
        return lMaxMsgLength;
    }

    public static String getCutomFeatureValue(
            String aClientId,
            CustomFeatures aCustomFeature)
    {
        final InmemCustomFeatures lCustomFeatures = (InmemCustomFeatures) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.CUSTOM_FEATURES);
        return lCustomFeatures.getValueOfCustomFeature(aClientId, aCustomFeature.getKey());
    }

    public static String getAppConfigValueAsString(
            ConfigParamConstants aConfigParamConstant)
    {
        final ApplicationConfiguration lAppConfiguration = (ApplicationConfiguration) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.APPLICATION_CONFIG);
        return lAppConfiguration.getConfigValue(aConfigParamConstant.getKey());
    }

    public static boolean isValidUserHeader(
            String aTemplateGroupId,
            String aHeader)
    {
        final DomesticUserHeaderInfo lDomesticUserHeaderInfo = (DomesticUserHeaderInfo) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.DOMESTIC_USER_HEADERS);
        return lDomesticUserHeaderInfo.isHeaderMatches(aTemplateGroupId, aHeader.toLowerCase());
    }

    private static boolean doCompleteDNDProcess(
            MessageRequest aMessageRequest)
    {
        return checkDND(aMessageRequest);
    }

    private static boolean checkDND(
            MessageRequest aMessageRequest)
    {
        final boolean lDNDCheck     = CommonUtility.isEnabled(CommonUtility.nullCheck(aMessageRequest.getDndCheckEnabled(), true));
        final boolean isForceDndChk = aMessageRequest.isForceDndCheck();

        if (log.isDebugEnabled()) {
            log.debug(aMessageRequest.getBaseMessageId()+" : lDNDCheck Check enable ? " + lDNDCheck);
            log.debug(aMessageRequest.getBaseMessageId()+" : Force isForceDndChk DND Check enable ? " + isForceDndChk);
            log.debug(aMessageRequest.getBaseMessageId()+" : aMessageRequest.getMessageType() " + aMessageRequest.getMessageType());

        }
        if (((aMessageRequest.getMessageType() == MessageType.PROMOTIONAL) && lDNDCheck) || isForceDndChk)
            if (isDND(aMessageRequest))
            {
                aMessageRequest.setIsDndScrubbed(true);
                return true;
            }
            else
                aMessageRequest.setIsDndScrubbed(false);

        return false;
    }

    private static boolean isDND(
            MessageRequest aMessageRequest)
    {
        String       lAllowDnd;

        final String lMobileNumber = aMessageRequest.getMobileNumber();

        try
        {
            lAllowDnd = DNDCheck.getDNDInfo(lMobileNumber);
        }
        catch (final Exception e)
        {
            return true;
        }

        if (lAllowDnd == null)
            return false;

        final int lDNDPref = aMessageRequest.getDndPref();

        if ((lDNDPref <= 0) || !lAllowDnd.contains(MessageUtil.getStringFromInt(lDNDPref)))
            return true;

        aMessageRequest.setDndPreferences(MessageUtil.getStringFromInt(lDNDPref)); // NCPR instaded of DND_ENABLE
        return false;
    }

    public static void setBypassDLTTemplateCheck(
            MessageRequest aMessageRequest)
    {
        final String  lDltEntityId   = CommonUtility.nullCheck(aMessageRequest.getDltEntityId(), true);
        final String  lDltTemplateId = CommonUtility.nullCheck(aMessageRequest.getDltTemplateId(), true);
        final boolean isDltCheckReq  = aMessageRequest.isDltCheckEnabled();

        if ((!lDltEntityId.isBlank() && !lDltTemplateId.isBlank()) || !isDltCheckReq)
        {
            if (log.isDebugEnabled())
                log.debug("Setting bypass Dlt Template Checking flag...");

            aMessageRequest.setBypassDltCheck(true);
        }

        if (lDltEntityId.isBlank() && !lDltTemplateId.isBlank())
        {
            aMessageRequest.setDltTemplateId("");
            aMessageRequest.setAdditionalErrorInfo("Ignore Customer TemplateId :" + lDltTemplateId);
        }
    }

    public static boolean containsURL(
            String aContent)
    {
        boolean       isUrlContain = false;
        final String  REGEX        = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
        final Pattern p            = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);
        final Matcher m            = p.matcher(aContent);
        if (m.find())
            isUrlContain = true;

        if (!isUrlContain)
            return (aContent.contains("www.") || aContent.contains("WWW."));

        return isUrlContain;
    }

    private static boolean isVLShortnerEnable(
            MessageRequest aMessageRequest)
    {
        final boolean isExplicitUrlShortnerEnable = CommonUtility.isEnabled(ICUtility.getCutomFeatureValue(aMessageRequest.getClientId(), CustomFeatures.EXPLICIT_URL_SHORTNER_YN));

        if (isExplicitUrlShortnerEnable)
        {

            if (aMessageRequest.getVlShortner() == 0)
            {
                // Disable the SmartLink & Tracking for VL Shortner disabled case.
                aMessageRequest.setUrlSmartLink(0);
                aMessageRequest.setUrlTrackEnabled(false);
                aMessageRequest.setAdditionalErrorInfo("Url Shortner is disabled by account level");
                return false;
            }

            final boolean isUrlShortnerReqFromClient = aMessageRequest.getUrlShortnerReq();

            if (!isUrlShortnerReqFromClient)
            {
                aMessageRequest.setVlShortner(0);
                aMessageRequest.setUrlSmartLink(0);
                aMessageRequest.setUrlTrackEnabled(false);
                aMessageRequest.setAdditionalErrorInfo("Customer UrlShortner Request Disabled.");
                return false;
            }
        }

        return true;
    }

    public static boolean updatePriceInfo(
            MessageRequest aMessageRequest)
    {
        final String          lClientId              = aMessageRequest.getClientId();
        final String          lPlatformBaseCurrency  = ICUtility.getAppConfigValueAsString(ConfigParamConstants.BASE_CURRENCY);

        final String          lBillingCurrency       = aMessageRequest.getBillingCurrency();
        final boolean         lConvertDatewise       = aMessageRequest.getBillingCurrencyConversionType() == 2 ? true : false;
        final String  lPlatformIntlBaseCurrency = PlatformUtil.getAppConfigValueAsString(ConfigParamConstants.INTL_BASE_CURRENCY);
     //   final String  lPlatformIntlBaseCurrency = lBillingCurrency;

        CalculateBillingPrice lCalculateBillingPrice = null;

        log.debug(aMessageRequest.getBaseMessageId()+" : lPlatformBaseCurrency : "+lPlatformBaseCurrency);
		log.debug(aMessageRequest.getBaseMessageId()+" : lBillingCurrency : "+lBillingCurrency);
		log.debug(aMessageRequest.getBaseMessageId()+" : lConvertDatewise : "+lConvertDatewise);

		if (!aMessageRequest.isIsIntl())
		{
		    final double                lBaseSmsRate      = aMessageRequest.getBaseSmsRate();
		    final double                lBaseAddFixedRate = aMessageRequest.getBaseAddFixedRate();

		    final CalculateBillingPrice lCBP              = new CalculateBillingPrice(lClientId, lBaseSmsRate, lBaseAddFixedRate, lBillingCurrency,lBillingCurrency,lBillingCurrency,
		    		
		            lConvertDatewise);
		    CurrencyUtil.getBillingPrice(lCBP);
		    lCalculateBillingPrice = lCBP;

		    aMessageRequest.setBaseCurrency(lBillingCurrency);
		    aMessageRequest.setBillingCurrency(lBillingCurrency);
		    aMessageRequest.setRefCurrency(lBillingCurrency);
		 	log.debug(aMessageRequest.getBaseMessageId()+" : BaseCurrency : "+aMessageRequest.getBillingCurrency());
			log.debug(aMessageRequest.getBaseMessageId()+" : BillingCurrency : "+aMessageRequest.getBillingCurrency());
			log.debug(aMessageRequest.getBaseMessageId()+" : RefCurrency : "+aMessageRequest.getBillingCurrency()
			);

		    aMessageRequest.setBillingSmsRate(lCalculateBillingPrice.getBillingSmsRate());
		    aMessageRequest.setBillingAddFixedRate(lCalculateBillingPrice.getBillingAdditionalFixedRate());

		    aMessageRequest.setBaseSmsRate(lCalculateBillingPrice.getBaseSmsRate());
		    aMessageRequest.setBaseAddFixedRate(lCalculateBillingPrice.getBaseAdditionalFixedRate());
		    aMessageRequest.setRefSmsRate(lCalculateBillingPrice.getRefSmsRate());
		    aMessageRequest.setRefAddFixedRate(lCalculateBillingPrice.getRefAdditionalFixedRate());
		    aMessageRequest.setBillingExchangeRate(lCalculateBillingPrice.getBillingConversionRate());
		    aMessageRequest.setRefExchangeRate(lCalculateBillingPrice.getRefConversionRate());
		}
        

        return true;
    }

}