package com.itextos.beacon.inmemory.spamcheck.util;

import java.util.HashMap;
import java.util.Map;

import com.itextos.beacon.commonlib.constants.ItextosEnum;

public enum Action
        implements
        ItextosEnum
{

    NONE("0"),
    BLOCK_MSG("1"),
    SPAM_LOG("2");

    private final String key;

    Action(
            String aKey)
    {
        key = aKey;
    }

    private static final Map<String, Action> mAllTypes = new HashMap<>();

    static
    {
        final Action[] lValues = Action.values();

        for (final Action ip : lValues)
            mAllTypes.put(ip.key, ip);
    }

    @Override
    public String getKey()
    {
        return key;
    }

    public static Action getSpamAction(
            String aKey)
    {
        return mAllTypes.get(aKey);
    }

}
