package com.itextos.beacon.platform.decimalutility;

import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DecimalRoundingMode;
import com.itextos.beacon.commonlib.utility.DecimalUtility;
import com.itextos.beacon.inmemory.configvalues.ApplicationConfiguration;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;

public final class PlatformDecimalUtil
{

    private PlatformDecimalUtil()
    {}

    public static double getRoundedValueForProcess(
            double aDouble)
    {
        // TODO we may need to change this later if required.
        // final DecimalRoundingMode toUse = getPlatformRoundingMode();

        final DecimalRoundingMode toUse = DecimalRoundingMode.UP;
        return DecimalUtility.round(aDouble, getPlatformDecimalPrecisionDataProcess(), toUse);
    }

    public static int getPlatformDecimalPrecisionDataProcess()
    {
        return CommonUtility.getInteger(getAppConfigValueAsString(ConfigParamConstants.PLATFORM_DECIMAL_PRCECISION_DATA_PROCESS), DecimalUtility.DEFAULT_DOUBLE_PRECISION_DIGITS_FOR_PROCESS);
    }

    public static int getPlatformDecimalPrecisionDataStore()
    {
        return CommonUtility.getInteger(getAppConfigValueAsString(ConfigParamConstants.PLATFORM_DECIMAL_PRCECISION_DATA_STORE), DecimalUtility.DEFAULT_DOUBLE_PRECISION_DIGITS_FOR_DATA_STORE);
    }

    public static DecimalRoundingMode getPlatformRoundingMode()
    {
        final String aValue   = getAppConfigValueAsString(ConfigParamConstants.PLATFORM_DECIMAL_PRCECISION_ROUND_MODE);
        final int    intValue = CommonUtility.getInteger(aValue, DecimalRoundingMode.HALF_UP.ordinal());

        switch (intValue)
        {
            case 1:
                return DecimalRoundingMode.DOWN;

            case 2:
                return DecimalRoundingMode.CEILING;

            case 3:
                return DecimalRoundingMode.FLOOR;

            case 4:
                return DecimalRoundingMode.HALF_UP;

            case 5:
                return DecimalRoundingMode.HALF_DOWN;

            case 6:
                return DecimalRoundingMode.HALF_EVEN;

            case 0:
            default:
                return DecimalRoundingMode.UP;
        }
    }

    private static String getAppConfigValueAsString(
            ConfigParamConstants aKey)
    {
        if (aKey == null)
            return null;
        final ApplicationConfiguration lAppConfiguration = (ApplicationConfiguration) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.APPLICATION_CONFIG);
        return lAppConfiguration.getConfigValue(aKey.getKey());
    }

}