package com.itextos.beacon.inmemory.encryptinfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.pwdencryption.CryptoType;
import com.itextos.beacon.commonlib.pwdencryption.EncryptedObject;
import com.itextos.beacon.commonlib.pwdencryption.Encryptor;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;

public class CustomerEncryptUtil
{

    private static final Log  log = LogFactory.getLog(CustomerEncryptUtil.class);

    private static CryptoType mCryptoType;

    private CustomerEncryptUtil()
    {}

    public static String encryptIncomingString(
            String aClientId,
            String aStringToEncrypt)
    {
        return encryptOrDecrypt(aClientId, aStringToEncrypt, CryptoCategory.INCOMING, Operation.ENCRYPT);
    }

    public static String decryptIncomingString(
            String aClientId,
            String aStringToEncrypt)
    {
        return encryptOrDecrypt(aClientId, aStringToEncrypt, CryptoCategory.INCOMING, Operation.DECRYPT);
    }

    public static String encryptBillingString(
            String aClientId,
            String aStringToEncrypt)
    {
        return encryptOrDecrypt(aClientId, aStringToEncrypt, CryptoCategory.BILLING, Operation.ENCRYPT);
    }

    public static String decryptBillingString(
            String aClientId,
            String aStringToEncrypt)
    {
        return encryptOrDecrypt(aClientId, aStringToEncrypt, CryptoCategory.BILLING, Operation.DECRYPT);
    }

    public static String encryptHandoverString(
            String aClientId,
            String aStringToEncrypt)
    {
        return encryptOrDecrypt(aClientId, aStringToEncrypt, CryptoCategory.HANDOVER, Operation.ENCRYPT);
    }

    public static String decryptHandoverString(
            String aClientId,
            String aStringToEncrypt)
    {
        return encryptOrDecrypt(aClientId, aStringToEncrypt, CryptoCategory.HANDOVER, Operation.DECRYPT);
    }

    private static String encryptOrDecrypt(
            String aClientId,
            String aStringToEncrypt,
            CryptoCategory aCategory,
            Operation aOperation)
    {

        try
        {
            final EncryptInfoCollection encryptInfoCollection = (EncryptInfoCollection) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.ENCRYPT_INFO);
            final EncryptInfo           lEncryptInfo          = encryptInfoCollection.getEncryptInfo(aClientId);

            if (log.isDebugEnabled())
                log.debug("Encryption infor for the client id '" + aClientId + "' is " + lEncryptInfo);

            if (lEncryptInfo != null)
            {
                final CryptoType cryptoType = getCryptoType(aCategory, lEncryptInfo);

                if (cryptoType != null)
                {
                    setCryptoType(cryptoType);

                    return doEncryptOrDecrypt(aCategory, cryptoType, aOperation, aStringToEncrypt, lEncryptInfo);
                }
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while doing " + aOperation + " for the client " + aClientId, e);
        }
        return aStringToEncrypt;
    }

    public static String encryptBilling(
            String aClientId,
            String aStringToEncrypt)
    {
        return bilingEncrypt(aClientId, aStringToEncrypt, CryptoCategory.BILLING, Operation.ENCRYPT);
    }

    private static String bilingEncrypt(
            String aClientId,
            String aStringToEncrypt,
            CryptoCategory aCategory,
            Operation aOperation)
    {

        try
        {
            final EncryptInfoCollection encryptInfoCollection = (EncryptInfoCollection) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.ENCRYPT_INFO);
            final EncryptInfo           lEncryptInfo          = encryptInfoCollection.getEncryptInfo(aClientId);

            if (log.isDebugEnabled())
                log.debug("Encryption infor for the client id '" + aClientId + "' is " + lEncryptInfo);

            if (lEncryptInfo != null)
            {
                final CryptoType cryptoType = CryptoType.ENCRYPTION_AES_256;

                if (cryptoType != null)
                    return doEncryptOrDecrypt(aCategory, cryptoType, aOperation, aStringToEncrypt, lEncryptInfo);
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while doing " + aOperation + " for the client " + aClientId, e);
        }
        return aStringToEncrypt;
    }

