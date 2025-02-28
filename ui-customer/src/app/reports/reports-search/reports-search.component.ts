import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { reportDetailModel } from '../reports-detailed/report.detail.model';
import { ReportService } from "src/app/reports/Helpers/summary.service";
import { Toast } from 'src/app/shared/toast/toast';
import { ToastService } from 'src/app/shared/toast/toast.service';
import { CommonService } from 'src/app/shared/commonservice';
import { CONSTANTS } from "src/app/shared/campaigns.constants";
import * as moment from 'moment';
import { LocalStorageService } from 'src/app/authentication/local-storage.service';


@Component({
  selector: 'app-reports-search',
  templateUrl: './reports-search.component.html',
  styleUrls: ['./reports-search.component.css']
})
export class ReportsSearchComponent implements OnInit, OnDestroy {

  typeOfSearch = "mobile"

  PlaceHolderterm = "Enter mobile with country code"

  inputType: any = "mobile";

  dateselection: any;

  term: string = "";

  NDHeadContent = CONSTANTS.RNDHeadContent;

  NDMessageCOntent = CONSTANTS.RNDMessageCOntent;

  showerror: boolean = false;

  errorMessage: string = "please give mobile num / ackid";

  showTable: boolean = false;


  public apiLoading = this.rservice.searchLoading.subscribe((data: any) => { this.apiLoading = data })

  campaignReport: reportDetailModel[] = [];

  p: number = 1;

  pagesize: number;

  itemsPerPage: number = 10;

  perpageCount: number = 10;
  noData: boolean;
  apiError: boolean;
  showHide: boolean;
  userZone: string = "";
  dateselectionType: any;
  showTodayIncludeInfo: boolean = false;
  includes: boolean = false;
  popUp: boolean = false;
  deleteResponse: any;
  downloadIconLoader: boolean;
  viewIconLoader: boolean;

  userDetails = JSON.parse(this.localStorageService.getLocal('user'));
  SMSRateHeader = `SMS RATE (${this.userDetails.billing_currency})`;
  DLTRateHeader = `DLT RATE (${this.userDetails.billing_currency})`;

  accept_header = `ACCEPTED AT (${this.userDetails.zone_abbr}) `
  submit_header = `SUBMITTED AT (${this.userDetails.zone_abbr})  `
  del_header = `DELIVERED AT (${this.userDetails.zone_abbr}) `

  ErrorTerm: string;

  regExp: any = CONSTANTS.numberCheck;

  public sDownloadIcon = this.rservice.downloadSearachapiLoading.subscribe((data: any) => { this.downloadIconLoader = data });

  public sViewIcon = this.rservice.searchLoading.subscribe((data: any) => { this.viewIconLoader = data });



  constructor(private rservice: ReportService, 
    private commonservice: CommonService, 
    private localStorageService:LocalStorageService
    ) {

  }


  ngOnInit(): void {
    this.userZone = this.commonservice.userZone;
    // console.log(this.userZone);

    // this.accept_header = "ACCEPTED AT (" + this.userZone + ")"
    // this.submit_header = "SUBMITTED AT (" + this.userZone + ")"
    // this.del_header = "DELIVERED AT (" + this.userZone + ")"

  }

  selectedRange(event) {

  }
  searchType(value) {
    this.term = "";
    this.showerror = false;
    if (value == "mobile") {
      this.typeOfSearch = "mobile";

      this.PlaceHolderterm = "Enter valid mobile with country code";

    } else {

      this.PlaceHolderterm = "Enter valid Ackid";
      this.typeOfSearch = "ackid";
    }
  }
  receivedDateSelection(event) {
    this.dateselection = event;
    this.dateselectionType = event.dateselectiontype;
    if (this.dateselectionType.includes('last')) {
      this.showTodayIncludeInfo = true;
      this.includes = true;
    } else if (this.dateselectionType.includes('this')) {
      this.showTodayIncludeInfo = true;
      this.includes = false;
    } else {
      this.showTodayIncludeInfo = false;
      this.includes = false;
    }

  }
  submitData() {

  }

  total: number = this.campaignReport.length;

  totalRecord: number = this.campaignReport.length;


  searchBarClear() {
    this.term = ""

  }

  searchTerm() {


    if (this.term.length == 0) {
      this.showerror = true;

      this.checkTypeSearch();
    } else {
      this.showerror = false;
      this.checkTypeSearch();
    }
  }

  checkTypeSearch() {
    if (this.typeOfSearch == "mobile") {
      if (!this.term.match(this.regExp)) {
        this.showerror = false;
      } else {
        this.showerror = true;
      }
    } else {
      this.showerror = false;
      this.typeOfSearch = "ackid";
    }
  }

  ReceivedpaginateValue(event) {
    this.p = event;
  }

