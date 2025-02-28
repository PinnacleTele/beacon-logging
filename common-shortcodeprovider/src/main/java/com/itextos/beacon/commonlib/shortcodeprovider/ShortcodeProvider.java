package com.itextos.beacon.commonlib.shortcodeprovider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.text.RandomStringGenerator;

import com.itextos.beacon.commonlib.constants.ShortcodeLength;
import com.itextos.beacon.commonlib.shortcodeprovider.operation.RedisOperation;

public class ShortcodeProvider
{

    private static final Log    log       = LogFactory.getLog(ShortcodeProvider.class);

    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = UPPERCASE.toLowerCase();
    private static final String NUMBERS   = "0123456789";
    private static final char[] ALL_CHARS = (UPPERCASE + LOWERCASE + NUMBERS).toCharArray();

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final ShortcodeProvider INSTANCE = new ShortcodeProvider();

    }

    public static ShortcodeProvider getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    RandomStringGenerator mGenerator = new RandomStringGenerator.Builder().selectFrom(ALL_CHARS).build();

    private ShortcodeProvider()
    {}

    public String getNextShortcode(
            ShortcodeLength aShortcodeLength)
    {
        final String shortCodeFromRedis = getRedisShortCode(aShortcodeLength);

        if (shortCodeFromRedis != null)
            return shortCodeFromRedis;

        log.error("<<<<<< SOMETHING IS NOT PROPER HERE >>>>>>. Unable to get the Short code from Redis. Getting Short code using the Apache's Randome String Generation.", new Exception());
        return mGenerator.generate(aShortcodeLength.getLength());
    }

    private static String getRedisShortCode(
            ShortcodeLength aShortcodeLength)
    {
        return ShortcodeLength.LENGTH_5 == aShortcodeLength ? RedisOperation.getNextCodeFromRedis(ShortcodeLength.LENGTH_5) : RedisOperation.getNextCodeFromRedis(ShortcodeLength.LENGTH_6);
    }

}