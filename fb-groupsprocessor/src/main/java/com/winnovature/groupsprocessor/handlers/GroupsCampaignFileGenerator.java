package com.winnovature.groupsprocessor.handlers;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Random;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.winnovature.groupsprocessor.daos.CampaignsDAO;
import com.winnovature.groupsprocessor.daos.GroupRedisDAO;
import com.winnovature.groupsprocessor.singletons.GroupsProcessorPropertiesTon;
import com.winnovature.groupsprocessor.utils.Constants;
import com.winnovature.groupsprocessor.utils.Utility;

public class GroupsCampaignFileGenerator {

	static Log log = LogFactory.getLog(Constants.GroupsProcessorLogger);
	private static final String className = "[SplitFileProcessor]";

	private int batchSize = 500;

	public void generate(Map<String, String> mapObject, String queueName, String fileStoreLocation) throws Exception {

		String methodName = "[generate]";

		CampaignsDAO campaignsDAO = new CampaignsDAO();
		Instant startTime = Instant.now();
		String id = mapObject.get("cg_id");
		String groupId = mapObject.get("group_id");
		String campaignId = mapObject.get("cm_id");
		//String clientId = mapObject.get("cli_id");
		String filename = "";
		try {
			campaignsDAO.updateFileStartTime(id);
		} catch (Exception e) {
			log.error(className + methodName + com.winnovature.utils.utils.Constants.FILEID_FOR_LOGGER + id
					+ " Error while updating started_ts in campaign_groups ", e);
		}
		PropertiesConfiguration groupProperties = GroupsProcessorPropertiesTon.getInstance()
				.getPropertiesConfiguration();
		try {
			String batch = groupProperties.getString(Constants.REDIS_PUSH_BATCH_SIZE, "500");
			batchSize = Integer.parseInt(batch);

			filename = fileStoreLocation + Utility.frameFileName(campaignId, groupId);

			long total = new GroupRedisDAO().writeGroupContactsToFile(groupId, filename, batchSize);

			String time = Utility.getTimeDifference(startTime) + " milli seconds.";
			if (log.isDebugEnabled()) {
				log.debug(className + methodName + " File creation end time= " + new Date().toString() + " file Name:"
						+ filename + " file count: " + total + " :: Time taken to process file is " + time);
			}

			char[] alphabet = groupProperties.getString("random.id.chars", "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789")
					.toCharArray();
			int nonoIdSize = groupProperties.getInt("nano.id.size", 22);
			String cf_id = NanoIdUtils.randomNanoId(new Random(), alphabet, nonoIdSize);
			mapObject.put("cf_id", cf_id);
			//mapObject.put("filename", FilenameUtils.getName(filename));
			//mapObject.put("filetype", FilenameUtils.getExtension(filename));
			mapObject.put("fileloc", filename);
			mapObject.put("total", total + "");
			mapObject.put("insert_status", groupProperties.getString("status.queued"));
			mapObject.put("update_status", groupProperties.getString("status.completed"));
			//mapObject.put("filesize", new File(filename).length() + "");

			campaignsDAO.insertToCampaignFilesAndUpdateCampaignGroups(mapObject);
		} // end of try block
		catch (Exception e) {
			log.error(className + methodName + "Exception " + com.winnovature.utils.utils.Constants.FILEID_FOR_LOGGER
					+ id + " file Name:" + filename, e);
			throw e;
		}
	}// end of method

}
