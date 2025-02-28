package com.itextos.beacon.smppsimulator.interfaces.validation;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cloudhopper.commons.util.HexUtil;
import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.pdu.BaseBind;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.pdu.SubmitSmResp;
import com.cloudhopper.smpp.type.SmppProcessingException;
import com.itextos.beacon.commonlib.constants.AccountStatus;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.DCS;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.InterfaceStatusCode;
import com.itextos.beacon.commonlib.constants.MessageClass;
import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.commonlib.constants.PlatformStatusCode;
import com.itextos.beacon.commonlib.constants.RouteType;
import com.itextos.beacon.commonlib.constants.UdhHeaderInfo;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.messageidentifier.MessageIdentifier;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.commonlib.utility.mobilevalidation.MobileNumberValidator;
import com.itextos.beacon.http.interfaceutil.InterfaceUtil;
import com.itextos.beacon.smppsimulator.interfaces.logs.LogWriter;
import com.itextos.beacon.smppsimulator.interfaces.queue.DNQueue;
import com.itextos.beacon.smppsimulator.interfaces.util.BuildMessageRequest;
import com.itextos.beacon.smppsimulator.interfaces.util.Communicator;
import com.itextos.beacon.smppsimulator.interfaces.util.TraiBlockoutCheck;
import com.itextos.beacon.smppsimulator.objects.SessionDetail;
import com.itextos.beacon.smppsimulator.objects.request.SmppMessageRequest;
import com.itextos.beacon.smppsimulator.objects.request.SubmitSmRequest;
import com.itextos.beacon.smppsimulator.redisoperations.RedisBindOperation;
import com.itextos.beacon.smppsimulator.redisoperations.Throttler;
import com.itextos.beacon.smppsimulator.utils.AccountDetails;
import com.itextos.beacon.smppsimulator.utils.ItextosSmppConstants;
import com.itextos.beacon.smppsimulator.utils.ItextosSmppUtil;
import com.itextos.beacon.smppsimulator.utils.SmppErrorCodes;
import com.itextos.beacon.smppsimulator.utils.properties.SmppProperties;

public class ValidateRequest
{

    private static final Log    log                    = LogFactory.getLog(ValidateRequest.class);

    private static final String ACCOUNT_SMPP_BIND_TYPE = "smpp_bind_type";

    private ValidateRequest()
    {}

    public static void validateRequestOnBind(
            BaseBind aBindRequest,
            SessionDetail aSessionDetail)
            throws SmppProcessingException
    {
        if (log.isDebugEnabled())
            log.debug("Validate Bind Request :: Instance Id '" + aSessionDetail.getInstanceId() + "' System id '" + aSessionDetail.getSystemId() + "' commandid '" + aSessionDetail.getCommandId()
                    + "' Bindname '" + aSessionDetail.getBindName() + "', Host : '" + aSessionDetail.getHost() + "'");

        try
        {
            final String password = aBindRequest.getPassword();

            checkForEmptySystemId(aSessionDetail);

            checkForEmptyPassword(aSessionDetail, password);

            checkForValidUser(aSessionDetail);

            checkForPassword(aSessionDetail, password);

            checkForAccountStatus(aSessionDetail);

            checkForIpWhiteList(aSessionDetail);

            checkForSmppAccess(aSessionDetail);

     //       checkForValidCluster(aSessionDetail);

            checkforBindTypeAllowed(aSessionDetail);

            checkForMaxBindReached(aSessionDetail);

            checkForBindAllowedForInstance(aSessionDetail);


            updateWindowSize(aSessionDetail);
        }
        catch (final Exception e)
        {

            if (!(e instanceof SmppProcessingException))
            {
                log.error("Exception while binding for user '" + aSessionDetail.getSystemId() + "'", e);

                Communicator.sendBindFailureLog(aSessionDetail, "0x" + HexUtil.toHexString(SmppConstants.STATUS_SYSERR), "Internal Error");
                throw new SmppProcessingException(SmppConstants.STATUS_SYSERR, "Internal Error");
            }

            log.error("Exception while binding for user '" + aSessionDetail.getSystemId() + "'");

            throw e;
        }
    }

