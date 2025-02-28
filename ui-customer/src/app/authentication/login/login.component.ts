import {
	Component,
	EventEmitter,
	OnDestroy,
	OnInit,
	Output
} from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { CONSTANTS } from 'src/app/shared/campaigns.constants';
//import { CampaignsService } from 'src/app/campaigns/campaigns.service';
import { AuthLoginService } from '../auth-login.service';
import { LocalStorageService } from '../local-storage.service';

@Component({
	selector: 'app-login',
	templateUrl: './login.component.html',
	styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit, OnDestroy {
	@Output() emitLoggedIn = new EventEmitter<null>();
	subscription: Subscription;
	refreshSubscription: Subscription;
	invalidCredentials = false;
	isSessionTimedOut = false;
	serverErr = false;
	spinner = false;
	showErr = false;
	errorMsg = '';
	//public invalidSessErr$ = this.authInterceptorSvc.invalidSess$.asObservable();

	constructor(
		private fb: FormBuilder,
		private authLoginSvc: AuthLoginService,
		private route: ActivatedRoute,
		private router: Router,
		private localStorage: LocalStorageService,
		//	private campaignService: CampaignsService
	) { }

	loginForm = this.fb.group({
		password: [''],
		email: ['']
	});

	ngOnInit() {
		const user = this.localStorage.getLocal('user');

		if (this.authLoginSvc.invalidSess) {
			this.isSessionTimedOut = this.authLoginSvc.invalidSess;
		} else {
			if (user != null && user != '') {
				this.router.navigate(['/campaigns']);
			}
		}
	}

	get email() {
		return this.loginForm.controls.email;
	}

	get password() {
		return this.loginForm.controls.password;
	}

	get localStorageInvalid(){
		return this.localStorage.invalidSession
	}
	onClickLogin() {
		this.setValidators();
		this.invalidCredentials = false;
		this.isSessionTimedOut = false;
		this.serverErr = false;
		this.showErr = false;
		if (this.loginForm.valid) {
			this.spinner = true;


			this.refreshSubscription = this.authLoginSvc.getRefreshInterval().subscribe();
			this.subscription = this.authLoginSvc
				.login(this.email.value, this.password.value)
				.subscribe(
					(res: any) => {
						if (res.billing_currency) {
							CONSTANTS.currency = res.billing_currency;
						}

						this.spinner = false;
						var data = res;

						if (res.statusCode < 0) {
							this.showErr = true;
							this.errorMsg = res.message;
						} else {
							localStorage.setItem('user', JSON.stringify(res));

							localStorage.setItem('token', res.token);
							this.localStorage.setLocal(JSON.stringify(res));

							this.authLoginSvc.setTimer = true;
							//this.authLoginSvc.setRefreshToken().subscribe();

							setTimeout(() => {

								this.authLoginSvc.startRefreshTokenTimer();
							}, 10000)


							this.router.navigate(['/dashboard']);
						}
					},
					(err) => {
						this.spinner = false;

						this.serverErr = true;

						this.showErr = true;
						this.errorMsg = 'Something went wrong';
					}
				);
		} else {
			this.authLoginSvc.validateAllFormFields(this.loginForm);
		}
	}

	setValidators() {
		//validations added on click of login to fix CU 243 where user put the cursor in username field of login page
		//and click forgot password link, the link dint work
		this.loginForm.controls.email.setValidators(Validators.required);
		this.loginForm.controls.email.updateValueAndValidity();
		this.loginForm.controls.password.setValidators(Validators.required);
		this.loginForm.controls.password.updateValueAndValidity();
	}

	ngOnDestroy() {
		if (this.subscription) {
			this.subscription.unsubscribe();
		}
		if (this.refreshSubscription)
			this.refreshSubscription.unsubscribe();
	}

	get userName() {
		return this.loginForm.controls.email.value;
	}
}
