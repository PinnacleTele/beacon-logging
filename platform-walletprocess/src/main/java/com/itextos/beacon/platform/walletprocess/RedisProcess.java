package com.itextos.beacon.platform.walletprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.redisconnectionprovider.RedisConnectionProvider;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.platform.decimalutility.PlatformDecimalUtil;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

class RedisProcess
{

    private static final String WALLET_AMOUNT = "wallet:amount";

    private RedisProcess()
    {}

    static double addOrDeduct(
            String aClientId,
            double aAmount)
    {

        try (
                Jedis jedis = getConnection())
        {
            return jedis.hincrByFloat(WALLET_AMOUNT, aClientId, PlatformDecimalUtil.getRoundedValueForProcess(aAmount));
        }
    }

    private static Jedis getConnection()
    {
        return RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, Component.WALLET_CHK, 1);
    }

    // public static boolean addWalletAmount(
    // WalletUpdateInput aWalletInput)
    // {
    // if (aWalletInput == null)
    // return false;
    //
    // try (
    // Jedis jedis = getConnection())
    // {
    // jedis.hincrByFloat(WALLET_AMOUNT, aWalletInput.getClientId(),
    // PlatformDecimalUtil.getRoundedValueForProcess(aWalletInput.getAmount()));
    // return true;
    // }
    // catch (final Exception e)
    // {
    // throw e;
    // }
    // }

    // public static boolean addWalletAmount(
    // List<WalletUpdateInput> aWalletInputList)
    // {
    // if ((aWalletInputList == null) || aWalletInputList.isEmpty())
    // return false;
    //
    // try (
    // Jedis jedis = getConnection();
    // Pipeline pipe = jedis.pipelined())
    // {
    // Map<String, String> lCurrentAmount = null;
    //
    // try
    // {
    // lCurrentAmount = getCurrentAmount(pipe, aWalletInputList);
    // }
    // catch (final Exception e)
    // {
    // throw e;
    // }
    //
    // try
    // {
    // for (final WalletUpdateInput walletInput : aWalletInputList)
    // pipe.hincrByFloat(WALLET_AMOUNT, walletInput.getClientId(),
    // PlatformDecimalUtil.getRoundedValueForProcess(walletInput.getAmount()));
    // pipe.sync();
    // return true;
    // }
    // catch (final Exception e)
    // {
    // resetOldAmount(lCurrentAmount);
    // return false;
    // }
    // }
    // catch (final Exception e)
    // {
    // throw e;
    // }
    // }

    // private static void resetOldAmount(
    // Map<String, String> aCurrentAmount)
    // {
    //
    // try (
    // Jedis jedis = getConnection();)
    // {
    // jedis.hmset(WALLET_AMOUNT, aCurrentAmount);
    // }
    // catch (final Exception e)
    // {
    // throw e;
    // }
    // }

    // private static Map<String, String> getCurrentAmount(
    // Pipeline aPipe,
    // List<WalletUpdateInput> aWalletInputList)
    // {
    // final Map<String, Response<String>> walletAmount = new HashMap<>();
    //
    // for (final WalletUpdateInput walletInput : aWalletInputList)
    // {
    // final Response<String> lHget = aPipe.hget(WALLET_AMOUNT,
    // walletInput.getClientId());
    // walletAmount.put(walletInput.getClientId(), lHget);
    // }
    //
    // aPipe.sync();
    //
    // final Map<String, String> returnValue = new HashMap<>();
    //
    // for (final Entry<String, Response<String>> entry : walletAmount.entrySet())
    // returnValue.put(entry.getKey(), entry.getValue().get());
    //
    // return returnValue;
    // }

    public static Map<String, Double> getWalletBalance(
            List<String> aClientIdList)
    {
        final Map<String, Double> returnValue = new HashMap<>();

        if ((aClientIdList == null) || aClientIdList.isEmpty())
            return returnValue;

        try (
                Jedis jedis = getConnection();
                Pipeline pipe = jedis.pipelined())
        {
            final Map<String, Response<String>> walletAmount = new HashMap<>();

            for (final String clientId : aClientIdList)
            {
                final Response<String> lHget = pipe.hget(WALLET_AMOUNT, clientId);
                walletAmount.put(clientId, lHget);
            }

            pipe.sync();

            for (final Entry<String, Response<String>> entry : walletAmount.entrySet())
                returnValue.put(entry.getKey(), PlatformDecimalUtil.getRoundedValueForProcess(CommonUtility.getDouble(entry.getValue().get(), -99999d)));
        }
        catch (final Exception e)
        {
            throw e;
        }
        return returnValue;
    }

    public static double getWalletBalance(
            String aClientId)
    {
        double returnValue = -1;

        if ((aClientId == null) || aClientId.isEmpty())
            return returnValue;

        try (
                Jedis jedis = getConnection();)
        {
            final String bal = jedis.hget(WALLET_AMOUNT, aClientId);
            returnValue = PlatformDecimalUtil.getRoundedValueForProcess(CommonUtility.getDouble(bal, 0));
        }
        catch (final Exception e)
        {
            throw e;
        }
        return returnValue;
    }

}