    /*
    private static void checkForValidCluster(
            SessionDetail aSessionDetail)
            throws SmppProcessingException
    {
        final boolean isCluterAllow = SmppProperties.getInstance().getClusterInstanceAllow(aSessionDetail.getClusterType().getKey());

        if (!isCluterAllow)
        {
            Communicator.sendBindFailureLog(aSessionDetail, "0x" + HexUtil.toHexString(SmppConstants.STATUS_BINDFAIL), "Unauthorized Request");
            Communicator.sendUnbindInfoToDb(aSessionDetail, SmppConstants.STATUS_BINDFAIL, "Unauthorized Request");

            throw new SmppProcessingException(SmppConstants.STATUS_BINDFAIL);
        }
    }

   */

    private static void checkForIpWhiteList(
            SessionDetail aSessionDetail)
            throws SmppProcessingException
    {

        if (!aSessionDetail.isAllowedIp())
        {
            Communicator.sendBindFailureLog(aSessionDetail, "0x" + HexUtil.toHexString(SmppConstants.STATUS_INVDLNAME), "IP not whitelisted");
            Communicator.sendUnbindInfoToDb(aSessionDetail, SmppConstants.STATUS_INVDLNAME, "IP not whitelisted");

            throw new SmppProcessingException(SmppConstants.STATUS_INVDLNAME);
        }
    }

    private static void updateWindowSize(
            SessionDetail aSessionDetail)
    {
        // TODO: Get the custom window size from user_config table
        // final String isSyncSmppDn =
        // AccountDetails.getAccountCustomeFeature(aUserInfo.getClientId(),
        // CustomFeatures.SMPP_WINDOW_SIZE);
        // if ((isSyncSmppDn != null) && isSyncSmppDn.trim().equals("1"))
        // aSessionConfiguration.setWindowSize(1);
    }

    private static void checkForMaxBindReached(
            SessionDetail aSessionDetail)
            throws SmppProcessingException
    {
        final int maxConnectionsAllowedForUser = aSessionDetail.getMaxBindAllowed();
        final int lBoundCount                  = RedisBindOperation.getBindCountForClient(aSessionDetail.getClientId());

        if (log.isDebugEnabled())
        {
        	
            log.debug("Max Connection Allowed for Client id : '" + aSessionDetail.getClientId() + " usernane : "+aSessionDetail.getSystemId()+" Count :" + maxConnectionsAllowedForUser);
            log.debug("Session Bind Count :" + lBoundCount);
        }

        if (maxConnectionsAllowedForUser <= lBoundCount)
        {
            Communicator.sendBindFailureLog(aSessionDetail, "0x" + HexUtil.toHexString(SmppConstants.STATUS_ALYBND), "Max Bind reached");
            Communicator.sendUnbindInfoToDb(aSessionDetail, SmppConstants.STATUS_ALYBND, "Max Bind reached");

            Map<String,String> errormap=new HashMap<String,String>();
            
            errormap.put("systemid", aSessionDetail.getSystemId());
            errormap.put("clientid",  aSessionDetail.getClientId());
            errormap.put("maxConnectionsAllowedForUser", ""+maxConnectionsAllowedForUser);
            errormap.put("lBoundCount", ""+lBoundCount);

            LogWriter.getInstance().logs("validationerror", errormap);
            
            throw new SmppProcessingException(SmppConstants.STATUS_ALYBND);
        }
    }

