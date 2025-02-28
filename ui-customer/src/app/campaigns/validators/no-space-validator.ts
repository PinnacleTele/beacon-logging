import { AbstractControl, ValidationErrors, ValidatorFn } from "@angular/forms";

export function noSpaceValidator(): ValidatorFn {
    // takes the input as the form control and returns a series of validation errrors
    // this below line will take the input as the control and returm either Validation Errors or null
    return (control: AbstractControl): ValidationErrors | null => {
        const fieldValue: string = control.value;
        let hasSpace = false;
        let hasTilde = false;

        if (!fieldValue || fieldValue.length == 0) {
            return null;
        }else{
             if(fieldValue.indexOf("~") != -1){
                hasTilde = true;
                return hasTilde ? { hasTildeError : true } : null;
             }
        }
        const len = (fieldValue.match(/\s/g) || '').length;
        if (len > 0 && fieldValue.length >0) {
            
            hasSpace = true;
            return hasSpace ? { hasSpaceError : true } : null;
        }

        
    };
}
