package com.itextos.beacon.httpclienthandover.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.itextos.beacon.commonlib.componentconsumer.processor.ProcessorInfo;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.messageprocessor.process.MessageProcessor;
import com.itextos.beacon.httpclienthandover.common.Inmemorydata;

public class TestRunner
{

    static final List<BaseMessage> messageList = new ArrayList<>();

    // static
    // {
    //
    // for (int i = 0; i < 1; i++)
    // {
    // BaseMessage sampleMessage = null;
    //
    // try
    // {
    // sampleMessage = new DeliveryObject(
    // "{\"msg_create_ts\":\"1623935951637\",\"pl_rds_id\":\"1\",\"int_msg\":\"0\",\"intf_type\":\"http_japi\",\"pl_exp\":\"21061801\",\"rty_atmpt\":\"0\",\"long_m\":\"Test\",\"sms_priority\":\"2\",\"recv_ts\":\"2021-06-17
    // 18:49:11.446\",\"platform_cluster\":\"bulk\",\"intf_grp_type\":\"api\",\"msg_type\":\"1\",\"rute_id\":\"LSR\",\"c_id\":\"6000000200000000\",\"m_id\":\"2002106171849110001200\"}");
    // }
    // catch (final Exception e)
    // {
    // e.printStackTrace();
    // }
    //
    // sampleMessage.putValue(MiddlewareConstant.MW_MESSAGE_ID, "" + (i + 1));
    //
    // sampleMessage.putValue(MiddlewareConstant.MW_ENCRYPTED_MOBILENUMBER,
    // "XSXSXSXSSXSXSXSXS");
    // sampleMessage.putValue(MiddlewareConstant.MW_MSG, "msg");
    // sampleMessage.putValue(MiddlewareConstant.MW_CLIENT_ID, "6000000200000000");
    // messageList.add(sampleMessage);
    // }
    // }

    public static void main(
            String[] args)
            throws Exception
    {
        // BasicConfigurator.configure();
        // final ClientDlrConfigCollection lClientDlrConfigCollection =
        // (ClientDlrConfigCollection)
        // InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.CLIENT_DLR_PREF);

        // final ProcessorInfo lChRetryData = new ProcessorInfo(Component.HTTP_DLR);
        // lChRetryData.process();
        final String s = "{\"car_ts_format\":null,\"intl_msg\":\"0\",\"dlt_tmpl_id\":\"54321\",\"app_type\":\"sms\",\"recv_dt\":\"2021-06-22\",\"intl_msg\":\"0\",\"intf_type\":\"http_japi\",\"dest\":\"919003054773\",\"dn_ori_sts_code\":\"538\",\"delv_lat_sla_in_millis\":\"0.00000\",\"term_car\":null,\"delv_lat_ori_in_millis\":\"0.00000\",\"a_recv_ts\":\"2021-06-22 11:21:41.645\",\"m_id\":\"2002106221121410012600\",\"cli_hdr\":\"abc123\",\"ft_cd\":null,\"cli_m_id\":\"this is cust ref\",\"dn_req_cli\":\"0\",\"dn_cli_sts_desc\":\"Header template check failed\",\"a_recv_dt\":\"2021-06-22\",\"dlt_enty_id\":\"12345\",\"platform_cluster\":\"bulk\",\"d_hdr\":\"win123\",\"intf_grp_type\":\"api\",\"udh\":null,\"is_hex_m\":\"0\",\"msg_type\":\"1\",\"f_id\":\"2002106221121410012500\",\"smsc_id\":null,\"sms_rety_avail\":\"0\",\"b_m_id\":\"2002106221121410012600\",\"mask_hdr\":null,\"cir\":\"dummy\",\"bill_ty\":\"0\",\"car_sub_ts\":null,\"sms_priority\":\"2\",\"a_car_sub_ts\":null,\"dcs\":\"0\",\"car\":\"dummy\",\"cntry\":null,\"c_id\":\"6000000200000000\",\"dn_cli_sts_code\":\"538\",\"treat_dom_as_spl_srs\":\"0\",\"tz_off\":null,\"tot_m_prts\":\"0\",\"msg_create_ts\":\"1624341101704\",\"atmpt_cnt\":null,\"m_tag\":\"this is msg tag\",\"udhi\":\"0\",\"file_name\":null,\"rty_ori_rute_id\":null,\"pf_reject\":\"1\",\"dn_ori_sts_desc\":\"Header template check failed\",\"sub_ori_sts_code\":\"538\",\"m_prt_no\":\"0\",\"term_cir\":null,\"rty_alt_rute_id\":null,\"max_valid_in_sec\":\"8640\",\"m\":\"Our core Java programming\",\"aplha_value\":null,\"m_class\":\"PM\",\"long_m\":\"Our core Java programming\",\"recv_ts\":\"2021-06-22 11:21:41.645\",\"dly_sts\":\"FAILED\",\"rute_id\":\"XX\"}";

        for (int i = 1; i < 2; i++)
        {
            final DeliveryObject deliveryObject = new DeliveryObject(s);

            deliveryObject.putValue(MiddlewareConstant.MW_MESSAGE_ID, i + "");
            Inmemorydata.getInstance().add(deliveryObject);
        }
        // try
        // {
        // sendDnReceiver();
        // }
        // catch (final ParseException e)
        // {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }

        // Map<Boolean, List<BaseMessage>> map = new HashMap<>();
        // map.put(true, messageList);
        // final ProcessStarter starter = new ProcessStarter(messageList, false, 1,
        // "6000000200000000");
        // new RetryProcessPoller(true, "6000000200000000");
        // new HandoverRetryReaper();
        // starter.process();
        // map = new HashMap<>();
        // map.put(false, messageList);
    }

