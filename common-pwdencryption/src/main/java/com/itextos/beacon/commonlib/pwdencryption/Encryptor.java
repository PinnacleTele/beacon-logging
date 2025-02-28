package com.itextos.beacon.commonlib.pwdencryption;

import java.net.URLEncoder;
import java.util.Base64;

import com.itextos.beacon.commonlib.constants.Constants;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;

public class Encryptor
{

    // private static final short MAX_ITERATIONN = 10;

    private Encryptor()
    {}

    public static EncryptedObject encrypt(
            CryptoType aCryptoType,
            String aToEncrypt,
            String aKey)
            throws Exception
    {
        EncryptedObject returnValue = null;

        switch (aCryptoType)
        {
            case ENCRYPTION_AES_256:
                returnValue = Aes256Encrypt.encrypt(aToEncrypt, aKey);
                break;

            case HASHING_BCRYPT:
                returnValue = BcryptHashing.hash(aToEncrypt);
                break;

            case EMPTY:
                returnValue = new EncryptedObject(aToEncrypt, "");
                break;

            case ENCODE:
                returnValue = new EncryptedObject(aToEncrypt, Base64.getEncoder().encodeToString(aToEncrypt.getBytes()));
                break;

            default:
                throw new ItextosRuntimeException("Invalid crypto type specified. CryptoType : '" + aCryptoType + "'");
        }
        return returnValue;
    }

    public static String decrypt(
            CryptoType aCryptoType,
            String aToDecrypt,
            String aKey)
            throws Exception
    {
        String returnValue = null;

        try {
        switch (aCryptoType)
        {
            case ENCRYPTION_AES_256:
                returnValue = Aes256Encrypt.decrypt(aToDecrypt, aKey);
                break;

            case ENCODE:
                returnValue = new String(Base64.getDecoder().decode(aToDecrypt));
                break;

            case HASHING_BCRYPT:
            case EMPTY:
                throw new ItextosRuntimeException("Not applicable for decrypt. CryptoType : '" + aCryptoType + "'");

            default:
                throw new ItextosRuntimeException("Invalid crypto type specified. CryptoType : '" + aCryptoType + "'");
        }
        }catch(Exception e) {
        	
        }
        return returnValue;
    }

    public static EncryptedObject getEncryptedDbPassword(
            String aDbPassword)
            throws Exception
    {
        return encrypt(CryptoType.ENCRYPTION_AES_256, aDbPassword, PasswordConstants.DB_PASSWORD_KEY);
    }

    public static String getDecryptedDbPassword(
            String aEncryptedDbPassword)
            throws Exception
    {
        return decrypt(CryptoType.ENCRYPTION_AES_256, aEncryptedDbPassword, PasswordConstants.DB_PASSWORD_KEY);
    }

    public static EncryptedObject getApiPassword()
            throws Exception
    {
        return encrypt(CryptoType.ENCRYPTION_AES_256, RandomString.getApiPassword(), PasswordConstants.API_PASSWORD_KEY);
    }

    public static String getApiDecryptedPassword(
            String aDbPassword)
            throws Exception
    {
        return decrypt(CryptoType.ENCRYPTION_AES_256, aDbPassword, PasswordConstants.API_PASSWORD_KEY);
    }

    public static EncryptedObject getSmppPassword()
            throws Exception
    {
        return encrypt(CryptoType.ENCRYPTION_AES_256, RandomString.getSmppPassword(), PasswordConstants.SMPP_PASSWORD_KEY);
    }

    public static String getSmppDecryptedPassword(
            String aDbPassword)
            throws Exception
    {
        return decrypt(CryptoType.ENCRYPTION_AES_256, aDbPassword, PasswordConstants.SMPP_PASSWORD_KEY);
    }

    public static EncryptedObject getGuiPassword()
            throws Exception
    {
        return encrypt(CryptoType.HASHING_BCRYPT, RandomString.getGuiPassword(), null);
    }
    
    public static EncryptedObject getGuiPassword(String password)
            throws Exception
    {
        return encrypt(CryptoType.HASHING_BCRYPT, password, null);
    }
    
    public static String getGuiDecryptedPassword(String pass)
            throws Exception
    {
        return decrypt(CryptoType.ENCODE, pass, null);
    }

