package com.winnovature.groupsprocessor.servlets;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.GenericServlet;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.groupsprocessor.consumers.GroupsCampaignQConsumer;
import com.winnovature.groupsprocessor.consumers.GroupsFileSplitQConsumer;
import com.winnovature.groupsprocessor.consumers.GroupsQConsumer;
import com.winnovature.groupsprocessor.consumers.PollerCampaignMasterCompleted;
import com.winnovature.groupsprocessor.consumers.PollerGroupFilesCompleted;
import com.winnovature.groupsprocessor.consumers.PollerGroupMasterCompleted;
import com.winnovature.groupsprocessor.pollers.GroupsMasterPoller;
import com.winnovature.groupsprocessor.singletons.GroupsProcessorPropertiesTon;
import com.winnovature.groupsprocessor.singletons.RedisConnectionTon;
import com.winnovature.groupsprocessor.utils.Constants;
import com.winnovature.utils.dtos.RedisServerDetailsBean;

@WebServlet(name = "InitializePoller", loadOnStartup = 1)
public class InitializePoller extends GenericServlet implements Servlet {

	private static final long serialVersionUID = 1L;
	static Log log = LogFactory.getLog(Constants.GroupsProcessorLogger);
	private static final String className = "InitializePoller";
	PropertiesConfiguration groupsProperties = null;
	GroupsMasterPoller groupsMasterPoller = null;
	GroupsQConsumer groupsQConsumer = null;
	GroupsFileSplitQConsumer groupsFileSplitQConsumer = null;
	PollerGroupFilesCompleted pollerGroupFilesCompleted = null;
	PollerGroupMasterCompleted pollerGroupMasterCompleted = null;
	GroupsCampaignQConsumer groupsCampaignQConsumer = null;
	PollerCampaignMasterCompleted pollerCampaignMasterCompleted = null;