    private static void sendDnReceiver()
            throws ParseException
    {
        final String              json       = "{\"platform_cluster\":\"bulk\", \"car_ts_format\":\"yyMMddHHmm\", \"app_type\":\"SMS\", \"recv_dt\":\"2021-06-25\", \"dest\":\"919080640503\", \"dn_ori_sts_code\":\"400\", \"intf_type\":\"http_japi\", \"delv_lat_sla_in_millis\":\"0.00000\", \"term_car\":\"Others\", \"car_full_dn\":\"id:e9515d4c-c5f5-49e4-91db-612d83206339 sub:1 dlvrd:1 submit date:2106251606 done date:2106251606 stat:DELIVRD err:000 Text: test message\", \"delv_lat_ori_in_millis\":\"0.15600\", \"car_sts_desc\":\"DELIVRD\", \"a_recv_ts\":\"2021-06-25 16:06:43.799\", \"m_id\":\"2002106251606430023000\", \"ft_cd\":\"PMS\", \"pl_rds_id\":\"1\", \"cli_m_id\":\"SSDD223\", \"msg_type\":\"2\", \"dn_req_cli\":\"1\", \"dn_cli_sts_desc\":\"Success\", \"rty_atmpt\":\"0\", \"a_recv_dt\":\"2021-06-25\", \"sms_priority\":\"1\", \"int_msg\":\"0\", \"car_ori_sts_desc\":\"DELIVRD\", \"d_hdr\":\"alerts\", \"is_hex_m\":\"0\", \"f_id\":\"2002106251606430022900\", \"a_dly_ts\":\"2021-06-25 16:06:43.955\", \"car_sys_id\":\"smppclient11\", \"smsc_id\":\"test\", \"b_m_id\":\"2002106251606430023000\", \"dly_ts\":\"2021-06-25 16:06:43.955\", \"car_rcvd_ts\":\"2021-06-25 16:06:00.000\", \"cir\":\"Others\", \"bill_ty\":\"0\", \"car_sub_ts\":\"2021-06-25 16:06:43.858\", \"db_ins_jndi\":\"\", \"a_car_sub_ts\":\"2021-06-25 16:06:43.858\", \"dcs\":\"0\", \"car\":\"Others\", \"cntry\":\"India\", \"c_id\":\"6000000200000001\", \"cli_encp\":\"0\", \"dn_cli_sts_code\":\"400\", \"treat_dom_as_spl_srs\":\"0\", \"tot_m_prts\":\"1\", \"atmpt_cnt\":\"1\", \"m_tag\":\"SSJJ1505\", \"udhi\":\"0\", \"pl_exp\":\"21062523\", \"dn_ori_sts_desc\":\"Success\", \"intf_grp_type\":\"api\", \"car_ack_id\":\"E9515D4C-C5F5-49E4-91DB-612D83206339\", \"m_prt_no\":\"1\", \"term_cir\":\"Others\", \"car_ori_sts_code\":\"000\", \"max_valid_in_sec\":\"5400\", \"m\":\"Welcome\", \"dn_fail_type\":\"2\", \"dn_pl_sts\":\"1\", \"m_class\":\"PM\", \"long_m\":\"Welcome\", \"car_sts_code\":\"000\", \"recv_ts\":\"2021-06-25 16:06:43.799\", \"dly_sts\":\"DELIVRD\", \"acc_hdr\":\"win231\", \"hdr\":\"alerts\", \"rute_id\":\"LSR\", \"msg_create_ts\":\"1624617403857\"}";
        final JSONParser          parser     = new JSONParser();
        final JSONObject          jsonObject = (JSONObject) parser.parse(json);
        final Map<String, Object> map        = new HashMap<>();
        map.putAll(jsonObject);
        System.out.println(map);

        try
        {
            final DeliveryObject deliveryObject = new DeliveryObject(json);

            MessageProcessor.writeMessage(Component.DNP, Component.HTTP_DLR, deliveryObject);
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            final ProcessorInfo lDnProcessor = new ProcessorInfo(Component.HTTP_DLR);
            lDnProcessor.process();
        }
        catch (final Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
