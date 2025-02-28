import { environment } from "src/environments/environment";

export const CONSTANTS_URL = {
   GLOBAL_URL: environment.GLOBAL_URL,

   FILE_UPLOAD_URL: environment.FILE_UPLOAD_URL,

   // GLOBAL_URL: `/campaigns/api`,
   // FILE_UPLOAD_URL: `/campaigns/uploads`,

   //GLOBAL_URL: `http://localhost:3000`,
   //FILE_UPLOAD_URL: `http://192.168.1.122:8180`,

   DASHBOARD_COMPAIGN_POST_ACTIVITY: `/dashboard/campaign-activities`,
   DASHBOARD_COMPAIGN_POST_LIST: `/dashboard/campaign-list`,
   DASHBOARD_COMPAIGN_POST_SCHEDULE: `/dashboard/scheduled-campaigns`,
   DASHBOARD_COMPAIGN_GET_STATISTICS: `/dashboard/statistics`,
   DASHBOARD_COMPAIGN_GET_SUMMARY: `/dashboard/summary`,
   OTM_SEND_COMPAIGN: `/campaigns/otm/save`,
   COMPAIGN_UPLOAD_FILE: `/files/upload/dropzonewrapper/autosubmit`,
   CAMPAIN_SENDER_ID: "/cm/campaigns/senderids?entity_id=",
   ALL_DLT_TEMPLATES: "/cm/template/dlttemplates",
   All_ENTITY_ID: "/cm/campaigns/entityids",
   //  ALL_DLT_TEMPLATES :'/api/DLTs',
   ALL_TEMPLATES: "/cm/template/templates",
   TEMPLATES_CAMPAIGN: "/cm/template/templatesForCampaign?entity_id=",
   DLT_TEMPLATE_CAMPAIGN: "/cm/template/dlttemplatesForCampaign?header=",
   // API_URL : "/cm/template/templates",
   TEMPLATES_API_TINFO_URL: "/cm/template/tinfo?id=",
   TEMPLATES_API_DELETEURL: "/cm/template/tdelete",
   TEMPLATES_API_UPDATEURL: "/cm/template/tupdate",
   TEMPLATES_API_CREATEURL: "/cm/template/tnew",
   TEMPLATES_API_DLTTEMPLATE_URL: "/cm/template/dlttemplates?entity_id=",
   // TEMPLATES_API_DLT_TEMPLATE_URL:
   //     "/cm/template/dlttemplates?entity_id=",
   T_UNIQUE_NAME_URL: "/cm/template/tnameunique?t_name=",

   SINGLE_FILE_UPLOAD: "/cm/singleFile",
   ALL_TEMPLATE_NAMES: "/api/templates",
   CAMPAIGN_COTM_SEND: "/cm/campaigns/cotm/send",
   CAMPAIGN_CMTM_SEND: "/cm/campaigns/cmtm/send",
   CAMPAIGN_CTSEND: "/cm/campaigns/ct/send",
   CAMPAIGN_CQSEND: "/cm/campaigns/cq/send",
   CAMPAIGN_CGSEND: "/cm/campaigns/cg/send",

   CAMPAIGN_LIST: "/cm/campaigns/clist",
   CAMPAIGN_LIST_STATS: "/cm/campaigns/ctodaysstats",

   CAMPAIGN_S_LIST: "/cm/campaigns/cslist",
   CAMPAIGN_S_LIST_STATS: "/cm/campaigns/cschedstats",

   CHK_UNIQUE_CNAME: "/cm/campaigns/cnameunique",
   GET_MSG_PARTS_API: "/cm/campaigns/msginfo",

   LOGIN_API: "/cm/auth/login",
   REFRESH_INTERVAL: "/cm/refreshinterval",
   AUTH_FORGOT_PWD_API: "/cm/auth/forgotpassword",
   REFRESH_API: "/cm/auth/token",
   LOGOUT_API: "/cm/auth/logout",
   CAMPAIGN_S_OTM_SEND: "/cm/campaigns/cotm/schedule",
   CAMPAIGN_S_MTM_SEND: "/cm/campaigns/cmtm/schedule",
   CAMPAIGN_S_CQ_SEND: "/cm/campaigns/cq/schedule",
   CAMPAIGN_S_CT_SEND: "/cm/campaigns/ct/schedule",
   CAMPAIGN_S_CG_SEND: "/cm/campaigns/cg/schedule",

   ACCOUNTS_LIST: "/cm/account/accounts",
   ACCOUNT_STATS: "/cm/account/astats",

   ALL_COUNTRY: "/cm/account/countries",
   TIME_ZONE: "/cm/utils/timezones",
   ACCT_TEMPLATE_GROUPS: "/cm/account/tgroups",
   ACCT_SHARE_GROUPS: "/cm/group/sharedgroups",
   ACCT_SUB_SERVICES: "/cm/account/assignedSubServices",
   ACCT_CREATE: "/cm/account/anew",
   ACCT_USERNAME: "/cm/account/unameunique",
   EDIT_ACCT_API: "/cm/account/ainfo?cli_id=",
   EDIT_ACCT_PI: "/cm/account/aupdatePI",
   EDIT_ACCT_2FA: "/cm/account/aupdate2FA",
   EDIT_ACCT_MS: "/cm/account/aupdateMS",
   EDIT_ACCT_DLT: "/cm/account/aupdateTGroups",
   EDIT_ACCT_SubServices: "/cm/account/aupdateServices",
   EDIT_ACCT_Groups: "/cm/account/aupdateGroups",
   CONV_API: "/cm/utils/convrate",

   MULTI_FILE_URL: `/FP-FileUpload-0.0.1/save`,
   MULTI_FILE_CT_URL: `/FP-FileUpload-0.0.1/templateplaceholders`,
   DLT_TEMPLATE_FILE_CT_URL: `/FP-FileUpload-0.0.1/dlttemplateplaceholders`,
   SINGLE_FILE_UPLOAD_URL: `/FP-FileUpload-0.0.1/template`,
   DLT_FILE_UPLOAD_URL: "/FP-DltFileProcessor-0.0.1/dlttemplate",

   EDIT_ACCT_ENCRYPT: "/cm/account/aupdateEncryption",
   EDIT_ACCT_WALLET: "/cm/account/aupdateWRates",

   CAMPAIGN_DETAIL: "/cm/campaigns/cdetails?c_id=",
   CAMPAIGN_FILE_DETAIL: "/cm/campaigns/cdetailsbyfile?c_id=",
   CAMPAIGN_PROG_STATS: "/cm/campaigns/cprocessedstats?c_id=",
   UPDATE_WALLET_URL: "/cm/account/aupdateWAmount",
   UPDATE_MULTI_WALLET_URL: "/cm/account/aupdatewamountmulti",
   API_WalletBal: "/cm/account/abal",
   API_WalletUsers: "/cm/account/ausersmw",
   MYACCT_STAT: "/cm/account/astats",
   VERIFY_PASSWD: "/cm/auth/verifypassword",
   UPDATE_PASSWD: "/cm/account/aupdatepassword",

   SC_CAMPAIGN_URL: "/cm/campaigns/csdetails?c_id=",
   SC_DELETE_CAMPAIGN_URL: "/cm/campaigns/csdelete",
   SC_RESCHEDULE_URL: "/cm/campaigns/csupdate",
   API_ACCT_STATUS_UPDATE: "/cm/account/updateaccstatus",

   DLT_FILE_UPLOAD_SAVE_URL: "/cm/dlt/dltadd",

   REPORT_SUMMARY_URL: "/cm/report/summary/rsummary",
   REPORT_DETAILED_URL: "/cm/report/log/rlogs",

   REPORT_SOURCE_URL: "/cm/report/rsources",
   REPORT_CAMPAIGN_URL: "/cm/report/rcampaigns",
   REPORT_SENDERID_URL: "/cm/report/rsenderids",
   REPORT_DOWNLOAD_URL: "/cm/report/summary/rsummarydownload",
   REPORT_DETAIL_DOWNLOAD_LOG: "/cm/download/downloadlog",
   REPORT_LOG_SEARCH: "/cm/report/search/rlogsearch",
   REPORTSEARCH_LOG_DOWNLOAD: "/cm/report/search/rlogsearchdownload",

   QUICK_LINK_URL: "/cm/account/aquicklinksettings",
   ACC_QUICK_LINKS_URL: "/cm/account/aquicklinks",
   UPDATE_QL_URL: "/cm/account/aupdatequicklinksettings",

   INTL_SENDERID_FORCAMPAIGNS: "/cm/campaigns/intlsenderids",
   uploadDLT_URL: "/cm/dlt/dlttelcos",
   DLT_STATS_URL: "/cm/dlt/dltstats",
   DLT_ALL_SENDER_ID_URL: "/cm/dlt/dltsenderids",
   // DLT_ALL_SENDER_ID_URL : "/cm/dlt/dltsenderid",

   DLT_ALL_ENTITY_ID_FILTER_URL: "/cm/dlt/dltentityidsforfilter",
   DLT_ALL_TEMPLATES_ID_FILTER_URL: "/cm/dlt/dlttemplateidsforfilter?entity_id=",
   // DLT_STATS_ENTITY_ID_URL : "/cm/dlt/dltentityidstat",
   DLT_STATS_ENTITY_ID_URL: "/cm/dlt/dltentityidstats",

   DLT_TEMPLATES_ALL_ENTITYID_TEMPLATEID_URL: "/cm/dlt/dlttemplates?entity_id=",
   // DLT_TEMPLATES_ALL_ENTITYID_TEMPLATEID_URL : "/cm/dlt/dlttemplates?entity_id",

   DLT_UPLOAD_RECORDS_URL: "/cm/dlt/dltuploads?dateselectiontype=",
   // DLT_UPLOAD_RECORDS_URL : "/cm/dlt/dltuploads?dateselectiontype",



   DOWNLOAD_STATS_URL: "/cm/download/dlogstats",
   DOWNLOAD_LOG_URL: "/cm/download/downloadlogfile?id=",
   DOWNLOAD_TABLE_URL: "/cm/download/logdownloads?dateselectiontype=",


   ALL_GROUPS_URL: "/cm/group/groups?g_type=all&status=all",
   DELETE_GROUP_URL: "/cm/group/gdelete",
   NEW_GROUP_URL: "/cm/group/gnew",
   GROUP_INFO_URL: "/cm/group/ginfo?id=",
   UPDATE_GROUP_URL: "/cm/group/gupdate",
   UNIQE_NAME_API_URL: "/cm/group/gnameunique?g_name=",
   CONTACT_LIST_API: "/cm/group/contact/contacts?g_id=",
   EDIT_CONTACT_URL: "/cm/group/contact/cupdate",
   ADD_CONTACTS_URL: "/cm/group/contact/cadd",
   DELETE_CONTACT_API: "/cm/group/contact/cdelete",


   DHTREND_API_URL: "/cm/dash/dhtrend",
   STATS_API_URL: "/cm/dash/dstats",
   PROCESSED_API_URL: "/cm/dash/dprocessedstats?dateselectiontype=",
   TREND_API_URL: "/cm/dash/dtrend?dateselectiontype=",
   MY_WALLET_ALLTX: "/cm/account/awt",
   API_INTL_RATE: "/cm/account/aparentbrrateintl",


   // billing 
   ACC_BILL_RATES: "/cm/account/abrratesintl",
   UPDATE_ROW_RATES: "/cm/br/brupdateROW",
   UPDATE_OTHERS: "/cm/br/brupdateOthers",
   UPDATE_INDIAN_RATES: "/cm/account/aupdateWRates",
   CURRENCY_CONVERT: "/cm/utils/convrate?rate=",

   ManageBillingRateList: "/cm/br/brusers",
   VIEW_RATE_USERLIST: "/cm/br/brusersforfilter",
   USERRATE_LISTAPI: "/cm/br/brchanges"



};
