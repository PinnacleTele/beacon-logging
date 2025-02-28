package com.itextos.beacon.platform.bwc.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.itextos.beacon.commonlib.constants.BillType;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.PlatformStatusCode;
import com.itextos.beacon.commonlib.constants.exception.InternationalSMSRateNotAvailableRuntimeException;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemdata.account.ClientAccountDetails;
import com.itextos.beacon.inmemdata.account.UserInfo;
import com.itextos.beacon.inmemory.configvalues.ApplicationConfiguration;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.platform.intlprice.CalculateBillingPrice;
import com.itextos.beacon.platform.intlprice.CalculateIntlBillingPrice;
import com.itextos.beacon.platform.intlprice.CurrencyUtil;
import com.itextos.beacon.platform.msgflowutil.util.PlatformUtil;
import com.itextos.beacon.platform.walletbase.data.WalletDeductInput;
import com.itextos.beacon.platform.walletbase.data.WalletInput;
import com.itextos.beacon.platform.walletbase.data.WalletRefundInput;
import com.itextos.beacon.platform.walletbase.data.WalletResult;
import com.itextos.beacon.platform.walletprocess.WalletDeductRefundProcessor;

public class WalletUtil
{

    private static final Log log = LogFactory.getLog(WalletUtil.class);

    private WalletUtil()
    {}

    public static void prepaidDeduct(
            MessageRequest aMessageRequest)
    {
        final String  lClientId      = aMessageRequest.getClientId();
        final String  lFileId        = aMessageRequest.getFileId();
        final String  lBaseMsgId     = aMessageRequest.getBaseMessageId();
        final String  lMsgId         = aMessageRequest.getBaseMessageId();
        int           lTotalMsgParts = aMessageRequest.getMessageTotalParts();
        final double  lSmsRate       = aMessageRequest.getBillingSmsRate();
        final double  lDltRate       = aMessageRequest.getBillingAddFixedRate();
        final boolean isIntl         = aMessageRequest.isIsIntl();

        if (lTotalMsgParts == 0)
            lTotalMsgParts = 1;

        if (log.isDebugEnabled())
            log.debug("Client Id: '" + lClientId + "', Total Parts:'" + lTotalMsgParts + "',Billing SmsRate:'" + lSmsRate + "', Billing Fixed Rate:'" + lDltRate + "'");

        boolean hasWallectDeductStatus = false;
        boolean hasWallectBallance     = false;
        boolean lStatus                = false;
        while (!lStatus)
            try
            {
                final WalletDeductInput lWalletInput        = WalletInput.getDeductInput(lClientId, lFileId, lBaseMsgId, lMsgId, lTotalMsgParts, lSmsRate, lDltRate, "", isIntl);
                final WalletResult      lDeductWalletForSMS = WalletDeductRefundProcessor.deductWalletForSMS(aMessageRequest,lWalletInput);

                if (lDeductWalletForSMS.isSuccess())
                {
                    hasWallectDeductStatus = true;

                    if (log.isDebugEnabled())
                        log.debug("Wallet Deduct status : " + hasWallectDeductStatus);

                    aMessageRequest.setIsWalletDeduct(true);
                }
                else
                    hasWallectBallance = true;

                lStatus = true;
            }
            catch (final ItextosException ite)
            {
                log.error(ite.getMessage());
                hasWallectDeductStatus = false;
                lStatus                = true;
            }
            catch (final Exception e)
            {

                try
                {
                    log.fatal(" Sleeping 2 sec since redis is not reachable.. ");
                    Thread.sleep(2000);
                }
                catch (final InterruptedException e2)
                {}
            }

        if (hasWallectDeductStatus)
            BWCProducer.sendToCarrierHandover(aMessageRequest);
        else
        {
            resetWalletInfo(aMessageRequest);

            if (hasWallectBallance)
                aMessageRequest.setSubOriginalStatusCode(PlatformStatusCode.INSUFFICIENT_WALLET_BALANCE.getStatusCode());
            else
                aMessageRequest.setSubOriginalStatusCode(PlatformStatusCode.PREPAID_CHECK_FAILED.getStatusCode());

            BWCProducer.sendToPlatformRejection(aMessageRequest);
        }
    }

