import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { ControlContainer,  FormGroup, Validators } from '@angular/forms';
import { Subscription } from 'rxjs';
import { TGroupSliderService } from 'src/app/shared/service/template-group-slider.service';
import { ACCT_CONSTANTS } from '../../account.constants';


import { AccountService } from '../../account.service';
import { SetValidationsService } from '../../set-validations.service';
import { TemplateGroup } from '../../shared/model/template-group-model';

@Component({
  selector: 'app-dlt-section',
  templateUrl: './dlt-section.component.html',
  styleUrls: ['./dlt-section.component.css']
})
export class DLTSectionComponent implements OnInit,OnDestroy {
  
  page5FormGroup : any;
  selectedAcctType : string = 'admin';
  showSlider = false;
  showAssignSlider = false;
  allocatedGroups : TemplateGroup[] = [];
  assignedGroups : TemplateGroup[] = [];
  groups : TemplateGroup[] = [];
  openClearModal = false;
  
  noDLTGroupsToAllocate = ACCT_CONSTANTS.EMPTY_DLT_GROUPS;
  response:{message:string,statusCode:number};
  status: string;
  popup = false;
  noRecords = false;
  spinner = false;
  noAlloc = false;
  showAssignSection = true;
  subscription : Subscription;

  constructor(private parentControl: ControlContainer,
    private accountService : AccountService,
    private groupSliderService : TGroupSliderService,
    private setValidatorsService : SetValidationsService,
    
    ) {
  }

  ngOnInit(): void {
    
    this.page5FormGroup = this.parentControl.control as FormGroup;
    this.selectedAcctType = this.page5FormGroup.controls.accountType.value;
    this.page5FormGroup.markAsPristine();
    let userType ="";
    if(this.selectedAcctType == "admin"){ 
      userType = "1"; 
    }else{
      userType = "2";
    }
    this.setValidatorsService.setValidatorsToDLTFields(this.page5FormGroup,userType);
    this.accountService.markAsUntouched(this.page5FormGroup);
    this.allocatedGroups = this.page5FormGroup.controls.allocatedTG.value;
    this.assignedGroups = this.page5FormGroup.controls.assignedTG.value;
    let prevGroups = this.page5FormGroup.controls.templateGroups.value;
    
    
    const fromSvc : TemplateGroup[] = this.groupSliderService.getAllSelectedGroups();
    
    if(prevGroups != null && prevGroups.length > 0){
     
      this.groups = prevGroups;
      
      this.groupSliderService.groupList = this.groups;
      this.setTGValue(this.groups);
      //back and forth screen trversing without selecting any group populated null, handled with if below 
      if(this.allocatedGroups != null && this.allocatedGroups.length > 0){
        this.groupSliderService.populateSelectedGroups(this.allocatedGroups);
      }else{
        this.groupSliderService.populateSelectedGroups([]);
      }
      
      
      
      
    }else{
      
      this.loadAllocatedGroups();
       
    }
  }
  ngAfterViewInit() {
    if(document.getElementById("btnAllocate")){
      document.getElementById("btnAllocate").focus();
    }
    
  }

  

  loadAllocatedGroups(){
    this.spinner = true;
    
    this.groups =[];
    this.subscription = this.accountService.getGroupsToAllocate().subscribe((data)=>{
      
      data.forEach((ele)=>{
            ele.checked=false;
            this.groups.push(ele);
            
        });
        this.spinner = false;
        if(this.selectedAcctType === 'user'){
          this.allocatedGroups = this.groups;
          this.setAllocatedTGValue(this.allocatedGroups);
          
        }
        if(this.groups.length === 0){
          this.noRecords = true;
          
        }
        
    },
    (error: HttpErrorResponse) => {
    
    let err =this.accountService.badError
      this.response = err;
      this.status=error.message
      this.spinner = false;
      this.popup=true;
    }
    );
    this.setTGValue(this.groups);
    this.groupSliderService.groupList = this.groups;
    
  }
  tryAgain(){
    this.popup = false;
    this.loadAllocatedGroups();
    
  }
  onClickAllocate(){
    //console.log(this.groups);
      this.showSlider = true;
      
  }
  
