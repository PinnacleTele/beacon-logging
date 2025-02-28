import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { Subscription } from 'rxjs';
import { LocalStorageService } from 'src/app/authentication/local-storage.service';
import { GroupModel } from 'src/app/campaigns/model/campaign-group-model';
import { maxLengthValidator } from 'src/app/campaigns/validators/maxlength-validator';
import { minLengthValidator } from 'src/app/campaigns/validators/minlength-validator';
import { CONSTANTS } from 'src/app/shared/campaigns.constants';
import { GroupSliderService } from 'src/app/shared/service/group-slider.service';
import { TGroupSliderService } from 'src/app/shared/service/template-group-slider.service';
import { ACCT_CONSTANTS } from '../account.constants';
import { AccountService } from '../account.service';
import { SetValidationsService } from '../set-validations.service';
import { SubServices } from '../shared/model/service-model';
import { TemplateGroup } from '../shared/model/template-group-model';

@Component({
  selector: 'create-new-account',
  templateUrl: './create-new-account.component.html',
  styleUrls: ['./create-new-account.component.css']
})
export class CreateNewAccountComponent implements OnInit, OnDestroy {
  genericMinimalLength = CONSTANTS.genericMinFieldMinLength;
  genericOptimalMinLength = CONSTANTS.genericOptimalFieldMinLength;
  genericMaxLength = ACCT_CONSTANTS.genericFieldMaxLength;
  alphabetsOnlyValidation = ACCT_CONSTANTS.alphabets_only;
  allowedSplChars = CONSTANTS.pattern_validation;

  genericMinLength = CONSTANTS.genericMinFieldMinLength;
  mobileValidation = CONSTANTS.mobile_pattern_validation;
  userNameValidation = ACCT_CONSTANTS.userNamePattern;
  userNameFieldName = ACCT_CONSTANTS.FIELD_NAMES.userName;
  contactMobileFieldName = ACCT_CONSTANTS.FIELD_NAMES.contactMobile;
  contactEmailFieldName = ACCT_CONSTANTS.FIELD_NAMES.contactEmail;
  userNameInfoText = ACCT_CONSTANTS.INFO_TXT.userName;
  contactMobileInfoText = ACCT_CONSTANTS.INFO_TXT.contactMobile;
  contactEmailInfoText = ACCT_CONSTANTS.INFO_TXT.contactEmail;
  enable2FA = false;
  //userNameChkErr = false;


  alphabletsOnly = ACCT_CONSTANTS.alphabets_only;
  alphabetsErrorPatter = CONSTANTS.pattern_validation;

  minLengthError = CONSTANTS.ERROR_DISPLAY.fieldMinLength;
  maxLengthError = CONSTANTS.ERROR_DISPLAY.fieldMaxLength;
  allowedPattern = "";
  allocatedGroups: TemplateGroup[] = [];
  assignedGroups: TemplateGroup[] = [];
  groups: TemplateGroup[] = [];
  disablePrev = true;
  userNameErr = false;
  response: { message: string, statusCode: number }
  status: string;
  popup = false;
  creating = false;
  pageError = false;
  subscription: Subscription;

  page = 1;
  showCreateModal = false;
  showCancelModal = false;
  page4formError: boolean;
  enableNext = true;
  eachPage = ["page1", "page2", "page3", "page4", "page5", "page6"]
  isPageValid: boolean[] = [];
  createAcctForm = this.fb.group({
    accountType: [],
    firstName: ['', [
      Validators.required,
      minLengthValidator(this.genericMinimalLength),
      maxLengthValidator(this.genericMaxLength),
      Validators.pattern(this.alphabetsOnlyValidation)
    ],
    ],
    lastName: ['', [
      Validators.required,
      minLengthValidator(this.genericMinimalLength),
      maxLengthValidator(this.genericMaxLength),
      Validators.pattern(this.alphabetsOnlyValidation)
    ]],
    address: ['', [
      minLengthValidator(this.genericOptimalMinLength),
      maxLengthValidator(this.genericMaxLength),
      Validators.pattern(this.allowedSplChars)
    ]],
    company: ['', [
      Validators.required,
      minLengthValidator(this.genericMinimalLength),
      maxLengthValidator(this.genericMaxLength),
      Validators.pattern(this.allowedSplChars)
    ]],

    //page3

    userName: [''],
    // countryCode: [null],
    contactMobile: [''],
    contactEmail: [''],
    twofa: [],

    //page4
    walletAmount: [],
    SMSRate: [],
    DLTRate: [],
    tz: [,],
    zone: [],
    rowRate: [],
    //country: [CONSTANTS.COUNTRY, Validators.required],
    newlineChar: [],
    encrytMob: [],
    encryMsg: [],
    otherCountriesBillRates: this.fb.array,

    //DLT section page5
    allocatedTG: [],
    assignedTG: [],
    templateGroups: [],

    subServices: [''],
    charset1: ['UTF-8',],
    sharedGroups: [],
    groups: [],
    currency: [,],



  });



