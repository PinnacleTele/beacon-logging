package com.itextos.beacon.inmemdata.account;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.itextos.beacon.commonlib.constants.AccountStatus;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.RedisKeys;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemdata.account.dao.AccountInfo;
import com.itextos.beacon.inmemdata.redis.RedisHandler;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;

public class ClientAccountDetails
{

    private static Log               log                    = LogFactory.getLog(ClientAccountDetails.class);

    private static final int         INVALID_ACCOUNT_STATUS = -1;
    private static final AccountInfo CLIENT_ACCOUNT_INFO    = (AccountInfo) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.ACCOUNT_INFO);
    public static final UserInfo     INVALID_USER           = new UserInfo(null, null, null, null, INVALID_ACCOUNT_STATUS);

    private ClientAccountDetails()
    {}

    // public static UserInfo getUserDetailsByUsername(
    // String aUsername)
    // {
    // final UserInfo userInfo = CLIENT_ACCOUNT_INFO.getUserByUser(aUsername);
    // if (userInfo != null)
    // return validateUser(userInfo);
    // return INVALID_USER;
    // }

    public static UserInfo getUserDetailsForSmpp(
            String aUsername,
            String aPassword)
    {
        aUsername = CommonUtility.nullCheck(aUsername, true);

        if (aUsername.isBlank())
            return INVALID_USER;

        final UserInfo userInfo = CLIENT_ACCOUNT_INFO.getUserByUser(aUsername.toLowerCase());
        if ((userInfo != null) && userInfo.getSmppPassword().equals(aPassword))
            return validateUser(userInfo);

        return INVALID_USER;
    }

    public static UserInfo getUserDetailsByAccessKey(
            String aUsername,
            String aAccessKey)
    {
        aUsername = CommonUtility.nullCheck(aUsername, true);

        if (aUsername.isBlank())
            return INVALID_USER;

        final UserInfo userInfo = CLIENT_ACCOUNT_INFO.getUserByAccessKey(aAccessKey);
        if ((userInfo != null) && userInfo.getUserName().equals(aUsername.toLowerCase()))
            return validateUser(userInfo);
        return INVALID_USER;
    }

    public static UserInfo getUserDetailsByAccessKey(
            String aAccessKey)
    {
        final UserInfo userInfo = CLIENT_ACCOUNT_INFO.getUserByAccessKey(aAccessKey);
        if (userInfo != null)
            return validateUser(userInfo);
        return INVALID_USER;
    }

    public static UserInfo getUserDetailsByClientId(
            String aClientId)
    {
        final UserInfo userInfo = CLIENT_ACCOUNT_INFO.getUserByClientId(aClientId);
        if (userInfo != null)
            return validateUser(userInfo);
        return INVALID_USER;
    }

    private static UserInfo validateUser(
            UserInfo aUserInfo)
    {

        if (aUserInfo.getAccountStatus() == AccountStatus.ACTIVE)
        {
            final String jsonString = getJsonFromRedis(aUserInfo.getClientId());
            if (jsonString != null)
                aUserInfo.setAccountDetails(jsonString);
            else
                aUserInfo.setStatus(INVALID_ACCOUNT_STATUS);
        }
        return aUserInfo;
    }

    private static String getJsonFromRedis(
            String aClientId)
    {
        Map<String, String> lMapResult = null;

        try
        {
            lMapResult = RedisHandler.getMapForKey(Component.ACCOUNT_SYNC, RedisKeys.CLIENTINFO_BY_CLID.getKey() + aClientId);

            if ((lMapResult != null) && !lMapResult.isEmpty())
            {
                if (log.isDebugEnabled())
                    log.debug("Account Details from Redis Map : - " + lMapResult);
            }
            else
            {
                lMapResult = AccountInfo.getAccountInfo(aClientId);
                if (log.isDebugEnabled())
                    log.debug("Account Details from DataBase Map : - " + lMapResult);
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while get the Account Info from Redis ", e);
            lMapResult = AccountInfo.getAccountInfo(aClientId);
        }
        return convertMspToJsonString(lMapResult);
    }

    private static String convertMspToJsonString(
            Map<String, String> aAccountInfo)
    {
        if ((aAccountInfo == null) || aAccountInfo.isEmpty())
            return null;
        final Gson gson = new Gson();
        return gson.toJson(aAccountInfo, Map.class);
    }

}