import {
	Component,
	EventEmitter,
	Input,
	OnDestroy,
	OnInit,
	Output
} from '@angular/core';
import {
	ControlContainer,
	FormArray,
	FormBuilder,
	FormControl,
	FormGroup,
	Validators
} from '@angular/forms';
import { CONSTANTS, value } from 'src/app/shared/campaigns.constants';

import { ACCT_CONSTANTS } from '../../account.constants';
import { AccountService } from '../../account.service';
import { decimalValidator } from 'src/app/campaigns/validators/decimal-validator';
import { LocalStorageService } from 'src/app/authentication/local-storage.service';
import { SetValidationsService } from '../../set-validations.service';
import { HttpErrorResponse } from '@angular/common/http';
import { Subscription } from 'rxjs';
import { Country } from '../../shared/model/country-model';
import { getCurrencySymbol } from '@angular/common';
import { callbackify } from 'util';
import { SubServices } from '../../shared/model/service-model';

@Component({
	selector: 'app-settings',
	templateUrl: './settings.component.html',
	styleUrls: ['./settings.component.css']
})
export class SettingsComponent implements OnInit, OnDestroy {
	decimalValidation = CONSTANTS.decimalNum_validation;
	decimalNumTotLength = ACCT_CONSTANTS.wallet_rate;
	countryInfoText = ACCT_CONSTANTS.INFO_TXT.country;
	response: { message: string; statusCode: number };
	status: string;
	popup = false;
	spinner = false;
	selectedTzOffSet = '';
	zoneDate: any;
	minDate: any;
	zoneTime: any;
	tzList: any = [];
	rowRates: any;
	currencyType = CONSTANTS.currency;
	currencyFormat = CONSTANTS.curencyFormat;
	myOptions = value;
	walletAmountFieldName = ACCT_CONSTANTS.FIELD_NAMES.walletAmount;
	walletAmountInfoText = ACCT_CONSTANTS.INFO_TXT.walletAmount;
	wholeNumTotLength = ACCT_CONSTANTS.intlRatewholeNumLength; //before decimal point
	rowRateWholeNumLength = ACCT_CONSTANTS.row_rate;
	showTZ = false;
	encryptMobile = false;
	encryptMsg = false;
	showWallet = false;
	cliId: string;
	walletBal: number;
	currencySymbol = '';
	itemList: any;
	countryList: Country[] = [];
	rowConvRate = '';
	@Output() pageError = new EventEmitter<boolean>();
	subscription: Subscription;
	page4FormGroup: any;
	intlEnabled: any;
	selectedCountries = [];
	countries: Country[] = [];
	apiIntlRatesvalue = [];
	decimalErrText = ACCT_CONSTANTS.ERROR_DISPLAY.intlDecimalPatternMsg;
	decimalErrROWText = ACCT_CONSTANTS.ERROR_DISPLAY.rowRatedecimalPatternMsg
	convRatesArr = [];

	@Input() DisableButton :any = false ;

	@Output() form4Error = new EventEmitter<boolean>();

	

	disableSubscription :any;

	createAcctForm = this.fb.group({});
	addIntlCountryRates(): FormGroup {
		return this.fb.group({
			country: [null, Validators.required],
			smsrate: [
				'',
				[
					Validators.required,
					decimalValidator('GTzero', this.wholeNumTotLength)
				]
			]
		});
	}
	constructor(
		private fb: FormBuilder,

		private accountService: AccountService,
		private parentControl: ControlContainer,
		private localStorageService: LocalStorageService,
		private setValidatorService: SetValidationsService
	) {
	
	}

