package com.winnovature.dltfileprocessor.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.winnovature.dltfileprocessor.singletons.DltFileProcessorPropertiesTon;
import com.winnovature.dltfileprocessor.utils.Constants;
import com.winnovature.utils.singletons.ConnectionFactoryForAccountsDB;
import com.winnovature.utils.singletons.ConnectionFactoryForCMDB;
import com.winnovature.utils.utils.JsonUtility;
import com.winnovature.utils.utils.UnicodeUtil;
import com.winnovature.utils.utils.Utility;

public class DltTemplateMasterDAO {
	static Log log = LogFactory.getLog(Constants.FileUploadLogger);
	private static final String className = "[DltTemplateMasterDAO]";
	PropertiesConfiguration prop = null;

	public DltTemplateMasterDAO() {
	}

	public int insertIntoDltTemplateGroupHeaderEntityMapAndDltTemplateInfo(Map<String, String> fileData,
			Map<String, String> columnsMapping) throws Exception {
		String methodName = "[insertIntoDltTemplateGroupHeaderEntityMapAndDltTemplateInfo]";

		StringBuilder dltTemplateInfoInsertSQL = new StringBuilder(
				"insert into accounts.dlt_template_info (template_id,template_content,template_content_ori,pattern_type,template_type,created_ts,telemarketer,template_name,category,registered_dlt,registered_on,status_date,approval_status,status,consent_type,fileloc,is_static) ");
		dltTemplateInfoInsertSQL.append(" values (?,?,?,?,?,now(),?,?,?,?,?,?,?,?,?,?,?)");

		StringBuilder dltTemplateGroupHeaderEntityMapInsertSQL = new StringBuilder(
				"insert into accounts.dlt_template_group_header_entity_map (entity_id,template_group_id,header,template_id,is_numeric_header,cts) values (?,?,?,?,?, now()) ");

		Connection con = null;
		PreparedStatement pstmt = null;
		PreparedStatement pstmt1 = null;
		boolean continueProcess = true;

		int invalidCount = 0;
		boolean isTemplateIdDuplicate = false;
		boolean txnTempInfoSuccess = true, txnHeaderMapSuccess = true;

		try {
			prepareData(fileData, columnsMapping);

			if (fileData.get("template_id") != null && (fileData.get("template_id").toUpperCase().contains("E+")
					|| fileData.get("template_id").toUpperCase().contains("E-"))) {
				throw new Exception("invalid data");
			} else if (fileData.get("entity_id") != null && (fileData.get("entity_id").toUpperCase().contains("E+")
					|| fileData.get("entity_id").toUpperCase().contains("E-"))) {
				throw new Exception("invalid data");
			}
			
			if(!fileData.get("telco").equalsIgnoreCase("custom")) {
				// check if template is approved then only insert to template tables
				String approvalTexts = columnsMapping.get("approval_text");
				List<String> approvalVariants = Arrays.asList(StringUtils.split(approvalTexts, ","));
				approvalVariants.replaceAll(String::toLowerCase);

				if (StringUtils.isBlank(fileData.get("approval_status"))) {
					throw new Exception("invalid approval status");
				} else if (!approvalVariants.contains(fileData.get("approval_status").toLowerCase())) {
					throw new Exception("invalid approval status");
				}
			}
				
			if (StringUtils.isBlank(fileData.get("header"))) {
				throw new Exception("cannot be null");
			}

			con = ConnectionFactoryForAccountsDB.getInstance().getConnection();
			con.setAutoCommit(false);

			try {
				pstmt = con.prepareStatement(dltTemplateInfoInsertSQL.toString());

				pstmt.setString(1, fileData.get("template_id"));
				pstmt.setString(2, fileData.get("template_content"));
				pstmt.setString(3, fileData.get("template_content_ori"));
				pstmt.setInt(4, Integer.parseInt(fileData.get("pattern_type")));
				pstmt.setString(5, fileData.get("template_type"));
				pstmt.setString(6, fileData.get("telemarketer"));
				pstmt.setString(7, fileData.get("template_name"));
				pstmt.setString(8, fileData.get("category"));
				pstmt.setString(9, fileData.get("registered_dlt"));

				if (StringUtils.isNotBlank(fileData.get("registered_on"))) {
					pstmt.setString(10, fileData.get("registered_on"));
				} else {
					pstmt.setNull(10, Types.TIMESTAMP);
				}

				if (StringUtils.isNotBlank(fileData.get("status_date"))) {
					pstmt.setString(11, fileData.get("status_date"));
				} else {
					pstmt.setNull(11, Types.TIMESTAMP);
				}

				pstmt.setString(12, fileData.get("approval_status"));
				pstmt.setString(13, fileData.get("status"));
				pstmt.setString(14, fileData.get("consent_type"));
				pstmt.setString(15, fileData.get("fileloc"));
				pstmt.setString(16, fileData.get("is_static"));
				
				pstmt.executeUpdate();

			} catch (Exception e) {
				if (e.getMessage().toLowerCase().contains("duplicate entry")) {
					isTemplateIdDuplicate = true;
					log.error(className + methodName
							+ " duplicate entry found while inserting to dlt_template_info. Ignoring... data = "
							+ fileData);
				} else if (e.getMessage().toLowerCase().contains("data too long for column")
						|| e.getMessage().toLowerCase().contains("cannot be null")) {
					log.error(className + methodName
							+ " template_id/template_content are null or data too long for column while inserting to dlt_template_info. Treating as invalid row ... data = "
							+ fileData);
					try {
						continueProcess = false;
						txnTempInfoSuccess = false;
						String json = new JsonUtility().convertMapToJSON(fileData);
						Map<String, String> request = new HashMap<String, String>();
						request.put("dtf_id", fileData.get("dtf_id"));
						request.put("data_json", json);
						request.put("fileloc", fileData.get("fileloc"));
						storeInvalidRows(request);
					} catch (Exception e1) {
						log.error(className + methodName + " Exception storing invalid rows", e1);
					}
					invalidCount = 1;
				} else {
					// re-process this row
					log.error(className + methodName, e);
					throw e;
				}
			}

			if (continueProcess) {
				try {
					pstmt1 = con.prepareStatement(dltTemplateGroupHeaderEntityMapInsertSQL.toString());
					String header = fileData.get("header");
					List<String> headers = Arrays.asList(header.split(","));
					int headersInserted = 0;
					for (String hdr : headers) {
						try {
							pstmt1.setString(1, fileData.get("entity_id"));
							pstmt1.setString(2, fileData.get("template_group_id"));
							pstmt1.setString(3, hdr.trim());
							pstmt1.setString(4, fileData.get("template_id"));
							try {
								Integer.parseInt(hdr.trim());
								pstmt1.setInt(5, 1);
							}catch(Exception e) {
								pstmt1.setInt(5, 0);
							}
							pstmt1.executeUpdate();
							headersInserted++;
						} catch (Exception e) {
							if (e.getMessage().toLowerCase().contains("duplicate entry")) {
								log.error(className + methodName
										+ " duplicate entry found while inserting to dlt_template_group_header_entity_map. Ignoring...  Data  entity_id= "
										+ fileData.get("entity_id") + ", template_group_id= "
										+ fileData.get("template_group_id") + ", header=" + hdr.trim()
										+ ", template_id=" + fileData.get("template_id"));
							} else if (e.getMessage().toLowerCase().contains("data too long for column")
									|| e.getMessage().toLowerCase().contains("cannot be null")) {
								log.error(className + methodName
										+ " entity_id/template_group_id/header/template_id are null or data too long for column while inserting to dlt_template_group_header_entity_map. Treating as invalid row ... data = "
										+ fileData);
								try {
									txnHeaderMapSuccess = false;
									String json = new JsonUtility().convertMapToJSON(fileData);
									Map<String, String> request = new HashMap<String, String>();
									request.put("dtf_id", fileData.get("dtf_id"));
									request.put("data_json", json);
									request.put("fileloc", fileData.get("fileloc"));
									storeInvalidRows(request);
								} catch (Exception e1) {
									log.error(className + methodName + " Exception storing invalid rows", e1);
								}
								invalidCount = 1;
								con.rollback();
								break;
							} else {
								con.rollback();
								closeConnection(null, pstmt1, null);
								closeConnection(null, pstmt, con);
								throw e;
							}
						}
					}

					if (isTemplateIdDuplicate && headersInserted == 0 && invalidCount == 0) {
						fileData.put("is_duplicate", "true");
					}

				} catch (Exception e) {
					throw e;
				}
			}
			
			if(txnTempInfoSuccess && txnHeaderMapSuccess) {
				con.commit();
			}
		} catch (Exception e) {
			if (con != null && !con.isClosed()) {
				con.rollback();
			}
			if (e.getMessage().toLowerCase().contains("invalid approval status")
					|| e.getMessage().toLowerCase().contains("invalid data")
					|| e.getMessage().toLowerCase().contains("cannot be null")) {
				log.error(className + methodName
						+ " invalid approval status or null header. Treating as invalid row ... data = " + fileData);
				try {
					String json = new JsonUtility().convertMapToJSON(fileData);
					Map<String, String> request = new HashMap<String, String>();
					request.put("dtf_id", fileData.get("dtf_id"));
					request.put("data_json", json);
					request.put("fileloc", fileData.get("fileloc"));
					storeInvalidRows(request);
				} catch (Exception e1) {
					log.error(className + methodName + " Exception storing invalid rows", e1);
				}
				invalidCount = 1;
			} else {
				log.error(className + methodName, e);
				throw e;
			}
		} finally {
			try {
				closeConnection(null, pstmt1, null);
				closeConnection(null, pstmt, con);
			} catch (Exception e) {
				log.error(className + methodName, e);
			}
		}
		return invalidCount;
	}

