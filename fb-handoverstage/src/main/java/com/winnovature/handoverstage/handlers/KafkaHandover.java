package com.winnovature.handoverstage.handlers;

import java.util.EnumMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.messageidentifier.MessageIdentifier;
import com.itextos.beacon.http.interfaceutil.uiftp.InterfaceConstant;
import com.itextos.beacon.http.interfaceutil.uiftp.InterfaceRequest;
import com.itextos.beacon.inmemdata.account.UserInfo;
import com.winnovature.handoverstage.singletons.HandoverStagePropertiesTon;
import com.winnovature.handoverstage.utils.Constants;

public class KafkaHandover {

	static Log log = LogFactory.getLog(Constants.HandoverStageLogger);
	static Log kafkaLog = LogFactory.getLog(Constants.KafkaHandoverLogger);
	private static final String className = "[KafkaHandover]";

	public static void sendToKafka(EnumMap<InterfaceConstant, String> aRequest, String aAccJson) throws Exception {
		try {
			InterfaceRequest.sendToKafka(aRequest, aAccJson,new StringBuffer());
		} catch (Exception e) {
			throw e;
		} catch (Throwable t) {
			throw t;
		}
	}

	public static EnumMap<InterfaceConstant, String> prepareDataAndSendToKafka(Map<String, String> data, UserInfo userDetailsByClientId, long count)
			throws Exception {
		String methodName = className + " [prepareDataAndSendToKafka] ";
		EnumMap<InterfaceConstant, String> aRequest = null;
		
		try {
			aRequest = new EnumMap<InterfaceConstant, String>(InterfaceConstant.class);
			aRequest.put(InterfaceConstant.CLIENT_ID, data.get("clientId"));
			aRequest.put(InterfaceConstant.MOBILE_NUMBER, data.get("mobile"));
			aRequest.put(InterfaceConstant.MESSAGE, data.get("message"));
			aRequest.put(InterfaceConstant.MESSAGE_TAG, data.get("message_tag"));
			aRequest.put(InterfaceConstant.IS_HEX_MSG, data.get("is_hex_msg"));
			aRequest.put(InterfaceConstant.MESSAGE_CLASS, data.get("message_class"));
			aRequest.put(InterfaceConstant.RECEIVED_DATE, data.get("received_date"));
			aRequest.put(InterfaceConstant.RECEIVED_TIME, data.get("received_time"));
			aRequest.put(InterfaceConstant.HEADER, data.get("header"));
			aRequest.put(InterfaceConstant.INTL_HEADER, data.get("intl_header"));
			// always dlr acknowledgement required, hence set 1
			aRequest.put(InterfaceConstant.DLR_REQURIED, "1");
			aRequest.put(InterfaceConstant.MAX_MESSAGE_VALIDITY_SEC, "0");
			aRequest.put(InterfaceConstant.APP_INSTANCE_ID, data.get("APP_INSTANCE_ID"));
			aRequest.put(InterfaceConstant.FILE_ID, data.get("file_id"));
			aRequest.put(InterfaceConstant.BASE_MESSAGE_ID, MessageIdentifier.getInstance().getNextId());
			aRequest.put(InterfaceConstant.CLIENT_IP, data.get("user_ip"));
			aRequest.put(InterfaceConstant.CLIENT_MESSAGE_ID, null);
			aRequest.put(InterfaceConstant.DLT_ENTITY_ID, data.get("dlt_entity_id"));
			aRequest.put(InterfaceConstant.DLT_TEMPLATE_ID, data.get("dlt_template_id"));
			aRequest.put(InterfaceConstant.UI_DUP_CHECK_ENABLE, data.get("dupe_yn"));
			aRequest.put(InterfaceConstant.CAMPAIGN_ID, data.get("camp_id"));
			aRequest.put(InterfaceConstant.CAMPAIGN_NAME, data.get("camp_name"));
			aRequest.put(InterfaceConstant.UI_VL_SHORT_REQ, data.get("shorten_url"));
			aRequest.put(InterfaceConstant.MSG_TAG1, data.get("tag1"));
			aRequest.put(InterfaceConstant.MSG_TAG2, data.get("tag2"));
			aRequest.put(InterfaceConstant.MSG_TAG3, data.get("tag3"));
			aRequest.put(InterfaceConstant.MSG_TAG4, data.get("tag4"));
			aRequest.put(InterfaceConstant.MSG_TAG5, data.get("tag5"));
			aRequest.put(InterfaceConstant.PRIORITY, data.get("PRIORITY"));

			// TODO - remove conditional ho to kafka once testing done
			boolean isKafkaHOEnabled = HandoverStagePropertiesTon.getInstance().getPropertiesConfiguration()
					.getBoolean("kafka.handover.enabled", true);
			if (isKafkaHOEnabled) {
				sendToKafka(aRequest, userDetailsByClientId.getAccountDetails());
				if (kafkaLog.isDebugEnabled()) {
					kafkaLog.debug(methodName + " Handover success, campaign name : " + data.get("camp_name")
							+ " ::: split file id : " + data.get("c_f_s_id") + " ::: count : " + count
							+ " for aRequest ::: " + aRequest + " aAccJson ::: "
							+ userDetailsByClientId.getAccountDetails());
				}
			}
		} catch (ItextosException e) {
			log.error(methodName + " ItextosException ", e);
			kafkaLog.error(methodName + " ItextosException ", e);
			throw e;
		} catch (Exception e) {
			log.error(methodName + " Exception ", e);
			kafkaLog.error(methodName + " Exception ", e);
		} catch (Throwable t) {
			log.error(methodName + " Throwable ", t);
			kafkaLog.error(methodName + " Throwable ", t);
			throw new Exception(t.getMessage());
		}
		return aRequest;
	}

}