    private static void checkForBindAllowedForInstance(
            SessionDetail aSessionDetail)
            throws SmppProcessingException
    {
        final int maxConnectionsForInstance = SmppProperties.getInstance().getMaxBindAllowed();
        final int lTotalBindCount           = RedisBindOperation.getTotalBindCount(aSessionDetail.getInstanceId());

        if (lTotalBindCount >= maxConnectionsForInstance)
        {
            Communicator.sendBindFailureLog(aSessionDetail, "0x" + HexUtil.toHexString(SmppConstants.STATUS_ALYBND), "Instance Max Bind reached");
            Communicator.sendUnbindInfoToDb(aSessionDetail, SmppConstants.STATUS_ALYBND, "Instance Max Bind reached");


            Map<String,String> errormap=new HashMap<String,String>();
            
            errormap.put("systemid", aSessionDetail.getSystemId());
            errormap.put("clientid",  aSessionDetail.getClientId());
            errormap.put("maxConnectionsForInstance", ""+maxConnectionsForInstance);
            errormap.put("lTotalBindCount", ""+lTotalBindCount);
            errormap.put("InstanceId", ""+aSessionDetail.getInstanceId());

            LogWriter.getInstance().logs("validationerror", errormap);

            throw new SmppProcessingException(SmppConstants.STATUS_ALYBND);
        }
    }

    private static void checkforBindTypeAllowed(
            SessionDetail aSessionDetail)
            throws SmppProcessingException
    {

        if (!aSessionDetail.isAllowedBindType())
        {
            Communicator.sendBindFailureLog(aSessionDetail, "0x" + HexUtil.toHexString(SmppConstants.STATUS_INVCMDID), "Invalid Bind Request Type");
            Communicator.sendUnbindInfoToDb(aSessionDetail, SmppConstants.STATUS_INVCMDID, "Invalid Bind Request Type");

            throw new SmppProcessingException(SmppConstants.STATUS_INVCMDID);
        }
    }

    private static void checkForSmppAccess(
            SessionDetail aSessionDetail)
            throws SmppProcessingException
    {

        if (!aSessionDetail.isSmppServiceEnabled())
        {
            Communicator.sendBindFailureLog(aSessionDetail, "0x" + HexUtil.toHexString(SmppConstants.STATUS_SUBMITFAIL), "SMPP Service Disabled");
            Communicator.sendUnbindInfoToDb(aSessionDetail, SmppConstants.STATUS_SUBMITFAIL, "SMPP Service Disabled");

            throw new SmppProcessingException(SmppConstants.STATUS_SUBMITFAIL);
        }
    }

    private static void checkForAccountStatus(
            SessionDetail aSessionDetail)
            throws SmppProcessingException
    {

        if (aSessionDetail.getAccountStatus() != AccountStatus.ACTIVE)
        {
            Communicator.sendBindFailureLog(aSessionDetail, "0x" + HexUtil.toHexString(SmppConstants.STATUS_INVPASWD), "Account is not active.");
            Communicator.sendUnbindInfoToDb(aSessionDetail, SmppConstants.STATUS_INVPASWD, "Account is not active.");

            throw new SmppProcessingException(SmppConstants.STATUS_INVPASWD);
        }
    }

    private static void checkForPassword(
            SessionDetail aSessionDetail,
            String aPassword)
            throws SmppProcessingException
    {

        if (!(aSessionDetail.isValidPassword(aPassword)))
        {
            Communicator.sendBindFailureLog(aSessionDetail, "0x" + HexUtil.toHexString(SmppConstants.STATUS_INVPASWD), "Invalid Password");
            Communicator.sendUnbindInfoToDb(aSessionDetail, SmppConstants.STATUS_INVPASWD, "Invalid Password");

            throw new SmppProcessingException(SmppConstants.STATUS_INVPASWD);
        }
    }

    private static void checkForValidUser(
            SessionDetail aSessionDetail)
            throws SmppProcessingException
    {

        try
        {
            aSessionDetail.updateUserInfo();

            log.fatal("User account information loaded for systemid:'" + aSessionDetail.getSystemId() + "'");
        }
        catch (final Exception e)
        {
            Communicator.sendBindFailureLog(aSessionDetail, "0x" + HexUtil.toHexString(SmppConstants.STATUS_INVSYSID), "User not exists");
            Communicator.sendUnbindInfoToDb(aSessionDetail, SmppConstants.STATUS_INVSYSID, "User not exists");

            throw new SmppProcessingException(SmppConstants.STATUS_INVSYSID);
        }
    }

