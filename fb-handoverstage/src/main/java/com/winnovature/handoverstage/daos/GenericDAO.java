package com.winnovature.handoverstage.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.handoverstage.utils.Constants;
import com.winnovature.utils.dtos.RedisServerDetailsBean;
import com.winnovature.utils.singletons.ConnectionFactoryForCMDB;

public class GenericDAO {

	static Log log = LogFactory.getLog(Constants.HandoverStageLogger);

	private static final String className = "[GenericDAO]";

	public Map<String, RedisServerDetailsBean> selectRedisServerDetailsAsMap() throws Exception {

		String methodName = " [selectRedisServerDetailsAsMap] ";
		if (log.isDebugEnabled())
			log.debug(className + methodName + " Begin");

		Map<String, RedisServerDetailsBean> redisServerDetails = null;

		String sql = "select * from redis_info_cm where status = ? order by rid";

		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		if (log.isDebugEnabled())
			log.debug(className + methodName + " sql:" + sql);

		try {

			con = ConnectionFactoryForCMDB.getInstance().getConnection();
			pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, com.winnovature.utils.utils.Constants.REDIS_STATUS);

			rs = pstmt.executeQuery();
			redisServerDetails = new LinkedHashMap<String, RedisServerDetailsBean>();
			while (rs.next()) {

				RedisServerDetailsBean bean = new RedisServerDetailsBean();
				bean.setRid(rs.getString("rid"));
				bean.setIpAddress(rs.getString("ip"));
				bean.setPort(rs.getString("port"));
				bean.setMdb(rs.getString("db"));
				bean.setMaxPool(rs.getString("maxpool"));
				bean.setTimeout(rs.getString("con_time_out_insec"));
				bean.setMaxWait(rs.getString("con_wait_time_insec"));

				redisServerDetails.put(bean.getRid(), bean);

			}

			if (log.isDebugEnabled())
				log.debug(className + methodName + " redis config size " + redisServerDetails.size());

		} catch (Exception e) {
			log.error(className + methodName + "Exception: ", e);
			throw e;
		} finally {
			closeConnection(rs, pstmt, con);
		}

