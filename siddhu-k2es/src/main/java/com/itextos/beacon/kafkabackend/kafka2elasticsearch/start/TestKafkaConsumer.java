package com.itextos.beacon.kafkabackend.kafka2elasticsearch.start;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.Properties;
import java.util.Set;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import com.itextos.beacon.commonlib.message.IMessage;
import com.itextos.beacon.commonlib.message.SubmissionObject;

public class TestKafkaConsumer
{

    public static void testConsumeSubMessage(
            String topic,
            String cGroupName)
            throws InterruptedException
    {
        final SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
        final SimpleDateFormat sdfTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        final Properties       config  = new Properties();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "129_kafka:9092,130_kafka:9092,131_kafka:9092");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "com.itextos.beacon.commonlib.message.serialize.KeyDeserializer");
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "com.itextos.beacon.commonlib.message.serialize.ValueDeserializer");
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.GROUP_ID_CONFIG, cGroupName);
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "20");
        config.put(ConsumerConfig.CLIENT_ID_CONFIG, "Test-" + cGroupName);

        System.out.println("Connecting Kafka...");
        final KafkaConsumer<String, IMessage> consumer = new KafkaConsumer<>(config);
        System.out.println("Subscribe Topic: " + topic);
        System.out.println("Consumer Group: " + cGroupName);
        final Set<String> ltopicName = Collections.singleton(topic);
        System.out.println(ltopicName);
        consumer.subscribe(ltopicName);

        int rowCount = 0;

        while (true)
        {
            // System.out.println("Polling Attempt: " + i);
            final ConsumerRecords<String, IMessage> records = consumer.poll(Duration.of(100, ChronoUnit.MILLIS));
            System.out.println("Poll Count: " + records.count());

            if (records.count() == 0)
            {
                Thread.sleep(100);
                continue;
            }
            rowCount += records.count();

            for (final ConsumerRecord<String, IMessage> record : records)
            {
                final String           key       = record.key();
                final IMessage         iMsg      = record.value();
                final SubmissionObject subObject = (SubmissionObject) iMsg;
                final String           msgId     = subObject.getMessageId();
                final Date             recvDate  = subObject.getMessageReceivedDate();
                System.out.println(sdfDate.format(recvDate) + "," + msgId);
            }
            if (rowCount >= 100)
                break;
        }
        consumer.commitAsync();
        consumer.close();
    }

}