    private static void checkForEmptyPassword(
            SessionDetail aSessionDetail,
            String aPassword)
            throws SmppProcessingException
    {
        final String password = CommonUtility.nullCheck(aPassword, true);

        if ("".equals(password))
        {
            Communicator.sendBindFailureLog(aSessionDetail, "0x" + HexUtil.toHexString(SmppConstants.STATUS_INVPASWD), "Invalid Password");
            Communicator.sendUnbindInfoToDb(aSessionDetail, SmppConstants.STATUS_INVPASWD, "Invalid Password");

            throw new SmppProcessingException(SmppConstants.STATUS_INVPASWD);
        }
    }

    private static void checkForEmptySystemId(
            SessionDetail aSessionDetail)
            throws SmppProcessingException
    {
        final String systemId = CommonUtility.nullCheck(aSessionDetail.getSystemId(), true);

        if ("".equals(systemId))
        {
            Communicator.sendBindFailureLog(aSessionDetail, "0x" + HexUtil.toHexString(SmppConstants.STATUS_INVSYSID), "Empty SystemId");
            Communicator.sendUnbindInfoToDb(aSessionDetail, SmppConstants.STATUS_INVSYSID, "Empty SystemId");

            throw new SmppProcessingException(SmppConstants.STATUS_INVSYSID);
        }
    }

    private static boolean checkBindType(
            BaseBind request,
            String bindType)
    {
        final int commandId       = request.getCommandId();
        boolean   bindTypeMatched = false;

        if (bindType != null)
            if ((bindType.trim().equalsIgnoreCase(ItextosSmppConstants.TRANSMITTER) && (commandId == SmppConstants.CMD_ID_BIND_TRANSMITTER))
                    || (bindType.trim().equalsIgnoreCase(ItextosSmppConstants.RECEIVER) && (commandId == SmppConstants.CMD_ID_BIND_RECEIVER))
                    || (bindType.trim().equalsIgnoreCase(ItextosSmppConstants.TRANSCEIVER) && (commandId == SmppConstants.CMD_ID_BIND_TRANSCEIVER)))
                bindTypeMatched = true;
        return bindTypeMatched;
    }

    public static void validateSubmitSm(
            SubmitSm aSubmitSmRequest,
            SubmitSmResp aSubmitSmResponse,
            SessionDetail aSessionDetail,
            StringBuffer sb)
    {
        final SubmitSmRequest    lSmRequest          = new SubmitSmRequest(aSubmitSmRequest, aSubmitSmResponse, aSessionDetail,sb);

        final SmppMessageRequest lSmppMessageRequest = lSmRequest.getMessageRequest();

        try
        {

            if (aSubmitSmResponse.getCommandStatus() != 0)
            {
             //   lSmppMessageRequest.setRouteType(RouteType.DOMESTIC);
                log.warn("Failed Invalid Expiry with command status=" + aSubmitSmResponse.getCommandStatus());
                return;
            }

            validateUDHForSP(aSubmitSmResponse, lSmppMessageRequest);

            if (aSubmitSmResponse.getCommandStatus() != 0)
            {
                log.warn("Failed Invalid UDH with command status=" + aSubmitSmResponse.getCommandStatus());
                return;
            }

            validateDest(aSubmitSmResponse, aSessionDetail, lSmppMessageRequest);

            if (aSubmitSmResponse.getCommandStatus() != 0)
            {
                log.warn("Failed Invalid Destiation with command status=" + aSubmitSmResponse.getCommandStatus());
                return;
            }

            validateHeader(lSmppMessageRequest, aSubmitSmResponse);

            if (aSubmitSmResponse.getCommandStatus() != 0)
            {
                log.warn("Failed Invalid Header with command status=" + aSubmitSmResponse.getCommandStatus());
                return;
            }

            validateMessage(lSmppMessageRequest, aSubmitSmResponse);

            if (aSubmitSmResponse.getCommandStatus() != 0)
            {
                log.warn("Failed message will not processed with Message Empty, command status=" + aSubmitSmResponse.getCommandStatus());
                return;
            }

            validateDltEntityId(lSmppMessageRequest, aSubmitSmResponse);

            if (aSubmitSmResponse.getCommandStatus() != 0)
            {
                log.warn("Failed message will not processed with Invalid DLT Entity Id, command status=" + aSubmitSmResponse.getCommandStatus());
                return;
            }

            validateDltTemplateId(lSmppMessageRequest, aSubmitSmResponse);

            if (aSubmitSmResponse.getCommandStatus() != 0)
            {
                log.warn("Failed message will not processed with Invalid DLT Template Id, command status=" + aSubmitSmResponse.getCommandStatus());
                return;
            }

            throttleMessage(aSessionDetail, aSubmitSmResponse, lSmppMessageRequest);

            if (aSubmitSmResponse.getCommandStatus() != 0)
            {
                log.warn("Failed message will not processed with Message Throttle Failed, command status=" + aSubmitSmResponse.getCommandStatus());
                return;
            }

            checkTimeOfDelivery(lSmppMessageRequest, aSubmitSmResponse, aSessionDetail);

            if (aSubmitSmResponse.getCommandStatus() != 0)
            {
                log.warn("Failed message will not processed with TRA/SCHEDULE Failed, command status=" + aSubmitSmResponse.getCommandStatus());
                return;
            }
        }
        finally
        {
            sendToPlatform(lSmppMessageRequest, aSubmitSmResponse, aSessionDetail,sb);
        }
    }

