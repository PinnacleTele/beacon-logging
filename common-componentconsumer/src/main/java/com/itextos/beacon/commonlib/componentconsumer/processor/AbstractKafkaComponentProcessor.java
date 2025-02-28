package com.itextos.beacon.commonlib.componentconsumer.processor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.ErrorMessage;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.kafkaservice.consumer.ConsumerInMemCollection;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.IMessage;
import com.itextos.beacon.commonlib.messageprocessor.process.MessageProcessor;
import com.itextos.beacon.commonlib.prometheusmetricsutil.PrometheusMetrics;
import com.itextos.beacon.errorlog.ErrorLog;
import com.itextos.beacon.smslog.LossLog;
import com.itextos.beacon.smslog.StartupFlowLog;

import io.prometheus.client.Histogram.Timer;

public abstract class AbstractKafkaComponentProcessor
        extends
        AbstractCommonComponentProcessor
{

    private static final Log log = LogFactory.getLog(AbstractKafkaComponentProcessor.class);

    protected AbstractKafkaComponentProcessor(
            String aThreadName,
            Component aComponent,
            ClusterType aPlatformCluster,
            String aTopicName,
            ConsumerInMemCollection aConsumerInMemCollection,
            int aSleepInMillis)
    {
        super(aThreadName, aComponent, aPlatformCluster, aTopicName, aConsumerInMemCollection, aSleepInMillis);
   
    	StartupFlowLog.log("AbstractKafkaComponentProcessor contsructer Entered");

    }

    @Override
    public void processMessage(
            IMessage aMessage)
    {
        if (log.isDebugEnabled())
            log.debug("Processing message " + aMessage);
        Timer timer = null;

        try
        {
            timer = addPrometheusCounters(aMessage);

            doProcess((BaseMessage) aMessage);
        }
        catch (final Exception e)
        {
        	
            
            ErrorLog.log("Exception while processing the message \n "+aMessage+" \n" +ErrorMessage.getStackTraceAsString(e));

            LossLog.log(aMessage.getJsonString());
            
       //     sendBackToTopic(aMessage);
        }
        finally
        {
            endPrometheusTimer(timer);
        }
    }

    private void endPrometheusTimer(
            Timer aTimer)
    {

        try
        {
            PrometheusMetrics.platformEndTimer(mComponent, aTimer);
        }
        catch (final Exception e)
        {
            // Add this exception in INFO mode.
            if (log.isInfoEnabled())
                log.info("IGNORE: Exception while working on prometheus counter", e);
        }
    }

    private Timer addPrometheusCounters(
            IMessage aMessage)
    {
        Timer timer = null;

        try
        {
            final BaseMessage baseMessage = (BaseMessage) aMessage;
            PrometheusMetrics.platformIncrement(mComponent, baseMessage.getClusterType(), mTopicName);
            timer = PrometheusMetrics.platformStartTimer(mComponent, baseMessage.getClusterType(), mTopicName);
        }
        catch (final Exception e)
        {
            // Add this exception in INFO mode.
            if (log.isInfoEnabled())
                log.info("IGNORE: Exception while working on prometheus counter", e);
        }
        return timer;
    }

    /*
    @Override
    protected void sendBackToTopic(
            IMessage aMessage)
    {

        try
        {
            updateBeforeSendBack(aMessage);
            MessageProcessor.writeMessage(mComponent, mComponent, aMessage);
        }
        catch (final ItextosException e)
        {
            log.error("Exception while sending the message to the same topic", e);
        }
    }
	*/
    public abstract void doProcess(
            BaseMessage aMessage)
            throws Exception;

}