	ngOnInit(): void {
		
		this.page4FormGroup = this.parentControl.control;
		const user = this.localStorageService.getLocal('user');
		const userDetails = JSON.parse(user);
		this.currencyType = userDetails.billing_currency;
		this.currencySymbol = getCurrencySymbol(this.currencyType, 'narrow');
		//this.addIntlCountryRates();
		//this.intlEnabled = userDetails.intl_enabled_yn;
		const selectedServices = this.page4FormGroup.controls.subServices.value as Array<SubServices>;
		selectedServices.forEach((ele:any) => {
			if(ele.sub_service === 'international' ){
				this.intlEnabled = true;
			}
		});
	//	this.setValidatorService.populateBillRates(	this.page4FormGroup);
		if (this.intlEnabled == 1) {
			this.setValidatorService.setValidationRowRate(this.page4FormGroup);

			this.setUpInternationSettings();
		}

		this.setValidatorService.setValidatorsToMsgSettings(
			this.page4FormGroup
		);
		this.setValidatorService.setValidatorsToWalletRates(
			this.page4FormGroup
		);
		this.accountService.markAsUntouched(this.page4FormGroup);
		this.getBillingType();

		this.setEncryption();
		this.disableSubscription = this.setValidatorService.disableButtonToBillRate.subscribe((data:any)=>{
			console.log(data);
			
			this.DisableButton = data;
			//this.DisableButton = true;
			//console.log(this.DisableButton,'inside subscription');
			
			this.form4Error.emit(this.DisableButton);
		});
	
	}

	
	setEncryption() {
		this.encryptMobile = this.page4FormGroup.controls.encrytMob.value;
		this.encryptMsg = this.page4FormGroup.controls.encryMsg.value;

		this.page4FormGroup.controls.encrytMob.setValue(this.encryptMobile);
		this.page4FormGroup.controls.encryMsg.setValue(this.encryptMsg);
	}
	async setUpInternationSettings() {
		//if ((<FormArray>( this.page4FormGroup.controls.otherCountriesBillRates )).length > 0 ) {
		//if we come from previous page to settings page set all the validations

		if (this.setValidatorService.apiCountryRates != undefined) {
			this.apiIntlRatesvalue = this.setValidatorService.apiCountryRates;
			this.rowRates = this.setValidatorService.apiRowRate;
			this.convRatesArr = this.setValidatorService.svcConvRates;
			this.countryList = this.setValidatorService.countryList;
			this.presetIntlRates(this.apiIntlRatesvalue);
			this.getConvertedRates(this.rowRate.value, 'row', 9999999);
			this.resetCountryList();
			this.addFormArrayvalidators();
		} else {
			this.page4FormGroup.controls.otherCountriesBillRates = this.fb.array(
				[this.addIntlCountryRates()]
			);
			this.getCountryList(() => {
				this.getIntlRates(() => {
					this.populateFormArray();
				});
			});
		}
	}
	addFormArrayvalidators() {
		(<FormArray>(
			this.page4FormGroup.controls.otherCountriesBillRates
		)).controls.forEach((x: FormControl) => {
			x.get('country').setValidators([Validators.required]);
			x.get('country').updateValueAndValidity();
			const result = this.apiIntlRatesvalue.find(
				({ country }) => country === x.get('country').value
			);
			x.get('smsrate').setValidators([
				Validators.required,
				decimalValidator('GTzero', this.wholeNumTotLength),
				Validators.min(result?.smsrate ? result.smsrate : 0)
			]);
			x.get('smsrate').updateValueAndValidity();
		});

		this.rowRate.setValidators([
			Validators.required,
			decimalValidator('GTzero', this.rowRateWholeNumLength),
			Validators.min(this.rowRates ? this.rowRates : 0)
		]);
	}
	presetIntlRates(countryRatesSet: any): FormArray {
		const formArray = new FormArray([]);
		countryRatesSet.forEach((ele) => {
			formArray.push(
				this.fb.group({
					country: ele.country,
					smsrate: ele.smsrate
				})
			);
			this.getConvertedRates(ele.smsrate, 'initial', 999999);
		});

		return formArray;
	}
	addButtonClicked() {
		this.resetCountryList();

		(<FormArray>this.page4FormGroup.get('otherCountriesBillRates')).push(
			this.addIntlCountryRates()
		);
	}
	resetCountryList() {
		this.page4FormGroup.value.otherCountriesBillRates = this.page4FormGroup.controls.otherCountriesBillRates.value;

		this.selectedCountries = [];
		this.itemList = this.countryList;
		let aFormArray = this.page4FormGroup.get(
			'otherCountriesBillRates'
		) as FormArray;
		for (let c of aFormArray['controls']) {
			this.selectedCountries.push(c['controls']['country'].value);
		}
		if (this.selectedCountries.length > 0) {
			this.itemList = [];
			this.itemList = this.countryList.filter(
				(i) => !this.selectedCountries.find((f) => f === i.country)
			);
		}
	}

	delete(index) {
		(<FormArray>(
			this.page4FormGroup.get('otherCountriesBillRates')
		)).removeAt(index);

		this.convRatesArr.splice(index, 1);
		this.setValidatorService.apiCountryRates = this.page4FormGroup.get(
			'otherCountriesBillRates'
		).value;
		this.resetCountryList();
	}
	convspinner = false;
	convPopup = false;

