package com.itextos.beacon.r3r.test;

import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;

public class TestComponent
{

    public static void main(
            String[] args)
    {
        // System.out.println("Start Time " + System.currentTimeMillis());
        // final Map<String, Object> shortCodeDataMap =
        // EsProcess.getShortCodeData("L7AQ0");
        // System.out.println("End Time " + System.currentTimeMillis());
        // final String userAgent = " Mozilla/5.0 (Windows NT 10.0; Win64; x64)
        // AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.164 Safari/537.36";
        // UrlRedirectProcessor.processRequestInfo("/L7AQ0", userAgent, "10.10.20.104");
        System.out.println(DateTimeUtility.getFormattedDateTime(System.currentTimeMillis(), DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS));

        // final R3RObject r3rObject = new R3RObject();
        // r3rObject.setClientId("600000020000002");
        // r3rObject.setDest("+917845705553");
        // r3rObject.setMsgId("1602107161800110002600");
        // r3rObject.setIpAddress("127.0.0.1");
        // r3rObject.setCountryCode("IN");
        // r3rObject.setCountryName("India");
        // r3rObject.setRegion("Tamil Nadu");
        // r3rObject.setCity("Madurai");
        // r3rObject.setLongitude("9.3333");
        // r3rObject.setLatitude("9.3333");
        // r3rObject.setOsName("Windows");
        // r3rObject.setOsGroup("MicroSoft");
        // r3rObject.setBrowserName("Chrome");
        // r3rObject.setBrowserVersion("0.9");
        // r3rObject.setDeviceName("Mobile");
        // r3rObject.setRequestTime("2021-07-19 16:00:01.123");
        // r3rObject.setSmartLinkId("1234567");
        // r3rObject.setShortenUrl("http://nunzioz.in/L7AQ0");
        // r3rObject.setShortCode("L7AQ0");
        // r3rObject.setRedirectUrl("http://peppertap.com/");
        // r3rObject.setUserAgent("");
        // r3rObject.setCustomParams("");
        //
        // try
        // {
        // DBHandler.insertRecords(r3rObject);
        // }
        // catch (final ItextosException e)
        // {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
    }

}
