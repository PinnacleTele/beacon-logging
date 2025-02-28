import {
    AfterViewInit,
    Component,
    EventEmitter,
    Input,
    OnChanges,
    OnDestroy,
    OnInit,
    Output,
    SimpleChanges,
    ViewChild
} from "@angular/core";
import {
    DropzoneConfigInterface,
    DropzoneDirective
} from "ngx-dropzone-wrapper";
import { UtilityService } from "src/app/core/utility.service";
import { ControlContainer } from "@angular/forms";
import { LocalStorageService } from "src/app/authentication/local-storage.service";
import { CONSTANTS_URL } from "../compaign.url";
import { CONSTANTS, value } from "../campaigns.constants";
import { TemplateCampaignService } from "src/app/campaigns/campaign-template/service/template-campaign.service";
@Component({
    selector: "app-file-uploader",
    templateUrl: "./file-uploader.component.html",
    styleUrls: ["./file-uploader.component.css"]
})
export class FileUploaderComponent implements AfterViewInit, OnInit, OnChanges, OnDestroy {
    constructor(
        private utility: UtilityService,
        public templateCampaignService: TemplateCampaignService,
        public controlContainer: ControlContainer,
        public localStorage: LocalStorageService
    ) { }

    //  BASE_URL = CONSTANTS_URL.GLOBAL_URL;
    @Input() hideDublicate = false;

    @Input() telcoValue: any;

    @Input() campaignType: string;

    myOptions = value

    API_URL = CONSTANTS_URL.COMPAIGN_UPLOAD_FILE;

    MULTI_FILE_URL = CONSTANTS_URL.MULTI_FILE_URL;

    dropzone: any;

    @ViewChild(DropzoneDirective, { static: false })
    directiveRef?: DropzoneDirective;

    @ViewChild(FileUploaderComponent, { static: false })
    fileupload: FileUploaderComponent;

    public fileUploaderFormGroup: any;

    public removeDuplicateForm: any;

    // public vlshorternForm: any;

    // shortner: any;

    // shortners: any;

    newUserParams: any;

    newURL = "";

    @Input() fileContentOrder: string;

    @Input() reset: boolean = false;

    @Input() dltForm: any;

    @Input() isDlt = false;

    @Output() responseEmitter = new EventEmitter();

    // valid files details
    filesMap = new Map();

    // invalid files details
    failedFiles: any = [];

    succeedFiles: any = [];

    // accepted files details - sent to parent component
    response: any = {};

    // NOTE: Taken from props
    acceptedFileTypes: string = CONSTANTS.ACCEPTED_FILE_TYPES;

    maxFilesAllowed: number = CONSTANTS.MAX_FILES_ALLOWED;

    maxFilesize: number = CONSTANTS.MAX_FILE_SIZE;

    duplicateFileMessage: string = CONSTANTS.DUPLICATE_FILE_MESSAGE; // preview template has tailwind css - lowercase. hence always shown in lowercase.

    maxFilesMessage: string = CONSTANTS.MAX_UPLOAD_MESSAGE;

    tooBigFileMessage: string = CONSTANTS.TOO_BIG_FILE_MESSAGE;

    invalidFileTypeMessage: string = CONSTANTS.INVALID_FILE_TYPE_MESSAGE;

    uploadFailedMessage: string = CONSTANTS.UPLOAD_FAILED_MESSAGE;

    username = JSON.parse(`${this.localStorage.getLocal("user")}`).user;

    @Input() fromPage = "";

    @Input() templateId = "";

    userParams = { frompage: this.fromPage, username: this.username };

    fileLImit = 4;

    maxFiles = 4;

    fileLimit = 5;


    contentShow = 5;

    @Input() dltTemplateSelectionType: string;

    @Input() dltTemplateSelectionMobileColumn: string;

    @Input() setErrorColumn: boolean = false;

    mobileNumbersCount = 0;

    get count() {
        let temp = 0;
        this.succeedFiles.forEach(element => {
            temp += parseInt(element.total)
        });
        return temp
    }

    source = "Files";

    openClearModal = false;

    borderStyle = "border-gray-300";

    message = "";

