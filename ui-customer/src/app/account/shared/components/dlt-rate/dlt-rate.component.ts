import { getCurrencySymbol } from '@angular/common';
import { Component, Input, OnInit } from '@angular/core';
import { ControlContainer } from '@angular/forms';
import { ACCT_CONSTANTS } from 'src/app/account/account.constants';
import { EditAccountService } from 'src/app/account/edit-account/edit-account.service';
import { LocalStorageService } from 'src/app/authentication/local-storage.service';
import { CONSTANTS } from 'src/app/shared/campaigns.constants';

@Component({
  selector: 'app-dlt-rate',
  templateUrl: './dlt-rate.component.html',
  styleUrls: ['./dlt-rate.component.css']
})
export class DLTRateComponent implements OnInit {
  @Input() canEdit = false;
  @Input() fromPage = "createAcct";
  @Input() create = false;

  currencyType = CONSTANTS.currency;
  currencyFormat = CONSTANTS.curencyFormat;
  currencySymbol : any;
  DLTRateFormGroup: any;

  decimalValidation = CONSTANTS.decimalNum_validation;
  decimalNumTotLength = CONSTANTS.decimalTotalLength;
  DLTRateFieldName = ACCT_CONSTANTS.FIELD_NAMES.DLTRate;
  DLTRateInfoText = ACCT_CONSTANTS.INFO_TXT.DLTRate;


  constructor(
    private parentControl: ControlContainer,private localStorageService : LocalStorageService,
    private editAcctService : EditAccountService) {
  }
  ngOnInit(): void {
    this.DLTRateFormGroup = this.parentControl.control;
    this.DLTRateInfoText = this.canEdit ? ACCT_CONSTANTS.INFO_TXT.DLT_Rate : ACCT_CONSTANTS.INFO_TXT.DLTRate;
    
    if(this.fromPage == "createAcct"){
       const user = this.localStorageService.getLocal("user");
       const userDetails = JSON.parse(user);
       //need to refresh token??
       this.currencyType = userDetails.billing_currency;
    }else{
      this.currencyType = this.editAcctService.getCurrencyType();
    }
    
   
    this.currencySymbol = getCurrencySymbol(this.currencyType, "narrow");

    if (!this.DLTRate.value && this.create) {
       
      
      const user =this.localStorageService.getLocalValue();
      const dltrate = user.dltrate;
      let rate;
     if (dltrate == undefined) {
       rate = 0;
     }else{
      rate = dltrate;
     }
      
      
      this.DLTRateFormGroup.controls.DLTRate.setValue(rate);

    }
    
  }

  get DLTRate() {
    return this.DLTRateFormGroup.controls.DLTRate;
  }
  

}