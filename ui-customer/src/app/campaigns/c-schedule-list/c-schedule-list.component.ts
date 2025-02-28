import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, OnDestroy, } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { CampaignsService } from "src/app/campaigns/campaigns.service";
import { SearchService } from "../c_Helper/searchservice";
import { CONSTANTS, value } from "src/app/shared/campaigns.constants";
import { SetIntervalService } from "src/app/campaigns/c_Helper/setintervalapicalls";
import * as moment from 'moment';
import { CommonService } from "src/app/shared/commonservice";
@Component({
  selector: 'app-c-schedule-list',
  templateUrl: './c-schedule-list.component.html',
  styleUrls: ['./c-schedule-list.component.css']
})
export class CScheduleListComponent implements OnInit, OnDestroy {
  accountUserList: any[] = [];

  accountStat_List: any;

  NDHeadContent = CONSTANTS.CSNDHeadContent

  NDMessageCOntent = CONSTANTS.CSNDMessageCOntent;


  onLoading: boolean = false;

  public onloading = this.campservice.loadingS_List.subscribe((data: any) => {
    this.onLoading = data;
  });


  p: number = 1;

  pagesize: number;

  itemsPerPage: number = 10;

  perpageCount: number = 10;

  public searchText: any = "";

  statsLoading: boolean = false;

  listLoading: boolean = false;


  public statsloading = this.campservice.loadingSC_List.subscribe((data: any) => { this.statsLoading = data });

  public listloading = this.campservice.loadingC_S_List.subscribe((data: any) => { this.listLoading = data });

  hideToday: boolean;

  showDeleteModal: boolean = false;

  deleteLoading: boolean = false;

  noRecords: any;

  C_list = CONSTANTS.CAMPAINGNS;

  userZone: string = "";
  // ksksksk
  isText: true;

  isUnicode: false;

  isShowContact: false;

  removeDuplicateChecked: true;

  showPreview: false;

  showClearModal: false;

  showCancelModal: false;

  label: any;

  deleteMessage = 'Are you sure want to delete the Schedule ?';

  deleteList: any;

  openDropDown: boolean = false;
  popUp: boolean = false;
  deleteResponse: any;

  myOptions = value;



  constructor(private campservice: CampaignsService,
    private s_service: SearchService, private router: Router,
    private commonservice: CommonService,
    private interval: SetIntervalService,
  ) { }

  campaignLists: any[] = [];

  c_Today_Stats: any;

  total: number = this.campaignLists.length;

  totalRecord: number = this.campaignLists.length;

  noData: boolean = false;

  apiError: boolean = false;

  includesToday: string;

  receivedFilterData: any;

  onload: boolean = true;

  retry: boolean = false;

  ButtonDisabler: boolean = false;

  selectedLabel: any;


  ngOnInit(): void {

    this.userZone = this.commonservice.getUserData();

    this.router.events.subscribe((event: any) => {

      if (event instanceof NavigationEnd) {

        if (this.router.url.includes('/campaigns/scdetail')) {
          this.interval.setschcampSearch(this.searchText);
        }
      }
    });



    this.subscribeData();
    this.createdTs = "SCHEDULED AT  (" + this.userZone + ")"
    if (this.interval.scflag == true) {

      this.searchText = this.interval.scsearch;
    } else {
      this.searchText = "";
    }

    //  this.timeThread();
  }

  subscribeData() {
    this.campservice.campaign_S_ListStats()
      .subscribe(
        (res: any) => {
          this.c_Today_Stats = res;
        },
        (error: HttpErrorResponse) => {
          let err = this.campservice.badError
        }

      )

  }

  editSchedule(list: any) {
    this.router.navigate(['campaigns/scdetail'],
      {
        queryParams: { scheduleDetail: list.id + '@' + list.at_id }
      }
    );



  }


  filtervalue(event) {
    this.receivedFilterData = event;
    if (this.onload == true) {
      this.submitData();
    }
  }

  lableValue(event) {
    this.selectedLabel = event;
    // this.label = event;
  }

  submitData() {
    this.onload = false;
    this.listData(this.receivedFilterData);
  }

  listData(type) {
    // this.onLoad.emit(true)
    this.label = this.selectedLabel;
    this.apiError = false;
    this.campservice.campaign_S_List(type)
      .subscribe(
        (res: any) => {
          if (res) {
            res.forEach(element => {
              element.selected_ts_unix = element.scheduled_ts_unix
            });
            this.campaignLists = res;
            this.getcount();
            this.bufferTime(res);
            this.apiError = false;
            this.retry = false;
            this.noData = false;
            if (this.campaignLists.length == 0) {
              this.noData = true;
            }

          }
          //  this.getcount();
        },
        (error: HttpErrorResponse) => {
          let err = this.campservice.badError

          this.apiError = true;
        })

  }


  subscribe(event) {
    if (event == true) {
      this.apiError = false;
      this.noRecords = 1;
      this.noData = false;
    }

  }
  close($event) {
    this.openDropDown = !this.openDropDown;
    // this.selectEvent()
  }

  selectEvent(value: any) {

    this.router.navigate(['campaigns/' + value])
  }
  Retry() {
    this.retry = true;
    this.apiError = false;
    this.subscribeData();
    this.submitData();
  }
  onClick(campaign: any) {
    this.campservice.campaignDetailPageContent = campaign
    this.router.navigate(["/campaigns/cdetail"], { queryParams: { campaignId: campaign } })

  }

  toggleProfileSlider() {
    this.openDropDown = false;
  }

  dropdownclose(event) {
    this.openDropDown = event;
  }

  ReceivedpaginateValue(event) {
    this.p = event;
  }

