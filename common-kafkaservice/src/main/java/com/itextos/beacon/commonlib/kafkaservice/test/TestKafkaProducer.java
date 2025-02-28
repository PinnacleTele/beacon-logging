package com.itextos.beacon.commonlib.kafkaservice.test;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;

import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.kafkaservice.common.KafkaUtility;

public class TestKafkaProducer
{

    private static final Log              log = LogFactory.getLog(TestKafkaProducer.class);

    private final String                  mTopicName;
    private final Properties              mKafkaProperties;
    private boolean                       mInitialized;
    private KafkaProducer<String, String> mProducer;
    private final TestProducerCallback        callBack;

    public TestKafkaProducer(
            String aTopicName,
            Properties aKafkaProperties)
    {
        mTopicName       = aTopicName;
        mKafkaProperties = aKafkaProperties;

        createProducer();
        callBack = new TestProducerCallback(mTopicName);
    }

    private void createProducer()
    {
        int counter = 0;

        while (!mInitialized)
        {
            counter++;

            try
            {
                if (log.isDebugEnabled())
                    log.debug(mTopicName);

                KafkaUtility.printProperties(mTopicName + " Producer", mKafkaProperties);

                mProducer = new KafkaProducer<>(mKafkaProperties);

                if (log.isDebugEnabled())
                {
                    log.debug(mTopicName + "Kafka Non-Trans Producer initialized successfully ");
                    log.debug(mTopicName + "Kafka Non-Trans Producer Configuration : " + mProducer.metrics());
                }
                mInitialized = true;
            }
            catch (final Exception exp)
            {
                log.error(mTopicName + "Attempt " + counter + ": Problem in initializing Kafka Non-Trans Producer. Will retry after 100 millis...", exp);

                try
                {
                    Thread.sleep(100L);
                }
                catch (final Exception interruptedException)
                {
                    //
                }
            }

            if (counter > 10)
            {
                log.error("Problem in recreating the Kafka Server more than 10 times. Exiting the application.");
                System.exit(-1);
            }
        }
    }

    public boolean sendAsync(
            String aKey,
            String aMessage)
            throws ItextosException
    {
        final boolean sent = false;

        try
        {
            final ProducerRecord<String, String> producerRecord = getProducerRecord(aKey, aMessage);
            mProducer.send(producerRecord, callBack);
            // final Future<RecordMetadata> send = mProducer.send(producerRecord, callBack);
            // sent = TestProducerCallback.updateOffsetValue(mTopicName, send.get());

            if (log.isDebugEnabled())
                log.debug(mTopicName + " IMessage sent successfully in Non-Trans mode (Async)");
        }
        catch (final KafkaException exp)
        {
            log.error(mTopicName + " Unrecoverable exception thrown during sending message to kafka. Recreating producer...", exp);
        }
        catch (final Exception exp)
        {
            log.error("Problem sending message failing this time...", exp);
            throw new ItextosException("Problem sending message failing this time...", exp);
        }
        return sent;
    }

    private ProducerRecord<String, String> getProducerRecord(
            String aKey,
            String aMessage)
    {
        return new ProducerRecord<>(mTopicName, aMessage);
    }

    public void closeProducer()
    {
        mProducer.flush();
        mProducer.close();
    }

}