    public static void prepaidRefund(
            MessageRequest aMessageRequest)
    {
        final boolean isCreditCheckEnabled = CommonUtility.isEnabled(aMessageRequest.getValue(MiddlewareConstant.MW_CREDIT_CHECK));

        if (log.isDebugEnabled())
            log.debug("Credit Check Enabled : " + isCreditCheckEnabled);

        if ((aMessageRequest.getBillType() == CommonUtility.getInteger(BillType.PRE_PAID.getKey())) || isCreditCheckEnabled)
        {
            if (log.isDebugEnabled())
                log.debug("Wallet refund calling ...");

            if (aMessageRequest.isWalletDeduct())
            {
                final String  lClientId      = aMessageRequest.getClientId();
                final String  lFileId        = aMessageRequest.getFileId();
                final String  lBaseMessageId = aMessageRequest.getBaseMessageId();
                final double  lSmsRate       = aMessageRequest.getBillingSmsRate();
                final double  lDltRate       = aMessageRequest.getBillingAddFixedRate();
                final String  lReason        = ""; // To Pricing change
                int           lTotalParts    = aMessageRequest.getMessageTotalParts();
                final boolean isIntl         = aMessageRequest.isIsIntl();

                if (lTotalParts == 0)
                    lTotalParts = 1;

                if (log.isDebugEnabled())
                    log.debug("Wallet Deduct Billing SMS Rate :  '" + lSmsRate + "', Billing Fixed Rate:'" + lDltRate + "'");

                doWalletRefund(lClientId, lFileId, lBaseMessageId, lBaseMessageId, lTotalParts, lSmsRate, lDltRate, lReason, isIntl);
            }
        }
    }

    public static boolean resetWalletInfo(
            MessageRequest aMessageRequest)
    {
        boolean          lStatus      = true;
        final JSONObject lUserDetails = getAccountInfo(aMessageRequest);

        if (lUserDetails != null)
        {
            final double lUpdatedSMSRate      = CommonUtility.getDouble(CommonUtility.nullCheck(lUserDetails.get(MiddlewareConstant.MW_BASE_SMS_RATE.getName())));
            final double lUpdatedAddFixedRate = CommonUtility.getDouble(CommonUtility.nullCheck(lUserDetails.get(MiddlewareConstant.MW_BASE_ADD_FIXED_RATE.getName())));

            if (log.isDebugEnabled())
                log.debug("Updated SMS Rate :" + lUpdatedSMSRate + ", DLT Rate :" + lUpdatedAddFixedRate);

            if (aMessageRequest.isIsIntl())
                lStatus = setIntlRates(aMessageRequest);
            else
            {
                aMessageRequest.setBaseSmsRate(lUpdatedSMSRate);
                aMessageRequest.setBaseAddFixedRate(lUpdatedAddFixedRate);
                lStatus = updatePriceInfo(aMessageRequest);
            }
        }
        return lStatus;
    }

    private static void doWalletRefund(
            String aClientId,
            String aFileId,
            String aBaseMsgId,
            String aMsgId,
            int aTotalParts,
            double aSmsRate,
            double aDltRate,
            String aReason,
            boolean isIntl)
    {
        boolean lStatus = false;
        while (!lStatus)
            try
            {
                final WalletRefundInput lWalletInput1 = WalletInput.getRefundInput(aClientId, aFileId, aBaseMsgId, aMsgId, aTotalParts, aSmsRate, aDltRate, aReason, isIntl);
                WalletDeductRefundProcessor.returnAmountToWallet(lWalletInput1);
                lStatus = true;
            }
            catch (final Exception e)
            {

                try
                {
                    log.fatal(" Sleeping 100 milli since redis is not reachable.. ");
                    Thread.sleep(100);
                }
                catch (final InterruptedException e2)
                {}
            }
    }

    private static JSONObject getAccountInfo(
            MessageRequest aMessageRequest)
    {
        final String   lClientId = aMessageRequest.getClientId();
        final UserInfo lUserInfo = ClientAccountDetails.getUserDetailsByClientId(lClientId);
        if (lUserInfo.getAccountDetails() != null)
            try
            {
                return parseJSON(lUserInfo.getAccountDetails());
            }
            catch (final ParseException e)
            {
                // ignore
            }

        return null;
    }

    private static JSONObject parseJSON(
            String aJsonString)
            throws ParseException
    {
        return (JSONObject) new JSONParser().parse(aJsonString);
    }

