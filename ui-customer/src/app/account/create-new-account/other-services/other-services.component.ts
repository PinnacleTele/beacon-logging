import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { ControlContainer, Validators } from '@angular/forms';
import { Subscription } from 'rxjs';
import { GroupModel } from 'src/app/campaigns/model/campaign-group-model';
import { CommonService } from 'src/app/shared/commonservice';
import { SharedGroupService } from 'src/app/shared/service/shared-group.service';
import { ACCT_CONSTANTS } from '../../account.constants';
import { AccountService } from '../../account.service';
import { SubServices } from '../../shared/model/service-model';

@Component({
  selector: 'app-other-services',
  templateUrl: './other-services.component.html',
  providers : [SharedGroupService],
  styleUrls: ['./other-services.component.css']
})
export class OtherServicesComponent implements OnInit,OnDestroy {
  page6FormGroup : any;
  subServices : SubServices[] = [];
  selectedServices :  SubServices[] = [];
  prevSubServices :  SubServices[] = [];
  selectedSharedGroups : GroupModel[] = [];
  showServices = false;
  groups : GroupModel[] = [];
  subscription : Subscription;
  showSlider = false;
  openClearModal = false;
  showSMPP = false;
  responce:{message:string,statusCode:number};
  status: string;
  popup = false;
  spinner = false;
  svcSpinner = false;
  noSharedGroups = ACCT_CONSTANTS.NO_SHARED_GROUPS;
  noRecords = false;
  apiError = false;
  clusterType:any;
  constructor(
    private accountService : AccountService,
    private groupSliderService : SharedGroupService,
    private parentControl: ControlContainer,
    private commonService:CommonService) {
  }

  ngOnInit(): void {
    // cluster type change CU-384
    const userData:any=this.commonService.tokenDecoder();
      
        let clusterCaseChange = userData.cluster.toLowerCase();

        this.clusterType = clusterCaseChange;
    this.page6FormGroup = this.parentControl.control;
    const fromSvc : GroupModel[] = this.groupSliderService.getAllSelectedGroups();
   //bug let prevGroups = this.page6FormGroup.controls.groups.value;
   let prevGroups = this.page6FormGroup.controls.groups.value;
    this.selectedSharedGroups = this.page6FormGroup.controls.sharedGroups.value;
   this.prevSubServices = this.subServicesCtrl.value as Array<SubServices>;
   const prevCharSet = this.charsetCtrl.value;
   
   
    this.accountService.markAsUntouched(this.page6FormGroup);

if(this.prevSubServices && this.prevSubServices.length > 0 ){
  
this.selectedServices = this.prevSubServices;
this.subServices = this.accountService.subServices;
this.selectedServices.forEach((ele:any) => {
  if(ele.sub_service === 'smpp' ){
    this.showSMPP = true;
    this.setValidatorsToFormFields();
  }
});

}else{
  this.getSubService();
}


   
    this.setServicesValidators();
    
    if(prevGroups != null && prevGroups.length > 0){
      
      this.groups = prevGroups;
      this.groupSliderService.groupList = this.groups;
      //back and forth screen trversing without selecting any group populated null, handled with if below 
      if(this.selectedSharedGroups != null && this.selectedSharedGroups.length > 0){
        this.groupSliderService.populateSelectedGroups(this.selectedSharedGroups);
      }else{
        this.groupSliderService.populateSelectedGroups([]);
      }
     
      this.setGroupsValue(this.groups);
      

    }else{
      
      this.getSharedGroups();
    }
  
  }

  ngAfterViewInit() {
  
    
    
  }
  tryAgain(){
    this.popup = false;
    this.getSharedGroups();
    
  }