    messageColor = "text-gray-500";

    public autoSubmitConfig: DropzoneConfigInterface = {
        url: this.newURL,


        maxFilesize: null,


        acceptedFiles: this.acceptedFileTypes,
        accept: (file, done) => {
            this.onAccept(file, done);
        },
        autoProcessQueue: true,
        parallelUploads: 10,
        clickable: true,

        dictMaxFilesExceeded: this.maxFilesMessage,
        // dictFileTooBig: this.tooBigFileMessage,
        dictInvalidFileType: this.invalidFileTypeMessage,
        dictResponseError: this.uploadFailedMessage,
        previewsContainer: "#previews",
        previewTemplate: `
<div></div>

    `
    };

    /** Lifecycle hook that is called after this component's view has been fully initialized */
    ngAfterViewInit() {
        this.dropzone = this.directiveRef?.dropzone();
    }



    ngOnInit() {
        this.fileUploaderFormGroup = this.controlContainer.control;
        this.removeDuplicateForm = this.controlContainer.control;
        // this.vlshorternForm = this.controlContainer.control;

        // let vlshortner1 = this.localStorage.getLocal("user");
        // this.shortner = JSON.parse(vlshortner1);
        // this.shortners = this.shortner.vl_shortner;
        if (this.fileUploaderFormGroup.controls.files.value) {
            this.succeedFiles = this.fileUploaderFormGroup.controls.files.value;
        }

        console.log(this.reset);

        if (this.fromPage === "CT") {
            this.getCLIid();
            this.autoSubmitConfig.params = {
                frompage: "campaign",
                username: this.username,
                cli_id: this.cliId,
                temp_id: this.templateId
            };
            this.autoSubmitConfig.url =
                CONSTANTS_URL.FILE_UPLOAD_URL + CONSTANTS_URL.MULTI_FILE_CT_URL;
        } else if (this.fromPage === "Telco") {
            this.autoSubmitConfig.params = {
                username: this.username,
                telco: this.telcoValue
            };
            this.autoSubmitConfig.url =
                CONSTANTS_URL.FILE_UPLOAD_URL +
                CONSTANTS_URL.DLT_FILE_UPLOAD_URL;

        } else if (this.fromPage === "template") {
            this.getCLIid();
            this.autoSubmitConfig.params = {
                frompage: "campaign",
                username: this.username,
                cli_id: this.cliId,
                temp_id: this.templateId
            };
            this.autoSubmitConfig.url =
                CONSTANTS_URL.FILE_UPLOAD_URL + CONSTANTS_URL.MULTI_FILE_CT_URL;
        } else if (this.fromPage === "dlttemplate") {
            this.getCLIid();
            if (!this.setErrorColumn) {
                this.autoSubmitConfig.clickable = true;
            } else {
                this.autoSubmitConfig.clickable = false;
            }
            this.autoSubmitConfig.params = {
                frompage: "campaign",
                username: this.username,
                t_type: this.dltTemplateSelectionType,
                mobile: this.dltTemplateSelectionMobileColumn
            };
            this.autoSubmitConfig.url =
                CONSTANTS_URL.FILE_UPLOAD_URL + CONSTANTS_URL.DLT_TEMPLATE_FILE_CT_URL;
        }
        else {
            this.newUserParams = {
                frompage: this.fromPage,
                username: this.username
            };
            this.autoSubmitConfig.url =
                CONSTANTS_URL.FILE_UPLOAD_URL + CONSTANTS_URL.MULTI_FILE_URL;
        }
    }

    cliId = "";

    getCLIid() {
        const user = this.localStorage.getLocal("user");
        let userObj = null;

        if (user) {
            userObj = JSON.parse(user);
        }

        this.cliId = userObj.cli_id;
    }

