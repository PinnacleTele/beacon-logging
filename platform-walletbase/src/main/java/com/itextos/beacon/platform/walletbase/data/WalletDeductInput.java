package com.itextos.beacon.platform.walletbase.data;

public class WalletDeductInput
        extends
        WalletInput
{

    private static final long serialVersionUID = 907644809913024848L;

    public WalletDeductInput(
            String aClientId,
            String aFileId,
            String aBaseMessageId,
            String aMessageId,
            int aNoOfParts,
            double aSmsRate,
            double aDltRate,
            String aReason,
            boolean isIntl)
    {
        super(WalletProcessType.DEDUCT, aClientId, aFileId, aBaseMessageId, aMessageId, aNoOfParts, aSmsRate, aDltRate, aReason, isIntl);
    }

}
