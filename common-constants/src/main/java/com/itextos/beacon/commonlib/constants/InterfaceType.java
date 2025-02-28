package com.itextos.beacon.commonlib.constants;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum InterfaceType
        implements
        ItextosEnum
{

    GUI("gui", InterfaceGroup.UI),
    FTP("ftp", InterfaceGroup.FTP),
    SMPP("smpp", InterfaceGroup.SMPP),
    HTTP_JAPI("http_japi", InterfaceGroup.API),
    CLOUD_API("cloud_api", InterfaceGroup.API),
    MIGRATION_API("migration_api", InterfaceGroup.API),
    ;

    InterfaceType(
            String aKey,
            InterfaceGroup aInterfaceGroup)
    {
        key            = aKey;
        interfaceGroup = aInterfaceGroup;
    }

    private final String                                          key;
    private final InterfaceGroup                                  interfaceGroup;
    private static final Map<String, InterfaceType>               mAllTypes        = new HashMap<>();
    private static final Map<InterfaceGroup, List<InterfaceType>> mGroupBasedTypes = new EnumMap<>(InterfaceGroup.class);

    static
    {
        final InterfaceType[] values = InterfaceType.values();

        for (final InterfaceType ip : values)
            mAllTypes.put(ip.key, ip);

        final InterfaceType[] groupValues = InterfaceType.values();

        for (final InterfaceType ip : groupValues)
        {
            final List<InterfaceType> list = mGroupBasedTypes.computeIfAbsent(ip.getGroup(), k -> new ArrayList<>());
            list.add(ip);
        }
    }

    @Override
    public String getKey()
    {
        return key;
    }

    public InterfaceGroup getGroup()
    {
        return interfaceGroup;
    }

    public static InterfaceType getType(
            String aKey)
    {
        return mAllTypes.get(aKey);
    }

    public static List<InterfaceType> getTypeBasedonGroup(
            String aInterfaceGroupKey)
    {
        return getTypeBasedonGroup(InterfaceGroup.getType(aInterfaceGroupKey));
    }

    public static List<InterfaceType> getTypeBasedonGroup(
            InterfaceGroup aInterfaceGroup)
    {
        if (aInterfaceGroup == null)
            return null;

        return mGroupBasedTypes.get(aInterfaceGroup);
    }

}