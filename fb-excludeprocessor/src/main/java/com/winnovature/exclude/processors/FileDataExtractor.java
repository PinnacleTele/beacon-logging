package com.winnovature.exclude.processors;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.mobilevalidation.MobileNumberValidator;
import com.itextos.beacon.inmemdata.account.UserInfo;
import com.winnovature.exclude.singletons.ExcludeProcessorPropertiesTon;
import com.winnovature.exclude.utils.Constants;
import com.winnovature.utils.singletons.ConfigParamsTon;
import com.winnovature.utils.utils.MobileValidator;
import com.winnovature.utils.utils.UserDetails;

public class FileDataExtractor {

	static Log log = LogFactory.getLog(Constants.ExcludeLogger);
	private static final String className = "[FileDataExtractor] ";

	public Map<String, String> process(String fileType, File file, String excludegroupIds, String delimiter,
			String clientId, String fileid, String id, String type, String username, String cm_id) throws Exception {

		String methodName = " [process] ";
		methodName += com.winnovature.utils.utils.Constants.FILEID_FOR_LOGGER + id + " ";

		ExcludeGroupsMatcher exclusionGroupsMatcher = new ExcludeGroupsMatcher();
		//String inputFileType = null;
		BufferedReader reader = null;
		Map<String, String> totalMap = new HashMap<String, String>();
		int total = 0;
		int excludeCnt = 0;
		FileWriter fw = null;
		FileWriter excfw = null;
		Map<String, String> configParamsTon = null;
		String tempfileName = "";
		String excludefileName = "";
		UserInfo userInfo = null;
		//Map<String, String> userDetails = null;
		boolean isIntlServiceEnabled = false;
		boolean considerDefaultLengthAsDomestic = false;
		String countryCode = null;
		boolean isDomesticSpecialSeriesAllow = false;
		try {
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
			
			countryCode = Utility.getDefaultCountryCode1();
			*/
			
			Map<String, Object> info = MobileValidator.getRequiredInfo(userInfo);
			isIntlServiceEnabled = (boolean) info.get("isIntlServiceEnabled");
			considerDefaultLengthAsDomestic = (boolean) info.get("considerDefaultLengthAsDomestic");
			isDomesticSpecialSeriesAllow = (boolean) info.get("isDomesticSpecialSeriesAllow");
			countryCode = (String) info.get("countryCode");

			PropertiesConfiguration prop = ExcludeProcessorPropertiesTon.getInstance().getPropertiesConfiguration();

			int batchcount = prop.getInt(Constants.BATCH_UPDATE_COUNT);
			configParamsTon = ConfigParamsTon.getInstance().getConfigurationFromconfigParams();

			String fileStorePath = configParamsTon.get(com.winnovature.utils.utils.Constants.CAMPAIGNS_FILE_STORE_PATH);
			fileStorePath = fileStorePath + username.toLowerCase() + File.separator;

			String excludefileStorePath = fileStorePath;

			if (fileType.equalsIgnoreCase("txt") || fileType.equalsIgnoreCase("csv")) {
				if (log.isDebugEnabled()) {
					log.debug(className + methodName + " reading file = " + file);
				}

				reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

				String line;
				String strDate = com.winnovature.utils.utils.Utility.getCustomDateAsString("yyyyMMddHHmmssSSS");
				String fileName = file.getName();
				fileName = fileName.substring(0, fileName.lastIndexOf("."))+ "_" + strDate;
				tempfileName = fileStorePath + fileName + "." + fileType;
				
				excludefileName = excludefileStorePath + "Exclude_" + cm_id + "_" + strDate + "." + fileType;
				File createFile = new File(tempfileName);
				fw = new FileWriter(createFile);
				File excludeFile = new File(excludefileName);
				excfw = new FileWriter(excludeFile);
				List<Map<String, String>> nonExcludeNo = new ArrayList<Map<String, String>>();
				List<Map<String, String>> excludeNo = new ArrayList<Map<String, String>>();
				List<Map<String, String>> numbersToBeChecked = new ArrayList<Map<String, String>>();
				// List<UnprocessRow> lstUnProcessRow = new ArrayList<UnprocessRow>();
				while ((line = reader.readLine()) != null) {
					if (line != null) {
						line = line.trim();
					}
					if (line.trim().length() == 0) {
						continue;
					}

					String mobileNo = "";
					String value = "";
					String msgType = "";
					Map<String, String> map = new HashMap<String, String>();

					if (type.equalsIgnoreCase(com.winnovature.utils.utils.Constants.OTM_CAMP)
							|| type.equalsIgnoreCase(com.winnovature.utils.utils.Constants.GROUP_CAMP)
							|| type.equalsIgnoreCase(com.winnovature.utils.utils.Constants.QUICK_CAMP)) {
						// fom
						mobileNo = line.trim();
						map.put("mobile", mobileNo);
					} else {
						// for fmm,ftm
						String[] stkTokens = StringUtils.splitByWholeSeparatorPreserveAllTokens(line, delimiter);
						if (stkTokens != null && stkTokens.length >= 2) {
							msgType = stkTokens[0].trim();
							mobileNo = stkTokens[1].trim();
							String message = "";
							for (int i = 2; i < stkTokens.length; i++) {
								String data = stkTokens[i];
								if (i == 2) {
									message = message + data.trim();
								} else {
									message = message + delimiter + data.trim();
								}
							}
							value = message;
							if ((mobileNo.trim().length() == 0) && (message.trim().length() == 0
									|| message.replace(delimiter, "").trim().length() == 0)) {
								continue;
							}
						}
						map.put("msgType", msgType);
						map.put("mobile", mobileNo);
						map.put("value", value);
					}

					MobileNumberValidator mobileNumberValidator = null;
					if (StringUtils.isNotBlank(mobileNo)) {
						mobileNumberValidator = MobileValidator.validate(mobileNo.trim(),
								countryCode, isIntlServiceEnabled, considerDefaultLengthAsDomestic, isDomesticSpecialSeriesAllow);
					}

					if (mobileNumberValidator != null) {
						if (mobileNumberValidator.isValidMobileNumber()) {
							mobileNo = mobileNumberValidator.getMobileNumber();
							map.put("mobile", mobileNo);
							numbersToBeChecked.add(map);
						} else {
							nonExcludeNo.add(map);
						}
					} else {
						nonExcludeNo.add(map);
					}
					
					if (numbersToBeChecked.size() >= batchcount) {
						List<Map<String, String>> exclude = checkExcludeGroups(numbersToBeChecked, excludegroupIds,
								exclusionGroupsMatcher);
						if (exclude.size() > 0) {
							excludeNo.addAll(exclude);
						}
						if (numbersToBeChecked.size() > 0) {
							nonExcludeNo.addAll(numbersToBeChecked);
							total = total + numbersToBeChecked.size();
						}
						for (int i = 0; i < nonExcludeNo.size(); i++) {
							Map<String, String> copyMap = (Map<String, String>) nonExcludeNo.get(i);
							if (type.equalsIgnoreCase(com.winnovature.utils.utils.Constants.OTM_CAMP)
									|| type.equalsIgnoreCase(com.winnovature.utils.utils.Constants.GROUP_CAMP)
									|| type.equalsIgnoreCase(com.winnovature.utils.utils.Constants.QUICK_CAMP)) {
								fw.write(copyMap.get("mobile").toString() + "\n");
							} else {
								fw.write(
										copyMap.get("msgType").toString() + delimiter + copyMap.get("mobile").toString()
												+ delimiter + copyMap.get("value").toString() + "\n");
							}
						}

						if (excludeNo.size() > 0) {
							excludeCnt = excludeCnt + excludeNo.size();
							for (int i = 0; i < excludeNo.size(); i++) {
								Map<String, String> copyMap = (Map<String, String>) excludeNo.get(i);
								excfw.write(copyMap.get("mobile").toString() + "\n");
							}
						}
						// reset Non Exclude and Exclude
						excludeNo.clear();
						nonExcludeNo.clear();
						numbersToBeChecked.clear();
					}
				} // end of while

				if (numbersToBeChecked.size() > 0) {
					List<Map<String, String>> exclude = checkExcludeGroups(numbersToBeChecked, excludegroupIds,
							exclusionGroupsMatcher);
					if (exclude.size() > 0) {
						excludeNo.addAll(exclude);
					}
					if (numbersToBeChecked.size() > 0) {
						nonExcludeNo.addAll(numbersToBeChecked);
						total = total + numbersToBeChecked.size();
					}

					for (int i = 0; i < nonExcludeNo.size(); i++) {
						Map<String, String> copyMap = (Map<String, String>) nonExcludeNo.get(i);
						if (type.equalsIgnoreCase(com.winnovature.utils.utils.Constants.OTM_CAMP)
								|| type.equalsIgnoreCase(com.winnovature.utils.utils.Constants.GROUP_CAMP)
								|| type.equalsIgnoreCase(com.winnovature.utils.utils.Constants.QUICK_CAMP)) {
							fw.write(copyMap.get("mobile").toString() + "\n");
						} else {
							fw.write(copyMap.get("msgType").toString() + delimiter + copyMap.get("mobile").toString()
									+ delimiter + copyMap.get("value").toString() + "\n");
						}

					}
				}

				if (excludeNo.size() > 0) {
					excludeCnt = excludeCnt + excludeNo.size();
					for (int i = 0; i < excludeNo.size(); i++) {
						Map<String, String> copyMap = (Map<String, String>) excludeNo.get(i);
						excfw.write(copyMap.get("mobile").toString() + "\n");
					}
				}

				totalMap.put("total", total + "");
				totalMap.put("file_loc", tempfileName);
				totalMap.put("excludeCnt", excludeCnt + "");
				
				// reset Non Exclude and Exclude
				excludeNo.clear();
				nonExcludeNo.clear();
				numbersToBeChecked.clear();

				if (fw != null) {
					fw.close();
				}
				if (excfw != null) {
					excfw.close();
				}
				if (reader != null) {
					reader.close();
				}
				if (excludeCnt == 0) {
					excludeFile.delete();
					if (log.isDebugEnabled()) {
						log.debug(" Exclude number count is 0 So Delete the empty file. File is  " + excludefileName);
					}
					totalMap.put("exc_file_loc", "");
				} else {
					totalMap.put("exc_file_loc", excludefileName);
				}
			} // .txt process ends

			return totalMap;
		} catch (Exception e) {
			log.error(className + methodName + "userInfo : "+userInfo);
			log.error(className + methodName + "Exception: ", e);
			if (reader != null) {
				reader.close();
			}
			if (fw != null) {
				fw.close();
			}
			if (excfw != null) {
				excfw.close();
			}

			throw e;
		} finally {
			if (reader != null) {
				reader.close();
			}
			if (excfw != null) {
				excfw.close();
			}
			if (fw != null) {
				fw.close();
			}
		}
	}