	getConvertedRates(value: string, event: string, index: number) {
		this.convspinner = true;
		this.convPopup = false;


		//this.page4FormGroup.controls.otherCountriesBillRates.setErrors({chkServerError: true });
		this.accountService.getConvRates(value).subscribe(
			(res: any) => {
				//this.page4FormGroup.controls.otherCountriesBillRates.setErrors(null);
				this.convspinner = false;
				this.convPopup = false;
				if (event == 'initial') {
					this.convRatesArr.push(res.smsrate);
					this.setValidatorService.svcConvRates = this.convRatesArr;
				} else if (event == 'indexed') {
					this.convRatesArr[index] = res.smsrate;
					this.setValidatorService.svcConvRates[index] = res.smsrate;
				} else if (event == 'row') {
					this.rowConvRate = res.smsrate;
				}
			},
			(error: HttpErrorResponse) => {
				//	this.page4FormGroup.controls.otherCountriesBillRates.setErrors({
				//		chkServerError: true
				//	});
				this.convspinner = false;
				this.convPopup = true;
			}
		);


	}
	getCountryList(done) {
		this.spinner = true;
		this.popup = false;
		//let selectedCountries = this.accountService.selectedCountries;
		this.page4FormGroup.controls.otherCountriesBillRates.setErrors({
			chkServerError: true
		});

		this.accountService.getAllCountries().subscribe(
			(res: any) => {
				this.page4FormGroup.controls.otherCountriesBillRates.setErrors(null);
				this.spinner = false;
				this.countryList = res;
				this.itemList = res;
				this.setValidatorService.countryList = this.itemList;
				done();
			},
			(error: HttpErrorResponse) => {
				this.page4FormGroup.controls.otherCountriesBillRates.setErrors({
					chkServerError: true
				});
				this.popup = true;
				this.spinner = false;
			}
		);
	}
	intlRatesApiError: any;
	getIntlRates(callback) {
		this.spinner = true;
		this.intlRatesApiError = false;

		//this.page4FormGroup.controls.otherCountriesBillRates.setErrors({
		//chkServerError: true
		//});
		this.accountService.getIntlRates().subscribe(
			(res: any) => {
				//	this.page4FormGroup.otherCountriesBillRates.controls.country.setErrors(null);
				this.spinner = false;

				this.apiIntlRatesvalue = [];
				res.forEach((ele: any) => {
					if (ele.country.toLowerCase() == 'row') {
						this.rowRate.setValue(ele.smsrate);
						this.rowRates = ele.smsrate;
						this.getConvertedRates(this.rowRates, 'row', 9999999); //setting some higher value for index
						this.setValidatorService.apiRowRate = this.rowRates;
					} else {
						this.apiIntlRatesvalue.push(ele);
					}
				});
				this.setValidatorService.apiCountryRates = this.apiIntlRatesvalue;

				callback();
			},
			(error: HttpErrorResponse) => {
				this.page4FormGroup.controls.otherCountriesBillRates.setErrors({
					chkServerError: true
				});
				this.intlRatesApiError = true;
				this.spinner = false;
			}
		);
	}
	checkRowRate() {
		const ctrlvalue = this.rowRate.value as string;
		let value = ctrlvalue.trim();
		if (value.length > 0 && Number(value) >= 0) {
			this.getConvertedRates(this.rowRate.value, 'row', 9999999);
		} else {
			this.rowConvRate = "0";
		}

	}
	populateFormArray() {
		this.page4FormGroup.controls.otherCountriesBillRates = this.presetIntlRates(
			this.apiIntlRatesvalue
		);
		this.addFormArrayvalidators();


	}
	getBillingType() {
		const user = this.localStorageService.getLocal('user');
		let userObj = null;

		if (user) {
			userObj = JSON.parse(user);
		}
		if (userObj.bill_type === 1) {
			this.showWallet = true;
			this.setWalletSectionValidators();
			this.cliId = userObj.cli_id;

			this.getWalletBalance();
		}
	}
	ngAfterViewInit() {
		if (document.getElementById('pg4WalletAmt')) {
			document.getElementById('pg4WalletAmt').focus();
		}
	}
	aBalSpinner = false;
	walletpopup = true;
	getWalletBalance() {
		this.aBalSpinner = true;
		this.walletpopup = true;
		this.subscription = this.accountService
			.getWalletBal(this.cliId)
			.subscribe(
				(res: any) => {
					this.aBalSpinner = false;
					this.walletBal = res.wallet_bal;
					this.walletpopup = false;
					this.walletAmount.setValidators([Validators.max(this.walletBal), decimalValidator('canBeZero', this.decimalNumTotLength)])
				},
				(error: HttpErrorResponse) => {
					this.aBalSpinner = false;
					this.walletpopup = true;
					let err = this.accountService.badError;
					this.response = err;
					this.status = error.message;
					this.walletpopup = true;
				}
			);
	}

