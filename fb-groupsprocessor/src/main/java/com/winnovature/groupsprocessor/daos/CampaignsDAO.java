package com.winnovature.groupsprocessor.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.groupsprocessor.singletons.GroupsProcessorPropertiesTon;
import com.winnovature.groupsprocessor.utils.Constants;
import com.winnovature.utils.singletons.ConfigParamsTon;
import com.winnovature.utils.singletons.ConnectionFactoryForCMDB;

public class CampaignsDAO {
	static Log log = LogFactory.getLog(Constants.GroupsProcessorLogger);
	private static final String className = "[CampaignsDAO]";
	PropertiesConfiguration prop = null;
	
	public void updateFileStartTime(String id) throws Exception {

		String methodName = " [updateFileStartTime] ";
		if (log.isDebugEnabled())
			log.debug(className + methodName + "begin id:" + id);

		Connection con = null;
		PreparedStatement pstmt = null;
		String sql = "update campaign_groups set started_ts=now()  where id = ? ";
		if (log.isDebugEnabled())
			log.debug(className + methodName + "sql = " + sql);

		try {

			con = ConnectionFactoryForCMDB.getInstance().getConnection();
			pstmt = con.prepareStatement(sql);

			pstmt.setString(1, id);

			pstmt.executeUpdate();

		} catch (Exception sqlex) {
			log.error(className + methodName + "SQLException: " + sqlex);
			throw sqlex;
		} finally {
			closeConnection(null, pstmt, con);
		}
		if (log.isDebugEnabled())
			log.debug(className + methodName + "end ..");
	}
	
	public void insertToCampaignFilesAndUpdateCampaignGroups(Map<String, String> campInfo)
			throws Exception {

		String methodName = " [insertToCampaignFilesAndUpdateCampaignGroups] ";
		
		StringBuilder campaign_files_sql = new StringBuilder("insert into campaign_files (");
		campaign_files_sql.append(" id,c_id,group_id,fileloc,exclude_group_ids,total,status,created_ts)");
		campaign_files_sql.append(" values (?,?,?,?,?,?,?,now())");
		
		StringBuilder campaign_groups_sql = new StringBuilder("update campaign_groups SET status = ?, instance_id=?, fileloc=?,total=?, completed_ts=now() where id=?");
		
		if (log.isDebugEnabled()) {
			log.debug(className + methodName + "campaign_files update sql - " + campaign_files_sql);
			log.debug(className + methodName + "campaign_groups update sql - " + campaign_groups_sql);
		}

		Connection con = null;
		PreparedStatement campaignFilesPstmt = null;
		PreparedStatement campaignGroupsPstmt = null;
		
		int campaingFilesInsertCount = 0, campaingGroupsUpdateCount = 0;
		
		try {
			con = ConnectionFactoryForCMDB.getInstance().getConnection();
			con.setAutoCommit(false);
			campaignFilesPstmt = con.prepareStatement(campaign_files_sql.toString());
			campaignFilesPstmt.setString(1, campInfo.get("cf_id"));
			campaignFilesPstmt.setString(2, campInfo.get("cm_id"));
			campaignFilesPstmt.setString(3, campInfo.get("group_id"));
			campaignFilesPstmt.setString(4, campInfo.get("fileloc"));
			campaignFilesPstmt.setString(5, campInfo.get("exclude_group_ids"));
			campaignFilesPstmt.setString(6, campInfo.get("total"));
			campaignFilesPstmt.setString(7, campInfo.get("insert_status"));
			
			campaingFilesInsertCount = campaignFilesPstmt.executeUpdate();
			
			if(campaingFilesInsertCount > 0) {
				campaignGroupsPstmt = con.prepareStatement(campaign_groups_sql.toString());
				campaignGroupsPstmt.setString(1, campInfo.get("update_status"));
				campaignGroupsPstmt.setString(2, campInfo.get("instance_id"));
				campaignGroupsPstmt.setString(3, campInfo.get("fileloc"));
				campaignGroupsPstmt.setString(4, campInfo.get("total"));
				campaignGroupsPstmt.setString(5, campInfo.get("cg_id"));
				
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
				log.debug(className + methodName + " campaign_files insert count : " + campaingFilesInsertCount+" campaign_groups update count : "+campaingGroupsUpdateCount);
			}
		} catch (Exception sqlex) {
			log.error(className + methodName, sqlex);
			con.rollback();
			throw sqlex;
		} finally {
			closeConnection(null, campaignFilesPstmt, con);
		}
	}
	
