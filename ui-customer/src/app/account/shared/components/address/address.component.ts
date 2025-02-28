import { Component, OnInit } from '@angular/core';
import { ControlContainer, FormBuilder } from '@angular/forms';
import { ACCT_CONSTANTS } from 'src/app/account/account.constants';
import { CONSTANTS } from 'src/app/shared/campaigns.constants';

@Component({
  selector: 'app-address',
  templateUrl: './address.component.html',
  styleUrls: ['./address.component.css']
})
export class AddressComponent implements OnInit {

  addressFormGroup : any;
  genericMinimalLength = CONSTANTS.genericMinFieldMinLength;
  genericOptimalMinLength = CONSTANTS.genericOptimalFieldMinLength;
  
  addressMaxLength = CONSTANTS.addressMaxLength;
  addrInfoText = ACCT_CONSTANTS.INFO_TXT.address;
  allowedSplChars = CONSTANTS.pattern_validation;
  firstNameFieldName = ACCT_CONSTANTS.FIELD_NAMES.firstName;
  lastNameFieldName =  ACCT_CONSTANTS.FIELD_NAMES.lastName;
  companyFieldName =  ACCT_CONSTANTS.FIELD_NAMES.company;
  firstNameInfoText =  ACCT_CONSTANTS.INFO_TXT.firstName;
  lastNameInfoText =  ACCT_CONSTANTS.INFO_TXT.lastName;
  companyInfoText =  ACCT_CONSTANTS.INFO_TXT.company;
  
  
 

  constructor(private fb: FormBuilder, private controlContainer: ControlContainer) {
  }

  ngOnInit(): void {
 
    this.addressFormGroup =  this.controlContainer.control;
    
  }
  get address(){
    return this.addressFormGroup.controls.address ;
  }
}