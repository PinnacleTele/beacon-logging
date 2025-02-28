import { AbstractControl, ValidationErrors, ValidatorFn } from "@angular/forms";
import { CONSTANTS } from 'src/app/shared/campaigns.constants'

export function emailValidator(): ValidatorFn {
    // takes the input as the form control and returns a series of validation errrors
    // this below line will take the input as the control and returm either Validation Errors or null
    return (control: AbstractControl): ValidationErrors | null => {
        if (control.value) {
            const fieldValue: string = control.value.trim();
            let invalidEmail = false;
            const usernameLenth = CONSTANTS.emailFirstPartLength;
            const middlePart = CONSTANTS.emailMiddlePartLength;
            const lastPartMinLength = CONSTANTS.emailLastPartMinLength;
            const lastPartMaxLength = CONSTANTS.emailLastPartMaxLength;

            if (!fieldValue || fieldValue.length == 0) {
                return null;
            }
            if (fieldValue.indexOf("@") >= usernameLenth) {
                const atIndex = fieldValue.indexOf("@");
                if (fieldValue.substr(atIndex).split("@").length > 2) {
                    // console.log("many @")
                    invalidEmail = true;
                }
                if (fieldValue.substr(atIndex).indexOf(".") > middlePart) {

                } else {
                    // console.log(". not comes afte @")
                    invalidEmail = true;
                    return invalidEmail ? { invalidEmailError: true } : null;

                }
                if (fieldValue.substr(atIndex).split(".").length > 3) {
                    //console.log("many dots")
                    invalidEmail = true;
                    return invalidEmail ? { invalidEmailError: true } : null;
                } else {
                    if (fieldValue.lastIndexOf(".") == fieldValue.length) {
                        //  console.log("last is a dot")

                        invalidEmail = true;
                        return invalidEmail ? { invalidEmailError: true } : null;
                    }
                    let len = fieldValue.split('.')
                    let last = len[len.length - 1];


                    if (last.match(/^[A-Za-z0-9- ]+$/)) {
                        if (last.startsWith('-') || last.endsWith('-')) {
                            invalidEmail = true;
                            return invalidEmail ? { invalidEmailError: true } : null;
                        }
                    }
                    else {
                        invalidEmail = true;
                        return invalidEmail ? { invalidEmailError: true } : null;
                    }


                    if (fieldValue.length - fieldValue.lastIndexOf(".") < lastPartMinLength || (fieldValue.length - fieldValue.lastIndexOf(".") > lastPartMaxLength)) {
                        //   console.log("last name is between 2 and 6 chars long")

                        invalidEmail = true;
                        return invalidEmail ? { invalidEmailError: true } : null;
                    }
                    let arr = fieldValue.substr(atIndex).split(".")
                    arr.forEach(el => {
                        if (el.length <= 1) {
                            // console.log("continuous dots")
                            invalidEmail = true;
                            return invalidEmail ? { invalidEmailError: true } : null;
                        }
                    })
                }


            } else {
                invalidEmail = true;
                return invalidEmail ? { invalidEmailError: true } : null;
            }


            return invalidEmail ? { invalidEmailError: true } : null;
        };
    }
}
