package com.itextos.beacon.platform.promokannelrediscleaner.process.redisprocess;

import java.util.List;

interface IRedisProcess
{

    List<String> getRedisKeys();

    boolean process();

}
