package com.itextos.beacon.platform.billing;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.RouteConstants;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.message.SubmissionObject;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.errorlog.SMSLog;
import com.itextos.beacon.inmemory.msgutil.cache.CarrierCircle;
import com.itextos.beacon.inmemory.msgutil.util.IndiaNPFinder;
import com.itextos.beacon.platform.billing.support.BillingProducer;
import com.itextos.beacon.platform.billing.support.BillingUtility;
import com.itextos.beacon.platform.msgflowutil.billing.BillingDatabaseTableIndentifier;
import com.itextos.beacon.platform.walletbase.data.WalletInput;
import com.itextos.beacon.platform.walletbase.data.WalletRefundInput;
import com.itextos.beacon.platform.walletprocess.WalletDeductRefundProcessor;

public abstract class CommonProcess
        implements
        IBillingProcess
{

    private static final Log      log                 = LogFactory.getLog(CommonProcess.class);
    protected static final String DUMMY_CARRIER       = BillingUtility.getAppConfigValueAsString(ConfigParamConstants.DEFAULT_CARRIER_VALUE);
    protected static final String DUMMY_CIRCLE        = BillingUtility.getAppConfigValueAsString(ConfigParamConstants.DEFAULT_CIRCLE_VALUE);
    protected static final String ROUTE_INVALID       = RouteConstants.DUMMY;
    protected static final int    FILE_NAME_MAX_LIMIT = 250;
    protected static final String PROMO_ALPHA         = "1";
    protected static final String TRANS_ALPHA         = "0";
    protected static final int    NO_PARTS            = 0;
    protected static final int    SINGLE_PART         = 1;

    protected final BaseMessage   mBaseMessage;

    protected CommonProcess(
            BaseMessage aBaseMessage)
    {
        mBaseMessage = aBaseMessage;
    }

    protected void identifySuffix()
    {

        try
        {
            final BillingDatabaseTableIndentifier lBillingDatabaseTableIndentifier = new BillingDatabaseTableIndentifier(mBaseMessage);
            lBillingDatabaseTableIndentifier.identifySuffix();
        }
        catch (final Exception e)
        {
            log.error("Exception occer while identifying table suffix...", e);
        }
    }

    protected void sendToOtherTopic(
            NextTopic aNextTopic,SMSLog sb)
    {
        if (log.isDebugEnabled())
            log.debug("Send to next topic '" + aNextTopic + "'");

        BaseMessage aBaseMessage=getBaseMessage();
        switch (aNextTopic)
        {
            case FULL_MESSAGE_INSERT:
                BillingProducer.sendToFullMessageTopic(aBaseMessage,sb);
                break;

            case DLR_QUERY:
                BillingProducer.sendToDlrQueryTopic(aBaseMessage,sb);
                break;

            case SUB_BILLING:
                BillingProducer.sendToBillingTopic(aBaseMessage,sb);
                break;

            default:
                break;
        }
    }

    private BaseMessage getBaseMessage() {

    	String json=mBaseMessage.getJsonString();
    	try {
    	if(mBaseMessage instanceof SubmissionObject) {
    		
    		
				return new SubmissionObject(json);
			
    		
    	}else if(mBaseMessage instanceof DeliveryObject) {
    		
    		return new DeliveryObject(json);
    	}else {
    		
    		return mBaseMessage;
    	}
    	
    	} catch (Exception e) {
			
    		return mBaseMessage;
		}
    	
	}

	protected CarrierCircle getDefaultCarrierCircle()
    {
        final CarrierCircle carrierAndCircle = null;
        final String        lMobileNumber    = mBaseMessage.getValue(MiddlewareConstant.MW_MOBILE_NUMBER);

        try
        {
            if (lMobileNumber.length() > 5)
                return IndiaNPFinder.getCarrierCircle(lMobileNumber, true);
            else
                if (log.isInfoEnabled())
                    log.info("Cannot find the Carrier and circle for the dest : '" + lMobileNumber + "'");
        }
        catch (final Exception e)
        {
            log.error("Problem in getting the Carrier and Circle for the Dest '" + lMobileNumber + "'", e);
        }
        return carrierAndCircle;
    }

    protected Date getDateFromMessage(
            MiddlewareConstant aMiddlewareConstant)
    {
        final Date d = null;

        try
        {
            final long lStsLong = CommonUtility.getLong(mBaseMessage.getValue(aMiddlewareConstant));

            if (lStsLong > 0)
                return new Date(lStsLong);

            if (log.isDebugEnabled())
                log.debug("Not received a proper Value for " + aMiddlewareConstant.getName() + " in the message. Setting null value for " + aMiddlewareConstant.getName() + ". Received : " + lStsLong);
        }
        catch (final Exception e)
        {
            if (log.isDebugEnabled())
                log.debug("Not received a proper Value for " + aMiddlewareConstant.getName() + " in the message. Setting null value for " + aMiddlewareConstant.getName() + ".");
        }
        return d;
    }

    protected static void doWalletRefund(
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

}