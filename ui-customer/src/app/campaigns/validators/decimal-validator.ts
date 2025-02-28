import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import { CONSTANTS } from 'src/app/shared/campaigns.constants';

export function decimalValidator(
	name: string,
	maxLengthExpected: number
): ValidatorFn {
	// takes the input as the form control and returns a series of validation errrors
	// this below line will take the input as the control and returm either Validation Errors or null
	let decimalPlaces = CONSTANTS.decimalPlaces;
	return (control: AbstractControl): ValidationErrors | null => {
		const fieldValue: string = control.value;
		//  console.log(fieldValue)
		//console.log(fieldValue, Number(fieldValue),name,'val');

		if (control.value && fieldValue != '' && fieldValue != undefined && fieldValue != null) {
			//console.log("validation considered")
			let decimValid = false;
			let lessThanZero = false;
			let negativeNum = false;
			//console.log(Number(fieldValue),'val');

			if (name === 'GTzero' && Number(fieldValue) == 0) {

				if (Number(fieldValue) <= 0) {
					//  console.log("negative",Number(fieldValue));
					lessThanZero = true;

					return lessThanZero ? { lessThanZero: true } : null;
				}
			}

			if (Number.isNaN(Number(fieldValue))) {


				decimValid = true;
				//  console.log("s a numb");
				return decimValid ? { decimalInvalid: true } : null;
			} /*else if( Number(fieldValue).toString().length > maxLengthExpected || Number(fieldValue) > 9999999999){
            
                console.log("max");
                decimValid = true;
                
                return decimValid ? { decimalInvalid: true } : null;
            }*/ else {
				if (Number(fieldValue) < 0) {
					// console.log("negative");
					negativeNum = true;

					return negativeNum ? { negativeNum: true } : null;
				}
			}


			if (fieldValue != null) {
				let splitUp = Number(fieldValue).toString().split('.');

				if (splitUp.length > 1) {
					//if a decimal number
					console.log(Number(fieldValue), Number(splitUp[0]));
					if (splitUp[0].length > maxLengthExpected || splitUp[1].length > decimalPlaces) {
						//console.log( splitUp[0],splitUp[1])
						decimValid = true;
						return decimValid ? { decimalInvalid: true } : null;
					}
				} else {
					// console.log("inside else",Number(fieldValue).toString().length)
					if (Number(fieldValue).toString().indexOf("e") != -1 || Number(fieldValue).toString().length > maxLengthExpected) {
						//console.log("caught here last")
						decimValid = true;
						return decimValid ? { decimalInvalid: true } : null;
					}
				}
			}
			//console.log("outside if else")
			//  return decimValid ? { decimalInvalid: true } : null;
		}
	};
}
