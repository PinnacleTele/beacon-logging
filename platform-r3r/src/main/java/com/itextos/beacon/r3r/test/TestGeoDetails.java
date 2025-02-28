package com.itextos.beacon.r3r.test;

import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.UserAgent;

public class TestGeoDetails
{

    public static void main(
            String[] args)
    {
        final String          userAgent       = "Mozilla/5.0 (iPhone; CPU iPhone OS 9_2 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13C75 Safari/601.1";
        final UserAgent       aUserAgent      = UserAgent.parseUserAgentString(userAgent);
        final OperatingSystem operatingSystem = aUserAgent.getOperatingSystem();
        System.out.println("Os Name : - " + operatingSystem.getName());
        System.out.println("Device Type : - " + operatingSystem.getDeviceType());
        System.out.println("Device name : - " + operatingSystem.getDeviceType().getName());
        System.out.println("Os Platform : - " + operatingSystem.getGroup());
        System.out.println();
    }

}