    ngOnChanges(changes: SimpleChanges): void {
        // code commented for template preview display with dz type file upload
        // this.autoSubmitConfig.params={ frompage: this.fromPage, username: this.username }
        // this.autoSubmitConfig.url=CONSTANTS_URL.FILE_UPLOAD_URL+CONSTANTS_URL.MULTI_FILE_URL
        // console.log(this.dltTemplateSelectionType, this.dltTemplateSelectionMobileColumn);
        console.log(this.reset, 'inside ngonchanges');

        if (!this.reset) {
            if (this.fromPage === "CT") {
                this.getCLIid();
                this.autoSubmitConfig.params = {
                    frompage: "campaign",
                    username: this.username,
                    cli_id: this.cliId,
                    temp_id: this.templateId
                };
                this.autoSubmitConfig.url =
                    CONSTANTS_URL.FILE_UPLOAD_URL +
                    CONSTANTS_URL.MULTI_FILE_CT_URL;
            } else if (this.fromPage === "Telco") {
                this.autoSubmitConfig.params = {
                    username: this.username,
                    telco: this.telcoValue
                };
                this.autoSubmitConfig.url =
                    CONSTANTS_URL.FILE_UPLOAD_URL +
                    CONSTANTS_URL.DLT_FILE_UPLOAD_URL;
            } else if (this.fromPage === "template") {
                this.getCLIid();
                this.autoSubmitConfig.params = {
                    frompage: "campaign",
                    username: this.username,
                    cli_id: this.cliId,
                    temp_id: this.templateId
                };
                this.autoSubmitConfig.url =
                    CONSTANTS_URL.FILE_UPLOAD_URL + CONSTANTS_URL.MULTI_FILE_CT_URL;
            } else if (this.fromPage === "dlttemplate") {
                this.getCLIid();
                if (this.setErrorColumn) {
                    this.autoSubmitConfig.clickable = false;
                } else {
                    this.autoSubmitConfig.clickable = true;
                }
                this.autoSubmitConfig.params = {
                    frompage: "campaign",
                    username: this.username,
                    t_type: this.dltTemplateSelectionType,
                    mobile: this.dltTemplateSelectionMobileColumn
                };
                this.autoSubmitConfig.url =
                    CONSTANTS_URL.FILE_UPLOAD_URL + CONSTANTS_URL.DLT_TEMPLATE_FILE_CT_URL;
            }
            else {
                this.autoSubmitConfig.params = {
                    frompage: this.fromPage,
                    username: this.username
                };
                this.autoSubmitConfig.url =
                    CONSTANTS_URL.FILE_UPLOAD_URL +
                    CONSTANTS_URL.MULTI_FILE_URL;
            }
        } else {
            console.log(this.reset, 'inside ngonchanges');
            this.directiveRef?.reset(true);
            this.filesMap.clear();
            this.failedFiles = [];
            this.succeedFiles = [];
            this.uploadStatus = false;
            this.mobileNumbersCount = 0;
            this.responseEmitter.emit(this.succeedFiles);
        }
    }

    ctRemoveFile() {
        console.log('ct remove files');
        this.directiveRef?.reset(true);
        this.filesMap.clear();
        this.failedFiles = [];
        this.succeedFiles = [];
        this.uploadStatus = false;
        this.mobileNumbersCount = 0;
        this.responseEmitter.emit(this.succeedFiles);
    }

    get senderId() {
        return this.removeDuplicateForm.get("removeDuplicates");
    }

    get vlshortner() {
        return this.vlshortner.controls.vlShortner;
    }

    onChange(event: any) {

        const duplicates = event.target.checked;

        this.removeDuplicateForm.controls.removeDuplicates.setValue(duplicates);
    }



    // To help upload files on click icons
    openFileSelectionWindow() {
        // CU-165
        if (this.fromPage == 'dlttemplate') {
            if (!this.setErrorColumn) {
                this.autoSubmitConfig.clickable = true;
                this.openWindow();
            } else {
                this.autoSubmitConfig.clickable = false;
                this.templateCampaignService.setErrorToMobileColumn(true);
            }
        } else {
            this.openWindow();
        }
    }

    openWindow() {
        if (!this.dropzone.hiddenFileInput) {
            this.dropzone = this.directiveRef?.dropzone();
        }
        this.dropzone.hiddenFileInput.click();
    }

    fileuploadset() {

        const focus = document.getElementById("fileupload");
        const focus1 = document.getElementById("fileupload1");
        if (focus) {
            focus.scrollIntoView();
        }
        if (focus1) {
            focus1.scrollIntoView();
        }
    }

