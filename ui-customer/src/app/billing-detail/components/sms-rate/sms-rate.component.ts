import { getCurrencySymbol } from '@angular/common';
import { Component, Input, OnInit } from '@angular/core';
import { ControlContainer } from '@angular/forms';
import { LocalStorageService } from 'src/app/authentication/local-storage.service';
import { BILL_CONSTANTS } from '../../billing-constants';

@Component({
  selector: 'app-sms-rate',
  templateUrl: './sms-rate.component.html',
  styleUrls: ['./sms-rate.component.css']
})
export class SMSRateComponent implements OnInit {

  constructor(private parentControl: ControlContainer,private localStorageService:LocalStorageService) { }
  SMSRateFormGroup: any;
  currencyType: string;
  currencySymbol: string;
  userDetails: any;
  @Input() disableField = true;

  // decimalValidation = CONSTANTS.decimalNum_validation;
  // decimalNumTotLength = CONSTANTS.decimalTotalLength;
  // SMSRateFieldName = ACCT_CONSTANTS.FIELD_NAMES.SMSRate;
  SMSRateInfoText = "Amount that will be deducted per sms.";

  ngOnInit(): void {

    this.SMSRateFormGroup = this.parentControl.control;

    this.userDetails = JSON.parse(this.localStorageService.getLocal("user"));
    this.currencyType = this.userDetails.billing_currency;
    this.currencySymbol = getCurrencySymbol(this.currencyType, "narrow");

  }
  @Input() apiError = false;
  @Input() loading = false;

  get SMSRate() {
    return this.SMSRateFormGroup.controls.SMSRate;
  }
  press() {
    this.SMSRateFormGroup.updateValueAndValidity();
  }
}
