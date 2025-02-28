import {
  HttpClient,
  HttpEventType,
  HttpHeaders,
  HttpResponse
} from "@angular/common/http";
import { Component, EventEmitter, OnInit, Output, OnDestroy, Input } from "@angular/core";
import { ControlContainer } from "@angular/forms";
import { Subscription } from "rxjs";
import { UtilityService } from "src/app/core/utility.service";
import { CONSTANTS, value } from "../campaigns.constants";
import { UploadService } from "../service/upload.service";

@Component({
  selector: "app-single-file-upload",
  templateUrl: "./single-file-upload.component.html",
  styleUrls: ["./single-file-upload.component.css"]
})
export class SingleFileUploadComponent implements OnInit, OnDestroy {
  fileName = "";
  fileSize: number;
  tooltip: any;
  totalSize: any;
  @Output() responseEmitter = new EventEmitter();
  progressBarWidth: number = 0;
  status = "";
  //progressPercentage : any;
  errorMsg: string;
  progressTextContent: string;
  subscription2: Subscription;
  openClearModal = false;
  @Input() fileUpload: any;

  file: any;
  cancelModal: boolean = false;
  FILE_SIZE_LIMIT = CONSTANTS.singleFileMaxSize;
  fileReqdText = CONSTANTS.REQD_TEXT.singleFileUpload;
  fileInfoText = CONSTANTS.INFO_TXT.singleFileUpload;
  myOptions = value;
  constructor(private uploadSvc: UploadService,
    private utilitySvc: UtilityService,
    private controlContainer: ControlContainer,
  ) { }

  ngOnChanges() {
    console.log("inside ngonchanges .....")
    if (this.fileUpload == "reset") {
      console.log("inside if .....")
      this.fileUpload = false;
    }
  }

  ngOnInit(): void {

    //this.progressPercentage = document.querySelector(".progress-text");
    console.log(this.fileUpload, "init")

    /*if(this.fileUpload){
      this.cancelModal = true;
      this.status= "success";
      this.progressBarWidth = 100;
//count, count_human
      this.fileName = this.fileUpload.filename;
      this.progressTextContent = this.fileUpload.count_human;
      this.totalSize = this.fileUpload.count;
      this.tooltip = this.fileUpload.filesize;

    }*/

  }
  httpOptions = {
    headers: new HttpHeaders({
      "Content-Type": "application/json"
    })
  };

  onChange(event: any) {


    this.fileSize = 0;
    this.status = 'success';
    this.file = event.target.files[0];
    this.cancelModal = false;
    this.fileSize = this.file.size;
    if (this.file.type == 'application/x-zip-compressed') {
      this.fileUpload = false;
      this.status = 'failure';
      this.fileName = "zip-compressed";
      this.handleError("Un supported Format ");
    } else if (this.file.type == 'text/plain') {
      this.fileUpload = false;
      this.status = 'failure';
      this.fileName = "txt file";
      this.handleError("Un supported Format ");

    }
    else if (event.count == "0") {
      this.fileUpload = false;
      this.status = 'failure';
      this.fileName = event.filename;
      console.log(this.fileName)
      this.handleError("Zero Contact File ");
    }


    else {


      let fileReader = new FileReader();
      fileReader.onload = (e) => {
      }

      const userName = JSON.parse(localStorage.getItem('user')).user
      this.fileName = this.file.name;

      const formData = new FormData();
      formData.append("frompage", "template");
      formData.append("username", userName);
      formData.append("file", this.file);
      this.fileUpload = true;
      console.log(this.fileName, "1")
      this.subscription2 = this.uploadSvc.uploadWithProgress(formData)
        .subscribe(event => {


          if (event.type == HttpEventType.UploadProgress) {
            const percentDone = Math.round(100 * event.loaded / event.total);
            this.progressBarWidth = Math.round(100 * event.loaded / event.total)
            this.progressTextContent = `${Math.round(100 * event.loaded / event.total)}%`;
          } else if (event instanceof HttpResponse) {
            if (event.status === 200 && event.body.code === undefined) {
              if (event.body.count == "0") {
                this.cancelModal = false;
                this.fileUpload = false;
                this.status = "failure";
                this.handleError("zero contact file");
              }
              else {
                this.fileUpload = true;
                console.log(this.fileName, "2")
                this.cancelModal = true;
                this.progressTextContent = event.body.count_human
                this.totalSize = event.body.count

                this.tooltip = this.formatBytes(this.fileSize);
                event.body.filesize = this.tooltip;

                var fileDetails = event.body;

                this.status = "success";
                this.responseEmitter.emit(fileDetails);
                //this.singleFileUploadFormGroup.controls.file.setValue(this.fileName);
              }
            } else {
              this.cancelModal = false;
              this.fileUpload = false;
              this.status = "failure";
              this.handleError(event.body.message);

            }
          }
        }, (err) => {
          this.fileUpload = false;
          this.cancelModal = false; this.handleError("Server error");
        })
    }

  }


  openModal() {
    this.openClearModal = true;
  }

  clearModalResponse(response: string) {
    if (response === "clear") {
      this.removeButtonClicked();
      this.openClearModal = false;
    }
    if (response === "close") {
      this.openClearModal = false;
    }
  }

  handleError(errText: string) {
    this.status = "failure";
    this.errorMsg = errText;
    this.responseEmitter.emit("error");
  }

  onClickUploadFile() {
    // Simulate the click on button as if the Input type as File is clicked
    this.file = null;
    if (document.getElementById("fileId")) {
      const fileInput = document.getElementById(
        "fileId"
      ) as HTMLInputElement;
      fileInput.click();
    }
  }

  removeButtonClicked() {
    this.progressBarWidth = 0;
    this.fileUpload = false;
    this.fileSize = 0;
    this.file = [" "]
    if (this.subscription2) {
      this.subscription2.unsubscribe();
    }

    this.responseEmitter.emit("error");
  }

  cancel() {

    this.progressBarWidth = 0;
    this.fileSize = 0;
    this.file = [" "];
    this.fileUpload = false;
    this.cancelModal = true;
    this.status = "failure";
    this.errorMsg = "Upload canceled"
    if (this.subscription2) {
      this.subscription2.unsubscribe();
    }

    this.responseEmitter.emit("error");
  }
  ngOnDestroy() {
    if (this.subscription2) {
      this.subscription2.unsubscribe();
    }
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
}
