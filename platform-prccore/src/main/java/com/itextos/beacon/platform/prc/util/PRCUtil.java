package com.itextos.beacon.platform.prc.util;

import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.Constants;
import com.itextos.beacon.commonlib.constants.ErrorMessage;
import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.commonlib.constants.PlatformStatusCode;
import com.itextos.beacon.commonlib.constants.RouteConstants;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.message.SubmissionObject;
import com.itextos.beacon.commonlib.prometheusmetricsutil.PrometheusMetrics;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.Name;
import com.itextos.beacon.inmemory.clidlrpref.ClientDlrConfig;
import com.itextos.beacon.inmemory.clidlrpref.ClientDlrConfigUtil;
import com.itextos.beacon.inmemory.configvalues.ApplicationConfiguration;
import com.itextos.beacon.inmemory.errorinfo.ErrorCodeUtil;
import com.itextos.beacon.inmemory.errorinfo.data.ErrorCategory;
import com.itextos.beacon.inmemory.errorinfo.data.PlatformErrorInfo;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.inmemory.whitelistnumbers.MobileWhitelistNumbers;
import com.itextos.beacon.platform.msgflowutil.util.PlatformUtil;

public class PRCUtil
{

  //  private static final Log log = LogFactory.getLog(PRCUtil.class);

    private PRCUtil()
    {}

    public static boolean processDNDToCarrier(
            MessageRequest aMessageRequest)
    {
        final boolean ldoDNDReject = aMessageRequest.isDndRejectEnable();
        // few interfaces may not pass dnd_reject_yn parameter like php....assuming
        // dnd_reject_yn is 0 (attempting to operator) for this case
        if (ldoDNDReject && PlatformStatusCode.DND_REJECT.getStatusCode().equals(aMessageRequest.getSubOriginalStatusCode()))
            return true;
        return false;
    }

    public static void processReq(
            MessageRequest aMessageRequest,
            boolean aIsProcessDNCarrier,
            boolean isMultiple)
            throws Exception
    {
        final List<BaseMessage> lSubmissions = aMessageRequest.getSubmissions();

        if (isMultiple)
        {
           
            for (final BaseMessage msg : lSubmissions)
            {
                final SubmissionObject sb = (SubmissionObject) msg;
                processReq(sb, aIsProcessDNCarrier);
            }
        }
        else
        {
            final SubmissionObject lSubmissionObject = (SubmissionObject) lSubmissions.get(0);

            updateFieldsforMultipart(aMessageRequest, lSubmissionObject);

            processReq(lSubmissionObject, aIsProcessDNCarrier);
        }
    }

    private static void updateFieldsforMultipart(
            MessageRequest aMessageRequest,
            SubmissionObject aSubmissionObject)
    {

        if ((aMessageRequest.getMessageTotalParts() > 1) && aMessageRequest.isPlatfromRejected())
        {
            aSubmissionObject.setMessage(aMessageRequest.getLongMessage());
            aSubmissionObject.setUdh("");
            aSubmissionObject.setMessagePartNumber(0);
        }
    }