  constructor(public fb: FormBuilder,
    private accountService: AccountService,
    private router: Router,
    private localStorageService: LocalStorageService,
    private groupSliderService: TGroupSliderService,
    private setValidatorService: SetValidationsService,
    private sharedGroupService: GroupSliderService) {
    this.accountService.populateBillRates();
  }

  ngOnInit(): void {

    this.setValidatorService.forCreateAccountGetAinfo();
    const user =JSON.parse(localStorage.user);
    this.createAcctForm.controls.SMSRate.setValue(user.smsrate);
    this.createAcctForm.controls.DLTRate.setValue(user.dltrate);

  }

  getUserSettings() {
    const user = this.localStorageService.getLocal('user');
    let userObj = null;

    if (user) {
      userObj = JSON.parse(user);
    }
    if (userObj.bill_type === 1) {
    }
  }


  createAcctSections =
    [
      { key: "01", value: "Account Type" },
      { key: "02", value: "Account Info" },
      { key: "03", value: "Auth Details" },
      { key: "04", value: "Services" },
      { key: "05", value: "Settings" },
      { key: "06", value: "DLT section" },

    ];

  closeCreateModal() {
    this.showCreateModal = false;
  }


  handlePageResponse(event: any) {
    if (event) {
      this.enableNext = true;
    }
  }

  clickEditLink() {
    this.router.navigate(['/accounts/edit'])
  }
  pageErrorSet(event) {
    this.page4formError = event;
  }

  nextClicked() {

    this.enableNext = this.validatePage(this.page)
    if (this.enableNext) {

      if (this.page == 5 && !this.page4formError && !this.pageError) {

        this.isPageValid[this.page - 1] = this.enableNext;

        this.page = this.page + 1;

      } else if (this.page == 1 || this.page == 2 || this.page == 3 || this.page == 4) {
        this.isPageValid[this.page - 1] = this.enableNext;

        this.page = this.page + 1;

      } else if (this.page > 5) {
        this.submitForm();
      }

    } else {
      // console.log("next is not enabled",this.pageError );
    }
    this.enableNext = false;

  }

  prevClicked() {
    this.accountService.clearAllValidators(this.createAcctForm);

    this.pageError = false;
    if (this.page > 1) {
      this.page = this.page - 1;
      this.disablePrev = false;
      if (this.page == 1) {
        this.disablePrev = true;
      }
    } else {
      this.disablePrev = true;
    }
  }

  goToPage(i: number) {
    if (i + 1 <= this.page) {
      this.page = i + 1;
    }
  }
  validatePage(currentPage: number) {
    this.createAcctForm.updateValueAndValidity();

    if (this.page == 1) {
      this.disablePrev = false;

      return true;
    } else {
      const pageNumb = this.eachPage[currentPage - 1];
      let isPageValid = this.createAcctForm.valid;
      /* if(this.page == 4 && isPageValid){
         this.updateFiledValidity(this.createAcctForm)
       }*/
      this.accountService.validateAllFormFields(this.createAcctForm);
      //console.log("ispage valid", isPageValid)
      return isPageValid
    }
  }

  updateFiledValidity(accountForm: FormGroup) {
    const formGroup = accountForm;
    Object.keys(formGroup.controls).forEach((field) => {

      const control = formGroup.get(field);

      if (control instanceof FormControl) {
        console.log(field)
        control.updateValueAndValidity();
      }
      if (control instanceof FormArray) {
        for (const control1 of control.controls) {
          if (control1 instanceof FormControl) {

            control.updateValueAndValidity();
          }
          if (control1 instanceof FormGroup) {
            this.updateFiledValidity(control1);
          }
        }
      }
    });
  }
  tryAgain(event: any) {
    this.popup = false;
    this.submitForm();
  }

  modalcontinue(event: boolean) {
    this.router.navigate(["/accounts"]);
  }
  modalClose(event: boolean) {
    this.popup = false;
  }

