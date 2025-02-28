package com.itextos.beacon.platform.messagetool;

import java.net.URLEncoder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Constants;
import com.itextos.beacon.commonlib.utility.MessageConvertionUtility;

public class MsgIdentifierUtil
{
 
	private static Log log = LogFactory.getLog(MsgIdentifierUtil.class);
	
    private MsgIdentifierUtil()
    {}

    public static Response messageIdentifier(
            String aClientId,
            int aAccountLevelSplCharLength,
            int aAccountLevelOccuranceCount,
            boolean aRemoveUcCharsInPlainMessage,
            String aMessage)
    {
        final Request  req       = new Request(aClientId, aMessage, aAccountLevelSplCharLength, aAccountLevelOccuranceCount, aRemoveUcCharsInPlainMessage);

        final Response lResponse = UcIdentifier.checkForUnicode(req);

        if (lResponse.isIsUniCode())
            lResponse.setMessage(MessageConvertionUtility.convertString2HexString(lResponse.getMessage()));

        return lResponse;
    }
    
    public static Response checkForUnicode(
            String aMessage)
    {
        final Response     response             = new Response();

        // If the Special char word count is > 3 we should consider as UC message.
        boolean            isMsgUnicode         = false;
         
        String             lMessage             = aMessage;

        if (log.isDebugEnabled())
            log.debug("Request Object " + lMessage);

        for (final char lChar : lMessage.toCharArray())
        {
            if (log.isDebugEnabled())
                log.debug("Total number of characters " + lMessage.length());

            try
            {
                final String lEncodedString = URLEncoder.encode(String.valueOf(lChar), Constants.ENCODER_FORMAT);

                if (log.isDebugEnabled())
                    log.debug("Encoded String :" + lEncodedString);

                if (lEncodedString.length() > 3)
                {
                	isMsgUnicode = true;
                }
                
            }
            catch (final Exception e)
            {
                log.error("Ingore the exception .", e);
            }
        }
      
        lMessage = isMsgUnicode ? MessageConvertionUtility.convertString2HexString(lMessage) : lMessage;

        response.setUnicode(isMsgUnicode);
        
        response.setMessage(lMessage);

        return response;
    }

    public static void main(
            String[] args)
    {
        final Response lResponse = messageIdentifier("6000000200000000", 3, 3, true, "Test");

        System.out.println(lResponse);
    }

}
