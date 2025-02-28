package com.itextos.beacon.commonlib.urlhitterthread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;

import com.itextos.beacon.commonlib.httpclient.HTTPRequestUtility;
import com.itextos.beacon.commonlib.httpclient.HttpHeader;
import com.itextos.beacon.commonlib.httpclient.HttpParameter;
import com.itextos.beacon.commonlib.httpclient.HttpResult;

public class HitterThread
        implements
        Runnable
{

    private static final Log log = LogFactory.getLog(HitterThread.class);

    private final int        threadIndex;
    private final int        maxIteration;

    private int              attemptCount;
    private int              successCount;
    private int              failuerCount;
    private int              exceptionCount;
    private boolean          completed;

    public HitterThread(
            int aIndex)
    {
        threadIndex  = aIndex;
        maxIteration = PropertyFileReader.getInstance().getMessagesPerThread();
    }

    @Override
    public void run()
    {
        final String message   = PropertyFileReader.getInstance().getMessageString();
        final String method    = PropertyFileReader.getInstance().getMethod();
        final String url       = PropertyFileReader.getInstance().getUrl();

        final long   startTime = System.currentTimeMillis();

        for (int index = 1; index <= maxIteration; index++)
        {
            ++attemptCount;
            if (PropertyFileReader.getInstance().useApacheClient())
                callApacheClient(index, url, method, message);
            else
                hitUrl(index, url, method, message);
        }
        final long endTime = System.currentTimeMillis();
        log.info("Time taken for the index '" + threadIndex + "' Time taken " + (endTime - startTime));
        completed = true;
    }

    public boolean isCompleted()
    {
        return completed;
    }

    private void hitUrl(
            int aIndex,
            String aUrl,
            String aMethod,
            String aMessage)
    {
        if (aUrl.startsWith("https"))
            callHttpSecureMethod(aIndex, aUrl, aMethod, aMessage);
        else
            callHttpMethod(aIndex, aUrl, aMethod, aMessage);
    }

    private void callHttpSecureMethod(
            int aIndex,
            String aUrl,
            String aMethod,
            String aMessage)
    {
        if ("G".equalsIgnoreCase(aMethod))
            callSecureGetMethod(aIndex, aUrl, aMessage);
        else
            if ("P".equalsIgnoreCase(aMethod))
                callSecurePostMethod(aIndex, aUrl, aMessage);
    }

    private void callHttpMethod(
            int aIndex,
            String aUrl,
            String aMethod,
            String aMessage)
    {
        if ("G".equalsIgnoreCase(aMethod))
            callGetMethod(aIndex, aUrl, aMessage);
        else
            if ("P".equalsIgnoreCase(aMethod))
                callPostMethod(aIndex, aUrl, aMessage);
    }

    private void callPostMethod(
            int aIndex,
            String aUrl,
            String aMessage)
    {

        try
        {
            final URL               obj = new URL(aUrl);
            final HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setReadTimeout(10000);
            con.setConnectTimeout(10000);

            // For POST only - START
            con.setDoOutput(true);
            final OutputStream os = con.getOutputStream();
            os.write(aMessage.getBytes());
            os.flush();
            os.close();
            // For POST only - END

            final int responseCode = con.getResponseCode();

            if ((responseCode == HttpURLConnection.HTTP_OK) || (responseCode == HttpURLConnection.HTTP_ACCEPTED) || (responseCode == HttpURLConnection.HTTP_CREATED))
            { // success
                final BufferedReader in       = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String               inputLine;
                final StringBuilder  response = new StringBuilder();

                while ((inputLine = in.readLine()) != null)
                    response.append(inputLine);
                in.close();
                ++successCount;
            }
            else
            {
                ++failuerCount;
                log.error("Thread " + threadIndex + " iteration " + aIndex + " Error Occured : " + responseCode);
            }
        }
        catch (final Exception e)
        {
            ++exceptionCount;

            try
            {
                throw new RuntimeException("Exception in Thread " + threadIndex + " iteration " + aIndex, e);
            }
            catch (final Exception e1)
            {
                log.error("", e1);
            }
        }
    }

    private void callGetMethod(
            int aIndex,
            String aUrl,
            String aMessage)
    {

        try
        {
            final URL               url        = new URL(aUrl + "?" + aMessage);
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(10000);

            final int    responseCode  = connection.getResponseCode();

            final String errorResponse = readResponse(connection);

            if (responseCode == HttpURLConnection.HTTP_OK)
            {
                ++successCount;
                log.debug(errorResponse);
            }
            else
            {
                ++failuerCount;
                log.error("Thread " + threadIndex + " iteration " + aIndex + " Error Occured : " + responseCode + " - " + errorResponse);
            }
        }
        catch (final Exception e)
        {
            ++exceptionCount;

            try
            {
                throw new RuntimeException("Exception in Thread " + threadIndex + " iteration " + aIndex, e);
            }
            catch (final Exception e1)
            {
                log.error("", e1);
            }
        }
    }

    private void callSecurePostMethod(
            int aIndex,
            String aUrl,
            String aMessage)
    {

        try
        {
            final URL                obj = new URL(aUrl);
            final HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setReadTimeout(10000);
            con.setConnectTimeout(10000);

            // For POST only - START
            con.setDoOutput(true);
            final OutputStream os = con.getOutputStream();
            os.write(aMessage.getBytes());
            os.flush();
            os.close();
            // For POST only - END

            final int responseCode = con.getResponseCode();

            if ((responseCode == HttpURLConnection.HTTP_OK) || (responseCode == HttpURLConnection.HTTP_ACCEPTED) || (responseCode == HttpURLConnection.HTTP_CREATED))
            { // success
                final BufferedReader in       = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String               inputLine;
                final StringBuilder  response = new StringBuilder();

                while ((inputLine = in.readLine()) != null)
                    response.append(inputLine);
                in.close();
                ++successCount;
            }
            else
            {
                ++failuerCount;
                log.error("Thread " + threadIndex + " iteration " + aIndex + " Error Occured : " + responseCode);
            }
        }
        catch (final Exception e)
        {
            ++exceptionCount;

            try
            {
                throw new RuntimeException("Exception in Thread " + threadIndex + " iteration " + aIndex, e);
            }
            catch (final Exception e1)
            {
                log.error("", e1);
            }
        }
    }

    private void callSecureGetMethod(
            int aIndex,
            String aUrl,
            String aMessage)
    {

        try
        {
            final URL                url        = new URL(aUrl + "?" + aMessage);
            final HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(10000);

            final int    responseCode  = connection.getResponseCode();
            final String errorResponse = readResponse(connection);

            if (responseCode == HttpURLConnection.HTTP_OK)
            {
                ++successCount;
                log.debug(errorResponse);
            }
            else
            {
                ++failuerCount;
                log.error("Thread " + threadIndex + " iteration " + aIndex + " Error Occured : " + responseCode + " - " + errorResponse);
            }
        }
        catch (final Exception e)
        {
            ++exceptionCount;

            try
            {
                throw new RuntimeException("Exception in Thread " + threadIndex + " iteration " + aIndex, e);
            }
            catch (final Exception e1)
            {
                log.error("", e1);
            }
        }
    }

    private static String readResponse(
            HttpURLConnection aConnection)
            throws IOException
    {
        final InputStream    inputStream = aConnection.getInputStream();
        final BufferedReader reader      = new BufferedReader(new InputStreamReader(inputStream));

        String               line        = null;
        final StringBuilder  response    = new StringBuilder();
        while ((line = reader.readLine()) != null)
            response.append(line).append("\n");
        reader.close();
        inputStream.close();
        return response.toString();
    }

    public int getAttemptCount()
    {
        return attemptCount;
    }

    public int getSuccessCount()
    {
        return successCount;
    }

    public int getFailuerCount()
    {
        return failuerCount;
    }

    public int getExceptionCount()
    {
        return exceptionCount;
    }

    private static void callApacheClient(
            int aIndex,
            String aUrl,
            String aMethod,
            String aMessage)
    {
        final String url                = aUrl + "?" + aMessage;
        HttpResult   lProcessGetRequest = null;

        if ("G".equalsIgnoreCase(aMethod))
            lProcessGetRequest = HTTPRequestUtility.processGetRequest(url, new HttpParameter<>(), new HttpHeader<>());
        else
            lProcessGetRequest = HTTPRequestUtility.doPostRequest(url, new HttpParameter<>(), new HttpHeader<>());

        if (lProcessGetRequest.getStatusCode() != HttpStatus.SC_OK)
            log.error("Error connecting to URL '" + url + "'. Iteration : '" + aIndex + "' Error Response '" + lProcessGetRequest.getErrorString() + "'");

        // if (log.isDebugEnabled())
        // log.debug(lProcessGetRequest.getResponseString());
    }

}