    // Checks if the file is duplicate
    onAccept(file: any, done: Function) {

        if (this.fromPage == 'dlttemplate') {
            if (this.setErrorColumn) {
                this.templateCampaignService.setErrorToMobileColumn(true);
            } else {
                this.onAcceptFile(file, done)
            }
        } else {
            this.onAcceptFile(file, done);
        }


        //  }

    }

    onAcceptFile(file: any, done: Function) {
        if (!this.filesMap.has(file.name)) {

            done();
        } else {
            done(this.duplicateFileMessage);
        }
    }

    reValidate() {
        if (this.isDlt) {
            console.log("revalidation");
            this.filesMap.clear();
            let tempSucceed = [];
            let tempFailed = [];
            if (this.dltTemplateSelectionType == 'column') {
                this.succeedFiles.forEach(element => {
                    let columns = Object.keys(element.forColumn)
                    if (columns.includes(this.dltTemplateSelectionMobileColumn.toLowerCase()) || columns.includes(this.dltTemplateSelectionMobileColumn.toUpperCase())) {
                        tempSucceed.push(element);
                        this.filesMap.set(element.name, element);
                    }
                    else {
                        element.error = `Missing Mobile Column ${this.dltTemplateSelectionMobileColumn} `
                        tempFailed.push(element)
                    }
                });
                this.failedFiles.forEach(element => {
                    if (!(element.error == this.duplicateFileMessage || element.error == "server Error")) {
                        let columns = Object.keys(element.forColumn)
                        if (columns.includes(this.dltTemplateSelectionMobileColumn.toLowerCase()) || columns.includes(this.dltTemplateSelectionMobileColumn.toUpperCase())) {
                            if (element.error == this.duplicateFileMessage) {
                                tempFailed.push(element)
                            }
                            else {
                                if (tempSucceed.length >= 1) {
                                    tempSucceed.forEach(ele => {
                                        if (ele.name == element.name) {
                                            element.error = "Duplicate File"
                                            tempFailed.push(element)
                                        }
                                        else {
                                            tempSucceed.push(element);
                                            this.filesMap.set(element.name, element)
                                        }
                                    })
                                }
                                else {

                                    tempSucceed.push(element)
                                    this.filesMap.set(element.name, element)
                                }
                            }
                        }
                        else {
                            element.error = `Missing Mobile Column ${this.dltTemplateSelectionMobileColumn} `
                            tempFailed.push(element)
                        }
                    }
                    else {
                        tempFailed.push(element)
                    }
                });
                this.succeedFiles = tempSucceed;
                this.failedFiles = tempFailed;
            }
            else {
                this.succeedFiles.forEach(element => {
                    let columns = Object.keys(element.forIndex)
                    if (columns.includes(this.dltTemplateSelectionMobileColumn)) {
                        tempSucceed.push(element);
                        this.filesMap.set(element.name, element);
                    }
                    else {
                        element.error = `Missing Mobile Index ${this.dltTemplateSelectionMobileColumn} `
                        tempFailed.push(element)
                    }
                });

                this.failedFiles.forEach(element => {
                    if (!(element.error == this.duplicateFileMessage || element.error == "server Error")) {
                        let columns = Object.keys(element.forIndex)
                        if (columns.includes(this.dltTemplateSelectionMobileColumn)) {
                            if (element.error == this.duplicateFileMessage) {
                                tempFailed.push(element)
                            }
                            else {
                                if (tempSucceed.length >= 1) {
                                    tempSucceed.forEach(ele => {
                                        if (ele.name == element.name) {
                                            element.error = "Duplicate File"
                                            tempFailed.push(element)
                                        }
                                        else {
                                            tempSucceed.push(element);
                                            this.filesMap.set(element.name, element)
                                        }
                                    })
                                }
                                else {

                                    tempSucceed.push(element)
                                    this.filesMap.set(element.name, element)
                                }
                            }
                        }
                        else {
                            element.error = `Missing Mobile Index ${this.dltTemplateSelectionMobileColumn} `
                            tempFailed.push(element)
                        }
                    }
                    else {
                        tempFailed.push(element)
                    }
                });
                this.succeedFiles = tempSucceed;
                this.failedFiles = tempFailed;
            }
            this.emitResponse();
            this.totalCount();
            this.validateDropzoneArea();
        }
    }

