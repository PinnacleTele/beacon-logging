import { AbstractControl, ValidationErrors, ValidatorFn } from "@angular/forms";

export function minLengthValidator(minLengthExpected: number): ValidatorFn {
    // takes the input as the form control and returns a series of validation errrors
    // this below line will take the input as the control and returm either Validation Errors or null
    return (control: AbstractControl): ValidationErrors | null => {
        const fieldValue: string = control.value;
        let minValid = false;

        if (!fieldValue) {
            return null;
        }

        // const spacesRemoved = fieldValue.replace(/\s/g, "");
        const spacesRemoved = fieldValue.trim();

        if (spacesRemoved.length < minLengthExpected ) {
            minValid = true;
        }

        return minValid ? { minLengthInvalid: true } : null;
    };
}
