package com.itextos.beacon.http.generichttpapi.common.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.itextos.beacon.commonlib.constants.AccountStatus;
import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.CustomFeatures;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.InterfaceStatusCode;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.messageidentifier.MessageIdentifier;
import com.itextos.beacon.commonlib.timezoneutility.TimeZoneUtility;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.commonlib.utility.MessageConvertionUtility;
import com.itextos.beacon.http.generichttpapi.common.data.BasicInfo;
import com.itextos.beacon.http.generichttpapi.common.data.InterfaceMessage;
import com.itextos.beacon.http.generichttpapi.common.data.InterfaceRequestStatus;
import com.itextos.beacon.http.generichttpapi.common.data.response.ObjectFactory;
import com.itextos.beacon.http.generichttpapi.common.data.response.ResponseObject;
import com.itextos.beacon.http.generichttpapi.common.data.response.Root;
import com.itextos.beacon.http.generichttpapi.common.data.response.Root.Status;
import com.itextos.beacon.inmemdata.account.ClientAccountDetails;
import com.itextos.beacon.inmemdata.account.UserInfo;
import com.itextos.beacon.inmemory.configvalues.ApplicationConfiguration;
import com.itextos.beacon.inmemory.customfeatures.InmemCustomFeatures;
import com.itextos.beacon.inmemory.encryptinfo.EncryptInfo;
import com.itextos.beacon.inmemory.encryptinfo.EncryptInfoCollection;
import com.itextos.beacon.inmemory.inmemdata.country.CountryInfo;
import com.itextos.beacon.inmemory.inmemdata.country.CountryInfoCollection;
import com.itextos.beacon.inmemory.interfaces.bean.InterfaceResponse;
import com.itextos.beacon.inmemory.interfaces.bean.InterfaceResponseCodeMapping;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.platform.messagetool.MsgIdentifierUtil;
import com.itextos.beacon.platform.messagetool.Response;

public class Utility
{

    private static final Log    log             = LogFactory.getLog(Utility.class);
    private static final String VL_START_STRING = "[~VL:";
    private static final String VL_END_STRING   = "~]";

    private static final String REQ_ID          = "$msg_id$";
    private static final String REQ_TIME        = "$req_time$";
    private static final String STATUS_CODE     = "$sts_code$";
    private static final String STATUS_INFO     = "$sts_info$";
    private static final String STATUS_REASON   = "$sts_reason$";

    private Utility()
    {}

    public static String getRequestFromBody(
            HttpServletRequest aRequest,
            String aParamterName)
            throws Exception
    {
        String reqString = aRequest.getParameter(aParamterName);
        reqString = CommonUtility.nullCheck(reqString, true);

        if ("".equals(reqString))
            reqString = getRequestFromBody(aRequest);
        return reqString;
    }

    public static String getRequestFromBody(
            HttpServletRequest aRequest)
            throws Exception
    {

        try (
                BufferedReader br = new BufferedReader(new InputStreamReader(aRequest.getInputStream()));)
        {
            int                 bytesRead  = -1;
            final char[]        charBuffer = new char[1024];

            final StringBuilder sb         = new StringBuilder();
            while ((bytesRead = br.read(charBuffer)) > 0)
                sb.append(charBuffer, 0, bytesRead);

            return sb.toString();
        }
        catch (final Exception e)
        {
            log.error("Exception while reading data from HTTP Request. " + aRequest.getServerName() + ":" + aRequest.getServerPort() + "/" + aRequest.getServletContext() + " Path :'"
                    + aRequest.getServletPath() + "'", e);
            throw e;
        }
    }

    public static String getJSONValue(
            JSONObject aJSOnObject,
            String aKeyName)
    {
        return (String) (aJSOnObject.get(aKeyName));
    }

    public static JSONArray getJSONArray(
            JSONObject aJSOnObject,
            String aKeyName)
    {
        return (JSONArray) aJSOnObject.get(aKeyName);
    }

    public static String stripStart(
            String str,
            char stripChar)
    {
        int start = 0;

        try
        {
            final char[] allChars = str.toCharArray();

            for (final char c : allChars)
                if (c == stripChar)
                    start++;
                else
                    break;
        }
        catch (final Exception e)
        {
            log.error("Exception while stripping the char " + stripChar + " in " + str, e);
        }
        return str.substring(start);
    }

    public static boolean isPromotionalMessage(
            String aMsgType)
    {
        return "0".equals(aMsgType);
    }

    public static String getFormattedDateTime(
            long aDateInTimeMillies)
    {
        return DateTimeUtility.getFormattedDateTime(aDateInTimeMillies, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS);
    }

