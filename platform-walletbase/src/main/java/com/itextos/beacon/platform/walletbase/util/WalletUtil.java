package com.itextos.beacon.platform.walletbase.util;

import com.itextos.beacon.commonlib.message.MessageRequest;

public final class WalletUtil
{

    private static final double DEFAULT_VALUE = 0d;

    private WalletUtil()
    {}

    public static void resetWalletInfo(
            MessageRequest aMessageRequest)
    {
        aMessageRequest.setBillingSmsRate(DEFAULT_VALUE);
        aMessageRequest.setBillingAddFixedRate(DEFAULT_VALUE);
        aMessageRequest.setBaseSmsRate(DEFAULT_VALUE);
        aMessageRequest.setBaseAddFixedRate(DEFAULT_VALUE);
        aMessageRequest.setRefSmsRate(DEFAULT_VALUE);
        aMessageRequest.setRefAddFixedRate(DEFAULT_VALUE);
        aMessageRequest.setBillingExchangeRate(DEFAULT_VALUE);
        aMessageRequest.setRefExchangeRate(DEFAULT_VALUE);
    }

}
