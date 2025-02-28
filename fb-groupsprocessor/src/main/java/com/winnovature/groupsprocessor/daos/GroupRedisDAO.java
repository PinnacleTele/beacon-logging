package com.winnovature.groupsprocessor.daos;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.groupsprocessor.singletons.RedisConnectionTon;
import com.winnovature.groupsprocessor.utils.Constants;
import com.winnovature.utils.utils.EmailValidator;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

public class GroupRedisDAO {
	static Log log = LogFactory.getLog(Constants.GroupsProcessorLogger);
	private final String className = "[SplitFileProcessor]";

	public long pushToGroupDetailsRedis(String groupID, List<String> numbers, String groupIdentifier) throws Exception {
		long count = 0L;
		Jedis connection = null;
		String key = null;
		try {
			if (groupIdentifier.equalsIgnoreCase(Constants.GROUP_IDENTIFIER_NORMAL)) {
				key = Constants.REDIS_QUEUE_NORMAL_GROUPS;
				connection = RedisConnectionTon.getInstance().getNormalGroupRedisConnection();
			} else {
				key = Constants.REDIS_QUEUE_EXCLUDE_GROUPS;
				connection = RedisConnectionTon.getInstance().getExcludeGroupRedisConnection();
			}
			key = key.replace("group_id", groupID);
			String[] arr = new String[numbers.size()];
			arr = numbers.toArray(arr);
			count = connection.sadd(key, arr);
			if (log.isDebugEnabled()) {
				log.debug("[GroupRedisDAO] [pushToGroupDetailsRedis] inserted " + count
						+ " numbers in mobile-numbers-redis with key " + key);
			}
		} catch (Exception e) {
			log.error("Error while adding number in mobile-numbers-redis,", e);
			throw e;
		} finally {
			if (connection != null)
				try {
					connection.close();
				} catch (Exception e) {
					log.error("Error closing redis connection,", e);
				}
		}
		return count;
	}

	public long pushContactDetailsToRedis(String groupID, List<Map<String, String>> groupDetailsList,
			String groupIdentifier, int nameMaxLen, String emailRegExp) throws Exception {
		Jedis connection = null;
		String key = null;
		Map<String, String> mobileWiseContactDetails = new HashMap<String, String>();
		try {
			if (groupIdentifier.equalsIgnoreCase(Constants.GROUP_IDENTIFIER_NORMAL)) {
				key = Constants.REDIS_QUEUE_GROUPS_CONTACT_DETAILS;
				connection = RedisConnectionTon.getInstance().getNormalGroupRedisConnection();
			} else {
				key = Constants.REDIS_QUEUE_EXCLUDE_GROUPS_CONTACT_DETAILS;
				connection = RedisConnectionTon.getInstance().getExcludeGroupRedisConnection();
			}
			key = key.replace("group_id", groupID);
			// creating copy of original list
			List<Map<String, String>> groupDetailsList1 = new ArrayList<Map<String, String>>();
			for (Map<String, String> map : groupDetailsList) {
				groupDetailsList1.add(new HashMap<String, String>(map));
			}

			for (Map<String, String> contactDetails : groupDetailsList1) {
				String mobile = contactDetails.remove("mobile");
				String details = "";
				if (StringUtils.isNotBlank(contactDetails.get("name"))) {
					// CU-129
					String name = contactDetails.get("name").trim();
					if(name.length() > nameMaxLen) {
						name = name.substring(0, nameMaxLen);
					}
					details = name.trim();
				}
				
				details = details + "~";
				if (StringUtils.isNotBlank(contactDetails.get("email"))) {
					String email = contactDetails.get("email").trim();
					if(EmailValidator.validate(email, emailRegExp)) {
						details = details + email;
					}
				}
				
				if (StringUtils.isNotBlank(details) && details.trim().length() > 1) {
					mobileWiseContactDetails.put(mobile, details);
				}
			}
			if (mobileWiseContactDetails.size() > 0) {
				connection.hmset(key, mobileWiseContactDetails);
				if (log.isDebugEnabled()) {
					log.debug("[GroupRedisDAO] [pushContactDetailsToRedis] inserted/updated "
							+ mobileWiseContactDetails.size() + " numbers in contact-details-redis with key " + key);
				}
			}
		} catch (Exception e) {
			log.error("Error while adding number in contact-details-redis,", e);
			throw e;
		} finally {
			if (connection != null)
				try {
					connection.close();
				} catch (Exception e) {
					log.error("Error closing redis connection,", e);
				}
		}
		return mobileWiseContactDetails.size();
	}