	/*
	
	public int insertIntoDltTemplateMasterAndUserHeaders(Map<String, String> fileData,
			Map<String, String> columnsMapping, List<String> clientIds) throws Exception {
		String methodName = "[insertIntoDltTemplateMasterAndUserHeaders]";
		StringBuilder dltTemplateGroupHeaderEntityMapInsertSQL = new StringBuilder(
				"insert into accounts.dlt_template_group_header_entity_map (entity_id,template_group_id,header,template_id) values (?,?,?,?) ");

		StringBuilder dltTemplateInfoInsertSQL = new StringBuilder(
				"insert into accounts.dlt_template_info (template_id,template_content,template_content_ori,pattern_type,template_type,created_ts,telemarketer,template_name,category,registered_dlt,registered_on,status_date,approval_status,status,consent_type,fileloc) ");
		dltTemplateInfoInsertSQL.append(" values (?,?,?,?,?,now(),?,?,?,?,?,?,?,?,?,?)");

		Connection con = null;
		PreparedStatement pstmt = null;
		PreparedStatement pstmt1 = null;

		int invalidCount = 0;

		try {
			prepareData(fileData, columnsMapping);

			// check if template is approved then only insert to dlt_template_master
			String approvalTexts = columnsMapping.get("approval_text");
			List<String> approvalVariants = Arrays.asList(StringUtils.split(approvalTexts, ","));
			approvalVariants.replaceAll(String::toLowerCase);

			if (StringUtils.isBlank(fileData.get("approval_status"))) {
				throw new Exception("invalid approval status");
			} else if (!approvalVariants.contains(fileData.get("approval_status").toLowerCase())) {
				throw new Exception("invalid approval status");
			}

			con = ConnectionFactoryForAccountsDB.getInstance().getConnection();
			con.setAutoCommit(false);
			pstmt = con.prepareStatement(dltTemplateGroupHeaderEntityMapInsertSQL.toString());

			pstmt.setString(1, fileData.get("entity_id"));
			pstmt.setString(2, fileData.get("template_group_id"));
			pstmt.setString(3, fileData.get("header"));
			pstmt.setString(4, fileData.get("template_id"));
			pstmt.setString(5, fileData.get("template_content"));
			pstmt.setString(6, fileData.get("template_content_ori"));
			pstmt.setInt(7, Integer.parseInt(fileData.get("pattern_type")));
			pstmt.setString(8, fileData.get("template_type"));
			pstmt.setString(9, fileData.get("telemarketer"));
			pstmt.setString(10, fileData.get("template_name"));
			pstmt.setString(11, fileData.get("category"));
			pstmt.setString(12, fileData.get("registered_dlt"));

			if (StringUtils.isNotBlank(fileData.get("registered_on"))) {
				pstmt.setString(13, fileData.get("registered_on"));
			} else {
				pstmt.setNull(13, Types.TIMESTAMP);
			}

			if (StringUtils.isNotBlank(fileData.get("status_date"))) {
				pstmt.setString(14, fileData.get("status_date"));
			} else {
				pstmt.setNull(14, Types.TIMESTAMP);
			}

			pstmt.setString(15, fileData.get("approval_status"));
			pstmt.setString(16, fileData.get("status"));
			pstmt.setString(17, fileData.get("consent_type"));
			pstmt.setString(18, fileData.get("fileloc"));

			if (pstmt.executeUpdate() > 0) {
				String userHeadersSql = "insert into accounts.user_headers (cli_id,header,entity_id,created_user) values(?,?,?,?) ";
				pstmt1 = con.prepareStatement(userHeadersSql);
				String header = fileData.get("header");
				List<String> headers = Arrays.asList(header.split(","));

				for (String cliId : clientIds) {
					for (String hdr : headers) {
						try {
							pstmt1.setString(1, cliId);
							pstmt1.setString(2, hdr.trim());
							pstmt1.setString(3, fileData.get("entity_id"));
							pstmt1.setString(4, fileData.get("cli_id"));
							pstmt1.executeUpdate();
						} catch (Exception e) {
							if (e.getMessage().toLowerCase().contains("duplicate entry")) {
								log.error(className + methodName
										+ " duplicate entry found while inserting to user_headers. Ignoring...  info = "
										+ e.getMessage());
							} else {
								con.rollback();
								closeConnection(null, pstmt1, null);
								closeConnection(null, pstmt, con);
								throw e;
							}
						}
					}
				}
				con.commit();
			} else {
				con.rollback();
				throw new Exception(
						"Something wrong while inserting to dlt_template_master. Rollingback insertions...");
			}
		} catch (Exception e) {
			if (con != null && !con.isClosed()) {
				con.rollback();
			}
			if (e.getMessage().toLowerCase().contains("duplicate entry")) {
				log.error(className + methodName
						+ " duplicate entry found while inserting to dlt_template_master. Ignoring... data = "
						+ fileData);
				fileData.put("is_duplicate", "true");
			} else if (e.getMessage().toLowerCase().contains("data too long for column")
					|| e.getMessage().toLowerCase().contains("cannot be null")
					|| e.getMessage().toLowerCase().contains("invalid approval status")) {
				log.error(className + methodName
						+ " header/tempid/content are null or invalid approval status or data too long for column while inserting to dlt_template_master. Treating as invalid row ... data = "
						+ fileData);
				// NOTE - header/tempid/content are too big. storing them to table for debugging
				try {
					String json = new JsonUtility().convertMapToJSON(fileData);
					Map<String, String> request = new HashMap<String, String>();
					request.put("dtf_id", fileData.get("dtf_id"));
					request.put("data_json", json);
					request.put("fileloc", fileData.get("fileloc"));
					storeInvalidRows(request);
				} catch (Exception e1) {
					log.error(className + methodName + " Exception storing invalid rows", e1);
				}
				invalidCount = 1;
			} else {
				log.error(className + methodName, e);
				throw e;
			}
		} finally {
			try {
				closeConnection(null, pstmt1, null);
				closeConnection(null, pstmt, con);
			} catch (Exception e) {
				log.error(className + methodName, e);
			}
		}
		return invalidCount;
	}

	*/
	
