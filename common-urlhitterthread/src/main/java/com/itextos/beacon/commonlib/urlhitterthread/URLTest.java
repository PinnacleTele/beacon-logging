package com.itextos.beacon.commonlib.urlhitterthread;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class URLTest
{

    public static void main(
            String[] args)
    {
        final String additionalParams = "%7B%22rp%22%3A%220%22%2C%22send%22%3A%22ICICIB%22%2C%22stime%22%3A%222020-04-14+19%3A50%3A35%3A813%22%2C%22segment%22%3A%22trans%22%2C%22payload_expiry%22%3A%2220041502%22%2C%22esmeaddr%22%3A%2280382800000000%22%2C%22routeid%22%3A%22WG%22%2C%22cust_mid%22%3A%22212913262%22%2C%22priority%22%3A%221%22%2C%22cluster%22%3A%22trans%22%2C%22actual_ts%22%3A%221586874035824%22%2C%22mid%22%3A%223795233150359377900%22%2C%22payload_rid%22%3A%2221%22%2C%22tag1%22%3A%22TEST%22%7D";
        String       decode;

        try
        {
            decode = URLDecoder.decode(additionalParams, "UTF-8");
            System.out.println(new java.util.Date() + " - URLTest main decode : '" + decode + "'");
            final Map<String, String> map = getQueryMap(decode);
            System.out.println(new java.util.Date() + " - URLTest main map : '" + map + "'");
        }
        catch (final UnsupportedEncodingException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static Map<String, String> getQueryMap(
            String query)
    {
        final String[]            params = query.split("&");
        final Map<String, String> map    = new HashMap<>();

        for (final String param : params)
        {
            final String name  = param.split("=")[0];
            final String value = param.split("=")[1];
            map.put(name, value);
        }
        return map;
    }

}
