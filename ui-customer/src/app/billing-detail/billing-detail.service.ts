import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { ERROR } from '../campaigns/error.data';
import { CONSTANTS } from '../shared/campaigns.constants';
import { CONSTANTS_URL } from '../shared/compaign.url';

@Injectable({
  providedIn: 'root'
})
export class BillingDetailService {

  constructor(public http: HttpClient) { }

  BASE_URL = CONSTANTS_URL.GLOBAL_URL;
  API_COUNTRY = CONSTANTS_URL.ALL_COUNTRY;

  Manage_Billing_list = CONSTANTS_URL.ManageBillingRateList
  countryArray: Country[] = [];
  badError: any;

  getAllCountries() {

    return this.http
      .get<Country[]>(this.BASE_URL + this.API_COUNTRY)
      .pipe(
        map((responseData) => {

          this.countryArray = [];
          for (const key in responseData) {
            if (
              Object.prototype.hasOwnProperty.call(
                responseData,
                key
              )
            ) {
              this.countryArray.push({
                ...responseData[key]
              });
            }
          }

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


  getBillingList() {

    return this.http
      .get(this.BASE_URL + this.Manage_Billing_list)
      .pipe(
        map((responseData) => {
          // console.log(responseData);

          return responseData;
        }),
        catchError((err) => {
          if (err.status == 0) {

            let setError = ERROR.REQUEST_NOT_SEND;

            this.badError = setError;
          }
          else {
            let setError = ERROR.SOMETHING_WENT_WRONG;
            this.badError = setError;
          }

          return throwError(this.badError);
        })
      );
  }
  ACC_RATES_URL = CONSTANTS_URL.GLOBAL_URL + CONSTANTS_URL.ACC_BILL_RATES;

  getAccRates(cliid: string) {
    let data = {
      "cli_id": cliid
    }
    return this.http.post(this.ACC_RATES_URL, data)
      .pipe
      (map((responceData) => {
        return responceData
      }),
        catchError((err) => {
          if (err.status == 0) {
            let setError = ERROR.REQUEST_NOT_SEND;

            this.badError = setError;
          } else {
            let setError = ERROR.SOMETHING_WENT_WRONG;
            this.badError = setError;
          }
          return this.badError;
        })

      )
  }

  getAcctInfo(acctId: any) {
    // this.cliId = acctId;
    return this.http.get(this.BASE_URL + CONSTANTS_URL.EDIT_ACCT_API + acctId).pipe(map((responseData) => {
      return responseData
    }), catchError((err) => {
      if (err.status == 0) {
        let setError = ERROR.REQUEST_NOT_SEND;
        console.log(err);
        this.badError = setError;
      } else {
        let setError = ERROR.SOMETHING_WENT_WRONG;
        this.badError = setError;
      }
      console.log(this.badError);
      return this.badError;
    })

    )
  }
  // /cm/br/brupdateROW
  ROW_UPDATE_URL = CONSTANTS_URL.GLOBAL_URL + CONSTANTS_URL.UPDATE_ROW_RATES;
  updateROW(cliid: string, details: any) {
    let data = {
      "cli_id": cliid,
      "sms_rate": details.newRate,
      "sms_rate_old": details.oldRate
    }
    console.log(data);

    return this.http.post(this.ROW_UPDATE_URL, data)
      .pipe
      (map((responceData) => {
        return responceData
      }),
        catchError((err) => {
          if (err.status == 0) {
            let setError = ERROR.REQUEST_NOT_SEND;
            console.log(err);

            this.badError = setError;
          } else {
            let setError = ERROR.SOMETHING_WENT_WRONG;
            this.badError = setError;
          }
          console.log(this.badError);

          return this.badError;
        })

      )
  }
  OTHERS_UPDATE_URL = CONSTANTS_URL.GLOBAL_URL + CONSTANTS_URL.UPDATE_OTHERS
  updateOthers(cliid: string, details) {
    return this.http.post(this.OTHERS_UPDATE_URL, details)
      .pipe
      (map((responceData) => {
        return responceData
      }),
        catchError((err) => {
          if (err.status == 0) {
            let setError = ERROR.REQUEST_NOT_SEND;
            console.log(err);
            this.badError = setError;
          } else {
            let setError = ERROR.SOMETHING_WENT_WRONG;
            this.badError = setError;
          }
          console.log(this.badError);
          return this.badError;
        })

      )
  }
  UPDATE_INDIAN_RATES = CONSTANTS_URL.GLOBAL_URL + CONSTANTS_URL.UPDATE_INDIAN_RATES
  updateIndianRates(details) {
    return this.http.post(this.UPDATE_INDIAN_RATES, details)
      .pipe
      (map((responceData) => {
        return responceData
      }),
        catchError((err) => {
          if (err.status == 0) {
            let setError = ERROR.REQUEST_NOT_SEND;
            console.log(err);
            this.badError = setError;
          } else {
            let setError = ERROR.SOMETHING_WENT_WRONG;
            this.badError = setError;
          }
          console.log(this.badError);
          return this.badError;
        })

      )
  }

  internationalRates() {
    return this.http.get(this.BASE_URL + CONSTANTS_URL.API_INTL_RATE).pipe(map((responseData) => {
      return responseData
    }), catchError((err) => {
      if (err.status == 0) {
        let setError = ERROR.REQUEST_NOT_SEND;
        console.log(err);
        this.badError = setError;
      } else {
        let setError = ERROR.SOMETHING_WENT_WRONG;
        this.badError = setError;
      }
      console.log(this.badError);
      return this.badError;
    //  return throwError(err)
    })

    )
  }
}


export interface Country {
  country_code_iso_3: string,
  country: string
}