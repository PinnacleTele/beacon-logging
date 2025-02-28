package com.itextos.beacon.commonlib.utility;

import java.security.Key;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;

public class Basic3DESEncryption
{

    private static final Log log = LogFactory.getLog(Basic3DESEncryption.class);
    private String           fullAlgorithm;
    private String           keyAlgorithm;

    public Basic3DESEncryption(
            String algorithm) throws ItextosRuntimeException
    {

        if (algorithm == null)
        {
            log.error("No algorithm provided");
            throw new ItextosRuntimeException("No algorithm provided");
        }

        final int slashIndex = algorithm.indexOf("/");

        if (slashIndex == -1)
        {
            fullAlgorithm = algorithm;
            keyAlgorithm  = algorithm;
        }
        else
        {
            fullAlgorithm = algorithm;
            keyAlgorithm  = algorithm.substring(0, slashIndex);
        }
    }

    public byte[] encrypt(
            byte[] keyBytes,
            byte[] data)
            throws ItextosException
    {
        byte[] cipherText = null;

        try
        {
            final Key    key = convertBytesToKey(keyBytes);
            final Cipher c   = Cipher.getInstance(fullAlgorithm);
            c.init(Cipher.ENCRYPT_MODE, key);
            cipherText = c.doFinal(data);
        }
        catch (final Exception e)
        {
            throw new ItextosException("Exception while encrypting the data", e);
        }

        return cipherText;
    }

    public String encrypt(
            String hexKey,
            String plainText)
            throws ItextosException
    {
        byte[] result        = null;
        String encryptedText = "";

        try
        {
            final byte[] keyBytes  = stringToBytes(hexKey);
            final byte[] textBytes = plainText.getBytes();
            result        = encrypt(keyBytes, textBytes);
            encryptedText = byteToHexString(result);
        }
        catch (final ItextosException e)
        {
            throw new ItextosException("Exception while encrypting the data", e);
        }

        return encryptedText;
    }

    public byte[] decrypt(
            byte[] keyBytes,
            byte[] encryptedData)
            throws ItextosException
    {
        byte[] text = null;

        try
        {
            final Key    key = convertBytesToKey(keyBytes);
            final Cipher c   = Cipher.getInstance(fullAlgorithm);
            c.init(Cipher.DECRYPT_MODE, key);
            text = c.doFinal(encryptedData);
        }
        catch (final Exception e)
        {
            throw new ItextosException("Exception while decrypting the data", e);
        }

        return text;
    }

    public String decrypt(
            String hexKey,
            String hexCipherText)
            throws ItextosException
    {
        byte[] result        = null;
        String decryptedText = "";

        try
        {
            final byte[] keyBytes    = stringToBytes(hexKey);
            final byte[] cipherBytes = stringToBytes(hexCipherText);
            result        = decrypt(keyBytes, cipherBytes);
            decryptedText = byteToCharString(result);
        }
        catch (final ItextosException e)
        {
            throw new ItextosException("Exception while decrypting the data", e);
        }

        return decryptedText;
    }

    public static Map<String, String> parseToken(
            String token)
    {
        final Map<String, String> result = new HashMap<>();
        final StringTokenizer     st     = new StringTokenizer(token, "&");

        while (st.hasMoreTokens())
        {
            final String          nameValue = st.nextToken();
            final StringTokenizer eTok      = new StringTokenizer(nameValue, "=");
            String                name      = null;
            String                value     = null;

            if (eTok.hasMoreTokens())
                name = eTok.nextToken();

            if (eTok.hasMoreTokens())
                value = eTok.nextToken();

            if ((name != null) || (value != null))
                result.put(name, value);
        }

        return result;
    }

    public Key convertBytesToKey(
            byte[] keyBytes)
            throws ItextosException
    {
        SecretKeySpec keySpec = null;

        try
        {
            keySpec = new SecretKeySpec(keyBytes, keyAlgorithm);
        }
        catch (final Exception e)
        {
            throw new ItextosException("Exception while decrypting the data", e);
        }

        return keySpec;
    }

    public String generateKey()
            throws ItextosException
    {
        String keyText = "";

        try
        {
            final KeyGenerator keyGenerator = KeyGenerator.getInstance(keyAlgorithm);
            final SecretKey    key          = keyGenerator.generateKey();
            final byte[]       keyBytes     = key.getEncoded();
            keyText = byteToHexString(keyBytes);
        }
        catch (final Exception e)
        {
            throw new ItextosException("Exception while generating the keys", e);
        }

        return keyText;
    }

    public static byte[] stringToBytes(
            String hex)
    {
        final int    len = hex.length();
        final byte[] buf = new byte[((len + 1) / 2)];

        int          i   = 0;
        int          j   = 0;

        if ((len % 2) == 1)
            buf[j++] = (byte) fromDigit(hex.charAt(i++));

        while (i < len)
            buf[j++] = (byte) ((fromDigit(hex.charAt(i++)) << 4) | fromDigit(hex.charAt(i++)));

        return buf;
    }

    public static int fromDigit(
            char ch)
    {
        if ((ch >= '0') && (ch <= '9'))
            return ch - '0';

        if ((ch >= 'A') && (ch <= 'F'))
            return (ch - 'A') + 10;

        if ((ch >= 'a') && (ch <= 'f'))
            return (ch - 'a') + 10;
        throw new IllegalArgumentException("invalid hex digit '" + ch + "'");
    }

    public static String byteToHexString(
            byte[] bytes)
    {
        final StringBuilder buf = new StringBuilder();

        for (final byte c : bytes)
        {
            final int b      = (c) & 0xff;
            String    hexVal = Integer.toHexString(b);

            if (hexVal.length() == 1)
                hexVal = "0" + hexVal;
            buf.append(hexVal);
        }

        return buf.toString();
    }

    public static String byteToCharString(
            byte[] bytes)
    {
        final StringBuilder buf = new StringBuilder();

        for (final byte b : bytes)
            buf.append((char) b);

        return buf.toString();
    }

}