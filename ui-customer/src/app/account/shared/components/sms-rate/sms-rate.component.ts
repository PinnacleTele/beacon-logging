import { getCurrencySymbol } from '@angular/common';
import { Component, Input, OnInit } from '@angular/core';
import { ControlContainer } from '@angular/forms';
import { ACCT_CONSTANTS } from 'src/app/account/account.constants';
import { EditAccountService } from 'src/app/account/edit-account/edit-account.service';
import { LocalStorageService } from 'src/app/authentication/local-storage.service';
import { CONSTANTS } from 'src/app/shared/campaigns.constants';

@Component({
  selector: 'app-sms-rate',
  templateUrl: './sms-rate.component.html',
  styleUrls: ['./sms-rate.component.css']
})
export class SMSRateComponent implements OnInit {

  SMSRateFormGroup: any;
  @Input() canEdit = false;
  @Input() fromPage = "createAcct";
  @Input() create = false;

  decimalValidation = CONSTANTS.decimalNum_validation;
  decimalNumTotLength = CONSTANTS.decimalTotalLength;
  SMSRateFieldName = ACCT_CONSTANTS.FIELD_NAMES.SMSRate;
  SMSRateInfoText = ACCT_CONSTANTS.INFO_TXT.SMSRate;
  currencyType: string;
  currencySymbol: string;
  constructor(
    private parentControl: ControlContainer,
    private localStorageService : LocalStorageService,
    private editAcctService : EditAccountService) {
  }

  ngOnInit(): void {
    this.SMSRateFormGroup = this.parentControl.control;
    this.SMSRateInfoText = this.canEdit ? ACCT_CONSTANTS.INFO_TXT.SMS_Rate : ACCT_CONSTANTS.INFO_TXT.SMSRate;
    //need to refresh token??
    if(this.fromPage == "createAcct"){
      const user = this.localStorageService.getLocal("user");
       const userDetails = JSON.parse(user);
       this.currencyType = userDetails.billing_currency;
    }else{
      this.currencyType = this.editAcctService.getCurrencyType();
    }
    
    this.currencySymbol = getCurrencySymbol(this.currencyType, "narrow");
    
    if (!this.SMSRate.value && this.create ) {  
      //need to refresh token??    
      const user = this.localStorageService.getLocalValue();
      const smsRate = user.smsrate;
      let rate;
      if (smsRate == undefined) {
        rate = 0;
      }else{
       rate = smsRate;
      }
      this.SMSRateFormGroup.controls.SMSRate.setValue(rate);

    }

  }

  ngAfterViewInit() {
    const path = window.location.href;
    if(document.getElementById("smsRate") && path.includes("accounts/new")){
      document.getElementById("smsRate").focus();
    }
    
  }

  get SMSRate() {
    return this.SMSRateFormGroup.controls.SMSRate;
  }
}