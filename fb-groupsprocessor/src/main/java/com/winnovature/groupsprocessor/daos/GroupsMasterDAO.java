package com.winnovature.groupsprocessor.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.groupsprocessor.singletons.GroupsProcessorPropertiesTon;
import com.winnovature.groupsprocessor.utils.Constants;
import com.winnovature.groupsprocessor.utils.FileSender;
import com.winnovature.utils.dtos.SplitFileData;
import com.winnovature.utils.singletons.ConnectionFactoryForCMDB;

public class GroupsMasterDAO {

	static Log log = LogFactory.getLog(Constants.GroupsProcessorLogger);
	private static final String className = "[GroupsMasterDAO]";
	PropertiesConfiguration prop = null;

	public GroupsMasterDAO() {
	}

	public void pollGroups(String maxRetryCount, String groupQueueName) throws Exception {

		String methodName = " [pollGroups] ";
		PropertiesConfiguration prop = GroupsProcessorPropertiesTon.getInstance().getPropertiesConfiguration();
		String status = prop.getString(Constants.FILE_SMS_ALL_STATUS);
		long sleepTime = com.winnovature.utils.utils.Utility.getConsumersSleepTime();
		String instanceId = prop.getString(Constants.MONITORING_INSTANCE_ID);

		String sql = "select gm.id as gm_id, gm.cli_id, gm.g_name, gm.g_visibility, gm.g_type, gm.created_ts, gf.id as gf_id, gf.filename_ori, gf.fileloc, gf.retry_count, gf.total "
				+ " from cm.group_master gm, cm.group_files gf where gm.id = gf.g_id and lower(gm.status) in (" + status
				+ ") and lower(gf.status) in (" + status + ") and "
				+ " IFNULL(gf.retry_count,0) <=? ORDER BY gm.cli_id,gf.total ";

		if (log.isDebugEnabled())
			log.debug(className + methodName + sql);

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
				Map<String, String> groupMasterMap = new HashMap<String, String>();
				String gm_id = rs.getString("gm_id");
				if (log.isDebugEnabled()) {
					log.debug(className + methodName + " Group request found in group_files. id :::" + rs.getString("gf_id"));
				}
				groupMasterMap.put("gm_id", gm_id);
				groupMasterMap.put("gf_id", rs.getString("gf_id"));
				groupMasterMap.put("cli_id", rs.getString("cli_id"));
				groupMasterMap.put("g_name", rs.getString("g_name"));
				groupMasterMap.put("g_visibility", rs.getString("g_visibility"));
				groupMasterMap.put("g_type", rs.getString("g_type"));
				groupMasterMap.put("filename_ori", rs.getString("filename_ori"));
				groupMasterMap.put("fileloc", rs.getString("fileloc"));
				groupMasterMap.put("created_ts", rs.getString("created_ts"));
				groupMasterMap.put("retry_count", rs.getString("retry_count"));
				groupMasterMap.put("total", rs.getString("total"));

				String key = rs.getString("cli_id") + "~" + rs.getString("gm_id");
				if (requestsList.containsKey(key)) {
					List<Map<String, String>> data = requestsList.get(key);
					data.add(groupMasterMap);
					data.sort(new CountSorter());
				} else {
					List<Map<String, String>> data = new ArrayList<Map<String, String>>();
					data.add(groupMasterMap);
					requestsList.put(key, data);
				}
			} // end of result set iteration

			// No data found let consumer rest for some time
			if (!foundData) {
				if (log.isDebugEnabled())
					log.debug(className + methodName + " No request found with matching criteria, sleeping for "
							+ sleepTime + " milli seconds.");
				consumerSleep(sleepTime);
			}

