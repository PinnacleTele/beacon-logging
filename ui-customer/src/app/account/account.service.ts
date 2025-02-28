import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { FormArray, FormControl, FormGroup } from '@angular/forms';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { GroupModel } from '../campaigns/model/campaign-group-model';
import { CONSTANTS_URL } from '../shared/compaign.url';
import { ERROR } from '../campaigns/error.data';
import { Country } from './shared/model/country-model';
import { SubServices } from './shared/model/service-model';
import { TemplateGroup } from './shared/model/template-group-model';
import { HeaderComponent } from '../core/header/header.component';
import { DataSharingService } from '../core/data-sharing.service';
import { ACCT_CONSTANTS } from './account.constants';
import { LocalStorageService } from '../authentication/local-storage.service';

@Injectable({
	providedIn: 'root'
})
export class AccountService {
	badError: any;
	BASE_URL = CONSTANTS_URL.GLOBAL_URL;
	UPDATE_WALLET_URL = CONSTANTS_URL.UPDATE_WALLET_URL;
	GET_WALLET_USERS = CONSTANTS_URL.API_WalletUsers;
	UPDATE_MULTI_WALLET_URL = CONSTANTS_URL.UPDATE_MULTI_WALLET_URL;
	API_UNIQUE_USERNAME = CONSTANTS_URL.ACCT_USERNAME;
	API_COUNTRY = CONSTANTS_URL.ALL_COUNTRY;
	API_ALLOC_GROUP = CONSTANTS_URL.ACCT_TEMPLATE_GROUPS;
	API_SHARE_GROUP = CONSTANTS_URL.ACCT_SHARE_GROUPS;
	API_SUB_SERVICES = CONSTANTS_URL.ACCT_SUB_SERVICES;
	createAcct_Url = CONSTANTS_URL.ACCT_CREATE;
	API_WalletBal = CONSTANTS_URL.API_WalletBal;
	API_ACCT_STATUS_UPDATE = CONSTANTS_URL.API_ACCT_STATUS_UPDATE;
    API_INTL_RATE = CONSTANTS_URL.API_INTL_RATE;
	countryArray: Country[] = [];
	selectedCountries = [];
	allocateGroupArray: TemplateGroup[] = [];
	sharedGroupArray: GroupModel[] = [];
	subServices: SubServices[] = [];
	httpOptions = {
		headers: new HttpHeaders({
			Accept: 'application/json',
			'Content-Type': 'application/json'
		})
	};
	constructor(
		public http: HttpClient,
		private dataShare: DataSharingService,
		private localStorageService:LocalStorageService
	) {}

	headerControl: HeaderComponent;

	setaddGroupControl(control: HeaderComponent) {
		this.headerControl = control;
	}

	checkUniqueUserNames(paramValue: string) {
		//const headers = new HttpHeaders().append('header', 'value');
		let params = new HttpParams().set('uname', paramValue);
		return this.http
			.get(this.BASE_URL + this.API_UNIQUE_USERNAME, { params: params })
			.pipe(
				map((responseData: any) => {
					//  console.log(responseData);
					//if(responseData.statusCode >299){

					return responseData as any;
				}),
				catchError((err) => {
					//                console.log(`server error${err}`);
					return throwError(err);
				})
			);
	}

	updateMultiUsersWalletBal(
		amount: number,
		action: string,
		cliIds: number[],
		comments: string
	) {
		let walletInfo = {
			amount: amount,
			action: action,
			cli_ids: cliIds,
			comments: comments
		};

		return this.http
			.post(this.BASE_URL + this.UPDATE_MULTI_WALLET_URL, walletInfo)
			.pipe(
				map((responseData) => {
					return responseData;
				}),
				catchError((err) => {
					if (err.status == 0) {
						let setError = ERROR.REQUEST_NOT_SEND;

						this.badError = setError;
					} //else if(err.status == 400){}
					else {
						let setError = ERROR.SOMETHING_WENT_WRONG;
						this.badError = setError;
					}

					return throwError(this.badError);
				})
			);
	}
	getUsersWallet(amount: number, action: string) {
		let walletInfo = {
			amount: amount,
			action: action
		};

		return this.http
			.post(this.BASE_URL + this.GET_WALLET_USERS, walletInfo)
			.pipe(
				map((responseData) => {
					return responseData;
				}),
				catchError((err) => {
					if (err.status == 0) {
						let setError = ERROR.REQUEST_NOT_SEND;

						this.badError = setError;
					} //else if(err.status == 400){}
					else {
						let setError = ERROR.SOMETHING_WENT_WRONG;
						this.badError = setError;
					}

					return throwError(this.badError);
				})
			);
	}
	updateWalletBal(
		editCliId: string,
		amount: number,
		action: string,
		comments: string
	) {
		let walletInfo = {
			cli_id: editCliId,
			amount: amount,
			action: action,
			comments: comments
		};

		return this.http
			.post(this.BASE_URL + this.UPDATE_WALLET_URL, walletInfo)
			.pipe(
				map((responseData) => {
					return responseData;
				}),
				catchError((err) => {
					if (err.status == 0) {
						let setError = ERROR.REQUEST_NOT_SEND;

						this.badError = setError;
					} //else if(err.status == 400){}
					else {
						let setError = ERROR.SOMETHING_WENT_WRONG;
						this.badError = setError;
					}

					return throwError(this.badError);
				})
			);
	}

