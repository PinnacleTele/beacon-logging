import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

import { catchError, map } from 'rxjs/operators';
import { ERROR } from 'src/app/campaigns/error.data';
import { GroupModel } from 'src/app/campaigns/model/campaign-group-model';
import { CONSTANTS_URL } from 'src/app/shared/compaign.url';
import { SubServices } from '../../../shared/model/service-model';
import { TemplateGroup } from '../../../shared/model/template-group-model';
import { EditAccountService } from '../../edit-account.service';

@Injectable({
  providedIn: 'root'
})
export class SaveApisService {
  BASE_URL = CONSTANTS_URL.GLOBAL_URL;
  EDIT_ACCT_PI = CONSTANTS_URL.EDIT_ACCT_PI;
  EDIT_ACCT_2FA = CONSTANTS_URL.EDIT_ACCT_2FA;
  EDIT_ACCT_MsgSettings = CONSTANTS_URL.EDIT_ACCT_MS;
  EDIT_ACCT_DLT = CONSTANTS_URL.EDIT_ACCT_DLT;
  EDIT_ACCT_SubServices = CONSTANTS_URL.EDIT_ACCT_SubServices;
  EDIT_ACCT_Groups = CONSTANTS_URL.EDIT_ACCT_Groups;
  EDIT_ACCT_ENCRYPT = CONSTANTS_URL.EDIT_ACCT_ENCRYPT;
  EDIT_ACCT_WALLET = CONSTANTS_URL.EDIT_ACCT_WALLET;
  badError: any;


  constructor(private http: HttpClient,
    private editAcctSvc: EditAccountService) { }

  updateGroups(formGroup: any, cliId: number) {
    let formSharedValues = formGroup.controls.sharedGroups.value;
    let sharedGroupsId: string[] = [];

    if (formSharedValues != null || formSharedValues != undefined) {
      formSharedValues.forEach((ele: GroupModel) => {
        sharedGroupsId.push(ele.id)
      });

    }
    let body = {
      "cli_id": cliId,
      "assigned_groups": sharedGroupsId

    }
    //this.updatePILoading.next(true)
    return this.http.post(this.BASE_URL + this.EDIT_ACCT_Groups, body)
      .pipe(map((res) => {
        //this.updatePILoading.next(false)
        this.editAcctSvc.populateSharedGroups("saved", formSharedValues);
        return res

      }), catchError((err) => {
        if (err.status == 0) {
          let setError = ERROR.REQUEST_NOT_SEND;
          this.badError = setError;
        } else {
          let setError = ERROR.SOMETHING_WENT_WRONG;
          this.badError = setError;
        }
        return this.badError;
      }));
  }

  private getHasRow = new Subject<string>();
  notifyHasRowChg(){
    return this.getHasRow.asObservable();
   }
   emitHasRowChanged(theType: string) {
     this.getHasRow.next(theType);
   }
   
  getAcctInfo() {
	//whenever there is an update in the Services section, call the ainfo api and get the 
  //latest value in the editAcctSvc.hasRow
			this.editAcctSvc.getAcctInfoToEdit(this.editAcctSvc.cliId).subscribe(
				(res: any) => {
					if (res) {
                 const value = res.has_row_yn;
                 this.editAcctSvc.hasRow = value;
                 this.emitHasRowChanged(this.editAcctSvc.hasRow); 
					}
				},
				(error: HttpErrorResponse) => {
					
				}
			);
		};
	

