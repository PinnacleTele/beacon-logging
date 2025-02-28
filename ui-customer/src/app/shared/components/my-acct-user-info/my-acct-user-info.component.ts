import { HttpErrorResponse } from '@angular/common/http';
import { Component, EventEmitter, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { EditAccountService } from 'src/app/account/edit-account/edit-account.service';
import { PersonalInfo } from 'src/app/account/edit-account/model/personal-info';
import { AuthLoginService } from 'src/app/authentication/auth-login.service';
import { LocalStorageService } from 'src/app/authentication/local-storage.service';
import { ERROR } from 'src/app/campaigns/error.data';


@Component({
  selector: 'app-my-acct-user-info',
  templateUrl: './my-acct-user-info.component.html',
  styleUrls: ['./my-acct-user-info.component.css']
})
export class MyAcctUserInfoComponent implements OnInit {
  email = "";
  firstName = "";
  userName = "";
  userTypeDesc = "";
  signout = new EventEmitter();
  response:{message:string,statusCode:number}
  status: string;
  popup = false;
  spinner = false;
  subscription : Subscription;
  constructor(private localStorage : LocalStorageService,
    private authLoginSvc : AuthLoginService,
    private router: Router,
    private editAcctSvc : EditAccountService) { }

  ngOnInit(): void {
    const user = this.localStorage.getLocal('user');
        let userObj = null;
        
        if(user){
            userObj = JSON.parse(user);
            
            this.email = userObj.email;
            this.firstName = userObj.firstname;
            this.userName = userObj.user;
            this.userTypeDesc = this.getUserType(userObj.user_type);
            
        }
     
        
  }

  
  getUserType(user : any){
    if(user === 0) return 'Super Admin'
    if(user === 1) return 'Admin'
    if(user === 2) return 'User'
  }
  onClickSignOut() {
    this.signout.emit();
    this.spinner = true;
    this.authLoginSvc.logout(null).subscribe(
      (res: any) => {
        this.spinner = false;
       
      
      },(error: any) => {
        this.spinner = false;
          this.popup=true;
          this.response = this.authLoginSvc.internalServerErr;
          this.status=this.response.message;
         
      }
    );

  }

  tryAgain(event: any) {
    this.popup =false;
    this.onClickSignOut();
  }

  modalcontinue(event: boolean) {
    this.router.navigate(["/accounts"]);
  }
  modalClose(event: boolean) {
    this.popup = false;
}

}
