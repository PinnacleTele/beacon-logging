package com.itextos.beacon.smpp.interfaces.workers;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import com.itextos.beacon.commonlib.utility.CommonUtility;

public class CustomerRedisHeartBeatData
{

    private static class SingletonHolder
    {

        static final CustomerRedisHeartBeatData INSTANCE = new CustomerRedisHeartBeatData();

    }

    public static CustomerRedisHeartBeatData getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final ConcurrentHashMap<String, Date> hbMap = new ConcurrentHashMap<>();

    public synchronized void addHeartBeat(
            String aThreadName,
            String aUserName)
    {
        hbMap.put(CommonUtility.combine(aThreadName, aUserName), new Date());
    }

    public void remove(
            String aThreadName,
            String aUserName)
    {
        hbMap.remove(CommonUtility.combine(aThreadName, aUserName));
    }

}