	onClickEncryptMobile(event: any) {
		this.encryptMobile = !this.encryptMobile;
		this.page4FormGroup.controls.encrytMob.setValue(this.encryptMobile);
	}
	onClickEncryptMsg(event: any) {
		this.encryptMsg = !this.encryptMsg;
		this.page4FormGroup.controls.encryMsg.setValue(this.encryptMsg);
	}
	// checkBal(event: any) {
	// 	if (event.target.value > this.walletBal) {
	// 		this.walletAmount.setErrors({
	// 			insufficientBalErr: true
	// 		});
	// 	}
	// }
	onRateChange(index: number, from: string) {
		const formGroup = (this.page4FormGroup.get(
			'otherCountriesBillRates'
		) as FormArray).at(index) as FormGroup;

		let rateForCountry = '';
		let intlRateFieldValue = 0;
		if (formGroup.get('country')) {
			rateForCountry = formGroup.get('country').value;
		}
		const result = this.apiIntlRatesvalue.find(
			({ country }) => country === rateForCountry
		);

		if (result && from == "chgCountry") {
			formGroup.get('smsrate').setValue(result.smsrate);
		}
		if (formGroup.get('smsrate')) {
			intlRateFieldValue = formGroup.get('smsrate').value;
		}

		this.getConvertedRates(intlRateFieldValue.toString(), 'indexed', index);

		//formGroup.get('country').touched && formGroup.get('intlRate').touched


	}

	hasError(event: boolean) {
		this.pageError.emit(event);
	}
	get walletAmount() {
		return this.page4FormGroup.controls.walletAmount;
	}
	get SMSRate() {
		return this.page4FormGroup.controls.SMSRate;
	}
	get DLTRate() {
		return this.page4FormGroup.controls.DLTRate;
	}

	getSelectedCountry(event: any, index: number) {
		this.resetCountryList();
		this.onRateChange(index, "chgCountry");
	}
	/*get country() {
		return this.page4FormGroup.controls.country;
	}*/
	get tz() {
		return this.page4FormGroup.controls.tz;
	}
	get rowRate() {
		return this.page4FormGroup.controls.rowRate;
	}

	get newlineChar() {
		return this.page4FormGroup.controls.newlineChar;
	}
	setWalletSectionValidators() {
		if(this.walletAmount.value){
			
			this.walletAmount.setValidators([
				decimalValidator('canBeZero', this.decimalNumTotLength)
			]);
			this.walletAmount.updateValueAndValidity();
	
		}
		
		//  this.country.setValidators(Validators.required)
		// this.country.updateValueAndValidity();
		this.tz.setValidators(Validators.required);
		this.tz.updateValueAndValidity();
	}
	handleRetryClick() {
		this.setUpInternationSettings();
	}

	// setError(){
	// //if (this.DisableButton ) {
	// 	// this.page4FormGroup.controls.otherCountriesBillRates.setErrors({
	// 	// 	chkServerError: true
	// 	// });
	// 	this.page4FormGroup.controls.SMSRate.setErrors({user_rate:true});
	// 	//this.page4FormGroup.controls.SMSRate.updateValueAndValidity();
	// 	this.page4FormGroup.controls.DLTRate.setErrors({user_rate:true});
	// 	this.page4FormGroup.controls.DLTRate.updateValueAndValidity();
	// 	console.log(this.page4FormGroup.controls,'set error');
		
	// // }	else{
	// // 	this.setValidatorService.setValidatorsToWalletRates(this.page4FormGroup);
	// // }
		
	// }

	retryToGetBillRate(){
		this.setValidatorService.populateBillRates(this.page4FormGroup);
	}
	ngOnDestroy() {
		if (this.subscription) {
			this.subscription.unsubscribe();
		}
	}
}
