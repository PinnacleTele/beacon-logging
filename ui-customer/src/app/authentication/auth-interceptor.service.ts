import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpErrorResponse
} from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { throwError , Observable, Subject} from 'rxjs';
import { catchError, switchMap, tap } from 'rxjs/operators';
import { CONSTANTS } from '../shared/campaigns.constants';
import { AuthLoginService } from './auth-login.service';
import { LocalStorageService } from './local-storage.service';

@Injectable({
  providedIn: 'root'
})
export class AuthInterceptorService implements HttpInterceptor {
  
public msgTest : any;
token = "";
refreshToken = "";
refreshTokenRetry = CONSTANTS.RETRY_COUNT;
refreshTokenInProgress = false;
retry = 0;
    tokenRefreshedSource = new Subject();
    tokenRefreshed$ = this.tokenRefreshedSource.asObservable();
  constructor(private localStorage : LocalStorageService,
    private router: Router,
    private authLoginService : AuthLoginService) { }

 
  handleResponseError(error, request?, next?) {
            
    if (error.status === 401 && this.retry < this.refreshTokenRetry
      && !request.url.includes("auth/token") ) {
        
      this.retry++;
      
        return this.getRefreshToken().pipe(
            switchMap(() => {
             
                request = this.setAuthHeader(request);
                return next.handle(request);
                
            }),
            catchError(e => {
              
              
                if (e.status !== 401) {
                  
                    return this.handleResponseError(e);
                } else {
                  
                  this.authLoginService.logoutInvalidSess();
                  
                    
                }
            }));
    }else if (error.status === 401 && 
              !request.url.includes("auth/token") && this.retry >= this.refreshTokenRetry) {
                
                this.retry = 0;
                this.authLoginService.logoutInvalidSess();
          }
   
    return throwError(error);
  }
  setAuthHeader(req :HttpRequest<any>, ){
   
    const user = this.localStorage.getLocal('user');
    if(user!=null){
      let json = JSON.parse(user);
      
      this.token = localStorage.getItem('token');
      this.refreshToken = json["refresh_token"]
      
    }
    
    let authHeader = "Bearer "+this.token;
    
    const modifiedRequest = req.clone({
      headers: req.headers.set('Authorization', authHeader)
    });
    return modifiedRequest;
    
  }

  private _refreshSubject: Subject<any> = new Subject<any>();

  getRefreshToken(){
   
        this.refreshTokenInProgress = true;
        
        //console.log("call service to refresh §§§")
        return this.authLoginService.setRefreshToken().pipe(
            tap(() => {
                this.retry = 0;
                this.refreshTokenInProgress = false;
                //this.tokenRefreshedSource.next();
            }),
            catchError((err) => {
              
                this.refreshTokenInProgress = false;
                
                this.authLoginService.logoutInvalidSess();
              
                return err
            }));
    
}

intercept(request: HttpRequest<any>, next: HttpHandler): Observable<any> {
    // Handle request
  request = this.setAuthHeader(request);

  // Handle response
  return next.handle(request).pipe(catchError(error => {
    
      return this.handleResponseError(error, request, next);
  }));
}

}
