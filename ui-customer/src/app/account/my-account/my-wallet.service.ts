import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { ERROR } from 'src/app/campaigns/error.data';
import { CONSTANTS_URL } from 'src/app/shared/compaign.url';

@Injectable({
  providedIn: 'root'
})
export class MyWalletService {
  badError : any ;
  BASE_URL = CONSTANTS_URL.GLOBAL_URL;

  API_URL = CONSTANTS_URL.MY_WALLET_ALLTX;

  constructor(public http: HttpClient) {}

  totalRecords: any;

  totalUserRecords: any;


  public searchData(value: any): Observable<any> {
      return (this.totalRecords = value);
  }

  public searchUserData(value: any): Observable<any> {
    return (this.totalUserRecords = value);
}
  getAllWalletTx(selectionValues : any): Observable<any> {
      console.log(selectionValues);
      
    return this.http
        .post(
            this.BASE_URL+this.API_URL,selectionValues
            // '/assets/d.json'
            
        )
        .pipe(
            map((responseData : any) => {
                
                return responseData as any;

            }),
            catchError((err) => {
                if (err.status == 0) {
                        
                    let setError = ERROR.REQUEST_NOT_SEND;

                    this.badError = setError;
                }else {
                    let setError = ERROR.SOMETHING_WENT_WRONG;
                    this.badError =setError;
                }

                //return this.badError;
                return null
            })
        );
}
}