    public static void processReq(
            SubmissionObject aSubmissionObject,
            boolean aIsProcessDNCarrier)
            throws Exception
    {

        try
        {
            setStatusDesc(aSubmissionObject);

            PrometheusMetrics.incrementPlatformRejection(aSubmissionObject.getClusterType(), Component.PRC, CommonUtility.getApplicationServerIp(), aSubmissionObject.getSubOriginalStatusCode(),
                    aSubmissionObject.getDnOriStatusDesc());

            final int    lRetryAttempt = aSubmissionObject.getRetryAttempt();
            final String lMessageId    = aSubmissionObject.getMessageId();

            if (lRetryAttempt == 0)
            {
            	
                aSubmissionObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append((aSubmissionObject).getBaseMessageId()+" :: Attempting to sending biller queue.." );

      
                if (aIsProcessDNCarrier)
                {
                    setSTSAndActualts(aSubmissionObject);
                    String       lSubmisstionDNDStatus = CommonUtility.nullCheck(getAppConfigValueAsString(ConfigParamConstants.SUB_DND_STATUS), true);
                    final String lSubmissionDNDRouteId = getAppConfigValueAsString(ConfigParamConstants.SUB_DND_ROUTE_ID);

                    if (lSubmisstionDNDStatus.isEmpty())
                        lSubmisstionDNDStatus = PlatformStatusCode.SUBMISSION_DND_STATUS_ID.getStatusCode();

                    aSubmissionObject.setSubOriginalStatusCode(PlatformStatusCode.getStatusDesc(lSubmisstionDNDStatus).getStatusCode());

                    aSubmissionObject.setRouteId(lSubmissionDNDRouteId == null ? RouteConstants.DUMMY : lSubmissionDNDRouteId);
                    // aNunMessage.putValue(MiddlewareConstant.MW_PLATFORM_REJECT_ROUTE_ID_EXISTS,
                    // "y");
                }

        
                aSubmissionObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append((aSubmissionObject).getBaseMessageId()+" :: Request sending Biller Topic : ");

                PRProducer.sendToBillerTopic(aSubmissionObject,aSubmissionObject.getLogBuffer());

         
                aSubmissionObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append((aSubmissionObject).getBaseMessageId()+" :: sending to biller topic succesful..for mid=");

                final boolean         lNoPayloadForPromoMsg = CommonUtility.isEnabled(getAppConfigValueAsString(ConfigParamConstants.NOPAYLOAD_FOR_PROMO_MSG));

                final ClientDlrConfig lClientDlrConfig      = ClientDlrConfigUtil.getDlrHandoverConfig(aSubmissionObject.getClientId(), "sms", aSubmissionObject.getInterfaceType(),
                        aSubmissionObject.isDlrRequestFromClient());

                boolean               isDlrOnPlatformFail   = false;
                if (lClientDlrConfig != null)
                    isDlrOnPlatformFail = lClientDlrConfig.isDlrOnPlatformFail();

        
                aSubmissionObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append((aSubmissionObject).getBaseMessageId()+" :: Client : '" + aSubmissionObject.getClientId() + "', Dlr Handover on Platform fail : " + isDlrOnPlatformFail);

                if (aIsProcessDNCarrier && (lNoPayloadForPromoMsg || isDlrOnPlatformFail) && (aSubmissionObject.getMessageType() == MessageType.PROMOTIONAL))
                {
                	
                    aSubmissionObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append((aSubmissionObject).getBaseMessageId()+" :: No need to send dlr for promotional account " );

                    return;
                }
            }
        }
        catch (final Exception exp)
        {
        	
            aSubmissionObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append((aSubmissionObject).getBaseMessageId()+" :: problem sending to billerq due to..."+ErrorMessage.getStackTraceAsString(exp) );

            throw exp;
        }

        try
        {
            final DeliveryObject lDeliveryObject = aSubmissionObject.getDeliveryObject();

            if (lDeliveryObject.getRetryMsgReject() != null)
            {
                lDeliveryObject.setDlrFromInternal("failed at MT while retry");
                lDeliveryObject.setRetryMsgReject(Constants.ENABLED);
            }
            else
                lDeliveryObject.setRouteId(RouteConstants.DUMMY);

            lDeliveryObject.setPlatformRejected(true);

            if (aIsProcessDNCarrier)
            {
                // this is required for slab and other adjustments
                if (lDeliveryObject.getCircle() == null)
                    lDeliveryObject.setCircle("OTHERS");

                String lDeliveryDNDStatus = CommonUtility.nullCheck(getAppConfigValueAsString(ConfigParamConstants.DELV_DND_STATUS), true);
                if (lDeliveryDNDStatus.isEmpty())
                    lDeliveryDNDStatus = PlatformStatusCode.DELIVERIES_DND_STATUS_ID.getStatusCode();

                final String lDeliveryDNDRouteId = getAppConfigValueAsString(ConfigParamConstants.DELV_DND_ROUTE_ID);

                lDeliveryObject.setSubOriStatusCode(PlatformStatusCode.getStatusDesc(lDeliveryDNDStatus).getStatusCode());
                lDeliveryObject.setRouteId(lDeliveryDNDRouteId == null ? RouteConstants.DUMMY : lDeliveryDNDRouteId);
            }

            final String lCluster = lDeliveryObject.getClusterType().getKey();

     
            aSubmissionObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append((aSubmissionObject).getBaseMessageId()+" :: sending delivery Object message to Kafka cluster=" + lCluster );

            // Setting submittion error code to delivery error code when platform
            // rejections.
            lDeliveryObject.setDnOrigianlstatusCode(lDeliveryObject.getSubOriStatusCode());

            setStatusDesc(lDeliveryObject);

            PRProducer.sendToDLRTopic(lDeliveryObject,aSubmissionObject.getLogBuffer());
        }
        catch (final Exception exp)
        {
            aSubmissionObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append((aSubmissionObject).getBaseMessageId()+" :: problem sending to Dlr internal topic due to... "+ErrorMessage.getStackTraceAsString(exp));

            throw exp;
        }
    }