		return redisServerDetails;
	}

	public List<RedisServerDetailsBean> selectRedisServerDetails() throws Exception {

		String methodName = "[selectRedisServerDetails]";
		if (log.isDebugEnabled())
			log.debug(className + methodName + " Begin");

		List<RedisServerDetailsBean> redisServerDetails = null;

		String sql = "select * from redis_info_cm where status = ? order by rid";

		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		if (log.isDebugEnabled())
			log.debug(className + methodName + " sql:" + sql);

		try {

			con = ConnectionFactoryForCMDB.getInstance().getConnection();
			pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, com.winnovature.utils.utils.Constants.REDIS_STATUS);

			rs = pstmt.executeQuery();
			redisServerDetails = new LinkedList<RedisServerDetailsBean>();
			while (rs.next()) {

				RedisServerDetailsBean bean = new RedisServerDetailsBean();
				bean.setRid(rs.getString("rid"));
				bean.setIpAddress(rs.getString("ip"));
				bean.setPort(rs.getString("port"));
				bean.setMdb(rs.getString("db"));
				bean.setMaxPool(rs.getString("maxpool"));
				bean.setTimeout(rs.getString("con_time_out_insec"));
				bean.setMaxWait(rs.getString("con_wait_time_insec"));

				redisServerDetails.add(bean);

			}

			if (log.isDebugEnabled())
				log.debug(className + methodName + " redis config size " + redisServerDetails.size());

		} catch (Exception e) {
			log.error(className + methodName + "Exception: ", e);
			throw e;
		} finally {
			closeConnection(rs, pstmt, con);
		}

		return redisServerDetails;

	}

	public String updateFailedRequestToQueuedSql(String id, String status, String reason, int noOfRetryCount)
			throws Exception {

		String sql = "update campaign_file_splits set status = '" + status.toLowerCase() + "',reason = '" + reason
				+ "',retry_count=" + noOfRetryCount + " where id = '" + id + "' ";

		return sql;
	}
	
	public void updateFailedRequestToQueued(String id, String status, String reason, int noOfRetryCount)
			throws Exception {

		String methodName = " [updateFailedRequestToQueued] ";
		if (log.isDebugEnabled())
			log.debug(className + methodName + "begin id:" + id + " status:" + status);

		Connection con = null;
		PreparedStatement pstmt = null;
		String sql = "update campaign_file_splits set status = ?, reason = ?, retry_count=? where id = ? ";

		if (log.isDebugEnabled())
			log.debug(className + methodName + "sql = " + sql);

		try {

			con = ConnectionFactoryForCMDB.getInstance().getConnection();
			pstmt = con.prepareStatement(sql);

			pstmt.setString(1, status.toUpperCase());
			pstmt.setString(2, reason);
			pstmt.setInt(3, noOfRetryCount);
			pstmt.setString(4, id);

			pstmt.executeUpdate();

		} catch (Exception sqlex) {
			log.error(className + methodName + "Exception: " + sqlex);
			throw sqlex;
		} finally {
			closeConnection(null, pstmt, con);
		}
		if (log.isDebugEnabled())
			log.debug(className + methodName + "end ..");

	}
	
	public void updateFileStartTime(String id) throws Exception {

		String methodName = " [updateFileStartTime] ";
		if (log.isDebugEnabled())
			log.debug(className + methodName + "begin id:" + id);

		Connection con = null;
		PreparedStatement pstmt = null;
		String sql = "update campaign_file_splits set started_ts=now()  where id = ? ";
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
			int excludeCount, String instanceId) throws Exception {

		String methodName = "[updateProcessStatusSql]";
		if (log.isDebugEnabled())
			log.debug(className + methodName + "begin ..");

		StringBuilder sql = new StringBuilder();
		sql.append("update campaign_file_splits set total = ");
		sql.append("IFNULL(total,0)+");
		sql.append(total);
		sql.append(",valid= ");
		sql.append("IFNULL(valid,0)+");
		sql.append(valid);
		sql.append(",invalid= ");
		sql.append("IFNULL(invalid,0)+");
		sql.append(invalid);
		sql.append(",excluded= ");
		sql.append("IFNULL(excluded,0)+");
		sql.append(excludeCount);
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

	public static synchronized void updateProcessStatus(String id, int valid, int invalid, int total, String status, String reason,
			int excludeCount, String instanceId) throws Exception {

		String methodName = "[updateProcessStatus]";
		if (log.isDebugEnabled())
			log.debug(className + methodName + "begin ..");

		Connection con = null;
		PreparedStatement pstmt = null;

		StringBuilder sql = new StringBuilder();
		sql.append("update campaign_file_splits set total = ");
		sql.append("IFNULL(total,0)+");
		sql.append(total);
		sql.append(",valid= ");
		sql.append("IFNULL(valid,0)+");
		sql.append(valid);
		sql.append(",invalid= ");
		sql.append("IFNULL(invalid,0)+");
		sql.append(invalid);
		sql.append(",excluded= ");
		sql.append("IFNULL(excluded,0)+");
		sql.append(excludeCount);
		sql.append(",status ='");
		sql.append(status).append("',");
		if(StringUtils.isNotBlank(reason)) {
			sql.append("reason ='").append(reason).append("',");
		}
		sql.append("instance_id ='").append(instanceId);
		sql.append("', completed_ts=now() where id = '");
		sql.append(id);
		sql.append("'");		

		if (log.isDebugEnabled())
			log.debug(className + methodName + "sql = " + sql);

		try {

			con = ConnectionFactoryForCMDB.getInstance().getConnection();
			pstmt = con.prepareStatement(sql.toString());

			pstmt.executeUpdate();

		} catch (Exception sqlex) {
			log.error(className + methodName + "Exception: " + sqlex);
			throw sqlex;
		} finally {
			//closeConnection(null, pstmt, con);
			if (pstmt != null) {
				pstmt.close();
			}

			if (con != null) {
				con.close();
			}
		}
		if (log.isDebugEnabled())
			log.debug(className + methodName + "end ..");
	}
	
	public String updateCountMasterSql(String tagid, int valid, int invalid, int duplicate, int cancelledCount,
			String sche_ts) throws Exception {

		String methodName = "[updateCountMasterSql]";
		if (log.isDebugEnabled())
			log.debug(className + methodName + "begin ..");

		String sql = "";

		if (log.isDebugEnabled()) {
			log.debug(className + methodName + "tagid=" + tagid);
			log.debug(className + methodName + "valid=" + valid);
			log.debug(className + methodName + "invalid=" + invalid);
			log.debug(className + methodName + "duplicate=" + duplicate);
			log.debug(className + methodName + "cancelledCount=" + cancelledCount);
			log.debug(className + methodName + "sche_ts=" + sche_ts);
		}

		sql = "update SEND_SMS_REQ set valid=(select sum(IFNULL(valid,0)) from SEND_SMS_DELIVERY  where TAGID = "
				+ tagid
				+ " AND SPLIT_YN=1), INVALID=(select sum(IFNULL(INVALID,0)) from SEND_SMS_DELIVERY  where TAGID = "
				+ tagid
				+ " AND SPLIT_YN=1), DUPLICATE=(select sum(IFNULL(DUPLICATE,0)) from SEND_SMS_DELIVERY  where TAGID = "
				+ tagid
				+ " AND SPLIT_YN=1), EXCLUDE=(select sum(IFNULL(EXCLUDE,0)) from SEND_SMS_DELIVERY  where TAGID = "
				+ tagid
				+ " AND SPLIT_YN=1)	, cancelled_count =(select sum(IFNULL(cancelled_count ,0)) from SEND_SMS_DELIVERY  where TAGID = "
				+ tagid + " AND SPLIT_YN=1) where TAGID = " + tagid + " AND SPLIT_YN=0";

		if (log.isDebugEnabled()) {
			log.debug(className + methodName + "sql = " + sql);
			log.debug(className + methodName + "end ..");
		}

		return sql;
	}
	
	public static synchronized void updateCountMaster(String tagid, int valid, int invalid, int duplicate,
			int cancelledCount, String sche_ts, int excludeCount) throws Exception {

		String methodName = "[updateCountMaster]";

		if (log.isDebugEnabled())
			log.debug(className + methodName + "begin ..");

		Connection con = null;

		Statement stmt = null;
		String sql = "";

		int total = valid + invalid + duplicate;

		int result = 0;
		if (log.isDebugEnabled()) {
			log.debug(className + methodName + "tagid=" + tagid);
			log.debug(className + methodName + "valid=" + valid);
			log.debug(className + methodName + "invalid=" + invalid);
			log.debug(className + methodName + "duplicate=" + duplicate);
			log.debug(className + methodName + "sche_ts=" + sche_ts);
			log.debug(className + methodName + "excludeCount=" + excludeCount);
			log.debug(className + methodName + "cancelledCount=" + cancelledCount);

		}

		if (sche_ts != "") {
			sql = "update SEND_SMS_REQ set valid=(select sum(IFNULL(valid,0)) from SEND_SMS_REQ  where TAGID = " + tagid
					+ " AND SPLIT_YN=1), INVALID=(select sum(IFNULL(INVALID,0)) from SEND_SMS_REQ  where TAGID = "
					+ tagid
					+ " AND SPLIT_YN=1), DUPLICATE=(select sum(IFNULL(DUPLICATE,0)) from SEND_SMS_REQ  where TAGID = "
					+ tagid
					+ " AND SPLIT_YN=1), EXCLUDE=(select sum(IFNULL(EXCLUDE,0)) from SEND_SMS_REQ  where TAGID = "
					+ tagid
					+ " AND SPLIT_YN=1)	, cancelled_count =(select sum(IFNULL(cancelled_count ,0)) from SEND_SMS_REQ  where TAGID = "
					+ tagid + " AND SPLIT_YN=1) where TAGID = " + tagid + " AND SPLIT_YN=0";
		} else {
			sql = "update SEND_SMS_REQ set VALID= IFNULL(VALID,0) + " + valid + ",INVALID=IFNULL(INVALID,0)+ " + invalid
					+ ",DUPLICATE =IFNULL(DUPLICATE,0)+" + duplicate + ",EXCLUDE =IFNULL(EXCLUDE,0)+" + excludeCount
					+ ",cancelled_count =IFNULL(cancelled_count,0)+" + cancelledCount + ",total = " + total
					+ "+IFNULL(VALID,0) +IFNULL(INVALID,0)+IFNULL(DUPLICATE,0)+IFNULL(EXCLUDE,0)+IFNULL(cancelled_count,0)  where TAGID = "
					+ tagid + " AND SPLIT_YN=0 ";
		}

		if (log.isDebugEnabled())
			log.debug(className + methodName + "sql = " + sql);

		try {

			con = ConnectionFactoryForCMDB.getInstance().getConnection();

			stmt = con.createStatement();
			result = stmt.executeUpdate(sql);

			if (log.isDebugEnabled()) {
				log.debug(className + methodName + "result=" + result);
			}

		} catch (Exception sqlex) {
			log.error(className + methodName + "SQLException: " + sqlex);
			throw sqlex;
		} finally {

			if (stmt != null) {
				stmt.close();
			}

			if (con != null) {
				con.close();
			}
		}
		if (log.isDebugEnabled()) {
			log.debug(className + methodName + "updated record=" + result);
			log.debug(className + methodName + "end ..");
		}

	}
	
	public static synchronized void updateRequestStatus(String id, int valid, int invalid, int total, String status, String reason,
			int excludeCount, String instanceId, String retry) throws Exception {

		String methodName = "[updateRequestStatus]";
		if (log.isDebugEnabled())
			log.debug(className + methodName + "begin ..");

		Connection con = null;
		PreparedStatement pstmt = null;

		StringBuilder sql = new StringBuilder();
		sql.append("update campaign_file_splits set total = ");
		sql.append("IFNULL(total,0)+");
		sql.append(total);
		sql.append(",valid= ");
		sql.append("IFNULL(valid,0)+");
		sql.append(valid);
		sql.append(",invalid= ");
		sql.append("IFNULL(invalid,0)+");
		sql.append(invalid);
		sql.append(",excluded= ");
		sql.append("IFNULL(excluded,0)+");
		sql.append(excludeCount);
		sql.append(",status ='");
		sql.append(status).append("',");
		if(StringUtils.isNotBlank(reason)) {
			sql.append("reason ='").append(reason).append("',");
		}
		sql.append("instance_id ='").append(instanceId);
		sql.append("',retry_count =").append(retry);
		sql.append(", completed_ts=now() where id = '");
		sql.append(id);
		sql.append("'");		

		if (log.isDebugEnabled())
			log.debug(className + methodName + "sql = " + sql);

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
		if (log.isDebugEnabled())
			log.debug(className + methodName + "end ..");
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
