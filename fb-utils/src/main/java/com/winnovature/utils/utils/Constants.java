package com.winnovature.utils.utils;

public class Constants {
	public static final String UtilsLogger = "UtilsLogger";
	public static final String UPNotificationERRORLogger = "UPNotificationErrorLogger";

	// JNDIs
	public static final String MARIADB_JNDI_NAME_ACCOUNTS_DB = "mariadb.jndi.name.accountsdb";
	public static final String MARIADB_JNDI_NAME_CM_DB = "mariadb.jndi.name.cmdb";
	public static final String MARIADB_JNDI_NAME_CONFIG_DB = "mariadb.jndi.name.configdb";

	// Sleep times
	public static final String THREAD_SLEEP_TIME_IN_MILLISECONDS = "thread.sleep.time.in.milliseconds";
	public static final String CONSUMERS_SLEEP_TIME_IN_MILLI_SECONDS = "idle.thread.sleep.time.in.milliseconds";
	public static final String IDLE_THREAD_SLEEP_TIME_IN_MILLISECONDS = "idle.thread.sleep.time.in.milliseconds";

	// Others
	public static final String MONITORING_INSTANCE_ID = "instance.monitoring.id";
	public static final String FILE_SPLIT_QUEUE_NAME = "file.split.queue.name";
	public static final String GROUP_QUEUE_NAME = "group.queue.name";
	public static final String GROUP_CAMPAIGN_QUEUE_NAME = "groups.campaign.queue.name";
	public static final String MAX_RETRY_COUNT = "max.file.retry.count";
	public static final String FILEID_FOR_LOGGER = "fileId:";
	public static final String REDIS_HEART_BEAT_PARENT_KEY = "redis.heart.beat.parent.key";
	public static final String SPLIT_FILE_DELIMITER = "split.file.delimiter";
	public static final String EXCLUDE = "_exclude";
	public static final String DEFAULT_HIGH_VOL_QUEUE_NAME = "default.high.vol.delivery.queue.name";
	public static final String LINE_BREAK_REPLACER = "replace.newline.character.with";
	public static final String SPLIT_FILE_STORE_PATH = "split.file.store.path";
	public static final String PATTERN_DECIMALFORMATTER_FOR_DIGITS = "pattern.decimal.formatter.for.digits";
	public static final String UNSUPPORTED_ROW_HEADER = "ROW_STATUS";
	public static final String OS_SPECIFIC_LINE_BREAK = "\r\n|\r|\n";
	public static final String UNSUPPORTED_UC = "UNSUPPORTED_UC";
	public static final String STATS_UPDATE_STATUS_QUERY_QUEUE_NAME = "stats.update.status.query.queue.name";
	public static final String EXCLUDE_NUMBERS_INSERT_QUEUE_NAME = "unprocess.data.insert.queue.name";
	public static final String BATCH_UPDATE_COUNT = "batch.update.count";
	public static final String LINE_BREAK = System.lineSeparator();
	public static final String UNSUPPORTED_UNICODE_MSG = "Unsupported_unicode_msg";
	public static final String CAMPAIGNS_FILE_STORE_PATH = "campaigns.file.store.path";
	public static final String TEMPLATE_FILE_STORE_PATH = "template.file.store.path";
	public static final String GROUP_FILE_STORE_PATH = "group.file.store.path";
	public static final String CAMPAIGN = "campaign";
	public static final String TEMPLATE = "template";
	public static final String GROUP = "group";
	public static final String DLT = "dlt";
	public static final String ABANDONED_FILES_DELIMITER = "::";

	// Redis
	public static final int REDIS_STATUS = 1;
	public static final int REDIS_STATUS_FOR_NORMAL_GROUPS = 2;
	public static final int REDIS_STATUS_FOR_EXCLUDE_GROUPS = 3;
	public static final int REDIS_STATUS_HEART_BEAT = 4;
	public static final int REDIS_STATUS_FOR_DLT_FILE_PROCESS = 5;
	public static final int REDIS_STATUS_FOR_UNPROCESS = 6;
	public static final int REDIS_STATUS_FOR_DUPCHECK = 6;

	// activities table status
	public static final String ACTIVITIES_STATUS_UPDATE_QUEUE_NAME = "activities.status.update.queue.name";
	public static final String ACTIVITY_TABLE_COMPLETED_STATUS = "activity.table.statusCompleted";
	
	// date formats
	public static final String DATE_FORMAT_DD_MM_YYYY_HH_MM_SS = "dd-MM-yyyy HH:mm:ss";
	public static final String DATE_FORMAT_DDMMYYYY = "ddMMyyyy";
	
	// SMS types
	public static final String QUICK_CAMP = "quick";
	public static final String GROUP_CAMP = "group";
	public static final String OTM_CAMP = "otm";
	public static final String MTM_CAMP = "mtm";
	public static final String TEM_CAMP = "template";
	
	// MSG type
	public static final String MSG_TYPE_MULTI_LANG = "multiLang";
	public static final String MSG_TYPE_TEXT = "text";

}
