package com.itextos.beacon.http.clouddataprocessor.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.http.clouddataprocessor.common.logging.FailuerLogger;
import com.itextos.beacon.http.clouddataprocessor.common.logging.LogLevel;
import com.itextos.beacon.http.clouddataprocessor.common.logging.SuccessLogger;
import com.itextos.beacon.http.clouddatautil.common.CloudInterfaceFileReader;
import com.itextos.beacon.http.clouddatautil.common.CloudUtility;

public class HttpCall
{

    private final static Log    log                     = LogFactory.getLog(HttpCall.class);

    private final static int    HTTP_CONNECTION_TIMEOUT = 10 * 1000;
    private final static int    HTTP_RESPONSE_TIMEOUT   = 10 * 1000;
    private final static int    EXCEPTION_OCCURED       = -999;
    private final static String SUCCESS_STRING_GET      = "Statuscode=200";
    private final static String SUCCESS_STRING_POST     = "Request accepted";

    public static void hitTheDataToThePlatformGetUrl(
            String aData,
            int redisIndex)
    {
        BufferedReader     in           = null;
        final StringBuffer result       = new StringBuffer();
        int                responseCode = 0;
        HttpURLConnection  connection   = null;
        Throwable          exception    = null;

        try
        {
            final String platformURL = CloudInterfaceFileReader.getInstance().getPlatformURL() + aData;
            final URL    url         = new URL(platformURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setConnectTimeout(HTTP_CONNECTION_TIMEOUT);
            connection.setReadTimeout(HTTP_RESPONSE_TIMEOUT);
            connection.setRequestMethod("GET");
            responseCode = connection.getResponseCode();
            in           = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String temp = null;
            while ((temp = in.readLine()) != null)
                result.append(temp);
        }
        catch (final Exception e)
        {
            pushBackToRedis(aData, redisIndex);
            exception = e;
        }
        finally
        {

            try
            {
                if (in != null)
                    in.close();
            }
            catch (final IOException e)
            {
                // ignore it
            }

            if (connection != null)
                connection.disconnect();
        }

        if (exception == null)
        {
            final String  response          = result.toString();
            final boolean isSuccessResponse = isSuccessResponseGet(response);
            final String  message           = "[" + aData + "] [" + responseCode + "] [" + response + "]";
            if (isSuccessResponse)
                SuccessLogger.log(RequestProcess.class, LogLevel.INFO, message);
            else
                FailuerLogger.log(RequestProcess.class, LogLevel.WARN, message);
        }
        else
        {
            final String message = "[" + aData + "] [" + EXCEPTION_OCCURED + "] [" + exception.getMessage() + "]";
            FailuerLogger.log(RequestProcess.class, LogLevel.ERROR, message, exception);
        }
    }

    public static void hitTheDataToThePlatformPostUrl(
            String aData)
    {
        BufferedReader     in           = null;
        final StringBuffer result       = new StringBuffer();
        int                responseCode = 0;
        HttpURLConnection  connection   = null;
        Throwable          exception    = null;

        try
        {
            final String platformURLBulk = CloudInterfaceFileReader.getInstance().getPlatformBulkURL();
            final URL    url             = new URL(platformURLBulk);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setConnectTimeout(HTTP_CONNECTION_TIMEOUT);
            connection.setReadTimeout(HTTP_RESPONSE_TIMEOUT);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestMethod("POST");
            final OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());

            wr.write(aData);
            wr.flush();
            responseCode = connection.getResponseCode();

            in           = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String temp = null;
            while ((temp = in.readLine()) != null)
                result.append(temp);
        }
        catch (final Exception e)
        {
            exception = e;
        }
        finally
        {

            try
            {
                if (in != null)
                    in.close();
            }
            catch (final IOException e)
            {
                // ignore it
            }

            if (connection != null)
                connection.disconnect();
        }

        if (exception == null)
        {
            final String  response          = result.toString();
            final boolean isSuccessResponse = isSuccessResponsePost(response);
            final String  message           = "[" + aData + "] [" + responseCode + "] [" + response + "]";
            if (isSuccessResponse)
                SuccessLogger.log(RequestProcess.class, LogLevel.INFO, message);
            else
                FailuerLogger.log(RequestProcess.class, LogLevel.WARN, message);
        }
        else
        {
            final String message = "[" + aData + "] [" + EXCEPTION_OCCURED + "] [" + exception.getMessage() + "]";
            FailuerLogger.log(RequestProcess.class, LogLevel.ERROR, message, exception);
            hitTheDataToThePlatformPostUrl(aData);
        }
    }

    private static void pushBackToRedis(
            String aData,
            int aRedisIndex)
    {
        final List<String> list = new ArrayList<>();
        list.add(aData);
        CloudUtility.pushToRedis(list, aRedisIndex);
    }

    private static boolean isSuccessResponseGet(
            String aString)
    {
        return aString.contains(SUCCESS_STRING_GET);
    }

    private static boolean isSuccessResponsePost(
            String aString)
    {
        return aString.contains(SUCCESS_STRING_POST);
    }

    public static void generateBulkRequest(
            ArrayList<String> aList,
            Object aObject)
    {
        // TODO Auto-generated method stub
    }

}
