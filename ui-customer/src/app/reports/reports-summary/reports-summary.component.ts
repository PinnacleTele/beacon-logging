import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { NgSelectComponent } from "@ng-select/ng-select";
import { ReportService } from "src/app/reports/Helpers/summary.service";
import { summaryList } from "src/app/reports/Helpers/summaryList.modal";
import { FiltercomponentComponent } from "src/app/reports/filtercomponent/filtercomponent.component";
import { HttpErrorResponse } from '@angular/common/http';
import { CommonService } from 'src/app/shared/commonservice';
import { CONSTANTS } from "src/app/shared/campaigns.constants";
import * as moment from 'moment';
// import { saveAs } from "file-saver";
@Component({
  selector: 'app-reports-summary',
  templateUrl: './reports-summary.component.html',
  styleUrls: ['./reports-summary.component.css']
})
export class ReportsSummaryComponent implements OnInit, OnDestroy {

  isText: false;
  isUnicode: boolean = false;
  isShowContact: boolean = false;
  removeDuplicateChecked: boolean = false;
  showPreview: boolean = false;
  showClearModal: boolean = false;
  showCancelModal: boolean = false;
  index: 5;
  filter: boolean = false;

  indexTable: number = 1;

  reportBy: string = 'date'

  tableIndex: number;

  userZone: string = "";
  popUp: boolean = false;
  deleteResponse: any;

  downloadIconLoader: boolean = false;
  viewIconLoader: boolean = false;


  constructor(private rsummary: ReportService,
    private commonservice: CommonService) {
    this.userZone = this.commonservice.userZone;
  }

  rSummaryList: summaryList[] = [];

  source: boolean = false;

  status: boolean = false;

  senderid: boolean = false;

  campaign: boolean = false;

  sourceList: string[] = [];

  campaignList: string[] = [];

  senderidList: string[] = [];

  viewReportByArray: any[] = [];

  reportByToBeSelected = 'Date';

  NDHeadContent = CONSTANTS.RNDHeadContent;

  NDMessageCOntent = CONSTANTS.RNDMessageCOntent;

  dateselectionType: string;

  public apiLoading = this.rsummary.summaryLoading.subscribe((data: any) => { this.apiLoading = data });

  noData: boolean = false;

  fdate: any;

  tdate: any;

  defaultFilter: any;

  campFilter: any;

  senderidFilter: any;

  sourceFilter: any;

  showHide: boolean = true;

  apiError: boolean = false;

  p: number = 1;

  pagesize: number;

  itemsPerPage: number = 10;

  perpageCount: number = 10;

  public searchText: any = "";

  total: number = this.rSummaryList.length;

  totalRecord: number = this.rSummaryList.length;

  buttonEnable: boolean = false;

  fileNameRange: string;

  UserTimeStamp = new Date();

  noRecords: number;

  finalObject: any;



  @ViewChild(FiltercomponentComponent, { static: false }) public clearsource: FiltercomponentComponent;

  ngOnInit(): void {
    this.viewReportByArray = [
      {display_name : "Date",apival :"date"},
      {display_name : "Campaign",apival :"campaign"},
      {display_name : "Source",apival :"source"},
      {display_name : "Sender Id",apival :"senderid"},
      {display_name : "Overall",apival :"overall"}
      
    ]
    this.dateselectionType = "last 7 days";
    this.fdate = "";
    this.tdate = "";
    let obj = {
      dateselectiontype: "last 7 days",
      fdate: "",
      tdate: ""
    }
    this.receivedDateSelection(obj);
  }


  paginationValue(tableList: any) {
    if (tableList == 0) {
      this.total = 0;
      this.p = 1;
      this.perpageCount = 0;
    } else {
      this.total = tableList;
      this.p = 1;
      this.itemsPerPage = this.perpageCount;
    }
  }

  getcount() {

    var count = this.rSummaryList.length

    if (count == 0) {
      this.noRecords = 0;
      this.total = 0;
      this.p = 1;
      this.perpageCount = 10;
    } else {
      this.noRecords = 1;
      this.total = count;
      this.totalRecord = count;
      this.p = 1;
      this.perpageCount = 10;
      this.itemsPerPage = this.perpageCount;
    }
  }

  indexFilter(col: any, value: any) {
    this.indexTable = col;
    this.reportBy = value;
    this.submitData();

  }

