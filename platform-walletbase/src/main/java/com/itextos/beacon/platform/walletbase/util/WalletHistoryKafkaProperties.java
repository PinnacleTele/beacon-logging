package com.itextos.beacon.platform.walletbase.util;

import org.apache.commons.configuration.PropertiesConfiguration;

import com.itextos.beacon.commonlib.commonpropertyloader.PropertiesPath;
import com.itextos.beacon.commonlib.commonpropertyloader.PropertyLoader;
import com.itextos.beacon.commonlib.utility.CommonUtility;

public class WalletHistoryKafkaProperties
{

    private static final String PROPS_KEY_WALLET_HISTORY_LOG_REQUIRED = "wallet.history.log.required";

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final WalletHistoryKafkaProperties INSTANCE = new WalletHistoryKafkaProperties();

    }

    public static WalletHistoryKafkaProperties getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final PropertiesConfiguration mWalletHistoryProperties = PropertyLoader.getInstance().getPropertiesConfiguration(PropertiesPath.WALLET_HISTORY_PROPERTIES, true);

    private WalletHistoryKafkaProperties()
    {}

    public boolean isWalletHistoryLogRequired()
    {
        return CommonUtility.isEnabled(mWalletHistoryProperties.getString(PROPS_KEY_WALLET_HISTORY_LOG_REQUIRED, "Y"));
    }

}