	public static List<Map<String, String>> checkExcludeGroups(List<Map<String, String>> numbersToBeChecked,
			String excludeGrpIds, ExcludeGroupsMatcher exclusionGroupsMatcher) throws Exception {
		String[] excludeGrpIdsArray = excludeGrpIds.split("~");
		List<Map<String, String>> exclude = new ArrayList<Map<String, String>>();
		for (String excludeGrpId : excludeGrpIdsArray) {
			List<Boolean> response = exclusionGroupsMatcher.findExcludeNumbers(excludeGrpId, numbersToBeChecked);
			for (int i = response.size()-1; i >= 0; i--) {
				if (response.get(i)) {
					exclude.add(numbersToBeChecked.remove(i));
				}
			}
		}
		return exclude;
	}

	public static boolean checkExcludeGroups(String mobile, String excludeGrpIds,
			ExcludeGroupsMatcher exclusionGroupsMatcher) throws Exception {
		boolean excludeNumber = false;
		String[] excludeGrpIdsArray = excludeGrpIds.split("~");
		for (String excludeGrpId : excludeGrpIdsArray) {
			if (exclusionGroupsMatcher.isExcludeNumber(excludeGrpId, mobile)) {
				excludeNumber = true;
			}
			if (excludeNumber)
				break;
		}
		return excludeNumber;
	}

}