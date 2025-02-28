import { Injectable } from "@angular/core";
import { Observable, throwError, BehaviorSubject } from "rxjs";
import { HttpHeaders, HttpClient } from "@angular/common/http";
import { catchError, map } from "rxjs/operators";
import { CONSTANTS_URL } from "../compaign.url";

@Injectable({
    providedIn: "root"
})
export class TimeZoneService {
    BASE_URL = CONSTANTS_URL.GLOBAL_URL;
    public loadingTZ$ = new BehaviorSubject<boolean>(false); 

    API_URL = CONSTANTS_URL.TIME_ZONE;

    constructor(public http: HttpClient) {}

    httpOptions = {
        headers: new HttpHeaders({
            Accept: "application/json",
            "Content-Type": "application/json"
        })
    };

    getTimeZone(): Observable<any> {
        this.loadingTZ$.next(true);
        return this.http
            .get(this.BASE_URL + this.API_URL, this.httpOptions)
            .pipe(
                map((responseData) => {
                    this.loadingTZ$.next(false);
                    return responseData;
                }),
                catchError((err) => {
                    this.loadingTZ$.next(true);
                    return throwError(err);
                })
            );
        // .pipe(map((data) => data as any   ));
    }
}
