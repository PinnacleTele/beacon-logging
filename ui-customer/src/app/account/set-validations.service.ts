import { Injectable } from '@angular/core';
import { Validators } from '@angular/forms';
import { decimalValidator } from '../campaigns/validators/decimal-validator';
import { maxLengthValidator } from '../campaigns/validators/maxlength-validator';
import { minLengthValidator } from '../campaigns/validators/minlength-validator';
import { noSpaceValidator } from '../campaigns/validators/no-space-validator';
import { CONSTANTS } from '../shared/campaigns.constants';
import { ACCT_CONSTANTS } from './account.constants';
import { CONSTANTS_URL } from 'src/app/shared/compaign.url';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { catchError, map } from 'rxjs/operators';
import { ERROR } from '../campaigns/error.data';
import { BehaviorSubject, throwError } from 'rxjs';
import { LocalStorageService } from '../authentication/local-storage.service';
import { AuthLoginService } from '../authentication/auth-login.service';


@Injectable({
  providedIn: 'root'
})
export class SetValidationsService {
  genericMinimalLength = CONSTANTS.genericMinFieldMinLength;
  genericOptimalMinLength = CONSTANTS.genericOptimalFieldMinLength;
  genericMaxLength = ACCT_CONSTANTS.genericFieldMaxLength;
  addressMaxLength = CONSTANTS.addressMaxLength;
  alphabetsOnlyValidation = ACCT_CONSTANTS.alphabets_only;
  allowedSplChars = CONSTANTS.company_validation;
  wholeNumTotLength = ACCT_CONSTANTS.sms_dlt_rate;
  rowRateWholeNumLength = ACCT_CONSTANTS.row_rate;
  companyMaxLength = CONSTANTS.companyNameMaxLength;
  apiCountryRates: any;
  apiRowRate = 0;
  svcConvRates: any;
  countryList: any;
  BASE_URL = CONSTANTS_URL.GLOBAL_URL;
  EDIT_ACCT_API = CONSTANTS_URL.EDIT_ACCT_API;
  badError: any;

  smsBillRate: any;
  dltBillRate: any;

  constructor(public http: HttpClient,
    private localStorageService:LocalStorageService,
    private authLoginSvc : AuthLoginService) { }
  setValidatorsToAcctInfo(formgroup: any) {

    formgroup.controls.firstName.setValidators([Validators.required,
    minLengthValidator(this.genericMinimalLength),
    maxLengthValidator(this.genericMaxLength),
    Validators.pattern(this.alphabetsOnlyValidation)]);
    formgroup.controls.firstName.updateValueAndValidity();


    formgroup.controls.lastName.setValidators([Validators.required,
    minLengthValidator(this.genericMinimalLength),
    maxLengthValidator(this.genericMaxLength),
    Validators.pattern(this.alphabetsOnlyValidation)]);
    formgroup.controls.lastName.updateValueAndValidity();

    formgroup.controls.company.setValidators([Validators.required,
    minLengthValidator(this.genericMinimalLength),
    maxLengthValidator(this.companyMaxLength),
    Validators.pattern(this.allowedSplChars)]);
    formgroup.controls.company.updateValueAndValidity();

    formgroup.controls.address.setValidators([minLengthValidator(this.genericOptimalMinLength),
    maxLengthValidator(this.addressMaxLength),
      //Validators.pattern(this.allowedSplChars)
    ]);
    formgroup.controls.address.updateValueAndValidity();


  }
  setValidatorsToDLTFields(formgroup: any, selectedAcctType: string) {

    if (+selectedAcctType == 1 || selectedAcctType == "admin") {
      formgroup.controls.allocatedTG.setValidators(Validators.required);
      formgroup.controls.allocatedTG.updateValueAndValidity();
    }

    formgroup.controls.assignedTG.setValidators(Validators.required);
    formgroup.controls.assignedTG.updateValueAndValidity();
  }
  setValidatorsToServices(formgroup: any) {

    formgroup.controls.subServices.setValidators(Validators.required);
    formgroup.controls.subServices.updateValueAndValidity();
  }


