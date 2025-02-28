package com.itextos.beacon.platform.prepaiddata;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.AccountStatus;
import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.RedisKeys;
import com.itextos.beacon.commonlib.redisconnectionprovider.RedisConnectionProvider;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemdata.account.ClientAccountDetails;
import com.itextos.beacon.inmemdata.account.UserInfo;
import com.itextos.beacon.inmemdata.account.dao.AccountInfo;
import com.itextos.beacon.inmemdata.redis.RedisHandler;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.platform.decimalutility.PlatformDecimalUtil;
import com.itextos.beacon.platform.prepaiddata.inmemory.CurrencyMaster;

import redis.clients.jedis.Jedis;

public class ReadRedisData
{

    private static final Log    log              = LogFactory.getLog(ReadRedisData.class);
    public static final String  LOW_BALANCE      = "LOW";
    public static final String  NO_USER          = "NOUSER";
    public static final String  NON_ACTIVE       = "NONACTIVE";
    public static final String  OTHER_BALANCE    = "OTHERS";

    private static final String PREPAID_KEY      = "wallet:amount";
    private static final String BILLING_CURRENCY = "billing_currency";
    private static final int    MIN_LOW_BALANCE  = 100;
    private final String        mFormFeedString;

    public ReadRedisData(
            String aFormFeedString)
    {
        mFormFeedString = CommonUtility.nullCheck(aFormFeedString, true);
    }

    public Map<String, Set<PrepaidData>> getData()
    {
        if (log.isDebugEnabled())
            log.debug("String from Form : '" + mFormFeedString + "'");

        final Set<PrepaidData>    lowBalance        = new TreeSet<>();
        final Set<PrepaidData>    otherBalance      = new TreeSet<>();
        final Set<PrepaidData>    noUserData        = new TreeSet<>();
        final Set<PrepaidData>    nonActiveUserData = new TreeSet<>();

        final CurrencyMaster      currencyMaster    = (CurrencyMaster) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.CURRENCY_MASTER);

        final Map<String, String> lHgetAll          = getPrepaidData();

        if (log.isDebugEnabled())
            log.debug("Redis Data Size '" + (lHgetAll != null ? lHgetAll.size() : "null") + "'");

        if ((lHgetAll == null) || lHgetAll.isEmpty())
        {
            log.fatal("Something is not correct. Unable to get data from Redis.");
            return new HashMap<>();
        }

        for (final Entry<String, String> entry : lHgetAll.entrySet())
        {
            final String        cliId                  = entry.getKey();
            final String        balAmount              = entry.getValue();
            UserInfo            lUserDetailsByClientId = null;
            Map<String, String> lAccountDetails        = null;

            try
            {
                final double lRoundedValueForProcess = PlatformDecimalUtil.getRoundedValueForProcess(CommonUtility.getDouble(balAmount));
                lUserDetailsByClientId = ClientAccountDetails.getUserDetailsByClientId(cliId);
                lAccountDetails        = getAccountInfoFromRedis(cliId);

                PrepaidData   data            = null;
                final boolean isUserAvailable = lUserDetailsByClientId.getAccountStatus() != AccountStatus.INVALID;

                if (isUserAvailable)
                    data = new PrepaidData(cliId, lAccountDetails.get("user"), (currencyMaster != null ? currencyMaster.getCurrencyInfo(lAccountDetails.get(BILLING_CURRENCY)) : null),
                            lRoundedValueForProcess, lUserDetailsByClientId.getAccountStatus());
                else
                    data = new PrepaidData(cliId, cliId, null, lRoundedValueForProcess, null);

                // if (!isUserAvailable)
                // {
                // noUserData.add(data);
                // continue;
                // }

                if (isUserAvailable && (lUserDetailsByClientId.getClientId().contains(mFormFeedString) || lUserDetailsByClientId.getUserName().contains(mFormFeedString)))
                {
                    if ((lUserDetailsByClientId.getAccountStatus() == AccountStatus.EXPIRY) || (lUserDetailsByClientId.getAccountStatus() == AccountStatus.DEACTIVATED))
                        continue;

                    if ((lUserDetailsByClientId.getAccountStatus() != AccountStatus.ACTIVE))
                    {
                        nonActiveUserData.add(data);
                        continue;
                    }

                    if (lRoundedValueForProcess < MIN_LOW_BALANCE)
                        lowBalance.add(data);
                    else
                        otherBalance.add(data);
                }
            }
            catch (final Exception e)
            {
                log.error("Exception while processing for the client id '" + cliId + "' User Details By ClientId '" + lUserDetailsByClientId + "' Account Details '" + lAccountDetails + "'", e);
            }
        }

        final Map<String, Set<PrepaidData>> returnValue = new HashMap<>();
        returnValue.put(NO_USER, noUserData);
        returnValue.put(NON_ACTIVE, nonActiveUserData);
        returnValue.put(LOW_BALANCE, lowBalance);
        returnValue.put(OTHER_BALANCE, otherBalance);
        return returnValue;
    }

    private static Map<String, String> getPrepaidData()
    {

        try (
                final Jedis jedis = RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, Component.WALLET_CHK, 1);)
        {
            return jedis.hgetAll(PREPAID_KEY);
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the data from Redis.", e);
        }
        return null;
    }

    private static Map<String, String> getAccountInfoFromRedis(
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
        return lMapResult;
    }

    public static void main2(
            String[] args)
    {
        ClientAccountDetails.getUserDetailsByClientId("1234");

        final Scanner sc = new Scanner(System.in);
        String        s  = null;
        System.out.println("Enter username / cli id");

        while (!(s = sc.next()).contentEquals("exit"))
        {
            s = "";
            final ReadRedisData                 rrd   = new ReadRedisData(s);
            final Map<String, Set<PrepaidData>> lData = rrd.getData();

            printLow(lData.get(NO_USER), NO_USER);
            printLow(lData.get(NON_ACTIVE), NON_ACTIVE);
            printLow(lData.get(LOW_BALANCE), LOW_BALANCE);
            printLow(lData.get(OTHER_BALANCE), OTHER_BALANCE);

            System.out.println("Enter username / cli id");
        }
    }

    public static void main(
            String[] args)
    {

        try
        {
            final ReadRedisData                 rrd   = new ReadRedisData("");
            final Map<String, Set<PrepaidData>> lData = rrd.getData();
            System.out.println(lData);
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

    private static void printLow(
            Set<PrepaidData> aSet,
            String aString)
    {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> " + aString);
        for (final PrepaidData data : aSet)
            System.out.println(data);
    }

}