	public long removeFromGroupDetailsRedis(String groupID, String groupIdentifier, String fileName, long batchSize)
			throws Exception {
		long count = 0L, count1 = 0l, count2 = 0l;
		Jedis connection = null;
		List<String> numbers = new ArrayList<String>();
		BufferedReader reader = null;
		try {
			if (groupIdentifier.equalsIgnoreCase(Constants.GROUP_IDENTIFIER_NORMAL)) {
				connection = RedisConnectionTon.getInstance().getNormalGroupRedisConnection();
			} else {
				connection = RedisConnectionTon.getInstance().getExcludeGroupRedisConnection();
			}
			String line = null;
			reader = new BufferedReader(new FileReader(fileName));
			while ((line = reader.readLine()) != null) {
				numbers.add(line);
				if (numbers.size() == batchSize) {
					String[] arr = new String[numbers.size()];
					arr = numbers.toArray(arr);
					String key = groupIdentifier.equalsIgnoreCase(Constants.GROUP_IDENTIFIER_NORMAL)
							? Constants.REDIS_QUEUE_NORMAL_GROUPS
							: Constants.REDIS_QUEUE_EXCLUDE_GROUPS;
					key = key.replace("group_id", groupID);
					count1 = connection.srem(key, arr);
					if (log.isDebugEnabled()) {
						log.debug("Deleted " + count1 + " numbers from redis " + key);
					}
					numbers.clear();
				}
			}

			if (numbers.size() > 0) {
				String[] arr = new String[numbers.size()];
				arr = numbers.toArray(arr);
				String key = groupIdentifier.equalsIgnoreCase(Constants.GROUP_IDENTIFIER_NORMAL)
						? Constants.REDIS_QUEUE_NORMAL_GROUPS
						: Constants.REDIS_QUEUE_EXCLUDE_GROUPS;
				key = key.replace("group_id", groupID);
				count2 = connection.srem(key, arr);
				if (log.isDebugEnabled()) {
					log.debug("Deleted " + count2 + " numbers from redis " + key);
				}
			}

			count = count1 + count2;

			if (log.isDebugEnabled()) {
				log.debug("Deleted " + count + " numbers from groupId " + groupID);
			}

		} catch (Exception e) {
			log.error("Error while removing numbers from mobile-numbers-redis,", e);
			throw e;
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (Exception e) {
					log.error("Error closing redis connection,", e);
				}
			}
			if (reader != null) {
				reader.close();
			}
		}
		return count;
	}

	public long getGroupCount(String groupId, String groupIdentifier) {
		String logName = className + " [getGroupCount] ";
		Jedis jedis = null;
		long count = 0l;
		String redisKey = null;
		try {
			if (groupIdentifier.equalsIgnoreCase(Constants.GROUP_IDENTIFIER_NORMAL)) {
				jedis = RedisConnectionTon.getInstance().getNormalGroupRedisConnection();
				redisKey = Constants.REDIS_QUEUE_NORMAL_GROUPS;
			} else {
				jedis = RedisConnectionTon.getInstance().getExcludeGroupRedisConnection();
				redisKey = Constants.REDIS_QUEUE_EXCLUDE_GROUPS;
			}
			String groupKey = redisKey.replace("group_id", groupId);
			count = jedis.scard(groupKey);
		} catch (Exception e) {
			log.error(logName + "Exception while finding group wise total", e);
		} finally {
			if (jedis != null) {
				try {
					jedis.close();
				} catch (Exception e) {
					log.error("Error closing redis connection,", e);
				}
			}
		}
		return count;
	}

	public long writeGroupContactsToFile(String groupId, String filename, int batchSize) throws Exception {
		String logName = className + " [writeGroupContactsToFile] ";
		Jedis jedis = null;
		String redisKey = null;
		long total = 0L;
		try {
			jedis = RedisConnectionTon.getInstance().getNormalGroupRedisConnection();
			redisKey = Constants.REDIS_QUEUE_NORMAL_GROUPS;
			String key = redisKey.replace("group_id", groupId);
			if (jedis.exists(key)) {
				total = createGroupFile(batchSize, jedis, key, filename);
			}
		} catch (Exception e) {
			log.error(logName + "Error while finding numbers", e);
			throw e;
		} finally {
			if (jedis != null)
				try {
					jedis.close();
				} catch (Exception e) {
					log.error("Error closing redis connection,", e);
				}
		}
		return total;
	}

	private long createGroupFile(int limit, Jedis jedis, String key, String filename) throws Exception {
		String logName = className + " [createGroupFile] ";
		long total = 0L;
		try {
			ScanParams scanParams = new ScanParams().count(limit);
			String cur = redis.clients.jedis.ScanParams.SCAN_POINTER_START;
			boolean cycleIsFinished = false;
			while (!cycleIsFinished) {
				ScanResult<String> scanResult = jedis.sscan(key, cur, scanParams);
				List<String> result = scanResult.getResult();
				if (result != null && result.size() > 0) {
					writeToFile(result, filename);
					total += result.size();
				}
				// cur = scanResult.getStringCursor();
				cur = scanResult.getCursor();
				if (cur.equals("0")) {
					cycleIsFinished = true;
				}
			}
		} catch (Exception e) {
			log.error(logName + "Error fething mobile numbers,", e);
			throw e;
		}
		return total;
	}

	private void writeToFile(List<String> mobileNumbers, String fileName) throws Exception {
		FileWriter fw = null;
		try {
			fw = new FileWriter(fileName, true);
			for (String mobile : mobileNumbers) {
				fw.write(mobile);
				fw.write(System.lineSeparator());
			}
		} catch (Exception e) {
			log.error(className + " [writeToFile]  Error while creating group file ... ", e);
			throw e;
		} finally {
			try {
				fw.close();
			} catch (Exception e) {
				log.error(className + " [writeToFile] ", e);
			}
		}
	}

}