	getWalletBal(paramValue: string) {
		return this.http
			.post(this.BASE_URL + this.API_WalletBal, { cli_id: paramValue })
			.pipe(
				map((responseData) => {
					return responseData;
				}),
				catchError((err) => {
					if (err.status == 0) {
						let setError = ERROR.REQUEST_NOT_SEND;

						this.badError = setError;
					} //else if(err.status == 400){}
					else {
						let setError = ERROR.SOMETHING_WENT_WRONG;
						this.badError = setError;
					}

					return throwError(this.badError);
				})
			);
	}

	updateAcctStatus(paramValue: string, action: string) {
		return this.http
			.post(this.BASE_URL + this.API_ACCT_STATUS_UPDATE, {
				cli_id: paramValue,
				astatus: action
			})
			.pipe(
				map((responseData) => {
					return responseData;
				}),
				catchError((err) => {
					if (err.status == 0) {
						let setError = ERROR.REQUEST_NOT_SEND;

						this.badError = setError;
					} //else if(err.status == 400){}
					else {
						let setError = ERROR.SOMETHING_WENT_WRONG;
						this.badError = setError;
					}

					return throwError(this.badError);
				})
			);
	}

	getAllCountries() {
		return this.http.get<Country[]>(this.BASE_URL + this.API_COUNTRY).pipe(
			map((responseData) => {
				return responseData;
			}),
			catchError((err) => {
				if (err.status == 0) {
					let setError = ERROR.REQUEST_NOT_SEND;

					this.badError = setError;
				} //else if(err.status == 400){}
				else {
					let setError = ERROR.SOMETHING_WENT_WRONG;
					this.badError = setError;
				}

				return throwError(this.badError);
			})
		);
	}
	getConvRates(rate: string) {
		let params = new HttpParams().set('rate', rate);
		return this.http
			.get(this.BASE_URL + CONSTANTS_URL.CONV_API, { params: params }).pipe(
			map((responseData) => {
				return responseData;
			}),
			catchError((err) => {
				if (err.status == 0) {
					let setError = ERROR.REQUEST_NOT_SEND;

					this.badError = setError;
				} //else if(err.status == 400){}
				else {
					let setError = ERROR.SOMETHING_WENT_WRONG;
					this.badError = setError;
				}

				return throwError(this.badError);
			})
		);
	}

    getIntlRates() {
		return this.http.get<Country[]>(this.BASE_URL + this.API_INTL_RATE).pipe(
			map((responseData) => {
				return responseData;
			}),
			catchError((err) => {
				if (err.status == 0) {
					let setError = ERROR.REQUEST_NOT_SEND;

					this.badError = setError;
				} //else if(err.status == 400){}
				else {
					let setError = ERROR.SOMETHING_WENT_WRONG;
					this.badError = setError;
				}

				return throwError(this.badError);
			})
		);
	}

	getGroupsToAllocate() {
		return this.http
			.get<TemplateGroup[]>(this.BASE_URL + this.API_ALLOC_GROUP)
			.pipe(
				map((responseData) => {
					return responseData;
				}),
				catchError((err) => {
					if (err.status == 0) {
						let setError = ERROR.REQUEST_NOT_SEND;

						this.badError = setError;
					} //else if(err.status == 400){}
					else {
						let setError = ERROR.SOMETHING_WENT_WRONG;
						this.badError = setError;
					}

					return throwError(this.badError);
				})
			);
	}

	getGroupsToShare() {
		return this.http
			.get<GroupModel[]>(this.BASE_URL + this.API_SHARE_GROUP)
			.pipe(
				map((responseData) => {
					return responseData;
				}),
				catchError((err) => {
					if (err.status == 0) {
						let setError = ERROR.REQUEST_NOT_SEND;

						this.badError = setError;
					} //else if(err.status == 400){}
					else {
						let setError = ERROR.SOMETHING_WENT_WRONG;
						this.badError = setError;
					}

					return throwError(this.badError);
				})
			);
	}