    public static String getFormattedDateTime(
            Date aDate,
            DateTimeFormat aFormat,
            String aTimezone)
    {
        if (!"".equals(CommonUtility.nullCheck(aTimezone, true)))
            return TimeZoneUtility.getDateStringBasedOnTimeZone(aDate, aFormat, aTimezone);

        return TimeZoneUtility.getDateStringBasedOnTimeZone(aDate, aFormat);
    }

    public static String getFormattedDateTime(
            Date aDate,
            String aFormat,
            String aTimezone)
    {
        if (!"".equals(CommonUtility.nullCheck(aTimezone, true)))
            return TimeZoneUtility.getDateStringBasedOnTimeZone(aDate, aFormat, aTimezone);

        return TimeZoneUtility.getDateStringBasedOnTimeZone(aDate, aFormat);
    }

    public static boolean isUnicodeMessage(
            String aMessage)
    {

        try
        {

            for (int i = 0; i < aMessage.length(); i++)
            {
                final char   c            = aMessage.charAt(i);
                final String encodestring = URLEncoder.encode(String.valueOf(c), StandardCharsets.UTF_8);
                if (encodestring.length() > 3)
                    return true;
            }
        }
        catch (final Exception e)
        {
            log.error("isMessageContainUnicode() ", e);
        }
        return false;
    }

    public static String processUnicodeMessage(
            String aMessage)
    {
        if (log.isDebugEnabled())
            log.debug("processRequest() message is identified as Unicode");

        String hexa = null;

        try
        {
            hexa = MessageConvertionUtility.convertString2HexString(aMessage);

            if (log.isDebugEnabled())
                log.debug("processRequest() message hexa : " + hexa);

            if (hexa.indexOf("FEFF") != -1)
                hexa = hexa.substring(hexa.lastIndexOf("FEFF") + 4);

            if (log.isDebugEnabled())
                log.debug("processRequest() Hexa Msg : " + hexa);
        }
        catch (final Exception e)
        {
            log.error("Error while process the unicode message ", e);
        }
        return hexa;
    }

    public static void setMessageId(
            InterfaceMessage aMessageBean)
    {
        final String messageId = MessageIdentifier.getInstance().getNextId();
        aMessageBean.setBaseMessageId(messageId);
        aMessageBean.setMsgId(messageId);
    }

    public static JSONArray splitIntoJsonArray(
            String aStr,
            String aDelimiter)
    {
        final String    lStr  = CommonUtility.nullCheck(aStr);
        final JSONArray array = new JSONArray();

        if (!lStr.isBlank())
        {
            final String[] lData = lStr.split(aDelimiter);
            for (final String lString : lData)
                array.add(lString);
        }

        if (log.isDebugEnabled())
            log.debug("seperated string " + array);

        return array;
    }

    public static void setAccInfo(
            BasicInfo aBasicInfo)
            throws ParseException
    {
        if (log.isDebugEnabled())
            log.debug("Access Key value : " + aBasicInfo.getAccessKey());

        final UserInfo lUserInfo = ClientAccountDetails.getUserDetailsByAccessKey(aBasicInfo.getAccessKey());

        if (log.isDebugEnabled())
            log.debug("Access key: '" + aBasicInfo.getAccessKey() + "' userAccessInfo: '" + lUserInfo + "'");

        if (lUserInfo == ClientAccountDetails.INVALID_USER)
        {
            aBasicInfo.setAccountStatus(AccountStatus.INVALID);
            return;
        }

        final String        lClientID      = lUserInfo.getClientId();
        final AccountStatus lAccountStatus = lUserInfo.getAccountStatus();

        if (log.isDebugEnabled())
            log.debug("Client Id :  '" + lClientID + "' AccountStatus : '" + lAccountStatus + "'");

        aBasicInfo.setAccountStatus(lAccountStatus);

        if (lAccountStatus == AccountStatus.ACTIVE)
        {
            final String     lAccountJson = lUserInfo.getAccountDetails();
            final JSONObject obj          = parseJSON(lAccountJson);
            aBasicInfo.setClientId(lClientID);
            aBasicInfo.setUserAccountInfo(obj);
        }

        // TODO KP What will happen if not found.
    }

