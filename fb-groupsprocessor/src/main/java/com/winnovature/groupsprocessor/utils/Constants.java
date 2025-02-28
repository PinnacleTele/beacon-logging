package com.winnovature.groupsprocessor.utils;

public class Constants {
	public static final String GroupsProcessorLogger = "GroupsProcessorLogger";
	public static final String MONITORING_INSTANCE_ID = "instance.monitoring.id";
	public static final String GroupsPollerRequired = "groups.poller.required";
	public static final String FILE_SMS_ALL_STATUS = "file.sms.all.status";
	public static final String PROCESS_STATUS_COMPLETED = "completed";
	public static final String PROCESS_STATUS_FAILED = "failed";
	public static final String PROCESS_STATUS_INPROGRESS = "inprocess";
	public static final String SPLIT_FILE_CONSUMERS_PER_REDIS = "split.file.consumers.per.redis";
	public static final String FILE_SPLIT_LIMIT = "file.split.limit";
	public static final String DEADLOCK_EXCEPTION_DEFAULT = "Deadlock found";
	public static final String PROCESS_STATUS_INVALIDFILE = "invalidfiles";
	public static final String FILE_SMS_INPROCESS_STATUS = "file.sms.inprocess.status";
	public static final String COMPLETED_STATUS = "status.completed";
	public static final String FAILED_STATUS = "status.failed";
	public static final String INVALID_STATUS = "status.invalid";
	public static final String GROUP_SPLIT_FILES_QUEUE = "group.split.files.queue";
	public static final String GROUPS_CONSUMERS_PER_REDIS = "groups.consumers.per.redis";
	public static final String REDIS_PUSH_BATCH_SIZE = "contacts.push.batch.size";
	public static final String GROUP_IDENTIFIER_EXCLUDE = "exclude";
	public static final String GROUP_IDENTIFIER_NORMAL = "normal";
	public static final String REDIS_QUEUE_NORMAL_GROUPS = "groups:contacts:group_id";
	public static final String REDIS_QUEUE_EXCLUDE_GROUPS = "excludegroups:contacts:group_id";
	public static final String REDIS_QUEUE_GROUPS_CONTACT_DETAILS = "groups:contactdetails:group_id";
	public static final String REDIS_QUEUE_EXCLUDE_GROUPS_CONTACT_DETAILS = "excludegroups:contactdetails:group_id";
	public static final String REDIS_QUEUE_GROUPS_OTHER_DETAILS = "groups:otherdetails";
	public static final String GROUPS_CAMPAIGN_Q_CONSUMERS_PER_REDIS = "groups.campaign.queue.consumers.per.redis";
	public static final String PROCESS_STATUS_GRPINPROGRESS = "ginprocess";
}