    private static String doEncryptOrDecrypt(
            CryptoCategory aCategory,
            CryptoType aCryptoType,
            Operation aOperation,
            String aStringToEncrypt,
            EncryptInfo aEncryptInfo)
            throws Exception
    {

        switch (aCryptoType)
        {
            case ENCRYPTION_AES_256:
                return doEncryptOrDecryptAes256(aCategory, aOperation, aStringToEncrypt, aEncryptInfo);

            case HASHING_BCRYPT:
                return doEncryptOrDecryptBcrypt(aOperation, aStringToEncrypt);

            case EMPTY:
                return doEncryptOrDecryptEmpty(aOperation, aStringToEncrypt);

            case ENCODE:
                return doEncryptOrDecryptOnEncode(aOperation, aStringToEncrypt);

            default:
                break;
        }

        return aStringToEncrypt;
    }

    private static String doEncryptOrDecryptOnEncode(
            Operation aOperation,
            String aStringToEncrypt)
            throws Exception
    {

        switch (aOperation)
        {
            case ENCRYPT:
            {
                final EncryptedObject lEncrypt = Encryptor.encrypt(CryptoType.ENCODE, aStringToEncrypt, null);
                return (lEncrypt != null) ? lEncrypt.getEncryptedWithIvAndSalt() : null;
            }

            case DECRYPT:
                return Encryptor.decrypt(CryptoType.ENCODE, aStringToEncrypt, null);

            default:
                break;
        }
        return aStringToEncrypt;
    }

    private static String doEncryptOrDecryptEmpty(
            Operation aOperation,
            String aStringToEncrypt)
            throws Exception
    {

        switch (aOperation)
        {
            case ENCRYPT:
            {
                final EncryptedObject lEncrypt = Encryptor.encrypt(CryptoType.EMPTY, aStringToEncrypt, null);
                return (lEncrypt != null) ? lEncrypt.getEncryptedWithIvAndSalt() : null;
            }

            case DECRYPT:
                // not applicable
            default:
                break;
        }
        return aStringToEncrypt;
    }

    private static String doEncryptOrDecryptBcrypt(
            Operation aOperation,
            String aStringToEncrypt)
            throws Exception
    {

        switch (aOperation)
        {
            case ENCRYPT:
            {
                final EncryptedObject lEncrypt = Encryptor.encrypt(CryptoType.HASHING_BCRYPT, aStringToEncrypt, null);
                return (lEncrypt != null) ? lEncrypt.getEncryptedWithIvAndSalt() : null;
            }

            case DECRYPT:
                // not applicable
            default:
                break;
        }
        return aStringToEncrypt;
    }

    private static String doEncryptOrDecryptAes256(
            CryptoCategory aCategory,
            Operation aOperation,
            String aStringToEncrypt,
            EncryptInfo aEncryptInfo)
            throws Exception
    {
        String secretKey = null;

        switch (aCategory)
        {
            case BILLING:
                secretKey = aEncryptInfo.getBillingCryptoParam1();
                break;

            case HANDOVER:
                secretKey = aEncryptInfo.getHandoverCryptoParam1();
                break;

            case INCOMING:
                secretKey = aEncryptInfo.getIncomingCryptoParam1();
                break;

            default:
                break;
        }

        if (log.isDebugEnabled())
            log.debug("Secret Key for the client " + aEncryptInfo.getClientId() + "' is '" + secretKey + "'");

        if ((secretKey != null) && !secretKey.isBlank())
            switch (aOperation)
            {
                case ENCRYPT:
                {
                    final EncryptedObject lEncrypt = Encryptor.encrypt(CryptoType.ENCRYPTION_AES_256, aStringToEncrypt, secretKey);
                    return (lEncrypt != null) ? lEncrypt.getEncryptedWithIvAndSalt() : null;
                }

                case DECRYPT:
                    return Encryptor.decrypt(CryptoType.ENCRYPTION_AES_256, aStringToEncrypt, secretKey);

                default:
                    break;
            }
        return aStringToEncrypt;
    }

    private static CryptoType getCryptoType(
            CryptoCategory aCategory,
            EncryptInfo lEncryptInfo)
    {

        switch (aCategory)
        {
            case BILLING:
                return CryptoType.getCryptoType(lEncryptInfo.getBillingCryptoType());

            case HANDOVER:
                return CryptoType.getCryptoType(lEncryptInfo.getHandoverCryptoType());

            case INCOMING:
                return CryptoType.getCryptoType(lEncryptInfo.getIncomingCryptoType());

            default:
                break;
        }
        return null;
    }

    public static CryptoType getCryptoType()
    {
        return mCryptoType;
    }

    public static void setCryptoType(
            CryptoType aCryptoType)
    {
        mCryptoType = aCryptoType;
    }

}

enum CryptoCategory
{

    INCOMING,
    BILLING,
    HANDOVER;
}

enum Operation
{
    ENCRYPT,
    DECRYPT;
}