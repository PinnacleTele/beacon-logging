import { Component, Input, OnInit } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { ACCT_CONSTANTS } from 'src/app/account/account.constants';
import { CONSTANTS } from 'src/app/shared/campaigns.constants';

@Component({
  selector: 'app-error-display',
  templateUrl: './error-display.component.html',
  styleUrls: ['./error-display.component.css']
})
export class ErrorDisplayComponent implements OnInit {
  @Input() formGroup: FormGroup;
  @Input() fieldName: string;
  @Input() controlName: string;
  @Input() fieldInfoText: string;
  @Input() minimumLength: number;
  @Input() maximumLength: number;
  @Input() pattern: string;

  alphabletsOnly = ACCT_CONSTANTS.alphabets_only;
  allowedSplChars = CONSTANTS.pattern_validation;
  decimalErrors = CONSTANTS.ERROR_DISPLAY.decimalPatternMsg;
  mobileValidation = CONSTANTS.mobile_pattern_validation;
  userNameValidation = ACCT_CONSTANTS.userNamePattern;
  decimalValidation = CONSTANTS.decimalNum_validation;
  greaterThanZero = CONSTANTS.ERROR_DISPLAY.gtZero;
  emailValidation = CONSTANTS.emailPattern;
  patternError = CONSTANTS.ERROR_DISPLAY.fieldSplChars;
  allowedPattern = CONSTANTS.allowed_special_characters;
  minLengthError = CONSTANTS.ERROR_DISPLAY.fieldMinLength;
  maxLengthError = CONSTANTS.ERROR_DISPLAY.fieldMaxLength;
  minimumBillRateError = CONSTANTS.ERROR_DISPLAY.minimumBillLength;
  passwordErrMsg = ACCT_CONSTANTS.ERROR_DISPLAY.mustHaveOneSplChar;

  constructor() { }

  ngOnInit(): void {
    
    if (this.controlName == 'walletAmount' || this.controlName == 'patchAmount') {
      this.decimalErrors = CONSTANTS.ERROR_DISPLAY.walletDecimalPatternMsg;
    }

    if (this.pattern === this.alphabletsOnly) {
      this.patternError = CONSTANTS.ERROR_DISPLAY.onlyAphabets;
      this.allowedPattern = "";
    } else if (this.pattern === this.allowedSplChars) {
      this.patternError = CONSTANTS.ERROR_DISPLAY.fieldSplChars;
      this.allowedPattern = CONSTANTS.allowed_special_characters;
    } else if (this.pattern === this.mobileValidation) {
      this.patternError = CONSTANTS.ERROR_DISPLAY.mobilePattern;
      this.allowedPattern = "";
    } else if (this.pattern === this.userNameValidation) {
      this.allowedPattern = ACCT_CONSTANTS.ERROR_DISPLAY.userNameAllowedChars;
      this.patternError = this.fieldName;
    } else if (this.pattern === this.decimalValidation) {
      this.allowedPattern = ACCT_CONSTANTS.ERROR_DISPLAY.decimalPatternMsg;
      this.patternError = "";
    } else if (this.pattern === this.emailValidation) {
      this.allowedPattern = ACCT_CONSTANTS.ERROR_DISPLAY.emailPatternMsg;
      this.patternError = "";
    }
  }

  get name() {
    const control = this.formGroup.get(this.controlName);
    return control as FormControl;
  }

}
