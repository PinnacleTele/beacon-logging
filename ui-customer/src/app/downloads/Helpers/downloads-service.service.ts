import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { ERROR } from 'src/app/campaigns/error.data';
import { CONSTANTS_URL } from 'src/app/shared/compaign.url';

import { HeaderComponent } from "src/app/core/header/header.component";

@Injectable({
    providedIn: 'root'
})
export class DownloadsService {

downloadIds: any[] = [];

    constructor(private http: HttpClient) { }
    totalRecords: any;

    public searchData(value: any): Observable<any> {
        return (this.totalRecords = value);
    }

    BASE_URL = CONSTANTS_URL.GLOBAL_URL;

    badError: any;

    public loadingS_List = new BehaviorSubject<boolean>(false);

    recentDownload: HeaderComponent;

    setHeaderDownloadControl(control: HeaderComponent) {
        this.recentDownload = control;
    }


    downloadStats(): Observable<any> {
         this.loadingS_List.next(true)
        return this.http
            .get(
                this.BASE_URL + CONSTANTS_URL.DOWNLOAD_STATS_URL
            )
            .pipe(
                map((responseData: any) => {
                    // console.log(responseData,'response data');
                     this.loadingS_List.next(false)
                    return responseData as any;
                }),
                catchError((err) => {
                    this.loadingS_List.next(false)
                    if (err.status == 0) {

                        let setError = ERROR.REQUEST_NOT_SEND;

                        this.badError = setError;
                    } else {
                        

                        let setError = ERROR.SOMETHING_WENT_WRONG;
                        this.badError = setError;
                    }
                    return this.badError;
                    // return throwError(err);
                })
            );
    }


    dowloadTableData(date: any): Observable<any> {
        //  console.log(date);

        return this.http.get(CONSTANTS_URL.GLOBAL_URL + CONSTANTS_URL.DOWNLOAD_TABLE_URL + date.dateselectiontype + "&fdate=" + date.fdate + "&tdate=" + date.tdate)
            .pipe(map((res) => {
                return res
            }),
                catchError((err) => {
                    //this.loadingS_List.next(false)
                    if (err.status == 0) {

                        let setError = ERROR.REQUEST_NOT_SEND;

                        this.badError = setError;
                    } else {
                        

                        let setError = ERROR.SOMETHING_WENT_WRONG;
                        this.badError = setError;
                    }
                    return this.badError;
                    // return throwError(err);
                })
            )
    }

    public logDownloadStatus = new BehaviorSubject<boolean>(false);

    setDownloadIds(ids:any){
        this.downloadIds = ids;
    }

    removeDownloadIds(){
        this.downloadIds = [];
    }

    spliceDownloadIds(sid:any){
        //this.downloadIds = [];
        let i = this.downloadIds.map((item: any) => item).indexOf(sid);

        this.downloadIds.splice(i, 1);
               
        this.recentDownload?.downloadProcess(sid);


    }

    getTextFile(val: any) {
        this.logDownloadStatus.next(true);
        //  console.log(date);
        const headers = new HttpHeaders({
            Accept: "application/zip",
            "Content-Type": "application/zip",
            "Cache-Control": "no-cache"
        });

        return this.http
            .get(
                CONSTANTS_URL.GLOBAL_URL + CONSTANTS_URL.DOWNLOAD_LOG_URL + val,
                { headers, responseType: "blob" }
            )
            .pipe(map((res) => {
                this.logDownloadStatus.next(false);

                //console.log(res);
                return res;
            }),
                catchError((err) => {
                    this.logDownloadStatus.next(false);

                    //this.loadingS_List.next(false)
                    if (err.status === 0) {
                        this.badError = ERROR.REQUEST_NOT_SEND;
                    } else {
                        console.error(err);
                        this.badError = ERROR.SOMETHING_WENT_WRONG;
                    }
                    return this.badError;
                    // return throwError(err);
                })
            )
    }


    // getTextFile(filename: string) {
    //      this._downloadFile(filename);
    //     return this.http.get(filename, { responseType: 'text' })
    //         .pipe(
    //             tap(
    //             )
    //         );
    // }

    // private _downloadFile = function (sUrl) {
    //     window.open(sUrl, '_self');
    //     return true;
    // }
}
