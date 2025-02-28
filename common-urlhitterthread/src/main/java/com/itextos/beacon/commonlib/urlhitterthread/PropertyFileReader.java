package com.itextos.beacon.commonlib.urlhitterthread;

import java.io.FileInputStream;
import java.util.Properties;

import com.itextos.beacon.commonlib.utility.CommonUtility;

public class PropertyFileReader
{

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final PropertyFileReader INSTANCE = new PropertyFileReader();

    }

    public static PropertyFileReader getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private String  method;
    private int     noOfThreads;
    private String  url;
    private String  messageString;
    private int     messagesPerThread;
    private boolean useApacheClient;

    private PropertyFileReader()
    {

        try (
                FileInputStream fis = new FileInputStream(System.getProperty("hitter.properties.file.path")))
        {
            final Properties props = new Properties();
            props.load(fis);
            props.list(System.out);

            method            = CommonUtility.nullCheck(props.getProperty("url.hit.http.method"), true);
            url               = CommonUtility.nullCheck(props.getProperty("url.hit.http.url"), true);
            messageString     = CommonUtility.nullCheck(props.getProperty("url.hit.message"), true);
            noOfThreads       = CommonUtility.getInteger(props.getProperty("url.hit.http.thread.count", "10"));
            messagesPerThread = CommonUtility.getInteger(props.getProperty("url.hit.messages.per.thread", "1"));
            useApacheClient   = CommonUtility.isTrue(props.getProperty("use.apache.http", "n"));
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

    public String getMethod()
    {
        return method;
    }

    public int getNoOfThreads()
    {
        return noOfThreads;
    }

    public String getUrl()
    {
        return url;
    }

    public String getMessageString()
    {
        return messageString;
    }

    boolean useApacheClient()
    {
        return useApacheClient;
    }

    public int getMessagesPerThread()
    {
        return messagesPerThread;
    }

    public static void main(
            String[] args)
    {
        final String messageString2 = PropertyFileReader.getInstance().getMessageString();
        System.out.println(new java.util.Date() + " - PropertyFileReader main messageString2.length() : '" + messageString2.length() + "'");
    }

}