  searchClean(event) {
    if (event.searchText == "clean") {
      if (this.interval.scflag == true) {
        this.searchText = this.interval.scsearch;
        this.interval.scflag = false;
      } else {
        this.searchText = "";
      }
    }
    if (event.includeToday == true) {
      this.includesToday = "";
      this.hideToday = false;
    } else if (event.includeToday == false) {
      this.includesToday = "does not";
      this.hideToday = false;
    } else {
      this.hideToday = true;
    }
  }

  bufferTime(value) {

    value.forEach(element => {
      element.disablebt = false;
      if (element?.scheduled_ts) {
        let currentZoneTime = moment().tz(element.selected_zone).format('LLLL');
        let sdTime = element.scheduled_ts.slice(0, 19)
        let scheduledZoneTime = moment(element.selected_dt).format('LLLL');
        if (moment(scheduledZoneTime).diff(moment(currentZoneTime), "minutes") <= CONSTANTS.EDIT_DELETE_SCHEDULE_BUFFER) {
          element.disablebt = true;
        }
        else {
          element.disablebt = false;
        }
      }
    });
  }

  onDelete(element) {
    if (element?.scheduled_ts) {
      let currentZoneTime = moment().tz(element.selected_zone).format('LLLL');
      let scheduledZoneTime = moment(element.selected_dt).format('LLLL');
      if (moment(scheduledZoneTime).diff(moment(currentZoneTime), "minutes") <= CONSTANTS.EDIT_DELETE_SCHEDULE_BUFFER) {
        element.disablebt = true;
      }
      else {
        element.disablebt = false;
        this.deleteList = element;
        this.showDeleteModal = true;
      }
    }

  }


  onDeletePreviewResp(response: string) {
    if (response === "yes") {
      // this.showDeleteModal = false;
      this.deleteLoading = true;
      const params: any = {
        c_id: this.deleteList.c_id,
        at_id: this.deleteList.at_id
      }
      this.campservice.deleteScCampaign(this.deleteList.id, this.deleteList.at_id)
        .subscribe((res: any) => {
          this.deleteLoading = false;
          this.showDeleteModal = false;
          this.deleteResponse = res;
          this.popUp = true;
          this.listData(this.receivedFilterData)
        }
          , (error: HttpErrorResponse) => {
            this.deleteLoading = false;
            this.showDeleteModal = false;
            this.deleteResponse = this.campservice.badError;
            this.popUp = true;
          })
    }
    if (response === "no") {
      this.deleteLoading = false;
      this.showDeleteModal = false;
      //no
    }
  }



  getcount() {
    if (this.searchText.trim().length == 0) {
      this.noRecords = 1;
      this.total = this.campaignLists.length;
      this.totalRecord = this.campaignLists.length;
      this.p = 1;
      this.itemsPerPage = this.perpageCount;
    } else {
      let counts;
      this.s_service.scheduleData.subscribe((data: any) => {
        counts = data.length;
        if (counts >= 0) {
          var count = counts;
          if (count == 0) {
            this.noRecords = 0;
            this.total = 0;
            this.p = 1;
            this.perpageCount = 10;
          } else {
            this.noRecords = 1;
            this.total = counts;
            this.p = 1;
            this.perpageCount = 10;
          }
        }
      });
    }
  }

  newSearchText(): string {
    return this.searchText.trim();
  }


  closeDeletePopup(event) {
    this.getcount();
    this.popUp = false;
  }

  tryAgainOnPopup(event) {
    //  this
  }
  continueDeletePopup(event) {
    this.getcount();
    this.popUp = false
  }


  order = "";
  searchprop: any = "";

  nameIcon = 0;
  msgIcon = 0;
  scheduledIcon = 0;
  totalIcon = 0;
  createdTsIcon = 0;
  selectedDtIcon = 0;


  defaultProp = "created_ts_unix"
  createdTs = ""

  sort(event) {
    this.searchprop = event.prop;
    this.order = event.order;
  }

  iconChange(event) {
    if (event.prop == "c_name") {
      this.nameIcon = event.icon;
      this.msgIcon = 0;
      this.scheduledIcon = 0;
      this.totalIcon = 0;
      this.createdTsIcon = 0;
      this.selectedDtIcon = 0;
    }
    else if (event.prop == "scheduled_ts_unix") {
      this.scheduledIcon = event.icon;
      this.nameIcon = 0;
      this.msgIcon = 0;
      this.totalIcon = 0;
      this.createdTsIcon = 0;
      this.selectedDtIcon = 0;

    }
    else if (event.prop == "total") {
      this.totalIcon = event.icon;
      this.nameIcon = 0;
      this.msgIcon = 0;
      this.scheduledIcon = 0;
      this.createdTsIcon = 0;
      this.selectedDtIcon = 0;

    }
    else if (event.prop == "msg") {
      this.msgIcon = event.icon;
      this.nameIcon = 0;
      this.scheduledIcon = 0;
      this.totalIcon = 0;
      this.createdTsIcon = 0;
      this.selectedDtIcon = 0;

    }
    else if (event.prop == "created_ts_unix") {
      this.createdTsIcon = event.icon;
      this.nameIcon = 0;
      this.msgIcon = 0;
      this.scheduledIcon = 0;
      this.totalIcon = 0;
      this.selectedDtIcon = 0;
    } else if (event.prop == "selected_ts_unix") {
      this.selectedDtIcon = event.icon;
      this.createdTsIcon = 0;
      this.nameIcon = 0;
      this.msgIcon = 0;
      this.scheduledIcon = 0;
      this.totalIcon = 0;
    }



  }

  ngOnDestroy(): void {
    if (this.statsloading) {
      this.statsloading.unsubscribe();
    }
    if (this.listloading) {
      this.listloading.unsubscribe();
    }
  }

}
