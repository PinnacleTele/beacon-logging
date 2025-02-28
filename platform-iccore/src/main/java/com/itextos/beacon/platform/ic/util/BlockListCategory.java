package com.itextos.beacon.platform.ic.util;

import java.util.HashMap;
import java.util.Map;

import com.itextos.beacon.commonlib.constants.ItextosEnum;

public enum BlockListCategory
        implements
        ItextosEnum
{

    NONE("0"),
    GLOBAL("1"),
    USER("2"),
    USER_GLOBAL("3"),
    USER_ADMIN_SUPERADMIN("4"),
    USER_ADMIN_SUPERADMIN_GLOBAL("5"),
    USER_PARENT_GRANDPARENT("6"),
    USER_PARENT_GRANDPARENT_GLOBAL("7");

    private final String key;

    BlockListCategory(
            String aKey)
    {
        key = aKey;
    }

    @Override
    public String getKey()
    {
        return key;
    }

    private static final Map<String, BlockListCategory> mAllTypes = new HashMap<>();

    static
    {
        final BlockListCategory[] values = BlockListCategory.values();

        for (final BlockListCategory ip : values)
            mAllTypes.put(ip.key, ip);
    }

    public static BlockListCategory getBlockListCategory(
            String aBlockListCategory)
    {
        return mAllTypes.get(aBlockListCategory);
    }

}
