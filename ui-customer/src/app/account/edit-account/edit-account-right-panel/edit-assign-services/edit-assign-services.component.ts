import { getCurrencySymbol } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { ControlContainer, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import * as _ from 'lodash';
import { Subscription } from 'rxjs';
import { ACCT_CONSTANTS } from 'src/app/account/account.constants';
import { AccountService } from 'src/app/account/account.service';
import { SetValidationsService } from 'src/app/account/set-validations.service';
import { SubServices } from 'src/app/account/shared/model/service-model';
import { SaveApisService } from '../../buttons/save-button/save-apis.service';
import { EditAccountService } from '../../edit-account.service';
import { value } from 'src/app/shared/campaigns.constants';
import { ThrowStmt } from '@angular/compiler';
import { debounceTime } from 'rxjs/operators';

@Component({
  selector: 'app-edit-assign-services',
  templateUrl: './edit-assign-services.component.html',
  styleUrls: ['./edit-assign-services.component.css'],
  
  })
export class EditAssignServicesComponent implements OnInit, OnDestroy {
  @Input() fromPage : any;
  
  subServices : SubServices[] = [];
  selectedServices :  SubServices[] = [];
  responce:{message:string,statusCode:number};
  status: string;
  popup = false;
  showServices = false;
  showSMPP = false;
  showROWSection = false;
  showEditSection = false;
  showSlider = false;
  openClearModal = false;
  formChanged = false;
  subscription : Subscription;
  myOptions = value;
  deimalErrText = ACCT_CONSTANTS.ERROR_DISPLAY.rowRatedecimalPatternMsg;
  servicesFormGroup = this.fb.group({
    charset1 :['',],
    subServices: [],
    rowRate : []
  });
  rowRates : any;
  rowConvRate :any;
  currencyType: string;
  currencySymbol: string;
  intlSvcEnabled = false;
  origSMPP = "";
  constructor(private fb :FormBuilder,
    private accountService: AccountService,
    private setValidatorService: SetValidationsService,
    private editAcctService : EditAccountService,
    private saveApiSvc : SaveApisService,
    private router:Router){}

  ngOnInit(): void {
    this.editAcctService.setCtrlServices(this.servicesFormGroup);
    this.rowRates = this.setValidatorService.apiRowRate ;
    this.setValues(this.fromPage);
    this.origSMPP = this.editAcctService.smppCharset;
    this.servicesFormGroup.controls.charset1.setValue(this.editAcctService.smppCharset); 
    this.setServicesValidators();
    this.subscription = this.editAcctService
            .notifyServicesCompleted()
            .subscribe((type : string) => {
              
                  this.setValues(this.fromPage);
           });
    this.saveApiSvc.notifyHasRowChg().subscribe(
      (hasRow : any) => {
        this.setValues(this.fromPage);
       if(hasRow == "1"){
         this.showROWSection = false;
         this.showEditSection = true;
       }
     }
    )
    this.rowRate.valueChanges.pipe(
      debounceTime(400)
    ).subscribe(res=>{
      if(this.rowRate.errors?.required || res==0 || (!isFinite(res))){
        this.rowConvRate=0;
      }
       if(!(this.rowRate.errors?.required || this.rowRate.errors?.lessThanZero) && isFinite(res)){
        this.getConvertedRates(res,"row",9999999 )
       }
    })     
  
    
}
setValues(fromPage : string){
  
  const emittedSubServices = _.cloneDeep(this.editAcctService.cloneServices);
  
  
  if(fromPage === "editAcct"){
    this.subServices = emittedSubServices;
    console.log(this.subServices);
    
    this.selectedServices =[];
    this.subServices.forEach((ele:SubServices) => {
    
      if( ele.enabled_yn == 1 || ele.sub_service === 'cm'){
        ele.checked=true;
        this.selectedServices.push(ele);
       
      }else{
        ele.checked =false;
        this.selectedServices.push(ele);
    }
      if(ele.enabled_yn === 0 && ele.sub_service === 'international'){
        this.showROWSection = false;
        this.intlSvcEnabled = false;
      }
      if(ele.enabled_yn === 1 && ele.sub_service === 'international'){
        
        this.showEditSection = true;
        this.showROWSection = false;
        this.intlSvcEnabled = true;
      }
      if(ele.sub_service === "smpp"){
        this.setValidatorsToFormFields();
      }
    });
    this.servicesFormGroup.controls.subServices.setValue(emittedSubServices);
  }
  
}
onClickService(index : number){
  
  const svcName = this.subServices[index].sub_service.toLowerCase();
  if(svcName !== "cm"){
   
    
    this.subServices[index].checked = !(this.subServices[index].checked);
    if(this.subServices[index].checked){
      this.subServices[index].enabled_yn = 1;
   
      
     // _.cloneDeep(this.selectedServices.push(this.subServices[index]));
      console.log(this.subServices[index]);
      if(svcName === "smpp"){

        this.setValidatorsToFormFields();
        this.servicesFormGroup.controls.charset1.setValue(this.editAcctService.smppCharset); 
        this.showSMPP = true;
      }
      if(svcName === "international" ){
        if(this.editAcctService.hasRow == 0){
          
          this.currencyType = this.editAcctService.getCurrencyType();
          this.currencySymbol = getCurrencySymbol(this.currencyType, 'narrow');
          
          this.showROWSection = true;
          this.showEditSection = false;
          this.getIntlRates();
          this.setValidatorService.setValidationRowRate(this.servicesFormGroup);
        }else{
          
          this.showEditSection =false;
          if(this.intlSvcEnabled){
            
             this.showEditSection =true;
          }
         
          this.showROWSection = false;
        }
        
      }

    }else{
      for (var i = this.selectedServices.length - 1; i >= 0; i--) {
        if (svcName === this.selectedServices[i].sub_service) { 
          this.selectedServices[i].enabled_yn = 0;
          //this.selectedServices.splice(i,1);
        }
      }
      if(svcName === "smpp"){
        this.charsetCtrl.setValue('');
        
        this.clearValidatorsToFormFields();
        this.showSMPP = false;
      }
      if(svcName === "international"){

        this.showROWSection = false;
        this.showEditSection = false;
        this.servicesFormGroup.controls.rowRate.setValue(0);
        this.servicesFormGroup.controls.rowRate.clearValidators();
        this.servicesFormGroup.controls.rowRate.updateValueAndValidity();
      }
    }
    this.setSubServicesVal(this.selectedServices);
  }
  this.checkingValueChange();
}

closeSlider(){
  this.showSlider = false;
}
setSubServicesVal(val : SubServices[]){
  this.formChanged = true;
  const filteredService = val.filter((data: any) => data.enabled_yn == 1);
  this.servicesFormGroup.controls.subServices.setValue(filteredService);
}
handleSMPP(){
  if(this.origSMPP != this.charsetCtrl.value){
    this.formChanged = true;
  }else{
    this.formChanged = false;
  }
  

}
onClickEditRates(){
  this.router.navigate(['billing/edit'],
    {
      queryParams: { id: this.editAcctService.cliId, intl_en : 1 }
      //, skipLocationChange: true  
    });

}
intlRatesApiError : any;
spinner = false;
getIntlRates(){
  this.spinner = true;
  this.intlRatesApiError = false;

  this.rowRate.setErrors({ chkServerError: true  });
  this.accountService.getIntlRates().subscribe(
    (res: any) => {
      	this.rowRate.setErrors(null);
      this.spinner = false;
      
      
      res.forEach((ele: any) => {
        if (ele.country.toLowerCase() == 'row' ) {
          const value = ele.smsrate;
          this.rowRate.setValue(value);
          this.rowRates = ele.smsrate;
          this.getConvertedRates(this.rowRates,"row",9999999 )
          this.setValidatorService.apiRowRate = this.rowRates;
        
        }
        
      });
      

      
    },
    (error: HttpErrorResponse) => {
      this.rowRate.setErrors({ chkServerError: true  });
      this.intlRatesApiError = true;
      this.spinner = false;
    }
  );
  
}
handleRetryClick(){
  this.getIntlRates();

}
convspinner = false;
convPopup = false;
getConvertedRates(value : string, event :string, index:number) {
  this.convspinner = true;
  this.convPopup = false;
      
  this.accountService.getConvRates(value).subscribe(
    (res: any) => {
      //	this.page4FormGroup.otherCountriesBillRates.controls.country.setErrors(null);
      this.convspinner = false;
      this.convPopup = false;
      if(event == "row"){
        this.rowConvRate =res.smsrate
      }
    },(error: HttpErrorResponse) => {
      //	this.page4FormGroup.otherCountriesBillRates.controls.country.setErrors({ apiRequestError: true });
      this.convspinner = false;
      this.convPopup = true;
    }
  );
}
checkRowRate() {

  if (this.rowRate.value > 0 && this.rowRate.value < this.rowRates) {
    
    this.rowRate.setErrors({
      ratelow: true
    })
  }
  this.getConvertedRates(this.rowRate.value,"row",9999999 )
}

get rowRate() {
  return this.servicesFormGroup.controls.rowRate;
}
get subServicesCtrl() {
  return this.servicesFormGroup.controls.subServices;
}
get charsetCtrl() {
  return this.servicesFormGroup.controls.charset1;
}
handleUpdate(){
  this.formChanged = false;
}

setValidatorsToFormFields(){
  this.charsetCtrl.setValidators(Validators.required);
  this.charsetCtrl.updateValueAndValidity();
}

setServicesValidators(){
  this.subServicesCtrl.setValidators(Validators.required);
  this.subServicesCtrl.updateValueAndValidity();
}

clearValidatorsToFormFields(){
  this.charsetCtrl.clearValidators();
  this.charsetCtrl.updateValueAndValidity();
}
ngOnDestroy(){
  this.subscription.unsubscribe();
}


checkingValueChange(){
  let breakLoop = false;
  let ExistValue  = _.cloneDeep(this.editAcctService.cloneServices);
  let formValues = this.selectedServices;  
   
  ExistValue.forEach(element => {
    formValues.forEach((data:any)=>{
  if (element.sub_service == data.sub_service  && 
        element.enabled_yn  != data.enabled_yn && !breakLoop ) {
          this.formChanged = true;
           breakLoop = true;
         }else{
           if (!breakLoop) {
            
            this.formChanged = false;
           }
         }
    })
  });    
}

}