    progressingFiles: any[] = [];

    uploadStatus = false;




    onFilesAdded(files: any) {
        if (this.fromPage == 'dlttemplate') {
            if (this.setErrorColumn) {
                this.templateCampaignService.setErrorToMobileColumn(true);
            } else {
                this.onFileAdd(files);
            }
        } else {
            this.onFileAdd(files);
        }
    }

    onFileAdd(files: any) {
        for (const file of files) {
            this.uploadStatus = true;

           // Add only valid files to the list and show in preview
           if (this.succeedFiles.length <= this.fileLimit) {
            if (file.accepted && !this.filesMap.has(file.name)) {
                console.log(files[0].type,files.type);
                
                 if (files[0].type == 'application/x-zip-compressed') {
                    
                 }else{
                    this.filesMap.set(file.name, null);
                }
               

                const rawFile = {
                    name: file.name,
                    no_of_contacts: this.formatBytes(file.size),
                    rawFile: true,
                    progressBar: 0
                };
                this.fileLImit += 1;
                this.progressingFiles.push(rawFile);
            }
        }
    
        }
        console.log(this.filesMap);

    }

    progressPercentage: any;

    progressBar: any;

    progressFiles = 0;

    progressBarWidth: number;

    totalfiles = 0;

    uploadProgress(event: any) {
        console.log('inside upload progress');

        this.utility.fileUploadProgress.next(true);
        this.progressFiles += 1;
        const index = this.progressingFiles.findIndex(
            (item: any) => item.name === event[0].name
        );
        if (this.progressingFiles[index])
            this.progressingFiles[index].progressBar = Math.round(event[1]);
        if (event[0].previewElement) {

            this.progressPercentage = event[0].previewElement.querySelector(
                ".progress-text"
            );

            if (event[0].upload.progress >= 100) {
            }
        }
    }

    onFileRemoved(file: any) {
        // Add only valid files to the list and show in preview

        if (this.fromPage == 'dlttemplate') {
            if (this.setErrorColumn) {
                this.templateCampaignService.setErrorToMobileColumn(true);
            } else {
                this.onRemovedFile(file);
            }

        } else {
            this.onRemovedFile(file);
        }
    }

    onRemovedFile(file: any) {
        if (this.filesMap.has(file.originalname)) {
            this.mobileNumbersCount -= parseInt(file.total);
            this.filesMap.delete(file.originalname);

            this.succeedFiles.splice(this.succeedFiles.indexOf(file), 1);
            this.totalfiles = this.succeedFiles.length;
            this.emitResponse();
        }
    }
    onFailedFileRemoved(file) {

        this.failedFiles.splice(this.failedFiles.indexOf(file), 1);
        this.emitResponse();
        // }
    }
    removeInvalidFile(index: number) {
        this.failedFiles.splice(index, 1);
    }

    removeAllFiles() {
        this.directiveRef?.reset(true);
        this.filesMap.clear();
        this.failedFiles = [];
        this.succeedFiles = [];
        this.uploadStatus = false;
        this.mobileNumbersCount = 0;
        this.emitResponse();
    }
    clearFailedFiles() {
        this.failedFiles = [];
        this.emitResponse();
    }
    inValidFiles: any[];

    ValidFiles: any = [];

    excessFile: any = [];

