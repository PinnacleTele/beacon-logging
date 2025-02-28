package com.itextos.beacon.platform.walletbase.data;

public class WalletRefundInput
        extends
        WalletInput
{

    private static final long serialVersionUID = 978882664079145325L;

    public WalletRefundInput(
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
        super(WalletProcessType.REFUND, aClientId, aFileId, aBaseMessageId, aMessageId, aNoOfParts, aSmsRate, aDltRate, aReason, isIntl);
    }

}
