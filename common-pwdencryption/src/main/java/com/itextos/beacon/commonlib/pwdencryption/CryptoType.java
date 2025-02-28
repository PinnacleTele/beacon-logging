package com.itextos.beacon.commonlib.pwdencryption;

import java.util.HashMap;
import java.util.Map;

public enum CryptoType
{

    ENCODE("encode", 1),
    ENCRYPTION_AES_256("aes256", 2),
    HASHING_BCRYPT("bcrypt", 3),
    EMPTY("empty", 4);

    private String key;
    private int    type;

    CryptoType(
            String aKey,
            int aType)
    {
        key  = aKey;
        type = aType;
    }

    private static final Map<String, CryptoType>  allTypesByName = new HashMap<>();
    private static final Map<Integer, CryptoType> allTypesByType = new HashMap<>();

    static
    {
        final CryptoType[] lValues = CryptoType.values();

        for (final CryptoType ct : lValues)
            allTypesByName.put(ct.key, ct);

        for (final CryptoType ct : lValues)
            allTypesByType.put(ct.type, ct);
    }

    public static CryptoType getCryptoType(
            String aKey)
    {
        return allTypesByName.get(aKey);
    }

    public static CryptoType getCryptoType(
            int aType)
    {
        return allTypesByType.get(aType);
    }

}