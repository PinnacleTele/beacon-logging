package com.itextos.beacon.monitortopics;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

public class KafkaTopicQueueSize {
    public static void main(String[] args) {
        // Set Kafka consumer properties
    	
    	Map<String,Integer> topicsmap=new TreeMap<String,Integer>();
        Properties consumerProps = new Properties();
    //    consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.1.127:9092");
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "115_Kafka_1:9092,116_Kafka_2:9092,117_Kafka_3:9092");
        
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        try (AdminClient adminClient = AdminClient.create(consumerProps)) {
            // Get the list of topics
            ListTopicsResult topicsResult = adminClient.listTopics();
            Set<String> topicNames = topicsResult.names().get();

            // Create Kafka consumer
            try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps)) {
                // Subscribe to topics to let Kafka automatically assign partitions
                consumer.subscribe(topicNames);

                // Poll to ensure consumer assignment is complete
                consumer.poll(0);

                // Get the assignment (partitions assigned to the consumer)
                Set<TopicPartition> assignedPartitions = consumer.assignment();

                // Iterate over each assigned partition and get its queue size
                for (TopicPartition partition : assignedPartitions) {
                    long position = consumer.position(partition);
                    long endOffset = consumer.endOffsets(Collections.singletonList(partition)).get(partition);
                    int queueSize = (int) (endOffset - position);
                    System.out.println("Topic: " + partition.topic() + ", Partition: " + partition.partition() + ", Queue size: " + queueSize);
                
                  Integer exsistingcount=  topicsmap.get(partition.topic());
                  
                  if(exsistingcount==null) {
                	  
                	  exsistingcount=new Integer(0);
                	  
                  }
                  
                  exsistingcount=exsistingcount.intValue()+queueSize;
                  
                  topicsmap.put(partition.topic(),exsistingcount);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        
        topicsmap.forEach((t,c)->{
        	
            System.out.println(t+" : "+ c);

        });
    }
}
