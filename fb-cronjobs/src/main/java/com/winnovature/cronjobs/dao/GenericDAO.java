package com.winnovature.cronjobs.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.cronjobs.utils.Constants;
import com.winnovature.utils.singletons.ConnectionFactoryForCMDB;

public class GenericDAO {

	static Log log = LogFactory.getLog(Constants.CronJobLogger);
	private static final String className = "[GenericDAO]";

	public List<String> getFilesForDate(String date) throws Exception {
		String logname = className + " [getFilesForDate] " + date;

		ResultSet rs = null;
		Connection con = null;
		PreparedStatement pstmt = null;
		List<String> filesList = new ArrayList<String>();

		String query_campaigns = "select fileloc from cm.campaign_files where fileloc is not null and created_ts >=? and created_ts <= ?";
		String query_campaigns_sched = "select filelocs as fileloc from cm.campaign_schedule_master where filelocs is not null and created_ts >=? and created_ts <= ?";
		String query_groups = "select fileloc from cm.group_files where fileloc is not null and created_ts >=? and created_ts <= ?";
		String query_dlt_templates = "select fileloc from cm.dlt_template_files where fileloc is not null and created_ts >=? and created_ts <= ?";

		StringBuilder query = new StringBuilder();
		query.append(query_campaigns).append(" UNION ALL ").append(query_campaigns_sched).append(" UNION ALL ")
				.append(query_groups).append(" UNION ALL ").append(query_dlt_templates);

		String sqlQuery = query.toString();
		try {
			if (log.isDebugEnabled()) {
				log.debug(logname + " sqlQuery = " + sqlQuery);
			}

			String start = date + " 00:00:00";
			String end = date + " 23:59:59";

			con = ConnectionFactoryForCMDB.getInstance().getConnection();
			pstmt = con.prepareStatement(sqlQuery);
			pstmt.setString(1, start);
			pstmt.setString(2, end);
			pstmt.setString(3, start);
			pstmt.setString(4, end);
			pstmt.setString(5, start);
			pstmt.setString(6, end);
			pstmt.setString(7, start);
			pstmt.setString(8, end);

			rs = pstmt.executeQuery();

			while (rs.next()) {
				String files = rs.getString("fileloc");
				if (files != null && files.trim().length() > 0) {
					String[] fls = StringUtils.split(files, ",");
					filesList.addAll(Arrays.asList(fls));
				}
			}

		} catch (Exception e) {
			log.error(logname + " Exception: ", e);
			throw e;
		} finally {
			closeConnection(rs, pstmt, con);
		}

		if (log.isDebugEnabled()) {
			log.debug(logname + " files found are " + filesList);
		}

		return filesList.size() > 0 ? filesList : null;
	}

