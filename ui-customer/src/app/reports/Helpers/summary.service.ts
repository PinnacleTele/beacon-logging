import { HttpClient, HttpHeaders } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { map ,catchError, tap } from "rxjs/operators";
import { CONSTANTS_URL } from "../../shared/compaign.url";
import { BehaviorSubject, throwError } from 'rxjs';
import { ERROR } from "src/app/campaigns/error.data";
import { CONSTANTS } from "src/app/shared/campaigns.constants";


@Injectable({
    providedIn: "root"
})
export class ReportService {

BASE_URL = CONSTANTS_URL.GLOBAL_URL;

R_DETAIL = CONSTANTS_URL.REPORT_DETAILED_URL;

R_SEARCH = CONSTANTS_URL.REPORT_LOG_SEARCH;

R_SUMMARY = CONSTANTS_URL.REPORT_SUMMARY_URL;

R_SOURCE = CONSTANTS_URL.REPORT_SOURCE_URL;

R_CAMPAIGN = CONSTANTS_URL.REPORT_CAMPAIGN_URL;

R_SENDERID = CONSTANTS_URL.REPORT_SENDERID_URL;

DOWNLOAD_LOG = CONSTANTS_URL.REPORT_DETAIL_DOWNLOAD_LOG;

DOWNLOAD_SEARCH = CONSTANTS_URL.REPORTSEARCH_LOG_DOWNLOAD;

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

summaryLoading= new BehaviorSubject<boolean>(false);

    getSummaryList(value) {
        this.summaryLoading.next(true);
        return this.http
            .post(
               this.BASE_URL + this.R_SUMMARY,value,

                this.httpOptions
            )
            .pipe(
                map((responseData : any) => {
                    this.summaryLoading.next(false);
                    return responseData as any;
                }),
                catchError((err) => {
                    this.summaryLoading.next(false);
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


    detailedLoading= new BehaviorSubject<boolean>(false);

    getDetailedList(value) {
        this.detailedLoading.next(true);
        return this.http
            .post(
               this.BASE_URL + this.R_DETAIL,value,

                this.httpOptions
            )
            .pipe(
                map((responseData : any) => {
                    this.detailedLoading.next(false);
                    return responseData as any;
                }),
                catchError((err) => {
                    this.detailedLoading.next(false);
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

    searchLoading= new BehaviorSubject<boolean>(false);

    getSearchList(value) {
        this.searchLoading.next(true);
        return this.http
            .post(
               this.BASE_URL + this.R_SEARCH,value,

                this.httpOptions
            )
            .pipe(
                map((responseData : any) => {
                    this.searchLoading.next(false);
                    return responseData as any;
                }),
                catchError((err) => {
                    this.searchLoading.next(false);
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




    public searchData(value: any): Observable<any> {
        return (this.totalRecords = value);
    }


    sourceLoading= new BehaviorSubject<boolean>(false)


     getReportSource(value : any)   {
        this.sourceLoading.next(true);
        return this.http
            .post( this.BASE_URL + this.R_SOURCE,value , this.httpOptions )
            .pipe(
                map((responseData : any) => {
                    this.sourceLoading.next(false)

                    return responseData as any;
                }),
                catchError((err) => {
                    this.sourceLoading.next(false)


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

      campaignLoading= new BehaviorSubject<boolean>(false)

     getReportCampaign(value : any)   {
        this.campaignLoading.next(true)

       return this.http
            .post( this.BASE_URL + this.R_CAMPAIGN,value , this.httpOptions )
            .pipe(
                map((responseData : any) => {
                    this.campaignLoading.next(false)

                    return responseData as any;
                }),
                catchError((err) => {
                    this.campaignLoading.next(false)

                    this.summaryLoading.next(false)
                    if (err.status == 0) {

                        let setError = ERROR.REQUEST_NOT_SEND;

                        this.badError = setError;
                    }else {
                        let setError = ERROR.SOMETHING_WENT_WRONG;
                        this.badError =setError;
                    }
                    

                    return this.badError;
                })
                );;
      }
     senderidLoading= new BehaviorSubject<boolean>(false)

      getReportSenderid(value: any): Observable<any> {
        let payload = {
            "t_name": value.templateName,
            "id": value.id
          }
          this.senderidLoading.next(true)

        return this.http
            .post(
                this.BASE_URL + this.R_SENDERID,value
               ,
                this.httpOptions
            )
            .pipe(
                map((responseData : any) => {
                    this.senderidLoading.next(false)

                    return responseData as any;

                }),
                catchError((err) => {
                    this.senderidLoading.next(false)
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

    summaryDownloadApiLoading= new BehaviorSubject<boolean>(false);



    public downloadrsummary(value): Observable<any> {
        this.summaryDownloadApiLoading.next(true);
        // Create url
        let payload = {
            dateselectiontype: value.dateselectiontype,
            fdate:  value.fdate,
            tdate:  value.tdate,
            source:  value.source,
            campaign_id: value.campaign_id,
            senderid: value.senderid,
            reportby:  value.reportby
        };
        const url = this.BASE_URL + CONSTANTS_URL.REPORT_DOWNLOAD_URL;
        //var body = { filename: payload };

        return this.http.post(url, payload, {
            // responseType: "blob",
            responseType: "text",
            headers: new HttpHeaders()
        })
        .pipe(
            map((responseData : any) => {
                this.summaryDownloadApiLoading.next(false);

                return responseData as any;

            }),
            catchError((err) => {
                this.summaryDownloadApiLoading.next(false);
                if (err.status == 0) {

                    let setError = ERROR.REQUEST_NOT_SEND;

                    this.badError = setError;
                }else {
                    let setError = ERROR.SOMETHING_WENT_WRONG;
                    this.badError =setError;
                }
                return this.badError;
            })
        );;
    }

      downloadLOGapiLoading= new BehaviorSubject<boolean>(false);



      public downloadrDetailed(value): Observable<any> {
        this.downloadLOGapiLoading.next(true);
          // Create url
          let payload = {
              dateselectiontype: value.dateselectiontype,
              fdate:  value.fdate,
              tdate:  value.tdate,
              source:  value.source,
              campaign_id: value.campaign_id,
              senderid: value.senderid,
              reportby:  value.reportby,
              status : value.status
            }
          let url = this.BASE_URL + this.DOWNLOAD_LOG;
          //var body = { filename: payload };

          return this.http.post(url, value,this.httpOptions)
          .pipe(
            map((responseData : any) => {
                this.downloadLOGapiLoading.next(false);

                return responseData as any;

            }),
            catchError((err) => {
                this.downloadLOGapiLoading.next(false);
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

        downloadSearachapiLoading= new BehaviorSubject<boolean>(false);



        public downloadrSearch(value): Observable<any> {
            this.downloadSearachapiLoading.next(true);
            let url = this.BASE_URL +CONSTANTS_URL.REPORTSEARCH_LOG_DOWNLOAD;
            //var body = { filename: payload };

            return this.http.post(url, value, {
                // responseType: "blob",
                responseType: "text",
                headers: new HttpHeaders()
            })
            .pipe(map((response:any)=>{
                this.downloadSearachapiLoading.next(false);
                
                return response as any;
            }),
            catchError((err) => {
               // console.log(err);
               this.downloadSearachapiLoading.next(false);
                if (err.status == 0) {

                    let setError = ERROR.REQUEST_NOT_SEND;

                    this.badError = setError;
                }else {
                    let setError = ERROR.SOMETHING_WENT_WRONG;
                    this.badError =setError;
                }
                 return err;
            })
        
            );
          }
      
}


