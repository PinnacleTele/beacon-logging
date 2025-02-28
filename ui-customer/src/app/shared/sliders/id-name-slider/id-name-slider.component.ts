import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

import { TemplateGroup } from 'src/app/account/shared/model/template-group-model';
import { Container1, EnterExitRight } from '../../animation';
import { GroupSliderService } from '../../service/group-slider.service';
@Component({
  selector: 'app-id-name-slider',
  templateUrl: './id-name-slider.component.html',
  styleUrls: ['./id-name-slider.component.css'],
  animations: [EnterExitRight, Container1]
})
export class IdNameSliderComponent implements OnInit {
@Input() groupList : any;
@Input() singleSelect = false;
@Output() closeSlider = new EventEmitter<null>();
@Output() addedGroups = new EventEmitter<any>();
groups : TemplateGroup[];
selectedGroups:TemplateGroup[] = [];
selectAllCheckBox = false;
searchElement = "";
borderStyle = "";
messageColor = "";
overall = false;

  constructor(
    private groupSliderService : GroupSliderService) { }

  ngOnInit(): void {
    this.groups = this.groupList;
  }

  closeGroupSlider() {
    this.searchElement = "";
    
    this.closeSlider.emit();
  }
  changes() {
    this.overall = false;
  }
  searchBarClear() {
    this.searchElement = "";
    this.overall=false;
  }
  selectAll() {
    this.groupSliderService.selectAllGroups();
    this.passCompleteData();
  }

passCompleteData() {
  this.selectedGroups = this.groupSliderService.getAllSelectedGroups();
  this.addedGroups.emit(this.selectedGroups);
  this.validateAddedGroupArea();
}


addGroupById(id: number) {
  const i = this.groups.map((item: any) => item.template_group_id).indexOf(id);
  if(!this.singleSelect){
    this.overall = this.groupSliderService.onClickGroupItem(i);
    this.passCompleteData();
  }else{
    this.selectedGroups = [];
    this.selectedGroups.push(this.groups[i]);
    this.addedGroups.emit(this.selectedGroups);
    this.closeGroupSlider();
  }
}

validateAddedGroupArea() {
  if (this.selectedGroups.length > 0) {
      this.borderStyle = "border-gray-300";
      this.messageColor = "text-gray-500";
  } else {
      this.borderStyle = "border-red-300";
      this.messageColor = "text-red-600";
  }
 // console.log("validation");
}


}
