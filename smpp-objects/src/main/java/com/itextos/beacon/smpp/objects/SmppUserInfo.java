package com.itextos.beacon.smpp.objects;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.itextos.beacon.commonlib.constants.AccountStatus;
import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.MessagePriority;
import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.ipvalidation.IPValidator;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemdata.account.UserInfo;
import com.itextos.beacon.inmemory.smpp.account.SmppAccInfo;
import com.itextos.beacon.inmemory.smpp.account.util.SmppAccUtil;
import com.itextos.beacon.smpp.utils.properties.SmppUtilConstants;

public class SmppUserInfo
{

    private static final Log log = LogFactory.getLog(SmppUserInfo.class);

    private final UserInfo   mUserInfo;
    private final String     mSystemId;
    private final String     mClientId;
    private final String     mJsonString;
    private final JsonObject mJsonObject;
    private ClusterType      mClusterType;
    private MessageType      mMessageType;
    private MessagePriority  mMessagePriority;
    private boolean          isDomoesticTraReject;
    private SmppAccInfo      mSmppAccountInfo;

    public SmppUserInfo(
            UserInfo aUserInfo)
    {
        if (log.isDebugEnabled())
            log.debug("User Info :" + aUserInfo);

        mUserInfo   = aUserInfo;
        mSystemId   = mUserInfo.getUserName();
        mClientId   = mUserInfo.getClientId();
        mJsonString = mUserInfo.getAccountDetails(); // TODO Need to verify the JSON.
        mJsonObject = getJsonObject(mJsonString, mClientId);

        updateObject();
    }

    private JsonObject getJsonObject(
            String aJsonString,
            String aClientId)
    {
        if (log.isDebugEnabled())
            log.debug("User Details : " + aJsonString);
        JsonObject lAccountJson = null;

        if (aJsonString != null)
        {
            lAccountJson     = JsonParser.parseString(aJsonString).getAsJsonObject();
            mSmppAccountInfo = SmppAccUtil.getSmppAccountInfo(aClientId);

            if (mSmppAccountInfo == null)
            {
                lAccountJson = null;
                log.error(new Throwable("SMPP Account information is not configure in accounts.user_smpp_config table for the account :" + aClientId));
            }
        }

        return lAccountJson;
    }

    public String getClientId()
    {
        return mClientId;
    }

    public String getSystemId()
    {
        return mSystemId;
    }

    private String getAccountValueFromJson(
            MiddlewareConstant aMiddlewareConstant)
    {
        final JsonElement lJsonElement = mJsonObject.get(aMiddlewareConstant.getName());
        if (lJsonElement != null)
            return lJsonElement.getAsString();
        return null;
    }

    @Deprecated
    private String getAccountValueFromJson(
            String aMiddlewareConstantKey)
    {
        final JsonElement lJsonElement = mJsonObject.get(aMiddlewareConstantKey);
        if (lJsonElement != null)
            return lJsonElement.getAsString();
        return null;
    }

    @Deprecated
    public String getAccountValue(
            String aKey)
    {
        return mJsonObject.get(aKey).getAsString();
    }

    @Deprecated
    public String getAccountJson()
    {
        return mJsonString;
    }

    public int getMaxBindAllowed()
    {
        return mSmppAccountInfo.getMaxConnAllowed();
    }

    private void updateObject()
    {
        mClusterType = ClusterType.getCluster(getAccountValueFromJson(MiddlewareConstant.MW_PLATFORM_CLUSTER));
        if (mClusterType == null)
            mClusterType = ClusterType.BULK;

        mMessageType         = MessageType.getMessageType(getAccountValueFromJson(MiddlewareConstant.MW_MSG_TYPE));
        mMessagePriority     = MessagePriority.getMessagePriority(getAccountValueFromJson(MiddlewareConstant.MW_SMS_PRIORITY));
        isDomoesticTraReject = CommonUtility.isTrue(CommonUtility.nullCheck(getAccountValueFromJson(MiddlewareConstant.MW_DOMESTIC_TRA_BLOCKOUT_REJECT), true));
    }

    public ClusterType getClusterType()
    {
        return mClusterType;
    }

    public MessageType getMessageType()
    {
        return mMessageType;
    }

    public MessagePriority getMessagePriority()
    {
        return mMessagePriority;
    }

    public boolean isDomesticTraBlockoutReject()
    {
        return isDomoesticTraReject;
    }

    public String getSmppPassword()
    {
        return mUserInfo.getSmppPassword();
    }

    public AccountStatus getAccountStatus()
    {
        return mUserInfo.getAccountStatus();
    }

    public String getAllowedIps()
    {
        return getAccountValueFromJson(MiddlewareConstant.MW_IP_LIST);
    }

    public boolean isAllowedIp(
            String aHost)
    {
        return IPValidator.getInstance().isValidIP(getAccountValueFromJson(MiddlewareConstant.MW_IP_VALIDATION), mClientId, getAccountValueFromJson(MiddlewareConstant.MW_IP_LIST), aHost);
    }

    public boolean isSmppServiceEnabled()
    {
        return CommonUtility.isEnabled(getAccountValueFromJson(SmppUtilConstants.ACCOUNT_SMS_SMPP_SERVICE));
    }

    public boolean isAllowedBindType(
            String aBindName)
    {
        final List<String> lBindTypes = mSmppAccountInfo.getBindTypes(); // TRX

        if (!lBindTypes.isEmpty())
            return lBindTypes.contains(aBindName.toUpperCase());
        return false;
    }

    public int getMaxSpeedAllowed()
    {
        return mSmppAccountInfo.getMaxSpeed();
    }

    public boolean considerDefaultLengthAsDomesitic()
    {
        return CommonUtility.isEnabled(getAccountValueFromJson(MiddlewareConstant.MW_CONSIDER_DEFAULTLENGTH_AS_DOMESTIC));
    }

    public int getDndPreferences()
    {
        return CommonUtility.getInteger(getAccountValueFromJson(MiddlewareConstant.MW_DND_PREF), 0);
    }

    public boolean isDndRejectYN()
    {
        return CommonUtility.isEnabled(getAccountValueFromJson(MiddlewareConstant.MW_DND_REJECT_YN));
    }

    public boolean isIntlServiceAllowed()
    {
        return CommonUtility.isEnabled(getAccountValueFromJson(SmppUtilConstants.ACCOUNT_SMS_INTL_SERVICE_ALLOW));
    }

    public String getDltEntityId()
    {
        return mSmppAccountInfo.getDltEntityIdTag();
    }

    public String getDltTemplateId()
    {
        return mSmppAccountInfo.getDltTemplateIdTag();
    }

    public String getAccountTimeZone()
    {
        return getAccountValueFromJson(MiddlewareConstant.MW_TIME_ZONE);
    }

    public String getSmppCharSet()
    {
        return mSmppAccountInfo.getCharSet();
    }

    public boolean isDomesticSpecialSeriesAllow()
    {
        return CommonUtility.isEnabled(getAccountValueFromJson(MiddlewareConstant.MW_DOMESTIC_SPECIAL_SERIES_ALLOW));
    }

    public String getClientMidTag()
    {
        return mSmppAccountInfo.getClientMidTag();
    }

}