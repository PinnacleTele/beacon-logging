import {
	HttpClient,
	HttpRequest
} from '@angular/common/http';
import { Injectable } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { ERROR } from '../campaigns/error.data';
import { CONSTANTS } from '../shared/campaigns.constants';
import { CONSTANTS_URL } from '../shared/compaign.url';
import { LocalStorageService } from './local-storage.service';

@Injectable({
	providedIn: 'root'
})
export class AuthLoginService {
	badError: any;
	BASE_URL = CONSTANTS_URL.GLOBAL_URL;
	LOGIN_API = CONSTANTS_URL.LOGIN_API;
	REFRESH_TOKEN_INTERVAL_API = CONSTANTS_URL.REFRESH_INTERVAL;
	AUTH_LOGIN_API = this.BASE_URL + this.LOGIN_API;
	REFRESH_TOKEN_API = this.BASE_URL + CONSTANTS_URL.REFRESH_API;
	LOGOUT_API = this.BASE_URL + CONSTANTS_URL.LOGOUT_API;
	AUTH_FORGOT_PWD_API = this.BASE_URL + CONSTANTS_URL.AUTH_FORGOT_PWD_API;
	setTimer = true;
	invalidSess = false;

	refreshToken = '';
	internalServerErr: any;
	tokenSubscription : any;
	

	constructor(
		private http: HttpClient,
		private router: Router,
		private localService:LocalStorageService
	) {}

	validateAllFormFields(loginForm: FormGroup) {
		const formGroup = loginForm;

		Object.keys(formGroup.controls).forEach((field) => {
			const control = formGroup.get(field);

			if (control instanceof FormControl) {
				control.markAsTouched({ onlySelf: true });
			}
		});
	}

	forgotPwd(email: string, user: string) {
		return this.http
			.post(this.AUTH_FORGOT_PWD_API, { username: user, email: email })
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
	login(username: string, password: string) {
		return this.http
			.post(this.AUTH_LOGIN_API, { uname: username.trim(), pass: password.trim() })
			.pipe(
				map((responseData: any) => {
					//  this.startRefreshTokenTimer();
					// this.commonService.accessTokenDetail=this.parseJwt(responseData.token)
					return responseData as any;
				}),
				catchError((err) => {
					return throwError(err);
				})
			);
	}

	logout(err: any) {
		//this.loggedIn.next(false);
		this.setTimer = false;
		this.invalidSess = false;

		return this.http.post(this.LOGOUT_API, {}).pipe(
			map((responseData: any) => {
				
				// localStorage.clear();
				// this.localService.clearLocalStorage();
				localStorage.removeItem('user');
				localStorage.removeItem('token');
				localStorage.removeItem('refresh_interval');
				this.router.navigate(['login']);
				this.localService.invalidSession=false;
				if (this.tokenSubscription) {
					clearInterval(this.tokenSubscription);
				}
				return responseData as any;
			}),
			catchError((err) => {
				this.internalServerErr = ERROR.SOMETHING_WENT_WRONG;

				return err;
			})
		);
	}
	cachedRequests: Array<HttpRequest<any>> = [];

	public collectFailedRequest(request): void {
		this.cachedRequests.push(request);
	}

	public retryFailedRequests(): void {
		// retry the requests. this method can
		// be called after the token is refreshed
	}

	getRefreshInterval() {
		//console.log("get interval")
		return this.http
			.get<any>(this.BASE_URL + this.REFRESH_TOKEN_INTERVAL_API)
			.pipe(
				map((res: any) => {
					localStorage.setItem(
						'refresh_interval',
						res.interval_insec
					);
				}),
				catchError((err: any) => {
					return err;
				})
			);
	}
	setRefreshToken() {
		//const user = this.localStorage.getLocal("user");
		const user = this.localService.getFromLocalStorage('user');
		if (user != null) {
			let json = JSON.parse(user);

			this.refreshToken = json['refresh_token'];
		}
		//console.log("before hitting the api")
		return this.http
			.post<any>(this.REFRESH_TOKEN_API, {
				refresh_token: this.refreshToken
			})
			.pipe(
				map((res: any) => {
					localStorage.setItem('token', res.access_token);
					// const isActive=this.localService.parseJwt(res.access_token)?.acc_status					
					// if(isActive&&isActive!=0){
					// 	this.setTimer = false;
					// 	//localStorage.clear();
					// 	this.localService.clearLocalStorage();
					// }
					return res;
				}),
				catchError((err: any) => {
					
				if(err.status === 401){
					// console.log("5");
					this.setTimer =false;
					this.invalidSess = true;
					this.localService.clearLocalStorage();
					// localStorage.clear();
					this.destroySubscription();
					return null
				}
					return  err;
				})
			);
	}

	startRefreshTokenTimer() {
		if (this.setTimer) {
			const intervalStr = this.localService.getFromLocalStorage('refresh_interval');
			let interval = +intervalStr;
			if (interval == 0) {
				interval = +CONSTANTS.DEFAULT_REFRESH_INTERVAL;
			}
			this.tokenSubscription = setInterval(() => {
				this.setRefreshToken().subscribe();
			}, (interval > 0 ? interval : CONSTANTS.DEFAULT_REFRESH_INTERVAL) * 1000);
		}
	}
	
logoutInvalidSess(){
	
	// this.invalidSess = true;
	this.setTimer = false;
	// localStorage.clear();
	this.localService.clearLocalStorage();
  }
destroySubscription(){
		if(this.tokenSubscription){
			//this.tokenSubscription.unsubscribe();
			clearInterval(this.tokenSubscription);
		}
	}	
}
