package com.winnovature.initialstate.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.initialstate.singletons.InitialStagePropertiesTon;
import com.winnovature.initialstate.utils.Constants;
import com.winnovature.initialstate.utils.FileSender;
import com.winnovature.utils.singletons.ConnectionFactoryForCMDB;

public class GroupCampaignDAO {

	static Log log = LogFactory.getLog(Constants.InitialStageLogger);
	static Log hrtBtLog = LogFactory.getLog(Constants.InitialStageHeartBeat);
	private static final String className = "[GroupCampaignDAO]";
	PropertiesConfiguration prop = null;
	
	public GroupCampaignDAO(){
	}

	public void pollGroupsCampaigns(String maxRetryCount, String groupQueueName) throws Exception {

		String methodName = " [pollGroupsCampaigns] ";
		PropertiesConfiguration prop = InitialStagePropertiesTon
				.getInstance().getPropertiesConfiguration();
		String status = prop.getString(Constants.FILE_SMS_ALL_STATUS);
		long sleepTime = com.winnovature.utils.utils.Utility.getConsumersSleepTime();
		String instanceId = prop.getString(Constants.MONITORING_INSTANCE_ID);

		String sql = "select cm.id as cm_id, cm.cli_id, cm.username, cg.id as cg_id, cg.group_id, cg.exclude_group_ids, IFNULL(cg.total,0) as total, IFNULL(cg.retry_count,0) as retry_count, cg.created_ts"
				+ " from cm.campaign_master cm, cm.campaign_groups cg where cm.id = cg.c_id and lower(cm.status) in ("+ status +") and lower(cg.status) in ("+ status +") and "
				+ " IFNULL(cg.retry_count,0) <=? ORDER BY cm.cli_id, cg.total ";

		if (hrtBtLog.isDebugEnabled())
			hrtBtLog.debug(className + methodName +" sql "+ sql);

		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		Map<String, List<Map<String, String>>> requestsList = new HashMap<String, List<Map<String, String>>>();

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
							+ " Group SMS request found in campaign_groups : id "
							+ rs.getString("cg_id"));
				}
				campMasterMap.put("cm_id", cm_id);
				campMasterMap.put("cg_id", rs.getString("cg_id"));
				campMasterMap.put("cli_id", rs.getString("cli_id"));
				campMasterMap.put("username", rs.getString("username"));
				campMasterMap.put("created_ts", rs.getString("created_ts"));
				campMasterMap.put("exclude_group_ids", rs.getString("exclude_group_ids"));
				campMasterMap.put("retry_count", rs.getString("retry_count"));
				campMasterMap.put("group_id", rs.getString("group_id"));
				campMasterMap.put("total", rs.getString("total"));
				
				String key = rs.getString("cli_id") + "~" + rs.getString("cm_id");
				if(requestsList.containsKey(key)) {
					List<Map<String, String>> data = requestsList.get(key);
					data.add(campMasterMap);
					data.sort(new CountSorter1());
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
						try {
							handoverToRedis(campaignFileInfo, groupQueueName, campaignFileInfo.get("cm_id"),
									"GroupsCampaignQ HO Failed",instanceId);
						}catch(Exception e) {
							updateGroupCampaignStatus(campaignFileInfo, Constants.PROCESS_STATUS_FAILED, instanceId, "FileSplitQ HO Failed");
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
		// update record status as inprocess
		updateGroupCampaignStatus(campInfo, Constants.PROCESS_STATUS_GRPINPROGRESS,instanceId,null);
		// HO to GroupsCampaignQ
		boolean fileSenderStatus = FileSender.sendToFileQueue(campInfo, queueName);
		// if HO to redis failed update status=FAILED
		if (!fileSenderStatus) {
			updateGroupCampaignStatus(campInfo, Constants.PROCESS_STATUS_FAILED, instanceId, reason);
		}
	}
	
	public void updateGroupCampaignStatus(Map<String, String> campInfo, String status, String instanceId, String reason)
			throws Exception {

		String methodName = "[updateGroupCampaignStatus]";
		
		StringBuilder campaign_master_sql = new StringBuilder("update campaign_master SET status = ? ");
		if(status.equalsIgnoreCase(Constants.PROCESS_STATUS_FAILED)) {
			campaign_master_sql.append(", reason=? ");
		}
		campaign_master_sql.append(" where id=? and cli_id=?");
		
		StringBuilder campaign_groups_sql = new StringBuilder("update campaign_groups SET status = ?, instance_id=? ");
		if(status.equalsIgnoreCase(Constants.PROCESS_STATUS_FAILED)) {
			campaign_groups_sql.append(", retry_count=IFNULL(retry_count,0)+1, reason=? ");
		}else {
			campaign_groups_sql.append(", started_ts=now() ");
		}
		campaign_groups_sql.append(" where id=? and c_id=?");
		
		if (log.isDebugEnabled()) {
			log.debug(className + methodName + "campaign_master update sql - " + campaign_master_sql);
			log.debug(className + methodName + "campaign_groups update sql - " + campaign_groups_sql);
		}

		Connection con = null;
		PreparedStatement campaignMasterPstmt = null;
		PreparedStatement campaignGroupsPstmt = null;
		
		int campaingMasterUpdateCount = 0, campaingGroupsUpdateCount = 0;
		
		try {
			con = ConnectionFactoryForCMDB.getInstance().getConnection();
			con.setAutoCommit(false);
			campaignMasterPstmt = con.prepareStatement(campaign_master_sql.toString());
			if(status.equalsIgnoreCase(Constants.PROCESS_STATUS_FAILED)) {
				campaignMasterPstmt.setString(1, status);
				campaignMasterPstmt.setString(2, reason);
				campaignMasterPstmt.setString(3, campInfo.get("cm_id"));
				campaignMasterPstmt.setString(4, campInfo.get("cli_id"));
			}else {
				campaignMasterPstmt.setString(1, status);
				campaignMasterPstmt.setString(2, campInfo.get("cm_id"));
				campaignMasterPstmt.setString(3, campInfo.get("cli_id"));
			}
			campaingMasterUpdateCount = campaignMasterPstmt.executeUpdate();
			
			if(campaingMasterUpdateCount > 0) {
				campaignGroupsPstmt = con.prepareStatement(campaign_groups_sql.toString());
				if(status.equalsIgnoreCase(Constants.PROCESS_STATUS_FAILED)) {
					campaignGroupsPstmt.setString(1, status);
					campaignGroupsPstmt.setString(2, instanceId);
					campaignGroupsPstmt.setString(3, reason);
					campaignGroupsPstmt.setString(4, campInfo.get("cg_id"));
					campaignGroupsPstmt.setString(5, campInfo.get("cm_id"));
				}else {
					campaignGroupsPstmt.setString(1, status);
					campaignGroupsPstmt.setString(2, instanceId);
					campaignGroupsPstmt.setString(3, campInfo.get("cg_id"));
					campaignGroupsPstmt.setString(4, campInfo.get("cm_id"));
				}
				
				campaingGroupsUpdateCount = campaignGroupsPstmt.executeUpdate();
				if(campaingGroupsUpdateCount > 0) {
					con.commit();
				}else {
					con.rollback();
				}
			}else {
				con.rollback();
			}
			
			if (log.isDebugEnabled()) {
				log.debug(className + methodName + " campaign_master update count : " + campaingMasterUpdateCount+" campaign_groups update count : "+campaingGroupsUpdateCount);
			}
		} catch (Exception sqlex) {
			log.error(className + methodName, sqlex);
			con.rollback();
			throw sqlex;
		} finally {
			closeConnection(null, campaignMasterPstmt, con);
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

class CountSorter1 implements Comparator<Map<String, String>> {
	@Override
	public int compare(Map<String, String> o1, Map<String, String> o2) {
		Integer cnt1 = Integer.parseInt(o1.get("total"));
		Integer cnt2 = Integer.parseInt(o2.get("total"));
		return cnt1.compareTo(cnt2);
	}
}
