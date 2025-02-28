
package com.winnovature.scheduleProcessor.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.winnovature.scheduleProcessor.singletons.ScheduleProcessorPropertiesTon;
import com.winnovature.scheduleProcessor.utils.Constants;
import com.winnovature.utils.singletons.ConnectionFactoryForCMDB;



public class CampaignScheduleMasterDAO {
	
	static Log log = LogFactory.getLog(Constants.ScheduleProcessorLogger);
	private static final String className = "[CampaignScheduleMasterDAO]";
	PropertiesConfiguration prop = null;
	
	

	public void pollcampScheduleMaster(String maxRetryCount) throws Exception {
		
		String methodName = " [pollCampaignScheduleMaster] ";
		PropertiesConfiguration prop = ScheduleProcessorPropertiesTon
				.getInstance().getPropertiesConfiguration();
		
		//String tpinstanceId = prop.getString(Constants.MONITORING_INSTANCE_ID);
		String status = prop.getString(Constants.ALL_STATUS_TO_POLL );
		String file_c_types = prop.getString(Constants.FILE_C_TYPES );
		String group_c_types = prop.getString(Constants.GROUP_C_TYPES );
		//String failedStatus = prop.getString(Constants.PROCESS_STATUS_FAILED );
		//String completedStatus = prop.getString(Constants.PROCESS_STATUS_COMPLETE );
		String limit = prop.getString(Constants.FETCH_CSAPOLL_ROW_LIMIT);
		
		long sleepTime = com.winnovature.utils.utils.Utility.getConsumersSleepTime();

		String sql = "select cm.id as cm_id,csa.retry_count as retry_count,csa.id as csa_id, cm.cli_id, cm.username, cm.c_name, "
				+ "cm.msg, cm.header, cm.filenames,cm.filenames_ori, cm.filesizes ,cm.filetypes ,cm.filelocs, cm.totals, cm.template_id, cm.template_type, "
				+ "cm.template_mobile_column,  cm.dlt_entity_id, cm.dlt_template_id, upper(cm.c_type) as c_type, cm.c_lang_type, cm.remove_dupe_yn, "
				+ "csa.cs_id , cm.ipaddr,cm.group_ids, cm.sessionid,  cm.created_ts,csa.scheduled_ts, cm.exclude_group_ids, csa.reason, cm.shorten_url, cm.intl_header  "
				+ "from cm.campaign_schedule_master cm inner join cm.campaign_schedule_at csa on cm.id = csa.cs_id and csa.scheduled_ts <= now() "
				+ "and lower(csa.status) in ("+ status.toLowerCase() +") and csa.retry_count <= ? LIMIT ? ";
		
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
			con = ConnectionFactoryForCMDB.getInstance().getConnection();
			
			pstmt = con.prepareStatement(sql);
			
			pstmt.setInt(1, Integer.parseInt(maxRetryCount));
			pstmt.setInt(2, Integer.parseInt(limit));
			
			rs = pstmt.executeQuery();
			
			boolean foundData = false;
			
			while (rs.next()) {
				
				foundData = true;
				
				Map<String, String> campScheduleMasterMap = new HashMap<String, String>();
				String cm_id = rs.getString("cm_id");
				
				int noOfRetry = rs.getInt("retry_count");
				campScheduleMasterMap.put("cm_id", cm_id);
				campScheduleMasterMap.put("cli_id", rs.getString("cli_id"));
				campScheduleMasterMap.put("csa_id", rs.getString("csa_id"));
				campScheduleMasterMap.put("c_name", rs.getString("c_name"));
				campScheduleMasterMap.put("header", rs.getString("header"));
				campScheduleMasterMap.put("intl_header", rs.getString("intl_header"));
				campScheduleMasterMap.put("ipaddr", rs.getString("ipaddr"));
				campScheduleMasterMap.put("sessionid", rs.getString("sessionid"));
				campScheduleMasterMap.put("remove_dupe_yn", rs.getString("remove_dupe_yn"));
				campScheduleMasterMap.put("msg", rs.getString("msg"));
				campScheduleMasterMap.put("c_lang_type", rs.getString("c_lang_type"));
				campScheduleMasterMap.put("c_type", rs.getString("c_type"));
				campScheduleMasterMap.put("filenames", rs.getString("filenames"));
				campScheduleMasterMap.put("filenames_ori", rs.getString("filenames_ori"));
				campScheduleMasterMap.put("filesizes", rs.getString("filesizes"));
				campScheduleMasterMap.put("filetypes", rs.getString("filetypes"));
				campScheduleMasterMap.put("filelocs", rs.getString("filelocs"));
				campScheduleMasterMap.put("total", rs.getString("totals"));
				campScheduleMasterMap.put("username", rs.getString("username"));
				campScheduleMasterMap.put("created_ts", rs.getString("created_ts"));
				if(rs.getString("scheduled_ts") != null && rs.getString("scheduled_ts").length() >0)
				{
					SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Date schdate = formatter.parse(rs.getString("scheduled_ts"));
					String strschdate = formatter.format(schdate);
					campScheduleMasterMap.put("scheduled_ts", strschdate);
				}else {
					campScheduleMasterMap.put("scheduled_ts", rs.getString("scheduled_ts"));
				}
				campScheduleMasterMap.put("exclude_group_ids", rs.getString("exclude_group_ids"));
				campScheduleMasterMap.put("retry_count", rs.getString("retry_count"));
				campScheduleMasterMap.put("group_ids", rs.getString("group_ids"));
				campScheduleMasterMap.put("template_id", rs.getString("template_id"));
				campScheduleMasterMap.put("template_type", rs.getString("template_type"));
				campScheduleMasterMap.put("template_mobile_column", rs.getString("template_mobile_column"));
				campScheduleMasterMap.put("dlt_entity_id", rs.getString("dlt_entity_id"));
				campScheduleMasterMap.put("dlt_template_id", rs.getString("dlt_template_id"));
				campScheduleMasterMap.put("reason", rs.getString("reason"));
				campScheduleMasterMap.put("shorten_url", rs.getString("shorten_url"));
				
				Connection conn1 = null;
				try {
						
						conn1 = ConnectionFactoryForCMDB.getInstance().getConnection();
						conn1.setAutoCommit(false);
						updateCSAInprocess(conn1,campScheduleMasterMap.get("csa_id"), 
								Constants.PROCESS_STATUS_INPROGRESS,
								noOfRetry);
						
						String cm_uuid = insertCampMasterFromScheduleCampaign(conn1, campScheduleMasterMap);
						
						if( (file_c_types).contains(campScheduleMasterMap.get("c_type")) ){
							insertCampaignFiles(conn1, cm_uuid, campScheduleMasterMap);
						}
						else if( (group_c_types).contains(campScheduleMasterMap.get("c_type")) ){
							insertCampaignGroups(conn1, cm_uuid, campScheduleMasterMap);
						}
						
						updateCSAInprocess(conn1,campScheduleMasterMap.get("csa_id"), 
								Constants.PROCESS_STATUS_COMPLETE,
								noOfRetry);
						
						campScheduleMasterMap.clear();
						conn1.commit();
					
				} catch (Exception e) {
					
					log.error(className + methodName + "Exception: inner ", e);
					conn1.rollback();
					updateCSAInprocess(conn1,campScheduleMasterMap.get("csa_id"), 
							Constants.PROCESS_STATUS_FAILED,
							noOfRetry+1);
					conn1.commit();
					//throw e;
				}finally{
					if (log.isDebugEnabled())
						log.debug("conn closed successfully "+campScheduleMasterMap.get("csa_id"));
					
					if(conn1 != null) {
						conn1.close();
					}
				}

			} // end of result set iteration
				
			// No data found let consumer rest for some time
			if(!foundData) {
				if (log.isDebugEnabled())
					log.debug(className + methodName + " No request found with matching criteria, sleeping for "+sleepTime+" milli seconds.");
				consumerSleep(sleepTime);
			}

		} catch (Exception e) {
			log.error(className + methodName + "Exception: main ");
			throw e;
		} finally {
			closeConnection(rs, pstmt, con);
		}
	}
	
	private String insertCampMasterFromScheduleCampaign(Connection conn1, Map<String, String> cmMap) throws Exception {
		String methodName = " [insertCampMasterFromScheduleCampaign] ";
			
		PreparedStatement pstmtInsertCM = null;
		conn1.setAutoCommit(false);

		String sql = "INSERT INTO cm.campaign_master(id, cli_id, username, c_name, msg, header, "
				+ "template_id, template_type,template_mobile_column, dlt_entity_id, dlt_template_id,    "
				+ "c_type, c_lang_type, c_lang, remove_dupe_yn, "
				+ " ipaddr, sessionid, scheduled_ts, shorten_url,intl_header) values "
				+ "(?,?,?,?,?,?, ?,?,?,?,?, ?,?,?,?, ?,?,?,?,?)";
		
		pstmtInsertCM = conn1.prepareStatement(sql);
		
		PropertiesConfiguration schedulerProperties = ScheduleProcessorPropertiesTon.getInstance().getPropertiesConfiguration();
		char[] alphabet = schedulerProperties.getString("random.id.chars", "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789")
				.toCharArray();
		int nonoIdSize = schedulerProperties.getInt("campaign.master.id.size", 36);
		
		//final String uuid = UUID.randomUUID().toString().replace("-", "");
		final String uuid = NanoIdUtils.randomNanoId(new Random(), alphabet, nonoIdSize);
	
		pstmtInsertCM.setString(1, uuid);
		pstmtInsertCM.setLong(2, Long.parseLong(cmMap.get("cli_id")));
		pstmtInsertCM.setString(3, cmMap.get("username"));
		pstmtInsertCM.setString(4, cmMap.get("c_name"));
		pstmtInsertCM.setString(5, cmMap.get("msg"));
		pstmtInsertCM.setString(6, cmMap.get("header"));
		pstmtInsertCM.setString(7, cmMap.get("template_id"));
		pstmtInsertCM.setString(8, cmMap.get("template_type"));
		pstmtInsertCM.setString(9, cmMap.get("template_mobile_column"));
		pstmtInsertCM.setString(10, cmMap.get("dlt_entity_id"));
		pstmtInsertCM.setString(11, cmMap.get("dlt_template_id"));
		pstmtInsertCM.setString(12, cmMap.get("c_type").trim().toLowerCase());
		pstmtInsertCM.setString(13, cmMap.get("c_lang_type"));
		pstmtInsertCM.setString(14, null);
		pstmtInsertCM.setInt(15, Integer.parseInt(cmMap.get("remove_dupe_yn")));
		pstmtInsertCM.setString(16, cmMap.get("ipaddr"));
		pstmtInsertCM.setString(17, cmMap.get("sessionid"));
		pstmtInsertCM.setString (18, cmMap.get("scheduled_ts"));
		pstmtInsertCM.setInt(19, Integer.parseInt(cmMap.get("shorten_url")));
		pstmtInsertCM.setString(20, cmMap.get("intl_header"));
		
		pstmtInsertCM.executeUpdate();
		return uuid;
	}
	
	private void insertCampaignFiles(Connection conn1, String cm_uuid, Map<String, String> cmMap) throws Exception {
		String[] splitFileNames =  cmMap.get("filenames")!=null?cmMap.get("filenames").split(","):null;
		int count = splitFileNames == null ? 0 : splitFileNames.length;
		String[] splitFileOrigNames =  cmMap.get("filenames_ori")!=null?cmMap.get("filenames_ori").split(","): null;
		String[] splitFileSizes =  cmMap.get("filesizes")!=null?cmMap.get("filesizes").split(","):null;
		String[] splitFileTypes =  cmMap.get("filetypes")!=null?cmMap.get("filetypes").split(","):null;
		String[] splitFileLocs =  cmMap.get("filelocs")!=null?cmMap.get("filelocs").split(","):null;
		String[] splitFileTot =  cmMap.get("total")!=null?cmMap.get("total").split(","):null;
		
		String methodName = " [insertCampaignFiles] ";
		PreparedStatement pstmtInsertCMFiles = null;
		
		PropertiesConfiguration schedulerProperties = ScheduleProcessorPropertiesTon.getInstance().getPropertiesConfiguration();
		
		char[] alphabet = schedulerProperties.getString("random.id.chars", "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789")
				.toCharArray();
		int nonoIdSize = schedulerProperties.getInt("campaign.files.id.size", 22);
		
		conn1.setAutoCommit(false);
		for(int i= 0;i <count; i++ ) {
			String sql = "INSERT INTO cm.campaign_files(id, c_id, "
						+ "filename, filename_ori, filesize, filetype, fileloc, total) values "
						+ "(?,?, ?,?,?,?,?,?)";
				
			pstmtInsertCMFiles = conn1.prepareStatement(sql);
			
			//final String uuid = UUID.randomUUID().toString().replace("-", "");
			final String uuid = NanoIdUtils.randomNanoId(new Random(), alphabet, nonoIdSize);
			pstmtInsertCMFiles.setString(1, uuid);
			pstmtInsertCMFiles.setString(2, cm_uuid);
			if(splitFileNames!=null && StringUtils.isNotBlank(splitFileNames[i])) {
				pstmtInsertCMFiles.setString(3, splitFileNames[i]);
			}else {
				pstmtInsertCMFiles.setNull(3, java.sql.Types.VARCHAR);
			}
			if(splitFileOrigNames!=null && StringUtils.isNotBlank(splitFileOrigNames[i])) {
				pstmtInsertCMFiles.setString(4, splitFileOrigNames[i]);
			}else {
				pstmtInsertCMFiles.setNull(4, java.sql.Types.VARCHAR);
			}
			
			if(splitFileSizes!=null && StringUtils.isNotBlank(splitFileSizes[i])) {
				pstmtInsertCMFiles.setString(5, splitFileSizes[i]);
			}else {
				pstmtInsertCMFiles.setNull(5, java.sql.Types.VARCHAR);
			}
			
			if(splitFileTypes!=null && StringUtils.isNotBlank(splitFileTypes[i])) {
				pstmtInsertCMFiles.setString(6, splitFileTypes[i]);
			}else {
				pstmtInsertCMFiles.setNull(6, java.sql.Types.VARCHAR);
			}
			
			if(splitFileLocs!=null && StringUtils.isNotBlank(splitFileLocs[i])) {
				pstmtInsertCMFiles.setString(7, splitFileLocs[i]);
			}else {
				pstmtInsertCMFiles.setNull(7, java.sql.Types.VARCHAR);
			}
			
			if(splitFileTot!=null && StringUtils.isNotBlank(splitFileTot[i])) {
				pstmtInsertCMFiles.setInt(8, Integer.parseInt(splitFileTot[i]));
			}else {
				pstmtInsertCMFiles.setNull(8, java.sql.Types.VARCHAR);
			}
			
			pstmtInsertCMFiles.executeUpdate();
			if(pstmtInsertCMFiles != null) {
				pstmtInsertCMFiles.close();
			}
		}
	}
	
	private void insertCampaignGroups(Connection conn1, String cm_uuid, Map<String, String> cmMapGroup) throws Exception{
		String[] splitGroupIds =  cmMapGroup.get("group_ids").split(",");
		//String[] splitExcGroupIds =  cmMapGroup.get("exclude_group_ids").split(",");
		int count = splitGroupIds.length;
		
		String methodName = " [insertCampaignGroups] ";
		
		PreparedStatement pstmtInsertCMGroups = null;
		
		conn1.setAutoCommit(false);
		for(int i= 0;i <count; i++ ) {
		
			String sql = "INSERT INTO cm.campaign_groups(id, c_id,group_id, exclude_group_ids) values (?,?, ?,?)";
			
			pstmtInsertCMGroups = conn1.prepareStatement(sql);
			
			final String uuid = UUID.randomUUID().toString()
				.replace("-", "");
			pstmtInsertCMGroups.setString(1, uuid);
			pstmtInsertCMGroups.setString(2, cm_uuid);
			pstmtInsertCMGroups.setString(3, splitGroupIds[i]);
			pstmtInsertCMGroups.setString(4, cmMapGroup.get("exclude_group_ids"));
		
			pstmtInsertCMGroups.executeUpdate();
			if(pstmtInsertCMGroups != null) {
				pstmtInsertCMGroups.close();
			}
		}
	}


	private void updateCSAInprocess(Connection conn1, String csaId, String statusToUpdate, int retry_count) throws Exception {
		String methodName = " [updateCSAInprocess] ";
		
		PreparedStatement pstmtUpdateCSA = null;
						
		conn1.setAutoCommit(false);
		
			String sql = "";
			if(statusToUpdate == Constants.PROCESS_STATUS_FAILED ) {
				sql = "update cm.campaign_schedule_at csa set csa.status = '"+statusToUpdate
						+"',retry_count ="+retry_count+" where csa.cs_id = ?";
			}
			else {
				sql = "update cm.campaign_schedule_at csa set csa.status = '"+statusToUpdate+"' where csa.id = ?";
			}
			
			pstmtUpdateCSA = conn1.prepareStatement(sql);
			pstmtUpdateCSA.setString(1, csaId); 
			pstmtUpdateCSA.executeUpdate();
			if(pstmtUpdateCSA != null) {
				pstmtUpdateCSA.close();
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
		if (log.isDebugEnabled()) {
			log.debug(className + "conn closed successfully"
					);
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