	public void updateCampaignStatus(String id, String reason, String status, String retry) throws Exception {

		String methodName = " [updateCampaignStatus] ";

		if (log.isDebugEnabled()) {
			log.debug(className + methodName + " id:" + id + " Reason:" + reason);
		}
		
		PropertiesConfiguration groupProperties = GroupsProcessorPropertiesTon.getInstance().getPropertiesConfiguration();

		String instanceId = groupProperties.getString(Constants.MONITORING_INSTANCE_ID);

		StringBuilder sql = new StringBuilder("update campaign_groups SET status = ? ");
		if (status.equalsIgnoreCase(Constants.PROCESS_STATUS_FAILED)) {
			sql.append(", retry_count=IFNULL(retry_count,0)+?, reason=?, ");
		}
		sql.append(" instance_id=? where id=?");
		
		if (log.isDebugEnabled()) {
			log.debug(className + methodName + "sql" + sql);
		}

		Connection con = null;
		PreparedStatement pstmt = null;

		try {
			con = ConnectionFactoryForCMDB.getInstance().getConnection();
			pstmt = con.prepareStatement(sql.toString());
			pstmt.setString(1, status);
			pstmt.setString(2, retry);
			pstmt.setString(3, reason);
			pstmt.setString(4, instanceId);
			pstmt.setString(5, id);
			int count = pstmt.executeUpdate();
			if (log.isDebugEnabled()) {
				log.debug(className + methodName + " update " + count);
			}
		} catch (Exception sqlex) {
			log.error(className + methodName + "Exception : ", sqlex);
			throw sqlex;
		} finally {
			closeConnection(null, pstmt, con);
		}
	}
	
	public List<Map<String, String>> getGroupCampaignInprocessRecords() throws Exception {

		String methodName = " [getGroupCampaignInprocessRecords] ";

		List<Map<String, String>> groupMasterList = new ArrayList<Map<String, String>>();
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		//String inprocessStatus = GroupsProcessorPropertiesTon.getInstance().getPropertiesConfiguration().getString(Constants.FILE_SMS_INPROCESS_STATUS);
		String inprocessStatus = Constants.PROCESS_STATUS_GRPINPROGRESS;
		StringBuilder sql = new StringBuilder();
		sql.append(" select id, status from campaign_master where lower(status)= ? and c_type='group'");

		if (log.isDebugEnabled()) {
			log.debug(className + methodName + "sql = " + sql.toString());
		}

		try {
			con = ConnectionFactoryForCMDB.getInstance().getConnection();
			pstmt = con.prepareStatement(sql.toString());

			pstmt.setString(1, inprocessStatus.toLowerCase());

			rs = pstmt.executeQuery();

			while (rs.next()) {
				Map<String, String> campaignMaster = new HashMap<String, String>();
				campaignMaster.put("id", rs.getString("id"));
				campaignMaster.put("status", rs.getString("status"));
				groupMasterList.add(campaignMaster);
			}

			if (log.isDebugEnabled()) {
				log.debug(className + methodName + " campaign_master grpinprocess list : " + groupMasterList);
			}
		} catch (Exception e) {
			log.error(className + methodName + " Exception:", e);
			throw e;
		} finally {
			closeConnection(rs, pstmt, con);
		}
		return groupMasterList;
	}
	