	private void prepareData(Map<String, String> fileData, Map<String, String> columnsMapping) throws Exception {

		if (StringUtils.isBlank(fileData.get(columnsMapping.get("header")))) {
			fileData.put("header", null);
		} else {
			fileData.put("header", fileData.get(columnsMapping.get("header")).trim());
		}

		String template_id = fileData.get(columnsMapping.get("template_id"));
		if (StringUtils.isNotBlank(template_id)) {
			template_id = template_id.trim();
			template_id = template_id.replace("'", "");
			template_id = template_id.replaceAll("'", "");
		}
		fileData.put("template_id", template_id);

		if (StringUtils.isBlank(fileData.get(columnsMapping.get("template_content")))) {
			fileData.put("template_content_ori", null);
		} else {
			fileData.put("template_content_ori", fileData.get(columnsMapping.get("template_content")).trim());
		}

		fileData.put("template_type", fileData.get(columnsMapping.get("template_type")));
		fileData.put("telemarketer", fileData.get(columnsMapping.get("telemarketer")));
		fileData.put("template_name", fileData.get(columnsMapping.get("template_name")));
		fileData.put("category", fileData.get(columnsMapping.get("category")));
		fileData.put("registered_dlt", fileData.get(columnsMapping.get("registered_dlt")));
		fileData.put("approval_status", fileData.get(columnsMapping.get("approval_status")));
		fileData.put("status", fileData.get(columnsMapping.get("status")));
		fileData.put("consent_type", fileData.get(columnsMapping.get("consent_type")));

		if (fileData.get("entity_id") != null && fileData.get("entity_id").length() > 19) {
			fileData.put("entity_id", fileData.get("entity_id").substring(0, 19));
		}

		if (fileData.get("template_type") != null && fileData.get("template_type").length() > 30) {
			fileData.put("template_type", fileData.get("template_type").substring(0, 30));
		}

		if (fileData.get("telemarketer") != null && fileData.get("telemarketer").length() > 100) {
			fileData.put("telemarketer", fileData.get("telemarketer").substring(0, 100));
		}

		if (fileData.get("template_name") != null && fileData.get("template_name").length() > 100) {
			fileData.put("template_name", fileData.get("template_name").substring(0, 100));
		}

		if (fileData.get("category") != null && fileData.get("category").length() > 100) {
			fileData.put("category", fileData.get("category").substring(0, 100));
		}

		if (fileData.get("registered_dlt") != null && fileData.get("registered_dlt").length() > 100) {
			fileData.put("registered_dlt", fileData.get("registered_dlt").substring(0, 100));
		}

		if (fileData.get("approval_status") != null && fileData.get("approval_status").length() > 100) {
			fileData.put("approval_status", fileData.get("approval_status").substring(0, 100));
		}

		if (fileData.get("status") != null && fileData.get("status").length() > 50) {
			fileData.put("status", fileData.get("status").substring(0, 50));
		}

		if (fileData.get("consent_type") != null && fileData.get("consent_type").length() > 50) {
			fileData.put("consent_type", fileData.get("consent_type").substring(0, 50));
		}

		String sourceFormat = columnsMapping.get("datetime_format");
		String targetFormat = "yyyy-MM-dd HH:mm:ss";

		if (sourceFormat != null && columnsMapping.get("registered_on") != null
				&& fileData.get(columnsMapping.get("registered_on")) != null) {
			String registered_on = Utility.convertDateFormat(sourceFormat, targetFormat,
					fileData.get(columnsMapping.get("registered_on")));
			fileData.put("registered_on", registered_on);
		}

		if (sourceFormat != null && columnsMapping.get("status_date") != null
				&& fileData.get(columnsMapping.get("status_date")) != null) {
			String status_date = Utility.convertDateFormat(sourceFormat, targetFormat,
					fileData.get(columnsMapping.get("status_date")));
			fileData.put("status_date", status_date);
		}

		fileData.put("is_static", "1");
		
		if (StringUtils.isBlank(fileData.get(columnsMapping.get("template_content")))) {
			fileData.put("template_content", null);
			fileData.put("pattern_type", "0");
		} else {
			String template_content = fileData.get(columnsMapping.get("template_content")).trim();
			fileData.put("template_content", template_content);
			if (fileData.get("template_content") != null
					&& UnicodeUtil.INSTANCE.isUnicode(fileData.get("template_content"))) {
				//fileData.put("template_content",
						//UnicodeUtil.INSTANCE.convertToHexStringIgnoringPlaceholders(fileData.get("template_content")));
				fileData.put("pattern_type", "1");
			} else {
				fileData.put("pattern_type", "0");
			}
			
			if(StringUtils.containsIgnoreCase(template_content, "{#var#}")) {
				fileData.put("is_static", "0");
			}
		}
				
	}

