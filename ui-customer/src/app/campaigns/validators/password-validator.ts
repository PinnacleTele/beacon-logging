import { AbstractControl, ValidationErrors, ValidatorFn } from "@angular/forms";
import { ACCT_CONSTANTS } from "src/app/account/account.constants";

export function passwordValidator(): ValidatorFn {
    // takes the input as the form control and returns a series of validation errrors
    // this below line will take the input as the control and returm either Validation Errors or null
    return (control: AbstractControl): ValidationErrors | null => {
        const fieldValue: string = control.value;
        
        let hasSplChar = false;

        if (!fieldValue) {
            return null;
        }
        let reg = new RegExp(ACCT_CONSTANTS.passwordValidation);
        hasSplChar = reg.test(fieldValue);
             
        return hasSplChar && fieldValue.length >= ACCT_CONSTANTS.passwordMinLength ? null : { hasNoSplChar : true };
    };
}
