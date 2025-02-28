package com.itextos.beacon.kafkabackend.kafka2elasticsearch.kafkaconsumer;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.simple.JSONObject;

import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.message.IMessage;
import com.itextos.beacon.commonlib.message.SubmissionObject;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.commonlib.utility.HexUtil;
import com.itextos.beacon.kafkabackend.kafka2elasticsearch.start.StartApplication;

public class Kafka2ESJSONConverter
{

    public static int getRecvTimeHour(
            Date rcvTime)
    {
        final SimpleDateFormat sdf = new SimpleDateFormat("HH");
        sdf.setLenient(false);

        return Integer.parseInt(sdf.format(rcvTime));
    }

    @SuppressWarnings("unchecked")
    public static JSONObject buildSubJSON(
            IMessage iMsg)
            throws Exception
    {
        boolean                isEmptyJSON = true;
        final SubmissionObject subObject   = (SubmissionObject) iMsg;
        final BaseMessage      baseMessage = subObject;

        final JSONObject       subJSON     = new JSONObject();

        final String           msgId       = subObject.getMessageId();
        subJSON.put(StartApplication.ESIndexUniqueColumn, msgId);

        final String dataUpdTime = DateTimeUtility.getFormattedCurrentDateTime(DateTimeFormat.DEFAULT);
        subJSON.put(StartApplication.ESDocUpdTmColumn, dataUpdTime);

        for (final ESIndexColMapValue esColMap : StartApplication.ListESColMap)
        {
            final String  colName     = esColMap.ColumnName;
            final String  colType     = esColMap.ColumnType;
            final String  mapColName  = esColMap.MappedName;
            final boolean ciRequired  = esColMap.CIColumnRequired;
            // final String defaultVal = esColMap.DefaultValue;

            String        colValue    = Kafka2ESConstants.colInitNullValue;
            int           colIntValue = -1;

            switch (colName)
            {
                case "campaign_id":
                    colValue = subObject.getCampaignId();
                    break;

                case "campaign_name":
                    colValue = subObject.getCampaignName();
                    break;

                case "carrier":
                    colValue = subObject.getCarrier();
                    break;

                case "circle":
                    colValue = subObject.getCircle();
                    break;

                case "cli_id":
                    colValue = subObject.getClientId();
                    break;

                case "country":
                    colValue = subObject.getCountry();
                    break;

                case "dest":
                    colValue = subObject.getMobileNumber();
                    break;

                case "file_id":
                    colValue = subObject.getFileId();
                    break;

                case "hdr":
                    colValue = subObject.getHeader();
                    break;

                case "intf_grp_type":
                    colValue = subObject.getInterfaceGroupType().getKey();
                    break;

                case "intf_type":
                    colValue = subObject.getInterfaceType().getKey();
                    break;

                case "msg":
                    if (subObject.isHexMessage())
                        colValue = new String(HexUtil.toByteArray(subObject.getMessage()));
                    else
                        colValue = subObject.getMessage();

                    if (colValue.length() > 1000)
                        colValue = colValue.substring(0, 1000);
                    break;

                case "msg_part_no":
                    colIntValue = subObject.getMessagePartNumber();
                    colValue = "" + colIntValue;
                    break;

                case "total_msg_parts":
                    colIntValue = subObject.getMessageTotalParts();
                    colValue = "" + colIntValue;
                    break;

                case "msg_tag":
                    colValue = subObject.getMessageTag();
                    break;

                case "msg_tag1":
                    colValue = subObject.getMsgTag1();
                    break;

                case "msg_tag2":
                    colValue = subObject.getMsgTag2();
                    break;

                case "msg_tag3":
                    colValue = subObject.getMsgTag3();
                    break;

                case "msg_tag4":
                    colValue = subObject.getMsgTag4();
                    break;

                case "msg_tag5":
                    colValue = subObject.getMsgTag5();
                    break;

                case "cluster_type":
                    colValue = subObject.getClusterType().getKey();
                    break;

                case "recv_date":
                    colValue = DateTimeUtility.getFormattedDateTime(subObject.getMessageReceivedDate(),
                            DateTimeFormat.DEFAULT_DATE_ONLY);
                    break;

                case "recv_time":
                    final Date rcvTime = subObject.getMessageReceivedTime();
                    colValue = DateTimeUtility.getFormattedDateTime(rcvTime, DateTimeFormat.DEFAULT);
                    final int rcvHour = getRecvTimeHour(rcvTime);
                    subJSON.put("recv_hour", rcvHour);
                    break;

                case "retry_attempt":
                    colValue = "" + subObject.getRetryAttempt();
                    break;

                case "route_id":
                    colValue = subObject.getRouteId();
                    break;

                case "sub_cli_sts_code":
                    colValue = baseMessage.getValue(MiddlewareConstant.MW_SUB_CLI_STATUS_CODE);
                    break;

                case "sub_cli_sts_desc":
                    colValue = baseMessage.getValue(MiddlewareConstant.MW_SUB_CLI_STATUS_DESC);
                    break;

                case "sub_ori_sts_code":
                    colValue = baseMessage.getValue(MiddlewareConstant.MW_SUB_ORI_STATUS_CODE);
                    break;

                case "sub_ori_sts_desc":
                    colValue = baseMessage.getValue(MiddlewareConstant.MW_SUB_ORI_STATUS_DESC);
                    break;

                case "sub_status":
                    colValue = subObject.getSubStatus();
                    break;
            }

            if (Kafka2ESConstants.colInitNullValue.equals(colValue))
                continue;

            isEmptyJSON = false;

            if (colType.equals(Kafka2ESConstants.colTypeNumeric))
                subJSON.put(mapColName, colIntValue);
            else
            {
                colValue = CommonUtility.nullCheck(colValue, true);
                subJSON.put(mapColName, colValue);

                if (ciRequired)
                {
                    final String ciColName  = mapColName + "_ci";
                    final String ciColValue = colValue.toLowerCase();
                    subJSON.put(ciColName, ciColValue);
                }
            }
        }

        if (isEmptyJSON)
            return null;

        return subJSON;
    }