    private static void validateUDHForSP(
            SubmitSmResp aSubmitSmResponse,
            SmppMessageRequest aSmppMessageRequest)
    {

        if ((aSmppMessageRequest.getMsgClass() == MessageClass.SP_PLAIN_MESSAGE) || (aSmppMessageRequest.getMsgClass() == MessageClass.SP_UNICODE_MESSAGE))
        {
            if (log.isDebugEnabled())
                log.debug("Validate UDH for Special-Port Message udh:'" + aSmppMessageRequest.getUdh() + "'");

            if (!aSmppMessageRequest.getUdh().startsWith(UdhHeaderInfo.CONCAT_PORT_HEADER_PREFIX.getKey()))
            {
            //    aSmppMessageRequest.setRouteType(RouteType.DOMESTIC);
                aSmppMessageRequest.setInterfaceErrorCode(InterfaceStatusCode.INVALID_UDH.getStatusCode());
                aSubmitSmResponse.setCommandStatus(SmppConstants.STATUS_SUBMITFAIL);
            }
        }
    }

    private static void sendToPlatform(
            SmppMessageRequest aSubmitSmRequest,
            SubmitSmResp aSubmitSmResp,
            SessionDetail aSessionDetail,
            StringBuffer sb)
    {
        final String lMessageId = MessageIdentifier.getInstance().getNextId();
        aSubmitSmResp.setMessageId(lMessageId);
        aSubmitSmRequest.setAppInstanceId(MessageIdentifier.getInstance().getAppInstanceId());
        aSubmitSmRequest.setAckid(lMessageId);
        aSubmitSmRequest.setSmppInstance(aSessionDetail.getInstanceId());
        // Push Message
        mwHandover(aSubmitSmRequest, aSessionDetail, aSubmitSmResp,sb);
    }

    private static void throttleMessage(
            SessionDetail aSessionDetail,
            SubmitSmResp aSubmitSmResp,
            SmppMessageRequest aSmppMessageRequest)
    {
        final String lClientId = aSessionDetail.getClientId();
        final int    lMaxSpeed = aSessionDetail.getMaxSpeedAllowded();

        if ((lMaxSpeed > 0) && !Throttler.canSend(lClientId, lMaxSpeed))
        {
            aSubmitSmResp.setCommandStatus(SmppConstants.STATUS_THROTTLED);
            aSmppMessageRequest.setInterfaceErrorCode(InterfaceStatusCode.SMPP_THROTTLE_LIMIT_EXCEED.getStatusCode());
        }
    }

