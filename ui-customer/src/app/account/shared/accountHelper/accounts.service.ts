import { HttpClient, HttpHeaders } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { forkJoin, Observable } from "rxjs";
import { map ,catchError } from "rxjs/operators";
// import { TemplateModel } from "../model/templatemodal";
import { CONSTANTS_URL } from "src/app/shared/compaign.url";

import { ERROR } from "src/app/campaigns/error.data";

@Injectable({
    providedIn: "root"
})
export class AccountsService {
    BASE_URL = CONSTANTS_URL.GLOBAL_URL;

    API_URL = CONSTANTS_URL.ACCOUNTS_LIST;
    API_STATS = CONSTANTS_URL.ACCOUNT_STATS;
    API_DELETEURL = CONSTANTS_URL.TEMPLATES_API_DELETEURL;
    API_UPDATEURL = CONSTANTS_URL.TEMPLATES_API_UPDATEURL;
    API_CREATEURL = CONSTANTS_URL.TEMPLATES_API_CREATEURL;
  

// error strings
badError : any ;
totalRecords: any;

    constructor(public http: HttpClient) {}

    httpOptions = {
        headers: new HttpHeaders({
            Accept: "application/json",
            "Content-Type": "application/json"
        })
    };

   getAccountsList(): Observable<any> {
        return this.http
            .get(
                this.BASE_URL+this.API_URL
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
    getSTATS_List() {
        
        return this.http
            .get(
                this.BASE_URL+this.API_STATS
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
                    }else if(err.status == 401){
                        
                        this.badError =ERROR.UNAUTHORIZED;
                    }else{
                        let setError = ERROR.SOMETHING_WENT_WRONG;
                        this.badError =setError;
                    }
                    return this.badError;
                })
            );
    }

    public searchData(value: any): Observable<any> {
        return (this.totalRecords = value);
    }
      
    
}
