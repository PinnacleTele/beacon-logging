import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import {  throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { CONSTANTS_URL } from 'src/app/shared/compaign.url';

@Injectable({
  providedIn: 'root'
})
export class PostMsgPartsService {

  
  GET_MSG_PARTS_API = CONSTANTS_URL.GET_MSG_PARTS_API;
  BASE_URL = CONSTANTS_URL.GLOBAL_URL;

   msg = {
    "uname" : "erttg",
    "pass" : "ert" 
  }
  constructor(private http: HttpClient) {}
  

   badError: any;
  
  getMsgPartsInfo(paramValue : string){
    return this.http.post(this.BASE_URL+this.GET_MSG_PARTS_API, {msg : paramValue}).pipe(
        map((responseData : any) => {
            
           // console.log(responseData);
            //if(responseData.statusCode >299){
              
            return responseData as any;
        }),
        catchError((err) => {
          this.badError = err.error;
            return throwError(err);
        })
    );
}

}
