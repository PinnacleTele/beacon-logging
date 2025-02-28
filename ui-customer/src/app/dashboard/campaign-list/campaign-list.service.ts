import { HttpClient, HttpHeaders } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { map } from "rxjs/operators";
import { CampaignsList } from "../dashboard.models";
import { CONSTANTS_URL } from "../../shared/compaign.url";
@Injectable({
    providedIn: "root"
})
export class CampaignListService {
    BASE_URL = CONSTANTS_URL.GLOBAL_URL;

    API_URL = CONSTANTS_URL.DASHBOARD_COMPAIGN_POST_LIST;

    campaigns: CampaignsList;

    constructor(public http: HttpClient) {}

    httpOptions = {
        headers: new HttpHeaders({
            Accept: "application/json",
            "Content-Type": "application/json"
        })
    };

    getCampaignsList(body: any): Observable<CampaignsList> {
        return this.http
            .post(this.BASE_URL + this.API_URL, { ...body }, this.httpOptions)
            .pipe(map((data) => data as CampaignsList));
    }
}
