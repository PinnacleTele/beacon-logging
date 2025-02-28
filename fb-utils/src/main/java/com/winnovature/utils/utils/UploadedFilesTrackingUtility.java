package com.winnovature.utils.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.errorlog.FileUploadLog;
import com.winnovature.utils.daos.GenericDao;
import com.winnovature.utils.dtos.RedisServerDetailsBean;
import com.winnovature.utils.singletons.RedisConnectionFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

public class UploadedFilesTrackingUtility {
	static Log log = LogFactory.getLog(Constants.UtilsLogger);
	private static final String className = "[UploadedFilesTrackingUtility]";

	public static Long setUploadedFilesInfo(String type, String username, List<String> filesList) {
		String methodName = " [setUploadedFilesInfo()] ";
		Jedis con = null;
		Long count = null;
		try {
			RedisServerDetailsBean rBean = new GenericDao().getFileUploadTrackingRedisServerDetails();
			FileUploadLog.getInstance().debug(" [setUploadedFilesInfo()] rBean : "+rBean);
			con = RedisConnectionFactory.getInstance().getConnectionForFileUploadTrackingRedis(rBean.getRid());
			if (con != null) {
				String key = Utility.getCustomDateAsString("yyyy-MM-dd") + "_Fileuploads";

				String[] arr = new String[filesList.size()];
				for (int i = 0; i < filesList.size(); i++) {
					arr[i] = type + "::" + username + "::" + filesList.get(i);
				}
				count = con.sadd(key, arr);
			}
		} catch (Exception e) {
			FileUploadLog.getInstance().error(className + methodName + " Exception ::: ",e);

			log.error(className + methodName + " Exception ::: ", e);
		} finally {
			if (con != null) {
				con.close();
			}
		}
		return count;
	}

	public static List<String> getUploadedFilesInfo(int limit, int days) throws Exception {
		String logName = className + " [getUploadedFilesInfo] ";
		Jedis jedis = null;
		List<String> list = null;
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -days);

		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String key = dateFormat.format(calendar.getTime()) + "_Fileuploads";

			RedisServerDetailsBean rBean = new GenericDao().getFileUploadTrackingRedisServerDetails();
			jedis = RedisConnectionFactory.getInstance().getConnectionForFileUploadTrackingRedis(rBean.getRid());

			if (jedis.exists(key)) {
				ScanParams scanParams = new ScanParams().count(limit);
				String cur = redis.clients.jedis.ScanParams.SCAN_POINTER_START;
				list = new ArrayList<String>();
				boolean cycleIsFinished = false;
				while (!cycleIsFinished) {
					ScanResult<String> scanResult = jedis.sscan(key, cur, scanParams);
					List<String> result = scanResult.getResult();
					if (result != null && result.size() > 0) {
						list.addAll(result);
					}
					cur = scanResult.getCursor();
					if (cur.equals("0")) {
						cycleIsFinished = true;
					}
				}

				jedis.del(key);

			}

		} catch (Exception e) {
			log.error(logName + "Error fething uploaded files details from redis,", e);
		} finally {
			if (jedis != null) {
				jedis.close();
			}
		}
		return list;
	}

}
