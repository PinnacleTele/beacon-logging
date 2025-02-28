package com.itextos.beacon.platform.walletprocess;

import java.util.List;
import java.util.Map;

public class WalletUpdateProcessor
{

    private WalletUpdateProcessor()
    {}

    // public static boolean addWalletAmount(
    // WalletUpdateInput aWalletInput)
    // {
    // return RedisProcess.addWalletAmount(aWalletInput);
    // }
    //
    // public static boolean addWalletAmount(
    // List<WalletUpdateInput> aWalletInputList)
    // {
    // return RedisProcess.addWalletAmount(aWalletInputList);
    // }

    public static double getWalletBalance(
            String aClientId)
    {
        return RedisProcess.getWalletBalance(aClientId);
    }
    //
    // public static Map<String, Double> getWalletBalance(
    // List<String> aClientIdList)
    // {
    // return RedisProcess.getWalletBalance(aClientIdList);
    // }

    public static Map<String, Double> getWalletBalance(
            List<String> aCliIds)
    {
        return RedisProcess.getWalletBalance(aCliIds);
    }

}