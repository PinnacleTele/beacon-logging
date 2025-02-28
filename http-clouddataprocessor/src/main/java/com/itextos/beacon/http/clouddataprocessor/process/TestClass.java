package com.itextos.beacon.http.clouddataprocessor.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Date;

import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler2;

public class TestClass
{

    public static void main(
            String[] args)
    {
        final String        url                = args[0];
        final DecimalFormat dc                 = new DecimalFormat("00");
        final int           noOfThreads        = Integer.parseInt(args[1]);
        final int           noOfHitsPerThread  = Integer.parseInt(args[2]);
        final boolean       isResponseRequired = "1".equals(args[3]);
        System.out.println(System.currentTimeMillis() + " -- " + new Date() + " - TestClass main  : 'Starting'");

        for (int index = 0; index < noOfThreads; index++)
        {
            final Thread t = new Thread(new Hitter(url, "T-" + dc.format((index + 1)), noOfHitsPerThread, isResponseRequired));
            
            ExecutorSheduler2.getInstance().addTask(t,   "T-" );
        }
    }

}

class Hitter
        implements
        Runnable
{

    private final String  url;
    private final String  name;
    private final int     hitsPerThread;
    private final boolean isResponseRequired;

    public Hitter(
            String aURL,
            String aName,
            int aHitsPerThread,
            boolean aIsResponseRequired)
    {
        url                = aURL;
        name               = aName;
        hitsPerThread      = aHitsPerThread;
        isResponseRequired = aIsResponseRequired;
    }

    @Override
    public void run()
    {
        final int      httpConnectionTimeout = 10 * 1000;
        final int      httpResponseTimeout   = 10 * 1000;
        BufferedReader in                    = null;
        boolean        isSuccess             = true;

        for (int index = 0; index < hitsPerThread; index++)
        {
            HttpURLConnection connection = null;

            try
            {
                final String surl = url + "-" + name + "-" + index;
                final URL    url  = new URL(surl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setConnectTimeout(httpConnectionTimeout);
                connection.setReadTimeout(httpResponseTimeout);

                final int iGetResultCode = connection.getResponseCode();

                if (isResponseRequired)
                {
                    in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    final StringBuffer decodedString = new StringBuffer();
                    String             temp          = null;

                    while ((temp = in.readLine()) != null)
                        decodedString.append(temp);
                }
            }
            catch (final Exception e)
            {
                e.printStackTrace();
                isSuccess = false;
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
                    e.printStackTrace();
                }

                if (connection != null)
                    connection.disconnect();
            }
        }
        if (isSuccess)
            System.out.println(System.currentTimeMillis() + " -- " + new Date() + " >> " + name + ": Completed " + hitsPerThread + " Hits to the URL " + url);
    }

}
