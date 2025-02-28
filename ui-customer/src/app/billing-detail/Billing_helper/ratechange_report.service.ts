import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { ERROR } from 'src/app/campaigns/error.data';
import { CONSTANTS } from 'src/app/shared/campaigns.constants';
import { CONSTANTS_URL } from 'src/app/shared/compaign.url';

@Injectable({
  providedIn: 'root'
})
export class RateChangeReportService {

  constructor(public http: HttpClient) { }

  BASE_URL = CONSTANTS_URL.GLOBAL_URL;
  API_COUNTRY = CONSTANTS_URL.ALL_COUNTRY;

  USER_LISTAPI = CONSTANTS_URL.VIEW_RATE_USERLIST;

  Manage_Billing_list = CONSTANTS_URL.ManageBillingRateList;

  USER_RATE_CHANGE = CONSTANTS_URL.USERRATE_LISTAPI;
  badError: any;

  BillingList: BehaviorSubject<Array<any>> = new BehaviorSubject([]);
   
  billingData = this.BillingList.asObservable();
  
  totalBillingList: any;
 cliID:any;
  userFilterLoading= new BehaviorSubject<boolean>(false);


  getBillingList() {

    return this.http
      .get('assets/billinglist.db.json')
      .pipe(
        map((responseData) => {
          // console.log(responseData);

          return responseData;
        }),
        catchError((err) => {
          if (err.status == 0) {

            let setError = ERROR.REQUEST_NOT_SEND;

            this.badError = setError;
          }
          else {
            let setError = ERROR.SOMETHING_WENT_WRONG;
            this.badError = setError;
          }

          return throwError(this.badError);
        })
      );
  }

  getUserList() {
    this.userFilterLoading.next(true);
    return this.http
      .get(this.BASE_URL + this.USER_LISTAPI)
      .pipe(
        map((responseData) => {
          // console.log(responseData);
          this.userFilterLoading.next(false);
          return responseData;
        }),
        catchError((err) => {
          this.userFilterLoading.next(false);

          if (err.status == 0) {

            let setError = ERROR.REQUEST_NOT_SEND;

            this.badError = setError;
          }
          else {
            let setError = ERROR.SOMETHING_WENT_WRONG;
            this.badError = setError;
          }

          return throwError(this.badError);
        })
      );
  }

  getbillRateChangeList(params:any) {
    return this.http
      .post(this.BASE_URL + this.USER_RATE_CHANGE,params)
      .pipe(
        map((responseData) => {
          // console.log(responseData);

          return responseData;
        }),
        catchError((err) => {
          if (err.status == 0) {

            let setError = ERROR.REQUEST_NOT_SEND;

            this.badError = setError;
          }
          else {
            let setError = ERROR.SOMETHING_WENT_WRONG;
            this.badError = setError;
          }

          return throwError(this.badError);
        })
      );
  }

public setCliIDforRoute(value:any){
this.cliID = value;
}
  public searchBillingData(value: any): Observable<any> {
    console.log(value);
    
    this.BillingList.next(value);
    return (this.totalBillingList = value);
}
}