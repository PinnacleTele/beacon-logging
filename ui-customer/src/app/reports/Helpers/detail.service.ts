import { Injectable } from "@angular/core";
import { Observable } from "rxjs";


@Injectable({
    providedIn: "root"
})
export class ReportDetailService {

    totalRecords: any;

    public searchData(value: any): Observable<any> {
        return (this.totalRecords = value);
    }
      
}