	public List<String> getProcessedFilesForDate(String date, String status) throws Exception {
		String logname = className + " [getProcessedFilesForDate] " + date;

		ResultSet rs = null;
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs1 = null;
		PreparedStatement pstmt1 = null;
		List<String> filesList = new ArrayList<String>();
		List<String> multiSheduledFilesList = new ArrayList<String>();

		String query_campaigns = "select fileloc from cm.campaign_files where created_ts >=? and created_ts <= ? and lower(status) in (inclause)";
		String query_campaigns_splitfiles = "select fileloc from cm.campaign_file_splits where created_ts >=? and created_ts <= ?  and lower(status) in (inclause)";
		String query_groups = "select fileloc from cm.group_files where created_ts >=? and created_ts <= ? and lower(status) in (inclause)";
		String query_groups_splitfiles = "select fileloc from cm.group_file_splits where created_ts >=? and created_ts <= ? and lower(status) in (inclause)";
		String query_dlt_templates = "select fileloc from cm.dlt_template_files where fileloc is not null and created_ts >=? and created_ts <= ? and lower(status) in (inclause)";
		String query_campaigns_scheduled = "select csm.filelocs from cm.campaign_schedule_master csm, cm.campaign_schedule_at csa where csm.id = csa.cs_id and csa.status in ('queued') and filelocs is not null";

		StringBuilder query = new StringBuilder();
		query.append(query_campaigns).append(" UNION ALL ").append(query_campaigns_splitfiles).append(" UNION ALL ")
				.append(query_groups).append(" UNION ALL ").append(query_groups_splitfiles).append(" UNION ALL ")
				.append(query_dlt_templates);

		status = "'" + StringUtils.join(StringUtils.split(status, "~"), "','") + "'".toLowerCase();

		String sqlQuery = query.toString();
		sqlQuery = sqlQuery.replaceAll("inclause", status);
		try {
			if (log.isDebugEnabled()) {
				log.debug(logname + " sqlQuery = " + sqlQuery);
			}

			String start = date + " 00:00:00";
			String end = date + " 23:59:59";

			con = ConnectionFactoryForCMDB.getInstance().getConnection();
			pstmt = con.prepareStatement(sqlQuery);
			pstmt.setString(1, start);
			pstmt.setString(2, end);
			pstmt.setString(3, start);
			pstmt.setString(4, end);
			pstmt.setString(5, start);
			pstmt.setString(6, end);
			pstmt.setString(7, start);
			pstmt.setString(8, end);
			pstmt.setString(9, start);
			pstmt.setString(10, end);

			rs = pstmt.executeQuery();

			while (rs.next()) {
				String files = rs.getString("fileloc");
				if (files != null && files.trim().length() > 0) {
					filesList.add(files.trim());
				}
			}

			pstmt1 = con.prepareStatement(query_campaigns_scheduled);
			rs1 = pstmt1.executeQuery();

			while (rs1.next()) {
				String files = rs1.getString("filelocs");
				if (files != null && files.trim().length() > 0) {
					String[] fls = StringUtils.split(files, ",");
					multiSheduledFilesList.addAll(Arrays.asList(fls));
				}
			}
			// exclude multi-scheduled camp files which are yet to start
			if (multiSheduledFilesList.size() > 0) {
				filesList.removeAll(multiSheduledFilesList);
			}

		} catch (Exception e) {
			log.error(logname + " Exception: ", e);
			throw e;
		} finally {
			closeConnection(rs1, pstmt1, null);
			closeConnection(rs, pstmt, con);
		}

		if (log.isDebugEnabled()) {
			log.debug(logname + " files found are " + filesList);
		}

		return filesList.size() > 0 ? filesList : null;
	}

	public List<Map<String, String>> getProcessedCampaignsForDate(String date, String status) throws Exception {
		String logname = className + " [getProcessedCampaignsForDate] " + date;

		ResultSet rs = null;
		Connection con = null;
		PreparedStatement pstmt = null;
		List<Map<String, String>> campaigns = new ArrayList<Map<String, String>>();

		String sqlQuery = "select id, cli_id from cm.campaign_master where created_ts >=? and created_ts <= ? and lower(status) in (inclause)";

		try {
			status = "'" + StringUtils.join(StringUtils.split(status, "~"), "','") + "'".toLowerCase();
			sqlQuery = sqlQuery.replaceAll("inclause", status);

			if (log.isDebugEnabled()) {
				log.debug(logname + " sqlQuery = " + sqlQuery);
			}

			String start = date + " 00:00:00";
			String end = date + " 23:59:59";

			con = ConnectionFactoryForCMDB.getInstance().getConnection();
			pstmt = con.prepareStatement(sqlQuery);
			pstmt.setString(1, start);
			pstmt.setString(2, end);

			rs = pstmt.executeQuery();

			Map<String, String> data = null;

			while (rs.next()) {
				data = new HashMap<String, String>();
				data.put("id", rs.getString("id"));
				data.put("cli_id", rs.getString("cli_id"));
				campaigns.add(data);
			}

		} catch (Exception e) {
			log.error(logname + " Exception: ", e);
			throw e;
		} finally {
			closeConnection(rs, pstmt, con);
		}

		if (log.isDebugEnabled()) {
			log.debug(logname + " campaigns found are " + campaigns);
		}

		return campaigns.size() > 0 ? campaigns : null;
	}

	private void closeConnection(ResultSet rs, PreparedStatement ps, Connection con) throws Exception {
		try {
			if (rs != null) {
				rs.close();
			}
			if (ps != null) {
				ps.close();
			}
			if (con != null && !con.isClosed()) {
				con.close();
			}
		} catch (Exception e) {
			log.error(className + " [closeConnection] ", e);
		}
	}

}
