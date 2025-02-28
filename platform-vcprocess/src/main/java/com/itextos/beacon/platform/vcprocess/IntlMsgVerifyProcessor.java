package com.itextos.beacon.platform.vcprocess;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.PlatformStatusCode;
import com.itextos.beacon.commonlib.constants.exception.InternationalSMSRateNotAvailableRuntimeException;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.Name;
import com.itextos.beacon.inmemory.intlrouteinfo.util.IntlRouteUtil;
import com.itextos.beacon.platform.intlprice.CalculateBillingPrice;
import com.itextos.beacon.platform.intlprice.CalculateIntlBillingPrice;
import com.itextos.beacon.platform.msgflowutil.util.PlatformUtil;
import com.itextos.beacon.platform.prc.process.RejectionProcess;
import com.itextos.beacon.platform.vcprocess.util.VCProducer;

public class IntlMsgVerifyProcessor
{

 //   private final Log            log = LogFactory.getLog(IntlMsgVerifyProcessor.class);

    private final MessageRequest mMessageRequest;
    private final Component      mSourceComponent;

    public IntlMsgVerifyProcessor(
            Component aSourceComponent,
            MessageRequest aMessageRequest)
    {
        this.mMessageRequest  = aMessageRequest;
        this.mSourceComponent = aSourceComponent;
    }

