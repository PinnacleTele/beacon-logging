package com.itextos.beacon.platform.walletprocess;

import java.util.HashMap;
import java.util.Map;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.platform.walletbase.data.WalletInput;
import com.itextos.beacon.platform.walletbase.data.WalletResult;

public class WalletTestMain
{

    public static void main(
            String[] args)
    {
    //    walletDeduct();
        refundWallet();
        // updateWallet();
        // updateWalletList();
        // getWalletAmount();
        // getWalletAmountList();
    }

    private static void refundWallet()
    {

        try
        {
            final Map<String, String> mapMessage = new HashMap<>();// Sample message

            mapMessage.put("sms_rate", "1.00");
            mapMessage.put("dlt_rate", "2.75");

            final double smsRate = CommonUtility.getDouble(mapMessage.get("sms_rate"), -999);
            final double dltRate = CommonUtility.getDouble(mapMessage.get("dlt_rate"), -999);

            if ((smsRate == -999) || (dltRate == -999))
                throw new RuntimeException("");

            final int         middleWareRejectionMultipart = 3;

            final WalletInput lWalletInput1                = WalletInput.getRefundInput("6000000200000000", "fileid", "baseMessageid", "messageid", middleWareRejectionMultipart, smsRate, dltRate,
                    "Platform Rejection", false);
            WalletDeductRefundProcessor.returnAmountToWallet(lWalletInput1);

            final int         failedDn      = 1;
            // the DLT rate is ZERO here.
            final WalletInput lWalletInput2 = WalletInput.getRefundInput("6000000200000000", "fileid", "baseMessageid", "messageid", failedDn, smsRate, 0, "DN Rejection", false);
            WalletDeductRefundProcessor.returnAmountToWallet(lWalletInput2);
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

    private static void walletDeduct()
    {
        for (int index = 1; index < 10; index++)
            try
            {
                final WalletInput         lWalletInput        = WalletInput.getDeductInput("6000000200000000", "fileid", "baseMessageid", "messageid", 3, 0.75d, 2.25d, "Msg Processing", false);
                final WalletResult        lDeductWalletForSMS = null;;//WalletDeductRefundProcessor.deductWalletForSMS(lWalletInput);
                final Map<String, String> mapMessage          = new HashMap<>();// Sample message

                if (lDeductWalletForSMS.isSuccess())
                {
                    mapMessage.put("sms_rate", "" + lDeductWalletForSMS.getSmsRate());
                    mapMessage.put("dlt_rate", "" + lDeductWalletForSMS.getDltRate());
                }
                else
                    System.err.println("Not enough balance in Wallet. Rejecting the message.");
            }
            catch (final Exception e)
            {
                retryDeduction();
            }
    }

    private static void retryDeduction()
    {}

    // private static void updateWallet()
    // {
    // final WalletUpdateInput wdiUpdateInput = new
    // WalletUpdateInput("6000000200000000", 2500000);
    // WalletUpdateProcessor.addWalletAmount(wdiUpdateInput);
    //
    // final double lWalletBalance =
    // WalletUpdateProcessor.getWalletBalance("6000000200000000");
    // System.out.println("Balance for 6000000200000000 " + lWalletBalance);
    // }

    // private static void updateWalletList()
    // {
    // final WalletUpdateInput wdiUpdateInput1 = new
    // WalletUpdateInput("6000000200000001", 2500000);
    // final WalletUpdateInput wdiUpdateInput2 = new
    // WalletUpdateInput("6000000200000002", 3500000);
    // final WalletUpdateInput wdiUpdateInput3 = new
    // WalletUpdateInput("6000000200000003", 4500000);
    //
    // final List<WalletUpdateInput> updates = new ArrayList<>();
    // updates.add(wdiUpdateInput1);
    // updates.add(wdiUpdateInput2);
    // updates.add(wdiUpdateInput3);
    //
    // final boolean lAddWalletAmount =
    // WalletUpdateProcessor.addWalletAmount(updates);
    //
    // if (lAddWalletAmount)
    // {
    // final List<String> clientIds = new ArrayList<>();
    // clientIds.add("6000000200000001");
    // clientIds.add("6000000200000002");
    // clientIds.add("6000000200000003");
    //
    // final Map<String, Double> lWalletBalance =
    // WalletUpdateProcessor.getWalletBalance(clientIds);
    //
    // for (final Entry<String, Double> entry : lWalletBalance.entrySet())
    // System.out.println("Balance " + entry.getKey() + " is " + entry.getValue());
    // }
    // else
    // System.err.println("Update amount failed. Reset to the old amount");
    // }

    // private static void getWalletAmount()
    // {
    // final double lWalletBalance =
    // WalletUpdateProcessor.getWalletBalance("6000000200000001");
    // System.out.println("Balance for 6000000200000001 is " + lWalletBalance);
    // }

    // private static void getWalletAmountList()
    // {
    // final List<String> clientIds = new ArrayList<>();
    // clientIds.add("6000000200000001");
    // clientIds.add("6000000200000002");
    // clientIds.add("6000000200000003");
    //
    // final Map<String, Double> lWalletBalance =
    // WalletUpdateProcessor.getWalletBalance(clientIds);
    // for (final Entry<String, Double> entry : lWalletBalance.entrySet())
    // System.out.println("Balance " + entry.getKey() + " is " + entry.getValue());
    // }

}
