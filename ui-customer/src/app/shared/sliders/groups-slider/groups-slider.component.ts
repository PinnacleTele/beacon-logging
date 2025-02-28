import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { GroupModel } from 'src/app/campaigns/model/campaign-group-model';
import { EnterExitRight, Container1, EnterExitTop } from "src/app/shared/animation";
import { SharedGroupService } from '../../service/shared-group.service';
import { ChangeDetectorRef, AfterContentChecked } from '@angular/core'
@Component({
  selector: 'app-groups-slider',
  templateUrl: './groups-slider.component.html',
  styleUrls: ['./groups-slider.component.css'],
  animations: [EnterExitRight, Container1, EnterExitTop]
})
export class GroupsSliderComponent implements OnInit, AfterContentChecked {
  @Input() groupList: any;
  @Input() singleSelect = false;
  @Input() apiError: boolean;
  @Input() noRecords: boolean;
  @Input() spinner: boolean;
  @Input() showslider = false;
  @Input() fromPage = ""
  @Output() closeSlider = new EventEmitter<null>();
  @Output() addedGroups = new EventEmitter<any>();
  groups: any;
  selectedGroups: GroupModel[] = [];
  selectAllCheckBox = false;
  searchElement = "";
  borderStyle = "";
  messageColor = "";
  overall = false;
  helperBox = false;
  
  noMatch = false;
  @Output() tryAgain = new EventEmitter();


  constructor(
    private groupSliderService: SharedGroupService,
    
    private cdr: ChangeDetectorRef

  ) { }

  ngOnInit(): void {
    

    this.groups = this.groupList;
    const selected = this.groupSliderService.getAllSelectedGroups();
    if (this.groups !== null && this.groups.length > 0 && selected != null && this.groups.length === selected.length) {
      this.overall = true;
    }
    if (this.noRecords) {
      this.groups = [];
    }
    this.groupSliderService.noMatchFound.subscribe((data) => {
      this.noMatch = data;
    })

  }
  ngOnChanges() {

    this.groups = this.groupList;
  }
  onClickTry() {
    this.tryAgain.emit();
  }

  closeGroupSlider() {
    this.searchElement = "";

    this.closeSlider.emit();
  }
  changes() {
    // this.overall = false;
  }
  searchBarClear() {
    this.searchElement = "";
    this.overall = false;
  }
  selectAll(event) {
    const val=event.target.checked;   
    this.groupSliderService.selectAllGroups(val);
    this.passCompleteData();
  }

  passCompleteData() {
    this.selectedGroups = this.groupSliderService.getAllSelectedGroups();
    this.addedGroups.emit(this.selectedGroups);
    this.validateAddedGroupArea();
  }


  addGroupById(id: string) {
    const i = this.groups.map((item: { id: any; }) => item.id).indexOf(id);

    if (!this.singleSelect) {
      // this.overall = this.groupSliderService.onClickGroupItem(i);
      this.overall = this.groupSliderService.onClickGroupItem(id);

      this.passCompleteData();
    } else {
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
  noData() {
    if (this.noMatch && (!this.apiError && !this.spinner && this.searchElement && !this.noRecords)) {
      return true
    }
    else {
      return false
    }
  }
  ngAfterContentChecked() {
    this.cdr.detectChanges()
  }

  get overAllCheck(){
    //  console.log(this.groupSliderService.filteredGroups);
    
    return this.groupSliderService.filteredGroups.every((ele)=>{return ele.checked==true})
  }
  get groupsLength(){
    return this.groupSliderService.filteredGroups.length
  }
}
