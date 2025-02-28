package com.itextos.beacon.commonlib.daemonprocess;

class MyThread
        extends
        Thread
{

    @Override
    public void run()
    {
        System.out.println("shut down hook task completed..");
    }

}

public class TestShutdownhook
{

    public static void main(
            String[] args)
    {
        final Runtime r = Runtime.getRuntime();
        r.addShutdownHook(new MyThread());

        System.out.println("Now main sleeping... press ctrl+c to exit");

        try
        {
            Thread.sleep(3000);
        }
        catch (final Exception e)
        {}
    }

}