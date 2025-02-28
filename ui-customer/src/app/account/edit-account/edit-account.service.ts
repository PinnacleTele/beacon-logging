import { HttpClient } from '@angular/common/http';
import {  Injectable } from '@angular/core';
import { FormGroup } from '@angular/forms';
import * as _ from 'lodash';
import {  Subject, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { AuthLoginService } from 'src/app/authentication/auth-login.service';
import { LocalStorageService } from 'src/app/authentication/local-storage.service';
import { ERROR } from 'src/app/campaigns/error.data';
import { GroupModel } from 'src/app/campaigns/model/campaign-group-model';
import { CONSTANTS_URL } from 'src/app/shared/compaign.url';
import { SubServices } from '../shared/model/service-model';
import { TemplateGroup } from '../shared/model/template-group-model';
import { EncryptMobMsg } from './model/encrypt-obj';
import { MsgSettings } from './model/msg-settings';
import { PersonalInfo } from './model/personal-info';
import { TwoLvlAuth } from './model/twoFA';

@Injectable({
  providedIn: 'root'
})
export class EditAccountService {
 BASE_URL = CONSTANTS_URL.GLOBAL_URL;
 EDIT_ACCT_API = CONSTANTS_URL.EDIT_ACCT_API;
 MYACCT_STAT = CONSTANTS_URL.MYACCT_STAT;
 VERIFY_PASSWD = CONSTANTS_URL.VERIFY_PASSWD;
 API_UPDATE_PASSWD = CONSTANTS_URL.UPDATE_PASSWD;
 private getAllSharedGroups = new Subject<string>();
 private getAllAcctInfo = new Subject<string>();
 private getAllServices = new Subject<string>();
 private tzChanged = new Subject<MsgSettings>();
 private personalInfoChanged = new Subject<PersonalInfo>();
 cliId : number = 0; 
 badError : any;
 
 allDataRecieved = false;
 //apiResp : any
 personalInfo :PersonalInfo;
 msgSettingObj : MsgSettings;
 twoFAObj : TwoLvlAuth;
 encrypt : EncryptMobMsg;
 country = "";
 tz = "";
 zone = "";
 charSet1 = "";
 smsRate = 0;
 dltRate = 0;
 currency = "";
 cloneDLTCard : TemplateGroup[] = [];
 cloneDLTAssigned : TemplateGroup[] = [];
 sgArray : GroupModel[] = [];
 servicesArr : SubServices[] = [];
 cloneSharedGroups  : GroupModel[] = [];
 cloneServices : SubServices[] = [];
 
 assignedArr : TemplateGroup[] = [];
 dltStatus = "original";
 smppCharset = "";
 fromPage = "";
 hasRow : any;
 constructor(public http: HttpClient,
  private authLoginSvc :AuthLoginService,
  private localStorage : LocalStorageService) { }
 emitPIChanges(PIObj : PersonalInfo) {
  this.personalInfoChanged.next(PIObj);
}

listenForPIChanges(){
 return this.personalInfoChanged.asObservable();
}
 emitTzChanges(tzObj : MsgSettings) {
  this.tzChanged.next(tzObj);
}

listenForTzChanges(){
 return this.tzChanged.asObservable();
}
emitDLT(theType: string) {
  this.getAllAcctInfo.next(theType);
}

notifyDLTCompleted(){
 return this.getAllAcctInfo.asObservable();
}
emitSubServices(theType: string) {
  this.getAllServices.next(theType);
}

notifyServicesCompleted(){
 return this.getAllServices.asObservable();
}
emitSharedGrps(theType: string) {
  
   this.getAllSharedGroups.next(theType);
}

notifySGCompleted(){
  return this.getAllSharedGroups.asObservable();
 }
 verifyPassword(pass : string){
    
    return this.http.post(this.BASE_URL+this.VERIFY_PASSWD ,{"pass" : pass }
      ).pipe(map((responseData)=>{
      return responseData
    }),catchError((err)=>{
        return throwError(err)
    })

)
 }
 getAcctStatistics(){
  
      return this.http.get(this.BASE_URL+this.MYACCT_STAT).pipe(map((responseData)=>{
        return responseData
        }),catchError((err)=>{
              return throwError(err)
        })
      );
  }

 getAcctInfoToEdit(acctId:any){
   this.cliId = acctId;
  return this.http.get(this.BASE_URL+this.EDIT_ACCT_API+acctId).pipe(map((responseData)=>{
    return responseData
  }),catchError((err)=>{
       return throwError(err)
  })

)}

populateAcctInfo(acctObj : any, form : FormGroup, page : string){
  //this.apiResp = _.cloneDeep(acctObj);
  this.servicesArr = [];
  this.currency = acctObj.billing_currency;
  form.controls.currency.setValue(acctObj.currency);
  this.fromPage = page;
  let acctStatus = "";
  if(acctObj.acc_status === 0){
    acctStatus = "Active";
  }else if(acctObj.acc_status === 2){
    acctStatus = "deactivated"
  }
  
  this.hasRow = acctObj.has_row_yn;

  form.controls.acctStatus.setValue(acctStatus);
  form.controls.billType.setValue(acctObj.bill_type);
  
  form.controls.contactMobile.setValue(acctObj.mobile);
  form.controls.contactEmail.setValue(acctObj.email);
  form.controls.userName.setValue(acctObj.user);
  form.controls.walletAmount.setValue(acctObj.wallet);
  
  
  form.controls.userType.setValue(acctObj.user_type);

  //may be null values handle it
  let adminCount = 0;
  
  if(acctObj.total_admins !== undefined ){
    adminCount = acctObj.total_admins;
  }
  form.controls.adminCount.setValue(adminCount);

  let userCount = 0;
  
  if(acctObj.total_users !== undefined ){
    userCount = acctObj.total_users;
  }
  form.controls.userCount.setValue(userCount);
  form.controls.acctCount.setValue(userCount + adminCount);
  
  
  if(acctObj.smpp_charset !== ""){
    
  }

  this.populatePersonalInfo(acctObj.firstname,acctObj.lastname,acctObj.company,acctObj.address, acctObj.currency);  
  this.populateSharedGroups("original",acctObj.assigned_groups);
  this.populateDLTCard(acctObj.allocated_tgroups, acctObj.dlt_templ_grp_id,
    acctObj.dlt_templ_grp_name );
  
  this.populateServices(acctObj.services, acctObj.smpp_charset );

  this.populateMsgSettings(//acctObj.default_country_code,
    acctObj.time_offset, acctObj.time_zone,
    acctObj.newline_replace_char,
    acctObj.time_zone_abbr);
    this.setCtrlMsgSettings(form);
      
    this.populateWalletRates(acctObj.smsrate, acctObj.dltrate);
    //this.setWalletRates(form);

    this.setFormCtrlDLTCard(form);
  this.populateTwoFA(acctObj.two_level_auth);
  this.populateEncrypt(acctObj.encrypt_mobile_yn,acctObj.encrypt_message_yn);
  
  //this.emitDLT("completed");
  
  this.allDataRecieved = true;
  this.setFormCtrlPI(form);
  this.setFormCtrlTwoFA(form);
  this.setFormCtrlEncrypt(form);
 // this.setCtrlServices(form);
  this.setCtrlSharedGrps(form);
  
}

populateWalletRates(sms : any, dlt: any){
  this.smsRate = sms;
  this.dltRate = dlt;
  
}

getOldWalletRates(){
  const object = {
    smsrate : this.smsRate,
    dltrate : this.dltRate
  }
  return object;
}
setWalletRates(form : any){
  form.controls.SMSRate.setValue(this.smsRate);
  form.controls.DLTRate.setValue(this.dltRate);
}
populateSharedGroups(when: string,sg : any){
  
  if(when === "original"){
    this.sgArray = [];
    sg.forEach((el : any)=> {
      this.sgArray.push({id: el.g_id,
                   g_name:el.g_name,
                   g_type: el.g_type,
                  g_visibility: "",
                  total: 0,
                  total_human: "",
                  is_owner: false,
                  created_ts: "",
                  modified_ts: "",
                  created_ts_unix: 0,
                  "checked" :true
                          });
  
    })
  }else{
    this.sgArray = sg;
  }
 
  this.cloneSharedGroups = _.cloneDeep(this.sgArray);
  
  
}
setCtrlSharedGrps(form : any){
  
  this.emitSharedGrps("completed");
}
populateServices(services : any, smpp : string){
  
  if(this.servicesArr.length === 0){
    
    
    services.forEach((element : any) => {
      if(element.enabled_yn == 1){
        element.checked = true;
      }else{element.checked = false;}
      this.servicesArr.push(element);
    });
    
  }else{
    //iterate each element in servicesarr , find if there is an index in services
   
    this.servicesArr.forEach(el => {
        const i = services.findIndex((x: { sub_service: string; }) => x.sub_service === el.sub_service);
       
        if(i === -1){
            el.checked = false;
            el.enabled_yn = 0;
            
        }else{
           el.checked = true;
           el.enabled_yn = 1;
        }
    });
    
  }
  this.cloneServices =  _.cloneDeep(this.servicesArr);
  this.smppCharset = smpp;
}
setCtrlServices(form : any){
 
 form.controls.charset1.setValue(this.smppCharset); 
 this.emitSubServices("completed"); 
}
populateEncrypt(toggle1 : number, toggle2 : number){
 let mobBool = false;
 let msgBool = false;
  if(toggle1 ==1){
    mobBool = true;
  }
  if(toggle2 ==1){
    msgBool = true;
  }
    this.encrypt ={mobile : mobBool, msg :msgBool};
  }

setFormCtrlEncrypt(form: any){
  if(this.allDataRecieved){
    form.controls.encrytMob.setValue(this.encrypt.mobile );
    form.controls.encryMsg.setValue(this.encrypt.msg );
  }
}
populateTwoFA(toggle : number){
  
  if(toggle == 1){
    this.twoFAObj ={toggle : true};
  }else{
    this.twoFAObj ={toggle : false};
  }
}
setFormCtrlTwoFA(form : any){
  if(this.allDataRecieved){

    form.controls.twofa.setValue(this.twoFAObj.toggle );
  }
}
populateMsgSettings(
 
  offset: string, zone : string,
  charsetL : string,tzAbbr : string){
 
  this.msgSettingObj = {
    
    tz : offset,
    zone : zone,
    newlineChar : charsetL,
    tzAbbr : tzAbbr
  };
  if(this.fromPage == "myAcctPage"){
   // var localStorageData = JSON.parse(this.localStorage.getLocal("user"));

    // localStorageData.zone_abbr = tzAbbr;
    // localStorageData.time_zone = zone;
    
    // localStorage.setItem("user", JSON.stringify(localStorageData));
    // this.localStorage.setLocal(JSON.stringify(localStorageData));
    
    
        this.authLoginSvc.setRefreshToken().
        subscribe((res: any) => {
            
        });

   
  }
  
  this.emitTzChanges(this.msgSettingObj);//emit for both edit and myacct changes
  
}

setCtrlMsgSettings(form : any){
    //form.controls.country.setValue(this.msgSettingObj.country);
  form.controls.zone.setValue(this.msgSettingObj.zone);
  form.controls.tzAbbr.setValue(this.msgSettingObj.tzAbbr);
  form.controls.tz.setValue(this.msgSettingObj.tz);
  form.controls.newlineChar.setValue(this.msgSettingObj.newlineChar);
}


populateDLTCard(allocatedDLTs : any,assignedDLTid : any, assignedGrpName : string){
  this.cloneDLTCard = _.cloneDeep(allocatedDLTs);
  this.assignedArr = [];
  let assignedTemp : TemplateGroup = {template_group_id :0,
  template_group_name : "", checked : true};
  assignedTemp.template_group_id = assignedDLTid;
  assignedTemp.template_group_name = assignedGrpName;
  this.assignedArr.push(assignedTemp);
  this.cloneDLTAssigned = _.cloneDeep(this.assignedArr);;
     
}

setFormCtrlDLTCard(form : any){
   
    this.emitDLT(this.dltStatus);
   
}
populatePersonalInfo(fname : string, lname : string, company: string, address : string, currency : string ){
    this.personalInfo = {firstName : fname,
                        lastName : lname,
                        company ,
                        address ,
                        currency };
//    this.firstName = fname;

if(this.fromPage == "myAcctPage"){
  this.emitPIChanges(this.personalInfo);
  
}

    
}
setFormCtrlPI(form : any){
  if(this.allDataRecieved){
        
      form.controls.firstName.setValue(this.personalInfo.firstName);
      form.controls.lastName.setValue(this.personalInfo.lastName);
      form.controls.company.setValue(this.personalInfo.company);
      form.controls.address.setValue(this.personalInfo.address);
      form.controls.currency.setValue(this.personalInfo.currency);
  }
  //return this.personalInfo;
}

updatePassword(paramValue: string) {

  return this.http.post(this.BASE_URL + this.API_UPDATE_PASSWD,
      { "newpass": paramValue }).pipe(
          map((responseData) => {
              
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
getCurrencyType (){
  return this.currency
}

}