    private static void validateMessage(
            SmppMessageRequest aSmppMessageRequest,
            SubmitSmResp aSubmitSmResp)
    {
        final String lMessage = CommonUtility.nullCheck(aSmppMessageRequest.getMessage(), true);

        if (lMessage.isBlank())
        {
            aSubmitSmResp.setCommandStatus(SmppConstants.STATUS_INVMSGLEN);
            aSmppMessageRequest.setInterfaceErrorCode(InterfaceStatusCode.MESSAGE_EMPTY.getStatusCode());
        }
    }

    private static void validateDest(
            SubmitSmResp aSubmitSmResponse,
            SessionDetail aSessionDetail,
            SmppMessageRequest aSmppMessageRequest)
    {
        final String lMobileNumber = CommonUtility.nullCheck(aSmppMessageRequest.getMobileNumber(), true);

        if (lMobileNumber.isEmpty())
        {
      //      aSmppMessageRequest.setRouteType(RouteType.DOMESTIC);
            aSubmitSmResponse.setCommandStatus(SmppConstants.STATUS_INVDSTADR);
            aSmppMessageRequest.setInterfaceErrorCode(InterfaceStatusCode.DESTINATION_EMPTY.getStatusCode());
            return;
        }

        final String                lCountryCD                        = InterfaceUtil.getDefaultCountryCode();
        final boolean               isConsiderDefaultLengthAsDomestic = aSessionDetail.considerDefaultLengthAsDomesitic();
        final boolean               isDomesticSpecialSeriesAllow      = aSessionDetail.isDomesticSpecialSeriesAllow();
        final boolean               isIntlServiceAllow                = isIntlServiceAllow(aSessionDetail);
        final MobileNumberValidator lMobileValidator                  = InterfaceUtil.validateMobile(lMobileNumber, lCountryCD, isIntlServiceAllow, isConsiderDefaultLengthAsDomestic, false, "",
                isDomesticSpecialSeriesAllow);

        final boolean               isValidMobile                     = lMobileValidator.isValidMobileNumber();

        if (log.isDebugEnabled())
            log.debug("Is Valid MobileNumber : " + isValidMobile);

        if (isValidMobile)
        {
            aSmppMessageRequest.setMobileNumber(lMobileValidator.getMobileNumber());
            RouteType lRouteType = RouteType.DOMESTIC;

            if (lMobileValidator.isIntlMobileNumber())
            {

                if (isIntlServiceAllow)
                    lRouteType = RouteType.INTERNATIONAL;
                else
                {
                    // Reject as INTL Serivce not available.
                    aSubmitSmResponse.setCommandStatus(SmppConstants.STATUS_SUBMITFAIL);
                    aSmppMessageRequest.setInterfaceErrorCode(InterfaceStatusCode.INTL_SERVICE_DISABLED.getStatusCode());
                }
            }
            else
            {
                if (log.isDebugEnabled())
                    log.debug("Domestic Number --'" + lMobileValidator.getMobileNumber() + "'");
                // set special Series domestic info
                if (lMobileValidator.isSpecialSeriesNumber())
                    aSmppMessageRequest.setSpecialSeriesNumber(lMobileValidator.isSpecialSeriesNumber());
            }

            aSmppMessageRequest.setRouteType(lRouteType);
        }
        else
        {
            aSmppMessageRequest.setRouteType(RouteType.DOMESTIC);
            aSubmitSmResponse.setCommandStatus(SmppConstants.STATUS_INVDSTADR);
            aSmppMessageRequest.setInterfaceErrorCode(InterfaceStatusCode.DESTINATION_INVALID.getStatusCode());
        }
    }

