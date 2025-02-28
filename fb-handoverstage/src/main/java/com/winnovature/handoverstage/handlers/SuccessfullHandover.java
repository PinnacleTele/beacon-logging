package com.winnovature.handoverstage.handlers;

import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.handoverstage.daos.GenericDAO;
import com.winnovature.handoverstage.singletons.HandoverStagePropertiesTon;
import com.winnovature.handoverstage.singletons.RedisConnectionTon;
import com.winnovature.handoverstage.utils.Constants;
import com.winnovature.utils.singletons.ConfigParamsTon;
import com.winnovature.utils.utils.UnProcessListsCollector;

import redis.clients.jedis.Jedis;

public class SuccessfullHandover {

	static Log log = LogFactory.getLog(Constants.HandoverStageLogger);

	private static final String className = "[ SuccessfullHandover ] ";
	String methodName = null;
	private Map<String, String> mapObject;

	public SuccessfullHandover(Map<String, String> mapObject) {
		this.mapObject = mapObject;
	}

	public void handleSuccess(int total, int sucessCount, int failureCount,	List inValidAndDupLs, String reason) throws Exception {

		PropertiesConfiguration prop = HandoverStagePropertiesTon.getInstance().getPropertiesConfiguration();
		String id = mapObject.get("c_f_s_id");

		// adding exclude Count to total
		String exclude = mapObject.get("exclude") == null ? "0" : mapObject.get("exclude");
		int excludeCount = Integer.parseInt(exclude);
		// sucessCount = sucessCount + excludeCount;
		total = total + excludeCount;

		String status = prop.getString(Constants.FILE_SMS_COMPLETED_STATUS);
		//String reason = null;

		/*
		if (sucessCount > 0) {
			status = prop.getString(Constants.FILE_SMS_COMPLETED_STATUS);
		} else {
			status = prop.getString(Constants.FILE_SMS_FAILED_STATUS);
			reason = prop.getString(Constants.FILE_SMS_FAILED_REASON);
		}
		*/

		String instanceId = prop.getString(com.winnovature.utils.utils.Constants.MONITORING_INSTANCE_ID);

		updateSplitFileStatus(id, sucessCount, failureCount, total, status, reason,
				excludeCount, instanceId);

		// TODO - Need to handle unprocessed rows 
		//processInValidAndDupLs(inValidAndDupLs, id);

		if (log.isDebugEnabled()) {
			log.debug(className + methodName + com.winnovature.utils.utils.Constants.FILEID_FOR_LOGGER + id
					+ " SuccessfullHandover End ");
		}

	}
	
	public void dropRequest() throws Exception {
		String methodName = " [dropRequest] ";
		PropertiesConfiguration prop = HandoverStagePropertiesTon.getInstance().getPropertiesConfiguration();
		String id = mapObject.get("c_f_s_id");

		String status = prop.getString(Constants.FILE_SMS_FAILED_STATUS);
		String reason = prop.getString("cluster.otp.failed.reason");

		String instanceId = prop.getString(com.winnovature.utils.utils.Constants.MONITORING_INSTANCE_ID);

		try {
			GenericDAO.updateRequestStatus(id, 0, 0, 0, status, reason,
					0, instanceId, "6");
		} catch (Exception ex) {
			log.error(className + methodName + com.winnovature.utils.utils.Constants.FILEID_FOR_LOGGER + id
					+ " Exception while updating map:" + mapObject + " to completed..:");
		}

		if (log.isDebugEnabled()) {
			log.debug(className + methodName + com.winnovature.utils.utils.Constants.FILEID_FOR_LOGGER + id
					+ " SuccessfullHandover End ");
		}

	}

	private void processInValidAndDupLs(List inValidAndDupLs, String id) {
		String methodName = " [processInValidAndDupLs] ";

		if (log.isDebugEnabled())
			log.debug(
					className + methodName + " Begin " + com.winnovature.utils.utils.Constants.FILEID_FOR_LOGGER + id);

		if (inValidAndDupLs.size() > 0) {
			try {
				UnProcessListsCollector pushListsToUnprocessQ = new UnProcessListsCollector();
				pushListsToUnprocessQ.unprocessHandoverToQueue(inValidAndDupLs);
			} catch (Exception e) {
				log.error(className + methodName + com.winnovature.utils.utils.Constants.FILEID_FOR_LOGGER + id
						+ " Exception while pushing to pushToUnprocessQ: Exception:", e);
			}
		}
	}

	private void updateSplitFileStatus(String id, int sucessCount, int failureCount, int total, String status, String reason,
			int excludeCount, String instanceId) throws Exception {

		String methodName = "updateSplitFileStatus";
		try {
			String sql = null;
			try {
				// Frame sql for update Status
				sql = new GenericDAO().updateProcessStatusSql(id, sucessCount, failureCount, total, status, reason,
						excludeCount, instanceId);

				sendToUpdateQueue(sql, id);
			} catch (Exception e) {
				log.error(className + methodName + com.winnovature.utils.utils.Constants.FILEID_FOR_LOGGER + id
						+ " Exception while updating to Redis child update queue " + e);

				// If exception while sending to redis, update to DB
				try {
					GenericDAO.updateProcessStatus(id, sucessCount, failureCount, total, status, reason,
							excludeCount, instanceId);
				} catch (Exception ex) {
					// If could not update to DB, write to logger file
					log.error(className + methodName + com.winnovature.utils.utils.Constants.FILEID_FOR_LOGGER + id
							+ " Exception while updating map:" + mapObject + " to completed.. Sql:" + sql);
				}
			}
			/*
			try {
				// Frame sql for update Status
				sql = new GenericDAO().updateCountMasterSql(tagid, sucessCount, failureCount, duplicateCount,
						cancelledCount, sche_ts);
				sendToUpdateQueue(sql, id);
			} catch (Exception e) {
				log.error(className + methodName + com.winnovature.utils.utils.Constants.FILEID_FOR_LOGGER + id
						+ " Exception while updating master counts to Redis child update queue " + e);
				try {
					GenericDAO.updateCountMaster(tagid, sucessCount, failureCount, duplicateCount, cancelledCount,
							sche_ts, excludeCount);
				} catch (Exception ex) {
					// If could not update to DB, write to logger file
					log.error(className + methodName + com.winnovature.utils.utils.Constants.FILEID_FOR_LOGGER + id
							+ " Exception while updating to master file map:" + mapObject + " to completed.. Sql:"
							+ sql);
				}
			}
			*/
		} catch (Exception e) {
			log.error(
					className + methodName + " Exception: "
							+ com.winnovature.utils.utils.Constants.FILEID_FOR_LOGGER + id,
					e);

		}
	}

	private void sendToUpdateQueue(String sql, String id) throws Exception {

		Map<String, String> configParamsTon = ConfigParamsTon.getInstance().getConfigurationFromconfigParams();

		String statsUpdateStatusQueryQueueName = configParamsTon
				.get(com.winnovature.utils.utils.Constants.STATS_UPDATE_STATUS_QUERY_QUEUE_NAME);

		Jedis redis = null;
		try {
			redis = RedisConnectionTon.getInstance().getJedisConnectionAsRoundRobin();
			redis.lpush(statsUpdateStatusQueryQueueName, sql);
		} catch (Exception e) {
			log.error(className + methodName + com.winnovature.utils.utils.Constants.FILEID_FOR_LOGGER + id
					+ " Exception while sending to redis update queue", e);
			throw e;
		} finally {
			if (redis != null) {
				redis.close();
			}
		}
	}

}
