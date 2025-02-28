package com.winnovature.groupsprocessor.handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.mobilevalidation.MobileNumberValidator;
import com.itextos.beacon.inmemdata.account.UserInfo;
import com.winnovature.groupsprocessor.daos.GroupRedisDAO;
import com.winnovature.groupsprocessor.daos.GroupsMasterDAO;
import com.winnovature.groupsprocessor.singletons.GroupsProcessorPropertiesTon;
import com.winnovature.groupsprocessor.utils.Constants;
import com.winnovature.groupsprocessor.utils.FileSender;
import com.winnovature.groupsprocessor.utils.Utility;
import com.winnovature.utils.utils.MobileValidator;
import com.winnovature.utils.utils.UserDetails;

public class SplitFileProcessor {

	static Log log = LogFactory.getLog(Constants.GroupsProcessorLogger);
	private static final String className = "[SplitFileProcessor]";

	private long batchSize = 500;
	private String redisDataFile = null;
	private int nameMaxLen = 20;
	private String emailRegExp = null;

	public void process(Map<String, String> mapObject) throws Exception {

		String methodName = "[process]";

		if (log.isDebugEnabled())
			log.debug(className + methodName + "Begin " + com.winnovature.utils.utils.Constants.FILEID_FOR_LOGGER
					+ mapObject.get("g_f_s_id"));
		GroupsMasterDAO groupsDAO = new GroupsMasterDAO();
		Instant startTime = Instant.now();
		String id = mapObject.get("g_f_s_id");
		String fileName = mapObject.get("fileloc");
		String groupId = mapObject.get("gm_id");
		String groupType = mapObject.get("g_type");
		String delimiter = mapObject.get("delimiter");
		String clientId = mapObject.get("cli_id");
		String instanceId = mapObject.get("instance_id");
		UserInfo userInfo = null;
		//Map<String, String> userDetails = null;
		boolean isIntlServiceEnabled = false;
		boolean considerDefaultLengthAsDomestic = false;
		boolean isDomesticSpecialSeriesAllow = false;
		try {
			groupsDAO.updateFileStartTime(id);
		} catch (Exception e) {
			log.error(className + methodName + com.winnovature.utils.utils.Constants.FILEID_FOR_LOGGER + id
					+ " Error while updating started_ts in group_file_splits ", e);
		}
		BufferedReader reader = null;

		List<String> validMobilesList = new ArrayList<String>();
		List<Map<String, String>> contactDetailsList = new ArrayList<Map<String, String>>();
		batchSize = Integer.parseInt(mapObject.get(Constants.REDIS_PUSH_BATCH_SIZE));
		String countryCode = null;
		PropertiesConfiguration props = null;
		try {
			props = GroupsProcessorPropertiesTon.getInstance().getPropertiesConfiguration();
			// CU-129
			nameMaxLen = props.getInt("contacts.name.max.length", 20);
			emailRegExp = props.getString("email.regex");
			
			userInfo = UserDetails.getUserInfo(clientId);
			if (log.isDebugEnabled()) {
				log.debug(className + methodName + " AccountDetails - "+ userInfo.getAccountDetails());
			}
			/*
			userDetails = new JsonUtility().convertJsonStringToMap(userInfo.getAccountDetails());
			if(userDetails!=null) {
				String res = userDetails.get("sms~international");
				if(StringUtils.isNotBlank(res)) {
					try {
						isIntlServiceEnabled = Integer.parseInt(res.trim()) == 1 ? true:false;
					}catch(Exception e) {}
				}
				res = userDetails.get("considerdefaultlength_as_domestic");
				if(StringUtils.isNotBlank(res)) {
					try {
						considerDefaultLengthAsDomestic = Integer.parseInt(res.trim()) == 1 ? true:false;
					}catch(Exception e) {}
				}
				
				try {
					isDomesticSpecialSeriesAllow = CommonUtility.isEnabled(CommonUtility.nullCheck(
							userDetails.get(MiddlewareConstant.MW_DOMESTIC_SPECIAL_SERIES_ALLOW.getName()), true));
				} catch (Exception e) {
					log.error(className + methodName + " " + e.getMessage());
				} catch (Throwable e) {
					log.error(className + methodName + " " + e.getMessage());
				}
			}
			
			countryCode = com.winnovature.utils.utils.Utility.getDefaultCountryCode1();
			*/
			
			Map<String, Object> info = com.winnovature.utils.utils.MobileValidator.getRequiredInfo(userInfo);
			isIntlServiceEnabled = (boolean) info.get("isIntlServiceEnabled");
			considerDefaultLengthAsDomestic = (boolean) info.get("considerDefaultLengthAsDomestic");
			isDomesticSpecialSeriesAllow = (boolean) info.get("isDomesticSpecialSeriesAllow");
			countryCode = (String) info.get("countryCode");
			
			File file = new File(fileName);
			int validCount = 0;
			int duplicateCount = 0;
			int invalidCount = 0;
			int total = -1;

			if (file.exists()) {
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
				redisDataFile = file.getParent() + File.separator + UUID.randomUUID().toString().replaceAll("-", "")
						+ ".txt";

				String line;
				while ((line = reader.readLine()) != null) {

					if (line != null) {
						line = line.trim();
					}

					if (total == -1) {
						// ignoring first row since it will have column indexes only
						total = 0;
						continue;
					}

					if (line.trim().length() == 0) {
						continue;
					}

					String[] nextLine = StringUtils.splitByWholeSeparatorPreserveAllTokens(line, delimiter);
					if (nextLine != null) {
						String mobile = "";
						total++;
						
						MobileNumberValidator mobileNumberValidator = null;
						if (StringUtils.isNotBlank(nextLine[1])) {
							mobileNumberValidator = MobileValidator.validate(nextLine[1].trim(),
									countryCode, isIntlServiceEnabled, considerDefaultLengthAsDomestic, isDomesticSpecialSeriesAllow);
						}

						if (mobileNumberValidator != null) {
							if (mobileNumberValidator.isValidMobileNumber()) {
								mobile = mobileNumberValidator.getMobileNumber();
							} else {
								invalidCount++;
								continue;
							}
						}else {
							invalidCount++;
							continue;
						}
						
						if (!validMobilesList.contains(mobile)) {
							validMobilesList.add(mobile);
						} else {
							duplicateCount++;
							continue;
						}

						nextLine[1] = mobile;
						Map<String, String> contactInfo = Utility.constructGroupDetails(nextLine);
						if (contactInfo.size() > 1) {
							contactDetailsList.add(contactInfo);
						}

						if (validMobilesList.size() == batchSize) {
							long successCount = pushGroupsToRedis(groupId, groupType, validMobilesList,
									contactDetailsList, clientId);
							validCount += successCount;
							duplicateCount += validMobilesList.size() - successCount;
							validMobilesList.clear();
							contactDetailsList.clear();
						}
					} // End of line read not null
				} // end of while

				if (validMobilesList.size() > 0) {
					long successCount = pushGroupsToRedis(groupId, groupType, validMobilesList, contactDetailsList, clientId);
					validCount += successCount;
					duplicateCount += validMobilesList.size() - successCount;
					validMobilesList.clear();
					contactDetailsList.clear();
				}
				// file processing completed, hence delete the redisDataFile
				removeFile(redisDataFile);
			} else {
				log.error(className + methodName + "File Doesn't exists");
			}

			String time = Utility.getTimeDifference(startTime) + " milli seconds.";

			if (log.isDebugEnabled()) {
				log.debug(className + methodName + " File processing end time= " + new Date().toString() + " file Name:"
						+ fileName + " file count: " + total + " no.of contacts inserted :" + validCount
						+ " :: Time taken to process file is " + time);
			}
			String status = Constants.PROCESS_STATUS_FAILED;
			String reason = (total == 0 || validCount == 0) ? "No valid numbers found in file" : null;
			/*
			if(validCount > 0 || total == duplicateCount || total == invalidCount) {
				status = Constants.PROCESS_STATUS_COMPLETED;
				reason = null;
			}
			*/
			if(validCount > 0 || total == (validCount+invalidCount+duplicateCount)) {
				status = Constants.PROCESS_STATUS_COMPLETED;
				if(total == duplicateCount) {
					reason = "all numbers in file were duplicates";
				}else if(total == invalidCount) {
					reason = "No valid numbers found in file";
				}else {
					reason = null;
				}
			}
			String sql = groupsDAO.updateProcessStatusSql(id, validCount, invalidCount, total, status, reason, duplicateCount, instanceId);
			boolean success = FileSender.sendToUpdateQueue(sql, id);
			if(!success) {
				GroupsMasterDAO.updateStatus(id, validCount, invalidCount, total, status, reason, duplicateCount, instanceId);
			}
		} // end of try block
		catch (Exception e) {
			log.error(className + methodName + "userInfo : "+userInfo);
			log.error(className + methodName + "Exception " + com.winnovature.utils.utils.Constants.FILEID_FOR_LOGGER
					+ id + " file Name:" + fileName, e);
			throw e;
		} finally {
			if (reader != null)
				reader.close();
		}
	}// end of method