    /*
     * private static void canProcessDND(
     * String aMobileNumber,
     * SubmitSmResp aSubmitResponse,
     * SessionDetail aSessionDetail)
     * {
     * final int lDndPref = aSessionDetail.getDndPreferences();
     * if (log.isDebugEnabled())
     * log.debug("Before process DND check MobileNumber - " + aMobileNumber +
     * " DND_PREF : " + lDndPref);
     * try
     * {
     * final boolean isDndRejectEnable = aSessionDetail.isDndRejectYN();
     * if (isDndRejectEnable)
     * {
     * final String dndNcpr = DNDCheck.getDNDInfo(aMobileNumber);
     * if (((dndNcpr != null) && dndNcpr.equalsIgnoreCase("0")) || ((dndNcpr !=
     * null) && (lDndPref == 0)) || ((dndNcpr != null) &&
     * !dndNcpr.contains(String.valueOf(lDndPref))))
     * {
     * if (log.isWarnEnabled())
     * log.warn("Message DND rejeceted...");
     * aSubmitResponse.setCommandStatus(SmppConstants.STATUS_INVDSTADR);
     * }
     * }
     * }
     * catch (final Exception e)
     * {
     * log.error(" DND Check err", e);
     * }
     * }
     */
    private static void checkTimeOfDelivery(
            SmppMessageRequest aSmppMessageReq,
            SubmitSmResp aSubmitResponse,
            SessionDetail aSessionDetails)
    {

        try
        {
            final String lScheduleTime = aSmppMessageReq.getScheduleTime();
            if (log.isDebugEnabled())
                log.debug("Schedule Time : " + lScheduleTime);

            final Date lToDate = (lScheduleTime == null) ? new Date() : DateTimeUtility.getDateFromString(lScheduleTime, DateTimeFormat.DEFAULT);

            if (lToDate != null)
            {
                final MessageType lMsgType = aSessionDetails.getMessageType();

                if ((MessageType.PROMOTIONAL == lMsgType) && ( aSmppMessageReq.getRouteType()!=null && RouteType.DOMESTIC == aSmppMessageReq.getRouteType()))
                {
                    final boolean lTraBlockoutStatus = TraiBlockoutCheck.isValidTraiBlockOut(lToDate, aSessionDetails, aSmppMessageReq, aSubmitResponse);

                    if (log.isDebugEnabled())
                        log.debug("Tra blockout status : " + lTraBlockoutStatus);
                }
            }
        }
        catch (final Exception e)
        {
            aSubmitResponse.setCommandStatus(SmppConstants.STATUS_SYSERR);
            aSmppMessageReq.setInterfaceErrorCode(InterfaceStatusCode.INTERNAL_SERVER_ERROR.getStatusCode());
            log.error("Problem processing pdu scheduled time due to ...", e);
        }
    }

    public static boolean isIntlServiceAllow(
            SessionDetail aSessionDetail)
    {
        return aSessionDetail.isIntlServiceAllowed();
    }

    private static void mwHandover(
            SmppMessageRequest aSmppMessageRequest,
            SessionDetail aSessionDetail,
            SubmitSmResp aSubmitResponse,
            StringBuffer sb)
    {
        boolean            isValidDCS          = true;

        PlatformStatusCode aPlatformStatusCode = null;

        try
        {
            final DCS dcs = DCS.getDcs(aSmppMessageRequest.getDcs());
            dcs.getKey();
        }
        catch (final Exception e)
        {
            isValidDCS = false;
            log.fatal(aSmppMessageRequest.getSystemId() + ", Invalid Data Coding Scheme :'" + aSmppMessageRequest.getDcs() + "'");
            aPlatformStatusCode = PlatformStatusCode.INVALID_DATA_CODING_SCHEME;
        }

    
            if (log.isDebugEnabled())
                log.debug("Processing singlepart request ...........");
            send2Platform(aSmppMessageRequest, aSubmitResponse, aSessionDetail, aPlatformStatusCode,sb);
    }

 