	@Override
	public void init() throws ServletException {
		super.init();
		
		String module=System.getenv("groupsprocessor");
		if(module!=null&&module.equals("1")) {


			try {
				if (log.isDebugEnabled()) {
					log.debug(className + " InitializePoller Servlet started...");
				}
				groupsProperties = GroupsProcessorPropertiesTon.getInstance().getPropertiesConfiguration();
				String instanceId = groupsProperties.getString(Constants.MONITORING_INSTANCE_ID);
				String groupsPollerRequired = groupsProperties.getString(Constants.GroupsPollerRequired);
				boolean runGroupsPoller = false;
				if (StringUtils.isNotBlank(groupsPollerRequired)) {
					runGroupsPoller = groupsPollerRequired.trim().equalsIgnoreCase("yes");
				}
				// picks request from group_master/group_files tables
				if (runGroupsPoller) {
					groupsMasterPoller = new GroupsMasterPoller("GroupsMasterPoller");
					groupsMasterPoller.setName("GroupsMasterPoller");
					groupsMasterPoller.start();
					if (log.isDebugEnabled()) {
						log.debug(className + " GroupsMasterPoller started.");
					}
				}

				// parse/chops user files to one/more txt files
				int groupFileConsumersPerRedisServer = groupsProperties.getInt(Constants.GROUPS_CONSUMERS_PER_REDIS);
				List<RedisServerDetailsBean> normalRedisServerDetails = RedisConnectionTon.getInstance()
						.getConfigurationFromconfigParams();

				Iterator<RedisServerDetailsBean> iterator = normalRedisServerDetails.iterator();

				while (iterator.hasNext()) {
					RedisServerDetailsBean bean = iterator.next();
					for (int i = 0; i < groupFileConsumersPerRedisServer; i++) {
						groupsQConsumer = new GroupsQConsumer(bean, instanceId);
						groupsQConsumer.setName("Thread" + (i + 1) + "-" + "GroupsQConsumer");
						groupsQConsumer.start();
						if (log.isDebugEnabled())
							log.debug("[InitializePoller.init()] >>>>>> STARTING GroupsQConsumer" + (i + 1) + " ThreadName:"
									+ groupsQConsumer.getName() + " bean:" + bean.getIpAddress());
					}
				} // end of REDIS servers iteration

				// reads the parsed/choped files and push to redis
				int groupSplitFileConsumersPerRedisServer = groupsProperties
						.getInt(Constants.SPLIT_FILE_CONSUMERS_PER_REDIS);
				String batchSize = groupsProperties.getString(Constants.REDIS_PUSH_BATCH_SIZE);
				iterator = normalRedisServerDetails.iterator();
				while (iterator.hasNext()) {
					RedisServerDetailsBean bean = iterator.next();
					for (int i = 0; i < groupSplitFileConsumersPerRedisServer; i++) {
						groupsFileSplitQConsumer = new GroupsFileSplitQConsumer(bean, instanceId, batchSize);
						groupsFileSplitQConsumer.setName("Thread" + (i + 1) + "-" + "GroupsFileSplitQConsumer");
						groupsFileSplitQConsumer.start();
						if (log.isDebugEnabled())
							log.debug("[InitializePoller.init()] >>>>>> STARTING GroupsFileSplitQConsumer" + (i + 1)
									+ " ThreadName:" + groupsFileSplitQConsumer.getName() + " bean:" + bean.getIpAddress());
					}
				} // end of REDIS servers iteration
				
				// updates group_master, groups_files to be completed
				if (runGroupsPoller) {
					pollerGroupFilesCompleted = new PollerGroupFilesCompleted();
					pollerGroupFilesCompleted.setName("PollerGroupFilesCompleted");
					pollerGroupFilesCompleted.start();
					if (log.isDebugEnabled()) {
						log.debug(className + " PollerGroupFilesCompleted started.");
					}
					
					pollerGroupMasterCompleted = new PollerGroupMasterCompleted();
					pollerGroupMasterCompleted.setName("PollerGroupMasterCompleted");
					pollerGroupMasterCompleted.start();
					if (log.isDebugEnabled()) {
						log.debug(className + " PollerGroupMasterCompleted started.");
					}
					
					pollerCampaignMasterCompleted = new PollerCampaignMasterCompleted();
					pollerCampaignMasterCompleted.setName("PollerCampaignMasterCompleted");
					pollerCampaignMasterCompleted.start();
					if (log.isDebugEnabled()) {
						log.debug(className + " PollerCampaignMasterCompleted started.");
					}
				}
				
				// get request from GroupsCampaignQ and create a text file with group numbers
				int groupsCampaignQConsumersPerRedisServer = groupsProperties.getInt(Constants.GROUPS_CAMPAIGN_Q_CONSUMERS_PER_REDIS);
				List<RedisServerDetailsBean> redisServerDetails = com.winnovature.utils.singletons.RedisConnectionTon.getInstance()
						.getConfigurationFromconfigParams();
				Iterator<RedisServerDetailsBean> iterator2 = redisServerDetails.iterator();
				while (iterator2.hasNext()) {
					RedisServerDetailsBean bean = iterator2.next();
					for (int i = 0; i < groupsCampaignQConsumersPerRedisServer; i++) {
						groupsCampaignQConsumer = new GroupsCampaignQConsumer(bean, instanceId);
						groupsCampaignQConsumer.setName("Thread" + (i+1) + "-" + "GroupsCampaignQConsumer");
						groupsCampaignQConsumer.start();
						if (log.isDebugEnabled())
							log.debug("[SplitStageServlet.init()] >>>>>> STARTING GroupsCampaignQConsumer[GroupsCampaignQ]  " + (i+1)
									+ " ThreadName:" + groupsCampaignQConsumer.getName() + " bean:" + bean.getIpAddress());
					}

				} // end of REDIS servers iteration

			} catch (Exception e) {
				log.error(className + " Exception:", e);
				log.error(className + " RESTART FP-GroupsProcessor MODULE ");
			}

		}
	}

	@Override
	public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {

	}

}