  updateServices(formGroup: any, cliId: number) {
    let charSet = "";
    if(formGroup.get('charset1').value){
      charSet = formGroup.get('charset1').value;
    }
    let rowRate = 0;
    if(formGroup.get('rowRate')?.value){
      rowRate = formGroup.get('rowRate').value;
    }
    let body = {
      "cli_id": cliId,
      "smpp_charset": charSet,
      "services": formGroup.controls.subServices.value as Array<SubServices>,
      "row_rate" : rowRate
    }
    //this.updatePILoading.next(true)

    return this.http.post(this.BASE_URL + this.EDIT_ACCT_SubServices, body)
      .pipe(map((res) => {
        //this.updatePILoading.next(false)
        this.editAcctSvc.populateServices(formGroup.controls.subServices.value as Array<SubServices>,
          formGroup.get('charset1').value);
        return res

      }), catchError((err) => {
        if (err.status == 0) {
          let setError = ERROR.REQUEST_NOT_SEND;
          this.badError = setError;
        } else {
          let setError = ERROR.SOMETHING_WENT_WRONG;
          this.badError = setError;
        }
        return this.badError;
      }));
  }


  updateDLT(formGroup: any, cliId: number, userType: number) {
    let formAllocatedValues = formGroup.get('allocatedTG').value;
    let formAssignedValues = formGroup.get('assignedTG').value;

    let allocatedIds: number[] = [];
    if (userType != 2) {
      formAllocatedValues.forEach((ele: TemplateGroup) => {
        allocatedIds.push(ele.template_group_id);
      });
    }

    let assignedIds: number;
    const assignId = formAssignedValues[0].template_group_id;
    const assignedGrpName = formAssignedValues[0].template_group_name;
    assignedIds = assignId;

    let body = {
      "cli_id": cliId,
      "allocated_tgroup_ids": allocatedIds,
      "assigned_tgroup_id": assignedIds
    }
    console.log(this.BASE_URL + this.EDIT_ACCT_DLT);

    //this.updatePILoading.next(true)
    return this.http.post(this.BASE_URL + this.EDIT_ACCT_DLT, body)
      .pipe(map((res) => {
        //this.updatePILoading.next(false);

        this.editAcctSvc.populateDLTCard(formAllocatedValues, assignedIds, assignedGrpName);
        return res

      }), catchError((err) => {
        if (err.status == 0) {
          let setError = ERROR.REQUEST_NOT_SEND;
          this.badError = setError;
        } else {
          let setError = ERROR.SOMETHING_WENT_WRONG;
          this.badError = setError;
        }
        return this.badError;
      }));
  }

  updateMsgSettings(formGroup: any, cliId: number) {
    let offset = formGroup.get('tz').value;
    var withoutParanthesis = offset.substring(offset.indexOf('(') + 1, offset.indexOf(')'));
    var withoutUTC = withoutParanthesis.substring('UTC'.length);
    //let country = formGroup.get('country').value;
    let zone = formGroup.get('zone').value;
    let newlineChar = formGroup.get('newlineChar').value.trim();
    let tzAbbr = formGroup.get('tzAbbr').value;

    let body = {
      "cli_id": cliId,
      "zone_name": zone,
      "offset": withoutUTC,
      //"country_code_iso3": country,
      "newline_chars": newlineChar
    }
    //this.updatePILoading.next(true)
    return this.http.post(this.BASE_URL + this.EDIT_ACCT_MsgSettings, body)
      .pipe(map((res) => {
        //this.updatePILoading.next(false)
        this.editAcctSvc.populateMsgSettings(//country,
          withoutUTC, zone, newlineChar, tzAbbr);
        return res

      }), catchError((err) => {
        if (err.status == 0) {
          let setError = ERROR.REQUEST_NOT_SEND;
          this.badError = setError;
        } else {
          let setError = ERROR.SOMETHING_WENT_WRONG;
          this.badError = setError;
        }
        return this.badError;
      }));
  }

