package com.itextos.beacon.smpp.objects.request;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cloudhopper.commons.charset.Charset;
import com.cloudhopper.commons.charset.CharsetUtil;
import com.cloudhopper.commons.util.HexUtil;
import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.pdu.SubmitSmResp;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.CustomFeatures;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.FeatureCode;
import com.itextos.beacon.commonlib.constants.InterfaceStatusCode;
import com.itextos.beacon.commonlib.constants.MessageClass;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.timezoneutility.TimeZoneUtility;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.smpp.objects.SessionDetail;
import com.itextos.beacon.smpp.utils.AccountDetails;
import com.itextos.beacon.smpp.utils.ItextosSmppConstants;
import com.itextos.beacon.smpp.utils.ItextosSmppUtil;
import com.itextos.beacon.smpp.utils.SmppDateHandler;
import com.itextos.beacon.smpp.utils.UdhExtractor;
import com.itextos.beacon.smpp.utils.enums.SmppCharset;

public class SubmitSmRequest
        extends
        AbstractSmppRequestObject
{

    private static final Log log                  = LogFactory.getLog(SubmitSmRequest.class);

    private String           mSystemId            = null;
    private String           mMobileNumber        = null;
    private String           mMessage             = null;
    private String           mHeader              = null;
    private String           mScheduleTime        = null;
    private String           mUdh                 = null;
    private String           mDlrReqFromClient    = null;
    private String           mMessageExpiry       = null;
    private long             mReceivedTime        = 0;
    private MessageClass     mMsgclass            = null;
    private String           mUdhi                = null;
    private String           mClientIp            = null;
    private String           mDltEntityId         = null;
    private String           mDltTemplateId       = null;
    private String           mDltTelemarketerId       = null;

    private boolean          mIsHexMsg            = false;
    private int              mDestPort            = 0;
    private SessionDetail    mSessionDetail       = null;
    private SubmitSm         mSubmitSm            = null;
    private String           mEsmClass            = null;
    private String           mClientId            = null;
    private String           mUdhRefNum           = null;
    private int              mTotalMsgParts       = 0;
    private int              mPartNumber          = 0;
    private String           mServiceType         = null;
    private boolean          isConcatMessage      = false;
    private FeatureCode      mFeatureCode         = null;
    private int              mDcs                 = -1;
    private String           mCluster             = null;
    private String           mInterfaceStatusCode = null;
    private String           mClientMid           = null;

    private StringBuffer sb =null;
    public SubmitSmRequest(
            SubmitSm aSubmitSm,
            SubmitSmResp aSubmitSmResp,
            SessionDetail aSessionDetail, StringBuffer sb)
    {
    	this.sb=sb;
        mSubmitSm      = aSubmitSm;
        mSessionDetail = aSessionDetail;
        mSystemId      = mSessionDetail.getSystemId();

        mReceivedTime  = System.currentTimeMillis();
        mServiceType   = mSubmitSm.getServiceType();

        mClientIp      = mSessionDetail.getHost();
        mMobileNumber  = CommonUtility.nullCheck(mSubmitSm.getDestAddress().getAddress(), true);
        mHeader        = CommonUtility.nullCheck(mSubmitSm.getSourceAddress().getAddress(), true);
        mClientId      = mSessionDetail.getClientId();

        updateDcs();
        updateEsmClass();

        updateMessageAndMessageClass();
        setScheduleDelivery(aSubmitSm);
        setMessageValidityPeriod(aSubmitSm, aSubmitSmResp);

        if (log.isDebugEnabled())
            log.debug("Registered Delivery Value : " + mSubmitSm.getRegisteredDelivery());

        if (mSubmitSm.getRegisteredDelivery() > 0)
            mDlrReqFromClient = "1";
        else
            mDlrReqFromClient = "0";

        log.info(mSystemId + " Delivery type : " + mDlrReqFromClient);

        mDltEntityId    = getOptionalParameter(mSystemId, aSubmitSm, aSessionDetail.getDltEntityId());
        mDltTemplateId  = getOptionalParameter(mSystemId, aSubmitSm, aSessionDetail.getDltTemplateId());
        mDltTelemarketerId  = getOptionalParameter(mSystemId, aSubmitSm, aSessionDetail.getClientMidTag());
        mClientMid      = getOptionalParameter(mSystemId, aSubmitSm, aSessionDetail.getClientMidTag());

        isConcatMessage = isConcatMessage();
        mCluster        = aSessionDetail.getPlatformCluster().getKey();

        extractUdh();
        setFeatureCode();
    }

    private void updateMessageAndMessageClass()
    {

        /**
         * In MIDE the message length more than 160 characters will be encrypted
         * and set in the Messagepayload and privacy indicator will be set to 1.
         * If the esm value is set then UDH will be splitted from the message.
         */
        try
        {

            if ((mEsmClass != null) && (mEsmClass.equals(ItextosSmppConstants.ESM_CLASS_40) || mEsmClass.equals(ItextosSmppConstants.ESM_CLASS_43)))
            {
                // Multipart Message
                final String msgWithHeader = HexUtil.toHexString(mSubmitSm.getShortMessage());
                log.info(mSystemId + "  UDH+message : " + msgWithHeader);
                spiltMsgWithHeader(msgWithHeader);

                mMessage = (mMessage == null) ? "" : mMessage;
                if (!mMessage.isBlank())
                    convertMessageFromMessage(mIsHexMsg ? mMessage.getBytes() : HexUtil.toByteArray(mMessage));

                mUdhi = "0";
            }
            else
            {
                if (log.isDebugEnabled())
                    log.debug("Dcs Value : " + mDcs);

                if ((mDcs == ItextosSmppConstants.DCS_8) || (mDcs == ItextosSmppConstants.DCS_18) || (mDcs == ItextosSmppConstants.DCS_24))
                {
                    // Single Part Unicode
                    mMessage  = HexUtil.toHexString(mSubmitSm.getShortMessage());
                    mIsHexMsg = true;
                    log.info(mSystemId + " Unicode Msg : " + mMessage);
                }
                else
                {
                  
                   
                    sb.append("\n").append("Hex message content--->" + HexUtil.toHexString(mSubmitSm.getShortMessage())).append("\n");
                    convertMessageFromObject();

                    log.info(mSystemId + " Message : " + mMessage);

                    mMessage = (mMessage == null) ? "" : mMessage;
                }
            }

            if (mDcs != ItextosSmppConstants.DCS_INVALID)
            {
                final Map map = UdhExtractor.extractParams(mUdh, true);
                checkMsgType(CommonUtility.getInteger(CommonUtility.nullCheck(map.get(MiddlewareConstant.MW_DESTINATION_PORT))));
            }
            else
                mMsgclass = MessageClass.PLAIN_MESSAGE;
        }
        catch (final Exception e)
        {
            log.error(mSystemId + " Error while finding the message. ", e);
        }
    }

    private void updateDcs()
    {

        /**
         * Fetching the dcs value. This value will be used to check the message
         * type
         */
        try
        {
            final String dataCoding = Byte.toString(mSubmitSm.getDataCoding());

            log.info(mSystemId + " DataCoding : " + dataCoding);
            mDcs = CommonUtility.getInteger(dataCoding, ItextosSmppConstants.DCS_INVALID);
        }
        catch (final Exception e)
        {
            log.error(mSystemId + "  Error while finding dataCoding.  ", e);
        }
    }

    private void updateEsmClass()
    {
        String esmClass = null;

        /**
         * Fetching the esm value. Esm indicates the UDH present in the message
         * or not
         */
        try
        {
            esmClass = Byte.toString(mSubmitSm.getEsmClass());

            if ((esmClass != null) && (esmClass.length() > 0))
            {
                log.info(mSystemId + " EsmClass : " + esmClass);

                esmClass = ItextosSmppUtil.getHexString(esmClass);
                log.info(mSystemId + " EsmClass (HEX): " + esmClass);
            }
            else
                esmClass = null;

            mEsmClass = esmClass;
        }
        catch (final Exception e)
        {
            log.error(mSystemId + " Error while finding esmClass. ", e);
        }
    }

    private static String getOptionalParameter(
            String aSystemId,
            SubmitSm aSubmitSm,
            String aOptionalParamter)
    {
        String       returnValue = null;
        final String optParam    = CommonUtility.nullCheck(aOptionalParamter, true);

        if (log.isDebugEnabled())
            log.debug("Optional Param: " + optParam);

        if (!optParam.isBlank())
            try
            {
                final short tlvParam = Short.parseShort(optParam, 16);

                if (log.isDebugEnabled())
                    log.debug("TLV Param :" + tlvParam);

                if ((aSubmitSm.hasOptionalParameter(tlvParam)) && (aSubmitSm.getOptionalParameter(tlvParam) != null))
                {
                    returnValue = aSubmitSm.getOptionalParameter(tlvParam).getValueAsString();
                    log.info(aSystemId + " Optional Param: '" + optParam + "' Returns '" + returnValue + "'");
                }
            }
            catch (final Exception e)
            {
                log.error("Exception while getting Optional Param: '" + optParam + "' for SystemId '" + aSystemId + "'", e);
            }
        return returnValue;
    }

    private void setScheduleDelivery(
            SubmitSm aSubmitSm)
    {

        /**
         * Fetching the schedule delivery time.
         */

        try
        {
            // the full smpp format is yymmddhhmmsstnnp.ignore last 6
            final String origDelTS = aSubmitSm.getScheduleDeliveryTime();
            if (log.isInfoEnabled())
                log.info(mSystemId + " origDelTS: " + origDelTS);

            if ((origDelTS != null) && !origDelTS.isEmpty() && (origDelTS.length() > 10))
            {
                final String deliveryTS = origDelTS.substring(0, 10);

                if (log.isInfoEnabled())
                    log.info(mSystemId + " DeliveryTS: " + deliveryTS);

                mScheduleTime = DateTimeUtility.getFormattedDateTime(DateTimeUtility.getDateFromString(deliveryTS, DateTimeFormat.NO_SEPARATOR_YY_MM_DD_HH_MM),
                        DateTimeFormat.DEFAULT_YYYY_MM_DD_HH_MM);
                /*
                 * final SimpleDateFormat smppDateFormat = new
                 * SimpleDateFormat(SmppUtilConstants.SMPP_DATE_FORMAT);
                 * smppDateFormat.setLenient(false);
                 * final Date date = smppDateFormat.parse(deliveryTS);
                 * if (log.isInfoEnabled())
                 * log.info("sdf_1.parse(deliveryTS)=>>" + date);
                 * final SimpleDateFormat dateFormat = new
                 * SimpleDateFormat(SmppUtilConstants.DATE_FORMAT);
                 * dateFormat.setLenient(false);
                 * mScheduleTime = dateFormat.format(date);
                 */

                if (log.isInfoEnabled())
                    log.info(mSystemId + "  Scheduled Delivery Time: " + mScheduleTime);

                scheduleValidity();
            }
            else
                mScheduleTime = null;
        }
        catch (final Exception e)
        {
            log.error(mSystemId + "Error while finding scheduled delivery time. ", e);
        }
    }

    private void scheduleValidity()
    {
        if (log.isDebugEnabled())
            log.debug("Schedule Validity Period ............");

        mScheduleTime = CommonUtility.nullCheck(mScheduleTime, true);

        if (!mScheduleTime.isEmpty())
        {
            String       deliveryTime = DateTimeUtility.getFormattedDateTime(DateTimeUtility.getDateFromString(mScheduleTime, DateTimeFormat.DEFAULT_YYYY_MM_DD_HH_MM), DateTimeFormat.DEFAULT);

            final String timeZone     = CommonUtility.nullCheck(mSessionDetail.getAccountTimeZone());

            if (log.isDebugEnabled())
                log.debug("Time Zone Value :" + timeZone + ", Delivery time:'" + deliveryTime + "'");

            if (!timeZone.isBlank())
                try
                {
                    // Converting schedule time from UTC into IST timezone
                    final Date lScheduleDate = TimeZoneUtility.getDateBasedOnTimeZone(deliveryTime, DateTimeFormat.DEFAULT, timeZone);
                    if (log.isDebugEnabled())
                        log.debug("After convert Timezone sctime :" + lScheduleDate);

                    final String deliveryTimeInTz = DateTimeUtility.getFormattedDateTime(lScheduleDate, DateTimeFormat.DEFAULT);

                    if (log.isDebugEnabled())
                        log.debug("Formated Timezone sctime :" + deliveryTimeInTz);

                    if (deliveryTimeInTz != null)
                        deliveryTime = deliveryTimeInTz;
                }
                catch (final Exception ignore)
                {}
            mScheduleTime = deliveryTime;

            if (log.isDebugEnabled())
                log.debug("Schedule time after timezone set : " + mScheduleTime);
        }
    }

    private void setMessageValidityPeriod(
            SubmitSm aSubmitSm,
            SubmitSmResp aSubmitSmResp)
    {

        /**
         * Fetching the validity period, finding the difference from the current
         * time in minutes.
         */
        try
        {
            final String origValPeriod = aSubmitSm.getValidityPeriod();
            if (log.isInfoEnabled())
                log.info(mSystemId + "  origValPeriod :" + origValPeriod);

            if ((origValPeriod != null) && !origValPeriod.isEmpty() && (origValPeriod.length() > 4))
            {
                final boolean isValidityPeriodTimeZoneBasedOnApplicationTimeZone = isValidityPeriodTimeZoneBasedOnApplicationTimeZone(String.valueOf(mSessionDetail.getClientId()));

                if (isValidityPeriodTimeZoneBasedOnApplicationTimeZone)
                {
                    if (log.isInfoEnabled())
                        log.info(mSystemId + " validityPeriod (IST) ....");

                    // the full smpp format is yymmddhhmmsstnnp.ignore last 4
                    final String validityPeriod = origValPeriod.substring(0, origValPeriod.length() - 4);

                    if (log.isInfoEnabled())
                        log.info(mSystemId + " validityPeriod:" + validityPeriod);

                    final Date validDate = DateTimeUtility.getDateFromString(validityPeriod, DateTimeFormat.NO_SEPARATOR_YY_MM_DD_HH_MM_SS);
                    checkAndSetExpiryTime(aSubmitSmResp, validDate);
                }
                else
                {
                    // Default SmppInterface allowd ValidityPeriod time zone is UTC.
                    if (log.isInfoEnabled())
                        log.info(mSystemId + "  validityPeriod (UTC) ....");

                    try
                    {
                        final Date lValidDate = new SmppDateHandler(origValPeriod).getScheduledTime();

                        if (lValidDate != null)
                            checkAndSetExpiryTime(aSubmitSmResp, lValidDate);
                        else
                            mMessageExpiry = null;
                    }
                    catch (final Exception e)
                    {
                        mMessageExpiry = null;
                        log.error(mSystemId + "Exception occer while parsing Validity period- ", e);
                    }
                }
            }
            if (log.isInfoEnabled())
                log.info(mSystemId + "Validity Period expiry Time in Sec:" + mMessageExpiry);
        }
        catch (final Exception e)
        {
            log.error(mSystemId + "Error while finding validity time. Error: ", e);
        }
    }

    private void checkAndSetExpiryTime(
            SubmitSmResp aSubmitSmResp,
            Date validDate)
    {
        final int  _MAX_EXPIRY = CommonUtility.getInteger(AccountDetails.getConfigParamsValueAsString(ConfigParamConstants.SMPP_MAX_EXPIRY_MINUTES_ALLOW));
        final long MAX_EXPIRY  = _MAX_EXPIRY * 60;

        final Date currentDate = new Date();
        final long validInSec  = (validDate.getTime() / 1000) - (currentDate.getTime() / 1000);

        if (log.isDebugEnabled())
        {
            log.debug("checkAndSetExpiryTime() - Max Expiry sec :" + MAX_EXPIRY);
            log.debug("checkAndSetExpiryTime() - Valid in sec :" + validInSec);
        }

        if (validInSec > MAX_EXPIRY)
        {
            aSubmitSmResp.setCommandStatus(SmppConstants.STATUS_INVEXPIRY);
            mInterfaceStatusCode = InterfaceStatusCode.EXPIRY_MINUTES_BEYOUND_TIME_BOUNDRY.getStatusCode();
            if (log.isInfoEnabled())
                log.info(mSystemId + "Invalid validity period");
        }
        else
        {
            if (validInSec <= 0)
                mMessageExpiry = null;
            else
                mMessageExpiry = String.valueOf(validInSec);

            if (log.isInfoEnabled())
                log.info(mSystemId + "validInSec :" + validInSec);
        }
    }

    private void convertMessageFromObject()
    {
        convertMessageFromMessage(mSubmitSm.getShortMessage());
    }

    private void convertMessageFromMessage(
            byte[] aMessageInBytes)
    {
        String charset = "";

        if ((mDcs == 3) || (mDcs == 1))
            charset = SmppCharset.ISO_8859_1.getKey();
        else
            charset = mSessionDetail.getSmppCharSet();

        if (log.isInfoEnabled())
            log.info("current charset=" + charset);
        
        sb.append("current charset=" + charset);

        Charset           toUserCharSet = CharsetUtil.CHARSET_ISO_8859_1;

        final SmppCharset lCharset      = SmppCharset.getCharset(charset);

        switch (lCharset)
        {
            case GSM:
                toUserCharSet = CharsetUtil.CHARSET_GSM;
                break;

            case ISO_8859_1:
                toUserCharSet = CharsetUtil.CHARSET_ISO_8859_1;
                break;

            case ISO_8859_15:
                toUserCharSet = CharsetUtil.CHARSET_ISO_8859_15;
                break;

            case UTF_8:
                toUserCharSet = CharsetUtil.CHARSET_UTF_8;
                break;

            case UCS_2:
                toUserCharSet = CharsetUtil.CHARSET_UCS_2;
                break;

            case GSM8:
                toUserCharSet = CharsetUtil.CHARSET_GSM8;
                break;

            case GSM7:
                toUserCharSet = CharsetUtil.CHARSET_GSM7;
                break;

            default:
                toUserCharSet = CharsetUtil.CHARSET_ISO_8859_1;
                break;
        }

        if (log.isDebugEnabled())
            log.debug("Charset :'" + toUserCharSet + "', Message Bytes:'" + Arrays.asList(aMessageInBytes) + "'");

        mMessage = CharsetUtil.decode(aMessageInBytes, toUserCharSet);

        if (log.isInfoEnabled())
            log.info("Message after conversion--->" + mMessage);
    }

    /**
     * This method is used to split the header from the message.
     *
     * @param msgWithHeader
     */
    private void spiltMsgWithHeader(
            String msgWithHeader)
    {
        final String headerLen = msgWithHeader.substring(0, 2);
        final int    totLen    = Integer.parseInt(headerLen, 16);

        if (log.isDebugEnabled())
            log.debug("Header Length :" + totLen);

        mUdh = msgWithHeader.substring(0, ((totLen * 2) + 2));
        log.info("UDH :" + mUdh);

        final String msgHex = msgWithHeader.substring(mUdh.length(), msgWithHeader.length());

        if ((mDcs == ItextosSmppConstants.DCS_INVALID) || (mDcs == ItextosSmppConstants.DCS_16) || (mDcs == ItextosSmppConstants.DCS_ZERO) || (mDcs == ItextosSmppConstants.DCS_12)
                || (mDcs == ItextosSmppConstants.DCS_MINUS_16) || (mDcs == ItextosSmppConstants.DCS_4))
            if (mUdh.toUpperCase().indexOf(ItextosSmppConstants.UDH_158_A) > 0)
            {
                mMessage  = msgHex;
                mIsHexMsg = true;
            }
            else
            {
                // mMessage = new String(HexUtil.toByteArray(msgHex));
                mMessage = msgHex;
                if (log.isInfoEnabled())
                    log.info("message after udh split--->" + mMessage);
            }
        else
        {
            mMessage  = msgHex;
            mIsHexMsg = true;
        }

        log.info(" IsHexMsg:" + mIsHexMsg + "\n" + " message=" + mMessage);
    }

    /**
     * This method is used to check the message type based on the esm and dcs
     * value.
     *
     * @param aDcs
     */
    private void checkMsgType(
            Integer aDestPort)
    {
        if (log.isDebugEnabled())
            log.debug("destPort : " + aDestPort);
        mDestPort = aDestPort;

        switch (mDcs)
        {
            case 11:
            case -11:
                mMsgclass = MessageClass.BINARY_MESSAGE;
                break;

            case 4:
                mMsgclass = MessageClass.SP_PLAIN_MESSAGE;
                break;

            case 12:
                mMsgclass = MessageClass.SP_PLAIN_MESSAGE;
                break;

            case 8:
                if ((aDestPort == null) || (aDestPort == 0))
                    mMsgclass = MessageClass.UNICODE_MESSAGE;
                else
                    mMsgclass = MessageClass.SP_UNICODE_MESSAGE;
                break;

            case 16:
            case -16:
                mMsgclass = MessageClass.FLASH_PLAIN_MESSAGE;
                break;

            case 18:
            case 24:
                mMsgclass = MessageClass.FLASH_UNICODE_MESSAGE;
                break;

            case 0:
                mMsgclass = MessageClass.PLAIN_MESSAGE;
                break;

            default:
                mMsgclass = MessageClass.PLAIN_MESSAGE;
                break;
        }
    }

    private static boolean isValidityPeriodTimeZoneBasedOnApplicationTimeZone(
            String aClientId)
    {
        final String lagacyVPTimeZoneEnabled = AccountDetails.getAccountCustomeFeature(aClientId, CustomFeatures.LEGACY_MESSAGE_EXPIRY_TIME_ZONE);
        // TODO This need to be done through the properties
        return CommonUtility.nullCheck(lagacyVPTimeZoneEnabled, true).equalsIgnoreCase("IST");
    }

    public SmppMessageRequest getMessageRequest()
    {
        final SmppMessageRequest lMessageRequest = new SmppMessageRequest();

        lMessageRequest.setDcs(mDcs);
        lMessageRequest.setUdh(mUdh);
        lMessageRequest.setEsmClass(mEsmClass);
        lMessageRequest.setMsgClass(mMsgclass);
        lMessageRequest.setDltEntityId(CommonUtility.nullCheck(mDltEntityId, true));
        lMessageRequest.setHexMsg(mIsHexMsg);
        lMessageRequest.setUdhReferenceNumber(mUdhRefNum);
        lMessageRequest.setDlrReqFromClient(mDlrReqFromClient);
        lMessageRequest.setDltTemplateId(CommonUtility.nullCheck(mDltTemplateId, true));
        lMessageRequest.setDltTelemarketerId(CommonUtility.nullCheck(mDltTelemarketerId, true));

        lMessageRequest.setTotalParts(mTotalMsgParts);
        lMessageRequest.setConcatMessage(isConcatMessage);
        lMessageRequest.setHeader(CommonUtility.nullCheck(mHeader, true));
        lMessageRequest.setScheduleTime(mScheduleTime);
        lMessageRequest.setReceivedTime(mReceivedTime);
        lMessageRequest.setClientId(mClientId);
        lMessageRequest.setClientIp(mClientIp);
        lMessageRequest.setUdhi(mUdhi);
        lMessageRequest.setMobileNumber(mMobileNumber);
        lMessageRequest.setMessage(mMessage);
        lMessageRequest.setMessageExpiry(mMessageExpiry);
        lMessageRequest.setPartNumber(mPartNumber);
        lMessageRequest.setDestPort(mDestPort);
        lMessageRequest.setFeatureCode(mFeatureCode);
        lMessageRequest.setServicetype(mServiceType);
        lMessageRequest.setCluster(mCluster);
        lMessageRequest.setInterfaceErrorCode(mInterfaceStatusCode);
        lMessageRequest.setSystemId(mSystemId);
        lMessageRequest.setBindType(mSessionDetail.getBindName());
        lMessageRequest.setCustMid(CommonUtility.nullCheck(mClientMid, true));

        return lMessageRequest;
    }

    private boolean isConcatMessage()
    {
        if (log.isDebugEnabled())
            log.debug("UDH value : " + mUdh);

        if (mUdh != null)
            try
            {
                final String lTempUDh = mUdh.substring(0, 6);
                return lTempUDh.equals(ItextosSmppConstants.UDH_CONCATENATE_1) || lTempUDh.equals(ItextosSmppConstants.UDH_CONCATENATE_2);
            }
            catch (final Exception e)
            {
                log.error("Invalid UDH received from customer: '" + mSystemId + "', UDH: '" + mUdh + "'", e);
            }

        return false;
    }

    private void extractUdh()
    {

        if (mUdh != null)
        {
            if (log.isDebugEnabled())
                log.debug("UDH : " + mUdh);

            if (mUdh.startsWith(ItextosSmppConstants.UDH_0500))
            {
                mUdhRefNum     = mUdh.substring(6, 8);
                mTotalMsgParts = new BigInteger(mUdh.substring(8, 10), 16).intValue();
                mPartNumber    = new BigInteger(mUdh.substring(10), 16).intValue();
            }
            else
                if (mUdh.startsWith(ItextosSmppConstants.UDH_0608))
                {
                    mUdhRefNum     = mUdh.substring(6, 10);
                    mTotalMsgParts = new BigInteger(mUdh.substring(10, 12), 16).intValue();
                    mPartNumber    = new BigInteger(mUdh.substring(12), 16).intValue();
                }
        }
    }

    private void setFeatureCode()
    {

        switch (mMsgclass)
        {
            case PLAIN_MESSAGE:
                mFeatureCode = isConcatMessage ? FeatureCode.PLAIN_MESSAGE_MULTI : FeatureCode.PLAIN_MESSAGE_SINGLE;
                break;

            case UNICODE_MESSAGE:
                mFeatureCode = isConcatMessage ? FeatureCode.UNICODE_MULTI : FeatureCode.UNICODE_SINGLE;
                break;

            case FLASH_PLAIN_MESSAGE:
                mFeatureCode = isConcatMessage ? FeatureCode.FLASH_PLAIN_MESSAGE_MULTI : FeatureCode.FLASH_PLAIN_MESSAGE_SINGLE;
                break;

            case FLASH_UNICODE_MESSAGE:
                mFeatureCode = isConcatMessage ? FeatureCode.FLASH_UNICODE_MULTI : FeatureCode.FLASH_UNICODE_SINGLE;
                break;

            case SP_PLAIN_MESSAGE:
                mFeatureCode = isConcatMessage ? FeatureCode.SPECIAL_PORT_PLAIN_MESSAGE_MULTI : FeatureCode.SPECIAL_PORT_PLAIN_MESSAGE_SINGLE;
                break;

            case SP_UNICODE_MESSAGE:
                mFeatureCode = isConcatMessage ? FeatureCode.SPECIAL_PORT_UNICODE_MULTI : FeatureCode.SPECIAL_PORT_UNICODE_SINGLE;
                break;

            case BINARY_MESSAGE:
                mFeatureCode = FeatureCode.BINARY_MSG;
                break;

            default:
                mFeatureCode = FeatureCode.BINARY_MSG;
                break;
        }
    }

}
