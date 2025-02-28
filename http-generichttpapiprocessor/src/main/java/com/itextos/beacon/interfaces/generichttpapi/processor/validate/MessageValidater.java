package com.itextos.beacon.interfaces.generichttpapi.processor.validate;

import java.text.MessageFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;

import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.ErrorMessage;
import com.itextos.beacon.commonlib.constants.InterfaceStatusCode;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.RouteType;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.Name;
import com.itextos.beacon.commonlib.utility.mobilevalidation.MobileNumberValidator;
import com.itextos.beacon.http.generichttpapi.common.data.BasicInfo;
import com.itextos.beacon.http.generichttpapi.common.data.InterfaceMessage;
import com.itextos.beacon.http.generichttpapi.common.data.InterfaceRequestStatus;
import com.itextos.beacon.http.generichttpapi.common.utils.APIConstants;
import com.itextos.beacon.http.generichttpapi.common.utils.InterfaceMessageClass;
import com.itextos.beacon.http.generichttpapi.common.utils.TraiBlockoutCheck;
import com.itextos.beacon.http.generichttpapi.common.utils.Utility;
import com.itextos.beacon.http.interfaceutil.InterfaceUtil;
import com.itextos.beacon.http.interfaceutil.log.HttpInterfaceLog;
import com.itextos.beacon.inmemory.encryptinfo.CustomerEncryptUtil;
import com.itextos.beacon.inmemory.encryptinfo.EncryptInfo;
import com.itextos.beacon.inmemory.interfaces.util.IInterfaceUtil;

public class MessageValidater
{

    private static final Log       log = LogFactory.getLog(MessageValidater.class);

    private final BasicInfo        mBasicInfo;
    private final InterfaceMessage mInterfaceMessage;

    private StringBuffer sb;
    public MessageValidater(
            InterfaceMessage aInterfaceMessage,
            BasicInfo aBasicInfo,
            StringBuffer sb)
    {
        mInterfaceMessage = aInterfaceMessage;
        mBasicInfo        = aBasicInfo;
        this.sb=sb;
        
        this.sb.append("MessageValidater").append("\n");
        
    }

    public InterfaceStatusCode validate()
    {
        sb.append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()); 

        final InterfaceStatusCode    lClientStatusCode = validateMessageRequest();
        final InterfaceRequestStatus lRequestStatus    = new InterfaceRequestStatus(lClientStatusCode, null);

        if (log.isDebugEnabled())
            log.debug("Message object status after validation:  '" + lRequestStatus + "'");