  getcount() {
    var count = this.campaignReport.length
    if (count == 0) {
      // this.noRecords=0;
      this.total = 0;
      this.p = 1;
      this.perpageCount = 10;
    } else {
      // this.noRecords=1;
      this.total = count;
      this.p = 1;
      this.perpageCount = 10;
    }
  }
  noRecords: any;

  searchData() {
    let rangeValue = this.dateselection;
    this.term = (this.term as string).toLowerCase().trim();

    let obj = {
      dateselectiontype: rangeValue.dateselectiontype,
      fdate: rangeValue.fdate,
      tdate: rangeValue.tdate,
      input: this.term,
      search_for: this.typeOfSearch
    }

    if (this.term == "") {

      this.showerror = true;
    } else {
      this.viewIconLoader = true;
      this.noData = false;
      this.showTable = true;
      this.rservice.getSearchList(obj)
        .subscribe(
          (res: any) => {
            this.viewIconLoader = false;
            // if (res) {
            //  this.noData = false;
            // res.forEach((element, i) => {
            //   element.sortprop = i
            //   element.acc_unix = moment(element.recv_time).unix();
            //   element.del_unix = element.del_dly_time ? moment(element.del_dly_time).unix() : 0;
            //   element.sub_unix = element.sub_unix ? moment(element.sub_carrier_sub_time).unix() : 0;
            // });
            // console.log(res);

            this.apiError = false;
            this.campaignReport = res;
            if (res.length == 0) {
              this.noData = true;
              // this.noRecords == 0;
            }
            this.getcount();
            //}
          },
          (error: HttpErrorResponse) => {
            this.viewIconLoader = false;
            this.apiError = true;

          }
        )

    }

  }

  downloadData() {
    let rangeValue = this.dateselection;
    this.term = (this.term as string).toLowerCase().trim();
    let obj = {
      dateselectiontype: rangeValue.dateselectiontype,
      fdate: rangeValue.fdate,
      tdate: rangeValue.tdate,
      input: this.term,
      search_for: this.typeOfSearch
    }

    if (this.term == "") {
      this.showerror = true;
    } else {
      this.downloadIconLoader = true;

      this.rservice.downloadrSearch(obj)
        .subscribe((data: any) => {
          const a = document.createElement('a');
          const objectUrl = "data:text/csv;charset=utf-8,%EF%BB%BF" + encodeURIComponent(data);
          a.href = objectUrl;
          a.download = "log_" + obj.fdate + "_" + obj.tdate + '_' + obj.input + '.csv';
          //  this.downloadIconLoader = false;
          a.click();




          URL.revokeObjectURL(objectUrl);
        },
          (error: HttpErrorResponse) => {
            this.downloadIconLoader = false;

            this.popUp = true;
            this.deleteResponse = this.rservice.badError;

          })
    }

  }

  nullHandler(num: any) {

    if (num == null || num == undefined || null == "NaN") {
      return 0;
    } else {
      return num;
    }

  }
  retry() {
    this.apiError = false;
    this.searchData();
  }