    public static void main(
            String[] args)
    {

        try
        
        
        

        {
        /*	String type=args[0];
        	String encode=args[1];
        	
        	if(type.equals("db")) {
        		
        		  final String lDecryptedDbPassword = getDecryptedDbPassword(encode);
                  System.out.println(lDecryptedDbPassword);
                  
        	}else if(type.equals("smpp")) {
        	
      		  final String lDecryptedDbPassword = getSmppDecryptedPassword(encode);
              System.out.println(lDecryptedDbPassword);
    
        	}else if(type.equals("api")) {
        	
      		  final String lDecryptedDbPassword = getApiDecryptedPassword(encode);
              System.out.println(lDecryptedDbPassword);
    
        	}*/

        	  String lDecryptedSmppPassword = getSmppDecryptedPassword("UeAxlOVCQjELmFxwx1QEXjNoTDAwOW12ZXo31l8wDYEBY2wM81C45A+o");
                System.out.println(lDecryptedSmppPassword);//Pf5X7wpt  //  hs+ItzXLENtN1DfrvK1teWxlYjFhdWpCYkRtdo3iEJ+IRekwl8/G7fn3

        	
        	  String lApiPassword = getApiDecryptedPassword("46AsqfI5S3lclHDF4GbXe0YyZkp5SVhWdWeN7Iin9T5APveHY+CtB1ir");
          //   System.out.println(lApiPassword);//WPzDMqbaVqG8  // i1GTPzBhdrTTErUi2F6U629SVVNLNWF3dFZYTXn/pv6C3V77YZLFNXYY
             

         	 //pinnacle1

         	  lDecryptedSmppPassword = getSmppDecryptedPassword("jGXYnyp2ns1iv3q5Kl4pEUllaHdWT0ltc0j2TCNtN+L/DbMlWtn93k8c");
         	
         //       System.out.println(lDecryptedSmppPassword);// 7R8es6Dg  //  i1GTPzBhdrTTErUi2F6U629SVVNLNWF3dFZYTXn/pv6C3V77YZLFNXYY

                lApiPassword = getApiDecryptedPassword("2Sdu9dt/vgQ/E5/VkUJtz2w2bmltTFBaUkt4iDJjHQNrj8628IGDVD8c");
             //   System.out.println(lApiPassword);// b0vlFpgZ7Pm6 ,7zhFMHs49ziY,ftW0BphOQQtG // u0SE1rkBCPmKvC+5LsiLCnpzOERjSXM0RXY5Gcqxmf/02rqv4MX5mB8H
                  
                  
            // $2a$10$QZbSGc10EcUdBX9Z00Hmgepw5RK8RFaP4xPbcZX87GEWddoUGHna.
        	// gui ; EncryptedObject [mActualString=HyePFO6oNqNz, mEncryptedWithIvAndSalt=$2a$10$d8jxByZmbR2ZFziViQbSneaGzPJ5rs/R.c8J6n1OyvBm8zrVhrHpa]

           //  System.out.println(" gui : "+getGuiPassword("test@123"));// $2a$10$bOcAL/Bp4sEeMrZM0IJfVueXiux3UJT5JzmlT1Bx5YPSQDJE8RwFi
         	//
             lApiPassword = getApiDecryptedPassword("O3qFcs1c5VVFk3++2mG3D0FrMUxWdGtkcUplWxQtjQmiXylt8NDgNRj+");
             //
        //     System.out.println(lApiPassword);//LSIqeA3pGMsi
        	
            
             

        	 String lDbPassword = getDecryptedDbPassword("N5mIleJjtYx2EFg8+cd3uFpGaUgxdEpKQjde+JBw9AjmsAX7iQEVAvlI");

      //     System.out.println(lDbPassword);//Sy5Cf8@123


     lDbPassword = getDecryptedDbPassword("YSrU1+RIM5hwN+ycQQdy4XhlOGhoU3RrYmtELgjZwSBQBXSHJ8AUHk/I");

      //    System.out.println(lDbPassword);//itextos@202110

     final String lEncodedString = URLEncoder.encode(String.valueOf("a"), Constants.ENCODER_FORMAT);
   
     System.out.println(lEncodedString);
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void main1(
            String[] args)
    {

        try
        {
            final EncryptedObject lEncryptedObject        = getSmppPassword();
            final String          lActualString           = lEncryptedObject.getActualString();
            final String          lEncryptedWithIvAndSalt = lEncryptedObject.getEncryptedWithIvAndSalt();
            final String          s                       = getSmppDecryptedPassword(lEncryptedWithIvAndSalt);
       //     System.out.println("'" + lActualString + "', '" + lEncryptedWithIvAndSalt + "', '" + s + "'\t" + lActualString.equals(s));

        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
        
    }

}