        mInterfaceMessage.setRequestStatus(lRequestStatus);
        return lClientStatusCode;
    }

    private InterfaceStatusCode validateMessageRequest()
    {

    	
        sb.append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()); 

        try
        {
            final String lHeader          = CommonUtility.nullCheck(mInterfaceMessage.getHeader());
            final String lUrlTrack        = CommonUtility.nullCheck(mInterfaceMessage.getUrlTrack());
            final String lDestinationPort = CommonUtility.nullCheck(mInterfaceMessage.getDestinationPort());
            final String lMsgExpiry       = CommonUtility.nullCheck(mInterfaceMessage.getExpiry());
            final String lAppCountry      = CommonUtility.nullCheck(mInterfaceMessage.getAppendCountry());
            final String lAppCountryCode  = CommonUtility.nullCheck(mInterfaceMessage.getCountryCode());
            final String lCustRef         = CommonUtility.nullCheck(mInterfaceMessage.getCustRef());
            final String lDcs             = CommonUtility.nullCheck(mInterfaceMessage.getDcs());
            final String lDltTemplateId   = CommonUtility.nullCheck(mInterfaceMessage.getDltTemplateId(), true);
            final String lDltEntityId     = CommonUtility.nullCheck(mInterfaceMessage.getDltEntityId(), true);
            // final String lUdhi = CommonUtility.nullCheck(mMessage.getUdhi());

            boolean      isMsgIdenAllow   = CommonUtility.isEnabled(CommonUtility.nullCheck(mBasicInfo.getUserAccountInfo().get(MiddlewareConstant.MW_UC_IDEN_ALLOW.getName())));

            if (log.isDebugEnabled())
                log.debug("Acc UC Identification Allow : " + isMsgIdenAllow);

            final boolean isReqHexMsg = CommonUtility.isEnabled(CommonUtility.nullCheck(mBasicInfo.getUserAccountInfo().get(MiddlewareConstant.MW_REQ_HEX_MSG.getName())));

            if (log.isDebugEnabled())
                log.debug("Acc Request Hex Message : " + isReqHexMsg);

            if (isReqHexMsg)
            {
                mInterfaceMessage.setMegTypeHex(InterfaceMessageClass.UNICODE_HEX.getKey());
                isMsgIdenAllow = false;
            }

            InterfaceStatusCode lSc = validateMessage(isMsgIdenAllow);

            if (lSc != InterfaceStatusCode.SUCCESS)
                return lSc;

            lSc = validateHeader(lHeader);
            if (lSc != InterfaceStatusCode.SUCCESS)
                return lSc;

            if (!isMsgIdenAllow)
            {
                lSc = validateMsgType(mInterfaceMessage.getMsgType(), lDcs);
                if (lSc != InterfaceStatusCode.SUCCESS)
                    return lSc;
            }

            lSc = validateDestinationPort(lDestinationPort, mInterfaceMessage.getMsgType());
            if (lSc != InterfaceStatusCode.SUCCESS)
                return lSc;

            lSc = validateMessageExpiry(lMsgExpiry);
            if (lSc != InterfaceStatusCode.SUCCESS)
                return lSc;

            lSc = validateAppCountry(lAppCountry);
            if (lSc != InterfaceStatusCode.SUCCESS)
                return lSc;

            lSc = validateCountryCode(lAppCountry, lAppCountryCode);
            if (lSc != InterfaceStatusCode.SUCCESS)
                return lSc;

            lSc = validateVLinkMessage(lUrlTrack);
            if (lSc != InterfaceStatusCode.SUCCESS)
                return lSc;

            lSc = validateCustomerRefrenceNumber(lCustRef);
            if (lSc != InterfaceStatusCode.SUCCESS)
                return lSc;

            lSc = validateDltTemplateId(lDltTemplateId);
            if (lSc != InterfaceStatusCode.SUCCESS)
                return lSc;

            lSc = validateDltEntityId(lDltEntityId);
            if (lSc != InterfaceStatusCode.SUCCESS)
                return lSc;
        }
        catch (final Exception e)
        {
            log.error("Exception occured while message object validation ", e);
            return InterfaceStatusCode.INTERNAL_SERVER_ERROR;
        }
        return InterfaceStatusCode.SUCCESS;
    }

    private  InterfaceStatusCode validateDltEntityId(
            String aDltEntityId)
    {
        sb.append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()); 

        if (!aDltEntityId.isEmpty())
            if (!validateNumaricAndLength(aDltEntityId))
                return InterfaceStatusCode.INVALID_DLT_ENTITY_ID;

        return InterfaceStatusCode.SUCCESS;
    }

    private  InterfaceStatusCode validateDltTemplateId(
            String aDltTemplateId)
    {
        sb.append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()); 

        if (!aDltTemplateId.isEmpty())
            if (!validateNumaricAndLength(aDltTemplateId))
                return InterfaceStatusCode.INVALID_DLT_TEMPLATE_ID;

        return InterfaceStatusCode.SUCCESS;
    }

    private static boolean validateNumaricAndLength(
            String aValue)
    {
        final int lMinValue = Utility.getConfigParamsValueAsInt(ConfigParamConstants.DLT_PARAM_MIN_LENGTH);
        final int lMaxValue = Utility.getConfigParamsValueAsInt(ConfigParamConstants.DLT_PARAM_MAX_LENGTH);

        if (Utility.isNumaric(aValue) && ((aValue.length() >= lMinValue) && (aValue.length() <= lMaxValue)))
            return true;

        return false;
    }

    private static String[] getTemplateValuesUnicodeHex(
            String[] aTemplateValues)
    {
        final int      lIntLength         = aTemplateValues.length;
        final String[] lHexTemplateValues = new String[lIntLength];

        for (int i = 0; i < lIntLength; i++)
        {
            String lValue = aTemplateValues[i];
            lValue                = Utility.processUnicodeMessage(lValue);
            lHexTemplateValues[i] = lValue;
        }
        return lHexTemplateValues;
    }

    public InterfaceStatusCode validateDest(
            String aDestination,StringBuffer sb)
    {
        String lMobileNumber = CommonUtility.nullCheck(aDestination, true);
        
        sb.append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()); 

         
        HttpInterfaceLog.getInstance(mBasicInfo.getClientId()).log(mBasicInfo.getClientId(), mBasicInfo.getFileId()+ " :  validateDest :() aDestination : "+aDestination);

        if (lMobileNumber.isBlank())
        {
            if (log.isDebugEnabled())
                log.debug("Destination is empty:  '" + lMobileNumber + "'");
            sb.append("Destination is empty:  '" + lMobileNumber + "' InterfaceStatusCode.DESTINATION_EMPTY :"+ InterfaceStatusCode.DESTINATION_EMPTY).append("\n");
            
            HttpInterfaceLog.getInstance(mBasicInfo.getClientId()).log(mBasicInfo.getClientId(), mBasicInfo.getFileId()+ " : InterfaceStatusCode.DESTINATION_EMPTY : "+aDestination);


            return InterfaceStatusCode.DESTINATION_EMPTY;
        }

        try
        {
            lMobileNumber = decryptString(lMobileNumber);
        }
        catch (final Exception e1)
        {
            
            sb.append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append(" exception got"); 

            sb.append("Exception occured while decrypting destination...InterfaceStatusCode.DESTINATION_INVALID : "+InterfaceStatusCode.DESTINATION_INVALID).append("\t").append(ErrorMessage.getStackTraceAsString(e1)).append("\n");
            		
            log.error("Exception occured while decrypting destination...", e1);
            
            HttpInterfaceLog.getInstance(mBasicInfo.getClientId()).log(mBasicInfo.getClientId(), mBasicInfo.getFileId()+ " : Exception occured while decrypting destination...InterfaceStatusCode.DESTINATION_INVALID : "+InterfaceStatusCode.DESTINATION_INVALID);

            
           return InterfaceStatusCode.DESTINATION_INVALID;
        }

        /*
         * final String lAccDefaultCountryCD =
         * CommonUtility.nullCheck(mBasicInfo.getUserAccountInfo().get(
         * MiddlewareConstant.MW_DEFAULT_COUNTRY_CODE.getName()), true);
         * if (log.isDebugEnabled())
         * log.debug("Account Default Country Table : " + lAccDefaultCountryCD);
         */
        final String lCountryCD = InterfaceUtil.getDefaultCountryCode();

        if (log.isDebugEnabled())
            log.debug("Default Contury From Config Values : " + lCountryCD);
        
        
        HttpInterfaceLog.getInstance(mBasicInfo.getClientId()).log(mBasicInfo.getClientId(), mBasicInfo.getFileId()+ " : Default Country From Config Values : " + lCountryCD);


        // lCountryCD = (!lAccDefaultCountryCD.isEmpty()) ? lAccDefaultCountryCD :
        // lCountryCD;

        if (log.isDebugEnabled())
            log.debug("Validate Country Code :" + lCountryCD);

        HttpInterfaceLog.getInstance(mBasicInfo.getClientId()).log(mBasicInfo.getClientId(), mBasicInfo.getFileId()+" : Validate Country Code : " + lCountryCD);

        sb.append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("Validate Country Code :" + lCountryCD); 

        
        final boolean               isConsiderDefaultLengthAsDomestic = CommonUtility
                .isEnabled(CommonUtility.nullCheck(mBasicInfo.getUserAccountInfo().get(MiddlewareConstant.MW_CONSIDER_DEFAULTLENGTH_AS_DOMESTIC.getName()), true));
        
        HttpInterfaceLog.getInstance(mBasicInfo.getClientId()).log(mBasicInfo.getClientId(), mBasicInfo.getFileId()+" : isConsiderDefaultLengthAsDomestic : " + isConsiderDefaultLengthAsDomestic);


        final boolean               isDomesticSpecialSeriesAllow      = CommonUtility
                .isEnabled(CommonUtility.nullCheck(mBasicInfo.getUserAccountInfo().get(MiddlewareConstant.MW_DOMESTIC_SPECIAL_SERIES_ALLOW.getName()), true));

        
        HttpInterfaceLog.getInstance(mBasicInfo.getClientId()).log(mBasicInfo.getClientId(), mBasicInfo.getFileId()+" : isDomesticSpecialSeriesAllow : " + isDomesticSpecialSeriesAllow);
        

        final boolean               isIntlServiceAllow                = InterfaceUtil.isIntlServiceAllow(mBasicInfo.getUserAccountInfo());
        
        
        HttpInterfaceLog.getInstance(mBasicInfo.getClientId()).log(mBasicInfo.getClientId(), mBasicInfo.getFileId()+" : isIntlServiceAllow : " + isIntlServiceAllow);

        
        final boolean               isAppendCountryCode               = CommonUtility.isEnabled(mInterfaceMessage.getAppendCountry());
        final String                aAppendCountryCode                = CommonUtility.nullCheck(mInterfaceMessage.getCountryCode(), true);

        final MobileNumberValidator lMobileValidator                  = InterfaceUtil.validateMobile(lMobileNumber, lCountryCD, isIntlServiceAllow, isConsiderDefaultLengthAsDomestic,
                isAppendCountryCode, aAppendCountryCode, isDomesticSpecialSeriesAllow);

        
        HttpInterfaceLog.getInstance(mBasicInfo.getClientId()).log(mBasicInfo.getClientId(), mBasicInfo.getFileId()+" : AccountMobileInfo : " + lMobileValidator.getmAccountMobileInfo());

        
        try
        {
            if (log.isDebugEnabled())
                log.debug("Mobile Number Validation Details :" + lMobileValidator.toString());
            
            sb.append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("Mobile Number Validation Details :" + lMobileValidator.toString()); 


            final boolean isValidMobileNumber = lMobileValidator.isValidMobileNumber();

            sb.append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("isValidMobileNumber :" + isValidMobileNumber); 

            mInterfaceMessage.setMobileNumber(lMobileValidator.getMobileNumber());

            if (isValidMobileNumber)
            {
                if (log.isDebugEnabled())
                    log.debug("The given number  " + mInterfaceMessage.getMobileNumber() + " is International Number ? " + lMobileValidator.isIntlMobileNumber());
                RouteType lRouteType = null;

                if (lMobileValidator.isIntlMobileNumber())
                {
                    if (isIntlServiceAllow)
                        lRouteType = RouteType.INTERNATIONAL;
                    else
                        // Reject as INTL Serivce not available.
                        return InterfaceStatusCode.INTL_SERVICE_DISABLED;
                }
                else
                {
                    lRouteType = RouteType.DOMESTIC;

                    if (lMobileValidator.isSpecialSeriesNumber())
                        // set special Series domestic info
                        mInterfaceMessage.setIsSpecialSeriesNumber(lMobileValidator.isSpecialSeriesNumber() ? "1" : "0");
                }

                mInterfaceMessage.setRouteType(lRouteType);
            }
            else
                return InterfaceStatusCode.DESTINATION_INVALID;
        }
        catch (final Exception e)
        {
            if (log.isDebugEnabled())
                log.debug("exception while parsing number as long  :  '" + lMobileNumber + "'", e);

            sb.append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("exception while parsing number as long  :  '" + lMobileNumber + "'"+ ErrorMessage.getStackTraceAsString(e)); 

            return InterfaceStatusCode.DESTINATION_INVALID;
        }

        return InterfaceStatusCode.SUCCESS;
    }

    public InterfaceStatusCode validateTraiBlockOut(
            Date aScheduleTime)
    {
        final JSONObject    lUserDetails  = mBasicInfo.getUserAccountInfo();
        InterfaceStatusCode lClientStatus = InterfaceStatusCode.SUCCESS;

        if (log.isDebugEnabled())
            log.debug("Before Trai Blockout time :  '" + aScheduleTime + "  '");

        if (Utility.isPromotionalMessage((String) lUserDetails.get(MiddlewareConstant.MW_MSG_TYPE.getName())))
        {
            final RouteType lRouteType = mInterfaceMessage.getRouteType();

            if (log.isDebugEnabled())
                log.debug("user message type :  'promotional' Route Type " + lRouteType);

            if (lRouteType == RouteType.DOMESTIC)
            {
                if (log.isDebugEnabled())
                    log.debug("The number belongs to Domastic series. ");

                lClientStatus = TraiBlockoutCheck.isValidTraiBlockOut(aScheduleTime, mBasicInfo);

                if (log.isDebugEnabled())
                    log.debug("After validate trai blockout status :  '" + lClientStatus + " ' and time :'" + aScheduleTime + "'");
            }
            else
            {
                if (log.isDebugEnabled())
                    log.debug("The number is internartional series  '");

                lClientStatus = InterfaceStatusCode.SUCCESS;
            }
        }

        return lClientStatus;
    }

    private String decryptString(
            String aEncryptString)
            throws Exception
    {
        if (log.isDebugEnabled())
            log.debug("Encrypt String :" + aEncryptString);
        
        
        sb.append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()); 

        final String lEncryptedStr = CommonUtility.nullCheck(mBasicInfo.getEncrypt());

        sb.append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append(" lEncryptedStr : "+lEncryptedStr); 

        if ("1".equals(lEncryptedStr)) {
            try
            {
                final String      lClientId    = mBasicInfo.getClientId();
                final EncryptInfo lEncryptInfo = Utility.getEncryptInfo(lClientId);

                if (lEncryptInfo == null)
                    throw new ItextosRuntimeException("Encryption Info is not available for client : " + lClientId);

                final String lDecryptedStr = CustomerEncryptUtil.decryptIncomingString(lClientId, aEncryptString);

                if (log.isDebugEnabled())
                    log.debug("After decrypt the string for client '" + lClientId + "' :: String: " + lDecryptedStr);

                return lDecryptedStr;
            }
            catch (final Exception e)
            {
                log.error("Exception occured while encryption ");
                throw e;
            }
        }
        
        sb.append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append(" finished"); 

        return aEncryptString;
    }

    private InterfaceStatusCode validateMessage(
            boolean isMsgIdenAllow)
    {
    	
        sb.append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()); 

        final String lTemplateId = CommonUtility.nullCheck(mInterfaceMessage.getTemplateId(), true);

        if (lTemplateId.isBlank())
            return processNonTemplateMessage(isMsgIdenAllow);

        return processTemplateMessage(lTemplateId);
    }

    private InterfaceStatusCode processTemplateMessage(
            String aTemplateId)
    {
        if (log.isDebugEnabled())
            log.debug("Template Id: '" + aTemplateId + "'");

        sb.append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()); 

        final String[] lTemplateValues = mInterfaceMessage.getTemplateValues();
        final String   lTemplate       = IInterfaceUtil.getInterfaceSMSTeamplate((String) mBasicInfo.getUserAccountInfo().get(MiddlewareConstant.MW_CLIENT_ID.getName()), aTemplateId);

        if (log.isDebugEnabled())
            log.debug("template from db:  '" + lTemplate + "'");

        if (lTemplate == null) {
            sb.append("INVALID_TEMPLATEID" ).append("\n");

            return InterfaceStatusCode.INVALID_TEMPLATEID;
        }

        if (lTemplateValues == null)
        {

            sb.append("TEMPLATE_VALUES_EMPTY" ).append("\n");

            return InterfaceStatusCode.TEMPLATE_VALUES_EMPTY;
        }

        String[]     lHexTemplateValues = null;

        final String lMsgTypeHex        = CommonUtility.nullCheck(mInterfaceMessage.getMegTypeHex(), true);

        if ((!InterfaceMessageClass.UNICODE_HEX.getMessageType().equalsIgnoreCase(lMsgTypeHex)) && mInterfaceMessage.getMsgType().equalsIgnoreCase(InterfaceMessageClass.UNICODE.getMessageType()))
        {
            if (log.isDebugEnabled())
                log.debug("Message Type is unicode:  '" + mInterfaceMessage.getMsgType() + "'");

            lHexTemplateValues = getTemplateValuesUnicodeHex(lTemplateValues);

            if (log.isDebugEnabled())
                log.debug("template values :  '" + lHexTemplateValues + "'");
        }
        else
            lHexTemplateValues = lTemplateValues;

        final String lMessage = MessageFormat.format(lTemplate, lHexTemplateValues);

        if (log.isDebugEnabled())
            log.debug("After replace Template Message : " + lMessage);

        mInterfaceMessage.setMessage(lMessage);
        return InterfaceStatusCode.SUCCESS;
    }

    private InterfaceStatusCode processNonTemplateMessage(
            boolean isMsgIdenAllow)
    {
    	
        sb.append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()); 

        String lMessage = CommonUtility.nullCheck(mInterfaceMessage.getMessage());

        if (lMessage.isBlank())
        {

            sb.append("Message Empty :  '" + lMessage + "'").append("\n");
            return InterfaceStatusCode.MESSAGE_EMPTY;
        }

        try
        {
            lMessage = decryptString(lMessage);
        }
        catch (final Exception e)
        {
            sb.append("Exception occured while decrypting message").append("\t").append(ErrorMessage.getStackTraceAsString(e)).append("\n");

            return InterfaceStatusCode.MESSAGE_EMPTY;
        }

        if (isMsgIdenAllow)
        {
            final boolean isMsgUC = Utility.isMessageContainsUnicode(lMessage, mInterfaceMessage, mBasicInfo);
            if (log.isDebugEnabled())
                log.debug("Is the request Message is Unicode : " + isMsgUC + " :: Message : " + mInterfaceMessage.getMessage());

            lMessage = mInterfaceMessage.getMessage();

            if (isMsgUC)
                /*
                 * if (log.isDebugEnabled())
                 * lMessage = Utility.processUnicodeMessage(mInterfaceMessage.getMessage());
                 */
                mInterfaceMessage.setMsgType(InterfaceMessageClass.UNICODE.getKey());
            else
                mInterfaceMessage.setMsgType(InterfaceMessageClass.PLAIN.getKey());
        }

        final String lMsgType = CommonUtility.nullCheck(mInterfaceMessage.getMsgType(), true);
        if (log.isDebugEnabled())
            log.debug("Message Type is unicode : '" + lMsgType + "'");

        final String lMsgTypeHex = CommonUtility.nullCheck(mInterfaceMessage.getMegTypeHex(), true);

        if (log.isDebugEnabled())
            log.debug("Message Type Hex is unicode : '" + lMsgTypeHex + "'");

        if ((!lMsgTypeHex.equalsIgnoreCase(InterfaceMessageClass.UNICODE_HEX.getMessageType()))
                && (lMsgType.equalsIgnoreCase(InterfaceMessageClass.UNICODE.getMessageType()) || lMsgType.equalsIgnoreCase(InterfaceMessageClass.FLASH_UNICODE.getMessageType())))
        {
            if (log.isDebugEnabled())
                log.debug("Message contains VL ? : '" + (mInterfaceMessage.getMessage().toUpperCase().contains("[~VL:")) + "'");

            if (mInterfaceMessage.getMessage().toUpperCase().contains("[~VL:"))
                lMessage = Utility.convertStringAloneWithoutVLIntoHex(mInterfaceMessage.getMessage());
            else
            {
                if (log.isDebugEnabled())
                    log.debug("Ingnore the conversion when MsgIdenAllow case ..............");
                if (!isMsgIdenAllow)
                    lMessage = Utility.processUnicodeMessage(lMessage);
            }
        }
        /*
         * else
         * if
         * (lMsgType.equalsIgnoreCase(InterfaceMessageClass.UNICODE_HEX.getMessageType()
         * ))
         * {}
         */

        mInterfaceMessage.setMessage(lMessage);
        return InterfaceStatusCode.SUCCESS;
    }

    private  InterfaceStatusCode validateHeader(
            String aHeader)
    {
        if (log.isDebugEnabled())
            log.debug("Sender Id: '" + aHeader + "'");

        sb.append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()); 

        if (aHeader.isBlank())
            return InterfaceStatusCode.SENDER_ID_EMPTY;

        if (aHeader.length() > 15)
            return InterfaceStatusCode.INVALID_SENDERID;

        return InterfaceStatusCode.SUCCESS;
    }

    private  InterfaceStatusCode validateMessageExpiry(
            String aMsgExpiry)
    {
    	
        sb.append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()); 


        if (!aMsgExpiry.isBlank())
        {
            final int lIntMsgExpiry = CommonUtility.getInteger(aMsgExpiry, 0);

            if (lIntMsgExpiry <= 0)
            {
                if (log.isDebugEnabled())
                    log.debug("Invalid validity period while parsing vp as integer:  '" + aMsgExpiry + "'");
                return InterfaceStatusCode.EXPIRY_MINUTES_INVALID;
            }

            if ((lIntMsgExpiry < APIConstants.VP_MIN_VALUE) || (lIntMsgExpiry > APIConstants.VP_MAX_VALUE))
            {
                if (log.isDebugEnabled())
                    log.debug("Invalid validity period :  '" + aMsgExpiry + "'");

                return InterfaceStatusCode.EXPIRY_MINUTES_BEYOUND_TIME_BOUNDRY;
            }
        }
        return InterfaceStatusCode.SUCCESS;
    }

    private  InterfaceStatusCode validateMsgType(
            String aMsgType,
            String aDcs)
    {
        aMsgType = CommonUtility.nullCheck(aMsgType, true);

        sb.append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("aMsgType : "+aMsgType); 

        if (aMsgType.isBlank())
            return InterfaceStatusCode.INVALID_MSGTYPE;

        if (!aMsgType.isBlank() && (aMsgType).equalsIgnoreCase(InterfaceMessageClass.ADVANCE.getMessageType()))
            if (aDcs.isBlank())
            {
                if (log.isDebugEnabled())
                    log.debug("empty dcs:  '" + aDcs + "'");
                return InterfaceStatusCode.DCS_INVALID;
            }

        if (log.isDebugEnabled())
            log.debug("Message Type : '" + aMsgType + "'  MsgType Length: '" + aMsgType.length() + "'");

        if (!aMsgType.isBlank())
        {
            final InterfaceStatusCode msgTypeStatus = validateMessageType(aMsgType);

            if (msgTypeStatus != InterfaceStatusCode.SUCCESS)
            {
                if (log.isDebugEnabled())
                    log.debug("message type is not valid: '" + aMsgType + "'");

                return InterfaceStatusCode.INVALID_MSGTYPE;
            }
        }

        return InterfaceStatusCode.SUCCESS;
    }

    private  InterfaceStatusCode validateAppCountry(
            String aAppCountry)
    {
        sb.append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()); 

        if (!aAppCountry.isBlank() && (!(aAppCountry.equals("1")) && !(aAppCountry.equals("0"))))
        {
            if (log.isDebugEnabled())
                log.debug("append country option is invalid:  '" + aAppCountry + "'");
            
           sb.append("append country option is invalid:  '" + aAppCountry + "'  InterfaceStatusCode.COUNTRY_CODE_INVALID_APPEND").append(InterfaceStatusCode.COUNTRY_CODE_INVALID_APPEND).append("\n");

            return InterfaceStatusCode.COUNTRY_CODE_INVALID_APPEND;
        }
        return InterfaceStatusCode.SUCCESS;
    }

    private  InterfaceStatusCode validateCountryCode(
            String aAppCountry,
            String aCountryCode)
    {
        final boolean isAppendCountryCode = CommonUtility.isEnabled(aAppCountry);
        final String  lCountryCode        = CommonUtility.nullCheck(aCountryCode, true);
        sb.append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()); 

        if (isAppendCountryCode && lCountryCode.isBlank())
        {
            if (log.isDebugEnabled())
                log.debug("append country code is invalid:  '" + lCountryCode + "'");

            return InterfaceStatusCode.INVALID_COUNTRY_CODE;
        }
        return InterfaceStatusCode.SUCCESS;
    }

    private  InterfaceStatusCode validateDestinationPort(
            String aDestinationPort,
            String aMsgType)
    {
        if (log.isDebugEnabled())
            log.debug("Special Port : " + aDestinationPort);

        sb.append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()); 

        if (!aDestinationPort.isBlank() && (CommonUtility.getInteger(aDestinationPort) != 0))
        {
            final int lIntPort = CommonUtility.getInteger(aDestinationPort, 0);

            if ((lIntPort <= 0) || ((lIntPort <= APIConstants.PORT_MIN_VALUE) || (lIntPort > APIConstants.PORT_MAX_VALUE)))
            {
                if (log.isDebugEnabled())
                    log.debug("port is not valid:  '" + lIntPort + "'");

                return InterfaceStatusCode.PORT_INVALID;
            }
        }
        else
            if ((InterfaceMessageClass.SPECIFIC_PORT.getMessageType().equalsIgnoreCase(aMsgType)) || (InterfaceMessageClass.SPECIFIC_PORT_UNICODE.getMessageType().equalsIgnoreCase(aMsgType)))
                return InterfaceStatusCode.PORT_INVALID;

        return InterfaceStatusCode.SUCCESS;
    }

    private  InterfaceStatusCode validateVLinkMessage(
            String aUrlTrack)
    {

        sb.append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()); 

        if (!aUrlTrack.isBlank() && (!(aUrlTrack.equals("1")) && !(aUrlTrack.equals("0"))))
        {
            if (log.isDebugEnabled())
                log.debug("url track option is invalid:  '" + aUrlTrack + "'");

            return InterfaceStatusCode.URLTRACK_INVALID_OPTION;
        }
        return InterfaceStatusCode.SUCCESS;
    }

    private  InterfaceStatusCode validateCustomerRefrenceNumber(
            String aCustomerRefrenceNum)
    {

        sb.append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()); 

        if (!aCustomerRefrenceNum.isBlank() && (aCustomerRefrenceNum.length() > APIConstants.CUST_REF_MAX_VALUE))
        {
            if (log.isDebugEnabled())
                log.debug("Customer Refrence Number is greate than max value:  '" + aCustomerRefrenceNum + "'");

            return InterfaceStatusCode.CUST_REFERENCE_ID_INVALID_LENGTH;
        }
        return InterfaceStatusCode.SUCCESS;
    }

    private  InterfaceStatusCode validateMessageType(
            String aMsgType)
    {
        sb.append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("aMsgType : "+aMsgType); 

        final InterfaceMessageClass lMessageType = InterfaceMessageClass.getMessageType(aMsgType);

        if (log.isDebugEnabled())
            log.debug("Validate message type  '" + aMsgType + "' and Message Type Value '" + lMessageType + "'");

        if (lMessageType == null)
            return InterfaceStatusCode.INVALID_MSGTYPE;

        return InterfaceStatusCode.SUCCESS;
    }

}
