import { Component, OnDestroy, OnInit } from '@angular/core';
import { ControlContainer, FormGroup } from '@angular/forms';
import * as _ from 'lodash';
import { Subscription } from 'rxjs';
import { AccountService } from 'src/app/account/account.service';
import { SetValidationsService } from 'src/app/account/set-validations.service';
import { TemplateGroup } from 'src/app/account/shared/model/template-group-model';
import { TGroupSliderService } from 'src/app/shared/service/template-group-slider.service';
import { EditAccountService } from '../../edit-account.service';

@Component({
  selector: 'app-edit-dlt-card',
  templateUrl: './edit-dlt-card.component.html',
  styleUrls: ['./edit-dlt-card.component.css']
})
export class EditDltCardComponent implements OnInit, OnDestroy {
  selectedAcctType : string = '';
  showSlider = false;
  showAssignSlider = false;
  allocatedGroups : TemplateGroup[] = [];
  assignedGroups : TemplateGroup[] = [];

  groups : TemplateGroup[] = [];
  openClearModal = false;
  DLTCardFormGroup : any;
  subscription : Subscription;
  formChanged = false;
  showAssignSection = true;

  //dirtychk changes
  originalAllocGrps = [];
  origAssignedGrpId :any;

  constructor(private parentControl: ControlContainer,
    private accountService : AccountService,
    private groupSliderService : TGroupSliderService,
    private editAcctService : EditAccountService,
    private setValidatorSvc : SetValidationsService
    ) {
  }

  ngOnInit(): void {
    
    this.DLTCardFormGroup = this.parentControl.control ;
    this.selectedAcctType = this.DLTCardFormGroup.controls.userType.value;  
    this.setValues();
    this.setValidatorSvc.setValidatorsToDLTFields(this.DLTCardFormGroup,this.selectedAcctType);
    
    this.subscription = this.editAcctService
            .notifyDLTCompleted()
            .subscribe((type : string) => {
              
              this.selectedAcctType = this.DLTCardFormGroup.controls.userType.value;
              
                  this.setValues();
                  
                  this.setValidatorSvc.setValidatorsToDLTFields(this.DLTCardFormGroup,this.selectedAcctType);
           });
            
  }

  setValues(){
    const emittedAlloc = _.cloneDeep(this.editAcctService.cloneDLTCard);
    this.allocatedGroups = emittedAlloc;
    const emittedAssign = _.cloneDeep(this.editAcctService.cloneDLTAssigned);
    
    this.assignedGroups = emittedAssign;
    this.DLTCardFormGroup.controls.allocatedTG.setValue(emittedAlloc);
    this.DLTCardFormGroup.controls.assignedTG.setValue(emittedAssign);
    this.groupSliderService.populateSelectedGroups(this.allocatedGroups);

    //added for CU 209
    this.origAssignedGrpId = this.assignedGroups[0].template_group_id;
    this.groupSliderService.assignedGroupId = this.assignedGroups[0].template_group_id;
    this.loadAllocatedGroups();
       
  }
  
  loadAllocatedGroups(){
    this.originalAllocGrps =[];
    this.accountService.getGroupsToAllocate().subscribe((data)=>{
      this.groups =[];
      data.forEach((ele)=>{
            ele.checked=false;
            this.groups.push(ele);
            
        });
        
        this.groupSliderService.groupList = this.groups;
        
        this.setTGValue(this.groups);
        
        this.groupSliderService.loopGroupsAndMarkChecked(this.allocatedGroups);
        if(+this.selectedAcctType === 2){
      
          this.allocatedGroups = this.groups;
          this.setAllocatedTGValue(this.allocatedGroups);
        }
      });

      //dirtychk changes
      this.allocatedGroups.forEach(ele => {
        this.originalAllocGrps.push(ele.template_group_id)
       });
      
   
    
  }
  onClickAllocate(){
    
    this.showSlider = true;
  }
  
  closeAssignSlider(){
    this.showAssignSlider = false;
  }
  onClickAssign(){
    
    this.showAssignSlider = true;
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
    this.formChanged = true;
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
    //dirtychk changes
    this.checkForFormChanges();
  }
  emptyAssignedValue(){
    this.assignedGroups = [];
    this.setAssignedTGValue(this.assignedGroups);
  }
  
  onClickRemoveAll(){
    this.formChanged = true; 
    this.allocatedGroups = [];
    this.assignedGroups = [];
    this.showAssignSection = false;
    this.groups.forEach((el) => {
      el.checked = false;
    });
    this.groupSliderService.groupList = this.groups;
    this.setTGValue(this.groups);
    this.groupSliderService.removeAllSelected();
    this.setAllocatedTGValue(this.allocatedGroups);
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
        this.formChanged = true;
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
    this.checkForFormChanges();//dirtychk changes
  }

  handleAssignedGroups(event : TemplateGroup[]){
    
    this.assignedGroups = event;
    this.setAssignedTGValue(this.assignedGroups);
    
    //dirtychk changes
    if(this.assignedGroups[0].template_group_id != this.origAssignedGrpId){
      this.formChanged = true;
    }else{
      this.formChanged = false;
    }
    
  }
  onRemoveAssigned(index : number){
    this.formChanged =true;//dirtychk changes
    this.assignedGroups = [];
    this.setAssignedTGValue("");
    this.assignedCtrl.markAsTouched();
  }

  setAssignedTGValue(val: any){
    this.DLTCardFormGroup.controls.assignedTG.setValue(val);
    //if else added for CU-209
    if(val === null || val ==="" ||  val == undefined || val.length == 0){
      
        this.groupSliderService.assignedGroupId  = 0;
    }else {
      
      this.groupSliderService.assignedGroupId  = val[0].template_group_id;
    }
  
  }
  setAllocatedTGValue(val : any){
     this.DLTCardFormGroup.controls.allocatedTG.setValue(val);
      
  }
 
  setTGValue(val : any){
    this.DLTCardFormGroup.controls.templateGroups.setValue(val);
 }
 handleUpdate(){
   //console.log("handle update called")
   this.formChanged = false;
 }
 checkForFormChanges(){
 
      let selectedAllocatedGrps = [];
      this.allocatedGroups.forEach(ele => {
        selectedAllocatedGrps.push(ele.template_group_id)
      });
      //console.log(selectedAllocatedGrps)
      if(selectedAllocatedGrps.length != this.originalAllocGrps.length){
        
        this.formChanged = true;
        return true;
      }else{
          let originalSprted = this.originalAllocGrps.sort();
          let selectedSorted = selectedAllocatedGrps.sort();
          
          if(originalSprted.toString() != selectedSorted.toString()){
            
            this.formChanged = true;
          }else{
            
            this.formChanged = false;
            this.DLTCardFormGroup.markAsUntouched();
          }
    }
 }
  
 get grpListCtrl(){
  return this.DLTCardFormGroup.controls.templateGroups;
}
  get allocatedCtrl(){
    return this.DLTCardFormGroup.controls.allocatedTG;
  }
  get assignedCtrl(){
    return this.DLTCardFormGroup.controls.assignedTG;
  }
  ngOnDestroy(){
    if(this.subscription){
      this.subscription.unsubscribe();
    }
    
  }

}