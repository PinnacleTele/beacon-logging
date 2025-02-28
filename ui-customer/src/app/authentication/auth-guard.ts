import { Injectable } from '@angular/core';
import { Router, CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, CanActivateChild } from '@angular/router';
import { CommonService } from '../shared/commonservice';
import { AuthLoginService } from './auth-login.service';
import { LocalStorageService } from './local-storage.service';
import { AUTH_URLS } from './url-access';

@Injectable()
export class AuthGuard implements CanActivate, CanActivateChild {
  status: string;
  userDenied = AUTH_URLS.USER_DENIED;
  linkEnds = AUTH_URLS.END_LINKS;
  cluserDenied = AUTH_URLS.OTP_CLUSTER_DENIED;
  postPaidLinkEnds = AUTH_URLS.POSTPAID_END_LINKS;
  clusterType:any;
  constructor(private router: Router,
    private authService: AuthLoginService,
    private localStorageService: LocalStorageService,private commonService:CommonService) { }
  canActivateChild(childRoute: ActivatedRouteSnapshot,
    state: RouterStateSnapshot) {
    let splitOnSlash = state.url.split("/");
 //otp - cluster type auth added
 console.log('can activate child');
 
 if (this.clusterType == 'otp') {
   const urlValue = '/' + splitOnSlash[1];
  var clusterChildDenied = this.cluserDenied.some(s => urlValue.endsWith(s) );
console.log('inside cluster if');

  if (clusterChildDenied) {
    console.log(clusterChildDenied);
    
    this.router.navigate(['/page-401']);//page not found
  }

}
    if (splitOnSlash[0] == "" && splitOnSlash.length == 2) {
      console.log('inside child if');
      
      return false;
    } else {
      console.log('inside child else');
      return true;
    }
  }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
    const userData:any=this.commonService.tokenDecoder();
    let clusterCaseChange = userData.cluster.toLowerCase();
    this.clusterType = clusterCaseChange;
    const user = this.localStorageService.getLocal('user');
    let url = state.url;
    console.log(url,"canActivate");
    // this.navigationEnd = splitOnSlash[splitOnSlash.length-1]
    if (userData != null) {
      let loggedInuserType: number;
      let loggedInuserBillType: number;

      const userObj = this.commonService.tokenDecoder();

      loggedInuserType = userData.user_type;
      loggedInuserBillType = userData.bill_type;
      console.log(loggedInuserType,loggedInuserBillType);
      
      if (url === '/') {
        this.router.navigate(['/dashboard']);
      }

      if (loggedInuserType === 2) {
        

        var result = this.userDenied.some(s => url.startsWith(s));
        var denied = this.linkEnds.some(s => url.endsWith(s));
        if (result || denied) {
          
          
          this.router.navigate(['/page-401']);//page not found
        }
        if (url == "/billing/edit" || url == "/billing") {
          this.router.navigate(['/page-401']);//page not found
        }

      }


      // otp - cluster type auth added
      if (this.clusterType == 'otp') {
        var clusterdenied = this.cluserDenied.some(s => url.endsWith(s));
        if (clusterdenied) {
          console.log('cluster denied');
          
          this.router.navigate(['/page-401']);//page not found
        }

      }
      // if(loggedInuserType === 2){
      //   // /billing/edit
      // }
      if (loggedInuserBillType === 0) {
         console.log("step 1")

        var billTypeDenied = this.postPaidLinkEnds.some(s => url.endsWith(s));

        if (billTypeDenied) {
          console.log('billtype denied');
          
          this.router.navigate(['/page-401']);//page not found
        }

      }
      return true;
    } else {
      //console.log("no token");
      //this.authService.logout();
      this.router.navigate(['/login']);
    }


    this.router.navigate([url]);
    this.router.navigate(['/']);
    return false;
  }
}
