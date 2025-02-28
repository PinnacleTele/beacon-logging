import { Injectable } from '@angular/core';
import { TemplateGroup } from 'src/app/account/shared/model/template-group-model';


@Injectable({
  providedIn: 'root'
})
export class GroupSliderService {
  groupList: TemplateGroup[] = [];
  selectedGroups: TemplateGroup[] = [];
  constructor() { }
  populateGroupList(gList: any) {
    this.groupList = gList;
  }
  getAllGroups() {
    return this.groupList;
  }
  //  selectAllGroups(gList : any){
  //this.selectedGroups = gList;
  selectAllGroups() {
    this.selectedGroups = []
    this.groupList.forEach((element: TemplateGroup) => {
      this.selectedGroups.push(element);
      element.checked = true;
    });
  }
  onClickGroupItem(i: number) {
    let selectedGroupsSvc = this.getAllSelectedGroups();

    if (selectedGroupsSvc.length != 0) {
      this.selectedGroups = selectedGroupsSvc;
    }
    if (!this.selectedGroups.includes(this.groupList[i])) {

      this.markGroupChecked(i, true);
      this.selectedGroups.push(this.groupList[i]);

      this.populateSelectedGroups(this.selectedGroups);
      if (this.selectedGroups.length === this.groupList.length) {
        return true;
      } else { return false; }
    } else {// groupItem clicked is in the text area ,already selected

      const y = this.selectedGroups.indexOf(this.groupList[i]);
      this.markGroupChecked(i, false);
      this.selectedGroups.splice(y, 1);
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



}