			while (requestsList.size() > 0) {
				Iterator<String> it = requestsList.keySet().iterator();
				while (it.hasNext()) {
					String key = (String) it.next();
					List<Map<String, String>> groupFiles = requestsList.get(key);
					if (groupFiles.size() > 0) {
						Map<String, String> groupFileInfo = groupFiles.remove(0);
						try {
							handoverToRedis(groupFileInfo, groupQueueName, "GroupQ HO Failed", instanceId);
						} catch (Exception e) {
							updateGroupsRequestStatus(groupFileInfo, Constants.PROCESS_STATUS_FAILED, instanceId,
									"GroupQ HO Failed");
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

	public void handoverToRedis(Map<String, String> groupFileInfo, String queueName, String reason, String instanceId)
			throws Exception {
		// update master record status as inprocess
		updateGroupsRequestStatus(groupFileInfo, Constants.PROCESS_STATUS_INPROGRESS, instanceId, null);
		// HO to FileSplitQ
		boolean fileSenderStatus = FileSender.sendToFileQueue(groupFileInfo, queueName);
		// if HO to redis failed update status=FAILED
		if (!fileSenderStatus) {
			updateGroupsRequestStatus(groupFileInfo, Constants.PROCESS_STATUS_FAILED, instanceId, reason);
		}
	}

	public void updateGroupsRequestStatus(Map<String, String> campInfo, String status, String instanceId, String reason)
			throws Exception {

		String methodName = "[updateGroupsRequestStatus]";

		StringBuilder group_master_sql = new StringBuilder("update group_master SET status = ? ");
		if (status.equalsIgnoreCase(Constants.PROCESS_STATUS_FAILED)) {
			group_master_sql.append(", reason=? ");
		}
		group_master_sql.append(" where id=? and cli_id=?");

		StringBuilder group_files_sql = new StringBuilder("update group_files SET status = ?, instance_id=? ");
		if (status.equalsIgnoreCase(Constants.PROCESS_STATUS_FAILED)) {
			group_files_sql.append(", retry_count=IFNULL(retry_count,0)+1 ");
		} else {
			group_files_sql.append(", started_ts=now() ");
		}
		group_files_sql.append(" where id=? and g_id=?");

		if (log.isDebugEnabled()) {
			log.debug(className + methodName + "group_master update sql - " + group_master_sql);
			log.debug(className + methodName + "group_files update sql - " + group_files_sql);
		}

		Connection con = null;
		PreparedStatement groupMasterPstmt = null;
		PreparedStatement groupFilesPstmt = null;

		int groupMasterUpdateCount = 0, groupFilesUpdateCount = 0;

		try {
			con = ConnectionFactoryForCMDB.getInstance().getConnection();
			con.setAutoCommit(false);
			groupMasterPstmt = con.prepareStatement(group_master_sql.toString());
			if (status.equalsIgnoreCase(Constants.PROCESS_STATUS_FAILED)) {
				// never update group_master as failed
				groupMasterPstmt.setString(1, Constants.PROCESS_STATUS_COMPLETED);
				groupMasterPstmt.setString(2, reason);
				groupMasterPstmt.setString(3, campInfo.get("gm_id"));
				groupMasterPstmt.setString(4, campInfo.get("cli_id"));
			} else {
				groupMasterPstmt.setString(1, status);
				groupMasterPstmt.setString(2, campInfo.get("gm_id"));
				groupMasterPstmt.setString(3, campInfo.get("cli_id"));
			}

			groupMasterUpdateCount = groupMasterPstmt.executeUpdate();
			if (groupMasterUpdateCount > 0) {
				groupFilesPstmt = con.prepareStatement(group_files_sql.toString());
				if (status.equalsIgnoreCase(Constants.PROCESS_STATUS_FAILED)) {
					groupFilesPstmt.setString(1, status);
					groupFilesPstmt.setString(2, instanceId);
					groupFilesPstmt.setString(3, campInfo.get("gf_id"));
					groupFilesPstmt.setString(4, campInfo.get("gm_id"));
				} else {
					groupFilesPstmt.setString(1, status);
					groupFilesPstmt.setString(2, instanceId);
					groupFilesPstmt.setString(3, campInfo.get("gf_id"));
					groupFilesPstmt.setString(4, campInfo.get("gm_id"));
				}

				groupFilesUpdateCount = groupFilesPstmt.executeUpdate();
				if (groupFilesUpdateCount > 0) {
					con.commit();
				} else {
					con.rollback();
				}
			} else {
				con.rollback();
			}

			if (log.isDebugEnabled()) {
				log.debug(className + methodName + " group_master update count : " + groupMasterUpdateCount
						+ " group_files update count : " + groupFilesUpdateCount);
			}
		} catch (Exception ex) {
			log.error(className + methodName, ex);
			con.rollback();
			throw ex;
		} finally {
			closeConnection(null, groupMasterPstmt, con);
		}
	}

	public void updateCampaignStatus(String gm_id, String gf_id, String status, String reason, String retry_count) throws Exception {

		String methodName = "[updateCampaignStatus]";

		StringBuilder group_master_sql = new StringBuilder("update group_master SET status = ? ");
		if (status.equalsIgnoreCase(Constants.PROCESS_STATUS_FAILED)) {
			group_master_sql.append(", reason=? ");
		}
		group_master_sql.append(" where id=?");

		StringBuilder group_files_sql = new StringBuilder("update group_files SET status = ? ");
		if (status.equalsIgnoreCase(Constants.PROCESS_STATUS_FAILED)) {
			if(StringUtils.isNotBlank(retry_count)) {
				group_files_sql.append(", retry_count=IFNULL(retry_count,0)+").append(retry_count).append(", reason=? ");
			}else {
				group_files_sql.append(", retry_count=IFNULL(retry_count,0)+1, reason=? ");
			}
		}
		group_files_sql.append(" where id=?");

		if (log.isDebugEnabled()) {
			log.debug(className + methodName + "group_master update sql - " + group_master_sql);
			log.debug(className + methodName + "group_files update sql - " + group_files_sql);
		}

		Connection con = null;
		PreparedStatement groupMsaterPstmt = null;
		PreparedStatement groupFilesPstmt = null;

		int groupMasterUpdateCount = 0, groupFilesUpdateCount = 0;

		try {
			con = ConnectionFactoryForCMDB.getInstance().getConnection();
			con.setAutoCommit(false);
			groupMsaterPstmt = con.prepareStatement(group_master_sql.toString());
			// always update group_master as completed
			if (status.equalsIgnoreCase(Constants.PROCESS_STATUS_FAILED)) {
				groupMsaterPstmt.setString(1, Constants.PROCESS_STATUS_COMPLETED);
				groupMsaterPstmt.setString(2, reason);
				groupMsaterPstmt.setString(3, gm_id);
			} else {
				groupMsaterPstmt.setString(1, Constants.PROCESS_STATUS_COMPLETED);
				groupMsaterPstmt.setString(2, gm_id);
			}

			groupMasterUpdateCount = groupMsaterPstmt.executeUpdate();
			if (groupMasterUpdateCount > 0) {
				groupFilesPstmt = con.prepareStatement(group_files_sql.toString());
				if (status.equalsIgnoreCase(Constants.PROCESS_STATUS_FAILED)) {
					groupFilesPstmt.setString(1, status);
					groupFilesPstmt.setString(2, reason);
					groupFilesPstmt.setString(3, gf_id);
				} else {
					groupFilesPstmt.setString(1, status);
					groupFilesPstmt.setString(2, gf_id);
				}

				groupFilesUpdateCount = groupFilesPstmt.executeUpdate();
				if (groupFilesUpdateCount > 0) {
					con.commit();
				} else {
					con.rollback();
				}
			} else {
				con.rollback();
			}

			if (log.isDebugEnabled()) {
				log.debug(className + methodName + " group_master update count : " + groupMasterUpdateCount
						+ " group_files update count : " + groupFilesUpdateCount);
			}
		} catch (Exception ex) {
			log.error(className + methodName, ex);
			con.rollback();
			throw ex;
		} finally {
			closeConnection(null, groupMsaterPstmt, con);
		}
	}

	public void updateCampaignFilesStatusInvalidFile(String id, String reason, String total) throws Exception {

		String methodName = "[updateCampaignFilesStatusInvalidFile]";

		if (log.isDebugEnabled()) {
			log.debug(className + methodName + " id:" + id + " Reason:" + reason);
		}

		String instanceId = GroupsProcessorPropertiesTon.getInstance().getPropertiesConfiguration()
				.getString(Constants.MONITORING_INSTANCE_ID);

		String sql = "UPDATE group_files SET status = ?,reason=?, retry_count=IFNULL(retry_count,0) +1, total=?, instance_id=?  WHERE id = ? ";

		if (log.isDebugEnabled()) {
			log.debug(className + methodName + "sql" + sql);
		}

		Connection con = null;
		PreparedStatement pstmt = null;

		try {
			con = ConnectionFactoryForCMDB.getInstance().getConnection();
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, Constants.PROCESS_STATUS_INVALIDFILE);
			pstmt.setString(2, reason);
			pstmt.setString(3, total);
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
	
	public void insertSplitDetailsInGroupFileSplits(List<SplitFileData> filelocList, Map<String, String> reqMap)
			throws Exception {

		String methodName = " [insertSplitDetailsInGroupFileSplits] ";
		if (log.isDebugEnabled()) {
			log.debug(className + methodName + " begin ...");
		}

		Connection con = null;
		PreparedStatement pstmt = null;
		String sql = "INSERT INTO group_file_splits (id, g_id, g_f_id, filename, fileloc, instance_id, status, created_ts) values "
				+ "(?,?,?,?,?,?,?,now())";

		String status = GroupsProcessorPropertiesTon.getInstance().getPropertiesConfiguration()
				.getString(Constants.FILE_SMS_INPROCESS_STATUS);

		String instanceId = GroupsProcessorPropertiesTon.getInstance().getPropertiesConfiguration()
				.getString(Constants.MONITORING_INSTANCE_ID);

		if (log.isDebugEnabled()) {
			log.debug(className + methodName + " sql =" + sql);
		}

		try {
			con = ConnectionFactoryForCMDB.getInstance().getConnection();
			con.setAutoCommit(false);
			pstmt = con.prepareStatement(sql);

			for (SplitFileData split_data : filelocList) {
				int i = 1;
				String fileName = split_data.getFileName();

				final String uuid = UUID.randomUUID().toString().replace("-", "");
				split_data.setId(uuid);

				pstmt.setString(i++, uuid);
				pstmt.setString(i++, reqMap.get("gm_id"));
				pstmt.setString(i++, reqMap.get("gf_id"));
				pstmt.setString(i++, reqMap.get("filename"));
				pstmt.setString(i++, fileName);
				pstmt.setString(i++, instanceId);
				pstmt.setString(i++, status);

				pstmt.addBatch();
			}

			int result[] = pstmt.executeBatch();
			if (log.isDebugEnabled()) {
				log.debug(className + methodName + " inserted records  " + result.length);
			}
			con.commit();
		} catch (Exception e) {
			log.error(className + methodName + "Exception : ", e);
			if (con != null) {
				con.rollback();
			}
			throw e;
		} finally {
			closeConnection(null, pstmt, con);
		}
	}
	
	public List<SplitFileData> getSplitFiles(String masterId, String maxRetryCount) throws Exception {

		List<SplitFileData> lstPending = new ArrayList<SplitFileData>();

		String methodName = " [getSplitFiles] ";

		if (log.isDebugEnabled())
			log.debug(className + methodName + " gf_id:" + masterId + " maxtrycount" + maxRetryCount);

		String sql = "SELECT fileloc, total, id FROM group_file_splits WHERE lower(status) NOT IN ('completed') AND g_f_id=?"
				+ " AND IFNULL(retry_count,0)<=?";

		if (log.isDebugEnabled())
			log.debug(className + methodName + " sql:" + sql);

		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			con = ConnectionFactoryForCMDB.getInstance().getConnection();

			pstmt = con.prepareStatement(sql);

			pstmt.setString(1, masterId);
			pstmt.setInt(2, Integer.parseInt(maxRetryCount));

			rs = pstmt.executeQuery();

			while (rs.next()) {
				SplitFileData splitData = new SplitFileData();
				splitData.setFileName(rs.getString("fileloc"));
				splitData.setTotal(rs.getLong("total"));
				splitData.setId(rs.getString("id"));
				lstPending.add(splitData);
			}

			if (log.isDebugEnabled()) {
				log.debug(className + methodName + "getSplitFiles Pending list size:" + lstPending.size());
			}
		} catch (Exception e) {
			log.error(className + methodName + "Exception : ", e);
			throw e;
		} finally {
			closeConnection(rs, pstmt, con);
		}
		return lstPending;
	}
	
	public void updateFileStartTime(String id) throws Exception {

		String methodName = " [updateFileStartTime] ";
		if (log.isDebugEnabled())
			log.debug(className + methodName + "begin id:" + id);

		Connection con = null;
		PreparedStatement pstmt = null;
		String sql = "update group_file_splits set started_ts=now()  where id = ? ";
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
	
	public String updateProcessStatusSql(String id, int valid, int invalid, int total, String status, String reason,
			int duplicate, String instanceId) throws Exception {

		String methodName = "[updateProcessStatusSql]";

		StringBuilder sql = new StringBuilder();
		sql.append("update group_file_splits set total = ");
		sql.append("IFNULL(total,0)+");
		sql.append(total);
		sql.append(",valid= ");
		sql.append("IFNULL(valid,0)+");
		sql.append(valid);
		sql.append(",invalid= ");
		sql.append("IFNULL(invalid,0)+");
		sql.append(invalid);
		sql.append(",duplicate= ");
		sql.append("IFNULL(duplicate,0)+");
		sql.append(duplicate);
		sql.append(",status ='");
		sql.append(status).append("',");
		if(StringUtils.isNotBlank(reason)) {
			sql.append("reason ='").append(reason).append("',");
		}
		sql.append("instance_id ='").append(instanceId);
		sql.append("', completed_ts=now() where id = '");
		sql.append(id);
		sql.append("'");
		
		if (log.isDebugEnabled()) {
			log.debug(className + methodName + "sql = " + sql.toString());
			log.debug(className + methodName + "end ..");
		}

		return sql.toString();
	}
	
	
	public static synchronized void updateStatus(String id, int valid, int invalid, int total, String status, String reason,
			int duplicate, String instanceId) throws Exception {

		String methodName = " [updateStatus] ";

		StringBuilder sql = new StringBuilder();
		sql.append("update group_file_splits set total = ");
		sql.append("IFNULL(total,0)+");
		sql.append(total);
		sql.append(",valid= ");
		sql.append("IFNULL(valid,0)+");
		sql.append(valid);
		sql.append(",invalid= ");
		sql.append("IFNULL(invalid,0)+");
		sql.append(invalid);
		sql.append(",duplicate= ");
		sql.append("IFNULL(duplicate,0)+");
		sql.append(duplicate);
		sql.append(",status ='");
		sql.append(status).append("',");
		if(StringUtils.isNotBlank(reason)) {
			sql.append("reason ='").append(reason).append("',");
		}
		sql.append("instance_id ='").append(instanceId);
		sql.append("', completed_ts=now() where id = '");
		sql.append(id);
		sql.append("'");
		
		Connection con = null;
		PreparedStatement pstmt = null;
		if (log.isDebugEnabled()) {
			log.debug(className + methodName + "sql = " + sql.toString());
		}
		try {
			con = ConnectionFactoryForCMDB.getInstance().getConnection();
			pstmt = con.prepareStatement(sql.toString());
			pstmt.executeUpdate();
		} catch (Exception sqlex) {
			log.error(className + methodName + "Exception: " + sqlex);
			throw sqlex;
		} finally {
			if (pstmt != null) {
				pstmt.close();
			}

			if (con != null) {
				con.close();
			}
		}
		
		if (log.isDebugEnabled()) {
			log.debug(className + methodName + "end ..");
		}
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
