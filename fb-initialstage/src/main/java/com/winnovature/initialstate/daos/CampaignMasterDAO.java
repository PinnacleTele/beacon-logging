package com.winnovature.initialstate.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.inmemdata.account.UserInfo;
import com.winnovature.initialstate.singletons.InitialStagePropertiesTon;
import com.winnovature.initialstate.utils.Constants;
import com.winnovature.initialstate.utils.FileSender;
import com.winnovature.utils.singletons.ConnectionFactoryForCMDB;
import com.winnovature.utils.utils.JsonUtility;
import com.winnovature.utils.utils.UserDetails;

public class CampaignMasterDAO {

	static Log log = LogFactory.getLog(Constants.InitialStageLogger);
	static Log hrtBtLog = LogFactory.getLog(Constants.InitialStageHeartBeat);
	private static final String className = "[CampaignMasterDAO]";
	PropertiesConfiguration prop = null;
	
	public CampaignMasterDAO(){
	}

	public void pollCampaigns(String maxRetryCount, String fileSplitQueueName) throws Exception {

		String methodName = " [pollCampaigns] ";
		PropertiesConfiguration prop = InitialStagePropertiesTon
				.getInstance().getPropertiesConfiguration();
		String status = prop.getString(Constants.FILE_SMS_ALL_STATUS);
		long sleepTime = com.winnovature.utils.utils.Utility.getConsumersSleepTime();
		String instanceId = prop.getString(Constants.MONITORING_INSTANCE_ID);

		String sql = "select cm.id as cm_id, cm.cli_id, cm.username, cm.c_name, cm.msg, cm.header, cm.template_id, cm.template_type, cm.template_mobile_column, "
				+ " cm.dlt_entity_id, cm.dlt_template_id, cm.c_type, cm.c_lang_type, cm.remove_dupe_yn, cm.scheduled_ts, cm.ipaddr, cm.sessionid, "
				+ " cm.created_ts, cf.id as cf_id, cf.group_id, cf.filename_ori, cf.fileloc, cf.exclude_group_ids, cf.retry_count, cf.total, cf.reason, cm.shorten_url, cm.intl_header "
				+ " from cm.campaign_master cm, cm.campaign_files cf where cm.id = cf.c_id and lower(cm.status) in ("+ status +") and lower(cf.status) in ("+ status +") and "
				+ " IFNULL(cf.retry_count,0) <=? ORDER BY cm.cli_id,cf.total ";

		if (hrtBtLog.isDebugEnabled())
			hrtBtLog.debug(className + methodName + sql);

		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		Map<String, List<Map<String, String>>> requestsList = new HashMap<String, List<Map<String, String>>>();
		UserInfo userDetailsByClientId = null;
		Map<String, String> userClusters = new HashMap<String, String>();
		try {
			con = ConnectionFactoryForCMDB.getInstance().getConnection();

			pstmt = con.prepareStatement(sql);

			pstmt.setInt(1, Integer.parseInt(maxRetryCount));
			rs = pstmt.executeQuery();
			
			boolean foundData = false;

			while (rs.next()) {
				foundData = true;
				Map<String, String> campMasterMap = new HashMap<String, String>();
				String cm_id = rs.getString("cm_id");
				if (log.isDebugEnabled()) {
					log.debug(className
							+ methodName
							+ " SMS request found in campaign_master. "
							+ com.winnovature.utils.utils.Constants.FILEID_FOR_LOGGER
							+ rs.getString("cf_id"));
				}
				//int noOfRetry = rs.getInt("retry_count");
				campMasterMap.put("cm_id", cm_id);
				campMasterMap.put("cf_id", rs.getString("cf_id"));
				campMasterMap.put("cli_id", rs.getString("cli_id"));
				campMasterMap.put("c_name", rs.getString("c_name"));
				campMasterMap.put("header", rs.getString("header"));
				campMasterMap.put("intl_header", rs.getString("intl_header"));
				campMasterMap.put("fileid", rs.getString("cf_id"));
				campMasterMap.put("ipaddr", rs.getString("ipaddr"));
				campMasterMap.put("sessionid", rs.getString("sessionid"));
				campMasterMap.put("filename_ori",
						rs.getString("filename_ori"));
				campMasterMap.put("remove_dupe_yn", rs.getString("remove_dupe_yn"));
				campMasterMap.put("msg", rs.getString("msg"));
				campMasterMap.put("msg_original", rs.getString("msg"));
				campMasterMap.put("c_lang_type", rs.getString("c_lang_type"));
				campMasterMap.put("c_type", rs.getString("c_type"));
				campMasterMap.put("fileloc", rs.getString("fileloc"));
				campMasterMap.put("username", rs.getString("username"));
				campMasterMap.put("created_ts", rs.getString("created_ts"));
				if(rs.getString("scheduled_ts") != null && rs.getString("scheduled_ts").length() >0)
				{
					SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Date schdate = formatter.parse(rs.getString("scheduled_ts"));
					String strschdate = formatter.format(schdate);
					campMasterMap.put("scheduled_ts", strschdate);
				}else {
					campMasterMap.put("scheduled_ts", rs.getString("scheduled_ts"));
				}
				campMasterMap.put("exclude_group_ids", rs.getString("exclude_group_ids"));
				campMasterMap.put("retry_count", rs.getString("retry_count"));
				campMasterMap.put("group_id", rs.getString("group_id"));
				campMasterMap.put("template_id", rs.getString("template_id"));
				campMasterMap.put("template_type", rs.getString("template_type"));
				campMasterMap.put("template_mobile_column", rs.getString("template_mobile_column"));
				campMasterMap.put("dlt_entity_id", rs.getString("dlt_entity_id"));
				campMasterMap.put("dlt_template_id", rs.getString("dlt_template_id"));
				campMasterMap.put("shorten_url", rs.getString("shorten_url"));
				campMasterMap.put("total", rs.getString("total"));
				campMasterMap.put("reason", rs.getString("reason"));
				
				String key = rs.getString("cli_id") + "~" + rs.getString("cm_id");
				if(requestsList.containsKey(key)) {
					List<Map<String, String>> data = requestsList.get(key);
					data.add(campMasterMap);
					data.sort(new CountSorter());
				}else {
					List<Map<String, String>> data = new ArrayList<Map<String, String>>();
					data.add(campMasterMap);
					requestsList.put(key, data);
				}
			} // end of result set iteration
			
			// No data found let consumer rest for some time
			if(!foundData) {
				if (hrtBtLog.isDebugEnabled())
					hrtBtLog.debug(className + methodName + " No request found with matching criteria, sleeping for "+sleepTime+" milli seconds.");
				consumerSleep(sleepTime);
			}
			
			while(requestsList.size() > 0) {
				Iterator<String> it = requestsList.keySet().iterator();
				while (it.hasNext()) {
					String key = (String) it.next();
					List<Map<String, String>> campaignFiles = requestsList.get(key);
					if (campaignFiles.size() > 0) {
						Map<String, String> campaignFileInfo = campaignFiles.remove(0);
						String cluster = userClusters.get(campaignFileInfo.get("cli_id"));
						if(cluster == null) {
							userDetailsByClientId = UserDetails.getUserInfo(campaignFileInfo.get("cli_id"));
							Map<String, String> userMap = new JsonUtility()
									.convertJsonStringToMap(userDetailsByClientId.getAccountDetails());
							if (userMap != null && userMap.get("platform_cluster") != null) {
								cluster = userMap.get("platform_cluster");
								userClusters.put(campaignFileInfo.get("cli_id"), cluster);
							}
						}
						
						if("otp".equalsIgnoreCase(cluster)) {
							updateCampaignStatus(campaignFileInfo, Constants.PROCESS_STATUS_FAILED, instanceId, "File not processed, platform_cluster=otp", false);
						}else {
							try {
								// sms with file
								handoverToRedis(campaignFileInfo, fileSplitQueueName, campaignFileInfo.get("cm_id"),
										"FileSplitQ HO Failed",instanceId);
							}catch(Exception e) {
								updateCampaignStatus(campaignFileInfo, Constants.PROCESS_STATUS_FAILED, instanceId, "FileSplitQ HO Failed", true);
							}
						}						
					} else {
						it.remove();
					}
				}
			}

		} catch (Exception e) {
			log.error(className + methodName + "Exception: ", e);
			throw e;
		} finally {
			closeConnection(rs, pstmt, con);
		}
	}
	
	
	