  viewReportSelect(event){
// console.log(event);
this.reportBy = event.apival;
  }

  backEnter(event: any, col: any, value: any) {
    if (event.keyCode == 13) {
      this.indexFilter(col, value);
    }
  }

  DefaultSelection: any;

  showTodayIncludeInfo: boolean = false;

  includes: boolean = false;

  receivedDateSelection(event) {
    let obj = {
      dateselectiontype: event.dateselectiontype,
      fdate: event.fdate,
      tdate: event.tdate
    }

    this.dateselectionType = event.dateselectiontype;
    this.fdate = event.fdate;
    this.tdate = event.tdate;
    if (this.dateselectionType.includes('last')) {
      this.showTodayIncludeInfo = true;
      this.includes = true;
    } else if (this.dateselectionType.includes('this')) {
      this.showTodayIncludeInfo = true;
      this.includes = false;
    } else {
      this.includes = false;
      this.showTodayIncludeInfo = false;
    }
    this.DefaultSelection = obj;
  }



  submitData() {
    if (this.finalObject == undefined) {
      // do nothing
    } else {
      this.finalObject.reportby = this.reportBy;

      this.onSubmit(this.finalObject);
    }
  }
  onSubmit(value) {
    this.viewIconLoader = true;
    this.rsummary.getSummaryList(value)
      .subscribe(
        (res: any) => {
          if (res) {
            this.viewIconLoader = false;

            this.noData = false;
            this.apiError = false;
            res.forEach((element, i) => {
              element.sortProp = i;
              // console.log(element);

              element.recv_unix = moment(element.recv_date).unix();
            });
            this.rSummaryList = res;
            if (res.length == 0) {
              this.noData = true;
              this.noRecords == 0;
            }
            this.getcount();
            this.showHide = false;

          }
        },
        (error: HttpErrorResponse) => {
          this.viewIconLoader = true;

          this.apiError = true;
        }
      )
  }

  submit(event) {
    let obj = {
      dateselectiontype: event.dateselectiontype,
      fdate: event.fdate,
      tdate: event.tdate,
      source: event.source,
      campaign_id: event.campaign_id,
      senderid: event.senderid,
      reportby: this.reportBy
    }
    this.finalObject = obj;
    this.onSubmit(obj)
  }

  ReceivedpaginateValue(event) {
    this.p = event;
  }

  retry() {
    this.submitData();
  }

  download(event) {
    this.downloadIconLoader = true;
    let obj = {
      dateselectiontype: event.dateselectiontype,
      fdate: event.fdate,
      tdate: event.tdate,
      source: event.source,
      campaign_id: event.campaign_id,
      senderid: event.senderid,
      reportby: this.reportBy
    }
    this.rsummary.downloadrsummary(obj)
      .subscribe((data: any) => {

        //data = "यह  के लिए एक नमूना संदेश है.";
        const a = document.createElement('a');
        const objectUrl = "data:text/csv;charset=utf-8,%EF%BB%BF" + encodeURI(data);
        a.href = objectUrl;
        a.download = "summary" + "_" + event.fdate + "_" + event.tdate + '.csv';
        this.downloadIconLoader = false;
        a.click();
        URL.revokeObjectURL(objectUrl);
      },
        (error: HttpErrorResponse) => {
          this.downloadIconLoader = false;
          this.popUp = true;
          this.deleteResponse = this.rsummary.badError;

        });
  }

  nullHandler(num: string) {

    if (num == (null || undefined || 'NaN')) {
      return 0;
    } else {
      return num;

    }

  }


  percentageConvertor(num: string) {

    if (num == (null || undefined || 'NaN')) {
      return 0 + "%";
    } else {
      return num + "%";

    }

  }

  ngOnDestroy(): void {

    if (this.apiLoading) {

      this.apiLoading.unsubscribe();
    }

  }

  closeDeletePopup(event) {

    this.popUp = false;
  }

  tryAgainOnPopup(event) {
    //  this
  }
  continueDeletePopup(event) {
    this.popUp = false
  }
  nameOrder = "";
  searchprop: any = "";

  dateIcon = 2;
  campaignIcon = 0;
  sourceIcon = 0;
  senderIdIcon = 0;
  submittedIcon = 0
  rejectedIcon = 0;
  deliveredIcon = 0;
  failedIcon = 0
  pendingIcon = 0;
  totalIcon = 0

  defaultProp = "sortProp"

