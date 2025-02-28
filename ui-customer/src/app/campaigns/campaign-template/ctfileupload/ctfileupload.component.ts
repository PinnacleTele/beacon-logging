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
import { CONSTANTS_URL } from "src/app/shared/compaign.url";
import { CONSTANTS, value } from "src/app/shared/campaigns.constants";

@Component({
  selector: 'app-ctfileupload',
  templateUrl: './ctfileupload.component.html',
  styleUrls: ['./ctfileupload.component.css']
})
export class CtfileuploadComponent implements OnInit {

  constructor(
    private utility: UtilityService,

    public controlContainer: ControlContainer,
    public localStorage: LocalStorageService
) { }

//  BASE_URL = CONSTANTS_URL.GLOBAL_URL;
@Input() hideDublicate = false;

@Input() telcoValue: any;

@Input() campaignType: string;

myOptions = value;

API_URL = CONSTANTS_URL.COMPAIGN_UPLOAD_FILE;

MULTI_FILE_URL = CONSTANTS_URL.MULTI_FILE_URL;

dropzone: any;

@ViewChild(DropzoneDirective, { static: false })
directiveRef?: DropzoneDirective;



public fileUploaderFormGroup: any;

public removeDuplicateForm: any;

// public vlshorternForm: any;

// shortner: any;

// shortners: any;

newUserParams: any;

newURL = "";

@Input() fileContentOrder: string;

@Input() reset: boolean;

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

contentShow = 5;



mobileNumbersCount = 0;

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
   
  
    this.autoSubmitConfig.params = {
          frompage: "campaign",
          username: this.username
      };
      this.autoSubmitConfig.url =
          CONSTANTS_URL.FILE_UPLOAD_URL + CONSTANTS_URL.SINGLE_FILE_UPLOAD_URL;
  
  
      
   
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
    if (!this.reset) {
    
        this.getCLIid();
         this.autoSubmitConfig.params = {
            frompage: "campaign",
            username: this.username
        };
        this.autoSubmitConfig.url =
            CONSTANTS_URL.FILE_UPLOAD_URL + CONSTANTS_URL.SINGLE_FILE_UPLOAD_URL;
    
  } else {
      this.directiveRef?.reset(true);
      this.filesMap.clear();
      this.failedFiles = [];
      this.succeedFiles = [];
      this.uploadStatus = false;
      this.mobileNumbersCount = 0;
      this.responseEmitter.emit(this.succeedFiles);
  }
}

get senderId() {
    return this.removeDuplicateForm.get("removeDuplicates");
}

// get vlshortner() {
//     return this.vlshortner.controls.vlShortner;
// }

// onChanges(event) {
//     if (this.shortners == 1) {
//         this.shortners = 1;
//     } else {
//         this.shortners = 0;

//     }
// }

onChange(event: any) {

    const duplicates = event.target.checked;

    this.removeDuplicateForm.controls.removeDuplicates.setValue(duplicates);
}



// To help upload files on click icons
openFileSelectionWindow() {
    // CU-165

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
    if (!this.filesMap.has(file.name)) {

        done();
    } else {
        done(this.duplicateFileMessage);
    }
}

progressingFiles: any[] = [];

uploadStatus = false;

onFilesAdded(files: any) {
    for (const file of files) {
        this.uploadStatus = true;

        // Add only valid files to the list and show in preview
        if (this.succeedFiles.length < 6) {
            if (file.accepted && !this.filesMap.has(file.name)) {
                this.filesMap.set(file.name, null);

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
}

progressPercentage: any;

progressBar: any;

progressFiles = 0;

progressBarWidth: number;

totalfiles = 0;

uploadProgress(event: any) {

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
    if (this.filesMap.has(file.originalname)) {
        this.mobileNumbersCount -= parseInt(file.total);
        this.filesMap.delete(file.originalname);

        this.succeedFiles.splice(this.succeedFiles.indexOf(file), 1);
        this.totalfiles = this.succeedFiles.length;
        this.emitResponse();
    }
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

inValidFiles: any[];

ValidFiles: any = [];

excessFile: any = [];

onUploadSuccess(event: any) {
    const { name, size } = event[0];

    // if (event[1].uploaded_files.success.length > 5) {
    //     this.ValidFiles = event[1].uploaded_files.success;
    //     this.ValidFiles.splice(5 - this.succeedFiles.length);

    //     // this.ValidFiles.splice( event[1].uploaded_files.success.length - 1, 4)
    // } else {
      console.log(event,'on success');
      
        // if (this.succeedFiles.length > 0) {
        //     this.ValidFiles = event[1];
        //     this.ValidFiles.splice(5 - this.succeedFiles.length);
        // } else {
           // this.ValidFiles = event[1];
      //  }

        //  this.ValidFiles.slice( 0, 5 - (this.succeedFiles.length + 1) )
   // }

   // this.ValidFiles.forEach((element: any) => {
        this.filesMap.set(event[1].filename, {
            originalname: event[1].filename,
            r_filename: event[1].r_filename,
            total: event[1].count,
            totalHuman: event[1].count_human,
            size: this.formatBytes(size),
            numericSize: size,
            isValidFile: event[1]?.isValidFile,
            missing: event[1]?.missing,
            placeholders: event[1]?.placeholders,
            misingParams: event[1]?.missing,
            isStatic: event[1]?.isStatic
        });

        if (event[1].count == 0) {
            const file = {
                name: event[1].filename,
                error: "Zero Contact"
            };
            this.filesMap.delete(file.name);
            this.failedFiles.push(file);
            this.emitResponse();
        } else {
            const file = {
                originalname: event[1].filename,
                r_filename: event[1].r_filename,
                total: event[1].count,
                no_of_contacts: event[1].count_human,
                progressBar: 100,
                rawFile: false,
                size: this.formatBytes(size),
                numericSize: size,
                isValidFile: event[1]?.isValidFile,
                missing: event[1]?.missing,
                placeholders: event[1]?.placeholders,
                misingParams: event[1]?.missing,
                isStatic: event[1]?.isStatic
            };
            this.succeedFiles.push(file);
            this.totalfiles = this.succeedFiles.length;
          
        }
   // });

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
    // this.excessFile = event[1].uploaded_files.success;
    // this.excessFile.splice(0, 4);
    // this.excessFile.forEach((element: any) => {
    //     const file = {
    //         name: element.filename,
    //         error: "Max File Count Reached"
    //     };

    //     this.failedFiles.push(file);
    // });
    // event[1].uploaded_files.failed.forEach((element: any) => {
    //     const file = {
    //         name: element.filename,
    //         error: element.error
    //     };
    //     this.failedFiles.push(file);
    //     this.filesMap.delete(file.name);
    // });
    this.utility.fileUploadProgress.next(false);
}

onUploadError(event: any) {
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


        if (
            file.error &&
            this.filesMap.has(file.name)
        ) {
            this.filesMap.delete(file.name);
            this.emitResponse();
        }
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

// NOTE:: This method transfers the selected files list
emitResponse() {
    if (this.succeedFiles.length == 0 && this.failedFiles.length == 0) {
        this.uploadStatus = false;
    }
    this.response.files = this.succeedFiles;
    this.validateDropzoneArea();
    this.fileUploaderFormGroup.controls.files.setValue(this.response);
    this.responseEmitter.emit(this.response);
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

kFormatter(num: number) {
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
}

