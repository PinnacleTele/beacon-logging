package com.itextos.beacon.commonlib.kafkaservice.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;

public class TestKafkaProducerConsumer
{

    public static void main(
            String[] args)
            throws FileNotFoundException,
            IOException,
            InterruptedException
    {
        final String     propsPath  = System.getProperty("kafka.service.properties");
        final Properties properties = new Properties();
        properties.load(new FileInputStream(new File(propsPath)));

        final String opType    = System.getProperty("operation.type");
        final String topicName = System.getProperty("topic.name");
        Thread       t         = null;

        final long   startTime = System.currentTimeMillis();

        switch (opType)
        {
            case "P":
            case "p":
                t = produceMessage(topicName, properties);
                break;

            case "C":
            case "c":
                consumerProperties(topicName, properties);
                break;
        }

        if (t != null)
        {
            t.join();
            final long endTime = System.currentTimeMillis();
            System.out.println("Overall time taken " + (endTime - startTime));
        }
    }

    private static void consumerProperties(
            String aTopicName,
            Properties aProperties)
    {
        final int consumerCount = Integer.parseInt(System.getProperty("consumer.count"));

        for (int index = 0; index < consumerCount; index++)
        {
            final TestKafkaConsumer consumer = new TestKafkaConsumer(aTopicName, aProperties);
            startConsumMessages(consumer);
        }
    }

    private static void startConsumMessages(
            TestKafkaConsumer aConsumer)
    {
        final Thread t = new Thread(aConsumer);
        
        ExecutorSheduler.getInstance().addTask(t,"TestConsumer : ");
    }

    private static Thread produceMessage(
            String aTopicName,
            Properties aProperties)
            throws InterruptedException
    {
        final int producerCount = Integer.parseInt(System.getProperty("producer.count"));
        Thread    t             = null;

        for (int index = 0; index < producerCount; index++)
        {
            final TestKafkaProducer producer = new TestKafkaProducer(aTopicName, aProperties);
            t = startMessaging(index, producer);
        }
        return t;
    }

    private static Thread startMessaging(
            int aIndex,
            TestKafkaProducer aProducer)
            throws InterruptedException
    {
        final Thread t = new Thread(() -> {
            final long startTime = System.currentTimeMillis();
            final int  count     = Integer.parseInt(System.getProperty("messages.per.thread", "1000"));
            for (int index = 0; index < count; index++)
                try
                {
                    aProducer.sendAsync(aIndex + "-" + index, getLongString());

                    if ((index % 10000) == 0)
                        System.out.println("Pushed " + index);
                }
                catch (final ItextosException e)
                {
                    e.printStackTrace();
                }
            final long endTime = System.currentTimeMillis();
            System.out.println("Thread " + aIndex + " Time taken " + (endTime - startTime) + " counts " + count);
        });
        t.start();
        // t.join();
        // aProducer.closeProducer();
        return t;
    }

    private static String getLongString()
    {
        return "{\"car_ts_format\":\"yyMMddHHmm\",\"dlt_tmpl_id\":\"-1\",\"app_type\":\"sms\",\"recv_dt\":\"2021-07-26\",\"if_reject\":\"0\",\"int_msg\":\"0\",\"intf_type\":\"http_japi\",\"dest\":\"919884227203\",\"is_sync\":\"1\",\"ch_http_sts_code\":\"-999\",\"dn_ori_sts_code\":\"400\",\"ch_res\":\"No Client Configuration found for 6000000200000001\",\"delv_lat_sla_in_millis\":\"0.00000\",\"term_car\":\"Others\",\"m_tag1\":null,\"m_tag3\":null,\"car_full_dn\":\"ID:1 SUB:001 DLVRD:001 SUBMITDATE:2107261152 DONEDATE:2107261152 STAT:DELIVRD ERR:000 TEXT:NULL\",\"m_tag2\":null,\"delv_lat_ori_in_millis\":\"631.00000\",\"m_tag5\":null,\"a_recv_ts\":\"2021-07-26 11:52:32.665\",\"m_tag4\":null,\"m_id\":\"2602107261152323281800\",\"cli_hdr\":\"BRAINT\",\"ft_cd\":\"PMS\",\"cli_m_id\":\"\",\"dn_req_cli\":\"1\",\"dn_cli_sts_desc\":\"Success\",\"db_ins_suffix\":\"\",\"a_recv_dt\":\"2021-07-26\",\"dlt_enty_id\":\"110100001352\",\"platform_cluster\":\"bulk\",\"d_hdr\":\"BRAINT\",\"intf_grp_type\":\"api\",\"udh\":null,\"is_hex_m\":\"0\",\"msg_type\":\"1\",\"f_id\":\"2602107261152323281700\",\"a_dly_ts\":\"2021-07-26 11:52:33.296\",\"dlr_from_intl\":\"dummyroute_dlr_came_from_MW\",\"car_sys_id\":\"smpp\",\"smsc_id\":\"smpp\",\"sms_rety_avail\":\"0\",\"b_m_id\":\"2602107261152323281800\",\"dly_ts\":\"2021-07-26 11:52:43.296\",\"car_rcvd_ts\":\"2021-07-26 11:52:33.296\",\"mask_hdr\":null,\"cir\":\"Others\",\"bill_ty\":\"0\",\"car_sub_ts\":\"2021-07-26 11:52:33.296\",\"db_ins_jndi\":\"\",\"sms_priority\":\"1\",\"a_car_sub_ts\":\"2021-07-26 11:52:33.296\",\"dcs\":\"-1\",\"car\":\"Others\",\"cntry\":\"India\",\"c_id\":\"6000000200000001\",\"camp_id\":null,\"dn_cli_sts_code\":\"400\",\"treat_dom_as_spl_srs\":\"0\",\"tz_off\":null,\"tot_m_prts\":\"1\",\"msg_create_ts\":\"1627280553317\",\"atmpt_cnt\":null,\"m_tag\":\"\",\"udhi\":\"0\",\"file_name\":null,\"rty_ori_rute_id\":null,\"dn_ori_sts_desc\":\"DELIVRD\",\"sub_ori_sts_code\":\"400\",\"car_ack_id\":\"1\",\"m_prt_no\":\"1\",\"term_cir\":\"Others\",\"rty_alt_rute_id\":null,\"max_valid_in_sec\":\"86400\",\"car_ori_sts_code\":\"000\",\"m\":\"Good news, your loan is almost paid off! Repay 9922 today to access larger loan amounts in the future.\",\"dn_fail_type\":\"2\",\"aplha_value\":null,\"m_class\":\"PM\",\"long_m\":\"Good news, your loan is almost paid off! Repay 9922 today to access larger loan amounts in the future.\",\"car_sts_code\":\"000\",\"recv_ts\":\"2021-07-26 11:52:32.665\",\"rute_id\":\"LSR\"}";
    }

}
