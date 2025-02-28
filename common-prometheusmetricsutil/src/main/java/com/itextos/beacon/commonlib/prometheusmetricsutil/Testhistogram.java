package com.itextos.beacon.commonlib.prometheusmetricsutil;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import io.prometheus.client.Histogram;
import io.prometheus.client.Histogram.Timer;
import io.prometheus.client.exporter.MetricsServlet;

public class Testhistogram
{

    public static void main(
            String[] args)
            throws Exception
    {
        final Server                server  = new Server(2525);
        final ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        context.addServlet(new ServletHolder(new MetricsServlet()), "/metrics");
        server.setHandler(context);
        server.start();

        final Histogram           lRegister = Histogram.build().name("Samplehistogram").help("HELP me to solve this ").register();

        final Map<Integer, Timer> allTimes  = new HashMap<>();

        for (int index = 0; index < 100; index++)
        {
            // System.out.println(System.currentTimeMillis() + " Press a number add timer");
            // final int lRead = new BufferedInputStream(System.in).read();
            final Timer lStartTimer = lRegister.startTimer();
            allTimes.put(index, lStartTimer);
        }
        System.in.read();

        for (int index = 0; index < 100; index++)
        {
            Thread.sleep(new Random().nextInt(100));
            // System.out.println(System.currentTimeMillis() + " Press a number to remove
            // timer ");
            // final int lRead = new BufferedInputStream(System.in).read();
            final Timer lTimer = allTimes.get(index);

            if (lTimer != null)
                lTimer.close();
            // lTimer = null;
            else
                System.err.println("Invalid options.");
        }
        System.out.println("Closed all");
        System.in.read();

        // server.stop();
    }

}
