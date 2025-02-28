import { Component, OnInit } from '@angular/core';
import { ControlContainer } from '@angular/forms';
import { ACCT_CONSTANTS } from 'src/app/account/account.constants';
import { CONSTANTS } from 'src/app/shared/campaigns.constants';

@Component({
  selector: 'app-newline-char',
  templateUrl: './newline-char.component.html',
  styleUrls: ['./newline-char.component.css']
})
export class NewlineCharComponent implements OnInit {

  newlineCharFormGroup : any;
  
  decimalValidation = CONSTANTS.decimalNum_validation;
  decimalNumTotLength = CONSTANTS.decimalTotalLength;
  noSpaceError = CONSTANTS.ERROR_DISPLAY.noSpaces;
  newLineMaxLimit = CONSTANTS.ERROR_DISPLAY.newLineMaxLimit;
  constructor(
    private parentControl: ControlContainer) {
  }

  ngOnInit(): void {
    this.newlineCharFormGroup = this.parentControl.control ;
   }
   
  get name(){
    return this.newlineCharFormGroup.controls.newlineChar;
  }
}
