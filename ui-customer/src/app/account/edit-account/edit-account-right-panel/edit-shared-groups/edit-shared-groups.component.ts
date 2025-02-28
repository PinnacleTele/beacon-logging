import { Component, OnDestroy, OnInit } from '@angular/core';
import { ControlContainer } from '@angular/forms';
import * as _ from 'lodash';
import { Subscription } from 'rxjs';
import { AccountService } from 'src/app/account/account.service';
import { GroupModel } from 'src/app/campaigns/model/campaign-group-model';
import { SharedGroupService } from 'src/app/shared/service/shared-group.service';
import { EditAccountService } from '../../edit-account.service';


@Component({
  selector: 'app-edit-shared-groups',
  templateUrl: './edit-shared-groups.component.html',
  styleUrls: ['./edit-shared-groups.component.css'],
  providers : [SharedGroupService],
})
export class EditSharedGroupsComponent implements OnInit , OnDestroy{
  responce:{message:string,statusCode:number};
  selectedSharedGroups : GroupModel[] = [];
  status: string;
  popup = false;
  showServices = false;
  formChanged = false;
  groups : GroupModel[] = [];
  itemList: string[] = [];
  showSlider = false;
  openClearModal = false;
  SharedGroupsFormGroup : any;
  subscription : Subscription;
  origSharedGrps = [];
  
  constructor(private parentControl: ControlContainer,
    private editAccountService : EditAccountService,
    private accountService : AccountService,
    private groupSliderService : SharedGroupService
    
    ) {
  }

  ngOnInit(): void {
    
    this.SharedGroupsFormGroup = this.parentControl.control ;
    const sg = _.cloneDeep(this.editAccountService.cloneSharedGroups);
    this.selectedSharedGroups = sg;
    
    this.groupSliderService.populateSelectedGroups(this.selectedSharedGroups);
    this.getSharedGroups();
    
    this.subscription = this.editAccountService
    .notifySGCompleted()
    .subscribe((type : string) => {
      
      const sg = _.cloneDeep(this.editAccountService.cloneSharedGroups);
      
      this.selectedSharedGroups = sg;
      
      this.groupSliderService.populateSelectedGroups(this.selectedSharedGroups);
      this.getSharedGroups();
    });
      
   
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
    this.groups =[];
    this.origSharedGrps = [];
    
    this.accountService.getGroupsToShare().subscribe(
        (res: any) => {
        res.forEach((ele:any) => {
          
          this.showServices = true;
          ele.checked=false;
         
   
          this.groups.push(ele);
          });
          this.groupSliderService.groupList = this.groups;
          this.groupSliderService.loopGroupsAndMarkChecked(this.selectedSharedGroups);
          this.groups = this.groupSliderService.groupList;
          this.setGroupsVal(this.groups);
          this.selectedSharedGroups.forEach(ele => {
            this.origSharedGrps.push(ele.id)
           });
        },
        (error: any) => {
        
//        let err =this.accountService.badError
          this.responce = error;
          this.status=error.message
          this.popup=true;
          
        }

    );
    console.log(this.groups)
    
    
  }
  closeSlider(){
    this.showSlider = false;
  }

  addedGroups(event : any){
    this.selectedSharedGroups = event;
    
    //console.log(this.selectedSharedGroups)
    this.setSharedGroupsValue(event);
    this.setGroupsVal(this.groups);
    this.formChanged = this.checkForFormChanges();
  }
  onClickRemoveAll(){
    
    this.selectedSharedGroups = [];
    this.groups.forEach((el) => {
      el.checked = false;
    });
    this.groupSliderService.removeAllSelected();
    this.groupSliderService.groupList = this.groups;
    this.setSharedGroupsValue(this.selectedSharedGroups);
    this.setGroupsVal(this.groups);
    this.formChanged = this.checkForFormChanges();
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
      
    }
    this.setSharedGroupsValue(this.selectedSharedGroups);
    this.setGroupsVal(this.groups);
   
      this.formChanged = this.checkForFormChanges();
    
    
  }


  checkForFormChanges(){
     
    // on removeAll()
    if(this.selectedSharedGroups.length == 0 && this.selectedSharedGroups.length == this.origSharedGrps.length){
      return false;
    }
    
    //on addedGroups()
    if(this.selectedSharedGroups.length != this.origSharedGrps.length){
      return true;
    }else{
      let selectedGrps = []; 
      this.selectedSharedGroups.forEach(ele => {
        selectedGrps.push(ele.id)
       });
      let originalSorted = this.origSharedGrps.sort();
      let selectedSorted = selectedGrps.sort();
      if(originalSorted.toString() != selectedSorted.toString()){
        return true
       }else{
         return false
       }
    }

    
    
  }

  setGroupsVal(val : GroupModel[]){
    this.SharedGroupsFormGroup.controls.groups.setValue(val);
  }
  setSharedGroupsValue(val : any){
    this.formChanged = true;
    this.SharedGroupsFormGroup.controls.sharedGroups.setValue(val);
  }
  handleDiscarded(){
    this.formChanged = false;
  }
  handleSaved(){
    this.formChanged = false;
    
  }
   
  get sharedGroupsCtrl() {
    return this.SharedGroupsFormGroup.controls.sharedGroups;
  }
  
  ngOnDestroy(){
    this.subscription.unsubscribe();
  }

}