	getSubServices() {
		// this.loadingGroupsToAllocate$.next(true)
		return this.http
			.get<SubServices[]>(this.BASE_URL + this.API_SUB_SERVICES)
			.pipe(
				map((responseData) => {
					return responseData;
				}),
				catchError((err) => {
					if (err.status == 0) {
						let setError = ERROR.REQUEST_NOT_SEND;

						this.badError = setError;
					} //else if(err.status == 400){}
					else {
						let setError = ERROR.SOMETHING_WENT_WRONG;
						this.badError = setError;
					}

					return throwError(this.badError);
				})
			);
	}

	validateAllFormFields(accountForm: FormGroup) {
		const formGroup = accountForm;
		Object.keys(formGroup.controls).forEach((field) => {
			const control = formGroup.get(field);

			if (control instanceof FormControl) {
				control.markAsTouched({ onlySelf: true });
			}
			if (control instanceof FormArray) {
				for (const control1 of control.controls) {
					if (control1 instanceof FormControl) {
						control1.markAsTouched({
							onlySelf: true
						});
					}
					if (control1 instanceof FormGroup) {
						this.validateAllFormFields(control1);
					}
				}
			}
		});
	}

	smsBillRate: any;
	dltBillRate: any;

	populateBillRates() {
		let userData = localStorage.getItem('user');
		let jsonData = JSON.parse(userData);
		const user = jsonData;
		this.smsBillRate = user.smsrate;
		this.dltBillRate = user.dltrate;
	}

	clearAllValidators(accountForm: FormGroup) {
		const formGroup = accountForm;
		Object.keys(formGroup.controls).forEach((field) => {
			const control = formGroup.get(field);

			if (control instanceof FormControl) {
				control.clearValidators();
				control.updateValueAndValidity();
			}
			if (control instanceof FormArray) {
				for (const control1 of control.controls) {
					if (control1 instanceof FormControl) {
						control.clearValidators();
						control.updateValueAndValidity();
					}
					if (control1 instanceof FormGroup) {
						this.clearAllValidators(control1);
					}
				}
			}
		});
	}

	markAsUntouched(accountForm: FormGroup) {
		const formGroup = accountForm;
		Object.keys(formGroup.controls).forEach((field) => {
			//console.log(field)
			const control = formGroup.get(field);

			if (control instanceof FormControl) {
				control.markAsUntouched({ onlySelf: true });
			}
			if (control instanceof FormArray) {
				for (const control1 of control.controls) {
					if (control1 instanceof FormControl) {
						control.markAsUntouched({ onlySelf: true });
					}
					if (control1 instanceof FormGroup) {
						this.markAsUntouched(control1);
					}
				}
			}
		});
	}

	sendCreateAccount(value: any): Observable<any> {
		return this.http
			.post(this.BASE_URL + this.createAcct_Url, value, this.httpOptions)
			.pipe(
				map((responseData: any) => {
					return responseData as any;
				}),
				catchError((err) => {
					if (err.status == 0) {
						let setError = ERROR.REQUEST_NOT_SEND;

						this.badError = setError;
					} //else if(err.status == 400){}
					else {
						let setError = ERROR.SOMETHING_WENT_WRONG;
						this.badError = setError;
					}

					return this.badError;
				})
			);
	}
	QUICK_LINK_URL = CONSTANTS_URL.ACC_QUICK_LINKS_URL;
	getQuickLinks() {
		// this.loadingGroupsToAllocate$.next(true)
		return this.http
			.get<SubServices[]>(this.BASE_URL + this.QUICK_LINK_URL)
			.pipe(
				map((responseData) => {
					return responseData;
				}),
				catchError((err) => {
					if (err.status == 0) {
						let setError = ERROR.REQUEST_NOT_SEND;

						this.badError = setError;
					} //else if(err.status == 400){}
					else {
						let setError = ERROR.SOMETHING_WENT_WRONG;
						this.badError = setError;
					}

					return throwError(this.badError);
				})
			);
	}

	UPDATEQL_URL = CONSTANTS_URL.UPDATE_QL_URL;
	quicklinkupdating = new BehaviorSubject<boolean>(false);

	updateQuickLinks(value: any) {
		this.quicklinkupdating.next(true);
		let payload = { selected_quicklinks: value };
		return this.http.post(this.BASE_URL + this.UPDATEQL_URL, payload).pipe(
			map((responseData: any) => {
				this.quicklinkupdating.next(false);
				//  this.headerControl.getQuickLinks()
				this.dataShare.getQuickLinks().subscribe();
				return responseData as any;
			}),
			catchError((err) => {
				this.quicklinkupdating.next(false);
				if (err.status == 0) {
					let setError = ERROR.REQUEST_NOT_SEND;

					this.badError = setError;
				} //else if(err.status == 400){}
				else {
					let setError = ERROR.SOMETHING_WENT_WRONG;
					this.badError = setError;
				}

				return this.badError;
			})
		);
	}
}