    public static JSONObject buildDelJSON(
            IMessage iMsg)
            throws Exception
    {
        boolean              isEmptyJSON = true;
        final DeliveryObject delObject   = (DeliveryObject) iMsg;
        final BaseMessage    baseMessage = delObject;

        final JSONObject     delJSON     = new JSONObject();

        final String         msgId       = delObject.getMessageId();
        delJSON.put(StartApplication.ESIndexUniqueColumn, msgId);

        final String dataUpdTime = DateTimeUtility.getFormattedCurrentDateTime(DateTimeFormat.DEFAULT);
        delJSON.put(StartApplication.ESDocUpdTmColumn, dataUpdTime);

        for (final ESIndexColMapValue esColMap : StartApplication.ListESColMap)
        {
            final String  colName     = esColMap.ColumnName;
            final String  colType     = esColMap.ColumnType;
            final String  mapColName  = esColMap.MappedName;
            // final String defaultVal = esColMap.DefaultValue;
            final boolean ciRequired  = esColMap.CIColumnRequired;

            String        colValue    = Kafka2ESConstants.colInitNullValue;
            final int     colIntValue = -1;

            switch (colName)
            {
                case "carrier_ack_id":
                    colValue = delObject.getCarrierAcknowledgeId();
                    break;

                case "delivery_status":
                    colValue = delObject.getDeliveryStatus();
                    break;

                case "dly_time":
                    final Date dlyTime = delObject.getDeliveryTime();
                    if (dlyTime == null)
                        colValue = "1900-01-01 00:00:00";
                    else
                        colValue = DateTimeUtility.getFormattedDateTime(delObject.getDeliveryTime(),
                                DateTimeFormat.DEFAULT);
                    break;

                case "dn_cli_sts_code":
                    colValue = baseMessage.getValue(MiddlewareConstant.MW_DN_CLI_STATUS_CODE);
                    break;

                case "dn_cli_sts_desc":
                    colValue = baseMessage.getValue(MiddlewareConstant.MW_DN_CLI_STATUS_DESC);
                    break;

                case "dn_ori_sts_code":
                    colValue = baseMessage.getValue(MiddlewareConstant.MW_DN_ORI_STATUS_CODE);
                    break;

                case "dn_ori_sts_desc":
                    colValue = baseMessage.getValue(MiddlewareConstant.MW_DN_ORI_STATUS_DESC);
                    break;
            }

            if (Kafka2ESConstants.colInitNullValue.equals(colValue))
                continue;

            isEmptyJSON = false;

            if (colType.equals(Kafka2ESConstants.colTypeNumeric))
                delJSON.put(mapColName, colIntValue);
            else
            {
                colValue = CommonUtility.nullCheck(colValue, true);
                delJSON.put(mapColName, colValue);

                if (ciRequired)
                {
                    final String ciColName  = mapColName + "_ci";
                    final String ciColValue = colValue.toLowerCase();
                    delJSON.put(ciColName, ciColValue);
                }
            }
        }

        if (isEmptyJSON)
            return null;

        return delJSON;
    }

}
