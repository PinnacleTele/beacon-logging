package com.itextos.beacon.platform.r3c.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;

import com.itextos.beacon.commonlib.commonpropertyloader.PropertiesPath;
import com.itextos.beacon.commonlib.commonpropertyloader.PropertyLoader;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.ShortcodeLength;
import com.itextos.beacon.commonlib.message.MessagePart;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.shortcodeprovider.ShortcodeProvider;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.inmemory.configvalues.ApplicationConfiguration;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.inmemory.visualizelink.util.VLUtil;
import com.itextos.beacon.platform.duplicatecheckprocessor.DuplicateCheck;
import com.itextos.beacon.platform.r3c.db.R3CDataBaseUtil;

public class R3CUtil
{

    private static final Log               log                             = LogFactory.getLog(R3CUtil.class);

    private static PropertiesConfiguration vlAdditionalInfoProps;

    public static final String             URL_TRACK_PATTERN_PREFIX        = "[~";
    public static final String             URL_TRACK_PATTERN_SUFFIX        = "~]";

    public static final int                LENGTH_URL_TRACK_PATTERN_PREFIX = URL_TRACK_PATTERN_PREFIX.length();
    public static final int                LENGTH_URL_TRACK_PATTERN_SUFFIX = URL_TRACK_PATTERN_SUFFIX.length();

    private static final int               DUP_CHECK_ON_CLIENT_MESSAGE_ID  = 1;
    private static final int               DUP_CHECK_ON_MESSAGE            = 2;
    private static String                  URL_START_DELIMITER             = null;
    private static String                  URL_END_DELIMITER               = null;
    private static String                  EXCLUDE_URL_START_DELIMITER     = null;
    private static String                  EXCLUDE_URL_END_DELIMITER       = null;
    private static final String            URL_EXACT_MATCH                 = "0";
    private static final String            URL_PARTIAL_MATCH               = "1";

