package com.itextos.beacon.platform.billing.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.pwdencryption.CryptoType;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.configvalues.ApplicationConfiguration;
import com.itextos.beacon.inmemory.encryptinfo.CustomerEncryptUtil;
import com.itextos.beacon.inmemory.encryptinfo.EncryptInfo;
import com.itextos.beacon.inmemory.encryptinfo.EncryptInfoCollection;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;

public class BillingUtility
{

    private static final Log    log                        = LogFactory.getLog(BillingUtility.class);
    private static final double MAX_DATABASE_ALLOWED_VALUE = 9999999.999;

    private BillingUtility()
    {}

    public static double[] checkMinMaxValues(
            double[] aDoubleValues)
    {
        final double[] returnValue = new double[2];
        returnValue[0] = checkMinMaxValue(aDoubleValues[0]);
        returnValue[1] = checkMinMaxValue(aDoubleValues[1]);
        return returnValue;
    }

    private static double checkMinMaxValue(
            double aValue)
    {
        if (aValue > MAX_DATABASE_ALLOWED_VALUE)
            return MAX_DATABASE_ALLOWED_VALUE;
        if (aValue < 0)
            return 0;
        return aValue;
    }

    public static void encryptMobile(
            BaseMessage aBaseMessage)
    {

        try
        {
            final boolean isBillingEncryptEnabled = CommonUtility.isEnabled(aBaseMessage.getValue(MiddlewareConstant.MW_BILLING_ENCRYPT_TYPE));

            if (!isBillingEncryptEnabled)
                return;

            final String                clientId              = aBaseMessage.getValue(MiddlewareConstant.MW_CLIENT_ID);
            final EncryptInfoCollection encryptInfoCollection = (EncryptInfoCollection) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.ENCRYPT_INFO);
            final EncryptInfo           lEncryptInfo          = encryptInfoCollection.getEncryptInfo(clientId);
            if ((lEncryptInfo == null) || (lEncryptInfo.getBillingCryptoType() == 0))
                return;

            final int lBillingCryptoColumns = lEncryptInfo.getBillingCryptoColumns();

            switch (lBillingCryptoColumns)
            {
                case 1:
                case 3:
                    aBaseMessage.putValue(MiddlewareConstant.MW_MOBILE_NUMBER, "0");
                    updateFields(aBaseMessage, clientId, MiddlewareConstant.MW_MOBILE_NUMBER, MiddlewareConstant.MW_ENCRYPTED_MOBILENUMBER);
                    break;

                case 2:
                    break;

                default:
                    break;
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while encrypting mobile for deiveries.", e);
        }
    }

    public static void encryptData(
            BaseMessage aBaseMessage)
    {

        try
        {
            final boolean isBillingEncryptEnabled = CommonUtility.isEnabled(aBaseMessage.getValue(MiddlewareConstant.MW_BILLING_ENCRYPT_TYPE));

            if (log.isDebugEnabled())
                log.debug("Billing Encrypt Enabled :" + isBillingEncryptEnabled);

            if (!isBillingEncryptEnabled)
                return;

            final String                clientId              = aBaseMessage.getValue(MiddlewareConstant.MW_CLIENT_ID);
            final EncryptInfoCollection encryptInfoCollection = (EncryptInfoCollection) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.ENCRYPT_INFO);
            final EncryptInfo           lEncryptInfo          = encryptInfoCollection.getEncryptInfo(clientId);
            if ((lEncryptInfo == null) || (lEncryptInfo.getBillingCryptoType() == 0))
                return;

            if (log.isDebugEnabled())
                log.debug("Billing Encrypt info :" + lEncryptInfo);

            final int lBillingCryptoColumns = lEncryptInfo.getBillingCryptoColumns();

            switch (lBillingCryptoColumns)
            {
                case 1:
                    updateFields(aBaseMessage, clientId, MiddlewareConstant.MW_MOBILE_NUMBER, MiddlewareConstant.MW_ENCRYPTED_MOBILENUMBER);
                    break;

                case 2:
                    updateFields(aBaseMessage, clientId, MiddlewareConstant.MW_MSG, MiddlewareConstant.MW_ENCRYPTED_MESSAGE);
                    updateFields(aBaseMessage, clientId, MiddlewareConstant.MW_LONG_MSG, MiddlewareConstant.MW_ENCRYPTED_LONG_MSG);
                    break;

                case 3:
                    updateFields(aBaseMessage, clientId, MiddlewareConstant.MW_MOBILE_NUMBER, MiddlewareConstant.MW_ENCRYPTED_MOBILENUMBER);
                    updateFields(aBaseMessage, clientId, MiddlewareConstant.MW_MSG, MiddlewareConstant.MW_ENCRYPTED_MESSAGE);
                    updateFields(aBaseMessage, clientId, MiddlewareConstant.MW_LONG_MSG, MiddlewareConstant.MW_ENCRYPTED_LONG_MSG);
                    break;

                default:
                    break;
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while encrypting mobile/ message.", e);
        }
    }

    private static void updateFields(
            BaseMessage aBaseMessage,
            String aClientId,
            MiddlewareConstant aActualKey,
            MiddlewareConstant aEncryptedKey)
    {
        String       actualValue    = aBaseMessage.getValue(aActualKey);
        final String encryptedValue = CustomerEncryptUtil.encryptBillingString(aClientId, actualValue);

        if ((CustomerEncryptUtil.getCryptoType() == CryptoType.HASHING_BCRYPT) && (aActualKey == MiddlewareConstant.MW_MOBILE_NUMBER))
        {
            final String encryptedDest = CustomerEncryptUtil.encryptBilling(aClientId, actualValue);

            if (log.isDebugEnabled())
                log.debug("Encrypted Dest :" + encryptedDest);

            final String lAddErrorInfo = aBaseMessage.getValue(MiddlewareConstant.MW_ADD_ERROR_INFO);

            aBaseMessage.putValue(MiddlewareConstant.MW_ADD_ERROR_INFO, (lAddErrorInfo == null) ? encryptedDest : (lAddErrorInfo + ":" + encryptedDest));
        }

        if (aActualKey == MiddlewareConstant.MW_MOBILE_NUMBER)
            actualValue = "0";

        if ((aActualKey == MiddlewareConstant.MW_MSG) || (aActualKey == MiddlewareConstant.MW_LONG_MSG))
            actualValue = "";

        aBaseMessage.putValue(aActualKey, actualValue);
        aBaseMessage.putValue(aEncryptedKey, encryptedValue);
    }

    public static String getAppConfigValueAsString(
            ConfigParamConstants aConfigParamConstant)
    {
        final ApplicationConfiguration lAppConfiguration = (ApplicationConfiguration) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.APPLICATION_CONFIG);
        return lAppConfiguration.getConfigValue(aConfigParamConstant.getKey());
    }

}