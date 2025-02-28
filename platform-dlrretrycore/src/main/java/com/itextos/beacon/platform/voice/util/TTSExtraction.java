package com.itextos.beacon.platform.voice.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;

public class TTSExtraction
{

    private static final Log log = LogFactory.getLog(TTSExtraction.class);

    public List<Object> extractOTP(
            String aTemplate,
            String aMessage,
            String[] aClean,
            int aTtsCount,
            int[] aOrder,
            String[] aDataType,
            String aDateFormat)
            throws Exception
    {
        // validate
        if ((aTemplate == null) || (aMessage == null))
            throw new Exception("template/message should not be null/empty template=" + aTemplate + " message=" + aMessage);

        if (aTtsCount != aDataType.length)
            throw new Exception("Not all the datatypes specified ttsCount=" + aTtsCount + " datatype=" + Arrays.toString(aDataType));

        final List<String> lTemplateWords = new ArrayList<>(Arrays.asList(aTemplate.split(" ")));
        final List<String> lContentWords  = new ArrayList<>(Arrays.asList(aMessage.split(" ")));

        if (lTemplateWords.size() != lContentWords.size())
            throw new Exception("template words count and message words count is not matching template=" + aTemplate + " templatewords count=" + lTemplateWords.size() + " message=" + aMessage
                    + " contentWords count=" + lContentWords.size());

        final List<Object> ttsList = new ArrayList<>(aTtsCount);
        addNullTTS(ttsList, aTtsCount);

        lContentWords.removeAll(lTemplateWords);

        for (final int index : aOrder)
        {
            String tts      = lContentWords.get(index);
            String cleanStr = null;

            try
            {
                cleanStr = aClean != null ? aClean[index] : null;
            }
            catch (final ArrayIndexOutOfBoundsException ignore)
            {}

            if ((cleanStr != null) && !cleanStr.isEmpty())
            {
                if (log.isDebugEnabled())
                    log.debug("Clean str=" + cleanStr + " Chomping " + tts + " -->");

                tts = StringUtils.chomp(tts, cleanStr);
                final String e1 = StringUtils.substringAfter(tts, cleanStr);

                if (!e1.isEmpty())
                    tts = e1;

                if (log.isDebugEnabled())
                    log.debug("tts chomped=" + tts);
                addTTS(ttsList, index, tts, aDataType[index], aDateFormat);
            }
            else
                addTTS(ttsList, index, tts, aDataType[index], aDateFormat);
        }

        return ttsList.size() > 0 ? ttsList : null;
    }

    // if after common words removal the left count of words matches expected TTS
    // count the template is considered to be a match
    public Map<String, Object> getMatchingTemplate(
            String aMessage,
            List<Object> lTemplateInfoList)
    {
        Map<String, Object> lMatchedTemplateMap = null;

        for (final Object lTemplateInfo : lTemplateInfoList)
        {
            final Map<String, Object> templateMap     = (Map<String, Object>) lTemplateInfo;
            final String              matchedTemplate = (String) templateMap.get("MSG_TEMPLATE");
            final int                 ttsCount        = Integer.parseInt(templateMap.get("TTS_COUNT").toString());
            final ArrayList<String>   list1           = new ArrayList<>(Arrays.asList(matchedTemplate.replaceAll(" +", " ").split(" ")));
            if (log.isDebugEnabled())
                log.debug("Matched Template List :" + list1);
            final ArrayList<String> list2 = new ArrayList<>(Arrays.asList(aMessage.replaceAll(" +", " ").split(" ")));

            if (log.isDebugEnabled())
                log.debug(" Messgae List :" + list2);

            if (list1.size() == list2.size())
            {
                list2.removeAll(list1);

                if (list2.size() == ttsCount)
                {
                    if (log.isDebugEnabled())
                        log.debug("Matching template =" + matchedTemplate + " \nfor input message=" + aMessage);
                    lMatchedTemplateMap = templateMap;
                    break;
                }
            }
        }

        log.error(" No template matched for message=" + aMessage + "\n from templatelist=" + lTemplateInfoList);

        return lMatchedTemplateMap;
    }

    private void addNullTTS(
            List<Object> ttsList,
            int count)
    {

        for (int i = 0; i < count; i++)
        {
            ttsList.add(i, null);
            if (log.isDebugEnabled())
                log.debug("adding null index=" + count);
        }

        if (log.isDebugEnabled())
            log.debug("filled with empty" + ttsList);
    }