    private static final String            URL_FINDER_REG_EXP              = "(?:^|[\\W])((ht)tp(s?):\\/\\/|www\\.)" //
            + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*" //
            + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)";

    static
    {
        URL_START_DELIMITER         = getAppConfigValueAsString(ConfigParamConstants.VL_URL_STARTS_WITH);
        URL_END_DELIMITER           = getAppConfigValueAsString(ConfigParamConstants.VL_URL_ENDS_WITH);
        EXCLUDE_URL_START_DELIMITER = getAppConfigValueAsString(ConfigParamConstants.VL_EXCLUDE_URL_STARTS_WITH);
        EXCLUDE_URL_END_DELIMITER   = getAppConfigValueAsString(ConfigParamConstants.VL_EXCLUDE_URL_ENDS_WITH);
    }

    private R3CUtil()
    {}

    public static List<String> getUrlsWithPrefix(
            String aMessage,
            boolean aIsExclude)
    {
        final List<String> lExtractUrls    = new ArrayList<>();

        String             lStartDelimiter = URL_START_DELIMITER;
        String             lEndDelimiter   = URL_END_DELIMITER;

        if (aIsExclude)
        {
            lStartDelimiter = EXCLUDE_URL_START_DELIMITER;
            lEndDelimiter   = EXCLUDE_URL_END_DELIMITER;
        }

        if (log.isDebugEnabled())
            log.debug("getUrls() - VL Starts With : " + lStartDelimiter + " :: VL Ends With : " + lEndDelimiter);

        final String[] lStrExtractUrls = StringUtils.substringsBetween(aMessage, lStartDelimiter, lEndDelimiter);
        if (lStrExtractUrls != null)
            for (String url : lStrExtractUrls)
            {
                url = url.trim();
                if (isValidateUrl(url))
                    lExtractUrls.add(url);
            }

        if (log.isDebugEnabled())
            log.debug("Available URLs in the message " + lExtractUrls);

        return lExtractUrls;
    }

    private static boolean isValidateUrl(
            String aUrl)
    {
        return aUrl.matches("^(http|https)://.*$") || aUrl.startsWith("www.") || aUrl.startsWith("WWW.");
    }

    public static List<String> getNonPrefixedUrls(
            String aMessage)
    {
        final List<String> lUrlList   = new ArrayList<>();

        final Pattern      pattern    = Pattern.compile(URL_FINDER_REG_EXP, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        final Matcher      urlMatcher = pattern.matcher(aMessage);

        while (urlMatcher.find())
        {
            String lUrlFromMsg = aMessage.substring(urlMatcher.start(1), urlMatcher.end());

            if (lUrlFromMsg.endsWith("."))
                lUrlFromMsg = lUrlFromMsg.substring(0, lUrlFromMsg.length() - 1);

            lUrlList.add(lUrlFromMsg);
        }

        return lUrlList;
    }

    public static void addSmartLinkId(
            MessageRequest aMessageRequest,
            String aSmartLinkId)
    {
        String lTempSmartLinkId = aMessageRequest.getSmartlinkId();

        if (lTempSmartLinkId != null)
            lTempSmartLinkId += "," + aSmartLinkId;
        else
            lTempSmartLinkId = aSmartLinkId;

        aMessageRequest.setSmartlinkId(lTempSmartLinkId);
    }

    public static String rplSplCharinMessage(
            String aMessage)
    {

        if (aMessage.length() > 0)
        {
            aMessage = aMessage.replaceAll("\"", "\\\"");
            aMessage = aMessage.replaceAll("\'", "\\\'");
        }
        return aMessage;
    }

    public static List<String> removeExcludeUrls(
            List<String> aAllUrls,
            List<String> aExcludeUrls)
    {

        for (String excludeUrl : aExcludeUrls)
        {
            excludeUrl = excludeUrl + EXCLUDE_URL_END_DELIMITER;
            aAllUrls.remove(excludeUrl);
        }
        return aAllUrls;
    }

    public static boolean isExcludeUrl(
            String aClientId,
            String aUrl)
    {

        try
        {
            final List<String> lExcludeUrls = VLUtil.getExcludeUrls(aClientId);

            if (log.isDebugEnabled())
                log.debug("isExcludeUrl() - lExcludeUrls - " + lExcludeUrls);

            if ((lExcludeUrls != null) && !lExcludeUrls.isEmpty())
            {
                if (log.isDebugEnabled())
                    log.debug("Exclude Url from DB : " + lExcludeUrls.size());

                for (final String excludeUrl : lExcludeUrls)
                {
                    final String[] lTempExcludeUrl = excludeUrl.split("~");

                    if (URL_EXACT_MATCH.equals(lTempExcludeUrl[0]))
                        return isUrlExactMatch(aUrl, lTempExcludeUrl[1]);

                    if (URL_PARTIAL_MATCH.equals(lTempExcludeUrl[0]))
                        return isUrlPartialMatch(aUrl, lTempExcludeUrl[1]);
                }
            }
        }
        catch (final Exception e)
        {
            log.error("isExcludeUrl : err ", e);
        }

        return false;
    }

    public static boolean isUrlExactMatch(
            String aUrl,
            String aDBUrl)
    {
        return aUrl.equalsIgnoreCase(aDBUrl);
    }

    public static boolean isUrlPartialMatch(
            String aUrl,
            String aDBUrl)
    {
        return aUrl.toLowerCase().startsWith(aDBUrl.toLowerCase());
    }

    public static String replace(
            String aMessage,
            boolean isExcluede)
    {
        String lStartDelimiter = URL_START_DELIMITER;
        String lEndDelimiter   = URL_END_DELIMITER;

        if (isExcluede)
        {
            lStartDelimiter = EXCLUDE_URL_START_DELIMITER;
            lEndDelimiter   = EXCLUDE_URL_END_DELIMITER;
        }

        aMessage = StringUtils.replace(aMessage, lStartDelimiter, "");
        aMessage = StringUtils.replace(aMessage, lEndDelimiter, "");

        return aMessage;
    }

    public static boolean isDuplicate(
            MessageRequest aMessageRequest)
    {
        if (log.isDebugEnabled())
            log.debug("isDuplicate() start base message id : " + aMessageRequest.getBaseMessageId());

        if (aMessageRequest.getDupCheckForUI() == 1)
        {
            final String lCampId = CommonUtility.nullCheck(aMessageRequest.getCampaignId());

            if (log.isDebugEnabled())
                log.debug("Campiagn Id : " + lCampId);

            final String lMobileNumber = aMessageRequest.getMobileNumber();
            return DuplicateCheck.isDuplicatCampiagn(aMessageRequest.getClientId(), lMobileNumber, lCampId);
        }

        if (log.isDebugEnabled())
            log.debug("Duplicate Check for other applications ..");

        if (aMessageRequest.getDuplicateCheckEnabled() == DUP_CHECK_ON_CLIENT_MESSAGE_ID)
        {
            if (CommonUtility.nullCheck(aMessageRequest.getClientMessageId(), true).length() != 0)
                return DuplicateCheck.isDuplicateCustRef(aMessageRequest.getClientId(), aMessageRequest.getClientMessageId().toLowerCase(), aMessageRequest.getDuplicateCheckInterval() * 60);
        }
        else
            if (aMessageRequest.getDuplicateCheckEnabled() == DUP_CHECK_ON_MESSAGE)
                return DuplicateCheck.isDuplicateMessage(aMessageRequest.getClientId(), aMessageRequest.getMobileNumber(), aMessageRequest.getLongMessage().toLowerCase(),
                        aMessageRequest.getDuplicateCheckInterval() * 60);
        return false;
    }

    public static boolean isItextosDomain(
            String aDomainUrl)
    {
        final String lItextosUrl = getAppConfigValueAsString(ConfigParamConstants.R3C_DEFAULT_DOMAIN_URL);
        return aDomainUrl.startsWith(lItextosUrl);
    }

    public static JSONObject getAdditionInfoStoreToDB(
            MessageRequest aMessageRequest,
            VLRepository aVLRepository)
    {
        final Map<String, String> addInfo = new HashMap<>();

        try
        {
            if (vlAdditionalInfoProps == null)
                vlAdditionalInfoProps = PropertyLoader.getInstance().getPropertiesConfiguration(PropertiesPath.R3C_ELASTIC_ADDINFO_PATH, true);

            final Iterator<String> iterator = vlAdditionalInfoProps.getKeys();

            while (iterator.hasNext())
            {
                final String key = iterator.next();
                addInfo.put(key, CommonUtility.nullCheck(aMessageRequest.getValue(MiddlewareConstant.getMiddlewareConstantByName(key))));
            }
        }
        catch (final Exception e)
        {
            log.error("Exception occer while getting additionalInfo to Store mongo...", e);
        }
        return getJson(addInfo, aVLRepository);
    }

    private static JSONObject getJson(
            Map<String, String> aAddInfo,
            VLRepository aVLRepository)
    {
        final JSONObject lAddInfo = new JSONObject(aAddInfo);

        lAddInfo.put(MiddlewareConstant.MW_R3C_SMARTLINK_ID.getName(), aVLRepository.getSmartLinkId());
        lAddInfo.put(MiddlewareConstant.MW_R3C_URL.getName(), aVLRepository.getUrl());
        lAddInfo.put(MiddlewareConstant.MW_R3C_SHORTNER_URL.getName(), aVLRepository.getShortenUrl());

        return lAddInfo;
    }

    public static String getShortCode(
            int aShortCodeLen)
    {
        final String lShortCode = ShortcodeProvider.getInstance().getNextShortcode(ShortcodeLength.getShortcode(aShortCodeLen));

        if (log.isDebugEnabled())
            log.debug("ShortCode Value : " + lShortCode);

        return lShortCode;
    }

    public static boolean insertIntoElasticSearch(
            MessageRequest aMessageRequest,
            VLRepository aVLRepository)
    {
        boolean isInsertSuccess = false;

        try
        {
            final JSONObject addInfo = R3CUtil.getAdditionInfoStoreToDB(aMessageRequest, aVLRepository);

            if (addInfo != null)
            {
                addInfo.put(MiddlewareConstant.MW_PLATFORM_CLUSTER.getName(), aMessageRequest.getClusterType().getKey());
                final MessagePart lMessageObject = aMessageRequest.getMessageParts().get(0);
                final long        lMsgRecvTime   = lMessageObject.getMessageReceivedTime() == null ? 0 : lMessageObject.getMessageReceivedTime().getTime();
                addInfo.put(MiddlewareConstant.MW_MSG_RECEIVED_TIME.getName(), String.valueOf(lMsgRecvTime));
            }

            if (log.isDebugEnabled())
                log.debug("Additional Info to store in Database : " + addInfo);

            R3CUtil.setExpiryTime(aVLRepository, aMessageRequest.getUrlShortCodeLength());

            isInsertSuccess = R3CDataBaseUtil.insertIntoDb(aVLRepository, addInfo);
        }
        catch (final Exception e)
        {
            log.error("Exception in saving url payload along with short code in db...", e);
        }

        return isInsertSuccess;
    }

    public static VLRepository createVlRepoObject(
            MessageRequest aMessageRequest,
            String aRedirectUrl,
            String aSmartLinkId,
            String aDomainUrl,
            boolean isSmartLinkProcess)
    {
        if (log.isDebugEnabled())
            log.debug("Calling VL Repo Object");

        boolean            isItextosDomain = false;

        final VLRepository lVlRepository   = new VLRepository();
        lVlRepository.setClientId(aMessageRequest.getClientId());
        lVlRepository.setMid(aMessageRequest.getBaseMessageId());
        lVlRepository.setMobileNumber(aMessageRequest.getMobileNumber());
        lVlRepository.setUrl(aRedirectUrl);
        lVlRepository.setSmartLinkId(Long.parseLong(aSmartLinkId));
        lVlRepository.setCreatedTs(new Date());
        lVlRepository.setFileId(aMessageRequest.getFileId());

        if (!isSmartLinkProcess)
        {
            final String lRedirectAndDomainUrl = VLUtil.getVLInfo(CommonUtility.combine(aMessageRequest.getClientId(), aSmartLinkId));

            if (log.isDebugEnabled())
                log.debug("RedirectAndDomainUrl - '" + lRedirectAndDomainUrl + "'");

            final String[] lTempVal = StringUtils.split(lRedirectAndDomainUrl, "~");
            if (log.isDebugEnabled())
                log.debug("lTempVal length : " + lTempVal.length);

            aRedirectUrl = CommonUtility.nullCheck(lTempVal[0], true);

            if (lTempVal.length > 1)
                aDomainUrl = CommonUtility.nullCheck(lTempVal[1], true);
            else
            {
                if (log.isDebugEnabled())
                    log.debug("Domain Url alone Present Case ...");

                if (lRedirectAndDomainUrl.indexOf("~") != -1)
                {
                    aDomainUrl = lRedirectAndDomainUrl.substring(lRedirectAndDomainUrl.indexOf("~") + 1);
                    if (log.isDebugEnabled())
                        log.debug("Domain Url alone Present : '" + aDomainUrl + "'");
                }
            }

            if ((aDomainUrl != null) && !aDomainUrl.isEmpty())
                isItextosDomain = R3CUtil.isItextosDomain(aDomainUrl);

            if (log.isDebugEnabled())
                log.debug("Is Valid ITextos Domain : " + isItextosDomain);

            if (!isItextosDomain)
            {
                if (log.isDebugEnabled())
                    log.debug("Redirect Url : " + aRedirectUrl);

                if (aRedirectUrl.isEmpty())
                    return null;

                if (log.isDebugEnabled())
                    log.debug("Setting redirect url .. Hence Domain url is not available .." + aRedirectUrl);

                if ((aDomainUrl != null) && !aDomainUrl.isEmpty())
                    lVlRepository.setShortenUrl(aDomainUrl);
                else
                    lVlRepository.setShortenUrl(aRedirectUrl);

                lVlRepository.setIsRedirectUrlForShortner(true);

                return lVlRepository;
            }
        }

        updateShortCodeUrl(aMessageRequest, lVlRepository, aSmartLinkId, aDomainUrl, isSmartLinkProcess);

        final boolean isSuccessInsert = R3CUtil.insertIntoElasticSearch(aMessageRequest, lVlRepository);

        return isSuccessInsert ? lVlRepository : null;
    }

    public static void updateShortCodeUrl(
            MessageRequest aMessageRequest,
            VLRepository aVLRepository,
            String aSmartLinkId,
            String aDomainUrl,
            boolean isSmartLinkProcess)
    {
        if (log.isDebugEnabled())
            log.debug("Domain Url : " + aDomainUrl);

        if (!aDomainUrl.endsWith("/"))
            aDomainUrl = aDomainUrl + "/";

        final String lShortCode  = R3CUtil.getShortCode(aMessageRequest.getUrlShortCodeLength());
        final String lShortenUrl = aDomainUrl + lShortCode;

        aVLRepository.setId(lShortCode);
        aVLRepository.setShortCode(lShortCode);
        aVLRepository.setShortenUrl(lShortenUrl);
    }

    public static void setExpiryTime(
            VLRepository aVLRepository,
            int aScLength)
    {
        final int      lSlaValue = getSLAValue(aScLength);
        final Calendar lTempCal  = Calendar.getInstance();
        lTempCal.add(Calendar.DATE, lSlaValue);

        aVLRepository.setExpiryDate(DateTimeUtility.getDateFromString(DateTimeUtility.getFormattedDateTime(lTempCal.getTime(), DateTimeFormat.DEFAULT), DateTimeFormat.DEFAULT));
    }

    private static int getSLAValue(
            int aSCLength)
    {
        if (aSCLength == 5)
            return CommonUtility.getInteger(getAppConfigValueAsString(ConfigParamConstants.FIVE_DIGIT_SHORTCODE_SLA), 45);

        return CommonUtility.getInteger(getAppConfigValueAsString(ConfigParamConstants.SIX_DIGIT_SHORTCODE_SLA), 60);
    }

    public static String getAppConfigValueAsString(
            ConfigParamConstants aConfigParamConstant)
    {
        final ApplicationConfiguration lAppConfiguration = (ApplicationConfiguration) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.APPLICATION_CONFIG);
        return lAppConfiguration.getConfigValue(aConfigParamConstant.getKey());
    }

}