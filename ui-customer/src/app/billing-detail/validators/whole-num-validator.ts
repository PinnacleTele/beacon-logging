
import { AbstractControl, ValidationErrors, ValidatorFn } from "@angular/forms";
import { CONSTANTS } from "src/app/shared/campaigns.constants";


export function wholeNumValidator(maxLengthExpected: number): ValidatorFn {
  // takes the input as the form control and returns a series of validation errrors
  // this below line will take the input as the control and returm either Validation Errors or null
  let decimalPlaces = CONSTANTS.decimalPlaces;
  return (control: AbstractControl): ValidationErrors | null => {

    const fieldValue: string = control.value;
    if (fieldValue != "" && fieldValue != undefined && fieldValue != null) {
      let wholenum = false;
      let actualNum = fieldValue;
      let split = Math.floor(Number(actualNum))

      if (String(split).length > maxLengthExpected) {
        console.log(String(split).length);

        return { wholenumError: true };
      }

    }
  }

}
