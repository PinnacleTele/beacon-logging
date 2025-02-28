import { getCurrencySymbol } from '@angular/common';
import { Component, Input, OnInit } from '@angular/core';
import { ControlContainer } from '@angular/forms';
import { LocalStorageService } from 'src/app/authentication/local-storage.service';
import { BILL_CONSTANTS } from '../../billing-constants';

@Component({
  selector: 'app-dlt-rate',
  templateUrl: './dlt-rate.component.html',
  styleUrls: ['./dlt-rate.component.css']
})
export class DltRateComponent implements OnInit {
  dltRateFormGroup: any;
  currencyType: string;
  currencySymbol: string;
  userDetails: any;
  dltRateInfoText = "DLT charges to be deducted.";
  @Input() apiError = false;
  @Input() loading = false;
  constructor(private parentControl: ControlContainer,private localStorageService:LocalStorageService) { }

  ngOnInit(): void {
    this.dltRateFormGroup = this.parentControl.control;

    this.userDetails = JSON.parse(this.localStorageService.getLocal("user"));
    this.currencyType = this.userDetails.billing_currency;
    this.currencySymbol = getCurrencySymbol(this.currencyType, "narrow");
  }
  get dltRate() {
    return this.dltRateFormGroup.controls.dltRate;
  }
}
