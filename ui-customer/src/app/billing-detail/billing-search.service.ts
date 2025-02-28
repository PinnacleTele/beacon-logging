import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class BillingSearchService {

  totalRecords: any;

  BillingList: BehaviorSubject<Array<any>> = new BehaviorSubject([]);
   
  billingData = this.BillingList.asObservable();
  
  totalBillingList: any;
  
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
          return   this.totalBillingList;
      }
  
      public searchBillingData(value: any): Observable<any> {
          console.log(value);
          
          this.BillingList.next(value);
          return (this.totalBillingList = value);
      }
        
}