    private static void addTTS(
            List<Object> aTtsList,
            int aIndex,
            String aTts,
            String aDataType,
            String aDateFormat)
            throws Exception
    {
        if (log.isDebugEnabled())
            log.debug("setting tts=" + aTts + " index=" + aIndex + " dataType=" + aDataType);

        if (aDataType != null)
            if (aDataType.equalsIgnoreCase("string"))

                aTtsList.add(aTts);
            else
                if (aDataType.equalsIgnoreCase("date"))
                    aTtsList.add(DateTimeUtility.getFormattedDateTime(DateTimeUtility.getDateFromString(aTts, aDateFormat), DateTimeFormat.DEFAULT));
                else
                    throw new Exception("Invalid data type specified datatype=" + aDataType);
    }

    public static void main(
            String args[])
            throws Exception
    {
        final String[]            clean      =
        { "", ".At", "." };
        String                    ttsInfo    = null;
        final int[]               order      =
        { 0, 1 };
        final String[]            dataType   =
        { "string", "string" };
        final String              dateFormat = "ddMMMyy";
        final String              orgi_msg   = "123456 is your OTP for Netbanking transaction of INR 567890 for Payee Mr. Shriram (b) Shantaram Gokhale. Please do not share with anyone.";
        final String              tmp1       = "XXXXXX is your OTP for Netbanking transaction of INR XXXXXX for Payee Mr. Shriram (b) Shantaram Gokhale. Please do not share with anyone.";
        final List                tmpLst     = new ArrayList();
        final TTSExtraction       extraction = new TTSExtraction();
        final Map<String, Object> tmpMap     = new HashMap<>();
        tmpMap.put("MSG_TEMPLATE", tmp1);
        tmpMap.put("TTS_COUNT", "2");

        tmpLst.add(tmpMap);

        final Map<String, Object> _tmpMap = extraction.getMatchingTemplate(orgi_msg, tmpLst);

        System.out.println(_tmpMap);

        final int          ttsCnt = 2;

        /*
         * List<Object> op=new TTSExtraction().
         * extractOTP("Pls use ****** as One Time Password(OTP)to create your new ATMPIN at any HDFCBank ATM.This OTP is valid till ******.At the ATM,insert Debit Card&select'Create New ATM PIN'Option.Enter OTP&your mobile no.Now create 4digit ATMPIN.This OTP is for HDFC Bank DebitCard ending xxxxxx."
         * ,
         * "Pls use 222222 as One Time Password(OTP)to create your new ATMPIN at any HDFCBank ATM.This OTP is valid till 10May17.At the ATM,insert Debit Card&select'Create New ATM PIN'Option.Enter OTP&your mobile no.Now create 4digit ATMPIN.This OTP is for HDFC Bank DebitCard ending 333333."
         * ,clean,ttsCnt,order,dataType,dateFormat);
         */
        /*
         * List<Object> op=new TTSExtraction().
         * extractOTP("XXXXXX is your OTP for Netbanking transaction of INR XXXXXX for Payee Mr. Shriram (b) Shantaram Gokhale. Please do not share with anyone."
         * ,
         * "123456 is your OTP for Netbanking transaction of INR 567890 for Payee Mr. Shriram (b) Shantaram Gokhale. Please do not share with anyone."
         * ,clean,2,order,dataType,dateFormat);
         */
        /*
         * List<Object> op=new TTSExtraction().extractOTP("Your OTP Password is ****",
         * "Your OTP Password is 0123"
         * ,clean,1,order,dataType,dateFormat);
         */

        final List<Object> op     = new TTSExtraction().extractOTP(
                "One Time Password for Verified by Visa on ur HDFC Bank DbCard **** is *****. Pls use this password to complete your password re-set.",
                "One Time Password for Verified by Visa on ur HDFC Bank DbCard 1234 is 4566. Pls use this password to complete your password re-set.", clean, ttsCnt, order, dataType, dateFormat);

        System.out.println(op);

        for (final Object tts : op)
            if (ttsInfo == null)
            {
                if (tts != null)
                    ttsInfo = tts.toString();
            }
            else
                ttsInfo = ttsInfo + "-$-" + tts;
        System.out.println("TTSINfo : " + ttsInfo);
    }

}
