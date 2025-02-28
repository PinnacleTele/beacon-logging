package com.winnovature.dltfileprocessor.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.winnovature.dltfileprocessor.singletons.DltFileProcessorPropertiesTon;
import com.winnovature.dltfileprocessor.utils.Constants;
import com.winnovature.dltfileprocessor.utils.FileSender;
import com.winnovature.utils.singletons.ConnectionFactoryForAccountsDB;
import com.winnovature.utils.singletons.ConnectionFactoryForCMDB;

public class DltTemplateRequestDAO {

	static Log log = LogFactory.getLog(Constants.FileUploadLogger);
	private static final String className = "[DltTemplateRequestDAO]";
	PropertiesConfiguration prop = null;
	
	public DltTemplateRequestDAO(){
	}

	public void pollDltTemplateRequest(String queueName) throws Exception {

		String methodName = " [pollDltTemplateRequest] ";
		PropertiesConfiguration prop = DltFileProcessorPropertiesTon
				.getInstance().getPropertiesConfiguration();
		String status = prop.getString(Constants.FILES_ALL_STATUS);
		long sleepTime = com.winnovature.utils.utils.Utility.getConsumersSleepTime();
		String instanceId = prop.getString(Constants.MONITORING_INSTANCE_ID);

		String sql = "select dtr.id as dtr_id, dtr.cli_id, dtr.created_by, dtr.username, dtr.entity_id, dtr.template_group_id, dtr.telco, dtr.created_ts, "
				+ " dtf.id as dtf_id, dtf.filename_ori, dtf.fileloc, dtf.total, dtf.reason "
				+ " from dlt_template_request dtr, dlt_template_files dtf where dtr.id = dtf.d_id and lower(dtr.status) in ("+ status +") and lower(dtf.status) in ("+ status +") "
				+ " ORDER BY dtr.created_by,dtf.total ";

		if (log.isDebugEnabled())
			log.debug(className + methodName + sql);

		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		Map<String, List<Map<String, String>>> requestsList = new HashMap<String, List<Map<String, String>>>();

		try {
			con = ConnectionFactoryForCMDB.getInstance().getConnection();

			pstmt = con.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			boolean foundData = false;

			while (rs.next()) {
				foundData = true;
				Map<String, String> dltTemplateRequest = new HashMap<String, String>();
				String dtr_id = rs.getString("dtr_id");
				if (log.isDebugEnabled()) {
					log.debug(className
							+ methodName
							+ " Request found in dlt_template_files id:"
							+ rs.getString("dtf_id"));
				}
				dltTemplateRequest.put("dtr_id", dtr_id);
				dltTemplateRequest.put("dtf_id", rs.getString("dtf_id"));
				dltTemplateRequest.put("cli_id", rs.getString("cli_id"));
				dltTemplateRequest.put("filename_ori", rs.getString("filename_ori"));
				dltTemplateRequest.put("fileloc", rs.getString("fileloc"));
				dltTemplateRequest.put("username", rs.getString("username"));
				dltTemplateRequest.put("created_ts", rs.getString("created_ts"));
				dltTemplateRequest.put("entity_id", rs.getString("entity_id"));
				dltTemplateRequest.put("template_group_id", rs.getString("template_group_id"));
				dltTemplateRequest.put("total", rs.getString("total"));
				dltTemplateRequest.put("reason", rs.getString("reason"));
				dltTemplateRequest.put("telco", rs.getString("telco"));
				dltTemplateRequest.put("created_by", rs.getString("created_by"));
				
				String key = rs.getString("cli_id") + "~" + rs.getString("dtr_id");
				if(requestsList.containsKey(key)) {
					List<Map<String, String>> data = requestsList.get(key);
					data.add(dltTemplateRequest);
					data.sort(new CountSorter());
				}else {
					List<Map<String, String>> data = new ArrayList<Map<String, String>>();
					data.add(dltTemplateRequest);
					requestsList.put(key, data);
				}
			} // end of result set iteration
			
			// No data found let consumer rest for some time
			if(!foundData) {
				if (log.isDebugEnabled())
					log.debug(className + methodName + " No request found with matching criteria, sleeping for "+sleepTime+" milli seconds.");
				consumerSleep(sleepTime);
			}
			
			while(requestsList.size() > 0) {
				Iterator<String> it = requestsList.keySet().iterator();
				while (it.hasNext()) {
					String key = (String) it.next();
					List<Map<String, String>> dltFiles = requestsList.get(key);
					if (dltFiles.size() > 0) {
						Map<String, String> dltFileInfo = dltFiles.remove(0);
						try {
							handoverToRedis(dltFileInfo, queueName, dltFileInfo.get("dtr_id"),
									"DltFileQ HO Failed",instanceId);
						}catch(Exception e) {
							updateDltFilesStatus(dltFileInfo, Constants.PROCESS_STATUS_FAILED, instanceId, "DltFileQ HO Failed");
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
		updateDltFilesStatus(campInfo, Constants.PROCESS_STATUS_INPROGRESS,instanceId,null);
		// HO to DltFileQ
		boolean fileSenderStatus = FileSender.sendToFileQueue(campInfo, queueName);
		// if HO to redis failed update status=FAILED
		if (!fileSenderStatus) {
			updateDltFilesStatus(campInfo, Constants.PROCESS_STATUS_FAILED, instanceId, reason);
		}
	}
	
	public void updateDltFilesStatus(Map<String, String> campInfo, String status, String instanceId, String reason)
			throws Exception {

		String methodName = "[updateDltFilesStatus]";
		
		StringBuilder dlt_request_sql = new StringBuilder("update dlt_template_request SET status = ? ");
		if(status.equalsIgnoreCase(Constants.PROCESS_STATUS_FAILED)) {
			dlt_request_sql.append(", reason=? ");
		}
		dlt_request_sql.append(" where id=? and cli_id=?");
		
		StringBuilder dlt_files_sql = new StringBuilder("update dlt_template_files SET status = ?, instance_id=? ");
		if(status.equalsIgnoreCase(Constants.PROCESS_STATUS_FAILED)) {
			dlt_files_sql.append(", reason=? ");
		}else {
			dlt_files_sql.append(", started_ts=now() ");
		}
		dlt_files_sql.append(" where id=? and d_id=?");
		
		if (log.isDebugEnabled()) {
			log.debug(className + methodName + "dlt_template_request update sql - " + dlt_request_sql);
			log.debug(className + methodName + "dlt_template_files update sql - " + dlt_files_sql);
		}

		Connection con = null;
		PreparedStatement dltRequestPstmt = null;
		PreparedStatement dltFilesPstmt = null;
		
		int dltRequestUpdateCount = 0, dltFilesUpdateCount = 0;
		
		try {
			con = ConnectionFactoryForCMDB.getInstance().getConnection();
			con.setAutoCommit(false);
			dltRequestPstmt = con.prepareStatement(dlt_request_sql.toString());
			if(status.equalsIgnoreCase(Constants.PROCESS_STATUS_FAILED)) {
				dltRequestPstmt.setString(1, status);
				dltRequestPstmt.setString(2, reason);
				dltRequestPstmt.setString(3, campInfo.get("dtr_id"));
				dltRequestPstmt.setString(4, campInfo.get("cli_id"));
			}else {
				dltRequestPstmt.setString(1, status);
				dltRequestPstmt.setString(2, campInfo.get("dtr_id"));
				dltRequestPstmt.setString(3, campInfo.get("cli_id"));
			}
			
			dltRequestUpdateCount = dltRequestPstmt.executeUpdate();
			if(dltRequestUpdateCount > 0) {
				dltFilesPstmt = con.prepareStatement(dlt_files_sql.toString());
				if(status.equalsIgnoreCase(Constants.PROCESS_STATUS_FAILED)) {
					dltFilesPstmt.setString(1, status);
					dltFilesPstmt.setString(2, instanceId);
					dltFilesPstmt.setString(3, reason);
					dltFilesPstmt.setString(4, campInfo.get("dtf_id"));
					dltFilesPstmt.setString(5, campInfo.get("dtr_id"));
				}else {
					dltFilesPstmt.setString(1, status);
					dltFilesPstmt.setString(2, instanceId);
					dltFilesPstmt.setString(3, campInfo.get("dtf_id"));
					dltFilesPstmt.setString(4, campInfo.get("dtr_id"));
				}
				
				dltFilesUpdateCount = dltFilesPstmt.executeUpdate();
				if(dltFilesUpdateCount > 0) {
					con.commit();
				}else {
					con.rollback();
				}
			}else {
				con.rollback();
			}
			
			if (log.isDebugEnabled()) {
				log.debug(className + methodName + " dlt_template_request update count : " + dltRequestUpdateCount+" dlt_template_files update count : "+dltFilesUpdateCount);
			}
		} catch (Exception sqlex) {
			log.error(className + methodName, sqlex);
			con.rollback();
			throw sqlex;
		} finally {
			closeConnection(null, dltRequestPstmt, con);
		}
	}

	public void reprocessFailedRows(Map<String, String> requestMap) {
		String methodName = "[reprocessFailedRows]";
		StringBuilder dltTemplateFilesInsertSQL = new StringBuilder("insert into cm.dlt_template_files (id,d_id,fileloc,total,status,created_ts,master_id) ");
		dltTemplateFilesInsertSQL.append(" values (?,?,?,?,?,now(),?)");
		
		Connection con = null;
		PreparedStatement pstmt = null;
		PreparedStatement pstmt1 = null;
		PreparedStatement pstmt2 = null;
		
		try {
			con = ConnectionFactoryForAccountsDB.getInstance().getConnection();
			con.setAutoCommit(false);
			pstmt = con.prepareStatement(dltTemplateFilesInsertSQL.toString());
			
			PropertiesConfiguration prop = DltFileProcessorPropertiesTon
					.getInstance().getPropertiesConfiguration();
			
			char[] alphabet = prop.getString("random.id.chars", "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789")
					.toCharArray();
			int nonoIdSize = prop.getInt("dlt.template.files.id.size", 36);
			String statusQueued = prop.getString("status.queued");
			String statusComplted = prop.getString("status.completed");
			
			String id = NanoIdUtils.randomNanoId(new Random(), alphabet, nonoIdSize);
			
			pstmt.setString(1, id);
			pstmt.setString(2, requestMap.get("dtr_id"));
			pstmt.setString(3, requestMap.get("failed_fileloc"));
			pstmt.setString(4, requestMap.get("failed"));
			pstmt.setString(5, statusQueued);
			pstmt.setString(6, requestMap.get("dtf_id"));
			
			if(pstmt.executeUpdate() > 0) {
				String dltTemplateRequestUpdateSql = "update cm.dlt_template_request set status = ? where id=?";
				pstmt1 = con.prepareStatement(dltTemplateRequestUpdateSql);
				pstmt1.setString(1, statusQueued);
				pstmt1.setString(2, requestMap.get("dtr_id"));
				if(pstmt1.executeUpdate() > 0) {
					String dltTemplateFilesUpdateSql = "update cm.dlt_template_files set valid=?, invalid=?, failed=?, status = ?, total=?, duplicate=?, completed_ts=now() where id=?";
					pstmt2 = con.prepareStatement(dltTemplateFilesUpdateSql);
					pstmt2.setString(1, requestMap.get("valid"));
					pstmt2.setString(2, requestMap.get("invalid"));
					pstmt2.setString(3, requestMap.get("failed"));
					pstmt2.setString(4, statusComplted);
					pstmt2.setString(5, requestMap.get("total"));
					pstmt2.setString(6, requestMap.get("duplicate"));
					pstmt2.setString(7, requestMap.get("dtf_id"));
					if(pstmt2.executeUpdate() > 0) {
						con.commit();
					}else {
						con.rollback();
						throw new Exception("Could not able to update dlt_template_files completed status. Request object : "+requestMap);
					}
				}else {
					con.rollback();
					throw new Exception("Could not able to update dlt_template_request queued status to reprocess failed rows. Request object : "+requestMap);
				}
			}else {
				con.rollback();
				throw new Exception("Could not able to insert into dlt_template_files to reprocess failed rows. Request object : "+requestMap);
			}
		}catch(Exception e) {
			log.error(className+methodName, e);
		}finally {
			try {
				closeConnection(null, pstmt2, null);
				closeConnection(null, pstmt1, null);
				closeConnection(null, pstmt, con);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public List<Map<String, String>> getInprocessRecords() throws Exception {

		String methodName = " [getInprocessRecords] ";

		List<Map<String, String>> dltRequestList = new ArrayList<Map<String, String>>();
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		PropertiesConfiguration prop = DltFileProcessorPropertiesTon
				.getInstance().getPropertiesConfiguration();

		String inprocessStatus = prop.getString("status.inprocess");

		StringBuilder sql = new StringBuilder();
		sql.append(" select id, status from dlt_template_request where lower(status)= ?");

		if (log.isDebugEnabled()) {
			log.debug(className + methodName + "sql = " + sql.toString());
		}

		try {
			con = ConnectionFactoryForCMDB.getInstance().getConnection();
			pstmt = con.prepareStatement(sql.toString());

			pstmt.setString(1, inprocessStatus.toLowerCase());

			rs = pstmt.executeQuery();

			while (rs.next()) {
				Map<String, String> dltReq = new HashMap<String, String>();
				dltReq.put("id", rs.getString("id"));
				dltReq.put("status", rs.getString("status"));
				dltRequestList.add(dltReq);
			}

			if (log.isDebugEnabled()) {
				log.debug(className + methodName + " dlt_template_request inprocess list : " + dltRequestList);
			}
		} catch (Exception e) {
			log.error(className + methodName + " Exception:", e);
			throw e;
		} finally {
			closeConnection(rs, pstmt, con);
		}
		return dltRequestList;
	}
	
	public Map<String, String> getDltTemplateFiles(String masterId) throws Exception {

		String methodName = " [getDltTemplateFiles] ";

		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Map<String, String> result = new HashMap<String, String>();
		result.put("status", "inprocess");
		
		PropertiesConfiguration prop = DltFileProcessorPropertiesTon
				.getInstance().getPropertiesConfiguration();

		String failedStatus = prop.getString("status.failed");

		String completedStatus = prop.getString("status.completed");
		
		String reason = prop.getString("failure.reason");

		StringBuilder sql = new StringBuilder();
		sql.append(" select id, status, total from cm.dlt_template_files where d_id = ? ");

		if (log.isDebugEnabled()) {
			log.debug(className + methodName + "sql = " + sql.toString());
		}
		List<Map<String, String>> dltFilesList = new ArrayList<Map<String, String>>();
		try {
			con = ConnectionFactoryForCMDB.getInstance().getConnection();
			pstmt = con.prepareStatement(sql.toString());
			pstmt.setString(1, masterId);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				Map<String, String> row = new HashMap<String, String>();
				row.put("status", rs.getString("status").toLowerCase());
				row.put("total", rs.getString("total"));
				dltFilesList.add(row);
			}

			boolean completed = true, partial = false;
			int completedCount = 0;
			long groupTotal = 0;
			for (Map<String, String> dltFile : dltFilesList) {
				String status = dltFile.get("status");
				String total = dltFile.get("total") == null ? "0" : dltFile.get("total");
				groupTotal += Long.parseLong(total);
				
				if (status.equalsIgnoreCase(completedStatus) || status.equalsIgnoreCase(failedStatus)) {
					completedCount++;
				}else {
					completed = false;
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
				if(completedCount < dltFilesList.size()) {
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
	
	public boolean updateDltTemplateRequestCompleted(String id, Map<String, String> campaignFilesData) throws Exception {
		String methodName = "[updateDltTemplateRequestCompleted]";
		
		PropertiesConfiguration prop = DltFileProcessorPropertiesTon
				.getInstance().getPropertiesConfiguration();

		boolean updateStatus = false;
		String status = campaignFilesData.get("status") == null ? "" : campaignFilesData.get("status");
		if (status.equalsIgnoreCase("inprocess")) {
			return false;
		}

		Connection con = null;
		PreparedStatement pstmt = null;

		String failedStatus = prop.getString("status.failed");

		StringBuilder sql = new StringBuilder();
		sql.append(" update dlt_template_request set status = ? ");
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

			pstmt.setString(1, status.toLowerCase());
			if (failedStatus.equalsIgnoreCase(status)) {
				pstmt.setString(2, campaignFilesData.get("reason"));
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

	public void updateDltFilesStatus(Map<String, String> campInfo)
			throws Exception {

		String methodName = "[updateDltFilesStatus]";
		
		String status = campInfo.get("status");
		String instanceId = campInfo.get("instance_id");
		String reason = campInfo.get("reason");
		String total = campInfo.get("total");
		String valid = campInfo.get("valid");
		String invalid = campInfo.get("invalid");
		String failed = campInfo.get("failed");
		String duplicate = campInfo.get("duplicate");
		
		StringBuilder dlt_files_sql = new StringBuilder("update dlt_template_files SET status = ?, instance_id=? ");
		if(status.equalsIgnoreCase(Constants.PROCESS_STATUS_FAILED)) {
			dlt_files_sql.append(", reason=? ");
		}else {
			dlt_files_sql.append(", completed_ts=now() ");
		}
		
		if(StringUtils.isNotBlank(total)) {
			dlt_files_sql.append(", total="+total);
		}
		
		if(StringUtils.isNotBlank(valid)) {
			dlt_files_sql.append(", valid="+valid);
		}
		
		if(StringUtils.isNotBlank(invalid)) {
			dlt_files_sql.append(", invalid="+invalid);
		}
		
		if(StringUtils.isNotBlank(failed)) {
			dlt_files_sql.append(", failed="+failed);
		}
		
		if(StringUtils.isNotBlank(failed)) {
			dlt_files_sql.append(", duplicate="+duplicate);
		}
		
		dlt_files_sql.append(" where id=?");
		
		if (log.isDebugEnabled()) {
			log.debug(className + methodName + "dlt_template_files update sql - " + dlt_files_sql);
		}

		Connection con = null;
		PreparedStatement dltFilesPstmt = null;
		
		try {
			con = ConnectionFactoryForCMDB.getInstance().getConnection();
			
			dltFilesPstmt = con.prepareStatement(dlt_files_sql.toString());
			if(status.equalsIgnoreCase(Constants.PROCESS_STATUS_FAILED)) {
				dltFilesPstmt.setString(1, status);
				dltFilesPstmt.setString(2, instanceId);
				dltFilesPstmt.setString(3, reason);
				dltFilesPstmt.setString(4, campInfo.get("dtf_id"));
			}else {
				dltFilesPstmt.setString(1, status);
				dltFilesPstmt.setString(2, instanceId);
				dltFilesPstmt.setString(3, campInfo.get("dtf_id"));
			}
			
			dltFilesPstmt.executeUpdate();
			
		} catch (Exception sqlex) {
			log.error(className + methodName, sqlex);
			con.rollback();
			throw sqlex;
		} finally {
			closeConnection(null, dltFilesPstmt, con);
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
