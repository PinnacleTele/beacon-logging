import { Component, OnInit } from '@angular/core';
import { ControlContainer, Validators } from '@angular/forms';
import { Subscription } from 'rxjs';
import { EditAccountService } from 'src/app/account/edit-account/edit-account.service';
import { SubServices } from 'src/app/account/shared/model/service-model';

@Component({
  selector: 'app-my-services',
  templateUrl: './my-services.component.html',
  styleUrls: ['./my-services.component.css']
})
export class MyServicesComponent implements OnInit {
    
    smppDiscarded = false;
    servicesFormGroup : any;
    subServices : SubServices[] = [];
    selectedServices :  SubServices[] = [];
    responce:{message:string,statusCode:number};
    status: string;
    popup = false;
    showServices = false;
    showSMPP = false;
    showSlider = false;
    openClearModal = false;
    formChanged = false;
    subscription : Subscription;
    origSMPP = "";
    
    constructor(private parentControl: ControlContainer,
       private editAcctService : EditAccountService){}
  
    ngOnInit(): void {
      this.servicesFormGroup = this.parentControl.control;
      this.setValues();
      
      this.servicesFormGroup.controls.charset1.setValue(this.editAcctService.smppCharset); 
      this.origSMPP = this.editAcctService.smppCharset;
      this.subscription = this.editAcctService
              .notifyServicesCompleted()
              .subscribe((type : string) => {
                
                    this.setValues();
                   });
    
  }
  setValues(){
    
    const emittedSubServices = this.editAcctService.cloneServices;
    
      this.selectedServices =[];
      this.subServices = [];
      emittedSubServices.forEach((ele:any) => {
      
        if(ele.enabled_yn === 1 ){
          ele.checked=true;
          this.subServices.push(ele);
        }
        if(ele.sub_service_name === "SMPP"){
          
          this.setValidatorsToFormFields();
        }
        
      });
      this.servicesFormGroup.controls.subServices.setValue(this.subServices);
      this.selectedServices = this.subServices;
    
   
    //console.log(this.selectedServices);
  }
   
  handleSMPP(){
 
    this.origSMPP = this.editAcctService.smppCharset;
    console.log('handle',this.origSMPP,this.charsetCtrl.value);
    this.smppDiscarded =false;
    if(this.origSMPP != this.charsetCtrl.value){
      this.formChanged = true;
    }else{
      this.formChanged = false;
    }
  }
  
  get charsetCtrl() {
    return this.servicesFormGroup.controls.charset1;
  }
  handleUpdate(event :string){
    if(event == "discarded"){
      this.smppDiscarded =true;
      this.servicesFormGroup.markAsPristine();
      
    }
    
    this.formChanged = false;
  }
  
  setValidatorsToFormFields(){
    this.charsetCtrl.setValidators(Validators.required);
    this.charsetCtrl.updateValueAndValidity();
  }
  
  clearValidatorsToFormFields(){
    this.charsetCtrl.clearValidators();
    this.charsetCtrl.updateValueAndValidity();
  }
  ngOnDestroy(){
    this.subscription.unsubscribe();
  }
  
  
  }
  