  getSubService(){
    this.svcSpinner = true;
    this.apiError = false;
    this.subscription = this.accountService.getSubServices().subscribe(
        (res: any) => {
        res.forEach((ele:any) => {
          this.showServices = true;
          this.svcSpinner = false;
          if(ele.sub_service === 'cm' || ele.enabled_yn === 1 ){
            this.selectedServices.push(ele);
            this.setSubServicesVal(this.selectedServices);
            
            ele.checked=true;
          }else{
            ele.checked=false;
          }
          this.subServices.push(ele);
          });
          this.accountService.subServices = this.subServices;
          
        },
        (error: HttpErrorResponse) => {
        
        let err =this.accountService.badError
        this.svcSpinner = false;
          this.responce = err;
          this.status=error.message
          this.apiError=true;
        }
    );
  }

  openModal() {
    this.openClearModal = true;
  }

clearModalResponse(response: string) {
    
    if (response === "clear") {
       this.onClickRemoveAll();
       this.openClearModal = false;
    }
    if (response === "close") {
        this.openClearModal = false;
    }
}
 
  getSharedGroups(){
    this.spinner = true;
    this.groups =[];
    this.accountService.getGroupsToShare().subscribe(
        (res: any) => {
        res.forEach((ele:any) => {
          this.showServices = true;
          ele.checked=false;
          this.groups.push(ele);
          });
          this.spinner = false;
         
         if(this.groups.length === 0){
          this.noRecords = true;
          
        }
        
        this.setGroupsValue(this.groups);
    this.groupSliderService.groupList = this.groups;
        },
        (error: HttpErrorResponse) => {
        
        this.spinner = false;
        let err =this.accountService.badError
          this.responce = err;
          this.status=error.message
          this.popup=true;
        });
    
   
  }

  onClickService(index : number){
    const svcName = this.subServices[index].sub_service.toLowerCase();
    if(svcName !== "cm"){
      this.subServices[index].checked = !(this.subServices[index].checked);
      if(this.subServices[index].checked){
        this.selectedServices.push(this.subServices[index]);
        
        if(svcName === "smpp"){
          this.setValidatorsToFormFields();
          this.showSMPP = true;
        }
      }else{
        for (var i = this.selectedServices.length - 1; i >= 0; i--) {
          if (svcName === this.selectedServices[i].sub_service) { 
            this.selectedServices.splice(i,1);
          }
        }
        if(svcName === "smpp"){
          //this.charsetCtrl.setValue('');
          this.clearValidatorsToFormFields();
          this.showSMPP = false;
        }
      }
    }else{
      this.subServices[index].checked = true;
    }
    this.setSubServicesVal(this.selectedServices);
  }

  closeSlider(){
    this.showSlider = false;
  }

  addedGroups(event : any){
    this.selectedSharedGroups = event;
    this.setSharedGroupsValue(event);
  }
  onClickRemoveAll(){
    
    this.selectedSharedGroups = [];
    this.groups.forEach((el) => {
      el.checked = false;
    });
    this.groupSliderService.groupList = this.groups;
    this.groupSliderService.populateSelectedGroups(this.selectedSharedGroups);
        
    this.setSharedGroupsValue(this.selectedSharedGroups);
  }
  onClickChoose(){
    this.showSlider = true;
  }
  removeGroup(i : number){
    const deleted : GroupModel = this.selectedSharedGroups.splice(i, 1)[0];
    const index = this.groups.findIndex(x => x.id === deleted.id);

    if (index !== undefined){
      
      this.groups[index].checked = false;
      this.groupSliderService.populateSelectedGroups(this.selectedSharedGroups);
      this.setSharedGroupsValue(this.selectedSharedGroups);
      this.setGroupsValue(this.groups);
    }
  }

  setSubServicesVal(val : SubServices[]){
    this.page6FormGroup.controls.subServices.setValue(val);
  }
  setSharedGroupsValue(val : any){
    this.page6FormGroup.controls.sharedGroups.setValue(val);
  }
  setGroupsValue(val : any){
    this.page6FormGroup.controls.groups.setValue(val);
 }
  
  get subServicesCtrl() {
    return this.page6FormGroup.controls.subServices;
  }
  get charsetCtrl() {
    return this.page6FormGroup.controls.charset1;
  }
 
  get sharedGroupsCtrl() {
    return this.page6FormGroup.controls.sharedGroups;
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
    if(this.subscription){
      this.subscription.unsubscribe();
    }
  }

}
  