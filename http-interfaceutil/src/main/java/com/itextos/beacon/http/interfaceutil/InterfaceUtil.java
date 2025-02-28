package com.itextos.beacon.http.interfaceutil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.ErrorMessage;
import com.itextos.beacon.commonlib.constants.InterfaceStatusCode;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.messageprocessor.process.MessageProcessor;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.mobilevalidation.AccountMobileInfo;
import com.itextos.beacon.commonlib.utility.mobilevalidation.MobileNumberValidator;
import com.itextos.beacon.http.interfacefallback.FallBackProcess;
import com.itextos.beacon.http.interfacefallback.inmem.FallbackQ;
import com.itextos.beacon.inmemory.configvalues.ApplicationConfiguration;
import com.itextos.beacon.inmemory.inmemdata.country.CountryInfo;
import com.itextos.beacon.inmemory.inmemdata.country.CountryInfoCollection;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.platform.ic.process.ICProcessor;

public class InterfaceUtil
{

    private static final Log    log                = LogFactory.getLog(InterfaceUtil.class);

    private static final String INTL_SERVICE_ALLOW = "sms~international";
    
    private static final String handovergw=System.getenv("handovergw");

    private InterfaceUtil()
    {}

    public static MobileNumberValidator validateMobile(
            String aMobileNumber,
            String aCountryCode,
            boolean aIsIntlServiceEnable,
            boolean aConsiderDefaultLengthAsDomestic,
            boolean isAppendCountryCode,
            String aAppendCountryCode,
            boolean isDomesticSpecialSeriesAllow)
    {
        if (log.isDebugEnabled())
            log.debug("Default Contury From Account Table : " + aCountryCode);

        final CountryInfo       lCountryInfo         = getCountryInfo(aCountryCode);
        final String 			lCountryCurrency	 = lCountryInfo.getCountryCurrency();
        
        final int               lCountrryCode        = lCountryInfo.getDialInCode();
        final int               lDefaultMobileLength = lCountryInfo.getDefaultMobileLength();
        final int               lMinMobileLength     = lCountryInfo.getMinMobileLength();
        final int               lMaxMobileLength     = lCountryInfo.getMaxMobileLength();
        final int[]             lOtherMobileLength   = lCountryInfo.getOtherMobileLength();

        final AccountMobileInfo lAccountMobileInfo   = new AccountMobileInfo("" + lCountrryCode, lDefaultMobileLength, isDomesticSpecialSeriesAllow, lOtherMobileLength, lMinMobileLength,
                lMaxMobileLength, aIsIntlServiceEnable, aConsiderDefaultLengthAsDomestic, getConfigParamsValueAsInt(ConfigParamConstants.MOBILE_DEFAULT_MIN_LENGTH),
                getConfigParamsValueAsInt(ConfigParamConstants.MOBILE_DEFAULT_MAX_LENGTH),lCountryCurrency);
        return new MobileNumberValidator(aMobileNumber, lAccountMobileInfo, isAppendCountryCode, aAppendCountryCode);
    }

    public static String getCountry()
    {
        final String      lCountryCode = getDefaultCountryCode();

        final CountryInfo lCountryInfo = getCountryInfo(lCountryCode);
        if (lCountryInfo != null)
            return getCountryInfo(lCountryCode).getCountry();

        return null;
    }

