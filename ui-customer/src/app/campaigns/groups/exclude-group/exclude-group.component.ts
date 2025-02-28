import { HttpErrorResponse } from "@angular/common/http";
import { Component, EventEmitter, OnInit, Output } from "@angular/core";
import { ControlContainer } from "@angular/forms";
import { EnterExitRight, Container1 } from "src/app/shared/animation";
import { SharedGroupService } from "src/app/shared/service/shared-group.service";
import { CampaignsService } from "../../campaigns.service";
import { GroupModel } from "../../model/campaign-group-model";
import { GroupsCampaignService } from "../groups-campaign.service";

@Component({
  selector: "app-exclude-group",
  templateUrl: "./exclude-group.component.html",
  styleUrls: ["./exclude-group.component.css"],
  providers: [SharedGroupService],
  animations: [EnterExitRight, Container1]
})
export class ExcludeGroupComponent implements OnInit {


  public campaignGroupFormGroup: any;

  showSlider = false;

  borderStyle = "";
  messageColor = "";
  searchElement = "";
  openClearModal = false;
  noofContactsSelected = 0;
  responce: { message: string, statusCode: number };
  selectedSharedGroups: GroupModel[] = [];
  status: string;
  popup = false;
  noOfGroups = 0;
  spinner = false;
  groups: GroupModel[] = []
  noRecords = false;

  @Output() excludedGroups = new EventEmitter<GroupModel[]>();

  constructor(
    public campaignService: CampaignsService,
    public controlContainer: ControlContainer,
    private groupSliderService: SharedGroupService,
    public groupCmpaingService: GroupsCampaignService
  ) { }


  ngOnInit(): void {
    //this.campaignService.selectedGroups = this.selectedGroups;
    this.campaignGroupFormGroup = this.controlContainer.control;
    const formVal = this.campaignGroupFormGroup.controls.excludedGroups.value;
    if (formVal !== null) {
      this.selectedSharedGroups = this.campaignGroupFormGroup.controls.excludedGroups.value;
    }

    // this.getGroups();
  }

  getGroups() {

    this.groups = [];
    this.spinner = true;
    this.groupCmpaingService.findAllGroups("exclude").subscribe((data) => {


      data.forEach((ele) => {
        ele.checked = false;
        if (this.selectedSharedGroups.length > 0) {
          this.selectedSharedGroups.forEach(element => {
            if (element.id == ele.id) {
              ele.checked = true;
            }
          });
        }
        this.groups.push(ele);
      });
      this.noRecords = false;
      this.popup = false;
      if (this.groups.length === 0) {
        this.noRecords = true;

      }
      this.spinner = false;
      this.groupSliderService.groupList = this.groups;

    },
      (error: HttpErrorResponse) => {
        this.spinner = false;
        let err = this.groupCmpaingService.badError
        this.responce = err;
        this.status = error.message
        this.popup = true;
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


  closeSlider() {
    this.showSlider = false;
  }
  passCompleteData() {
    this.excludedGroups.emit(this.selectedSharedGroups);
    this.infoOfSelected();
    this.validateAddedGroupArea();
  }

  infoOfSelected() {
    this.noOfGroups = this.selectedSharedGroups.length;
    let i = 0;
    this.selectedSharedGroups.forEach((element) => {
      i += element.total;
    });
    this.noofContactsSelected = i;
    i = 0;
  }
  emittedGroupsFromSlider(event: any) {

    this.selectedSharedGroups = event;

    this.passCompleteData();
  }
  onClickRemoveAll() {

    this.selectedSharedGroups = [];
    this.groups.forEach((el) => {
      el.checked = false;
    });
    this.groupSliderService.groupList = this.groups;
    this.groupSliderService.populateSelectedGroups(this.selectedSharedGroups);
    this.passCompleteData();
  }
  onClickAdd() {
    this.getGroups();
    this.showSlider = true;
  }

  validateAddedGroupArea() {

    if (this.selectedSharedGroups.length > 0) {
      this.borderStyle = "border-gray-300";
      this.messageColor = "text-gray-500";
    } else {
      this.borderStyle = "border-red-300";
      this.messageColor = "text-red-600";
    }
  }
  removeGroup(i: number) {
    const deleted: GroupModel = this.selectedSharedGroups.splice(i, 1)[0];
    const index = this.groups.findIndex(x => x.id === deleted.id);

    if (index !== undefined) {

      this.groups[index].checked = false;
      this.groupSliderService.populateSelectedGroups(this.selectedSharedGroups);

    }
    this.passCompleteData();
  }
  tryAgain() {
    this.popup = false;
    this.getGroups();

  }


}
