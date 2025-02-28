package com.itextos.beacon.platform.msgtool.util;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.MessageClass;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.MessageConvertionUtility;
import com.itextos.beacon.inmemdata.account.ClientAccountDetails;
import com.itextos.beacon.inmemdata.account.UserInfo;
import com.itextos.beacon.inmemory.configvalues.ApplicationConfiguration;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.platform.messagetool.FeatureCodeFinder;
import com.itextos.beacon.platform.messagetool.MessageSplitRequest;
import com.itextos.beacon.platform.messagetool.Request;
import com.itextos.beacon.platform.messagetool.Response;
import com.itextos.beacon.platform.messagetool.UcIdentifier;

public class MsgProcessUtil
{

    private static final Log log = LogFactory.getLog(MsgProcessUtil.class);

    private MsgProcessUtil()
    {}

    public static String requestProcess(
            HttpServletRequest aRequest)
    {
        String lResp;

        if (log.isDebugEnabled())
            log.debug("Received Request String :: " + aRequest);

        try
        {
            final String              lClientId            = aRequest.getParameter("cli_id");
            final String              lMessage             = CommonUtility.nullCheck(aRequest.getParameter("message"), true);
            final String              lHeader              = CommonUtility.nullCheck(aRequest.getParameter("header"));

            final MessageSplitRequest lMessageSplitRequest = papulateMsgSplitReq(lClientId, lHeader, lMessage);
            lResp = getMessageInfo(lMessageSplitRequest);
        }
        catch (final Exception e)
        {
            lResp = genarateFailResponse();
        }

        if (log.isDebugEnabled())
            log.debug("Final Response : " + lResp);

        return lResp;
    }

    public static String requestProcess(
            JSONObject aJsonReq)
    {
        String lResp;

        if (log.isDebugEnabled())
            log.debug("Received Request String :: " + aJsonReq);

        try
        {
            final String              lClientId            = CommonUtility.nullCheck(aJsonReq.get("cli_id"), true);
            final String              lHeader              = CommonUtility.nullCheck(aJsonReq.get("header"));
            final String              lMessage             = CommonUtility.nullCheck(aJsonReq.get("message"), true);

            final MessageSplitRequest lMessageSplitRequest = papulateMsgSplitReq(lClientId, lHeader, lMessage);
            lResp = getMessageInfo(lMessageSplitRequest);
        }
        catch (final Exception e)
        {
            lResp = genarateFailResponse();
        }

        if (log.isDebugEnabled())
            log.debug("Final Response : " + lResp);

        return lResp;
    }

    private static MessageSplitRequest papulateMsgSplitReq(
            String aClientId,
            String aHeader,
            String aMessage)
    {
        final UserInfo   lUserInfo                   = ClientAccountDetails.getUserDetailsByClientId(aClientId);
        final String     lAccountInfo                = lUserInfo.getAccountDetails();
        final JSONObject lJsonObject                 = parseJSON(lAccountInfo);

        final int        lAccSplCharLength           = CommonUtility.getInteger(CommonUtility.nullCheck(lJsonObject.get(MiddlewareConstant.MW_UC_IDEN_CHAR_LEN.getName())));
        final int        lAccountLevelOccuranceCount = CommonUtility.getInteger(CommonUtility.nullCheck(lJsonObject.get(MiddlewareConstant.MW_UC_IDEN_OCCUR.getName())));
        final boolean    lRemoveUcCharsInMessage     = CommonUtility.isEnabled(CommonUtility.nullCheck(lJsonObject.get(MiddlewareConstant.MW_IS_REMOVE_UC_CHARS.getName())));
        final boolean    lIs16BitUdh                 = CommonUtility.isEnabled(CommonUtility.nullCheck(lJsonObject.get(MiddlewareConstant.MW_IS_16BIT_UDH.getName())));
        String           lCountry                    = CommonUtility.nullCheck(getAppConfigValueAsString(ConfigParamConstants.DEFAULT_COUNTRY_CODE), true);
        if (lCountry.isEmpty())
            lCountry = "IND";

        final Request lRequestObj = new Request(aClientId, aMessage, lAccSplCharLength, lAccountLevelOccuranceCount, lRemoveUcCharsInMessage);

        if (log.isDebugEnabled())
            log.debug("Request Object : " + lRequestObj.toString());

        final Response lMessageInfo = UcIdentifier.checkForUnicode(lRequestObj);

        if (log.isDebugEnabled())
            log.debug("Unicode Check Response Object : " + lMessageInfo.toString());

        String        lTempMessage  = aMessage;

        final boolean isUnicode     = lMessageInfo.isIsUniCode();
        MessageClass  lMessageClass = MessageClass.PLAIN_MESSAGE;

        if (isUnicode)
        {
            lTempMessage  = MessageConvertionUtility.convertString2HexString(aMessage);
            lMessageClass = MessageClass.UNICODE_MESSAGE;

            if (log.isDebugEnabled())
                log.debug("Converted Message : " + lTempMessage);
        }

        final MessageSplitRequest lMessageSplitRequest = new MessageSplitRequest(aClientId, "", lTempMessage, lMessageClass.getKey(), isUnicode);
        lMessageSplitRequest.setIs16BitUdh(lIs16BitUdh);
        lMessageSplitRequest.setHeader(aHeader);
        lMessageSplitRequest.setCountry(lCountry);

        return lMessageSplitRequest;
    }

    private static String getMessageInfo(
            MessageSplitRequest aMsgSplitReq)
    {
        aMsgSplitReq.setClientMaxSplit(-1);
        aMsgSplitReq.setDcs(-1);
        aMsgSplitReq.setDestinationPort(-1);
        aMsgSplitReq.setDltEnabled(CommonUtility.isEnabled(getAppConfigValueAsString(ConfigParamConstants.DLT_ENABLE)));
        aMsgSplitReq.setDltTemplateType(null);

        final FeatureCodeFinder fcf                  = new FeatureCodeFinder(aMsgSplitReq);
        final List<String>      lSplitMessageProcess = fcf.splitMessageProcess();

        return genarateResponse(aMsgSplitReq);
    }

    private static String genarateResponse(
            MessageSplitRequest aMsgSplitReq)
    {
        final JSONObject lJsonObject = new JSONObject();
        lJsonObject.put(ResponseConstantKey.IS_UNICODE, aMsgSplitReq.isHexMessage());
        lJsonObject.put(ResponseConstantKey.MSG_LENGTH, aMsgSplitReq.getCharactersCount());
        lJsonObject.put(ResponseConstantKey.TOTAL_PARTS, aMsgSplitReq.getTotalSplitParts());
        lJsonObject.put(ResponseConstantKey.MESSAGE, aMsgSplitReq.getMessage());

        return lJsonObject.toJSONString();
    }

    private static String genarateFailResponse()
    {
        final JSONObject lJsonObject = new JSONObject();
        lJsonObject.put(ResponseConstantKey.ERROR, "Internal Error");

        return lJsonObject.toJSONString();
    }

    private static JSONObject parseJSON(
            String aJsonString)
    {

        try
        {
            return (JSONObject) new JSONParser().parse(aJsonString);
        }
        catch (final ParseException e)
        {
            return null;
        }
    }

    private static String getAppConfigValueAsString(
            ConfigParamConstants aConfigParamConstant)
    {
        final ApplicationConfiguration lAppConfiguration = (ApplicationConfiguration) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.APPLICATION_CONFIG);
        return lAppConfiguration.getConfigValue(aConfigParamConstant.getKey());
    }

}
