package com.itextos.beacon.platform.topic2table.es;

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
import com.itextos.beacon.commonlib.utility.MessageConvertionUtility;

public class Kafka2ESJSONUtilSubmission
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
    {
        boolean                isEmptyJSON = true;
        final SubmissionObject subObject   = (SubmissionObject) iMsg;
        final BaseMessage      baseMessage = subObject;

        final JSONObject       subJSON     = new JSONObject();

        final String           msgId       = subObject.getMessageId();
        subJSON.put(SubmissionK2ES.ESIndexUniqueColumn, msgId);

        final String dataUpdTime = DateTimeUtility.getFormattedCurrentDateTime(DateTimeFormat.DEFAULT);
        subJSON.put(SubmissionK2ES.ESDocUpdTmColumn, dataUpdTime);

        for (final ESIndexColMapValue esColMap : SubmissionK2ES.ListESColMap)
        {
            final String  colName       = esColMap.ColumnName;
            final String  colType       = esColMap.ColumnType;
            final String  mapColName    = esColMap.MappedName;
            final boolean ciRequired    = esColMap.CIColumnRequired;
            // final String defaultVal = esColMap.DefaultValue;

            String        colValue      = Kafka2ESConstants.colInitNullValue;
            int           colIntValue   = -1;
            long          colLongValue   = -1L;
            double        colFloatValue = 0;

            switch (colName)
            {
                case "base_msg_id":
                    colValue = subObject.getBaseMessageId();
                    break;

                case "billing_add_fixed_rate":
                    colFloatValue = subObject.getBillingAddFixedRate();
                    colValue = "" + colFloatValue;
                    break;

                case "billing_sms_rate":
                    colFloatValue = subObject.getBillingSmsRate();
                    colValue = "" + colFloatValue;
                    break;

                case "billing_currency":
                    colValue = subObject.getBillingCurrency();
                    break;

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

                case "cli_hdr":
                    colValue = subObject.getClientHeader();
                    break;

                case "intf_grp_type":
                    colValue = subObject.getInterfaceGroupType().getKey();
                    break;

                case "intf_type":
                    colValue = subObject.getInterfaceType().getKey();
                    break;

                case "msg":
                    colValue = subObject.getMessage();
                    if (subObject.isHexMessage())
                        colValue = MessageConvertionUtility.convertHex2String(colValue);
                    // new String(HexUtil.toByteArray(colValue), StandardCharsets.UTF_16);

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
                
                case "sub_lat_sla_in_millis":
                    colValue = baseMessage.getValue(MiddlewareConstant.MW_SUBMISSION_LATENCY_SLA_IN_MILLIS);
                    colLongValue = Long.parseLong(colValue);
                    break;
            }

            if (Kafka2ESConstants.colInitNullValue.equals(colValue))
                continue;

            isEmptyJSON = false;

            if (colType.equals(Kafka2ESConstants.colTypeInteger))
                subJSON.put(mapColName, colIntValue);
            else
                if (colType.equals(Kafka2ESConstants.colTypeLong))
                    subJSON.put(mapColName, colLongValue);
            	else
	                if (colType.equals(Kafka2ESConstants.colTypeFloat))
	                    subJSON.put(mapColName, colFloatValue);
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

    @SuppressWarnings("unchecked")
    public static JSONObject buildSubFMSGJSON(
            JSONObject joSubObject,
            String baseMsgId)
    {
        boolean          isEmptyJSON = true;
        String           colValue    = null;
        String           colName     = null;
        final JSONObject fmsgJSON    = new JSONObject();
        fmsgJSON.put(SubmissionK2ES.ESFmsgIndexUniqueColumn, baseMsgId);

        final int msgPartNo = (int) joSubObject.get("msg_part_no");

        if ((msgPartNo == 0) || (msgPartNo == 1))
        {
            colName  = "cli_id";
            colValue = CommonUtility.nullCheck(joSubObject.get(colName));

            if (!"".equals(colValue))
            {
                fmsgJSON.put(colName, colValue);
                isEmptyJSON = false;
            }

            colName  = "campaign_id";
            colValue = CommonUtility.nullCheck(joSubObject.get(colName));

            if (!"".equals(colValue))
            {
                fmsgJSON.put(colName, colValue);
                isEmptyJSON = false;
            }
            colName  = "campaign_name";
            colValue = CommonUtility.nullCheck(joSubObject.get(colName));

            if (!"".equals(colValue))
            {
                fmsgJSON.put(colName, colValue);
                isEmptyJSON = false;
            }

            colName  = "file_id";
            colValue = CommonUtility.nullCheck(joSubObject.get(colName));

            if (!"".equals(colValue))
            {
                fmsgJSON.put(colName, colValue);
                isEmptyJSON = false;
            }

            colName  = "intf_grp_type";
            colValue = CommonUtility.nullCheck(joSubObject.get(colName));

            if (!"".equals(colValue))
            {
                fmsgJSON.put(colName, colValue);
                isEmptyJSON = false;
            }

            colName  = "total_msg_parts";
            colValue = CommonUtility.nullCheck(joSubObject.get(colName));

            if (!"".equals(colValue))
            {
                fmsgJSON.put(colName, Integer.parseInt(colValue));
                isEmptyJSON = false;
            }

            colName  = "recv_date";
            colValue = CommonUtility.nullCheck(joSubObject.get(colName));

            if (!"".equals(colValue))
            {
                fmsgJSON.put(colName, colValue);
                isEmptyJSON = false;
            }

            colName  = "recv_time";
            colValue = CommonUtility.nullCheck(joSubObject.get(colName));

            if (!"".equals(colValue))
            {
                fmsgJSON.put(colName, colValue);
                isEmptyJSON = false;
            }

            if (isEmptyJSON)
                return null;
        }

        colName  = "sub_ori_sts_code";
        colValue = CommonUtility.nullCheck(joSubObject.get(colName));

        if (!"".equals(colValue))
            if (colValue.equals(Kafka2ESConstants.subStsCodeSuccess))
                fmsgJSON.put("sub_success", "1");
            else
                fmsgJSON.put("sub_failed", "1");

        return fmsgJSON;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject buildDelJSON(
            IMessage iMsg)
    {
        boolean              isEmptyJSON = true;
        final DeliveryObject delObject   = (DeliveryObject) iMsg;
        final BaseMessage    baseMessage = delObject;

        final JSONObject     delJSON     = new JSONObject();

        final String         msgId       = delObject.getMessageId();
        delJSON.put(SubmissionK2ES.ESIndexUniqueColumn, msgId);

        final String dataUpdTime = DateTimeUtility.getFormattedCurrentDateTime(DateTimeFormat.DEFAULT);
        delJSON.put(SubmissionK2ES.ESDocUpdTmColumn, dataUpdTime);

        for (final ESIndexColMapValue esColMap : SubmissionK2ES.ListESColMap)
        {
            final String  colName       = esColMap.ColumnName;
            final String  colType       = esColMap.ColumnType;
            final String  mapColName    = esColMap.MappedName;
            // final String defaultVal = esColMap.DefaultValue;
            final boolean ciRequired    = esColMap.CIColumnRequired;

            String        colValue      = Kafka2ESConstants.colInitNullValue;
            final int     colIntValue   = -1;
            long          colLongValue   = -1L;
            double        colFloatValue = 0;

            switch (colName)
            {
                case "base_msg_id":
                    colValue = delObject.getBaseMessageId();
                    break;

                case "billing_add_fixed_rate":
                    colFloatValue = delObject.getBillingAddFixedRate();
                    colValue = "" + colFloatValue;
                    break;

                case "billing_sms_rate":
                    colFloatValue = delObject.getBillingSmsRate();
                    colValue = "" + colFloatValue;
                    break;

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
                    
                case "delv_lat_sla_in_millis":
                    colValue = baseMessage.getValue(MiddlewareConstant.MW_DELV_LATENCY_SLA_IN_MILLIS);
                    colLongValue = Long.parseLong(colValue);
                    break;
            }

            if (Kafka2ESConstants.colInitNullValue.equals(colValue))
                continue;

            isEmptyJSON = false;

            if (colType.equals(Kafka2ESConstants.colTypeInteger))
                delJSON.put(mapColName, colIntValue);
            else
                if (colType.equals(Kafka2ESConstants.colTypeLong))
                	delJSON.put(mapColName, colLongValue);
            	else
	                if (colType.equals(Kafka2ESConstants.colTypeFloat))
	                    delJSON.put(mapColName, colFloatValue);
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

    @SuppressWarnings("unchecked")
    public static JSONObject buildDelFMSGJSON(
            JSONObject joDelObject,
            String baseMsgId)
    {
        boolean          isEmptyJSON = true;
        String           colValue    = null;
        String           colName     = null;
        final JSONObject fmsgJSON    = new JSONObject();
        fmsgJSON.put(SubmissionK2ES.ESFmsgIndexUniqueColumn, baseMsgId);
        colName  = "dn_ori_sts_code";
        colValue = CommonUtility.nullCheck(joDelObject.get(colName));

        if (!"".equals(colValue))
        {
            if (colValue.equals(Kafka2ESConstants.delStsCodeSuccess))
                fmsgJSON.put("dn_success", "1");
            else
                fmsgJSON.put("dn_failed", "1");
            isEmptyJSON = false;
        }

        if (isEmptyJSON)
            return null;

        return fmsgJSON;
    }

}
