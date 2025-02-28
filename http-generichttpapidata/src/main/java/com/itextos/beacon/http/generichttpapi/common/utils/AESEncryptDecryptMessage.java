package com.itextos.beacon.http.generichttpapi.common.utils;

import java.nio.ByteBuffer;
import java.security.AlgorithmParameters;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class AESEncryptDecryptMessage
{

    public static String encrypt(
            String aMessage,
            String aSecureyKey)
            throws Exception
    {
        final SecureRandom random = new SecureRandom();
        final byte[]       bytes  = new byte[20];
        random.nextBytes(bytes);
        final byte[]           saltBytes = bytes;
        final SecretKeyFactory factory   = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        final String           passWord  = new String(Base64.getDecoder().decode(aSecureyKey));
        final PBEKeySpec       spec      = new PBEKeySpec(passWord.toCharArray(), saltBytes, 65556, 256);
        final SecretKey        secretKey = factory.generateSecret(spec);
        final SecretKeySpec    secret    = new SecretKeySpec(secretKey.getEncoded(), "AES");
        final Cipher           cipher    = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(1, secret);
        final AlgorithmParameters params             = cipher.getParameters();
        final byte[]              ivBytes            = params.getParameterSpec(IvParameterSpec.class).getIV();
        final byte[]              encryptedTextBytes = cipher.doFinal(aMessage.getBytes("UTF-8"));
        final byte[]              buffer             = new byte[saltBytes.length + ivBytes.length + encryptedTextBytes.length];
        System.arraycopy(saltBytes, 0, buffer, 0, saltBytes.length);
        System.arraycopy(ivBytes, 0, buffer, saltBytes.length, ivBytes.length);
        System.arraycopy(encryptedTextBytes, 0, buffer, saltBytes.length + ivBytes.length, encryptedTextBytes.length);
        return Base64.getEncoder().encodeToString(buffer);
    }

    public static String decrypt(
            String aEncryptedText,
            String aSecureyKey)
            throws Exception
    {
        final Cipher     cipher    = Cipher.getInstance("AES/CBC/PKCS5Padding");

        final ByteBuffer buffer    = ByteBuffer.wrap(Base64.getDecoder().decode(aEncryptedText));
        final byte[]     saltBytes = new byte[20];
        buffer.get(saltBytes, 0, saltBytes.length);
        final byte[] ivBytes1 = new byte[cipher.getBlockSize()];
        buffer.get(ivBytes1, 0, ivBytes1.length);
        final byte[] encryptedTextBytes = new byte[buffer.capacity() - saltBytes.length - ivBytes1.length];

        buffer.get(encryptedTextBytes);

        final String           _secureyKey = new String(Base64.getDecoder().decode(aSecureyKey));
        // Deriving the key
        final SecretKeyFactory factory     = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        final PBEKeySpec       spec        = new PBEKeySpec(_secureyKey.toCharArray(), saltBytes, 65556, 256);
        final SecretKey        secretKey   = factory.generateSecret(spec);
        final SecretKeySpec    secret      = new SecretKeySpec(secretKey.getEncoded(), "AES");
        cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(ivBytes1));
        byte[] decryptedTextBytes = null;

        try
        {
            decryptedTextBytes = cipher.doFinal(encryptedTextBytes);
        }
        catch (final IllegalBlockSizeException e)
        {
            e.printStackTrace();
        }
        catch (final BadPaddingException e)
        {
            e.printStackTrace();
        }

        return new String(decryptedTextBytes);
    }

    public static void main(
            String args[])
    {
        new AESEncryptDecryptMessage();

        try
        {
            final String data = AESEncryptDecryptMessage.encrypt("919445335103", "NzEyMjY2MDAwMDAwMDAmc2FuZGJveA==");
            System.out.println(data);
            System.out.println("Decrypt - " + AESEncryptDecryptMessage.decrypt(data, "NzEyMjY2MDAwMDAwMDAmc2FuZGJveA=="));
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

}