    public void messageProcess()
            throws Exception
    {
        if (!checkIntlRequirements())
            return;

        final String  lClientId                 = mMessageRequest.getClientId();
        final String  lCountry                  = CommonUtility.nullCheck(mMessageRequest.getCountry(), true).toUpperCase();
        final String  lMcc                  = CommonUtility.nullCheck(mMessageRequest.getMcc(), true);
        final String  lMnc                  = CommonUtility.nullCheck(mMessageRequest.getMnc(), true);

        final boolean lConvertDatewise          = mMessageRequest.getBillingCurrencyConversionType() == 2 ? true : false;
        final String  lPlatformBaseCurrency     = PlatformUtil.getAppConfigValueAsString(ConfigParamConstants.BASE_CURRENCY);
        final String  lPlatformIntlBaseCurrency = PlatformUtil.getAppConfigValueAsString(ConfigParamConstants.INTL_BASE_CURRENCY);

        mMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(mMessageRequest.getFileId()+" : Country : " + lCountry );
        mMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(mMessageRequest.getFileId()+" : Intl From Currency : " + lPlatformIntlBaseCurrency);
        mMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(mMessageRequest.getFileId()+" : Billing Currency : " + mMessageRequest.getBillingCurrency());
        mMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(mMessageRequest.getFileId()+" : Reffrence Currency : " + lPlatformBaseCurrency);


        final CalculateIntlBillingPrice lBillingPrice = new CalculateIntlBillingPrice(lClientId, lCountry,lMcc,lMnc, mMessageRequest.getBillingCurrency(), lPlatformIntlBaseCurrency, lPlatformBaseCurrency,
                lConvertDatewise);
        CalculateBillingPrice           lCalculateBillingPrice;

        try
        {
            mMessageRequest.setRefCurrency(lPlatformBaseCurrency);

        	   mMessageRequest.setBaseCurrency(lPlatformIntlBaseCurrency);
        
            lCalculateBillingPrice = lBillingPrice.calculate();


            mMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(mMessageRequest.getFileId()+" : Intl Billing SMS Rate : " + lCalculateBillingPrice.getBillingSmsRate() );

            mMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(mMessageRequest.getFileId()+" : Intl Billing Additional Rate : " + lCalculateBillingPrice.getBillingAdditionalFixedRate() );


            mMessageRequest.setBaseSmsRate(lCalculateBillingPrice.getBaseSmsRate());
            mMessageRequest.setBaseAddFixedRate(lCalculateBillingPrice.getBaseAdditionalFixedRate());

            mMessageRequest.setBillingSmsRate(lCalculateBillingPrice.getBillingSmsRate());
            mMessageRequest.setBillingAddFixedRate(lCalculateBillingPrice.getBillingAdditionalFixedRate());

            mMessageRequest.setRefCurrency(lCalculateBillingPrice.getRefCurrency());
            mMessageRequest.setRefSmsRate(lCalculateBillingPrice.getRefSmsRate());
            mMessageRequest.setRefAddFixedRate(lCalculateBillingPrice.getRefAdditionalFixedRate());

            mMessageRequest.setBillingExchangeRate(lCalculateBillingPrice.getBillingConversionRate());
            mMessageRequest.setRefExchangeRate(lCalculateBillingPrice.getRefConversionRate());
        }catch(final InternationalSMSRateNotAvailableRuntimeException e) {
        	
        	   mMessageRequest.setSubOriginalStatusCode(PlatformStatusCode.PRICE_CONVERSION_FAILED.getStatusCode());
               mMessageRequest.setAdditionalErrorInfo(e.getMessage());
               VCProducer.sendToPlatformRejection(mSourceComponent, mMessageRequest);
        }
        catch (final Exception e)
        {
            mMessageRequest.setSubOriginalStatusCode(PlatformStatusCode.INTL_CREDIT_NOT_SPECIFIED.getStatusCode());
            mMessageRequest.setAdditionalErrorInfo("Intl Credit Not specified for Country - '" + lCountry + "'");
            VCProducer.sendToPlatformRejection(mSourceComponent, mMessageRequest);
            return;
        }

        /*
         * final ClientIntlCredits lCustomerIntlCredits =
         * MessageFlowUtil.getClientIntlCreditsInfo();
         * final IntlCredits lIntlCredits = MessageFlowUtil.getIntlCreditsInfo();
         * IntlSmsRates lIntllRates = lCustomerIntlCredits.getCustomerCredits(lClientId,
         * lCountry);
         * if (lIntllRates.getBaseSmsRate() <= 0)
         * {
         * if (log.isDebugEnabled())
         * log.debug("Client Intl Rates : " + lIntllRates);
         * if (lIntlCredits.isCountryHavingCredits(lCountry))
         * {
         * lIntllRates = lIntlCredits.getCountryCredits(lCountry);
         * if (log.isDebugEnabled())
         * log.debug("Country Intl Credits : " + lIntllRates);
         * }
         * }
         */

        /*
         * if (lIntllRates.getBaseSmsRate() <= 0)
         * {
         * mMessageRequest.setSubOriginalStatusCode(PlatformStatusCode.
         * INTL_CREDIT_NOT_SPECIFIED.getStatusCode());
         * mMessageRequest.
         * setAdditionalErrorInfo("Intl Credit Not specified for Country - '" + lCountry
         * + "'");
         * VCProducer.sendToPlatformRejection(mSourceComponent, mMessageRequest);
         * return;
         * }
         */

        final boolean isCreditCheckEnabled = CommonUtility.isEnabled(mMessageRequest.getValue(MiddlewareConstant.MW_CREDIT_CHECK));

   
        mMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(mMessageRequest.getFileId()+" : Credit Check Enabled : " + isCreditCheckEnabled );

        if ((mMessageRequest.getBillType() == 1) || isCreditCheckEnabled)
        {
            VCProducer.sendToNextComponent(mSourceComponent, Component.WC, mMessageRequest);

            mMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(mMessageRequest.getFileId()+"Message Send to Wallet Topic: Successfully");

         }
        else
        {
            VCProducer.sendToNextComponent(mSourceComponent, Component.RC, mMessageRequest);

            mMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(mMessageRequest.getFileId()+" : Message  sendtoIntlRouteQueue: Successfully : " );

         }
    }

    private boolean checkIntlRequirements()
            throws Exception
    {
        final PlatformStatusCode lErrorCode = IntlRouteUtil.checkAndUpdateRouteBasedOnIntlRoute(mMessageRequest);

        if (lErrorCode != null)
        {
         	
        	mMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(mMessageRequest.getFileId()+" : Intl Rejected Status Code : " + lErrorCode);

            mMessageRequest.setSubOriginalStatusCode(lErrorCode.getStatusCode());
          //  VCProducer.sendToNextComponent(mSourceComponent, Component.PRC, mMessageRequest);
            mMessageRequest.setFromComponent(mSourceComponent.getKey());
            mMessageRequest.setNextComponent(Component.PRC.getKey());
            RejectionProcess.forPRC(mMessageRequest);

            return false;
        }

        return true;
    }

}
