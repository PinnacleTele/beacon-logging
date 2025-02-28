package com.winnovature.splitstage.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.splitstage.singletons.SplitStagePropertiesTon;
import com.winnovature.splitstage.utils.Constants;
import com.winnovature.utils.dtos.SplitFileData;
import com.winnovature.utils.singletons.ConnectionFactoryForCMDB;

public class CampaignFileSplitsDAO {
	static Log log = LogFactory.getLog(Constants.SplitStageLogger);

	private static final String className = "[CampaignFileSplitsDAO]";

	public List<SplitFileData> getSplitFiles(String masterId, String maxRetryCount) throws Exception {

		List<SplitFileData> lstPending = new ArrayList<SplitFileData>();

		String methodName = " [getSplitFiles] ";

		if (log.isDebugEnabled())
			log.debug(className + methodName + " cf_id:" + masterId + " maxtrycount" + maxRetryCount);

		String sql = "SELECT fileloc, total, id FROM campaign_file_splits WHERE lower(status) NOT IN ('completed') AND c_f_id=?"
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
				log.debug(className + methodName + "get SplitFiles Pending list size:" + lstPending.size());
			}
		} catch (Exception e) {
			log.error(className + methodName + "Exception : ", e);
			throw e;
		} finally {
			closeConnection(rs, pstmt, con);
		}
		return lstPending;
	}

	public void insertSplitDetailsInCampaignFileSplits(List<SplitFileData> filelocList, Map<String, String> reqMap)
			throws Exception {

		String methodName = " [insertSplitDetailsInCampaignFileSplits] ";
		if (log.isDebugEnabled()) {
			log.debug(className + methodName + " begin ...");
		}

		Connection con = null;
		PreparedStatement pstmt = null;
		String sql = "INSERT INTO campaign_file_splits (id, c_id, c_f_id, group_id, filename, fileloc, exclude_group_ids, instance_id, status, created_ts) values "
				+ "(?,?,?,?,?,?,?,?,?,now())";

		String status = SplitStagePropertiesTon.getInstance().getPropertiesConfiguration()
				.getString(Constants.FILE_SMS_INPROCESS_STATUS);

		String instanceId = SplitStagePropertiesTon.getInstance().getPropertiesConfiguration()
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
				pstmt.setString(i++, reqMap.get("cm_id"));
				pstmt.setString(i++, reqMap.get("cf_id"));
				pstmt.setString(i++, reqMap.get("group_id"));
				pstmt.setString(i++, reqMap.get("filename"));
				pstmt.setString(i++, fileName);
				pstmt.setString(i++, reqMap.get("exclude_group_ids"));
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
