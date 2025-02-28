package com.winnovature.downloadhandler.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.downloadhandler.utils.Constants;
import com.winnovature.downloadhandler.utils.FileSender;
import com.winnovature.utils.singletons.ConnectionFactoryForCMDB;

public class DownloadReqDAO {
	static Log log = LogFactory.getLog(Constants.DownloadHandlerLogger);
	private static final String className = "[DownloadReqDAO]";

	public void pollDownloadReq(String csvCompleteStatus, String redisQueueName) throws Exception {

		String methodName = " [pollDownloadReq] ";

		String sql = "select id, download_path from download_req where lower(status) = ? order by created_ts desc";

		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Map<String, String> downloadReq = null;

		try {

			con = ConnectionFactoryForCMDB.getInstance().getConnection();
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, csvCompleteStatus.toLowerCase());

			rs = pstmt.executeQuery();
			while (rs.next()) {
				downloadReq = new HashMap<String, String>();
				downloadReq.put("id", rs.getString("id"));
				downloadReq.put("csv_download_path", rs.getString("download_path"));
				handoverToRedis(downloadReq, redisQueueName);
			}

		} catch (Exception e) {
			log.error(className + methodName + "Exception: ", e);
			throw e;
		} finally {
			closeConnection(rs, pstmt, con);
		}

	}

	public void handoverToRedis(Map<String, String> downInfo, String queueName) throws Exception {
		updateDownloadReqStatus(downInfo.get("id"), Constants.PROCESS_STATUS_XL_INPROCESS, null, null);
		// HO to DltFileQ
		boolean fileSenderStatus = FileSender.sendToFileQueue(downInfo, queueName);
		// if HO to redis failed update status=FAILED
		if (!fileSenderStatus) {
			updateDownloadReqStatus(downInfo.get("id"), Constants.PROCESS_STATUS_FAILED, "Redis handover failed", null);
		}
	}

	public void updateDownloadReqStatus(String id, String status, String reason, String path) throws Exception {

		String methodName = "[updateDownloadReqStatus]";

		StringBuilder download_req_update = new StringBuilder("update download_req SET status = ?, ");
		
		if(StringUtils.isNotBlank(reason)) {
			download_req_update.append(" reason=?, ");
		}
		
		if(StringUtils.isNotBlank(path)) {
			download_req_update.append(" download_xl_path=?, ");
		}
		
		download_req_update.append(" modified_ts=now() where id=? ");
		
		
		Connection con = null;
		PreparedStatement downloadReqPstmt = null;
		int index = 0;

		try {
			con = ConnectionFactoryForCMDB.getInstance().getConnection();
			downloadReqPstmt = con.prepareStatement(download_req_update.toString());
			downloadReqPstmt.setString(++index, status);
			
			if(StringUtils.isNotBlank(reason)) {
				downloadReqPstmt.setString(++index, reason);
			}
			
			if(StringUtils.isNotBlank(path)) {
				downloadReqPstmt.setString(++index, path);
			}
			
			downloadReqPstmt.setString(++index, id);

			downloadReqPstmt.executeUpdate();

		} catch (Exception sqlex) {
			log.error(className + methodName, sqlex);
			throw sqlex;
		} finally {
			closeConnection(null, downloadReqPstmt, con);
		}
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