	public void handoverToRedis(Map<String, String> campInfo, String queueName,
			String id, String reason,String instanceId) throws Exception {
		// update master record status as inprocess
		updateCampaignStatus(campInfo, Constants.PROCESS_STATUS_INPROGRESS,instanceId,null, true);
		// HO to FileSplitQ
		boolean fileSenderStatus = FileSender.sendToFileQueue(campInfo, queueName);
		// if HO to redis failed update status=FAILED
		if (!fileSenderStatus) {
			updateCampaignStatus(campInfo, Constants.PROCESS_STATUS_FAILED, instanceId, reason, true);
		}
	}
	
	public void updateCampaignStatus(Map<String, String> campInfo, String status, String instanceId, String reason, boolean retry)
			throws Exception {

		String methodName = "[updateCampaignStatus]";
		
		StringBuilder campaign_master_sql = new StringBuilder("update campaign_master SET status = ? ");
		if(status.equalsIgnoreCase(Constants.PROCESS_STATUS_FAILED)) {
			campaign_master_sql.append(", reason=? ");
		}
		campaign_master_sql.append(" where id=? and cli_id=?");
		
		StringBuilder campaign_files_sql = new StringBuilder("update campaign_files SET status = ?, instance_id=? ");
		if(status.equalsIgnoreCase(Constants.PROCESS_STATUS_FAILED)) {
			if(retry) {
				campaign_files_sql.append(", retry_count=IFNULL(retry_count,0)+1, reason=? ");
			}else {
				campaign_files_sql.append(", retry_count=6, reason=? ");
			}
		}else {
			campaign_files_sql.append(", started_ts=now() ");
		}
		campaign_files_sql.append(" where id=? and c_id=?");
		
		if (log.isDebugEnabled()) {
			log.debug(className + methodName + "campaign_master update sql - " + campaign_master_sql);
			log.debug(className + methodName + "campaign_files update sql - " + campaign_files_sql);
		}

		Connection con = null;
		PreparedStatement campaignMsaterPstmt = null;
		PreparedStatement campaignFilesPstmt = null;
		
		int campaingMasterUpdateCount = 0, campaingFilesUpdateCount = 0;
		
		try {
			con = ConnectionFactoryForCMDB.getInstance().getConnection();
			con.setAutoCommit(false);
			campaignMsaterPstmt = con.prepareStatement(campaign_master_sql.toString());
			if(status.equalsIgnoreCase(Constants.PROCESS_STATUS_FAILED)) {
				campaignMsaterPstmt.setString(1, status);
				campaignMsaterPstmt.setString(2, reason);
				campaignMsaterPstmt.setString(3, campInfo.get("cm_id"));
				campaignMsaterPstmt.setString(4, campInfo.get("cli_id"));
			}else {
				campaignMsaterPstmt.setString(1, status);
				campaignMsaterPstmt.setString(2, campInfo.get("cm_id"));
				campaignMsaterPstmt.setString(3, campInfo.get("cli_id"));
			}
			
			campaingMasterUpdateCount = campaignMsaterPstmt.executeUpdate();
			if(campaingMasterUpdateCount > 0) {
				campaignFilesPstmt = con.prepareStatement(campaign_files_sql.toString());
				if(status.equalsIgnoreCase(Constants.PROCESS_STATUS_FAILED)) {
					campaignFilesPstmt.setString(1, status);
					campaignFilesPstmt.setString(2, instanceId);
					campaignFilesPstmt.setString(3, reason);
					campaignFilesPstmt.setString(4, campInfo.get("cf_id"));
					campaignFilesPstmt.setString(5, campInfo.get("cm_id"));
				}else {
					campaignFilesPstmt.setString(1, status);
					campaignFilesPstmt.setString(2, instanceId);
					campaignFilesPstmt.setString(3, campInfo.get("cf_id"));
					campaignFilesPstmt.setString(4, campInfo.get("cm_id"));
				}
				
				campaingFilesUpdateCount = campaignFilesPstmt.executeUpdate();
				if(campaingFilesUpdateCount > 0) {
					con.commit();
				}else {
					con.rollback();
				}
			}else {
				con.rollback();
			}
			
			if (log.isDebugEnabled()) {
				log.debug(className + methodName + " campaign_master update count : " + campaingMasterUpdateCount+" campaign_files update count : "+campaingFilesUpdateCount);
			}
		} catch (Exception sqlex) {
			log.error(className + methodName, sqlex);
			con.rollback();
			throw sqlex;
		} finally {
			closeConnection(null, campaignMsaterPstmt, con);
		}
	}
	
	private void closeConnection(ResultSet rs, PreparedStatement ps,
			Connection con) throws Exception {
		if (rs != null) {
			rs.close();
		}
		if (ps != null) {
			ps.close();
		}
		if (con != null) {
			con.close();
		}
	}
	
	private void consumerSleep(long sleepTime) {
		try {
			TimeUnit.MILLISECONDS.sleep(sleepTime);
		} catch (Exception e) {
			log.error("*** " + className, e);
		}
	}

}

class CountSorter implements Comparator<Map<String, String>> {
	@Override
	public int compare(Map<String, String> o1, Map<String, String> o2) {
		Integer cnt1 = Integer.parseInt(o1.get("total"));
		Integer cnt2 = Integer.parseInt(o2.get("total"));
		return cnt1.compareTo(cnt2);
	}
}