    public static void setAccInfo(
            BasicInfo aBasicInfo,
            String aClientId)
            throws ParseException
    {
        if (log.isDebugEnabled())
            log.debug("Client Id : " + aClientId);

        final UserInfo lUserInfo = ClientAccountDetails.getUserDetailsByClientId(aClientId);

        if (log.isDebugEnabled())
            log.debug("Client ID: '" + aClientId + "' userAccessInfo: '" + lUserInfo + "'");

        if (lUserInfo == ClientAccountDetails.INVALID_USER)
        {
            aBasicInfo.setAccountStatus(AccountStatus.INVALID);
            return;
        }

        final AccountStatus lAccountStatus = lUserInfo.getAccountStatus();

        if (log.isDebugEnabled())
            log.debug("Client Id :  '" + aClientId + "' AccountStatus : '" + lAccountStatus + "'");

        aBasicInfo.setAccountStatus(lAccountStatus);

        if (lAccountStatus == AccountStatus.ACTIVE)
        {
            final String     lAccountJson = lUserInfo.getAccountDetails();
            final JSONObject obj          = parseJSON(lAccountJson);
            aBasicInfo.setClientId(aClientId);
            aBasicInfo.setUserAccountInfo(obj);
        }

        // TODO KP What will happen if not found.
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
        return Integer.parseInt(getConfigParamsValueAsString(aKey));
    }

    public static String convertStringAloneWithoutVLIntoHex(
            String aMessage)
    {
        final List<String>  arrayList = splitStringAndVl(aMessage);
        final StringBuilder stringBuf = new StringBuilder();

        for (final String data : arrayList)
            if ((data.contains(VL_START_STRING)) && (data.contains(VL_END_STRING)))
                stringBuf.append(data);
            else
                stringBuf.append(MessageConvertionUtility.convertString2HexString(data));
        return stringBuf.toString();
    }

    private static List<String> splitStringAndVl(
            String aUnicodeMessage)
    {
        final List<String> lUnicodeMsgList = new ArrayList<>();

        if ((aUnicodeMessage.contains(VL_START_STRING)) && (aUnicodeMessage.contains(VL_END_STRING)))
        {
            String lUnicodeMessageTemp = aUnicodeMessage;

            while (lUnicodeMessageTemp.length() > 0)
            {
                final int lStartIndex = lUnicodeMessageTemp.indexOf(VL_START_STRING);
                final int lEndIndex   = lUnicodeMessageTemp.indexOf(VL_END_STRING);

                // TODO KP Need to evaluate
                if ((lStartIndex < 0) || (lEndIndex < 0))
                {
                    if (lUnicodeMessageTemp.length() <= 0)
                        break;

                    lUnicodeMsgList.add(lUnicodeMessageTemp);
                    break;
                }

                lUnicodeMsgList.add(lUnicodeMessageTemp.substring(0, lStartIndex));
                lUnicodeMsgList.add(lUnicodeMessageTemp.substring(lStartIndex, lEndIndex + 2));
                lUnicodeMessageTemp = lUnicodeMessageTemp.substring(lEndIndex + 2);
            }
        }
        return lUnicodeMsgList;
    }

    public static InterfaceRequestStatus validateCluster(
            String aCluster)
    {
        InterfaceRequestStatus lReqStatus = new InterfaceRequestStatus(InterfaceStatusCode.ACCESS_VIOLATION, null);

        try
        {
            if (APIConstants.requestAllowed(aCluster))
                lReqStatus = null;
        }
        catch (final Exception e)
        {
            log.error("Exception while checking the otp block ", e);
        }

        return lReqStatus;
    }

    public static String[] getAccessKey(
            String aAuthKey)
    {
        if (log.isDebugEnabled())
            log.debug("Authorization value befor seperate : " + aAuthKey);

        aAuthKey = aAuthKey.replace("Basic ", "");

        final String lAuthorizationString = CommonUtility.nullCheck(Utility.decodeString(aAuthKey));

        if (lAuthorizationString.contains(":"))
            return lAuthorizationString.split(":");
        return null;
    }

    public static String decodeString(
            String aValue)
    {
        return new String(Base64.getDecoder().decode(aValue));
    }