    private static void send2Platform(
            SmppMessageRequest aSmppMessageRequest,
            SubmitSmResp aSubmitResponse,
            SessionDetail aSessionDetail,
            PlatformStatusCode aPlatformStatusCode,
            StringBuffer sb)
    {

        try
        {
            final MessageRequest lMessageRequest = BuildMessageRequest.getMessageRequest(aSmppMessageRequest, aSessionDetail, aPlatformStatusCode);

            if (log.isDebugEnabled())
                log.debug("Message Request Object sending to Kafka : " + lMessageRequest);

            if(DNQueue.getInstance(lMessageRequest.getUser()).queuesize()>50000) {
            	
                aSubmitResponse.setCommandStatus(SmppConstants.STATUS_MSGQFUL);

            }else {
            
            	DNQueue.getInstance(lMessageRequest.getUser()).addToQueue(lMessageRequest);

            }
            
            if (aSubmitResponse.getCommandStatus() == 0)
                aSubmitResponse.setCommandStatus(SmppConstants.STATUS_OK);

            if (log.isDebugEnabled())
                log.debug("Successfully send to kafka ........");
        }
        catch (final Exception e)
        {
            log.error("Exception while processing Message Request Object..", e);
            aSubmitResponse.setCommandStatus(SmppConstants.STATUS_SYSERR);
        }
    }

    private static void validateHeader(
            SmppMessageRequest aSmppMessageRequest,
            SubmitSmResp aSubmitSmResp)
    {
        final String lHeader = aSmppMessageRequest.getHeader();

        if (lHeader.isBlank())
        {
            aSubmitSmResp.setCommandStatus(SmppErrorCodes.STATUS_INVDSRC);
            aSmppMessageRequest.setInterfaceErrorCode(InterfaceStatusCode.SENDER_ID_EMPTY.getStatusCode());
        }

        if (lHeader.length() > 15)
        {
            aSubmitSmResp.setCommandStatus(SmppErrorCodes.STATUS_INVDSRC);
            aSmppMessageRequest.setInterfaceErrorCode(InterfaceStatusCode.INVALID_SENDERID.getStatusCode());
        }
    }

    private static void validateDltEntityId(
            SmppMessageRequest aSmppMessageRequest,
            SubmitSmResp aSubmitSmResp)
    {
        final String aDltEntityId = aSmppMessageRequest.getDltEntityId();

        if ((aDltEntityId != null) && !aDltEntityId.isEmpty())
            if (!validateNumaricAndLength(aDltEntityId))
            {
                if (log.isDebugEnabled())
                    log.debug("Invalid DLT Entity Id :" + aDltEntityId);
                aSubmitSmResp.setCommandStatus(SmppConstants.STATUS_DELIVERYFAILURE);
                aSmppMessageRequest.setInterfaceErrorCode(InterfaceStatusCode.INVALID_DLT_ENTITY_ID.getStatusCode());
            }
    }

    private static void validateDltTemplateId(
            SmppMessageRequest aSmppMessageRequest,

            SubmitSmResp aSubmitSmResp)
    {
        final String aDltTemplateId = aSmppMessageRequest.getDltTemplateId();

        if ((aDltTemplateId != null) && !aDltTemplateId.isEmpty())
            if (!validateNumaricAndLength(aDltTemplateId))
            {
                if (log.isDebugEnabled())
                    log.debug("Invalid DLT Template  Id :" + aDltTemplateId);
                aSubmitSmResp.setCommandStatus(SmppConstants.STATUS_DELIVERYFAILURE);
                aSmppMessageRequest.setInterfaceErrorCode(InterfaceStatusCode.INVALID_DLT_TEMPLATE_ID.getStatusCode());
            }
    }

    private static boolean validateNumaricAndLength(
            String aValue)
    {
        final int lMinValue = CommonUtility.getInteger(AccountDetails.getConfigParamsValueAsString(ConfigParamConstants.DLT_PARAM_MIN_LENGTH));
        final int lMaxValue = CommonUtility.getInteger(AccountDetails.getConfigParamsValueAsString(ConfigParamConstants.DLT_PARAM_MAX_LENGTH));

        if (ItextosSmppUtil.isNumaric(aValue) && ((aValue.length() >= lMinValue) && (aValue.length() <= lMaxValue)))
            return true;

        return false;
    }

}