    onUploadSuccess(event: any) {
        const { name, size } = event[0];
        this.ValidFiles = event[1].uploaded_files.success;
        this.ValidFiles.forEach((element: any) => {
            if (this.succeedFiles.length > 0 && this.succeedFiles.length < this.fileLimit) {
                const found = this.succeedFiles.some(el => el.name === element.filename);
                if (!found){
                    console.log('!found');
                    
                    this.fileMapSetCall(element,size);
                    if (element.count == 0) {
                        const file = {
                            name: element.filename,
                            error: "Zero Contact"
                        };
                        this.filesMap.delete(file.name);
                        this.failedFiles.push(file);
                        this.emitResponse();
                    } else {
                        const file = this.fileSetCall(element,size);
                        this.succeedFiles.push(file);
                        this.totalfiles = this.succeedFiles.length;
                    }
                }
        } else if(this.succeedFiles.length == 0) {
            console.log('else  part 0');
            
            this.fileMapSetCall(element,size);
            if (element.count == 0) {
                const file = {
                    name: element.filename,
                    error: "Zero Contact"
                };
                this.filesMap.delete(file.name);
                this.failedFiles.push(file);
                this.emitResponse();
            } else {
                const file = this.fileSetCall(element,size);
                this.succeedFiles.push(file);
                this.totalfiles = this.succeedFiles.length;
            }
        }
        });

        for (let i = 0; i < this.progressingFiles.length; i++) {
            if (
                this.progressingFiles[i].name == name &&
                this.progressingFiles[i].rawFile == true
            ) {
                this.progressingFiles.splice(i, 1);
                this.fileLImit -= 1;
            }
        }

        this.mobileNumbersCount += event[1].total;
        this.excessFile = event[1].uploaded_files.success;
        this.excessFile.splice(0, 4);
        this.excessFile.forEach((element: any) => {
            const file = {
                name: element.filename,
                error: "Max File Count Reached"
            };

            this.failedFiles.push(file);
        });
        event[1].uploaded_files.failed.forEach((element: any) => {
            const file = this.fileSetCall(element,size);
            this.failedFiles.push(file);
            this.filesMap.delete(file.name);
        });
        this.utility.fileUploadProgress.next(false);
    }

    fileSetCall(element:any,size:any){
       const fileset = {
            name: element.filename,
            error: element.error,
            message: element.message,
            forColumn: element.placeholders_column,
            forIndex: element.placeholders_index,
            originalname: element.filename,
            r_filename: element.r_filename,
            total: element.count,
            no_of_contacts: element.count_human,
            progressBar: 100,
            rawFile: false,
            size: this.formatBytes(size),
            numericSize: size,
            isValidFile: element?.isValidFile,
            missing: element?.missing,
            placeholders: element.placeholders,
            misingParams: element.missing,
            isStatic: element.isStatic,
        };
        return fileset;
    }

    fileMapSetCall(element:any,size:any){
        this.filesMap.set(element.filename, {
            originalname: element.filename,
            r_filename: element.r_filename,
            total: element.count,
            totalHuman: element.count_human,
            size: this.formatBytes(size),
            numericSize: size,
            isValidFile: element?.isValidFile,
            missing: element?.missing,
            placeholders: element.placeholders,
            misingParams: element.missing,
            isStatic: element.isStatic,
            forColumn: element.placeholders_column,
            forIndex: element.placeholders_index
        });
     }

    onUploadError(event: any) {
        console.log('onuploaderror');
        console.log(event);

        this.uploadStatus = true;

        for (let i = 0; i < this.succeedFiles.length; i++) {
            if (this.succeedFiles[i].rawFile == true) {
                this.succeedFiles.splice(i, 1);
            }
        }
        this.totalfiles = this.succeedFiles.length;
        for (let i = 0; i < this.progressingFiles.length; i++) {
            if (
                this.progressingFiles[i].rawFile == true &&
                this.progressingFiles[i].name == event[0].name
            ) {
                this.progressingFiles.splice(i, 1);
            }
        }

        if (this.fromPage == 'dlttemplate') {
            if (!this.setErrorColumn) {
                this.failedFilesValidate(event);
            } else {

                this.templateCampaignService.setErrorToMobileColumn(true);

            }
        } else {
            this.failedFilesValidate(event);
        }


    }

