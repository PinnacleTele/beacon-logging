package com.winnovature.utils.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.utils.dtos.QueueType;
import com.winnovature.utils.dtos.RedisServerDetailsBean;
import com.winnovature.utils.dtos.Templates;
import com.winnovature.utils.singletons.ConnectionFactoryForCMDB;
import com.winnovature.utils.singletons.UtilsPropertiesTon;
import com.winnovature.utils.utils.Constants;

public class GenericDao {

	static Log log = LogFactory.getLog(Constants.UtilsLogger);

	private static final String className = "[GenericDao]";

	/**
	 * to fetch all configuration details from config_params
	 **/
	public Map<String, String> getConfigurations() throws Exception {

		String methodName = " [getConfigurations] ";
		if (log.isDebugEnabled())
			log.debug(className + methodName + " Begin");

		Map<String, String> configDetailsMap = null;
		
		/*
		String schema = UtilsPropertiesTon.getInstance()
		.getPropertiesConfiguration()
		.getString("database-schema","cm");
*/
	//	String sql = "select `key`,value from "+schema+".config_params";
		String sql = "select `key`,value from config_params";
		
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		if (log.isDebugEnabled())
			log.debug(className + methodName + " sql:" + sql);

		try {

			con = ConnectionFactoryForCMDB.getInstance().getConnection();
			pstmt = con.prepareStatement(sql);

			rs = pstmt.executeQuery();
			configDetailsMap = new HashMap<String, String>();
			while (rs.next()) {
				configDetailsMap.put(rs.getString("key"), rs.getString("value"));
			}

			if (log.isDebugEnabled())
				log.debug(className + methodName + " config_params size " + configDetailsMap.size());

		} catch (Exception e) {
			log.error(className + methodName + "Exception: ", e);
			throw e;
		} finally {
			closeConnection(rs, pstmt, con);
		}

		return configDetailsMap;
	}

