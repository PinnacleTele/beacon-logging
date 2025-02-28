import { Component, OnInit } from '@angular/core';
import { ControlContainer } from '@angular/forms';
import { ACCT_CONSTANTS } from 'src/app/account/account.constants';
import { CONSTANTS } from 'src/app/shared/campaigns.constants';

@Component({
  selector: 'app-last-name',
  templateUrl: './last-name.component.html',
  styleUrls: ['./last-name.component.css']
})
export class LastNameComponent implements OnInit {
  lastNameFormGroup : any;
  genericMinimalLength = CONSTANTS.genericMinFieldMinLength;
  genericOptimalMinLength = CONSTANTS.genericOptimalFieldMinLength;
  genericMaxLength = ACCT_CONSTANTS.genericFieldMaxLength;
  alphabetsOnlyValidation = ACCT_CONSTANTS.alphabets_only;
  lastNameFieldName =  ACCT_CONSTANTS.FIELD_NAMES.lastName;
  lastNameInfoText =  ACCT_CONSTANTS.INFO_TXT.lastName;
    
  constructor(private controlContainer : ControlContainer) { }

  ngOnInit(): void {
    this.lastNameFormGroup = this.controlContainer.control;
  }

  get lastName() {
    return this.lastNameFormGroup.get("lastName");
  }

}
