import { Injectable } from '@angular/core';
import { Router } from '@angular/router';

@Injectable({
	providedIn: 'root'
})
export class LocalStorageService {
	constructor(private router: Router) {}
	accountInfo: any;

	setAccount(acct: any) {
		this.accountInfo = acct;
	}
	getAccount() {
		return this.accountInfo;
	}

	setLocal(user: any) {
		console.log('set called');
		localStorage.setItem('user', user);
	}
	getLocal(obj: string) {
		return JSON.stringify(this.tokenDecoder());
	}

	getLocalValue() {
		// let userData = localStorage.getItem('user');
		// let jsonData = JSON.parse(userData);

		return this.tokenDecoder();
	}

	logout() {
		return localStorage.removeItem('user');
	}

	getFromLocalStorage(key: string) {
		return localStorage.getItem(key);
	}
	parseJwt(token) {
		var base64Url = token.split('.')[1];
		var base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
		var jsonPayload = decodeURIComponent(
			atob(base64)
				.split('')
				.map(function (c) {
					return (
						'%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
					);
				})
				.join('')
		);

		return JSON.parse(jsonPayload);
	}

	tokenDecoder() {
		try {
			let accessTokenDetail = localStorage.getItem('token');
			let data = this.parseJwt(accessTokenDetail);
			data.smsrate = JSON.parse(localStorage.getItem('user')).smsrate;
			data.dltrate = JSON.parse(localStorage.getItem('user')).dltrate;
			return data;
		} catch (Exception) {
			this.clearLocalStorage();
		}
	}
	invalidSession = false;
	clearLocalStorage() {
    if (localStorage.getItem('user') && localStorage.getItem('token')) {
      this.invalidSession = true;
    }
    localStorage.removeItem('user');
    localStorage.removeItem('token');
    localStorage.removeItem('refresh_interval');
    this.router.navigate(['login']);
	}
}