	public Map<String, String> getCampaignGroups(String masterId) throws Exception {

		String methodName = " [getCampaignGroups] ";
		if (log.isDebugEnabled()) {
			log.debug(className + methodName + "begin ..");
		}

		Map<String, String> masterCount = new HashMap<String, String>();
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		PropertiesConfiguration groupProperties = GroupsProcessorPropertiesTon.getInstance().getPropertiesConfiguration();

		String inprocessStatus = groupProperties.getString(Constants.FILE_SMS_INPROCESS_STATUS);
		String gInprocessStatus = Constants.PROCESS_STATUS_GRPINPROGRESS;

		String completeStatus = groupProperties.getString(Constants.COMPLETED_STATUS);

		String failedStatus = groupProperties.getString(Constants.FAILED_STATUS);

		String invalidFileStatus = groupProperties.getString(Constants.INVALID_STATUS);
		
		Map<String, String> configMap = (HashMap<String, String>) ConfigParamsTon.getInstance()
				.getConfigurationFromconfigParams();

		int fileNooftimesProcess = Integer
				.parseInt(configMap.get(com.winnovature.utils.utils.Constants.MAX_RETRY_COUNT));

		StringBuilder sql = new StringBuilder();
		sql.append(" select status, retry_count from campaign_groups ");
		sql.append(" where c_id = ? group By status, retry_count");

		if (log.isDebugEnabled()) {
			log.debug(className + methodName + "sql = " + sql.toString());
		}

		try {

			con = ConnectionFactoryForCMDB.getInstance().getConnection();
			pstmt = con.prepareStatement(sql.toString());
			pstmt.setString(1, masterId);
			rs = pstmt.executeQuery();
			boolean check = true;

			while (rs.next()) {
				String status = rs.getString("status");

				if (inprocessStatus.equalsIgnoreCase(status) || gInprocessStatus.equalsIgnoreCase(status)) {
					masterCount.put("status", "");
					check = false;
					continue;
				}

				int fileNooftimesProcessDB = rs.getString("retry_count") == null ? 0
						: Integer.parseInt(rs.getString("retry_count"));

				masterCount.put("retry_count", "" + fileNooftimesProcessDB);

				if (completeStatus.equalsIgnoreCase(status) && check) {
					masterCount.put("status", completeStatus);
					check = false;
					continue;
				}

				if (failedStatus.equalsIgnoreCase(status) && fileNooftimesProcessDB >= fileNooftimesProcess && check) {
					masterCount.put("status", failedStatus);
				}

				// Added to handle INVALID FILE status
				if (invalidFileStatus.equalsIgnoreCase(status) && check) {
					masterCount.put("status", invalidFileStatus);
				}
			}

			if (log.isDebugEnabled()) {
				log.debug(className + methodName + " masterCount : " + masterCount);
			}

		} catch (Exception e) {
			log.error(className + methodName + " Exception:", e);
			throw e;
		} finally {
			closeConnection(rs, pstmt, con);
		}
		if (log.isDebugEnabled()) {
			log.debug(className + methodName + "end ...");
		}
		return masterCount;
	}
	
	public boolean updateCampaignMaster(String id, Map<String, String> campaignGroupsData) throws Exception {
		String methodName = "[updateCampaignMaster]";

		boolean updateStatus = false;
		String status = campaignGroupsData.get("status") == null ? "" : campaignGroupsData.get("status");
		if (StringUtils.isBlank(status) || status.equalsIgnoreCase("inprocess")) {
			return false;
		}

		Connection con = null;
		PreparedStatement pstmt = null;

		String failedStatus = GroupsProcessorPropertiesTon.getInstance().getPropertiesConfiguration()
				.getString(Constants.FAILED_STATUS);

		StringBuilder sql = new StringBuilder();
		sql.append(" update campaign_master set status = ? ");
		if (failedStatus.equalsIgnoreCase(status)) {
			sql.append(", reason = ? ");
		}else {
			status = GroupsProcessorPropertiesTon.getInstance().getPropertiesConfiguration()
					.getString("status.queued");
		}
		sql.append(" where id = ? ");

		if (log.isDebugEnabled())
			log.debug(className + methodName + "sql:" + sql.toString());

		try {

			int count = 0;
			con = ConnectionFactoryForCMDB.getInstance().getConnection();
			pstmt = con.prepareStatement(sql.toString());

			pstmt.setString(1, status.toLowerCase());
			if (failedStatus.equalsIgnoreCase(status)) {
				pstmt.setString(2, campaignGroupsData.get("reason"));
				pstmt.setString(3, id);
			} else {
				pstmt.setString(2, id);
			}

			count = pstmt.executeUpdate();
			if (count > 0)
				updateStatus = true;
			if (log.isDebugEnabled()) {
				log.debug(className + methodName + "end ...");
			}
		} catch (Exception e) {
			log.error(className + methodName + " Exception:", e);
			updateStatus = false;
			throw e;
		} finally {
			closeConnection(null, pstmt, con);
		}
		return updateStatus;
	}
	
	private void closeConnection(ResultSet rs, PreparedStatement ps, Connection con) throws Exception {
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
	
}