	private long pushGroupsToRedis(String groupID, String groupIdentifier, List<String> mobileNumbers,
			List<Map<String, String>> groupDetailsList, String clientId) throws Exception {
		GroupRedisDAO groupRedisDAO = new GroupRedisDAO();
		long countsPushedToRedis = 0l;
		try {
			if (mobileNumbers.size() > 0) {
				// insert mobile numbers alone
				countsPushedToRedis = groupRedisDAO.pushToGroupDetailsRedis(groupID, mobileNumbers, groupIdentifier);
				// insert to redis data file for tracking
				writeToRedisDataFile(mobileNumbers, redisDataFile);
				// insert mobile number wise contact details
				groupRedisDAO.pushContactDetailsToRedis(groupID, groupDetailsList, groupIdentifier, nameMaxLen, emailRegExp);
			}
		} catch (Exception e) {
			removeInsertedDataFromRedis(groupID, groupIdentifier, clientId);
			throw e;
		}
		return countsPushedToRedis;
	}

	private void writeToRedisDataFile(List<String> mobileNumbers, String fileName) {
		FileWriter fw = null;
		try {
			fw = new FileWriter(fileName, true);
			for (String mobile : mobileNumbers) {
				fw.write(mobile);
				fw.write(System.lineSeparator());
			}
		} catch (Exception e) {
			log.error(className + " [writeToRedisDataFile]  Error while writing to redis data file ... ", e);
		} finally {
			try {
				fw.close();
			} catch (Exception e) {
				log.error(className + " [writeToRedisDataFile] ", e);
			}
		}
	}

	private void removeInsertedDataFromRedis(String groupID, String groupIdentifier, String clientId) throws Exception {
		try {
			if (redisDataFile != null && new File(redisDataFile).isFile()) {
				new GroupRedisDAO().removeFromGroupDetailsRedis(groupID, groupIdentifier, redisDataFile, batchSize);
				
				// data removed from redis hence no need to keep redisDataFile
				removeFile(redisDataFile);
			}
		} catch (Exception e) {
			throw e;
		}
	}

	private void removeFile(String filename) {
		try {
			if (filename != null) {
				new File(filename.trim()).delete();
			}
		} catch (Exception e) {
		}
	}

}
