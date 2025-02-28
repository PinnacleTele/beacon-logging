export const CONSTANTS = {
    LIMIT_DISPLAY_MOBILES_PREVIEW: 12,
    decimalPlaces: 6,
    TemplateFilePreviewTableLimit: 6,
    decimalTotalLength: 10,
    genericMinFieldMinLength: 3,
    genericOptimalFieldMinLength: 3,
    genericFieldMaxLength: 40,
    generalMobileMinLength: 8, //solomon islands has 8 including country code, 9 is for sweden
    generalMobileMaxLength: 15,
    emailMaxLength: 100,
    companyNameMaxLength: 100,

    numberCheck: "[a-zA-Z]",
    RETRY_COUNT: 5,
    decimalNum_validation: "^-?\\d*[.,]?\\d{0,4}$",
    emailPattern: "^[a-z0-9]+[._%+-]?[a-z0-9]+@[a-z0-9]+\\.[a-z]{2,6}$",

    QUICK_SMS_SHORT_FORM: "CQ",
    TEMPLATE_SHORT_FORM: "CT",
    MAX_DIGITS_COUNT: 90,
    DOMESTIC: "india",
    INTERNATIONAL: "international",
    DOMESTIC_CODE: "91",
    DOMESTIC_START_INDEX: ["6", "7", "8", "9"],
    CAMPAINGNS: [{ label: "Quick", link: "cq" },
    { label: "One To Many", link: "otm" },
    { label: "Many To Many", link: "mtm" },
    { label: "Template", link: "ct" },
    { label: "Group", link: "cg" }],
    ONE_TO_MANY_SHORT_FORM: "OTM",
    ONE_TO_MANY_TITLE: "One To Many",
    OTM_FILE_CONTENT: "Upload File With Mobile Number",
    MTM_FILE_CONTENT: "Upload File With Mobile + Message",
    CAMP_TEMPLATE_FILE_CONTENT: "Upload File With Template Details",
    GROUP_FILE_CONTENT: "Upload File With Contacts",
    DLT_FILE_CONTENT: "Upload DLT template files",
    MANY_TO_MANY_TITLE: "Many To Many",
    MANY_TO_MANY_SHORT_FORM: "MTM",
    GROUPSMS_SHORT_FORM: "CG",
    GROUPSMS_TITLE: "Group SMS",
    // campaign Name
    minLengthCampaignName: 3,
    maxLengthCampaignName: 40,
    hasSpace: "^\S+$",
    //pattern_validation: "[a-zA-Z]{1}[a-zA-Z0-9_-\\s]*$",
    pattern_validation: "[a-zA-Z0-9_-\\s]+$",
    company_validation: "^[a-zA-Z0-9_-\\s]+$",
    allowed_special_characters: "space , - , _ ",
    mobile_pattern_validation: "^[+]?(?!0+$)[0-9]{8,15}$",
    address_validation: "^[a-zA-Z0-9][a-zA-Z0-9,().\/_-\\s]+$",

    // all spl chars !"#$%&'()*+,-./:;<=>?@[\]^_`{|}~
    addressMaxLength: 200,
    // Template
    minLengthTemplateName: 3,
    maxLengthTemplateName: 40,

    dltEntityIDMinLength: 5,
    dltEntityIDMaxLength: 19,
    dltEntityIDPattern: "^[0-9]+$",



    DATE_FORMAT: "DD/MM/YYYY",
    TIME_FORMAT_12: "hh:mm A",
    TIME_FORMAT_24: "HH:mm",
    ANIMATE_TIMESET_300EASEIN: "200ms ease-in",
    ANIMATE_TIMESET_500EASEIN: "200ms ease-in",
    SLIDERIN_TIME: 200,
    SLIDEROUT_TIME: 100,
    POPIN_TIME: 300,
    POPOUT_TIME: 100,
    SLIDEDOWN_TIME: 100,
    SLIDEUP_TIME: 100,
    TIMESTAMP_FORMAT: "DD/MM/YYYY HH:mm:ss",
    TEMPL_MSG_START_PATTERN: "{#",
    TEMPL_MSG_END_PATTERN: "#}",
    TEMPLATE_MSG_CURLY_PATTERN: /[^{#]+(?=#})/g,
    TEMPLATE_VAR_PATTERN: /{#[a-zA-Z0-9\S]+#\}/g,


    MSG_CURLY_REMOVE_PATTERN: /{[^{]+(?=})}/g,


    singleFileMaxSize: 10000000,
    REQD_TEXT: {
        singleFileUpload: "Atleast one File is Required"
    },

    // TODO: Check  where UTC property is referred
    DATE_AND_TIME_FORMAT: "DD/MM/YYYY HH:mm",
    DEFAULT_OFF_SET: "+05:30",
    DEFAULT_TZ: "(UTC+05:30) Asia/Calcutta",
    DEFAULT_ZONE: "Asia/Calcutta",
    COUNTRY: "IND",
    STATIC_TEMPLATE: "Message to be displayed for static",

    ACCEPTED_FILE_TYPES:
        "text/csv,application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/zip,application/x-zip-compressed",
    MAX_FILES_ALLOWED: 5,
    MAX_FILE_SIZE: 40,
    DUPLICATE_FILE_MESSAGE: "Duplicate File",
    MAX_UPLOAD_MESSAGE: "max upload limit",
    TOO_BIG_FILE_MESSAGE: "File is too big",
    INVALID_FILE_TYPE_MESSAGE: "Invalid file type",
    UPLOAD_FAILED_MESSAGE: "Upload failed",

    DOMESTIC_MIN_LENGTH: 10,
    DOMESTIC_MAX_LENGTH: 12,
    INTL_MIN_LENGTH: 8,
    INTL_MAX_LENGTH: 15,
    MAX_MOBILE_COUNT: 25,
    UTC: 330,
    TIME_PICKER_TIME_DIFFERENCE: 10,

    EDIT_DELETE_SCHEDULE_BUFFER: 20,
    CAMPAIGN_SCHEDULE_BUFFER_IN_MINS: 15,
    TIME_ROUNDING_IN_MINS: 5,
    DEFAULT_REFRESH_INTERVAL: 90,
    INFO_TXT: {
        campaignName: "Alphanumerics of length 3 to 40 may have (space - _) chars",
        campaignMessage: "Some info text",
        campaignLanguage: "Language- some info text",
        cqMobileNumbersPart1: "Max of ",
        cqMobileNumbersPart2: " numbers separated by new line, comma. ",
        invalidInfoTxt: "Invalid numbers are numbers those are not 8 to 15 digits",
        campaignSenderId: "Senderid  -some info text",
        templateName: "Alphanumerics of length 3 to 40 may have (space - _) chars",
        templateMobileColumn: "Mobilecolumn some info Text",
        campaignCancelMessage:
            "Are you sure you want to cancel this campaign.. ?",
        templateCancelMessage:
            " Are you sure you want to cancel the creation of this template..?",

        templateEditCancelMessage:
            " Are you sure you want to cancel the edition of this template..?",

        templateDeleteMessage:
            " Are you sure you want to delete this template..?",

        templateColBased:
            "NOTE: template variables should be enclosed within {# #}" +
            " e.g. Dear {#name#} your bal is {#amount#}. the name and amount are the column " +
            "header names from the file. Variables are case-insensitive.",

        groupEditCancelMessage:
            "Are you sure you want to discard changes?",

        groupDeleteMessage:
            "Are you sure want to delete the group..?",

        contactAddMessage:
            "Are you sure you want to add this contact..?",

        contactEditCancelMessage:
            "Are you sure you want to cancel this action..?",

        contactDeleteMessage:
            "Are you sure want to delete the contact..?",

        templateIndexBased:
            "NOTE: template variables should be enclosed within {# #} " +
            "e.g. Dear {#2#} your bal is {#3#}. Numbers 2 and 3 are the column positions " +
            "in the file. Positions always starts from 1.",
        singleFileUpload: "Select a CSV, XLS, XLSX file up to 100MB",

    },
    ERROR_DISPLAY: {
        uniqueCampaignName: "Entered Campaign Name exists already!!",
        campaignNameSplChars: "Only allowed special Characters are ",
        campaignNameMinLength: "Minimum Length should be ",
        campaignNameMaxLength: "Maximum Length should be ",
        noUnicodeText: "Unicode not allowed under Text ",
        mobileCountExceeded: "Maximum Count allowed is ",
        invalidCountError: "You have invalid mobile number(s)",
        uniqueTemplateName: "Entered Template Name exists already!!",
        templateNameSplChars: "Only allowed special Characters are ",
        templateNameMinLength: "Minimum Length should be ",
        templateNameMaxLength: "Maximum Length should be ",
        uniqeGroupName: "Entered GroupName Already Exist",
        onlyAphabets: "Only Alphabets are allowed",
        onlyNumbers: "Only Numerical are allowed",
        fieldSplChars: "Only allowed special Characters ",
        fieldMinLength: "Minimum Length should be ",
        minimumBillLength: "Rate cannot be lesser than your rate",
        fieldMaxLength: "Maximum Length should be ",
        mobilePattern: "Enter mobile number with country code, length 8 till 15",
        decimalPatternMsg: "Supports upto max 4 digits before decimal and max 6 digits after decimal",
        walletDecimalPatternMsg: "Supports upto max 8 digits before decimal and max 6 digits after decimal",
        userNameUnique: "User Name exists already",
        noSpaces: "No space is allowed",
        newLineMaxLimit: "Accepts only 3 characters",
        NO_VALID_COUNT: "Enter atleast one valid Mobile number",
        gtZero: "Enter a number greater than zero",
        emailPatternMsg: "Entered Email ID is not valid",
        multiScheduleErrorMsg: "One/more selected dates are before",
        multiScheduleDuplicateErrorMsg: "selected more than once"
    },
    COMPAIGN_DELIMETERLIST: [
        { key: ",", value: ", comma" },
        { key: "|", value: "| pipe" },
        { key: "-", value: "- hyphen" }
    ],
    //groupName
    minimumGroupNameLength: 3,
    maximumGroupNameLength: 40,


    CampaignIcons: {
        complete_Bg: "py-0.5 px-2 sm:top-3 top-3 absolute right-0 bg-green-100 flex items-center justify-center rounded-tl-3xl rounded-bl-3xl",
        complete_text: "focus:outline-none text-xxs font-semibold text-center text-green-600"
    },

    // quick Link limit
    quickLinkLimit: 6,

    //campaign detail page background thread timing
    apiHitTimer: 30000,

    edit_Delete_enable: 30,



    CSNDHeadContent: "Nothing Scheduled",

    CSNDMessageCOntent: "You don't have any campaigns scheduled for the selected period. Please try with different range",


    RNDHeadContent: "No Reports",

    RNDMessageCOntent: "You don't have any Reports for the selected criteria. Please try with different range",

    CNDHeadContent: "No Campaigns",

    CNDMessageCOntent: "You don't have any campaigns for the selected period. Please try with different range",

    DLTNDHeadContent: "No Dlt Templates",

    DLTNDMessageCOntent: "You don't have any Dlt Templates for the selected period",

    DLTUPLOADNDHeadContent: "No Dlt Uploads",

    DLTUPLOADNDMessageCOntent: "You don't have any Uploads for the selected period",

    EIDNDHeadContent: "No Entity Id",

    EIDNDMessageCOntent: "You don't have any EntityId for your Account",

    SIDNDHeadContent: "No SenderId",

    SIDNDMessageCOntent: "You don't have any SenderId for your Account",

    //email id parts
    emailFirstPartLength: 1,
    emailMiddlePartLength: 2,
    emailLastPartMinLength: 2,
    emailLastPartMaxLength: 6,

    //currency format

    curencyFormat: "0.0-6",
    currency: "EUR"

};

export const value = {

    'tooltip-class': 'ngTemplateTips',
    'hide-delay': 100

}