    private static void setSTSAndActualts(
            SubmissionObject aSubmissionObject)
    {
        final long lSysdate = System.currentTimeMillis();
        long       lSts     = lSysdate;

        try
        {
            final Date lScheduleTime = aSubmissionObject.getScheduleDateTime();
            Date       lSTime        = aSubmissionObject.getMessageReceivedTime();

            if ((lScheduleTime != null))
                lSTime = lScheduleTime;

            final long   lSTimelong = lSTime.getTime();
            final Random lRandomGen = new Random();

            if (lSts < lSTimelong)
            {
                
                aSubmissionObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append((aSubmissionObject).getBaseMessageId()+" :: Looks Platform time is less than the time received from client (stime) so adjusting sts with random number");

                final int maxRandomSeed = CommonUtility.getInteger(PlatformUtil.getPropertyConfigValue(ConfigParamConstants.GLOBAL_DN_ADJUSTMENT_IN_SEC.getKey()), 10);
                lSts = lSTimelong + (lRandomGen.nextInt(maxRandomSeed) * 1000);
                aSubmissionObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append((aSubmissionObject).getBaseMessageId()+" Adjusted sts:" + lSts + " for stime:" + lSTimelong);

            }
            final String lDNAdjuestStr = CommonUtility.nullCheck(aSubmissionObject.getDnAdjustEnabled(), true);

            if (!lDNAdjuestStr.isEmpty() && !lDNAdjuestStr.equals("0"))
            {
                final long    lDnAdjustMills = CommonUtility.getLong(lDNAdjuestStr) * 1000;
                final boolean isWhiteListed  = checkNumberWhiteListed(CommonUtility.nullCheck(aSubmissionObject.getMobileNumber(), true));

                aSubmissionObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append((aSubmissionObject).getBaseMessageId()+" :: isWhiteListed:" + isWhiteListed);

                if (((lSts - lSTimelong) > lDnAdjustMills) && !isWhiteListed)
                    lSts = lSTimelong + (lRandomGen.nextInt((CommonUtility.getInteger(lDNAdjuestStr) + 1)) * 1000);
            }

            aSubmissionObject.setCarrierSubmitTime(new Date(lSts));
            aSubmissionObject.setActualCarrierSubmitTime(new Date(lSysdate));
        }
        catch (final Exception e)
        {
            aSubmissionObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append((aSubmissionObject).getBaseMessageId()+" :: error setSTSAndActualts ::: "+ErrorMessage.getStackTraceAsString(e));

            aSubmissionObject.setCarrierSubmitTime(new Date(lSysdate));
            aSubmissionObject.setActualCarrierSubmitTime(new Date(lSysdate));
        }
    }

    private static void setStatusDesc(
            SubmissionObject aSubmissionObject)
    {
        final String lSubOrgErrorCode = CommonUtility.nullCheck(aSubmissionObject.getSubOriginalStatusCode(), true);

        
        aSubmissionObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append((aSubmissionObject).getBaseMessageId()+" :: Sub Original Error Code :" + lSubOrgErrorCode);

       

        ErrorCategory lCategory = ErrorCategory.PLATFORM;
        if (aSubmissionObject.isInterfaceRejected())
            lCategory = ErrorCategory.INTERFACE;

        final PlatformErrorInfo lPlatformErrorInfo = (PlatformErrorInfo) ErrorCodeUtil.getPlatformErrorCode(lCategory, lSubOrgErrorCode);
        aSubmissionObject.setDnOriStatusDesc(lPlatformErrorInfo.getDisplayError());
        aSubmissionObject.setDeliveryStatus(lPlatformErrorInfo.getStatusFlag().getKey());
    }

    private static void setStatusDesc(
            DeliveryObject aDeliveryObject)
    {
        final String lSubOrgErrorCode = CommonUtility.nullCheck(aDeliveryObject.getSubOriStatusCode(), true);

    
        aDeliveryObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append((aDeliveryObject).getBaseMessageId()+" :: Sub Original Error Code :" + lSubOrgErrorCode);

        ErrorCategory lCategory = ErrorCategory.PLATFORM;
        if (aDeliveryObject.isInterfaceRejected())
            lCategory = ErrorCategory.INTERFACE;

        final PlatformErrorInfo lPlatformErrorInfo = (PlatformErrorInfo) ErrorCodeUtil.getPlatformErrorCode(lCategory, lSubOrgErrorCode);
        aDeliveryObject.setDnOriStatusDesc(lPlatformErrorInfo.getDisplayError());
        aDeliveryObject.setDeliveryStatus(lPlatformErrorInfo.getStatusFlag().getKey());
    }

    public static String getAppConfigValueAsString(
            ConfigParamConstants aConfigParamConstant)
    {
        final ApplicationConfiguration lAppConfiguration = (ApplicationConfiguration) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.APPLICATION_CONFIG);
        return lAppConfiguration.getConfigValue(aConfigParamConstant.getKey());
    }

    public static boolean checkNumberWhiteListed(
            String aMobileNumber)
    {
        final MobileWhitelistNumbers lWhiteListNumber = (MobileWhitelistNumbers) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.MOBILE_WHITELIST);
        return lWhiteListNumber.isNumberWhitelisted(aMobileNumber);
    }

}
