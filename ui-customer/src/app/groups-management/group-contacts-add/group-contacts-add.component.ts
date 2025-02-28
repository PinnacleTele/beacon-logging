import { AfterViewChecked, Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormControl, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { FileUploaderComponent } from 'src/app/shared/file-uploader/file-uploader.component';
import { GroupsManagementService } from '../groups-management.service';
import { CONSTANTS } from "src/app/shared/campaigns.constants";
import { emailValidator } from 'src/app/campaigns/validators/email-validator';
import { HttpErrorResponse } from '@angular/common/http';
import { LocalStorageService } from 'src/app/authentication/local-storage.service';

@Component({
  selector: 'app-group-contacts-add',
  templateUrl: './group-contacts-add.component.html',
  styleUrls: ['./group-contacts-add.component.css']
})
export class GroupContactsAddComponent implements OnInit, AfterViewChecked, OnDestroy {

  constructor(private fb: FormBuilder,
    private router: Router,
    private groupService: GroupsManagementService,
    private localStorageService:LocalStorageService
  ) {


  }
  ngOnDestroy(): void {
    localStorage.removeItem("editContact")
  }



  fileContentOrder = CONSTANTS.GROUP_FILE_CONTENT;

  preference: string = "fileUpload";

  cancelMessage = CONSTANTS.INFO_TXT.groupEditCancelMessage;

  spinner = false;

  group: any;

  groupId: string;

  skeleton = true;

  apiError = false;

  minLength = CONSTANTS.generalMobileMinLength;
  maxLength = CONSTANTS.generalMobileMaxLength;

  @ViewChild(FileUploaderComponent, { static: false })
  dropzoneControl: FileUploaderComponent;

  ngAfterViewChecked(): void {
    this.groupService.setDropzoneControl(this.dropzoneControl);

  }
  userDetail = JSON.parse(this.localStorageService.getLocal("user"))
  ngOnInit(): void {

    this.groupId = JSON.parse(localStorage.getItem("editContact") + "")
    this.groupInfo();



  }

  groupInfo() {
    this.apiError = false;
    this.skeleton = true;
    this.groupService.getGroupInfo(this.groupId).subscribe((res) => {
      this.group = res;
      this.addContactForm.controls.g_id.setValue(this.group.id)
      this.addContactForm.controls.g_type.setValue(this.group.g_type)
      this.skeleton = false;

      this.chooseMode("fileUpload")

    }, (error: HttpErrorResponse) => {


      this.apiError = true;
      this.skeleton = false;
    })
  }
  //addContactForm: any
  addContactForm = this.fb.group({
    files: [""],
    mobile: [""],
    email: ["", emailValidator()],
    name: ["", [Validators.minLength(3), Validators.maxLength(20)]],
    g_id: [""],
    g_type: ["normal"]
  })

  chooseMode(mode: string) {
    this.preference = mode;
    if (mode == "fileUpload") {
      this.addContactForm.controls['mobile'].reset();
      this.addContactForm.controls['name'].reset();
      this.addContactForm.controls['email'].reset();

      this.addContactForm.controls['files'].setValidators([Validators.required]);
      this.addContactForm.controls['mobile'].clearValidators();
      // this. addContactForm.controls['mobile'].markAsPristine()
      this.addContactForm.controls['mobile'].updateValueAndValidity();
      this.addContactForm.controls['files'].updateValueAndValidity();
    }
    else {
      // this.dropzoneControl.resetTheDropZone()
      this.addContactForm.controls['files'].reset();
      this.addContactForm.controls['mobile'].setValidators([Validators.required,
      Validators.pattern(CONSTANTS.mobile_pattern_validation), Validators.minLength(this.minLength), Validators.maxLength(this.maxLength)]); this.addContactForm.controls['files'].clearValidators();
      this.addContactForm.controls['files'].markAsPristine();
      this.addContactForm.controls['mobile'].updateValueAndValidity();
      this.addContactForm.controls['files'].updateValueAndValidity();

    }







  }


  onAdd() {
    this.spinner = true;
    //post api call for files either singleContact 
    setTimeout(() => this.spinner = false, 2000)
  }


  getFileUploadSectionData(event: any) {

    if (this.addContactForm.controls.files) {
      this.addContactForm.controls.files.setValue(event.files)

    }



  }

  // enter to select 

  routing(routepath: string) {
    this.router.navigate([routepath])
  }

  retry(eve) {
    this.groupInfo();
  }
}

