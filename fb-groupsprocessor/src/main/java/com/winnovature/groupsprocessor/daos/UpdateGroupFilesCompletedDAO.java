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

public class UpdateGroupFilesCompletedDAO {
	static Log log = LogFactory.getLog(Constants.GroupsProcessorLogger);
	private static final String className = "[UpdateGroupFilesCompletedDAO]";
	
	static Map<String, String> configMap = null;
	PropertiesConfiguration groupProperties = null;

	public UpdateGroupFilesCompletedDAO() throws Exception {
		if (configMap == null) {
			configMap = ConfigParamsTon.getInstance().getConfigurationFromconfigParams();
		}
		this.groupProperties = GroupsProcessorPropertiesTon.getInstance().getPropertiesConfiguration();
	}

	public List<Map<String, String>> getInprocessRecords() throws Exception {

		String methodName = " [getInprocessRecords] ";

		List<Map<String, String>> campaignFilesList = new ArrayList<Map<String, String>>();
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		String inprocessStatus = groupProperties.getString(Constants.FILE_SMS_INPROCESS_STATUS);

		StringBuilder sql = new StringBuilder();
		sql.append(" select id, status from group_files where lower(status)=?");

		if (log.isDebugEnabled()) {
			log.debug(className + methodName + "sql = " + sql.toString());
		}

		try {
			con = ConnectionFactoryForCMDB.getInstance().getConnection();
			pstmt = con.prepareStatement(sql.toString());

			pstmt.setString(1, inprocessStatus.toLowerCase());

			rs = pstmt.executeQuery();

			while (rs.next()) {
				Map<String, String> campaignFiles = new HashMap<String, String>();
				campaignFiles.put("id", rs.getString("id"));
				campaignFiles.put("status", rs.getString("status"));
				campaignFilesList.add(campaignFiles);
			}

			if (log.isDebugEnabled()) {
				log.debug(className + methodName + "group_files inprocess list : " + campaignFilesList);
			}
		} catch (Exception e) {
			log.error(className + methodName + " Exception:", e);
			throw e;
		} finally {
			closeConnection(rs, pstmt, con);
		}
		return campaignFilesList;
	}

	public Map<String, String> getGroupFileSplits(String masterId) throws Exception {

		String methodName = " [getGroupFileSplits] ";
		if (log.isDebugEnabled()) {
			log.debug(className + methodName + "begin ..");
		}

		Map<String, String> masterCount = new HashMap<String, String>();
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		String inprocessStatus = groupProperties.getString(Constants.FILE_SMS_INPROCESS_STATUS);

		String completeStatus = groupProperties.getString(Constants.COMPLETED_STATUS);

		String failedStatus = groupProperties.getString(Constants.FAILED_STATUS);

		String invalidFileStatus = groupProperties.getString(Constants.INVALID_STATUS);

		int fileNooftimesProcess = Integer
				.parseInt(configMap.get(com.winnovature.utils.utils.Constants.MAX_RETRY_COUNT));

		StringBuilder sql = new StringBuilder();
		sql.append(
				" select status, sum(total) as TOTAL, sum(valid) as VALID, Sum(invalid) as INVALID, Sum(duplicate) as DUPLICATE, ");
		sql.append(" retry_count from group_file_splits ");
		sql.append(" where g_f_id = ? group By status, total, valid, invalid, retry_count");

		if (log.isDebugEnabled()) {
			log.debug(className + methodName + "sql = " + sql.toString());
		}

		try {

			con = ConnectionFactoryForCMDB.getInstance().getConnection();
			pstmt = con.prepareStatement(sql.toString());

			pstmt.setString(1, masterId);

			rs = pstmt.executeQuery();

			int totalMasterCount = 0;
			int validMasterCount = 0;
			int invalidMasterCount = 0, duplicateMasterCount = 0;
			boolean check = true;

			while (rs.next()) {
				String status = rs.getString("status");

				if (inprocessStatus.equalsIgnoreCase(status)) {
					masterCount.put("status", "");
					check = false;
					continue;
				}

				int total = rs.getString("TOTAL") == null ? 0 : Integer.parseInt(rs.getString("TOTAL"));
				int valid = rs.getString("VALID") == null ? 0 : Integer.parseInt(rs.getString("VALID"));
				int invalid = rs.getString("INVALID") == null ? 0 : Integer.parseInt(rs.getString("INVALID"));
				int duplicate = rs.getString("DUPLICATE") == null ? 0 : Integer.parseInt(rs.getString("DUPLICATE"));

				int fileNooftimesProcessDB = rs.getString("retry_count") == null ? 0
						: Integer.parseInt(rs.getString("retry_count"));

				totalMasterCount = totalMasterCount + total;
				validMasterCount = validMasterCount + valid;
				invalidMasterCount = invalidMasterCount + invalid;
				duplicateMasterCount = duplicateMasterCount + duplicate;

				if (log.isDebugEnabled()) {
					log.debug(className + methodName + " total:" + total);
					log.debug(className + methodName + " valid:" + valid);
					log.debug(className + methodName + " duplicate:" + duplicate);
					log.debug(className + methodName + " invalid:" + invalid);
				}

				masterCount.put("total", "" + totalMasterCount);
				masterCount.put("valid", "" + validMasterCount);
				masterCount.put("invalid", "" + invalidMasterCount);
				masterCount.put("retry_count", "" + fileNooftimesProcessDB);
				masterCount.put("duplicate", "" + duplicateMasterCount);

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
	
	public boolean updateGroupFilesRecordCompleted(String id, Map<String, String> deliveryRecords) throws Exception {
		String methodName = "[updateGroupFilesRecordCompleted]";

		boolean updateStatus = false;
		String status = deliveryRecords.get("status") == null ? "" : deliveryRecords.get("status");
		// if status is empty which means no need to update the table, let it keep the
		// value as inprocess.
		if (status.isEmpty()) {
			return false;
		}
		String total = deliveryRecords.get("total");
		String valid = deliveryRecords.get("valid");
		String invalid = deliveryRecords.get("invalid");
		String duplicate = deliveryRecords.get("duplicate");

		Connection con = null;
		PreparedStatement pstmt = null;

		String failedStatus = groupProperties.getString(Constants.FAILED_STATUS);

		StringBuilder sql = new StringBuilder();
		sql.append(" update group_files set status = ?, completed_ts = now(),");
		if (failedStatus.equalsIgnoreCase(status)) {
			sql.append(" retry_count = IFNULL(retry_count,0)+1,");
		}
		sql.append(" total = ?, valid = ?, invalid = ?, duplicate=? where id = ? ");

		if (log.isDebugEnabled())
			log.debug(className + methodName + "sql:" + sql.toString());

		try {
			int count = 0;
			con = ConnectionFactoryForCMDB.getInstance().getConnection();
			pstmt = con.prepareStatement(sql.toString());

			pstmt.setString(1, status.toLowerCase());
			pstmt.setString(2, total);
			pstmt.setString(3, valid);
			pstmt.setString(4, invalid);
			pstmt.setString(5, duplicate);
			pstmt.setString(6, id);

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