    private static CountryInfo getCountryInfo(
            String aDefaultCountry)
    {
        final CountryInfoCollection lCountryInfoCollection = (CountryInfoCollection) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.COUNTRY_INFO);
        return lCountryInfoCollection.getCountryData(aDefaultCountry);
    }

    public static void sendToKafka(
            MessageRequest aMessageRequest,StringBuffer sb)
    {
    	
    	if(handovergw!=null&&handovergw.equals("unitia")) {

    		ICProcessor.forIC(aMessageRequest);
    	}else {
    		sendKafkaOriginal(aMessageRequest,sb);
    	}
    }

    private static void sendKafkaOriginal(MessageRequest aMessageRequest, StringBuffer sb) {
		
    	   if (log.isDebugEnabled())
               log.debug(" The MessageRequest object Handover to Kafka........" + aMessageRequest + " Message Request from : '" + aMessageRequest.getInterfaceGroupType() + "'");
           sb.append(" The MessageRequest object Handover to Kafka........" + " Message Request from : '" + aMessageRequest.getInterfaceGroupType() + "'" ).append("\n");
           final boolean isKafkaAvailable = CommonUtility.isEnabled(getConfigParamsValueAsString(ConfigParamConstants.IS_KAFKA_AVAILABLE));

           sb.append(" isKafkaAvailable : "+isKafkaAvailable).append("\n");
           if (!isKafkaAvailable)
           {
               if (log.isDebugEnabled())
                   log.debug("Unable to push kafka, Hence sending to Mysql ..");

               sb.append("Unable to push kafka, Hence sending to Mysql ..").append("\n");
               sendToFallback(aMessageRequest);
           }
           else
           {
               boolean status = false;

               try
               {
                   MessageProcessor.writeMessage(Component.INTERFACES, Component.IC, FallbackQ.getInstance().getBlockingQueue(), aMessageRequest);
                   status = true;
               }
               catch (final Exception e)
               {
                   log.error("Message sending to kafka is failed, Hence sending to Fallback table..", e);
                   sb.append("Message sending to kafka is failed, Hence sending to Fallback table.. "+ ErrorMessage.getStackTraceAsString(e)).append("\n");

                   sendToFallback(aMessageRequest);
               }

               if (log.isDebugEnabled())
                   log.debug("Kafka handover status -" + status);
               
               sb.append("Kafka handover status -" + status).append("\n");
           }

		
	}

	private static void sendToFallback(
            MessageRequest aMessageRequest)
    {

        try
        {
            FallBackProcess.sendToFallBack(aMessageRequest);
        }
        catch (final Exception e1)
        {
            log.error("Message storing in DB failed..", e1);
        }
    }

    public static String getDefaultCountryCode()
    {
        String lCountryCode = CommonUtility.nullCheck(getConfigParamsValueAsString(ConfigParamConstants.DEFAULT_COUNTRY_CODE), true);
        if (lCountryCode.isBlank())
            lCountryCode = "IND";

        return lCountryCode;
    }

    public static boolean isIntlServiceAllow(
            JSONObject aJsonUserDetails)
    {
        return CommonUtility.isEnabled((String) aJsonUserDetails.get(INTL_SERVICE_ALLOW));
    }

    public static String getConfigParamsValueAsString(
            ConfigParamConstants aKey)
    {
        final ApplicationConfiguration lAppConfigValues = (ApplicationConfiguration) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.APPLICATION_CONFIG);
        return lAppConfigValues.getConfigValue(aKey.getKey());
    }

    public static int getConfigParamsValueAsInt(
            ConfigParamConstants aKey)
    {
        final ApplicationConfiguration lAppConfigValues = (ApplicationConfiguration) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.APPLICATION_CONFIG);
        return CommonUtility.getInteger(lAppConfigValues.getConfigValue(aKey.getKey()));
    }

    public static void setMessageStatus(
            MessageRequest aMessageRequest,
            InterfaceStatusCode aInterfaceStatusCode,
            String aMobileNumber)
    {

        if (aInterfaceStatusCode != InterfaceStatusCode.SUCCESS)
        {
            aMessageRequest.setInterfaceRejected(true);
            aMessageRequest.setSubOriginalStatusCode(aInterfaceStatusCode.getStatusCode());
            aMessageRequest.setFailReason(aInterfaceStatusCode.getStatusDesc());
            if (aInterfaceStatusCode == InterfaceStatusCode.DESTINATION_INVALID)
                aMessageRequest.setAdditionalErrorInfo("Invalid mobile " + aMobileNumber);
        }
    }

}