    public static String replaceParamIfAvailable(
            InterfaceResponse aInterfaceResponse,
            ResponseObject aRequestObject,
            InterfaceResponseCodeMapping aResponseCodeMapping)
    {
        String lResponseTemplate = aInterfaceResponse.getResponseTemplate();
        if (lResponseTemplate.contains(REQ_ID))
            lResponseTemplate = getReplacedString(lResponseTemplate, REQ_ID, aRequestObject.getMessageId());

        if (lResponseTemplate.contains(REQ_TIME))
            lResponseTemplate = getReplacedString(lResponseTemplate, REQ_TIME, aRequestObject.getReqTime());

        if (lResponseTemplate.contains(STATUS_CODE))
            lResponseTemplate = getReplacedString(lResponseTemplate, STATUS_CODE,
                    aResponseCodeMapping == null ? aRequestObject.getStatusCode().getStatusCode() : aResponseCodeMapping.getClientStatusCode());

        if (lResponseTemplate.contains(STATUS_INFO))
            lResponseTemplate = getReplacedString(lResponseTemplate, STATUS_INFO,
                    (aResponseCodeMapping == null) || aResponseCodeMapping.getClientStatusInfo().isBlank() ? aRequestObject.getStatusInfo() : aResponseCodeMapping.getClientStatusInfo());

        if (lResponseTemplate.contains(STATUS_REASON))
            lResponseTemplate = getReplacedString(lResponseTemplate, STATUS_REASON,
                    (aResponseCodeMapping == null) || aResponseCodeMapping.getClientReason().isBlank() ? aRequestObject.getReason() : aResponseCodeMapping.getClientReason());

        return lResponseTemplate;
    }

    private static String getReplacedString(
            String aStringToReplace,
            String aCheckString,
            String aValue)
    {
        String lReplacedValue = "";

        if (StringUtils.isNotEmpty(aValue))
            lReplacedValue = aValue;
        else
            lReplacedValue = StringUtils.EMPTY;

        aStringToReplace = StringUtils.replace(aStringToReplace, aCheckString, lReplacedValue);
        return aStringToReplace;
    }

    public static String getGeneralJsonResponse(
            ResponseObject aResponseObject)
    {
        final JSONObject          obj = new JSONObject();
        final Map<String, String> map = new HashMap<>();
        map.put(InterfaceInputParameters.RESP_PARAMETER_CODE, aResponseObject.getStatusCode().getStatusCode());
        map.put(InterfaceInputParameters.RESP_PARAMETER_INFO, aResponseObject.getStatusInfo());
        map.put(InterfaceInputParameters.RESP_PARAMETER_REASON, aResponseObject.getReason());

        obj.put(InterfaceInputParameters.RESP_PARAMETER_STATUS, map);
        obj.put(InterfaceInputParameters.RESP_PARAMETER_REQID, aResponseObject.getMessageId());
        obj.put(InterfaceInputParameters.RESP_PARAMETER_REQTIME, aResponseObject.getReqTime());

        return obj.toString();
    }

    public static String getGeneralXmlResponse(
            ResponseObject aResponseObject)
            throws JAXBException
    {
        final ObjectFactory mr       = new ObjectFactory();
        final Root          response = mr.createRoot();
        response.setReqId(aResponseObject.getMessageId());
        response.setReqTime(aResponseObject.getReqTime());

        final Status       status         = new Status();
        final JAXBContext  jaxbContext    = JAXBContext.newInstance(Root.class);
        final StringWriter sw             = new StringWriter();
        final Marshaller   jaxbMarshaller = jaxbContext.createMarshaller();

        status.setCode(aResponseObject.getStatusCode().getStatusCode());
        status.setInfo(aResponseObject.getStatusInfo());
        status.setReason(aResponseObject.getReason());
        response.setStatus(status);

        jaxbMarshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        jaxbMarshaller.marshal(response, sw);
        return sw.toString();
    }

    public static Date changeScheduleTimeToGivenOffset(
            String aTimeZone,
            BasicInfo aBasicInfo)
    {
        final Date lScheduleDate = TimeZoneUtility.getDateBasedOnTimeZone(aBasicInfo.getScheduleTime(), DateTimeFormat.DEFAULT, aTimeZone);

        if (log.isDebugEnabled())
            log.debug("After converting to IST : '" + lScheduleDate + "'");

        aBasicInfo.setScheduleTime(DateTimeUtility.getFormattedDateTime(lScheduleDate, DateTimeFormat.DEFAULT));

        return lScheduleDate;
    }

