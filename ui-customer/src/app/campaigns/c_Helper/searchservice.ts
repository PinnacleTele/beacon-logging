import { HttpClient, HttpHeaders } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { BehaviorSubject, forkJoin, Observable } from "rxjs";

@Injectable({
    providedIn: "root"
})
export class SearchService {
// error strings
totalRecords: any;

ScheduleRecords: BehaviorSubject<Array<any>> = new BehaviorSubject([]);
 
scheduleData = this.ScheduleRecords.asObservable();

totalScheduleRecords: any;

    constructor(public http: HttpClient) {}

    httpOptions = {
        headers: new HttpHeaders({
            Accept: "application/json",
            "Content-Type": "application/json"
        })
    };

    public searchData(value: any): Observable<any> {
               
        return   (this.totalRecords = value);
    }

    get data(){
        return   this.totalRecords;
    }

    get sdata(){
        return   this.totalScheduleRecords;
    }

    public searchScheduleData(value: any): Observable<any> {
        this.ScheduleRecords.next(value);
        return (this.totalScheduleRecords = value);
    }
      
    
    
}
