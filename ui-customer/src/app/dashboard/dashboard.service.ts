import { Injectable } from "@angular/core";
import { map ,catchError } from "rxjs/operators";
import { CONSTANTS_URL } from "src/app/shared/compaign.url";
import { ERROR } from "src/app/campaigns/error.data";

import { BehaviorSubject, Observable } from "rxjs";
import { HttpClient, HttpHeaders } from "@angular/common/http";
import { THIS_EXPR } from "@angular/compiler/src/output/output_ast";
@Injectable({
    providedIn: "root"
})
export class DashboardService {
    
 
    constructor(private http: HttpClient) {}

    BASE_URL = CONSTANTS_URL.GLOBAL_URL;

    DHTREND_API_URL = this.BASE_URL + CONSTANTS_URL.DHTREND_API_URL;
    STATS_API_URL =this.BASE_URL + CONSTANTS_URL.STATS_API_URL;
    PROCESSED_API_URL = this.BASE_URL + CONSTANTS_URL.PROCESSED_API_URL;
    TREND_API_URL = this.BASE_URL + CONSTANTS_URL.TREND_API_URL;
    // API_CREATEURL = CONSTANTS_URL.TEMPLATES_API_CREATEURL;
    // API_DLTTEMPLATE_URL = CONSTANTS_URL.TEMPLATES_API_DLTTEMPLATE_URL;

    // API_UNUSED_DLT_TEMPLATE_URL = CONSTANTS_URL.TEMPLATES_API_DLTTEMPLATE_URL;


// error strings
badError : any ;


    httpOptions = {
        headers: new HttpHeaders({
            Accept: "application/json",
            "Content-Type": "application/json"
        })
    };

    dhTrendLoading = new BehaviorSubject<boolean>(false);

    getDHTrendData(loader) {

        if (loader == "onload") {
            this.dhTrendLoading.next(true);
        }
       
        return this.http
            .get<any>(
                this.DHTREND_API_URL,

                this.httpOptions
            )
            .pipe(
                map((responseData : any) => {
                    this.dhTrendLoading.next(false);
                    return responseData as any;
                }),
                catchError((err) => {
                    this.dhTrendLoading.next(false);
                    if (err.status == 0) {
                            
                        let setError = ERROR.REQUEST_NOT_SEND;

                        this.badError = setError;
                    }else {
                        let setError = ERROR.SOMETHING_WENT_WRONG;
                        this.badError =setError;
                    }
                    //console.log(this.badError,'bad');
                    
                    return this.badError;
                })
                );
            // .pipe(map((data) => data as TemplateModel));
    }

    getTrendDataLoading = new BehaviorSubject<boolean>(false)


     getTrendWiseData(value,loader)   {
         if (loader == 'onload') {
            this.getTrendDataLoading.next(true);
         }
       
        return this.http
            .get<any>(this.TREND_API_URL+value , this.httpOptions )
            .pipe(
                map((responseData : any) => {
                    this.getTrendDataLoading.next(false);
                    return responseData as any;
                }),
                catchError((err) => {
                    this.getTrendDataLoading.next(false);
                    if (err.status == 0) {
                            
                        let setError = ERROR.REQUEST_NOT_SEND;

                        this.badError = setError;
                    }else {
                        let setError = ERROR.SOMETHING_WENT_WRONG;
                        this.badError =setError;
                    }
                    return this.badError;
                })
                );
            
            // .pipe(map((data) => data as TemplateModel));
      }

      getStatsDataLoading = new BehaviorSubject<boolean>(false)

      
     getStatsUrl(loader)   {
        if (loader == "onload") {
            this.getStatsDataLoading.next(true);
        }
      
       return this.http
            .get(this.STATS_API_URL, this.httpOptions ) 
            .pipe(
                map((responseData : any) => {
                    this.getStatsDataLoading.next(false);
                    return responseData as any;
                }),
                catchError((err) => {
                    this.getStatsDataLoading.next(false);
                    if (err.status == 0) {
                            
                        let setError = ERROR.REQUEST_NOT_SEND;

                        this.badError = setError;
                    }else {
                        let setError = ERROR.SOMETHING_WENT_WRONG;
                        this.badError =setError;
                    }
                    return this.badError;
                })
            );
      }

      getProcessedDataLoading= new BehaviorSubject<boolean>(false)

      getProcessedUrl(value,loader): Observable<any> {
          if (loader == "onload") {
            this.getProcessedDataLoading.next(true);

          }
        return this.http
            .get(
                this.PROCESSED_API_URL+value,
                this.httpOptions
            )
            .pipe(
                map((responseData : any) => {
                    this.getProcessedDataLoading.next(false);
                    return responseData as any;
                }),
                catchError((err) => {
                    this.getProcessedDataLoading.next(false);
                    if (err.status == 0) {
                            
                        let setError = ERROR.REQUEST_NOT_SEND;

                        this.badError = setError;
                    }else {
                        let setError = ERROR.SOMETHING_WENT_WRONG;
                        this.badError =setError;
                    }
                    return this.badError;
                })
            );
    }
 
 
 
}