  sort(event) {


    this.searchprop = event.prop;
    this.nameOrder = event.order;
  }

  iconChange(event) {


    if (event.prop == "recv_unix") {
      this.dateIcon = event.icon;
      this.campaignIcon = 0;
      this.sourceIcon = 0;
      this.senderIdIcon = 0;
      this.submittedIcon = 0
      this.rejectedIcon = 0;
      this.deliveredIcon = 0;
      this.failedIcon = 0
      this.pendingIcon = 0;
      this.totalIcon = 0;
    }
    else if (event.prop == "campaign_name") {
      this.dateIcon = 0;
      this.campaignIcon = event.icon;
      this.sourceIcon = 0;
      this.senderIdIcon = 0;
      this.submittedIcon = 0
      this.rejectedIcon = 0;
      this.deliveredIcon = 0;
      this.failedIcon = 0
      this.pendingIcon = 0;
      this.totalIcon = 0;


    }
    else if (event.prop == "source") {
      this.dateIcon = 0;
      this.campaignIcon = 0;
      this.sourceIcon = event.icon;
      this.senderIdIcon = 0;
      this.submittedIcon = 0
      this.rejectedIcon = 0;
      this.deliveredIcon = 0;
      this.failedIcon = 0
      this.pendingIcon = 0;
      this.totalIcon = 0;

    }
    else if (event.prop == "senderid") {
      this.dateIcon = 0;
      this.campaignIcon = 0;
      this.sourceIcon = 0;
      this.senderIdIcon = event.icon;
      this.submittedIcon = 0
      this.rejectedIcon = 0;
      this.deliveredIcon = 0;
      this.failedIcon = 0
      this.pendingIcon = 0;
      this.totalIcon = 0;

    }
    else if (event.prop == "total") {
      this.dateIcon = 0;
      this.totalIcon = event.icon;
      this.campaignIcon = 0;
      this.sourceIcon = 0;
      this.senderIdIcon = 0;
      this.submittedIcon = 0
      this.rejectedIcon = 0;
      this.deliveredIcon = 0;
      this.failedIcon = 0
      this.pendingIcon = 0;
      this.totalIcon = 0;
    }
    else if (event.prop == "mtsuccess") {
      this.dateIcon = 0;
      this.totalIcon = 0;
      this.campaignIcon = 0;
      this.sourceIcon = 0;
      this.senderIdIcon = 0;
      this.submittedIcon = event.icon;
      this.rejectedIcon = 0;
      this.deliveredIcon = 0;
      this.failedIcon = 0
      this.pendingIcon = 0;
      this.totalIcon = 0;
    }
    else if (event.prop == "mtrejected") {
      this.dateIcon = 0;
      this.totalIcon = 0;
      this.campaignIcon = 0;
      this.sourceIcon = 0;
      this.senderIdIcon = 0;
      this.submittedIcon = 0;
      this.rejectedIcon = event.icon;
      this.deliveredIcon = 0;
      this.failedIcon = 0
      this.pendingIcon = 0;
      this.totalIcon = 0;
    }
    else if (event.prop == "dnsuccess") {
      this.dateIcon = 0;
      this.totalIcon = 0;
      this.campaignIcon = 0;
      this.sourceIcon = 0;
      this.senderIdIcon = 0;
      this.submittedIcon = 0;
      this.rejectedIcon = 0;
      this.deliveredIcon = event.icon;
      this.failedIcon = 0
      this.pendingIcon = 0;
      this.totalIcon = 0;
    }
    else if (event.prop == "dnfailed") {
      this.dateIcon = 0;
      this.totalIcon = 0;
      this.campaignIcon = 0;
      this.sourceIcon = 0;
      this.senderIdIcon = 0;
      this.submittedIcon = 0;
      this.rejectedIcon = 0;
      this.deliveredIcon = 0;
      this.failedIcon = event.icon
      this.pendingIcon = 0;
      this.totalIcon = 0;
    }
    else if (event.prop == "dnpending") {
      this.dateIcon = 0;
      this.totalIcon = 0;
      this.campaignIcon = 0;
      this.sourceIcon = 0;
      this.senderIdIcon = 0;
      this.submittedIcon = 0;
      this.rejectedIcon = 0;
      this.deliveredIcon = 0;
      this.failedIcon = 0;
      this.pendingIcon = event.icon;
      this.totalIcon = 0;
    }

  }

}
