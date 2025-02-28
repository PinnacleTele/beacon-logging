import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { LocalStorageService } from 'src/app/authentication/local-storage.service';
import { maxLengthValidator } from 'src/app/campaigns/validators/maxlength-validator';
import { minLengthValidator } from 'src/app/campaigns/validators/minlength-validator';
import { CONSTANTS } from 'src/app/shared/campaigns.constants';
import { GroupsManagementService } from '../groups-management.service';

@Component({
  selector: 'app-group-edit',
  templateUrl: './group-edit.component.html',
  styleUrls: ['./group-edit.component.css']
})
export class GroupEditComponent implements OnInit, OnDestroy {
  apiError: boolean;

  constructor(private fb: FormBuilder,
    private groupManagementService: GroupsManagementService,
    private router: Router,
    private localStorageService:LocalStorageService
    ) 
    { }

  ngOnDestroy(): void {
    localStorage.removeItem("editGroup");
    if (this.loader) {
      this.loader.unsubscribe();
    }
    if (this.loading) {
      this.loading.unsubscribe();
    }

  }

  group: any;

  groupId: any;

  public minimumLength = CONSTANTS.minimumGroupNameLength;

  public maximumLength = CONSTANTS.maximumGroupNameLength;

  uniqueNameError = CONSTANTS.ERROR_DISPLAY.uniqeGroupName;

  public minLengthError = CONSTANTS.ERROR_DISPLAY.fieldMinLength;

  public maxLengthError = CONSTANTS.ERROR_DISPLAY.fieldMaxLength;

  spinner = false;

  uniqueNameSpinner = false;

  loader = this.groupManagementService.updateLoading.subscribe((data) => { this.spinner = data });

  loading = this.groupManagementService.loadingGroupNames$.subscribe((data) => { this.uniqueNameSpinner = data });

  popUp = false;

  status: any;

  Responce: { message: string, statusCode: number };

  pattern_validation = CONSTANTS.pattern_validation;

  public splCharsError = CONSTANTS.ERROR_DISPLAY.campaignNameSplChars;

  public allowedSplChars = CONSTANTS.allowed_special_characters

  cancelMessage = CONSTANTS.INFO_TXT.groupEditCancelMessage;

  userType: any;

  groupNameInfoTxt = CONSTANTS.INFO_TXT.campaignName;

  skeleton = true;


  ngOnInit(): void {
    this.userType = JSON.parse(this.localStorageService.getLocal("user")).user_type
    this.groupId = JSON.parse(localStorage.getItem("editGroup") + "")
    this.getGroupInfo();
    this.name.valueChanges.pipe(
      debounceTime(400),
      distinctUntilChanged((pre,curr)=>pre===curr.trim()))
      .subscribe(res=>{
      this.uniqueNameExistance(res)
    })

  }

  getGroupInfo() {
    this.skeleton = true;
    this.apiError = false;
    this.uniqueNameSpinner = true;
    // this.editGroupForm.controls.name.setErrors({
    //   apiRequestError: false
    // });
    this.groupManagementService.getGroupInfo(this.groupId).subscribe((res: any) => {
      this.group = res;
      if (this.group.modified_ts) {
      }
      this.editGroupForm.controls.name.setValue(res.g_name)
      this.editGroupForm.controls.visibility.setValue(res.g_visibility)
      this.skeleton = false;
      this.uniqueNameSpinner = false;
    }, (error: HttpErrorResponse) => {
      this.apiError = true;
      this.skeleton = false;
      this.uniqueNameSpinner = false;
      // this.editGroupForm.controls.name.setErrors({
      //   apiRequestError: true
      // });
    })

  }

  editGroupForm = this.fb.group({
    name: [
      "",
      Validators.compose([
        Validators.required,
        minLengthValidator(this.minimumLength),
        maxLengthValidator(this.maximumLength),
        Validators.pattern(this.pattern_validation)
      ])
    ],
    visibility: ["private"],

  });

  get name() {
    return this.editGroupForm.controls.name
  }

  get visibility() {
    return this.editGroupForm.controls.visibility
  }

  tryAgain(event: any) {
    this.getGroupInfo();
  }


  modalClose(event: boolean) {

    this.popUp = false;

  }


  modalcontinue(event: boolean) {
    this.router.navigate(['/groups'])
  }


  uniqueNameExistance(value: any) {
    this.editGroupForm.controls.name.setErrors({
      apiRequestError: false
    });
    let Gname = value.trim();
    this.name.setValue(Gname);
    if (Gname.length > 0) {
      if (this.group.g_name.trim() != Gname && this.name.valid) {

        this.groupManagementService.uniqueNameValidation(Gname)
          .subscribe((data) => {
            if (!data.isUnique) {
              this.editGroupForm.controls.name.setErrors({ groupNameExist: true })
            }
          }, (error: HttpErrorResponse) => {
            this.editGroupForm.controls.name.setErrors({
              apiRequestError: true
            });
          }
          );
      }
    }

  }

  retry() {
    const focus = document.getElementById("name") as HTMLImageElement;
    focus.focus();
    focus.blur();
  }

  validation() {
    // console.log("validation");

    Object.keys(this.editGroupForm.controls).forEach((field) => {
      const control = this.editGroupForm.get(field);
      if (control instanceof FormControl) {
        control.markAsTouched({ onlySelf: true });
      }
    });
    this.cleanForm(this.editGroupForm)
  }

  cleanForm(formGroup: FormGroup) {
    Object.keys(formGroup.controls).forEach((key) => {
      const control = formGroup.get(key);
      if (control instanceof FormControl) {
        control.setValue(control.value.trim())
      }

    }


    );
  }

  backEnter(event: any) {
    this.router.navigate(['/groups']);

  }

  get isNameChanged(){
    return this.name.value.trim() ===this.group?.g_name  || this.uniqueNameSpinner
  }

}