	public void storeInvalidRows(Map<String, String> requestMap) {
		String methodName = "[storeInvalidRows]";
		StringBuilder sql = new StringBuilder(
				"insert into cm.dlt_template_invalid_rows (id,dtf_id,data_json,file_loc) values (?,?,?,?) ");

		Connection con = null;
		PreparedStatement pstmt = null;

		try {
			con = ConnectionFactoryForCMDB.getInstance().getConnection();
			pstmt = con.prepareStatement(sql.toString());

			PropertiesConfiguration prop = DltFileProcessorPropertiesTon.getInstance().getPropertiesConfiguration();

			char[] alphabet = prop
					.getString("random.id.chars", "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789")
					.toCharArray();
			int nonoIdSize = prop.getInt("dlt.template.files.id.size", 36);
			String id = NanoIdUtils.randomNanoId(new Random(), alphabet, nonoIdSize);

			pstmt.setString(1, id);
			pstmt.setString(2, requestMap.get("dtf_id"));
			pstmt.setString(3, requestMap.get("data_json"));
			pstmt.setString(4, requestMap.get("fileloc"));

			pstmt.executeUpdate();

		} catch (Exception e) {
			log.error(className + methodName, e);
		} finally {
			try {
				closeConnection(null, pstmt, con);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void closeConnection(ResultSet rs, PreparedStatement ps, Connection con) throws Exception {
		if (rs != null) {
			rs.close();
		}
		if (ps != null) {
			ps.close();
		}
		if (con != null && !con.isClosed()) {
			con.close();
		}
	}

}
