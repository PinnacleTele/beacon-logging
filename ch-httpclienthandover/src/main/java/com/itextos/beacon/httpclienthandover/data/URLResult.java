package com.itextos.beacon.httpclienthandover.data;

import com.itextos.beacon.commonlib.httpclient.HttpResult;

public class URLResult
{

    private final String     url;
    private final String     completeURL;
    private final HttpResult httpResult;

    public URLResult(
            String aURL,
            String aCompleteURL,
            HttpResult aResult)
    {
        url         = aURL;
        completeURL = aCompleteURL;
        httpResult  = aResult;
    }

    public String getUrl()
    {
        return url;
    }

    public String getCompleteURL()
    {
        return completeURL;
    }

    public HttpResult getHttpResult()
    {
        return httpResult;
    }

    @Override
    public String toString()
    {
        return "URLResult [url=" + url + ", completeURL=" + completeURL + ", httpResult=" + httpResult + "]";
    }

}
