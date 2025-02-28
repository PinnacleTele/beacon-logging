package com.itextos.beacon.commonlib.password.servlet;

import com.itextos.beacon.commonlib.pwdencryption.EncryptedObject;
import com.itextos.beacon.commonlib.pwdencryption.Encryptor;

public class M {

	public static void main(String[] args) throws Exception {
		
	     EncryptedObject apiObject  = null;

             apiObject = Encryptor.getApiPassword();
             
           String result=  getResponse("api",apiObject);
           
           
           System.out.println(result);
    
	}

	  private static String getResponse(
	            String aString,
	            EncryptedObject aEncryptedObject)
	    {
	        if (aEncryptedObject == null)
	            return "";

	        final StringBuilder sb = new StringBuilder("{");
	        sb.append("\"type\":\"").append(aString).append("\",");
	        sb.append("\"customer_password\":\"").append(aEncryptedObject.getActualString()).append("\",");
	        sb.append("\"dbinsert_password\":\"").append(aEncryptedObject.getEncryptedWithIvAndSalt()).append("\"");
	        sb.append("}");
	        return sb.toString();
	    }
}