  submitForm() {
    this.creating = true;
    let form = this.createAcctForm;
    let twofaNumb: number = 0;
    if (this.createAcctForm.controls.twofa.value == true) {
      twofaNumb = 1;
    } else { twofaNumb = 0; }
    let encrytMobNumb: number = 0;
    if (this.createAcctForm.controls.encrytMob.value == true) {
      encrytMobNumb = 1;
    } else { encrytMobNumb = 0; }
    let encryMsgNumb: number = 0;
    if (this.createAcctForm.controls.encryMsg.value == true) {
      encryMsgNumb = 1;
    } else { encryMsgNumb = 0; }

    let formAllocatedValues = form.controls.allocatedTG.value;
    let allocatedIds: number[] = [];
    formAllocatedValues.forEach((ele: TemplateGroup) => {

      allocatedIds.push(ele.template_group_id);
    });
    // change that made for get latest currency after hit ainfo api 
      const usercurrency = JSON.parse(this.localStorageService.getLocal('user'))
      let currency = usercurrency.billing_currency;
      //console.log("currency value ", currency);
      let charset1Field = "";
      let assignedIds: number;
      let formAssignedValues : TemplateGroup[] = form.controls.assignedTG.value;
      const assignId = formAssignedValues[0].template_group_id
      assignedIds = assignId ;

      let formSharedValues = form.controls.sharedGroups.value;
      let sharedGroupsId: string[] = [];

      if(formSharedValues ){
        formSharedValues.forEach((ele: GroupModel)=>{
          sharedGroupsId.push(ele.id)
        });

    }

    let userType = form.controls.accountType.value;
    let userNum = 1;
    if (userType === 'admin') {
      userNum = 1;
    } else {
      userNum = 2;
    }

    let tzDisplayName = form.get('tz').value;
    const offset = tzDisplayName.replace("UTC", "");
    var withoutParanthesis = offset.substring(offset.indexOf('(') + 1, offset.indexOf(')'));

    let mobileNumber = form.controls.contactMobile.value;
    if (mobileNumber.charAt(0) == "+") {
      mobileNumber = mobileNumber.substring(1);
    }
    let firstNameFieldValue = form.controls.firstName.value;
    if (firstNameFieldValue) {

      firstNameFieldValue = firstNameFieldValue.trim();
    }
    let lastNameFieldValue = form.controls.lastName.value;
    if (lastNameFieldValue) {
      lastNameFieldValue = lastNameFieldValue.trim();
    }
    let companyFieldValue = form.controls.company.value;
    if (companyFieldValue) {
      companyFieldValue = companyFieldValue.trim();
    }
    let userNameFieldValue = form.controls.userName.value;
    if (userNameFieldValue) {
      userNameFieldValue = userNameFieldValue.trim();
    }
    let emailFieldValue = form.controls.contactEmail.value;
    if (emailFieldValue) {
      emailFieldValue = emailFieldValue.trim();
    }
    let addressFieldValue = form.controls.address.value;
    if (addressFieldValue) {
      addressFieldValue = addressFieldValue.trim();
    }
    // const user = this.localStorageService.getLocal('user');
    //	const userDetails = JSON.parse(user);
    let intlFlagEnabled: any;

    let intlRatesArr = [];
    const selectedServices = form.controls.subServices.value as Array<SubServices>
    selectedServices.forEach((ele: any) => {
      if (ele.sub_service === 'international') {
        intlFlagEnabled = true;
      }
    });
    if (intlFlagEnabled == 1) {

      intlRatesArr = form.controls.otherCountriesBillRates.value;

      intlRatesArr.push({
        "country": "ROW",
        "smsrate": form.controls.rowRate.value
      });
    }

    let data = {
      "user_type": userNum,
      "firstname": firstNameFieldValue,
      "lastname": lastNameFieldValue,
      "company": companyFieldValue,
      "username": userNameFieldValue,
      "email": emailFieldValue,
      "billing_currency": currency,
      "mobile": mobileNumber,
      "zone_name": form.controls.zone.value,
      "offset": withoutParanthesis,
      //"country_code_iso3": form.controls.country.value,
      "allocated_tgroup_ids": allocatedIds,
      "assigned_tgroup_id": assignedIds,
      "smpp_charset": form.controls.charset1.value,
      "address": addressFieldValue,
      "twofa_yn": twofaNumb,
      "wallet": Number(form.controls.walletAmount.value),
      "smsrate": Number(form.controls.SMSRate.value),
      "dltrate": Number(form.controls.DLTRate.value),
      "newline_chars": form.controls.newlineChar.value,
      "encrypt_mobile_yn": encrytMobNumb,
      "encrypt_message_yn": encryMsgNumb,
      "assigned_groups": sharedGroupsId,
      "intl_rates": intlRatesArr,
      "services": form.controls.subServices.value as Array<SubServices>,
    }

    this.disablePrev = true;
    this.subscription = this.accountService.sendCreateAccount(data).subscribe(
      (res: any) => {
        let apiRes = "";

        if (res.statusCode < 0) {
          this.disablePrev = true
          this.response = res
          this.status = res.message
          this.creating = false;
          this.popup = true;
          this.page = 3;
          this.userNameErr = true;


        }
        if (res.statusCode > 199 || res.statusCode < 299) {
          this.creating = false;
          this.disablePrev = false;
          this.response = res
          this.status = res.message
          this.popup = true;
        }
      }, (error: HttpErrorResponse) => {
        this.disablePrev = false;
        this.creating = false;
        this.response = this.accountService.badError;
        this.status = this.response.message
        this.popup = true;
      }

    );
  }

  backEnter(event: any) {
    if (event.keyCode == 13) {
      this.router.navigate(['/accounts'])
    }
  }

  ngOnDestroy() {
    this.allocatedGroups = [];
    this.assignedGroups = [];
    this.groups.forEach((el) => {
      el.checked = false;
    });
    this.groupSliderService.groupList = this.groups;
    this.groupSliderService.populateSelectedGroups(this.allocatedGroups);
    this.sharedGroupService.groupList = this.groups;
    this.sharedGroupService.populateSelectedGroups(this.assignedGroups);
    this.groupSliderService.assignedGroupId = 0;
    this.setValidatorService.apiCountryRates = undefined;
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }

}



