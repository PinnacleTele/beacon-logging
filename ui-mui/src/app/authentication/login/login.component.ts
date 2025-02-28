import {
	Component,
	EventEmitter,
	OnDestroy,
	OnInit,
	Output
} from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import {  Router } from '@angular/router';


@Component({
	selector: 'app-login',
	templateUrl: './login.component.html',
	styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit, OnDestroy {
	@Output() emitLoggedIn = new EventEmitter<null>();
	invalidCredentials = false;
	isSessionTimedOut = false;
	serverErr = false;
	spinner = false;
	showErr = false;
	errorMsg = '';
	//public invalidSessErr$ = this.authInterceptorSvc.invalidSess$.asObservable();

	constructor(
		private fb: FormBuilder,
		private router: Router,
	
	//	private campaignService: CampaignsService
	) {}

	loginForm = this.fb.group({
		password: [''],
		email: ['']
	});

	ngOnInit() {
	
	}

	get email() {
		return this.loginForm.controls.email;
	}

	get password() {
		return this.loginForm.controls.password;
	}

	onClickLogin() {
		this.router.navigate(["/main"]);
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
  	}
}
