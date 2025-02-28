package com.winnovature.campaignfinisher.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.campaignfinisher.singletons.CampaignFinisherPropertiesTon;
import com.winnovature.campaignfinisher.singletons.RedisConnectionFactory;

import redis.clients.jedis.Jedis;

public class Utility {
	static Log log = LogFactory.getLog(Constants.CampaignFinisherLogger);
	private static final String className = "[Utility]";
	
	public boolean handoverCampIdstoDeleteQ(String jsonForQ) throws Exception {
		if (log.isDebugEnabled())
			log.debug(className
					+ " [handoverCampIdstoDeleteQ] Begin.. jsonForQ:" + jsonForQ);

		boolean sent = false;
		Jedis con = null;
		String rid = null;
		
		try {
			rid = CampaignFinisherPropertiesTon.getInstance()
					.getPropertiesConfiguration()
					.getString(Constants.REDIS_RID_INFO);
			
			con = RedisConnectionFactory.getInstance().getConnection(rid);
			String queueName = CampaignFinisherPropertiesTon.getInstance()
					.getPropertiesConfiguration()
					.getString(Constants.DELETE_CAMPID_QUEUE_NAME);
			if (con != null) {
				con.lpush(queueName, jsonForQ);
				sent = true;
			} else
				sent = false;

		} catch (Exception e) {
			log.error(className + " [handoverCampIdstoDeleteQ] Exception:", e);
			throw e;
		} finally {
			if (con != null)
				con.close();
		}

		return sent;
	}

}
