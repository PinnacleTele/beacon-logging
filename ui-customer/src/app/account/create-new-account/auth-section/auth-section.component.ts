import { Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges } from '@angular/core';
import { ControlContainer, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { event } from 'jquery';
import { Subscription } from 'rxjs';
import { emailValidator } from 'src/app/campaigns/validators/email-validator';
import { maxLengthValidator } from 'src/app/campaigns/validators/maxlength-validator';
import { minLengthValidator } from 'src/app/campaigns/validators/minlength-validator';
import { CONSTANTS } from 'src/app/shared/campaigns.constants';
import { ACCT_CONSTANTS } from '../../account.constants'
import { AccountService } from '../../account.service';

@Component({
  selector: 'app-auth-section',
  templateUrl: './auth-section.component.html',
  styleUrls: ['./auth-section.component.css']
})
export class AuthSectionComponent implements OnInit, OnDestroy {
  @Input() uniqueErr: boolean;
  spinner = false;
  @Output() pageError = new EventEmitter<boolean>();
  minMobileLength = CONSTANTS.generalMobileMinLength;
  maxMobileLength = CONSTANTS.generalMobileMaxLength;
  genericMinLength = CONSTANTS.genericMinFieldMinLength;
  genericMaxLength = ACCT_CONSTANTS.genericFieldMaxLength;
  emailMaxLength = CONSTANTS.emailMaxLength;
  mobileValidation = CONSTANTS.mobile_pattern_validation;
  emailValidation = CONSTANTS.emailPattern;
  emailPatternMsg = CONSTANTS.ERROR_DISPLAY.emailPatternMsg

  userNameValidation = ACCT_CONSTANTS.userNamePattern;
  userNameFieldName = ACCT_CONSTANTS.FIELD_NAMES.userName;
  contactMobileFieldName = ACCT_CONSTANTS.FIELD_NAMES.contactMobile;
  contactEmailFieldName = ACCT_CONSTANTS.FIELD_NAMES.contactEmail;
  userNameInfoText = ACCT_CONSTANTS.INFO_TXT.userName;
  contactMobileInfoText = ACCT_CONSTANTS.INFO_TXT.contactMobile;
  contactEmailInfoText = ACCT_CONSTANTS.INFO_TXT.contactEmail;
  enable2FA = false;
  page3FormGroup: any;
  apiError = false;
  uniqueNameError = CONSTANTS.ERROR_DISPLAY.userNameUnique;
  subscription: Subscription;
  constructor(private fb: FormBuilder,
    private parentControl: ControlContainer,
    private accountService: AccountService) {
  }

  ngOnInit(): void {

    this.page3FormGroup = this.parentControl.control;
    this.setValidatorsToFormFields();
    this.accountService.markAsUntouched(this.page3FormGroup);
    this.apiCallToCHkUserName();
    this.enable2FA = this.page3FormGroup.controls.twofa.value;


  }

  ngAfterViewInit() {
    if (document.getElementById("userName")) {
      document.getElementById("userName").focus();
    }

  }

  onClickEnable2FA(event: any) {

    this.enable2FA = !this.enable2FA;
    this.page3FormGroup.controls.twofa.setValue(this.enable2FA);
  }
  chkUniqueUserNameExists() {
  //  this.setUserNameValidators();
    this.apiCallToCHkUserName();
  }

  apiCallToCHkUserName() {
    this.apiError = false;
    const dotrim = (this.userName?.value as string).trim();
    this.userName.setValue(dotrim);
    const cNameField = dotrim;
    if (
      cNameField != null &&
      cNameField.length > 2) {
      let cname = (this.userName.value as string).toLowerCase().trim();
      //this.pageError.emit(true);
      this.spinner = true;
      this.page3FormGroup.controls.userName.setErrors({
        apiRequestError: true
      });
      this.subscription = this.accountService
        .checkUniqueUserNames(cname)
        .subscribe(
          res => {

            this.spinner = false;
            //   this.uniqueErr = false;
            // this.page3FormGroup.controls.userName.setErrors(null);
            this.pageError.emit(false);
            delete this.page3FormGroup.controls['userName'].errors['apiRequestError'];
            this.page3FormGroup.controls['userName'].updateValueAndValidity();
            if (res.statusCode > 299 || res.statusCode < 200) {
              
              
             
            } else if (!res.isUnique) {
              this.page3FormGroup.controls.userName.setErrors({
                userNameExists: true
              });
            }
          },
          (err) => {
            this.spinner = false;
            this.apiError = true;
            this.pageError.emit(false);
            this.page3FormGroup.controls.userName.setErrors({
              apiRequestError: true
            });
          }

        );
    }
  }
  get userName() {
    return this.page3FormGroup.controls.userName;
  }
  get contactMobile() {
    return this.page3FormGroup.controls.contactMobile;
  }
  get contactEmail() {
    return this.page3FormGroup.controls.contactEmail;
  }
  // get countryCode() {
  //   return this.page3FormGroup.controls.countryCode;
  // }
  setValidatorsToFormFields() {

    this.setUserNameValidators();

    this.contactMobile.setValidators([
      Validators.required,


      Validators.pattern(this.mobileValidation),
    ]);
    // this.countryCode.setValidators([
    //   Validators.required, Validators.maxLength(7), Validators.minLength(1)

    // ]);
    this.contactMobile.updateValueAndValidity();

    this.contactEmail.setValidators([Validators.required,
      // Validators.pattern(this.emailValidation),

    ]);
    this.contactEmail.updateValueAndValidity();
  }

  setUserNameValidators() {

    this.userName.setValidators([Validators.required,
    minLengthValidator(this.genericMinLength),
    maxLengthValidator(this.genericMaxLength),
    Validators.pattern(this.userNameValidation)]);
    // if (this.userName.errors?.userNameExists) {
    //   this.userName.setErrors({ userNameExists: null });
    //     }
    // if (this.userName.errors?.apiRequestError) {
    //   this.userName.setErrors({ apiRequestError: null });
    // }
    console.log(this.userName.errors);
    
    this.userName.updateValueAndValidity();

  }



  onBlurEmailValidate() {
    if (this.contactEmail.value != "") {
      this.contactEmail.setValidators([Validators.required,
      //        Validators.pattern(this.emailValidation),
      emailValidator(),
      maxLengthValidator(this.emailMaxLength)
      ]);
      this.contactEmail.updateValueAndValidity();
    }

  }
  ngOnDestroy() {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }
  codes = [{ code: 91, name: "+91 india" }, { code: 1, name: "+1 canada" }, { code: 213, name: "+213 ALGERIA" }, { code: 244, name: "+244 ANGOLA" }]

}
