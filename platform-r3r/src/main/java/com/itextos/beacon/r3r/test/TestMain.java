package com.itextos.beacon.r3r.test;

import java.sql.Timestamp;

import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;

public class TestMain
{

    public static void main(
            String[] args)
    {
        // DataProcessor.getInstance();
        // final String url = UrlRedirectProcessor.processRequestInfo(new
        // R3CUserInfo("/d9Ne9", "", "", -1));
        // System.out.println(url);

        final String    time   = "1648224979243";

        final String    date   = DateTimeUtility.getFormattedDateTime(Long.valueOf(time), DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS);
        final Timestamp sqlNow = Timestamp.valueOf(date);
        System.out.println(sqlNow);
    }

}
