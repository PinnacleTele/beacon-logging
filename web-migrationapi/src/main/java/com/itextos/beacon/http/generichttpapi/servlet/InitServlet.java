package com.itextos.beacon.http.generichttpapi.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.apperrorhandler.servlets.BasicServlet;
import com.itextos.beacon.commonlib.componentconsumer.processor.ProcessorInfo;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.InterfaceType;
import com.itextos.beacon.commonlib.messageidentifier.MessageIdentifier;
import com.itextos.beacon.commonlib.prometheusmetricsutil.PrometheusMetrics;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.http.generichttpapi.common.utils.APIConstants;
import com.itextos.beacon.http.interfacefallback.inmem.FallbackQReaper;
import com.itextos.beacon.interfaces.generichttpapi.processor.pollers.FilePoller;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class InitServlet
        extends
        BasicServlet
{

    private static final long serialVersionUID = -922457646505165311L;

    private static final Log  log              = LogFactory.getLog(InitServlet.class);

    public InitServlet()
    {
        super();
    }

    @Override
    public void init(
            ServletConfig config)
            throws ServletException
    {
        if (log.isDebugEnabled())
            log.debug(" init() -  start ");

        try
        {
            final MessageIdentifier lMsgIdentifier = MessageIdentifier.getInstance();
            lMsgIdentifier.init(InterfaceType.MIGRATION_API);

            final String lAppInstanceId = lMsgIdentifier.getAppInstanceId();

            if (log.isDebugEnabled())
                log.debug("Appinstance ID : " + lAppInstanceId);

            PrometheusMetrics.registerServer();
            PrometheusMetrics.registerApiMetrics();

            APIConstants.setAppInstanceId(lAppInstanceId);

            if (APIConstants.CLUSTER_INSTANCE == null)
            {
                log.error("InitServlet.inti() - Instance Cluster value is not Configured..., Hence Stoping the Instance.");
                System.exit(-1);
            }

            FallbackQReaper.getInstance();

//            startConsumers();
        }
        catch (final Exception e)
        {
            log.error("InitServlet.inti() - Error while gettting appInstance ID from property file due to ....", e);
        }
    }

    private static void startConsumers()
    {

        if (CommonUtility.isEnabled(APIConstants.START_CONSUMER))
        {
            if (log.isDebugEnabled())
                log.debug("**************Kafka consumer started*********************");

            try
            {
                final ProcessorInfo lProcessor = new ProcessorInfo(Component.INTERFACE_ASYNC_PROCESS, false);
                lProcessor.process();
            }
            catch (final Exception e)
            {
                log.error("Unable to start the Kafka Async Consumer.., Hence Stoping the Instance..", e);
                System.exit(-1);
            }

            final FilePoller lFilePoller = new FilePoller();
        }
        else
            if (log.isDebugEnabled())
                log.debug("**************Kafka consumer not started*********************");
    }

    @Override
    public void destroy()
    {
        MessageIdentifier.getInstance().resetMessageIdentifier();
    }

    @Override
    public void doGet(
            HttpServletRequest aRequest,
            HttpServletResponse aResponse)
            throws IOException
    {

        try (
                PrintWriter writer = aResponse.getWriter();)
        {
            writer.write("uphttpapiweb InitServlet. Time : " + new Date());
            writer.flush();
            aResponse.setStatus(HttpURLConnection.HTTP_OK);
        }
        catch (final Exception e)
        {
            aResponse.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }

    @Override
    public void doPost(
            HttpServletRequest aRequest,
            HttpServletResponse aResponse)
            throws IOException
    {
        doGet(aRequest, aResponse);
    }

}