import { Injectable } from '@angular/core';
import { TemplateGroup } from 'src/app/account/shared/model/template-group-model';


@Injectable({
  providedIn: 'root'
})
export class TGroupSliderService {
  groupList : TemplateGroup[] = [];
  selectedGroups:TemplateGroup[] = [];
  filterdGroups:TemplateGroup[] = [];
  assignedGroupId :number;
  constructor() { }
  populateGroupList(gList : any){
    this.groupList = gList;
  }
  getAllGroups(){
    return this.groupList;
  }
//  selectAllGroups(gList : any){
    //this.selectedGroups = gList;
  selectAllGroups(check : boolean){
    if(check){
      // this.selectedGroups =[];
      // this.groupList.forEach((element: TemplateGroup) => {
      //   this.selectedGroups.push(element);
      //   element.checked = true;
      // });
      this.filterdGroups.forEach((element: TemplateGroup) => {
        if(!element.checked){
          this.selectedGroups.push(element);
          element.checked = true;
        }
      });
    }else{
      this.filterdGroups.forEach((element: TemplateGroup) => {
        const i= this.selectedGroups.findIndex( e=>{return element.template_group_id ==e.template_group_id })
        console.log(i);
        if(i!=-1){
          
          this.selectedGroups.splice(i,1);
        }
         element.checked = false;
       });
    
    }
   
  }
  onClickGroupItem(id : number){
    
    let indexSelectedGroups = -1;
    if(this.selectedGroups !== null && this.selectedGroups.length > 0){
      indexSelectedGroups = this.selectedGroups.map((item) => item.template_group_id).indexOf(id);
    }
    
    
    const i = this.groupList.map((item) => item.template_group_id).indexOf(id);
    
    if ( indexSelectedGroups === -1) {
        this.markGroupChecked(i,true);
        this.selectedGroups.push(this.groupList[i]);

        this.populateSelectedGroups(this.selectedGroups);
        if(this.selectedGroups.length === this.groupList.length){
          return true;
        }else{ return false;}
    }else {// groupItem clicked is in the text area ,already selected
      
      this.markGroupChecked(i,false);
      this.selectedGroups.splice(indexSelectedGroups, 1);
      this.populateSelectedGroups(this.selectedGroups);
      return false;
    }
  }
  removeAllSelected(){
    this.selectedGroups.splice(0, this.selectedGroups.length);
    
  }
  markGroupChecked(index : number, chk : boolean){
    this.groupList[index].checked = chk;
  }
  getAllSelectedGroups(){
    return this.selectedGroups 
  }
  populateSelectedGroups(gList : any){
    
    this.selectedGroups = gList;
  }
  removeOneGroup( y : number ,i : number){
    this.selectedGroups.splice(y, 1);
    
  }
  loopGroupsAndMarkChecked(selectedGrps : TemplateGroup[]){
    selectedGrps.forEach((el : TemplateGroup) => {
      const i = this.groupList.map((item) => item.template_group_id).indexOf(el.template_group_id);
      if(i !== -1){
        this.groupList[i].checked = true;
      }
    });
  }
  


}
