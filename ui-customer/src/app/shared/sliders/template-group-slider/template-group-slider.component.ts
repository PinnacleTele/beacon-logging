import { AfterContentChecked, ChangeDetectorRef, Component, EventEmitter, Input, OnChanges, OnInit, Output } from '@angular/core';
import { single } from 'rxjs/operators';
import { TemplateGroup } from 'src/app/account/shared/model/template-group-model';
import { TemplateCampaignService } from 'src/app/campaigns/campaign-template/service/template-campaign.service';
import { EnterExitRight, Container1 } from "src/app/shared/animation";
import { TGroupSliderService } from '../../service/template-group-slider.service';
@Component({
  selector: 'app-template-group-slider',
  templateUrl: './template-group-slider.component.html',
  styleUrls: ['./template-group-slider.component.css'],
  animations: [EnterExitRight, Container1]
})
export class TemplateGroupSliderComponent implements OnInit, OnChanges, AfterContentChecked {
  @Input() groupList: any;
  @Input() singleSelect = false;
  @Input() apiError: boolean;
  @Input() noRecords: boolean;
  @Input() spinner: boolean;
  @Output() closeSlider = new EventEmitter<null>();
  @Output() addedGroups = new EventEmitter<any>();
  groups: TemplateGroup[];
  selectedGroups: TemplateGroup[] = [];
  selectAllCheckBox = false;
  searchElement = "";
  borderStyle = "";
  messageColor = "";
  overall = false;
  alreadyAssignedGroupId: number;
  @Output() tryAgain = new EventEmitter();
  noDataFound = false;
  constructor(
    private groupSliderService: TGroupSliderService, private tempService: TemplateCampaignService, private cdr: ChangeDetectorRef) { }
  ngAfterContentChecked(): void {
    this.cdr.detectChanges();
  }

  ngOnInit(): void {
    this.groups = this.groupList;

    const selected = this.groupSliderService.getAllSelectedGroups();
    if (this.groups !== null && this.groups.length > 0 && selected != null && this.groups.length === selected.length) {
      this.overall = true;
    }
    if (this.noRecords) {
      this.groups = [];
    }
    if (this.singleSelect && this.groupSliderService.assignedGroupId != undefined) {
      this.alreadyAssignedGroupId = this.groupSliderService.assignedGroupId;
    }
    this.tempService.noMatch.subscribe((data) => {
      this.noDataFound = data
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
    //console.log("pass Data ",this.selectedGroups);
    this.addedGroups.emit(this.selectedGroups);
    this.validateAddedGroupArea();
  }


  addGroupById(id: number) {
    const i = this.groups.map((item: any) => item.template_group_id).indexOf(id);

    if (!this.singleSelect) {//with checkboxes in slider
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
    if (this.noDataFound && !this.apiError && !this.spinner && !this.noRecords) {
      return true;
    }
    else {
      return false;
    }
  }
  get overAllCheck(){
    //  console.log(this.groupSliderService.filteredGroups);
    
     return this.groupSliderService.filterdGroups.every((ele)=>{return ele.checked==true})
  }
  get groupsLength(){
    return  this.groupSliderService.filterdGroups.length
  }
}
