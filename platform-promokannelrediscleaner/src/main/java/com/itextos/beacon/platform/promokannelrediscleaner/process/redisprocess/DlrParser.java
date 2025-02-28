package com.itextos.beacon.platform.promokannelrediscleaner.process.redisprocess;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang.StringUtils;

import com.google.gson.Gson;

class DlrParser
{

    private static final String STR_ADDL_INFO    = "add_info";
    private static final String STR_ADDL_QUESION = "?";
    private static final String STR_ADDL_AND     = "&";
    private static final String STR_ADDL_EQUAL   = "=";

    public static void main(
            String[] args)
    {
        final String    s         = "http%3A%2F%2F192.168.1.245%3A8480%2Fdnr%2Fdlrreceiver%3Fdr%3D%25a%26smscid%3D%25i%26statuscd%3D%25d%26add_info%3D%257B%2522car_ts_format%2522%253A%2522yyMMddHHmm%2522%252C%2522msg_create_ts%2522%253A%25221635782100141%2522%252C%2522pl_rds_id%2522%253A%25221%2522%252C%2522intl_msg%2522%253A%25220%2522%252C%2522intf_type%2522%253A%2522http_japi%2522%252C%2522pl_exp%2522%253A%252221110204%2522%252C%2522rty_atmpt%2522%253A%25220%2522%252C%2522sms_priority%2522%253A%25225%2522%252C%2522recv_ts%2522%253A%25222021-11-01%2B19%253A05%253A37.987%2522%252C%2522platform_cluster%2522%253A%2522bulk%2522%252C%2522intf_grp_type%2522%253A%2522api%2522%252C%2522msg_type%2522%253A%25221%2522%252C%2522rute_id%2522%253A%2522VDTOAA%2522%252C%2522c_id%2522%253A%25224000000200000012%2522%252C%2522m_id%2522%253A%25222882111011905390043300%2522%257D%26systemid%3D%25o";
        final UrlObject lFromJson = getUrlObject(s);
        System.out.println(lFromJson);
    }

    static UrlObject getUrlObject(
            String aString)
    {

        try
        {
            final String firstDecode      = URLDecoder.decode(aString, StandardCharsets.UTF_8);
            final int    questionPosition = firstDecode.indexOf(STR_ADDL_QUESION);

            if (questionPosition > 0)
            {
                final String   parameters   = firstDecode.substring(questionPosition + 1);
                final String[] allKeyValues = StringUtils.split(parameters, STR_ADDL_AND);

                if (allKeyValues != null)
                    for (final String temp : allKeyValues)
                    {
                        final String[] encoded = StringUtils.split(temp, STR_ADDL_EQUAL);

                        if ((encoded != null) && (encoded.length > 1) && (STR_ADDL_INFO.equals(encoded[0])))
                        {
                            final String    encodedJson = URLDecoder.decode(encoded[1], StandardCharsets.UTF_8);
                            final UrlObject urlObject   = new Gson().fromJson(encodedJson, UrlObject.class);
                            urlObject.setValid(true);
                            return urlObject;
                        }
                    }
            }
        }
        catch (final Exception e)
        {
            // ignore caller has to handle the null cases.
        }
        return new UrlObject();
    }

}