  ngOnDestroy(): void {

    if (this.apiLoading) {
      this.apiLoading.unsubscribe();
    }
    if (this.sDownloadIcon) {
      this.sDownloadIcon.unsubscribe();
    }

    if (this.sViewIcon) {
      this.sViewIcon.unsubscribe();
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

  acceptedTimeIcon = 2;
  submittedIcon = 0;
  deliveredIcon = 0;
  senderIdIcon = 0;
  statusIcon = 0;
  mobileIcon = 0
  msgIcon = 0;
  SMSrateIcon = 0;
  DLTrateIcon = 0;
  ACKidIcon = 0;
  reasonIcon = 0;
  defaultProp = "recv_unix"



  sort(event) {

    this.searchprop = event.prop;
    this.nameOrder = event.order;
  }

  iconChange(event) {

    if (event.prop == "recv_unix") {

      this.acceptedTimeIcon = event.icon;
      this.submittedIcon = 0;
      this.deliveredIcon = 0;
      this.senderIdIcon = 0;
      this.mobileIcon = 0
      this.statusIcon = 0;
      this.msgIcon = 0;
      this.SMSrateIcon = 0;
      this.DLTrateIcon = 0;
      this.ACKidIcon = 0;
      this.reasonIcon = 0;
    }
    else if (event.prop == "sub_unix") {
      this.submittedIcon = event.icon;
      this.acceptedTimeIcon = 0;
      this.deliveredIcon = 0;
      this.senderIdIcon = 0;
      this.mobileIcon = 0
      this.statusIcon = 0;
      this.msgIcon = 0;
      this.SMSrateIcon = 0;
      this.DLTrateIcon = 0;
      this.ACKidIcon = 0;
      this.reasonIcon = 0;
    }
    else if (event.prop == "del_unix") {
      this.deliveredIcon = event.icon;
      this.submittedIcon = 0;
      this.acceptedTimeIcon = 0;
      this.senderIdIcon = 0;
      this.mobileIcon = 0
      this.statusIcon = 0;
      this.msgIcon = 0;
      this.SMSrateIcon = 0;
      this.DLTrateIcon = 0;
      this.ACKidIcon = 0;
      this.reasonIcon = 0;
    }
    else if (event.prop == "description") {
      this.acceptedTimeIcon = event.icon;
      this.submittedIcon = 0;
      this.deliveredIcon = 0;
      this.senderIdIcon = 0;
      this.statusIcon = 0;
      this.msgIcon = 0;
      this.SMSrateIcon = 0;
      this.DLTrateIcon = 0;
      this.ACKidIcon = 0;
      this.reasonIcon = 0;
      this.mobileIcon = 0

    }
    else if (event.prop == "sub_cli_hdr") {
      this.senderIdIcon = event.icon;
      this.acceptedTimeIcon = 0;
      this.submittedIcon = 0;
      this.deliveredIcon = 0;
      this.statusIcon = 0;
      this.msgIcon = 0;
      this.SMSrateIcon = 0;
      this.DLTrateIcon = 0;
      this.ACKidIcon = 0;
      this.reasonIcon = 0;
    }

    else if (event.prop == "dest") {
      this.mobileIcon = event.icon
      this.senderIdIcon = 0;
      this.acceptedTimeIcon = 0;
      this.submittedIcon = 0;
      this.deliveredIcon = 0;
      this.statusIcon = 0;
      this.msgIcon = 0;
      this.SMSrateIcon = 0;
      this.DLTrateIcon = 0;
      this.ACKidIcon = 0;
      this.reasonIcon = 0;
    }
    else if (event.prop == "status") {
      this.statusIcon = event.icon;
      this.mobileIcon = 0;
      this.senderIdIcon = 0;
      this.acceptedTimeIcon = 0;
      this.submittedIcon = 0;
      this.deliveredIcon = 0;
      this.msgIcon = 0;
      this.SMSrateIcon = 0;
      this.DLTrateIcon = 0;
      this.ACKidIcon = 0;
      this.reasonIcon = 0;
    }
    else if (event.prop == "sub_msg") {
      this.statusIcon = 0;
      this.mobileIcon = 0;
      this.senderIdIcon = 0;
      this.acceptedTimeIcon = 0;
      this.submittedIcon = 0;
      this.deliveredIcon = 0;
      this.msgIcon = event.icon;
      this.SMSrateIcon = 0;
      this.DLTrateIcon = 0;
      this.ACKidIcon = 0;
      this.reasonIcon = 0;
    }

    else if (event.prop == "sms_price") {
      this.statusIcon = 0;
      this.mobileIcon = 0;
      this.senderIdIcon = 0;
      this.acceptedTimeIcon = 0;
      this.submittedIcon = 0;
      this.deliveredIcon = 0;
      this.msgIcon = 0;
      this.SMSrateIcon = event.icon;
      this.DLTrateIcon = 0;
      this.ACKidIcon = 0;
      this.reasonIcon = 0;
    }
    else if (event.prop == "dlt_price") {
      this.statusIcon = 0;
      this.mobileIcon = 0;
      this.senderIdIcon = 0;
      this.acceptedTimeIcon = 0;
      this.submittedIcon = 0;
      this.deliveredIcon = 0;
      this.msgIcon = 0;
      this.SMSrateIcon = 0;
      this.DLTrateIcon = event.icon;
      this.ACKidIcon = 0;
      this.reasonIcon = 0;
    }
    else if (event.prop == "dlt_price") {
      this.statusIcon = 0;
      this.mobileIcon = 0;
      this.senderIdIcon = 0;
      this.acceptedTimeIcon = 0;
      this.submittedIcon = 0;
      this.deliveredIcon = 0;
      this.msgIcon = 0;
      this.SMSrateIcon = 0;
      this.DLTrateIcon = event.icon;
      this.ACKidIcon = 0;
      this.reasonIcon = 0;
    }
    else if (event.prop == "reason") {
      this.statusIcon = 0;
      this.mobileIcon = 0;
      this.senderIdIcon = 0;
      this.acceptedTimeIcon = 0;
      this.submittedIcon = 0;
      this.deliveredIcon = 0;
      this.msgIcon = 0;
      this.SMSrateIcon = 0;
      this.DLTrateIcon = 0;
      this.ACKidIcon = 0;
      this.reasonIcon = event.icon;
    }
    else if (event.prop == "sub_file_id") {
      this.statusIcon = 0;
      this.mobileIcon = 0;
      this.senderIdIcon = 0;
      this.acceptedTimeIcon = 0;
      this.submittedIcon = 0;
      this.deliveredIcon = 0;
      this.msgIcon = 0;
      this.SMSrateIcon = 0;
      this.DLTrateIcon = 0;
      this.ACKidIcon = event.icon;
      this.reasonIcon = 0;
    }

  }

}
