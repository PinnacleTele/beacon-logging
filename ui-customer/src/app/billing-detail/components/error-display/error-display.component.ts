import { Component, Input, OnInit } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { BILL_CONSTANTS } from '../../billing-constants';

@Component({
  selector: 'app-error-display',
  templateUrl: './error-display.component.html',
  styleUrls: ['./error-display.component.css']
})
export class ErrorDisplayComponent implements OnInit {

  constructor() { }
  @Input() formGroup: FormGroup;
  @Input() fieldName: string;
  @Input() controlName: string;
  @Input() fieldInfoText: string;
  @Input() minimumLength: number;
  @Input() maximumLength: number;
  @Input() pattern: string;


  decimalErrors = BILL_CONSTANTS.ERROR_DISPLAY.indiaDecimalPatternMsg;
  minLengthError = BILL_CONSTANTS.ERROR_DISPLAY.minimumBillLength;
  maxLengthError = BILL_CONSTANTS.ERROR_DISPLAY.maxBillLength;
  greaterThanZero = BILL_CONSTANTS.ERROR_DISPLAY.gtZero;

  ngOnInit(): void {
  }


  get name() {
    const control = this.formGroup.get(this.controlName);
    return control as FormControl;
  }
}
