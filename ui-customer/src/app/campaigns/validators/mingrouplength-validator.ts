import { AbstractControl } from "@angular/forms";

export function minGrouplengthValidator(
    control: AbstractControl
): { [s: string]: boolean } | null {
    if (control.value !== null) {
        if (!control.value) {
            return { customvalidator: true };
        }
    }
    return null;
}
