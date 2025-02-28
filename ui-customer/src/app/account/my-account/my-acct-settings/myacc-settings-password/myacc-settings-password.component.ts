import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ControlContainer, FormBuilder, Validators } from '@angular/forms';
import { subscribeOn } from 'rxjs/operators';
import { ACCT_CONSTANTS } from 'src/app/account/account.constants';
import { AccountService } from 'src/app/account/account.service';
import { EditAccountService } from 'src/app/account/edit-account/edit-account.service';
import { maxLengthValidator } from 'src/app/campaigns/validators/maxlength-validator';
import { passwordValidator } from 'src/app/campaigns/validators/password-validator';

@Component({
  selector: 'app-myacc-settings-password',
  templateUrl: './myacc-settings-password.component.html',
  styleUrls: ['./myacc-settings-password.component.css']
})
export class MyaccSettingsPasswordComponent implements OnInit {
  infoText = ACCT_CONSTANTS.INFO_TXT.password;
  verifyPasswordServerErr = ACCT_CONSTANTS.ERROR_DISPLAY.verifyPwd;
  hideTop = false;
  apiError = false;
  spinner = false;
  
  isEmptyPwd = false;
  verifySpinner = false;
  
  maxLength = ACCT_CONSTANTS.passwordMaxLength;
  minLength = ACCT_CONSTANTS.passwordMinLength;
  pwdValidation = ACCT_CONSTANTS.passwordValidation;
  oldPasswordReqd = ACCT_CONSTANTS.oldPasswordRequiredMsg;
  misspelledNewPwdMsg = ACCT_CONSTANTS.misspelledNewPwdMsg;
  camparePwdMsg = "Newpassword cannot same as the current password"
  enableButton = true;
  invalidPwd = true;
  response:{message:string,statusCode:number}
  status: string;
  popup = false;
  creating = false;
  misspelledNewPwd = true;
  camparePwd = true;

 
  constructor(private editAcctService : EditAccountService,
    private acctService : AccountService,
    private fb : FormBuilder) { }

    pwdFormGroup = this.fb.group({
      currentPwd : [],
      newPwd1 : [],
      newPwd2 : [],
    });
  ngOnInit(): void {
    
    this.newPasswordSetValidators();
   
  }
  onClickReset(){
    
      this.formReset();
      this.hideTop = !this.hideTop;
    
  
    
  }
  verifyPassword(){
      
      this.invalidPwd = false;
      this.isEmptyPwd = false;
      this.currentPwd.setValue(this.currentPwd.value.trim())
      if(this.currentPwd.value !== null 
        && this.currentPwd.value.length > 0){
          this.verifySpinner = true;
          this.apiError = true;
        this.editAcctService.verifyPassword(this.currentPwd.value).
          subscribe((res:any)=>{
          if(res){
            this.verifySpinner = false;
            this.apiError = false;
            if(res.statusCode < 0){
              this.invalidPwd = true;
              
            }
        }
        },
        (error: HttpErrorResponse) => {
          
          this.verifySpinner = false;
            this.apiError = true;
           
        });
      }else{
        this.isEmptyPwd = true;
      }
   
  }

  testAllPassword(){
    this.camparePwd = false;
    if( this.currentPwd.value == this.newPwd1.value){
              this.camparePwd = true;
       
      }

  }

  checkBothPwd(){
    this.misspelledNewPwd = false;
    if(this.newPwd1.value !== null && 
      this.newPwd2.value !== null && 
      this.newPwd1.value !== this.newPwd2.value){
        
        this.misspelledNewPwd = true;
                
    }

  }
  newPasswordSetValidators(){
    
    this.newPwd1.setValidators([Validators.required, 
      passwordValidator(),maxLengthValidator(this.maxLength),
        
    ]);
    this.newPwd1.updateValueAndValidity();
   
    this.newPwd2.setValidators([Validators.required, 
      passwordValidator(),
      maxLengthValidator(this.maxLength),
      
    ]);
    this.newPwd2.updateValueAndValidity();
  }
  onClickButton(){
    
    this.acctService.validateAllFormFields(this.pwdFormGroup);
       if( !this.invalidPwd && !this.misspelledNewPwd &&
      !this.apiError &&
      !this.newPwd1.errors?.hasNoSplChar ){
        this.spinner = true;
      this.editAcctService.updatePassword(this.newPwd1.value).subscribe(
        (res: any) => {
          this.spinner = false;
          
         
          if(res.statusCode < 0 ){
            
            this.response = res
            this.status = res.message
            this.spinner=false;
            this.popup = true;
          }
          if(res.statusCode > 199 || res.statusCode <299){
            
            this.response=res
            this.status=res.message
            this.popup=true;
          }
        },(error: HttpErrorResponse) => {
          
           this.response = this.editAcctService.badError;
           this.status=this.response.message
           this.popup=true;
           this.spinner = false;
        }
       
      );
    }
  }
  modalClose(event: boolean) {
    this.popup = false;
    //this.pwdFormGroup.reset();

  }

  tryAgain(event: any) {
    this.onClickButton();
        
  }

  modalcontinue(event: boolean) {
    this.popup = false;
    this.formReset();
    this.hideTop = !this.hideTop;
    
   // this.router.navigate(["/accounts/list"]);
  }

  onClickCancel(){
    this.popup = false;
    this.hideTop = !this.hideTop;
    this.formReset();
    
  }

  formReset(){
    this.invalidPwd = false;
    this.currentPwd.setValue("");
    this.newPwd1.setValue("");
    this.newPwd2.setValue("");
    this.currentPwd.markAsUntouched();
    this.newPwd1.markAsUntouched();
    this.newPwd2.markAsUntouched();
  }
  

  get currentPwd(){
    return this.pwdFormGroup.controls.currentPwd
  }
  get newPwd1(){
    return this.pwdFormGroup.controls.newPwd1
  }
  get newPwd2(){
    return this.pwdFormGroup.controls.newPwd2
  }

}
