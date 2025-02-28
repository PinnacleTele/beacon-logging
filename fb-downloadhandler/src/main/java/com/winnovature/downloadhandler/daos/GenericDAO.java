package com.winnovature.downloadhandler.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.downloadhandler.utils.Constants;
import com.winnovature.utils.dtos.RedisServerDetailsBean;
import com.winnovature.utils.singletons.ConnectionFactoryForCMDB;

public class GenericDAO {
	static Log log = LogFactory.getLog(Constants.DownloadHandlerLogger);
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
			pstmt.setInt(1, com.winnovature.utils.utils.Constants.REDIS_STATUS_FOR_DLT_FILE_PROCESS);

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
				log.debug(className + methodName + " taskQueue" + redisServerDetails.size());

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
			pstmt.setInt(1, com.winnovature.utils.utils.Constants.REDIS_STATUS_FOR_DLT_FILE_PROCESS);

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
				log.debug(className + methodName + " redis config list "
						+ redisServerDetails.size());

		} catch (Exception e) {
			log.error(className + methodName + "Exception: ", e);
			throw e;
		} finally {
			closeConnection(rs, pstmt, con);
		}

		return redisServerDetails;

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
