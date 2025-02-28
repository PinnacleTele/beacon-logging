package com.itextos.beacon.commonlib.pwdencryption;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

class Aes256Encrypt
{

    /**
     * This method is used to decrypt the String which is coming along with the
     * InitialVector and SALT.
     *
     * @param aToDecrypt
     *                   A String to decrypt based on the AES 256 algorithm with the
     *                   IV params and Salt Bytes available within it.
     * @param aSecret
     *                   A String which is {@link Base64#getEncoder()} encoded to be
     *                   used as a secret key.
     *
     * @return
     *         Decrypted String
     *
     * @throws Exception
     *                   In case of the the <code>aToDecrypt</code> is not properly
     *                   encrypted and / or not properly padded with the IV and
     *                   SALT.
     */
    static String decrypt(
            String aToDecrypt,
            String aSecret)
            throws Exception
    {

        try
        {
            final byte[]     lDecode           = Base64.getDecoder().decode(aToDecrypt);
            final ByteBuffer byteBuffer        = ByteBuffer.wrap(lDecode);
            final byte[]     initialVectorByte = new byte[PasswordConstants.INITIAL_VECTOR_LENGTH];
            byteBuffer.get(initialVectorByte);

            final byte[] saltByte = new byte[PasswordConstants.SALT_LEGNTH];
            byteBuffer.get(saltByte);

            final byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);

            final Cipher    cipher    = Cipher.getInstance(PasswordConstants.ALGORITHM);
            final SecretKey secretKey = getSecrete(aSecret, saltByte);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(initialVectorByte));
            final byte[] plainText = cipher.doFinal(cipherText);
            return new String(plainText, StandardCharsets.UTF_8);
        }
        catch (final Exception e)
        {
            throw e;
        }
    }

    /**
     * This method is used to encrypt the passed string with the use of the secrete
     * key passed.
     *
     * @param aToEncrypt
     *                   A String to encrypt with the AES 256 algorithm.
     * @param aSecret
     *                   A String which is {@link Base64#getEncoder()} encoded to be
     *                   used as a secret key.
     *
     * @return
     *         An array of Strings with length 2. The first element represents the
     *         {@link Base64#getEncoder()} encoded encrypted String. The second
     *         element represents the {@link Base64#getEncoder()} encoded String
     *         with the InitialVector, Salt and the encrypted String.
     *
     * @throws Exception
     */
    static EncryptedObject encrypt(
            String aToEncrypt,
            String aSecret)
            throws Exception
    {

        try
        {
            final byte[]     intialVectorBytes = generateIv(PasswordConstants.INITIAL_VECTOR_LENGTH);
            final byte[]     saltBytes         = RandomString.getSaltString().getBytes();
            final byte[]     encryptedBytes    = encrypt(aToEncrypt, aSecret, saltBytes, intialVectorBytes);

            final ByteBuffer byteBuffer        = ByteBuffer.allocate(intialVectorBytes.length + saltBytes.length + encryptedBytes.length);
            byteBuffer.put(intialVectorBytes).put(saltBytes).put(encryptedBytes);

            final String encryptedWithIvAndSalt = Base64.getEncoder().encodeToString(byteBuffer.array());

            return new EncryptedObject(aToEncrypt, encryptedWithIvAndSalt);
        }
        catch (final Exception e)
        {
            throw e;
        }
    }

    private static byte[] encrypt(
            String aToEncrypt,
            String aSecret,
            byte[] aSaltByte,
            byte[] aInitialVector)
            throws Exception
    {
        final IvParameterSpec ivspec    = new IvParameterSpec(aInitialVector);
        final SecretKey       secretKey = getSecrete(aSecret, aSaltByte);
        final Cipher          cipher    = Cipher.getInstance(PasswordConstants.ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
        return cipher.doFinal(aToEncrypt.getBytes(StandardCharsets.UTF_8));
    }

    private static byte[] generateIv(
            int numBytes)
    {
        final byte[] nonce = new byte[numBytes];
        new SecureRandom().nextBytes(nonce);
        return nonce;
    }

    private static SecretKey getSecrete(
            String aSecret,
            byte[] aSaltByte)
            throws NoSuchAlgorithmException,
            InvalidKeySpecException
    {
        final String           decodedKey = new String(Base64.getDecoder().decode(aSecret));
        final SecretKeyFactory factory    = SecretKeyFactory.getInstance(PasswordConstants.SECRET_KEY_ALGORITHM);
        final KeySpec          spec       = new PBEKeySpec(decodedKey.toCharArray(), aSaltByte, 65536, 256);
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }

    public static void main(
            String[] args)
            throws Exception
    {
        final String stringToEncrypt = "Hello There";
        final String key             = "S3VtYXJhcGFuZGlhbg==";

        encrypt("sss", key); // To load required jars.
        System.out.println("here only");
        final long            start     = System.currentTimeMillis();
        final EncryptedObject encrypted = encrypt(stringToEncrypt, key);
        final long            first     = System.currentTimeMillis();
        final String          decrypted = decrypt(encrypted.getEncryptedWithIvAndSalt(), key);
        final long            later     = System.currentTimeMillis();

        System.out.println("'" + stringToEncrypt + "'");
        System.out.println("'" + encrypted + "' >> " + (first - start));
        System.out.println("'" + decrypted + "' >> " + (later - first));
    }

    private Aes256Encrypt()
    {}

}