    public static boolean isMessageContainsUnicode(
            String aMessage,
            InterfaceMessage aMessageObj,
            BasicInfo aBasicInfo)
    {
        if (log.isDebugEnabled())
            log.debug("Message : " + aMessage);

        boolean isMsgUnicode = false;

        try
        {
            final int lAccSpecialCharLen = CommonUtility.getInteger(CommonUtility.nullCheck(aBasicInfo.getUserAccountInfo().get(MiddlewareConstant.MW_UC_IDEN_CHAR_LEN.getName()), true), 0);

            if (log.isDebugEnabled())
                log.debug("Configured Special Char Len : " + lAccSpecialCharLen);

            final int lConfigOccurenceCount = CommonUtility.getInteger(CommonUtility.nullCheck(aBasicInfo.getUserAccountInfo().get(MiddlewareConstant.MW_UC_IDEN_OCCUR.getName()), true), 0);

            if (log.isDebugEnabled())
                log.debug("Configured Occurence count : " + lConfigOccurenceCount);

            final boolean isRemoveUCChars = CommonUtility.isEnabled(CommonUtility.nullCheck(aBasicInfo.getUserAccountInfo().get(MiddlewareConstant.MW_IS_REMOVE_UC_CHARS.getName()), true));

            if (log.isDebugEnabled())
                log.debug("Is Remove UC Chars : " + isRemoveUCChars);

            final Response lResponse = MsgIdentifierUtil.messageIdentifier(aMessage, lAccSpecialCharLen, lConfigOccurenceCount, isRemoveUCChars, aMessage);

            aMessage     = lResponse.getMessage();

            isMsgUnicode = lResponse.isIsUniCode();
        }
        catch (final Exception e)
        {
            log.error("Ingore the exception .", e);
        }

        aMessageObj.setMessage(aMessage);

        return isMsgUnicode;
    }

    public static boolean isMessageContainsUnicode(
            String aMessage)
    {
        if (log.isDebugEnabled())
            log.debug("Message : " + aMessage);

        boolean isMsgUnicode = false;

        try
        {
            final Response lResponse = MsgIdentifierUtil.messageIdentifier(aMessage, 2, 2, false, aMessage);

            isMsgUnicode = lResponse.isIsUniCode();
        }
        catch (final Exception e)
        {
            log.error("Ingore the exception .", e);
        }

        return isMsgUnicode;
    }

    public static JSONObject parseJSON(
            String aJsonString)
            throws ParseException
    {
        return (JSONObject) new JSONParser().parse(aJsonString);
    }

    public static ClusterType getClusterType(
            String aCluster)
    {
        if (aCluster != null)
            return ClusterType.getCluster(aCluster);
        return ClusterType.COMMON;
    }

    public static CountryInfo getCountryInfo(
            String aDefaultCountry)
    {
        final CountryInfoCollection lCountryInfoCollection = (CountryInfoCollection) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.COUNTRY_INFO);
        return lCountryInfoCollection.getCountryData(aDefaultCountry);
    }

    public static boolean isNumaric(
            String aValue)
    {

        try
        {
            Long.parseLong(aValue);
        }
        catch (final NumberFormatException e)
        {
            return false;
        }
        return true;
    }

    public static EncryptInfo getEncryptInfo(
            String aClientId)
    {
        final EncryptInfoCollection lEncryptInfoCollection = (EncryptInfoCollection) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.ENCRYPT_INFO);
        return lEncryptInfoCollection.getEncryptInfo(aClientId);
    }

    public static boolean isIgnoreVersion(
            String aClientId)
    {
        final InmemCustomFeatures lCustomFeatures = (InmemCustomFeatures) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.CUSTOM_FEATURES);
        return CommonUtility.isEnabled(lCustomFeatures.getValueOfCustomFeature(aClientId, CustomFeatures.IGNORE_JAPI_VERSION.getKey()));
    }

    public static String getJsonErrorResponse(
            String aResponseDateTimeString)
    {
        return "{\"status\":{\"code\":\"" + InterfaceStatusCode.INTERNAL_SERVER_ERROR.getStatusCode() + "\",\"info\": " + APIConstants.STATUS_INFO_REJECT + "\",\"reason\":\""
                + InterfaceStatusCode.INTERNAL_SERVER_ERROR.getStatusDesc() + "\"},\"req_id\":\"N/A\",\"req_time\":\"" + aResponseDateTimeString + "\"}";
    }

    public static String getXmlErrorResponse(
            String aResponseDateTimeString)
    {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + "<root xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"response.xsd\">\r\n" + "  <status>\r\n"
                + "    <code>" + InterfaceStatusCode.INTERNAL_SERVER_ERROR.getStatusCode() + "</code>\r\n" + "<info>\r\n" + APIConstants.STATUS_INFO_REJECT + "</info>\r\n" + "    <reason>"
                + InterfaceStatusCode.INTERNAL_SERVER_ERROR.getStatusDesc() + "</reason>\r\n" + "  </status>\r\n" + "  <req_id>N/A</req_id>\r\n" + " <req_time>" + aResponseDateTimeString
                + "</req_time>\r\n" + "</root>";
    }

}