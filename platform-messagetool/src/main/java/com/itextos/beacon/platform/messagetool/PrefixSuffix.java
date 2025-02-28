package com.itextos.beacon.platform.messagetool;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.MessageConvertionUtility;

public class PrefixSuffix
{

    public static final String PREFIX_CHAR_BEFORE_ANY_SUFFIX = " "; // TODO this has to be taken from config_params.
    public static final String SUFFIX_CHAR_AFTER_ANY_PREFIX  = " "; // TODO this has to be taken from config_params.

    private String             prefix;
    private String             suffix;
    private final int          suffixLength;

    public PrefixSuffix(
            String aPrefix,
            String aSuffix,
            boolean aIsHex)
    {
        if (!"".equals(CommonUtility.nullCheck(aPrefix, true)))
            prefix = SUFFIX_CHAR_AFTER_ANY_PREFIX + aPrefix;

        if (!"".equals(CommonUtility.nullCheck(aSuffix, true)))
            suffix = PREFIX_CHAR_BEFORE_ANY_SUFFIX + aSuffix;

        prefix       = getConvertedString(prefix, aIsHex);
        suffix       = getConvertedString(suffix, aIsHex);
        suffixLength = calculateLength(aIsHex, suffix);
    }

    private static int calculateLength(
            boolean aIsHex,
            String aSuffix)
    {
        if (aIsHex)
            return aSuffix.length();

        return FCUtility.getMessageLength(aSuffix);
    }

    public String getPrefix()
    {
        return prefix;
    }

    public String getSuffix()
    {
        return suffix;
    }

    public int getSuffixLength()
    {
        return suffixLength;
    }

    private static String getConvertedString(
            String aValue,
            boolean aIsHex)
    {
        String temp = CommonUtility.nullCheck(aValue, true);

        if (!"".equals(temp) && aIsHex)
            temp = MessageConvertionUtility.convertString2HexString(temp);

        return temp;
    }

    @Override
    public String toString()
    {
        return "PrefixSuffix [prefix=" + prefix + ", suffix=" + suffix + ", suffixLength=" + suffixLength + "]";
    }

}