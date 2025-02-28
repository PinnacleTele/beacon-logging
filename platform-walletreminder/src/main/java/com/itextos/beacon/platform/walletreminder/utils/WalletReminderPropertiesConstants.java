package com.itextos.beacon.platform.walletreminder.utils;

class WalletReminderPropertiesConstants
{

    protected static final String PROP_KEY_EMAIL_API_TYPE                             = "email.api.type";
    protected static final String PROP_KEY_EMAIL_API_URL                              = "email.api.url";
    protected static final String PROP_KEY_EMAIL_API_OWNER_ID                         = "email.api.owner.id";
    protected static final String PROP_KEY_EMAIL_API_TOKEN                            = "email.api.token";
    protected static final String PROP_KEY_EMAIL_API_SMTP_USER_NAME                   = "email.api.smtp.user.name";
    protected static final String PROP_KEY_FROM_NAME                                  = "from.name";
    protected static final String PROP_KEY_FROM_EMAIL                                 = "from.email";
    protected static final String PROP_KEY_ADMIN_EMAIL_LIST                           = "admin.email.list";
    protected static final String PROP_KEY_REPLY_TO_EMAIL                             = "reply.to.email";
    protected static final String PROP_KEY_OVERALL_BALANCE_CHECK_TRIGGER              = "overall.balance.check.trigger";
    protected static final String PROP_KEY_DAILY_BALANCE_CHECK_TRIGGER                = "daily.balance.check.trigger";
    protected static final String PROP_KEY_CLIENT_BALANCE_CHECK_TRIGGER               = "client.balance.check.trigger";
    protected static final String PROP_KEY_WALLET_BACKUP_TRIGGER                      = "wallet.backup.trigger";
    protected static final String PROP_KEY_CLIENT_BALANCE_REMINDER_MAIL_COUNT_PER_DAY = "client.balance.reminder.mail.count.per.day";
    protected static final String PROP_KEY_CLIENT_BALANCE_REMINDER_DATA_LOG_PATH      = "client.balance.reminder.data.log.path";
    protected static final String PROP_KEY_OVERALL_BALANCE_REMINDER_SUBJECT_FORMAT    = "overall.balance.reminder.subject.format";
    protected static final String PROP_KEY_DAILY_BALANCE_REMINDER_SUBJECT_FORMAT      = "daily.balance.reminder.subject.format";
    protected static final String PROP_KEY_OVERALL_SORT_BY                            = "overall.sort.by";
    protected static final String PROP_KEY_DAILY_SORT_BY                              = "daily.sort.by";
    protected static final String PROP_KEY_OVERALL_GROUPBY_ENABLED                    = "overall.groupby.enabled";
    protected static final String PROP_KEY_DAILY_GROUPBY_ENABLED                      = "daily.groupby.enabled";
    protected static final String PROP_KEY_CLIENT_IDS_TO_IGNORE                       = "clientids.to.ignore";
    protected static final String PROP_KEY_CLIENT_BALANCE_REMINDER_SUBJECT_FORMAT     = "client.balance.reminder.subject.format";
    protected static final String PROP_KEY_CLIENT_BALANCE_REMINDER_BODY_FORMAT        = "client.balance.reminder.body.format";
    protected static final String PROP_KEY_MINIMUM_BALANCE                            = "minimum.balance.to.notify";
    protected static final String PROP_KEY_FILE_BACKUP_DAYS                           = "file.backup.days";
    protected static final String PROP_KEY_FILE_BACKUP_PATH                           = "file.backup.path";
    protected static final String PROP_KEY_CLIENT_BALANCE_CHECK_ENABLED               = "client.balance.check.enabled";
    protected static final String PROP_KEY_FILTER_INVALID_RECORDS_ENABLED             = "filter.invalid.records.enabled";
    protected static final String PROP_KEY_DEFAULT_NOTIFICATION_MAIL_ID               = "default.notification.email.id";

    protected static final String DEFAULT_EMAIL_API_TYPE                              = "rml";
    protected static final String DEFAULT_FROM_NAME                                   = "Winnovature Support Team";
    protected static final String DEFAULT_FROM_EMAIL_ID                               = "wecare@winnovature.com";
    protected static final String DEFAULT_ADMIN_EMAIL_ID_LIST                         = "accounts@winnovature.com,wecare@winnovature.com,praveen@winnovature.com,nl@winnovature.com";
    protected static final String DEFAULT_REPLY_TO_EMAIL_ID                           = "wecare@winnovature.com";
    protected static final int    DEFAULT_REMINDER_PER_DAY                            = 3;
    protected static final int    DEFAULT_MINIMUM_BALANCE                             = 20;
    protected static final int    DEFAULT_BACKUP_DAYS                                 = 10;
    protected static final String DEFAULT_LOG_PATH                                    = "/home/apps/wallet/logs";
    protected static final String DEFAULT_OVERALL_BALANCE_REMINDER_SUBJECT_FORMAT     = "Beacon - Wallet Balance Alert on {0}";
    protected static final String DEFAULT_DAILY_BALANCE_REMINDER_SUBJECT_FORMAT       = "Beacon - Daily Wallet Balance Alert on {0}";
    protected static final String DEFAULT_CLIENT_BALANCE_REMINDER_SUBJECT_FORMAT      = "Beacon - Low Wallet Balance Alert on {0} for user - {1}";
    protected static final String DEFAULT_CLIENT_BALANCE_REMINDER_BODY_FORMAT         = "<html><body><h3><u>iTextos Beacon - Wallet Balance</u> - <font color='red'>{0}</font></h3>"
            + "<br><br>Dear {1}<br><br>Your Beacon wallet account balance is <font color='red'><b>{0}</b></font> as on <font color='blue'><b>{2}</b></font> for the user <font color='blue'><b>{3}</b></font> with client id <font color='blue'><b>{4}</b></font>."
            + "<br><br>Please recharge your wallet as early as possible to avoid message failing in Beacon." + "<br><br>Thanks and Regards<br>Beacon Support Team"
            + "<br><br><table border='0'><tr><td><b>Email</b></td><td>:</td><td>wecare@winnovature.com</td></tr>" + "<tr><td><b>Praveen</b></td><td>:</td><td>+ 91 84540 57026</td></tr>"
            + "<tr><td><b>Ashwin</b></td><td>:</td><td>+ 91 99444 31427</td></tr></table>" + "</body></html>";

}
