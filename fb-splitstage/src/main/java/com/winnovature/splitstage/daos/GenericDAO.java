package com.winnovature.splitstage.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.splitstage.singletons.SplitStagePropertiesTon;
import com.winnovature.splitstage.utils.Constants;
import com.winnovature.utils.dtos.RedisServerDetailsBean;
import com.winnovature.utils.dtos.Templates;
import com.winnovature.utils.singletons.ConnectionFactoryForCMDB;

public class GenericDAO {

	static Log log = LogFactory.getLog(Constants.SplitStageLogger);

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
				log.debug(className + methodName + " size " + redisServerDetails.size());

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
				log.debug(className + methodName + " taskQueue" + redisServerDetails.size());

		} catch (Exception e) {
			log.error(className + methodName + "Exception: ", e);
			throw e;
		} finally {
			closeConnection(rs, pstmt, con);
		}

		return redisServerDetails;

	}

	public void updateCampaignStatus(String cm_id, String cf_id, String status, String reason) throws Exception {

		String methodName = "[updateCampaignStatus]";

		StringBuilder campaign_master_sql = new StringBuilder("update campaign_master SET status = ? ");
		if (status.equalsIgnoreCase(Constants.PROCESS_STATUS_FAILED)) {
			campaign_master_sql.append(", reason=? ");
		}
		campaign_master_sql.append(" where id=?");

		StringBuilder campaign_files_sql = new StringBuilder("update campaign_files SET status = ? ");
		if (status.equalsIgnoreCase(Constants.PROCESS_STATUS_FAILED)) {
			campaign_files_sql.append(", retry_count=IFNULL(retry_count,0)+1, reason=? ");
		}
		campaign_files_sql.append(" where id=?");

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
			if (status.equalsIgnoreCase(Constants.PROCESS_STATUS_FAILED)) {
				campaignMsaterPstmt.setString(1, status);
				campaignMsaterPstmt.setString(2, reason);
				campaignMsaterPstmt.setString(3, cm_id);
			} else {
				campaignMsaterPstmt.setString(1, status);
				campaignMsaterPstmt.setString(2, cm_id);
			}

			campaingMasterUpdateCount = campaignMsaterPstmt.executeUpdate();
			if (campaingMasterUpdateCount > 0) {
				campaignFilesPstmt = con.prepareStatement(campaign_files_sql.toString());
				if (status.equalsIgnoreCase(Constants.PROCESS_STATUS_FAILED)) {
					campaignFilesPstmt.setString(1, status);
					campaignFilesPstmt.setString(2, reason);
					campaignFilesPstmt.setString(3, cf_id);
				} else {
					campaignFilesPstmt.setString(1, status);
					campaignFilesPstmt.setString(2, cf_id);
				}

				campaingFilesUpdateCount = campaignFilesPstmt.executeUpdate();
				if (campaingFilesUpdateCount > 0) {
					con.commit();
				} else {
					con.rollback();
				}
			} else {
				con.rollback();
			}

			if (log.isDebugEnabled()) {
				log.debug(className + methodName + " campaign_master update count : " + campaingMasterUpdateCount
						+ " campaign_files update count : " + campaingFilesUpdateCount);
			}
		} catch (Exception sqlex) {
			log.error(className + methodName, sqlex);
			con.rollback();
			throw sqlex;
		} finally {
			closeConnection(null, campaignMsaterPstmt, con);
		}
	}

	public void updateCampaignFilesStatusInvalidFile(String id, String reason, String total, String maxRetryCount) throws Exception {

		String methodName = "[updateCampaignFilesStatusInvalidFile]";

		if (log.isDebugEnabled()) {
			log.debug(className + methodName + " id:" + id + " Reason:" + reason);
		}

		String instanceId = SplitStagePropertiesTon.getInstance().getPropertiesConfiguration()
				.getString(Constants.MONITORING_INSTANCE_ID);

		String sql = "UPDATE campaign_files SET status = ?,reason=?, retry_count=IFNULL(retry_count,0)+?, total=?, instance_id=?  WHERE id = ? ";

		if (log.isDebugEnabled()) {
			log.debug(className + methodName + "sql" + sql);
		}

		Connection con = null;
		PreparedStatement pstmt = null;

		try {
			con = ConnectionFactoryForCMDB.getInstance().getConnection();
			pstmt = con.prepareStatement(sql);
			//pstmt.setString(1, Constants.PROCESS_STATUS_FAILED);
			pstmt.setString(1, Constants.PROCESS_STATUS_COMPLETED);
			pstmt.setString(2, reason);
			pstmt.setString(3, maxRetryCount);
			pstmt.setString(4, total);
			pstmt.setString(5, instanceId);
			pstmt.setString(6, id);
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
	
	public Templates selectTemplate(String templateId) throws Exception {

		String methodName = " [selectTemplate] ";
		Templates template = null;
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "select * from template_master where id = ? ";
		try {
			con = ConnectionFactoryForCMDB.getInstance().getConnection();
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, templateId);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				template = new Templates();
				template.setTmplid(rs.getString("id"));
				template.setClientId(rs.getString("cli_id"));
				template.setTmplname(rs.getString("t_name"));
				template.setTemplateType(rs.getString("t_type"));
				template.setPhoneNumberField(rs.getString("t_mobile_column"));
				template.setMsg_text(rs.getString("t_content"));
				template.setUnicode(rs.getInt("is_unicode"));
				template.setCdate(rs.getTimestamp("created_ts"));
				template.setMdate(rs.getTimestamp("modified_ts"));
				// template.setTmpl_flds(rs.getString("tmpl_flds"));
			}
		} catch (Exception ex) {
			log.error(className + methodName + "Exception: ", ex);
			throw ex;
		} finally {
			closeConnection(rs, pstmt, con);
		}
		return template;
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
