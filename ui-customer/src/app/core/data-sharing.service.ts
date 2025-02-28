import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { BehaviorSubject, throwError } from "rxjs";
import { catchError, map } from "rxjs/operators";
import { ERROR } from "../campaigns/error.data";
import { CONSTANTS } from "../shared/campaigns.constants";
import { CONSTANTS_URL } from "../shared/compaign.url";

@Injectable({
    providedIn: "root"
})
export class DataSharingService {
    constructor(private http: HttpClient) { }
    dataStore: Map<string, any> = new Map<string, any>();

    private timeZoneStore: Map<string, any> = new Map<string, any>();

    selectedTimes: Map<string, any> = new Map<string, any>();

    selectedISTTimes: Map<string, any> = new Map<string, any>();

    previewDates: Map<string, any> = new Map<string, any>();

    dataValue: any[] = [];

    setData(key: string, value: any) {
        this.dataStore.set(key, value);

    }

    setTime(key: string, value: any) {
        this.timeZoneStore.set(key, value);
    }

    setValue(key: any, value: any) {
        this.dataValue.push({ id: key, time: value })
    }

    get() {
        return this.dataValue;
    }

    getValue() {

    }

    getTime(value: string): any {
        return this.timeZoneStore.get(value);
    }

    getWholeTimeData(): Map<string, any> {
        return this.timeZoneStore;
    }

    getWholeData(): Map<string, any> {
        return this.dataStore;
    }

    getData(key: string): any {
        return this.dataStore.get(key);
    }

    scheduledTimes: Map<string, any> = new Map<string, any>();

    BASE_URL = CONSTANTS_URL.GLOBAL_URL;
    AQUICKLINK_URL = CONSTANTS_URL.ACC_QUICK_LINKS_URL


    badError: any
    accountQlinks: any = ""



    getQuickLinks() {
        console.log("get qlinks");

        return this.http.get(this.BASE_URL + this.AQUICKLINK_URL).pipe(
            map((responseData) => {
                this.accountQlinks = responseData
                //  this.accQuickLinks.next(responseData)
                return responseData;
            }),
            catchError((err) => {
                if (err.status == 0) {

                    let setError = ERROR.REQUEST_NOT_SEND;

                    this.badError = setError;
                } //else if(err.status == 400){}
                else {
                    let setError = ERROR.SOMETHING_WENT_WRONG;
                    this.badError = setError;
                }

                return throwError(this.badError);
            })
        );


    }



    quickLink_Url = CONSTANTS_URL.GLOBAL_URL + CONSTANTS_URL.QUICK_LINK_URL
    getACCQuickLinks() {

        console.log("quicklink call");

        return this.http.get(this.quickLink_Url).pipe(
            map((responseData) => {

                return responseData;
            }),
            catchError((err) => {
                if (err.status == 0) {
                    let setError = ERROR.ENTITY_ID_ERROR
                    this.badError = setError;
                } else {
                    this.badError = err.error;
                }

                return throwError(err);
            })
        );
    }
  //  quickLinks: any

    // updateql() {
    //     this.getACCQuickLinks().subscribe((data: any) => {
    //         console.log(data);
    //         this.quickLinks = data;
    //         console.log(this.quickLinks);
    //         return data;




    //     })
    // }
    data: any
    QLsize:number
  //  QLlink:any;
    setquickLinks(params) {
        this.getQuickLinks().subscribe((data:any)=>{
            this. QLsize=data.length;
           // this.QLlink=new Array(CONSTANTS.quickLinkLimit-this.QLsize)
            
         })
        return this.data = params;
        
    }
}
