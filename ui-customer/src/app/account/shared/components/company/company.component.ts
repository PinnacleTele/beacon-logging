import { Component, OnInit } from '@angular/core';
import { ControlContainer, FormBuilder } from '@angular/forms';
import { ACCT_CONSTANTS } from 'src/app/account/account.constants';
import { CONSTANTS } from 'src/app/shared/campaigns.constants';

@Component({
  selector: 'app-company',
  templateUrl: './company.component.html',
  styleUrls: ['./company.component.css']
})
export class CompanyComponent implements OnInit {

  companyFormGroup : any;
  genericMinimalLength = CONSTANTS.genericMinFieldMinLength;
  genericOptimalMinLength = CONSTANTS.genericOptimalFieldMinLength;
  genericMaxLength = CONSTANTS.companyNameMaxLength;

  allowedSplChars = CONSTANTS.pattern_validation;
  companyFieldName =  ACCT_CONSTANTS.FIELD_NAMES.company;
  companyInfoText =  ACCT_CONSTANTS.INFO_TXT.company;


  constructor( private controlContainer: ControlContainer) {
  }

  ngOnInit(): void {

    this.companyFormGroup =  this.controlContainer.control;

  }

  get company(){
    return this.companyFormGroup.controls.company;
  }
}