  updateTwoFA(formGroup: any, cliId: number) {
    let twofa = formGroup.get('twofa').value;
    let twofa_yn = 0;

    if (twofa) {
      twofa_yn = 1;
    }
    let body = {
      "cli_id": cliId,
      "twofa_yn": twofa_yn
    }
    //this.updatePILoading.next(true)
    return this.http.post(this.BASE_URL + this.EDIT_ACCT_2FA, body)
      .pipe(map((res) => {
        //this.updatePILoading.next(false)
        this.editAcctSvc.populateTwoFA(twofa_yn);
        return res

      }), catchError((err) => {
        if (err.status == 0) {
          let setError = ERROR.REQUEST_NOT_SEND;
          this.badError = setError;
        } else {
          let setError = ERROR.SOMETHING_WENT_WRONG;
          this.badError = setError;
        }
        return this.badError;
      }));
  }

  updateEncryptInfo(formGroup: any, cliId: number) {
    let encrytMob = formGroup.get('encrytMob').value;
    let encrytMob_yn = 0;

    if (encrytMob) {
      encrytMob_yn = 1;
    }
    let encryMsg = formGroup.get('encryMsg').value;
    let encryMsg_yn = 0;

    if (encryMsg) {
      encryMsg_yn = 1;
    }
    let body = {
      "cli_id": cliId,
      "encrypt_mobile_yn": encrytMob_yn,
      "encrypt_message_yn": encryMsg_yn
    }
    //this.updatePILoading.next(true)
    return this.http.post(this.BASE_URL + this.EDIT_ACCT_ENCRYPT, body)
      .pipe(map((res) => {
        //this.updatePILoading.next(false)
        this.editAcctSvc.populateEncrypt(encrytMob_yn, encryMsg_yn);
        return res

      }), catchError((err) => {
        if (err.status == 0) {
          let setError = ERROR.REQUEST_NOT_SEND;
          this.badError = setError;
        } else {
          let setError = ERROR.SOMETHING_WENT_WRONG;
          this.badError = setError;
        }
        return this.badError;
      }));
  }


  updateWalletRates(formGroup: any, cliId: number,oldsmsRate:number,olddltRate:number) {

    let sms = Number(formGroup.get('SMSRate').value);
    let dlt = Number(formGroup.get('DLTRate').value);
    let body = {
      "cli_id": cliId,
      "smsrate": sms,
      "dltrate": dlt,
      "smsrate_old": oldsmsRate,
      "dltrate_old": olddltRate
    }

    //this.updatePILoading.next(true)
    return this.http.post(this.BASE_URL + this.EDIT_ACCT_WALLET, body)
      .pipe(map((res) => {

        //this.updatePILoading.next(false);
        this.editAcctSvc.populateWalletRates(sms, dlt);
        return res

      }), catchError((err) => {
        if (err.status == 0) {
          let setError = ERROR.REQUEST_NOT_SEND;
          this.badError = setError;
        } else {
          let setError = ERROR.SOMETHING_WENT_WRONG;
          this.badError = setError;
        }
        return this.badError;
      }));
  }

  updatePersonalInfo(formGroup: any, cliId: number) {
    let address = formGroup.get('address').value;
    if (address) {
      address = address.trim();
    }
    let firstName = formGroup.get('firstName').value.trim();
    let lastName = formGroup.get('lastName').value.trim();
    let company = formGroup.get('company').value.trim();
    let currency = formGroup.get('currency').value

    let personalInfo = {
      "cli_id": cliId,
      "firstname": firstName,
      "lastname": lastName,
      "company": company,
      "address": address
    }

    //this.updatePILoading.next(true)
    return this.http.post(this.BASE_URL + this.EDIT_ACCT_PI, personalInfo)
      .pipe(map((res) => {

        //this.updatePILoading.next(false);
        this.editAcctSvc.populatePersonalInfo(firstName, lastName, company, address,currency);
        return res

      }), catchError((err) => {
        //this.updatePILoading.next(false);
        if (err.status == 0) {
          let setError = ERROR.REQUEST_NOT_SEND;
          this.badError = setError;
        } else {
          let setError = ERROR.SOMETHING_WENT_WRONG;
          this.badError = setError;
        }

        return this.badError;
      }));
  }

}
