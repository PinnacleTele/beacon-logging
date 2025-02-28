import { AbstractControl, ValidationErrors, ValidatorFn } from "@angular/forms";

export function maxLengthValidator(maxLength: number): ValidatorFn {
    // takes the input as the form control and returns a series of validation errrors
    // this below line will take the input as the control and returm either Validation Errors or null
    return (control: AbstractControl): ValidationErrors | null => {
        const fieldValue: string = control.value;
        let maxValid = false;

        if (!fieldValue) {
            return null;
        }

        const spacesRemoved = fieldValue.trim();

        if (spacesRemoved.length > maxLength) {
            maxValid = true;
        }

        return maxValid ? { maxLengthInvalid: true } : null;
    };
}
