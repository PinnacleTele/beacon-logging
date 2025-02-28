import { HttpClient, HttpHeaders } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { BehaviorSubject, Observable, throwError } from "rxjs";
import { CONSTANTS } from "src/app/shared/campaigns.constants";
import { CONSTANTS_URL } from "src/app/shared/compaign.url";
import { catchError, map } from "rxjs/operators";
import { ERROR } from "src/app/campaigns/error.data";



@Injectable({
    providedIn: "root"
})
export class SetIntervalService {
// error strings
BASE_URL = CONSTANTS_URL.GLOBAL_URL;

badError: any;

previewDate: Map<string, any> = new Map<string, any>();


previousData:any;

csearch:string = "";

scsearch:string = "";

flag:boolean = false;

scflag:boolean = false;

scpreviousData:any;



    constructor(public http: HttpClient) {}

    httpOptions = {
        headers: new HttpHeaders({
            Accept: "application/json",
            "Content-Type": "application/json"
        })
    };


    setPrevious(value:any,flag:any){
        // this.label = value;
        let obj = { 
            label : value,
            flag:flag
        }
        this.flag = flag;
        this.previousData = value;

    }

    setcampSearch(value:any){
        this.csearch = value;
    }
    setschcampSearch(value:any){
        this.scsearch = value;
    }

    fromBack(value:any){
        if(value == "campaignDetail"){
       
            this.flag = true;          
        }else{
            this.flag = false;
        }
    }

    setscPrevious(value:any,flag:any){
        // this.label = value;
        let obj = { 
            label : value,
            flag:flag
        }
        this.scflag = flag;
        this.scpreviousData = value;

    }
    fromschBack(value:any){
        if(value == "schedulecamp"){
       
            this.scflag = true;          
        }else{
            this.scflag = false;
        }
    }

    campaignList(type): Observable<any> {
       
        return this.http
            .post(
              this.BASE_URL+CONSTANTS_URL.CAMPAIGN_LIST,type
            )
            .pipe(
                map((responseData : any) => {
                  
                    return responseData as any;
                }),
                catchError((err) => {
                    return this.errHandler(err);
                    // return throwError(err);
                })
            );
    }

    campaignListStats(): Observable<any> {
        return this.http
            .get(
              this.BASE_URL+CONSTANTS_URL.CAMPAIGN_LIST_STATS
            )
            .pipe(
                map((responseData : any) => {
                    return responseData as any;
                }),
                catchError((err) => {
                    return this.errHandler(err);
                })
            );
    }
      
    errHandler(errValue:any){
        if (errValue.status == 0) {
                      
            let setError = ERROR.REQUEST_NOT_SEND;

            this.badError = setError;
        }else {
            
            let setError =ERROR.SOMETHING_WENT_WRONG;
            this.badError =setError;
        }
        return this.badError
    }
    
}
