import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscriber, Subscription } from 'rxjs';
import { ACCT_CONSTANTS } from 'src/app/account/account.constants';
import { emailValidator } from 'src/app/campaigns/validators/email-validator';
import { maxLengthValidator } from 'src/app/campaigns/validators/maxlength-validator';
import { minLengthValidator } from 'src/app/campaigns/validators/minlength-validator';
import { CONSTANTS } from 'src/app/shared/campaigns.constants';
import { AuthLoginService } from '../auth-login.service';

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css']
})
export class ForgotPasswordComponent implements OnInit {
  serverErr = false;
  spinner = false;
  showErr = false
  errorMsg = "";
  invalidEmail = true;
  popup = false;
  response:{message:string,statusCode:number}
  status: string;
  genericMinLength = CONSTANTS.genericMinFieldMinLength;
  genericMaxLength = ACCT_CONSTANTS.genericFieldMaxLength;
  emailMaxLength = CONSTANTS.emailMaxLength;
  maxLengthMsg = CONSTANTS.ERROR_DISPLAY.fieldMaxLength; 
  subscription : Subscription;
  emailValidation = CONSTANTS.emailPattern;
  emailPatternError = CONSTANTS.ERROR_DISPLAY.emailPatternMsg;
  
  
  forgotPwdForm = this.fb.group({
    userName: ["", { validators: [Validators.required,minLengthValidator(this.genericMinLength),
      maxLengthValidator(this.genericMaxLength)] }],
    email: ["", { validators: [Validators.required, maxLengthValidator(this.emailMaxLength)] }],
    
  });
  constructor(private fb: FormBuilder,
    private router : Router,
    private route : ActivatedRoute,
    private authLoginSvc : AuthLoginService) { }

  ngOnInit(): void {
// added this as an enhancement while fixing bug CU 243
    this.subscription = this.route.queryParams.subscribe(
      params => {
        const paramsVal = params["username"];
        this.user.setValue(paramsVal);
      }
    );
  }

  onBlurEmailValidate(){
    this.email.setValidators([Validators.required, 
    //  Validators.pattern(this.emailValidation),
    emailValidator(),
      maxLengthValidator(this.emailMaxLength)
    ]);
    this.email.updateValueAndValidity();
  }
  get email() {
    return this.forgotPwdForm.controls.email;
  }
  get user() {
    return this.forgotPwdForm.controls.userName;
  }

  onClickReset(){
    this.invalidEmail = false;
    this.serverErr = false;
    this.showErr = false;
    this.user.markAsTouched({ onlySelf: true });
    this.email.markAsTouched({ onlySelf: true });
    this.user.setValue(this.user.value.trim());
    this.email.setValue(this.email.value.trim());
    if(this.forgotPwdForm.valid){
      this.spinner = true;
      this.authLoginSvc.forgotPwd(this.email.value, this.user.value)
          .subscribe( (res:any) => {
            this.spinner = false;
            this.popup = true;
          if(res.statusCode < 0){
            this.response = res;
            this.status = res.message;
          }else{
            this.response = res;
            this.status = res.message
          }
            
      } ,
      (err) => {
          this.spinner = false;
          this.popup = true;
          this.serverErr = true;
          this.response = this.authLoginSvc.badError;
          
          this.status=this.response.message
          
          this.showErr = true;
        
      });
    }else{
      this.user.markAsTouched({ onlySelf: true });
      this.email.markAsTouched({ onlySelf: true });
    }
  }

  tryAgain(event: any) {
    this.popup =false;
    this.onClickReset();
  }

  toLogin() {
    this.router.navigate(["/login"]);
  }
  modalClose(event: any) {
    this.popup = false;
  }
}
