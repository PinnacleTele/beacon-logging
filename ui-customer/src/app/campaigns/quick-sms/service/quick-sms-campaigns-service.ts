import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { BehaviorSubject, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { CONSTANTS } from 'src/app/shared/campaigns.constants';
import { CONSTANTS_URL } from 'src/app/shared/compaign.url';
import { CampaignNames } from '../model/campaigns.models';
import { MobileCountStatistics } from '../../model/generic-campaign-mobile-statistics';

@Injectable()
export class QuickSMSCampaignService {
	BASE_URL = CONSTANTS_URL.GLOBAL_URL;

	CHK_UNIQUE_CNAME_API = this.BASE_URL + CONSTANTS_URL.CHK_UNIQUE_CNAME;

	mobileNumbers: string[] = [];

	validMobileNumbers: string[] = [];

	inValidMobileNumbers: string[] = [];

	uniqueMobileNumbers: string[] = [];

	accountType = '';

	domesticMinLength = CONSTANTS.DOMESTIC_MIN_LENGTH;

	domesticMaxLength = CONSTANTS.DOMESTIC_MAX_LENGTH;

	intlMinLength = CONSTANTS.INTL_MIN_LENGTH;

	intlMaxLength = CONSTANTS.INTL_MAX_LENGTH;

	CONST_DOMESTIC = CONSTANTS.DOMESTIC;

	CONST_INTL = CONSTANTS.INTERNATIONAL;

	CONST_INDIA_CODE = CONSTANTS.DOMESTIC_CODE;

	CONST_START_INDEX = CONSTANTS.DOMESTIC_START_INDEX;

	minMobileNumLength: number;

	maxMobileNumLength: number;

	public loadingcampaignNames$ = new BehaviorSubject<boolean>(false);

	constructor(private http: HttpClient) {}

	checkUniqueCampaignNames(paramValue: string) {
		this.loadingcampaignNames$.next(true);
		//const headers = new HttpHeaders().append('header', 'value');
		let params = new HttpParams().set('cname', paramValue);
		return this.http
			.get(this.CHK_UNIQUE_CNAME_API, { params: params })
			.pipe(
				map((responseData: any) => {
					this.loadingcampaignNames$.next(false);

					return responseData as any;
				}),
				catchError((err) => {
					this.loadingcampaignNames$.next(false);
					//                console.log(`server error${err}`);
					return throwError(err);
				})
			);
	}

	validMobNumbers: any[] = [];

	setValidMobileNumbers(value: any) {
		this.validMobNumbers = value;
	}

	splitIntoMobileNumbers(textAreaContent: string, accountType: string) {
		// split the textArea into mobilenumbers
		const textAreaValue = textAreaContent
			.replace(/(\r\n|\n|\r)/gm, ',')
			.split(/,+/);

		//this.mobileNumbers = textAreaValue.forEach((e) => e.trim());
		this.mobileNumbers = textAreaValue.map((element) => {
			return element.trim();
		});

		// this.getValidMobileNumbers(this.CONST_DOMESTIC);

		this.getValidMobileNumbers(accountType);
	}

	getAllMobileNumbers() {
		return this.mobileNumbers.slice();
	}

	getValidMobileNumbers(accountType: string) {
		this.accountType = accountType;
		this.validMobileNumbers = [];
		this.inValidMobileNumbers = [];
		this.uniqueMobileNumbers = [];
		const data = this.mobileNumbers;

		/*    if (accountType === this.CONST_DOMESTIC) {
                this.minMobileNumLength = this.domesticMinLength;
                this.maxMobileNumLength = this.domesticMaxLength;
            } else if (accountType === this.CONST_INTL) {
                this.minMobileNumLength = this.intlMinLength;
                this.maxMobileNumLength = this.intlMaxLength;
            }*/
		this.minMobileNumLength = this.intlMinLength;
		this.maxMobileNumLength = this.intlMaxLength;
		for (let i = 0; i < data.length; i++) {
			let modifiedStr = data[i];
			if (data[i][0] === '+') {
				modifiedStr = modifiedStr.substring(1);
			}
			if (
				!this.checkValidNumber(modifiedStr) &&
				this.checkValidLength(
					modifiedStr,
					this.minMobileNumLength,
					this.maxMobileNumLength
				)
			) {
				const stringWithNozeroes = modifiedStr.replace(/^0+/, '');
				let startingChar = '';
				this.validMobileNumbers.push(data[i]);
				/*    if (
                        stringWithNozeroes.startsWith(this.CONST_INDIA_CODE) &&
                        stringWithNozeroes.length === 12
                    ) {
                        startingChar = stringWithNozeroes.substring(2, 3);
                        this.validMobileNumbers.push(data[i]); 
                    } else if (stringWithNozeroes.length === 10) {
                        startingChar = stringWithNozeroes.substring(0, 1);
                        this.validMobileNumbers.push(data[i]); 
                    }else{
                        this.inValidMobileNumbers.push(data[i]);
                    }
                    This check is not needed and so commented*/
				/* if (this.CONST_START_INDEX.includes(startingChar)) {
                      // this.validMobileNumbers.push( (parseInt(stringWithNozeroes)) );//remove the leading zeroes and store as a number
                      this.validMobileNumbers.push(data[i]); // remove the leading zeroes and store as a number
                  } else {
                      this.inValidMobileNumbers.push(data[i]);
                  }*/
			} else {
				this.inValidMobileNumbers.push(data[i]);
			}
		}
	}

	checkValidNumber(input: string) {
		return Number.isNaN(Number(input));
	}

	checkValidLength(input: string, min: number, max: number) {
		const number = input.replace(/^0+/, ''); // remove leading zeroes
		const numToString = number.toString();

		if (numToString.length <= max && numToString.length >= min) {
			return true;
		}
		return false;
	}

	getUniqueMobileNumbers() {
		return this.uniqueMobileNumbers.slice();
	}

	getMobileNumbersCount() {
		return this.mobileNumbers.length;
	}

	getValidMobileNumbersCount() {
		return this.validMobileNumbers.length;
	}

	getUniqueMobileNumbersCount() {
		this.uniqueMobileNumbers = [];
		if (this.accountType === this.CONST_DOMESTIC) {
            
			for (let i = 0; i < this.validMobileNumbers.length; i++) {
				if (
					!this.uniqueMobileNumbers.includes(
						this.validMobileNumbers[i].slice(-10)
					)
				) {
					this.uniqueMobileNumbers.push(
						this.validMobileNumbers[i].slice(-10)
					);
				}
			}
		} else {
			let removePlus = this.validMobileNumbers.map((x) => {
				if (x[0] === '+') {
					return x.substring(1);
				} else {
					return x;
				}
			});

			const removeLeadZeroes = removePlus.map((x) =>
				Number(x).toString()
			);

			this.uniqueMobileNumbers = removeLeadZeroes.filter(
				(v, i, a) => a.indexOf(v) === i
			);
		}

		return this.uniqueMobileNumbers.length;
	}

	getInvalidMobileNumbersCount() {
		return this.inValidMobileNumbers.length;
	}

	getMobileCountStatistics() {
		const mobileCountStatistics = new MobileCountStatistics(
			this.validMobileNumbers,
			this.getMobileNumbersCount(),
			this.getValidMobileNumbersCount(),
			this.getUniqueMobileNumbersCount(),
			this.getInvalidMobileNumbersCount(),
			this.getUniqueMobileNumbers(),
			this.inValidMobileNumbers
		);

		return mobileCountStatistics;
	}
}