  closeAssignSlider(){
    this.showAssignSlider = false;
  }
  onClickAssign(){
    this.showAssignSlider = true;
    if(this.allocatedGroups.length === 0){
      this.noAlloc = true;
    }else{
      this.noAlloc = false;
    }
  }
  closeSlider(){
    this.showSlider = false;
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


  addedGroups(event : any){
    
    this.allocatedGroups = event;
    this.setAllocatedTGValue(event);
    this.setTGValue(this.groups);
    this.showAssignSection = true;
    if(this.allocatedGroups.length == 0){
      
      this.emptyAssignedValue();
      this.showAssignSection = false;
    }
    if(this.assignedGroups != null && this.assignedGroups.length > 0){
      const i = this.allocatedGroups.map((item) => item.template_group_id).
      indexOf(this.assignedGroups[0].template_group_id);
      if(i == -1)  {
        this.emptyAssignedValue();
      }
    }
  }
  emptyAssignedValue(){
    this.assignedGroups = [];
    this.setAssignedTGValue("");
  }
  
  onClickRemoveAll(){
    this.allocatedGroups = [];
    this.assignedGroups = [];
    this.showAssignSection = false;
    this.groups.forEach((el) => {
      el.checked = false;
    });
    this.groupSliderService.groupList = this.groups;
    this.groupSliderService.populateSelectedGroups(this.allocatedGroups);
    this.setAllocatedTGValue("");
    this.setTGValue("");
    this.emptyAssignedValue();
    this.allocatedCtrl.markAsTouched();
  }
  removeGroup(i : number){
    const deleted : TemplateGroup = this.allocatedGroups.splice(i, 1)[0];
    const index = this.groups.findIndex(x => x.template_group_id === deleted.template_group_id);

    if (index !== undefined){
      
      this.groups[index].checked = false;
      this.groupSliderService.markGroupChecked(index, false);
      this.groupSliderService.populateSelectedGroups(this.allocatedGroups);
      
      if(this.allocatedGroups.length === 0){
        this.showAssignSection = false;
        
        this.allocatedCtrl.markAsTouched();
      }
      if(this.assignedGroups != null && this.assignedGroups.length > 0){
       
        if(this.assignedGroups[0].template_group_name === this.groups[index].template_group_name){
          this.emptyAssignedValue();
          this.showAssignSection = false;
        }
      }
    }
    this.setAllocatedTGValue(this.allocatedGroups);
    this.setTGValue(this.groups);
  }

  handleAssignedGroups(event : any){
    this.assignedGroups = event;
    this.setAssignedTGValue(this.assignedGroups);
    
  }
  onRemoveAssigned(index : number){
    this.assignedGroups = [];
    this.setAssignedTGValue("");
    this.assignedCtrl.markAsTouched();
  }

  setAssignedTGValue(val: any){
    this.page5FormGroup.controls.assignedTG.setValue(val);
    //if else added for CU-209
    if(val === null || val ==="" || val == [] ){
      
      this.groupSliderService.assignedGroupId  = 0;
    }else{
      
      this.groupSliderService.assignedGroupId  = val[0].template_group_id;
    }
  }
  setAllocatedTGValue(val : any){
     this.page5FormGroup.controls.allocatedTG.setValue(val);
  }
  setTGValue(val : any){
    this.page5FormGroup.controls.templateGroups.setValue(val);
 }
  
  
  get allocatedCtrl(){
    return this.page5FormGroup.controls.allocatedTG;
  }
  get assignedCtrl(){
    return this.page5FormGroup.controls.assignedTG;
  }

  ngOnDestroy(){
    if(this.subscription){
      this.subscription.unsubscribe();
    }
  }

  

}