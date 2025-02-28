import { Component,  OnInit,  } from '@angular/core';
import { ControlContainer, FormBuilder, Validators,   } from '@angular/forms';
import { CONSTANTS } from 'src/app/shared/campaigns.constants';
import { ACCT_CONSTANTS } from '../../account.constants';
import { SetValidationsService } from '../../set-validations.service';

@Component({
  selector: 'app-account-info',
  templateUrl: './account-info.component.html',
  styleUrls: ['./account-info.component.css']
})
export class AccountInfoComponent implements OnInit {
  
  page2FormGroup : any;
  genericMinimalLength = CONSTANTS.genericMinFieldMinLength;
  genericOptimalMinLength = CONSTANTS.genericOptimalFieldMinLength;
  genericMaxLength = CONSTANTS.addressMaxLength;
  alphabetsOnlyValidation = ACCT_CONSTANTS.alphabets_only;
  
  firstNameFieldName = ACCT_CONSTANTS.FIELD_NAMES.firstName;
  lastNameFieldName =  ACCT_CONSTANTS.FIELD_NAMES.lastName;
  companyFieldName =  ACCT_CONSTANTS.FIELD_NAMES.company;
  firstNameInfoText =  ACCT_CONSTANTS.INFO_TXT.firstName;
  lastNameInfoText =  ACCT_CONSTANTS.INFO_TXT.lastName;
  companyInfoText =  ACCT_CONSTANTS.INFO_TXT.company;
  
  
 alphabletsOnly = ACCT_CONSTANTS.alphabets_only;
 alphabetsErrorPatter = CONSTANTS.pattern_validation;
 
 minLengthError = CONSTANTS.ERROR_DISPLAY.fieldMinLength;
 maxLengthError = CONSTANTS.ERROR_DISPLAY.fieldMaxLength;
 allowedPattern = "";

  constructor(private fb: FormBuilder, 
    private setValidatorsSvc : SetValidationsService,
    private controlContainer: ControlContainer) {
  }

  ngOnInit(): void {
 
    this.page2FormGroup =  this.controlContainer.control;
    this.setValidatorsSvc.setValidatorsToAcctInfo(this.page2FormGroup);
    
    //this.page2FormGroup.controls.currency.setValidators([Validators.required]);
    //this.page2FormGroup.controls.currency.updateValueAndValidity();
  }
  get address(){
    return this.page2FormGroup.controls.address ;
  }
  get firstName() {
    return this.page2FormGroup.controls.firstName;
  }
  get lastName() {
    return this.page2FormGroup.controls.lastName;
  }
  get company(){
    return this.page2FormGroup.controls.company;
  }
    
}
