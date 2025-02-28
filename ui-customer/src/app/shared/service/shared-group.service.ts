import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { Group, GroupModel } from 'src/app/campaigns/model/campaign-group-model';

export class SharedGroupService {
  groupList: GroupModel[];
  selectedGroups: GroupModel[] = [];
  filteredGroups:GroupModel[]=[]
  noMatchFound = new BehaviorSubject<boolean>(false);
  constructor() { }
  populateGroupList(gList: any) {
    this.groupList = gList;

  }
  getAllGroups() {
    return this.groupList;
  }
  selectAllGroups(check: boolean) {
    console.log(this.filteredGroups);
    
    if (check) {
      // this.selectedGroups = []
      console.log(this.filteredGroups);
      console.log(this.selectedGroups);
      
      this.filteredGroups.forEach((element: GroupModel) => {
        if(!element.checked){
          this.selectedGroups.push(element);
          element.checked = true;
        }
      });
    } else {
      console.log("else part");
      this.filteredGroups.forEach((element: GroupModel) => {
       const i= this.selectedGroups.findIndex( e=>{return element.id==e.id})
       console.log(i);
       if(i!=-1){
         
         this.selectedGroups.splice(i,1);
       }
        element.checked = false;
      });
      console.log(this.selectedGroups);
      
      // this.selectedGroups = []
      // this.groupList.forEach((element: GroupModel) => {

      //   element.checked = false;
      // });
    }
  }
  onClickGroupItem(id: string) {

    let indexSelectedGroups = -1;
    if (this.selectedGroups.length > 0) {

      indexSelectedGroups = this.selectedGroups.map((item: { id: any; }) => item.id).indexOf(id);
    }

    const i = this.groupList.map((item: { id: any; }) => item.id).indexOf(id);

    if (indexSelectedGroups === -1) {

      this.markGroupChecked(i, true);
      this.selectedGroups.push(this.groupList[i]);

      this.populateSelectedGroups(this.selectedGroups);
      if (this.selectedGroups.length === this.groupList.length) {
        return true;
      } else { return false; }
    } else {// groupItem clicked is in the text area ,already selected
      const y = this.selectedGroups.indexOf(this.groupList[i]);
      this.markGroupChecked(i, false);
      this.selectedGroups.splice(indexSelectedGroups, 1);
      this.populateSelectedGroups(this.selectedGroups);
      return false;
    }
  }
  markGroupChecked(index: number, chk: boolean) {
    this.groupList[index].checked = chk;
  }
  getAllSelectedGroups() {
    return this.selectedGroups
  }
  populateSelectedGroups(gList: any) {
    this.selectedGroups = gList;

  }
  removeOneGroup(y: number, i: number) {
    this.selectedGroups.splice(y, 1);
  }
  removeAllSelected() {
    this.selectedGroups.splice(0, this.selectedGroups.length);

  }
  loopGroupsAndMarkChecked(selectedGrps: any) {

    if (selectedGrps.length > 0) {
      selectedGrps.forEach((el: any) => {

        const i = this.groupList.map((item: any) => item.id).indexOf(el.id);

        if (i !== -1) {

          this.groupList[i].checked = true;

        }
      });
    }

  }
  //v54l2ewcsjdel119oipfl008c4d9q5a0ga99
}