    failedFilesValidate(event) {
        // if dont want to list duplicate/error files then remove them from dom as below
        // preparing duplicate/error files list - limit it to max 10 to avoid too many files listing
        if (this.failedFiles.length < 10) {
            let file: any;
            if (event[2] == undefined) {
                file = {
                    name: event[0].name,

                    error: event[1]
                    // error: event[1]
                };
            } else {
                file = {
                    name: event[0].name,

                    error: "server Error"
                    // error: event[1]
                };
            }
            this.failedFiles.push(file);
            console.log(this.failedFiles);


            // if (
            //     file.error &&
            //     this.filesMap.has(file.name)
            // ) {
            //     this.filesMap.delete(file.name);
            //     this.emitResponse();
            // }
        }
    }
    onQueueCompleted() {
        if (
            this.dropzone.getQueuedFiles().length === 0 &&
            this.dropzone.getUploadingFiles().length === 0
        ) {
            this.emitResponse();
        }
    }
    sendDisable = new EventEmitter<boolean>();

    // NOTE:: This method transfers the selected files list
    emitResponse() {
        console.log(this.succeedFiles.length, this.failedFiles.length);

        if (this.succeedFiles.length == 0 && this.failedFiles.length == 0 && this.progressingFiles.length == 0) {
            this.uploadStatus = false;
        }
        this.response.files = this.succeedFiles;
        this.validateDropzoneArea();
        this.fileUploaderFormGroup.controls.files.setValue(this.response);
        this.responseEmitter.emit(this.response);
        if (this.failedFiles.length > 0 && this.succeedFiles.length == 0) {
            this.sendDisable.emit(true)
        }
        else {
            this.sendDisable.emit(false)
        }
    }

    divClick() {

        if (this.fromPage == 'dlttemplate') {
            if (!this.setErrorColumn) {
                this.autoSubmitConfig.clickable = true;
            } else {
                this.autoSubmitConfig.clickable = false;
                this.templateCampaignService.setErrorToMobileColumn(true);

            }
        } else {
            this.autoSubmitConfig.clickable = true;
        }

        console.log('div click');

    }

    onNavigate(event: any) {
    }

    openModal() {
        this.openClearModal = true;
    }

    clearModalResponse(response: string) {
        if (response === "clear") {
            this.removeAllFiles();
            this.openClearModal = false;
        }
        if (response === "close") {
            this.openClearModal = false;
        }
    }

    validateDropzoneArea() {

        if (this.succeedFiles.length == 0) {
            this.borderStyle = "border-red-300";
            this.message = "At least 1 valid file is required";
            this.messageColor = "text-red-600";
        } else {
            this.borderStyle = "border-gray-300";

            this.messageColor = "text-gray-500";
        }
    }

    resetTheDropZone() {

        this.borderStyle = "border-gray-300";

        this.messageColor = "text-gray-500";
        this.failedFiles = [];
        this.succeedFiles = [];
        this.filesMap.clear();
        this.mobileNumbersCount = 0;
        this.emitResponse();
    }

    formatBytes(bytes: any, decimals = 2): string {
        if (bytes === 0) return "0 Bytes";

        const k = 1024;
        const dm = decimals < 0 ? 0 : decimals;
        const sizes = ["Bytes", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"];

        const i = Math.floor(Math.log(bytes) / Math.log(k));

        return `${parseFloat((bytes / Math.pow(k, i)).toFixed(dm))} ${sizes[i]
            }`;
    }

    kFormatter(num: any) {
        if (num > 999 && num < 1000000) {
            return parseFloat((num / 1000).toFixed(2)) + "k";
        } else if (num >= 1000000) {
            return parseFloat((num / 1000000).toFixed(2)) + "M";
        } else if (num < 900) {
            return num;
        }
    }
    cancel(event: any) {
        const files: any[] = this.dropzone.files;
        files.forEach((item) => {
            if (item.name === event.name) {
                this.dropzone.removeFile(item);
                this.filesMap.delete(event.name);

            }
        });
        this.utility.fileUploadProgress.next(false)
    }
    ngOnDestroy() {
        this.directiveRef?.reset(true);
    }
    shallowEqual(object1, object2) {
        const keys1 = Object.keys(object1);
        const keys2 = Object.keys(object2);
        if (keys1.length !== keys2.length) {
            return false;
        }
        for (let key of keys1) {
            if (object1[key] !== object2[key]) {
                return false;
            }
        }
        return true;
    }
    totalCount() {
        this.mobileNumbersCount = 0
        this.succeedFiles.forEach(element => {
            this.mobileNumbersCount += element.total;
        });
    }

}
