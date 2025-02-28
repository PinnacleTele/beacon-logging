package com.itextos.beacon.commonlib.encryption.process;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.pwdencryption.CryptoType;
import com.itextos.beacon.commonlib.pwdencryption.EncryptedObject;
import com.itextos.beacon.commonlib.pwdencryption.Encryptor;

public class Processor
{

    private static final Log log = LogFactory.getLog(Processor.class);

    public static EncryptedObject encryptProcess(
            String aCryptoType,
            String aTextToEncrypt,
            String aKey)
    {
        EncryptedObject lEncryptedObject = null;

        try
        {
            final CryptoType lCryptoTypeEnum = CryptoType.getCryptoType(aCryptoType);

            lEncryptedObject = Encryptor.encrypt(lCryptoTypeEnum, aTextToEncrypt, aKey);
        }
        catch (final Exception e)
        {
            log.error("Exception while encrypt the String", e);
        }

        return lEncryptedObject;
    }

    public static String decryptProcess(
            String aCryptoType,
            String aTextToDecrypt,
            String aKey)
    {
        String decryptString = null;

        try
        {
            final CryptoType lCryptoTypeEnum = CryptoType.getCryptoType(aCryptoType);

            decryptString = Encryptor.decrypt(lCryptoTypeEnum, aTextToDecrypt, aKey);
        }
        catch (final Exception e)
        {
            log.error("Exception while decrypt the String", e);
            return "Decryption Key not Matched";
        }

        return decryptString;
    }

    public static EncryptedObject encodeProcess(
            String aCryptoType,
            String aTextToEnccode,
            String aKey)
    {
        EncryptedObject lEncryptedObject = null;

        try
        {
            final CryptoType lCryptoTypeEnum = CryptoType.getCryptoType(aCryptoType);

            lEncryptedObject = Encryptor.encrypt(lCryptoTypeEnum, aTextToEnccode, aKey);
        }
        catch (final Exception e)
        {
            log.error("Exception while decrypt the String", e);
        }

        return lEncryptedObject;
    }

    public static String decodeProcess(
            String aCryptoType,
            String aTextToEnccode,
            String aKey)
    {
        String decodedString = null;

        try
        {
            final CryptoType lCryptoTypeEnum = CryptoType.getCryptoType(aCryptoType);

            decodedString = Encryptor.decrypt(lCryptoTypeEnum, aTextToEnccode, aKey);
        }
        catch (final Exception e)
        {
            log.error("Exception while decrypt the String", e);
        }

        return decodedString;
    }

    public static void main(
            String[] args)
    {
        final EncryptedObject lEncryptedObject = encryptProcess("aes256", "Hello World", "Test");
        final String          lProcessedText   = lEncryptedObject.getEncryptedWithIvAndSalt();
        System.out.println(lProcessedText);
    }

}