  getUserBillingRateForValidation(acctId: any) {

    return this.http.get(this.BASE_URL + this.EDIT_ACCT_API + acctId).pipe(map((responseData) => {
      return responseData
    }), catchError((err) => {
      return throwError(err)
    })

    )
  }

  disableButtonToBillRate = new BehaviorSubject<boolean>(false);
  disableButtonSpinner = new BehaviorSubject<boolean>(false);

  populateBillRates(formGroup: any) {
    this.callRefreshTokenOnce();
   /*  this.disableButtonToBillRate.next(false);
    this.disableButtonSpinner.next(true);
    let userData = this.localStorageService.getLocal('user');
    let jsonData = JSON.parse(userData);
    const userid = jsonData.cli_id;
    this.getUserBillingRateForValidation(userid).subscribe((data: any) => {
      this.disableButtonToBillRate.next(false);
      this.disableButtonSpinner.next(false);
      this. updateLocalStorage(data);
      this.smsBillRate = data.smsrate;
      this.dltBillRate = data.dltrate;
      this.setValidatorsToWalletRates(formGroup);
    },
      (error: HttpErrorResponse) => {
        console.log("error populatebillrates")
        this.disableButtonToBillRate.next(true);
        this.disableButtonSpinner.next(false);
      }); */
  }

  forCreateAccountGetAinfo() {
    this.callRefreshTokenOnce();
   //Code commented for userinfo to be taken from token change
    /* 
    this.disableButtonToBillRate.next(false);
    this.disableButtonSpinner.next(true);
    
    this.getUserBillingRateForValidation(userid).subscribe((data: any) => {
      this.disableButtonToBillRate.next(false);
      this.disableButtonSpinner.next(false);
     this. updateLocalStorage(data);
      this.smsBillRate = data.smsrate;
      this.dltBillRate = data.dltrate;
    },
      (error: HttpErrorResponse) => {
        console.log("error forcreateAcctgetAinfo")
        this.disableButtonToBillRate.next(true);
        this.disableButtonSpinner.next(false);
      }); */
  }

  callRefreshTokenOnce(){
    console.log("callRefreshToken method")
    this.disableButtonToBillRate.next(false);
    this.disableButtonSpinner.next(true);
      this.authLoginSvc.setRefreshToken().
      subscribe((res: any) => {
          if (res) {
            this.disableButtonToBillRate.next(false);
            this.disableButtonSpinner.next(false);
              let userData = this.localStorageService.getLocal('user');
              let jsonData = JSON.parse(userData);
              const userid = jsonData.cli_id;
              this.smsBillRate = jsonData.smsrate;
              this.dltBillRate = jsonData.dltrate;
              console.log(this.smsBillRate, this.dltBillRate)
          }
      },
          (error: HttpErrorResponse) => {
              this.disableButtonToBillRate.next(true);
              this.disableButtonSpinner.next(false);
          });

  }


  setValidatorsToWalletRates(formgroup: any) {
    formgroup.controls.DLTRate.setValidators([
      Validators.required,
      decimalValidator("GTzero", this.wholeNumTotLength),
      Validators.min(this.dltBillRate)
    ]);
    formgroup.controls.DLTRate.updateValueAndValidity();

    formgroup.controls.SMSRate.setValidators([
      Validators.required,
      decimalValidator("GTzero", this.wholeNumTotLength), Validators.min(this.smsBillRate)
    ]);
    formgroup.controls.SMSRate.updateValueAndValidity();
  }

  setValidationRowRate(formgroup: any) {

    formgroup.controls.rowRate.setValidators([

      Validators.required,

      decimalValidator("GTzero", this.rowRateWholeNumLength)

    ]);

    formgroup.controls.rowRate.updateValueAndValidity();

  }
  setValidatorsToMsgSettings(formgroup: any) {
    // formgroup.controls.country.setValidators(Validators.required);
    // formgroup.controls.country.updateValueAndValidity();

    formgroup.controls.tz.setValidators(Validators.required);
    formgroup.controls.tz.updateValueAndValidity();

    formgroup.controls.newlineChar.setValidators([
      maxLengthValidator(3),
      noSpaceValidator(),
    ]);
    formgroup.controls.newlineChar.updateValueAndValidity();

  }





}
