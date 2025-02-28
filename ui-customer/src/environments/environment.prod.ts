export const environment = {
    GLOBAL_URL: `/campaigns/api`,
    FILE_UPLOAD_URL: `/campaigns/uploads`,
    production: true,
    APIStatus: {
        success: {
            text: "success",
            code: 0
        },
        error: {
            text: "failure",
            code: 0
        }
    },
    acceptedFileTypes:
        "text/plain,text/csv,application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/zip,application/x-zip-compressed",
    fileUploadURL:
        "http://localhost:9090/files/upload/dropzonewrapper/autosubmit",
    maxFilesAllowed: 5,
    maxFilesize: 40,
    duplicateFileMessage: "Duplicate File",
    maxUploadMessage: "max upload limit",
    tooBigFileMessage: "File is too big",
    invalidFileTypeMessage: "Invalid file type",
    uploadFailedMessage: "Upload failed",
    minLengthCampaignName: 7,
    maxLengthCampaignName: 50,
    pattern_validation: "^[a-zA-Z0-9_-\\s]+$",
    maxMobileCount: 25,
    UTC: 330,
    timePickerTimeDifference: 10
};
