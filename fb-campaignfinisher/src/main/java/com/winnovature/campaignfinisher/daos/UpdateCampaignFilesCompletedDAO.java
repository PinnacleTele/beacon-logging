package com.winnovature.campaignfinisher.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.campaignfinisher.singletons.CampaignFinisherPropertiesTon;
import com.winnovature.campaignfinisher.utils.Constants;
import com.winnovature.utils.singletons.ConfigParamsTon;
import com.winnovature.utils.singletons.ConnectionFactoryForCMDB;

public class UpdateCampaignFilesCompletedDAO {
	static Log log = LogFactory.getLog(Constants.CampaignFinisherLogger);

	private static final String className = "[UpdateCampaignFilesCompletedDAO]";
	private static String methodName = null;

	static Map<String, String> configMap = null;

	public UpdateCampaignFilesCompletedDAO() throws Exception {
		if (configMap == null) {
			configMap = ConfigParamsTon.getInstance().getConfigurationFromconfigParams();
		}
	}

	public List<Map<String, String>> getInprocessRecords() throws Exception {

		String methodName = " [getInprocessRecords] ";

		List<Map<String, String>> campaignFilesList = new ArrayList<Map<String, String>>();
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		String inprocessStatus = CampaignFinisherPropertiesTon.getInstance().getPropertiesConfiguration()
				.getString(Constants.FILE_SMS_INPROCESS_STATUS);

		StringBuilder sql = new StringBuilder();
		sql.append(" select id, status from campaign_files where lower(status)=?");

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
				log.debug(className + methodName + "campaign_files inprocess list : " + campaignFilesList);
			}
		} catch (Exception e) {
			log.error(className + methodName + " Exception:", e);
			throw e;
		} finally {
			closeConnection(rs, pstmt, con);
		}
		return campaignFilesList;
	}

	public Map<String, String> getCampaignFileSplits(String masterId) throws Exception {

		methodName = "[getCampaignFileSplits]";
		if (log.isDebugEnabled()) {
			log.debug(className + methodName + "begin ..");
		}

		Map<String, String> masterCount = new HashMap<String, String>();
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		boolean anySplitFileFailed = false;

		String inprocessStatus = CampaignFinisherPropertiesTon.getInstance().getPropertiesConfiguration()
				.getString(Constants.FILE_SMS_INPROCESS_STATUS);

		String completeStatus = CampaignFinisherPropertiesTon.getInstance().getPropertiesConfiguration()
				.getString(Constants.FILE_SMS_COMPLETED_STATUS);

		String failedStatus = CampaignFinisherPropertiesTon.getInstance().getPropertiesConfiguration()
				.getString(Constants.FILE_SMS_FAILED_STATUS);

		String invalidFileStatus = CampaignFinisherPropertiesTon.getInstance().getPropertiesConfiguration()
				.getString(Constants.FILE_SMS_INVALID_FILE_STATUS);

		/*
		if (log.isDebugEnabled()) {
			log.debug(className + methodName + " inprocess status : " + inprocessStatus);
			log.debug(className + methodName + " complete status : " + completeStatus);
			log.debug(className + methodName + " failed status : " + failedStatus);
		}
		*/

		int fileNooftimesProcess = Integer
				.parseInt(configMap.get(com.winnovature.utils.utils.Constants.MAX_RETRY_COUNT));

		StringBuilder sql = new StringBuilder();
		sql.append(
				" select status, sum(total) as TOTAL, sum(valid) as VALID, Sum(invalid) as INVALID, sum(excluded) as EXCLUDE, ");
		sql.append(" retry_count from campaign_file_splits ");
		sql.append(" where c_f_id = ? group By status, total, valid, invalid, retry_count");

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
			int invalidMasterCount = 0;
			int excludeMasterCount = 0;
			boolean anyPendingRequests = false;

			while (rs.next()) {
				String status = rs.getString("status");

				if (inprocessStatus.equalsIgnoreCase(status)) {
					masterCount.put("status", "");
					anyPendingRequests = true;
					//continue;
					break;
				}

				int total = rs.getString("TOTAL") == null ? 0 : Integer.parseInt(rs.getString("TOTAL"));
				int valid = rs.getString("VALID") == null ? 0 : Integer.parseInt(rs.getString("VALID"));
				int invalid = rs.getString("INVALID") == null ? 0 : Integer.parseInt(rs.getString("INVALID"));
				int exclude = rs.getString("EXCLUDE") == null ? 0 : Integer.parseInt(rs.getString("EXCLUDE"));

				int fileNooftimesProcessDB = rs.getString("retry_count") == null ? 0
						: Integer.parseInt(rs.getString("retry_count"));

				totalMasterCount = totalMasterCount + total;
				validMasterCount = validMasterCount + valid;
				invalidMasterCount = invalidMasterCount + invalid;
				excludeMasterCount = excludeMasterCount + exclude;

				if (log.isDebugEnabled()) {
					log.debug(className + methodName + " total:" + total);
					log.debug(className + methodName + " valid:" + valid);
					log.debug(className + methodName + " invalid:" + invalid);
					log.debug(className + methodName + " exclude:" + exclude);
				}

				masterCount.put("total", "" + totalMasterCount);
				masterCount.put("valid", "" + validMasterCount);
				masterCount.put("invalid", "" + invalidMasterCount);
				masterCount.put("exclude", "" + excludeMasterCount);
				masterCount.put("retry_count", "" + fileNooftimesProcessDB);

				if (completeStatus.equalsIgnoreCase(status)) {
					masterCount.put("status", completeStatus);
					continue;
				}

				if (failedStatus.equalsIgnoreCase(status) && fileNooftimesProcessDB >= fileNooftimesProcess) {
					masterCount.put("status", failedStatus);
					anySplitFileFailed = true;
				} else if(failedStatus.equalsIgnoreCase(status) && fileNooftimesProcessDB < fileNooftimesProcess) {
					masterCount.put("status", "");
					anyPendingRequests = true;
					break;
				}

				// Added to handle INVALID FILE status
				if (invalidFileStatus.equalsIgnoreCase(status)) {
					masterCount.put("status", invalidFileStatus);
				}
			}
			// if any split file failed then update campaign_files as failed (for retry from ui)
			if(!anyPendingRequests && anySplitFileFailed) {
				masterCount.put("status", failedStatus);
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

	public boolean updateMasterRecordCompleted(String id, Map<String, String> deliveryRecords) throws Exception {
		String methodName = "[updateMasterRecordCompleted]";

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
		String exclude = deliveryRecords.get("exclude");
		String retry_count = deliveryRecords.get("retry_count");

		Connection con = null;
		PreparedStatement pstmt = null;

		String failedStatus = CampaignFinisherPropertiesTon.getInstance().getPropertiesConfiguration()
				.getString(Constants.FILE_SMS_FAILED_STATUS);

		StringBuilder sql = new StringBuilder();
		sql.append(" update campaign_files set status = ?, completed_ts = now(),");
		if (failedStatus.equalsIgnoreCase(status)) {
			sql.append(" retry_count = IFNULL(retry_count,0)+"+retry_count+",");
		}
		sql.append(" total = ?, valid = ?, invalid = ?, excluded=? where id = ? ");

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
			pstmt.setString(5, exclude);
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