    private static boolean setIntlRates(
            MessageRequest aMessageRequest)
    {
        final String                    lClientId                 = aMessageRequest.getClientId();
        final String                    lCountry                  = CommonUtility.nullCheck(aMessageRequest.getCountry(), true).toUpperCase();
        final String                    lMcc                  = CommonUtility.nullCheck(aMessageRequest.getMcc(), true);
        final String                    lMnc                  = CommonUtility.nullCheck(aMessageRequest.getMnc(), true);

        final boolean                   lConvertDatewise          = aMessageRequest.getBillingCurrencyConversionType() == 2 ? true : false;
        final String                    lPlatformBaseCurrency     = PlatformUtil.getAppConfigValueAsString(ConfigParamConstants.BASE_CURRENCY);
        final String                    lPlatformIntlBaseCurrency = PlatformUtil.getAppConfigValueAsString(ConfigParamConstants.INTL_BASE_CURRENCY);

        final CalculateIntlBillingPrice lBillingPrice = new CalculateIntlBillingPrice(lClientId, lCountry,lMcc,lMnc, aMessageRequest.getBillingCurrency(), lPlatformIntlBaseCurrency, lPlatformBaseCurrency,
                lConvertDatewise);
 
        CalculateBillingPrice           lCalculateBillingPrice;

        try
        {
            lCalculateBillingPrice = lBillingPrice.calculate();

            if (log.isDebugEnabled())
            {
                log.debug("Intl Billing SMS Rate : " + lCalculateBillingPrice.getBillingSmsRate());
                log.debug("Intl Billing Additional Rate : " + lCalculateBillingPrice.getBillingAdditionalFixedRate());
            }

            aMessageRequest.setBillingSmsRate(lCalculateBillingPrice.getBillingSmsRate());
            aMessageRequest.setBillingAddFixedRate(lCalculateBillingPrice.getBillingAdditionalFixedRate());

            aMessageRequest.setBaseCurrency(lPlatformIntlBaseCurrency);
            aMessageRequest.setBaseSmsRate(lCalculateBillingPrice.getBaseSmsRate());
            aMessageRequest.setBaseAddFixedRate(lCalculateBillingPrice.getBaseAdditionalFixedRate());

            aMessageRequest.setRefCurrency(lCalculateBillingPrice.getRefCurrency());
            aMessageRequest.setRefSmsRate(lCalculateBillingPrice.getRefSmsRate());
            aMessageRequest.setRefAddFixedRate(lCalculateBillingPrice.getRefAdditionalFixedRate());

            aMessageRequest.setBillingExchangeRate(lCalculateBillingPrice.getBillingConversionRate());
            aMessageRequest.setRefExchangeRate(lCalculateBillingPrice.getRefConversionRate());
        }
        catch (final InternationalSMSRateNotAvailableRuntimeException e)
        {
            aMessageRequest.setSubOriginalStatusCode(PlatformStatusCode.INTL_CREDIT_NOT_SPECIFIED.getStatusCode());
            aMessageRequest.setAdditionalErrorInfo("Intl Credit Not specified for Country - '" + lCountry + "'");
            BWCProducer.sendToPlatformRejection(aMessageRequest);
            return false;
        }
        return true;
    }

    public static boolean updatePriceInfo(
            MessageRequest aMessageRequest)
    {
        final String          lClientId              = aMessageRequest.getClientId();
        final String          lPlatformBaseCurrency  = WalletUtil.getAppConfigValueAsString(ConfigParamConstants.BASE_CURRENCY);
        final String  lPlatformIntlBaseCurrency = PlatformUtil.getAppConfigValueAsString(ConfigParamConstants.INTL_BASE_CURRENCY);

        final String          lBillingCurrency       = aMessageRequest.getBillingCurrency();
        final boolean         lConvertDatewise       = aMessageRequest.getBillingCurrencyConversionType() == 2 ? true : false;

        CalculateBillingPrice lCalculateBillingPrice = null;

        if (!aMessageRequest.isIsIntl())
		{
		    final double                lBaseSmsRate      = aMessageRequest.getBaseSmsRate();
		    final double                lBaseAddFixedRate = aMessageRequest.getBaseAddFixedRate();
		    final CalculateBillingPrice lCBP              = new CalculateBillingPrice(lClientId, lBaseSmsRate, lBaseAddFixedRate, lBillingCurrency, lPlatformIntlBaseCurrency, lPlatformBaseCurrency,
		            lConvertDatewise);
		    CurrencyUtil.getBillingPrice(lCBP);
		    lCalculateBillingPrice = lCBP;

		    aMessageRequest.setBaseCurrency(lPlatformIntlBaseCurrency);
		    aMessageRequest.setBillingCurrency(lBillingCurrency);
		    aMessageRequest.setRefCurrency(lPlatformBaseCurrency);
		    aMessageRequest.setBillingSmsRate(lCalculateBillingPrice.getBillingSmsRate());
		    aMessageRequest.setBillingAddFixedRate(lCalculateBillingPrice.getBillingAdditionalFixedRate());

		    aMessageRequest.setBaseSmsRate(lCalculateBillingPrice.getBaseSmsRate());
		    aMessageRequest.setBaseAddFixedRate(lCalculateBillingPrice.getBaseAdditionalFixedRate());
		    aMessageRequest.setRefSmsRate(lCalculateBillingPrice.getRefSmsRate());
		    aMessageRequest.setRefAddFixedRate(lCalculateBillingPrice.getRefAdditionalFixedRate());
		    aMessageRequest.setBillingExchangeRate(lCalculateBillingPrice.getBillingConversionRate());
		    aMessageRequest.setRefExchangeRate(lCalculateBillingPrice.getRefConversionRate());
		}

        return true;
    }

    public static String getAppConfigValueAsString(
            ConfigParamConstants aConfigParamConstant)
    {
        final ApplicationConfiguration lAppConfiguration = (ApplicationConfiguration) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.APPLICATION_CONFIG);
        return lAppConfiguration.getConfigValue(aConfigParamConstant.getKey());
    }

}
