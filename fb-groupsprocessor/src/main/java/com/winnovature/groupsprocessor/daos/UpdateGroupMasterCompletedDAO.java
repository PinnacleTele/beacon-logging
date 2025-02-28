package com.winnovature.groupsprocessor.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.groupsprocessor.singletons.GroupsProcessorPropertiesTon;
import com.winnovature.groupsprocessor.utils.Constants;
import com.winnovature.utils.singletons.ConfigParamsTon;
import com.winnovature.utils.singletons.ConnectionFactoryForCMDB;

public class UpdateGroupMasterCompletedDAO {
	static Log log = LogFactory.getLog(Constants.GroupsProcessorLogger);

	private static final String className = "[UpdateCampaignMasterCompletedDAO]";

	static Map<String, String> configMap = null;
	PropertiesConfiguration groupProperties = null;

	public UpdateGroupMasterCompletedDAO() throws Exception {
		if (configMap == null) {
			configMap = ConfigParamsTon.getInstance().getConfigurationFromconfigParams();
		}
		this.groupProperties = GroupsProcessorPropertiesTon.getInstance().getPropertiesConfiguration();
	}

	public List<Map<String, String>> getInprocessRecords() throws Exception {

		String methodName = " [getInprocessRecords] ";

		List<Map<String, String>> groupMasterList = new ArrayList<Map<String, String>>();
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		String inprocessStatus = groupProperties.getString(Constants.FILE_SMS_INPROCESS_STATUS);

		StringBuilder sql = new StringBuilder();
		sql.append(" select id, g_type, status from group_master where lower(status)= ?");

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
				campaignMaster.put("g_type", rs.getString("g_type"));
				groupMasterList.add(campaignMaster);
			}

			if (log.isDebugEnabled()) {
				log.debug(className + methodName + " group_master inprocess list : " + groupMasterList);
			}
		} catch (Exception e) {
			log.error(className + methodName + " Exception:", e);
			throw e;
		} finally {
			closeConnection(rs, pstmt, con);
		}
		return groupMasterList;
	}

	public Map<String, String> getGroupFiles(String masterId) throws Exception {

		String methodName = " [getGroupFiles] ";

		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Map<String, String> result = new HashMap<String, String>();
		result.put("status", "inprocess");
		result.put("full_completion", "yes");

		String failedStatus = groupProperties.getString(Constants.FAILED_STATUS);

		String invalidFileStatus = groupProperties.getString(Constants.INVALID_STATUS);

		String inprocessStatus = groupProperties.getString(Constants.FILE_SMS_INPROCESS_STATUS);

		String completedStatus = groupProperties.getString(Constants.COMPLETED_STATUS);
		
		String reason = groupProperties.getString("failure.reason");

		int fileNooftimesProcess = Integer
				.parseInt(configMap.get(com.winnovature.utils.utils.Constants.MAX_RETRY_COUNT));

		StringBuilder sql = new StringBuilder();
		sql.append(" select id, status, total, retry_count from cm.group_files where g_id = ? ");

		if (log.isDebugEnabled()) {
			log.debug(className + methodName + "sql = " + sql.toString());
		}
		List<Map<String, String>> groupFilesList = new ArrayList<Map<String, String>>();
		try {
			con = ConnectionFactoryForCMDB.getInstance().getConnection();
			pstmt = con.prepareStatement(sql.toString());
			pstmt.setString(1, masterId);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				Map<String, String> row = new HashMap<String, String>();
				row.put("status", rs.getString("status").toLowerCase());
				row.put("retry_count", rs.getString("retry_count"));
				row.put("total", rs.getString("total"));
				groupFilesList.add(row);
			}

			boolean completed = true, partial = false;
			int completedCount = 0;
			long groupTotal = 0;
			for (Map<String, String> group : groupFilesList) {
				String status = group.get("status");
				String retry = group.get("retry_count") == null ? "0" : group.get("retry_count");
				int retryCount = Integer.parseInt(retry);
				String total = group.get("total") == null ? "0" : group.get("total");
				groupTotal += Long.parseLong(total);
				
				if (status.equalsIgnoreCase(completedStatus)) {
					completedCount++;
				}else {
					// inprocess, failed, invalidfile
					if (status.equalsIgnoreCase(inprocessStatus)) {
						completed = false;
					} else if (status.equalsIgnoreCase(failedStatus) && retryCount < fileNooftimesProcess) {
						completed = false;
					}

					if (status.equalsIgnoreCase(failedStatus) && retryCount >= fileNooftimesProcess) {
						partial = true;
					} else if (status.equalsIgnoreCase(invalidFileStatus)) {
						partial = true;
					}
				}
				if (!completed) {
					break;
				}
			}

			if (completed) {
				result.put("status", failedStatus);
				if(completedCount > 0) {
					result.put("status", completedStatus);
				}
				if(completedCount < groupFilesList.size()) {
					result.put("reason", reason);
				}
			}
			if (partial) {
				result.put("full_completion", "no");
			}
			result.put("total", groupTotal+"");
		} catch (Exception e) {
			log.error(className + methodName + " Exception:", e);
			throw e;
		} finally {
			closeConnection(rs, pstmt, con);
		}
		if (log.isDebugEnabled()) {
			log.debug(className + methodName + "end ...");
		}
		return result;
	}

	public boolean updateCampaignMasterCompleted(String id, Map<String, String> campaignFilesData) throws Exception {
		String methodName = "[updateCampaignMasterCompleted]";

		boolean updateStatus = false;
		String status = campaignFilesData.get("status") == null ? "" : campaignFilesData.get("status");
		if (status.equalsIgnoreCase("inprocess")) {
			return false;
		}

		Connection con = null;
		PreparedStatement pstmt = null;

		String failedStatus = groupProperties.getString(Constants.FAILED_STATUS);

		StringBuilder sql = new StringBuilder();
		sql.append(" update group_master set status = ?, full_completion_yn = ? ");
		if (failedStatus.equalsIgnoreCase(status)) {
			sql.append(", reason = ? ");
		}
		sql.append(" where id = ? ");

		if (log.isDebugEnabled())
			log.debug(className + methodName + "sql:" + sql.toString());

		try {

			int count = 0;
			con = ConnectionFactoryForCMDB.getInstance().getConnection();
			pstmt = con.prepareStatement(sql.toString());

			// always update group_master status as completed (nodejs will fetch only completed groups for group campaign)
			//pstmt.setString(1, status.toLowerCase());
			pstmt.setString(1, groupProperties.getString(Constants.COMPLETED_STATUS));
			if (campaignFilesData.get("full_completion").equalsIgnoreCase("yes")) {
				pstmt.setInt(2, 1);
			} else {
				pstmt.setInt(2, 0);
			}
			if (failedStatus.equalsIgnoreCase(status)) {
				pstmt.setString(3, campaignFilesData.get("reason"));
				pstmt.setString(4, id);
			} else {
				pstmt.setString(3, id);
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
		try {
			if (rs != null) {
				rs.close();
			}
			if (ps != null) {
				ps.close();
			}
			if (con != null) {
				con.close();
			}
		} catch (Exception e) {
			log.error(className + " [closeConnection] ", e);
		}
	}

}
