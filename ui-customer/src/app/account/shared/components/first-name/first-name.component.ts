import { Component, OnInit } from '@angular/core';
import { ControlContainer } from '@angular/forms';
import { ACCT_CONSTANTS } from 'src/app/account/account.constants';
import { CONSTANTS } from 'src/app/shared/campaigns.constants';

@Component({
  selector: 'app-first-name',
  templateUrl: './first-name.component.html',
  styleUrls: ['./first-name.component.css']
})
export class FirstNameComponent implements OnInit {
  firstNameFormGroup : any;
  genericMinimalLength = CONSTANTS.genericMinFieldMinLength;
  genericOptimalMinLength = CONSTANTS.genericOptimalFieldMinLength;
  genericMaxLength = ACCT_CONSTANTS.genericFieldMaxLength;
  alphabetsOnlyValidation =  ACCT_CONSTANTS.alphabets_only;
  firstNameFieldName = ACCT_CONSTANTS.FIELD_NAMES.firstName;
  firstNameInfoText =  ACCT_CONSTANTS.INFO_TXT.firstName;
  

  constructor(private controlContainer : ControlContainer) { }

  ngOnInit(): void {
    
    this.firstNameFormGroup = this.controlContainer.control;
    
   
  }
  ngAfterViewInit() {
    document.getElementById("firstName").focus();
}

  get firstName() {
    return this.firstNameFormGroup.get("firstName");
  }

}