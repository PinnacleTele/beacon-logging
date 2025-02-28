import { HttpClient, HttpHeaders } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { map } from "rxjs/operators";
import { Statistics } from "../dashboard.models";
import { CONSTANTS_URL } from "../../shared/compaign.url";
@Injectable({
    providedIn: "root"
})
export class StatisticsService {
    BASE_URL = CONSTANTS_URL.GLOBAL_URL;

    API_URL = CONSTANTS_URL.DASHBOARD_COMPAIGN_GET_STATISTICS;

    constructor(public http: HttpClient) {}

    httpOptions = {
        headers: new HttpHeaders({
            Accept: "application/json",
            "Content-Type": "application/json"
        })
    };

    getAccountStatistics(): Observable<Statistics> {
        return this.http
            .get(this.BASE_URL + this.API_URL, this.httpOptions)
            .pipe(map((data) => data as Statistics));
    }
}
