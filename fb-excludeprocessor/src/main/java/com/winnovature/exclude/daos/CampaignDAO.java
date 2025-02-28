package com.winnovature.exclude.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.exclude.singletons.ExcludeProcessorPropertiesTon;
import com.winnovature.exclude.utils.Constants;
import com.winnovature.utils.singletons.ConnectionFactoryForCMDB;

public class CampaignDAO {
	static Log log = LogFactory.getLog(Constants.ExcludeLogger);
	private static final String className = "[CampaignDAO]";
	
	public String updateFileFailedRequestToQueuedSql(String id, String file_loc,String status,
			String reason,int excludeCount,int fileNooftimesProcess ,int allrecordExcude) throws Exception {

		String instanceId = ExcludeProcessorPropertiesTon.getInstance()
		.getPropertiesConfiguration().getString(Constants.MONITORING_INSTANCE_ID);

		String sql = "update campaign_file_splits set ";
				if(file_loc!=null && file_loc.trim().length() > 0){
					sql+=" fileloc='"+file_loc+"', ";
				}
				if(allrecordExcude == 1) {   // 1 - file all number is exclude  and  0  is file have non exclude number
					sql+=" total="+excludeCount+", ";
				}
				if(fileNooftimesProcess != -1) {
					sql+=" retry_count="+fileNooftimesProcess+", ";
				}
				if(StringUtils.isNotBlank(status)) {
					sql+=" status='"+status.toLowerCase()+"', ";
				}
				if(StringUtils.isNotBlank(reason)) {
					sql+=" reason='"+reason+"', ";
				}
				sql+= " excluded="+excludeCount+", instance_id='"+instanceId+"'  where id = '" + id+"'";  
		
		return sql;
	}
	
	public void updateFailedRequestToQueued(String id,String file_loc,String status,
			String reason,int excludecount,int fileNooftimesProcess ,int allrecordExcude) throws Exception {

		String methodName = "[updateFailedRequestToQueued]";

		if (log.isDebugEnabled()) {
			log.debug(className + methodName + "begin id:" + id + " status:"
				+ status);
		}
		
		String instanceId = ExcludeProcessorPropertiesTon.getInstance()
				.getPropertiesConfiguration().getString(Constants.MONITORING_INSTANCE_ID);

		Connection con = null;
		PreparedStatement pstmt = null;
		String sql = "update campaign_file_splits set ";
		if(file_loc!=null && file_loc.trim().length() > 0){
			sql+=" file_loc='"+file_loc+"'";
		}
		if(allrecordExcude == 1) {   // 1 - file all number is exclude  and  0  is file have non exclude number
			sql+=" total="+excludecount+", ";
		}
		if(fileNooftimesProcess != -1) {
			sql+=" retry_count="+fileNooftimesProcess+", ";
		}
		if(StringUtils.isNotBlank(status)) {
			sql+=" status='"+status.toLowerCase()+"', ";
		}
		if(StringUtils.isNotBlank(reason)) {
			sql+=" reason='"+reason+"', ";
		}
		sql+= " excluded=?, instance_id = ? where id = ? "; 

		if (log.isDebugEnabled()) {
			log.debug(className + methodName + "sql = " + sql);
		}

		try {

			con = ConnectionFactoryForCMDB.getInstance().getConnection();
			pstmt = con.prepareStatement(sql);

			//pstmt.setString(1, status.toLowerCase());
			//pstmt.setString(2, reason);
			pstmt.setInt(1, excludecount);
			//pstmt.setInt(4, fileNooftimesProcess);
			pstmt.setString(2, instanceId);
			pstmt.setString(3, id);

			pstmt.executeUpdate();

		} catch (Exception sqlex) {
			log.error(className + methodName + "Exception: ",sqlex);
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
	
	
	
}