	public Map<String, RedisServerDetailsBean> selectRedisServerDetailsAsMap() throws Exception {

		String methodName = "[selectRedisServerDetailsAsMap]";
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
			pstmt.setInt(1, Constants.REDIS_STATUS);

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
				log.debug(className + methodName + " taskQueue"
						+ redisServerDetails.size());

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
			pstmt.setInt(1, Constants.REDIS_STATUS);

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
				log.debug(className + methodName + " taskQueue"
						+ redisServerDetails.size());

		} catch (Exception e) {
			log.error(className + methodName + "Exception: ", e);
			throw e;
		} finally {
			closeConnection(rs, pstmt, con);
		}
		return redisServerDetails;

	} 
	
	public RedisServerDetailsBean selectRedisServerDetailsAsMapForHeartBeat() throws Exception {

	      String methodName = " [selectRedisServerDetailsAsMapForHeartBeat] ";
	      if (log.isDebugEnabled())
	            log.debug(className + methodName + " Begin");

	      String sql = "select * from redis_info_cm where status = ? order by rid";

	      Connection con = null;
	      PreparedStatement pstmt = null;
	      ResultSet rs = null;

	      if (log.isDebugEnabled())
	            log.debug(className + methodName + " sql:" + sql);
	      RedisServerDetailsBean bean=null;

	      try {

	            con = ConnectionFactoryForCMDB.getInstance().getConnection();
	            pstmt = con.prepareStatement(sql);
	            pstmt.setInt(1, Constants.REDIS_STATUS_HEART_BEAT);

	            rs = pstmt.executeQuery();
	            if (rs.next()) {

	                  bean = new RedisServerDetailsBean();
	                  bean.setRid(rs.getString("rid"));
	                  bean.setIpAddress(rs.getString("ip"));
	                  bean.setPort(rs.getString("port"));
	                  bean.setMdb(rs.getString("db"));
	                  bean.setMaxPool(rs.getString("maxpool"));
	                  bean.setTimeout(rs.getString("con_time_out_insec"));
	                  bean.setMaxWait(rs.getString("con_wait_time_insec"));
	                  bean.setPassword(rs.getString("password"));

	            }

	            if (log.isDebugEnabled())
	                  log.debug(className + methodName + " redis "
	                              + bean);

	      } catch (Exception e) {
	            log.error(className + methodName + "Exception: ",  e);
	            throw e;
	      } finally {
	            closeConnection(rs, pstmt, con);
	      }

	      return bean;

	}

	public Map<Long, QueueType> getQueueDetails() throws Exception {

		String methodName = "[getQueueDetails]";
		if(log.isDebugEnabled())
		log.debug(className + methodName + "begin ..");

		Map<Long, QueueType> countVsQueue = new LinkedHashMap<Long, QueueType>();
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		String sql = "SELECT max_count,name,type from queue_routing ORDER BY max_count DESC";

		if(log.isDebugEnabled())
		log.debug(className + methodName + "sql = " + sql);

		try {
			con = ConnectionFactoryForCMDB.getInstance().getConnection();
			pstmt = con.prepareStatement(sql);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				QueueType queueType = new QueueType();
				queueType.setQueueName(rs.getString("name"));
				queueType.setType(rs.getString("type"));
				countVsQueue.put(rs.getLong("max_count"), queueType);
			}
		} catch (Exception e) {
			log.error(className + methodName + " Exception:", e);
			throw e;
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (pstmt != null) {
				pstmt.close();
			}
			if (con != null) {
				con.close();
			}
		}
		return countVsQueue;
	}
	
	public List<RedisServerDetailsBean> selectRedisServerDetailsForUnProcess() throws Exception {

		String methodName = "[selectRedisServerDetailsForUnProcess]";
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
			pstmt.setInt(1, Constants.REDIS_STATUS_FOR_UNPROCESS);

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
				log.debug(className + methodName + " redis config size "
						+ redisServerDetails.size());

		} catch (Exception e) {
			log.error(className + methodName + "Exception: ", e);
			throw e;
		} finally {
			closeConnection(rs, pstmt, con);
		}

		return redisServerDetails;

	}
	
	public Map<String, RedisServerDetailsBean> selectRedisServerDetailsAsMapForUnProcess() throws Exception {

		String methodName = "[selectRedisServerDetailsAsMapForUnProcess]";
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
			// refer JIRA AIR-190
			pstmt.setInt(1, Constants.REDIS_STATUS_FOR_UNPROCESS);

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
				log.debug(className + methodName + " taskQueue"
						+ redisServerDetails.size());

		} catch (Exception e) {
			log.error(className + methodName + "Exception: ", e);
			throw e;
		} finally {
			closeConnection(rs, pstmt, con);
		}

		return redisServerDetails;

	}
	
	public Templates getTemplateById(String templateId) throws Exception {

		String methodName = "[getTemplateById]";
		if (log.isDebugEnabled())
			log.debug(className + methodName + " Begin");

		Templates template = null;

		String sql = "select id,cli_id,t_type,t_mobile_column,t_content,is_unicode from template_master where id = ?";

		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		if (log.isDebugEnabled())
			log.debug(className + methodName + " sql:" + sql);

		try {

			con = ConnectionFactoryForCMDB.getInstance().getConnection();
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, templateId);

			rs = pstmt.executeQuery();
			if(rs.next()) {
				template = new Templates();
				template.setTmplid(templateId);
				template.setClientId(rs.getString("cli_id"));
				template.setTemplateType(rs.getString("t_type"));
				template.setPhoneNumberField(rs.getString("t_mobile_column"));
				template.setMsg_text(rs.getString("t_content"));
				template.setUnicode(rs.getInt("is_unicode"));
			}

			if (log.isDebugEnabled())
				log.debug(className + methodName + " template "
						+ template);

		} catch (Exception e) {
			log.error(className + methodName + "Exception: ", e);
			throw e;
		} finally {
			closeConnection(rs, pstmt, con);
		}

		return template;

	}
	
	public RedisServerDetailsBean getFileUploadTrackingRedisServerDetails() throws Exception {

	      String methodName = " [getFileUploadTrackingRedisServerDetails] ";
	      if (log.isDebugEnabled())
	            log.debug(className + methodName + " Begin");

	      String sql = "select * from redis_info_cm where status = ? order by rid";

	      Connection con = null;
	      PreparedStatement pstmt = null;
	      ResultSet rs = null;

	      if (log.isDebugEnabled())
	            log.debug(className + methodName + " sql:" + sql);
	      RedisServerDetailsBean bean=null;

	      try {

	            con = ConnectionFactoryForCMDB.getInstance().getConnection();
	            pstmt = con.prepareStatement(sql);
	            pstmt.setInt(1, Constants.REDIS_STATUS_FOR_DLT_FILE_PROCESS);

	            rs = pstmt.executeQuery();
	            if (rs.next()) {

	                  bean = new RedisServerDetailsBean();
	                  bean.setRid(rs.getString("rid"));
	                  bean.setIpAddress(rs.getString("ip"));
	                  bean.setPort(rs.getString("port"));
	                  bean.setMdb(rs.getString("db"));
	                  bean.setMaxPool(rs.getString("maxpool"));
	                  bean.setTimeout(rs.getString("con_time_out_insec"));
	                  bean.setMaxWait(rs.getString("con_wait_time_insec"));
	                  bean.setPassword(rs.getString("password"));

	            }

	            if (log.isDebugEnabled())
	                  log.debug(className + methodName + " redis "
	                              + bean);

	      } catch (Exception e) {
	            log.error(className + methodName + "Exception: ",  e);
	            throw e;
	      } finally {
	            closeConnection(rs, pstmt, con);
	      }

	      return bean;

	}
	
	public List<RedisServerDetailsBean> getDupcheckRedisServerDetails() throws Exception {

	      String methodName = " [getDupcheckRedisServerDetails] ";
	      if (log.isDebugEnabled())
	            log.debug(className + methodName + " Begin");

	      String sql = "select * from redis_info_cm where status = ? order by rid";

	      Connection con = null;
	      PreparedStatement pstmt = null;
	      ResultSet rs = null;

	      if (log.isDebugEnabled())
	            log.debug(className + methodName + " sql:" + sql);
	      RedisServerDetailsBean bean=null;
	      List<RedisServerDetailsBean> list = new ArrayList<RedisServerDetailsBean>();

	      try {

	            con = ConnectionFactoryForCMDB.getInstance().getConnection();
	            pstmt = con.prepareStatement(sql);
	            pstmt.setInt(1, Constants.REDIS_STATUS_FOR_DUPCHECK);

	            rs = pstmt.executeQuery();
	            while (rs.next()) {
	                  bean = new RedisServerDetailsBean();
	                  bean.setRid(rs.getString("rid"));
	                  bean.setIpAddress(rs.getString("ip"));
	                  bean.setPort(rs.getString("port"));
	                  bean.setMdb(rs.getString("db"));
	                  bean.setMaxPool(rs.getString("maxpool"));
	                  bean.setTimeout(rs.getString("con_time_out_insec"));
	                  bean.setMaxWait(rs.getString("con_wait_time_insec"));
	                  bean.setPassword(rs.getString("password"));
	                  list.add(bean);
	            }

	            if (log.isDebugEnabled())
	                  log.debug(className + methodName + " redis "
	                              + bean);

	      } catch (Exception e) {
	            log.error(className + methodName + "Exception: ",  e);
	            throw e;
	      } finally {
	            closeConnection(rs, pstmt, con);
	      }

	      return list;

	}
	
	
	public void closeConnection(ResultSet rs, PreparedStatement ps, Connection con) {
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
			log.error(className + "[closeConnection] Exception: ", e);
		}

	}

}
