import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { ReportDetailService } from "src/app/reports/Helpers/detail.service";
import { reportDetailModel } from './report.detail.model';
import { ReportService } from "src/app/reports/Helpers/summary.service";
import { HttpErrorResponse } from '@angular/common/http';
import { Toast } from 'src/app/shared/toast/toast';
import { ToastService } from 'src/app/shared/toast/toast.service';
import { CommonService } from 'src/app/shared/commonservice';
import { CONSTANTS } from 'src/app/shared/campaigns.constants';
import * as moment from 'moment';
import { getCurrencySymbol } from '@angular/common';
@Component({
  selector: 'app-reports-detailed',
  templateUrl: './reports-detailed.component.html',
  styleUrls: ['./reports-detailed.component.css']
})
export class ReportsDetailedComponent implements OnInit, OnDestroy {
  DefaultSelection: { dateselectiontype: any; fdate: any; tdate: any; };

  reportBy: string = "date";

  NDHeadContent = CONSTANTS.RNDHeadContent;

  NDMessageCOntent = CONSTANTS.RNDMessageCOntent;

  finalObject: { dateselectiontype: any; fdate: any; tdate: any; source: any; campaign_id: any; senderid: any; reportby: string; status: any; };

  UserTimeStamp = new Date();

  rDetailedList = [];

  apiError: boolean = false;

  showHide: boolean = false;

  userZone: string = "";
  showTodayIncludeInfo: boolean = false;
  includes: boolean;
  popUp: boolean = false;
  deleteResponse: any;

  accept_header = "ACCEPTED AT "
  submit_header = "SUBMITTED AT "
  del_header = "DELIVERED AT "

  currencyType = CONSTANTS.currency;
  currency = getCurrencySymbol(this.currencyType, "wide");
  dltRateTitle = "";
  SMSRateTitle = "";

  constructor(private RDService: ReportDetailService,
    private rservice: ReportService,
    private commonservice: CommonService,
    private toastService: ToastService,
  ) {
    console.log('inside constrctor');

  }

  ngOnInit(): void {
    // this.userZone = this.commonservice.userZone;
    this.userZone = this.commonservice.getUserData();
    this.accept_header = "ACCEPTED AT (" + this.userZone + ")"
    this.submit_header = "SUBMITTED AT (" + this.userZone + ")"
    this.del_header = "DELIVERED AT (" + this.userZone + ")"
    //console.log(this.commonservice.getUserData());
    const userDataAsJson = this.commonservice.getUserdetail();
    this.currency = userDataAsJson.billing_currency;
    this.dltRateTitle = "Dlt rate (" + this.currency + ")";
    this.SMSRateTitle = "Sms rate (" + this.currency + ")";
  }

  selectedRange(event) {

  }

  items = ["UI", "SMPP", "API", "INTERNATIONAL"]

  term: string = ""
  p: number = 1;

  pagesize: number;

  itemsPerPage: number = 10;

  perpageCount: number = 10;

  dateselectionType: string;

  noData: boolean = false;

  fdate: any;

  tdate: any;


  campaignReport: reportDetailModel[] = [];

  total: number = this.campaignReport.length;

  totalRecord: number = this.campaignReport.length;

  showTable: boolean = false;

  finalObj: any;

  public apiLoading = this.rservice.detailedLoading.subscribe((data: any) => { this.apiLoading = data })



  searchBarClear() {
    this.term = "";
    this.getcount();

  }


  noRecords: any;

  getcount() {
    if (this.term.length == 0) {
      this.noRecords = 1;
      this.total = this.campaignReport.length;
      this.totalRecord = this.campaignReport.length;
      this.p = 1;
      this.itemsPerPage = this.perpageCount;
    } else {

      if (this.rservice.totalRecords.length >= 0) {
        var count = this.rservice.totalRecords.length;

        if (count == 0) {
          this.noRecords = 0;
          this.total = 0;
          this.p = 1;
          this.perpageCount = 10;
        } else {
          this.noRecords = 1;
          this.total = count;
          this.p = 1;
          this.perpageCount = 10;
          // this.SearchArray = this.search.totalRecords ;


        }
      }
    }
  }

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
      this.showTodayIncludeInfo = false;
      this.includes = false;
    }
    this.DefaultSelection = obj;

  }
  submitData() {

  }
  totalRows = 0;
  onSubmit(value) {
    this.showTable = true;
    this.rservice.getDetailedList(value)
      .subscribe(
        (res: any) => {
          if (res) {
            this.noData = false;
            this.apiError = false;
            this.campaignReport = res;
            if (res.length == 0) {
              this.noData = true;
              //this.noRecords == 0;
            }
            else {
              // res.forEach((element, i) => {
              //   element.sortProp = i;
              //   element.recv_unix = moment(element.recv_time).unix();
              //   element.del_unix = element.del_dly_time ? moment(element.del_dly_time).unix() : 0;
              //   element.sub_unix = element.sub_unix ? moment(element.sub_carrier_sub_time).unix() : 0;
              // });

            }



            this.getcount();
          }
        },
        (error: HttpErrorResponse) => {
          this.apiError = true;
        }
      )
  }


  ReceivedpaginateValue(event) {
    this.p = event;
  }

  submit(event) {
    let obj = {
      dateselectiontype: event.dateselectiontype,
      fdate: event.fdate,
      tdate: event.tdate,
      source: event.source,
      campaign_id: event.campaign_id,
      senderid: event.senderid,
      reportby: this.reportBy,
      status: event.status
    }
    
    this.finalObject = obj;
    this.onSubmit(obj)
  }

  download(event) {

    let obj = {
      dateselectiontype: event.dateselectiontype,
      fdate: event.fdate,
      tdate: event.tdate,
      source: event.source,
      campaign_id: event.campaign_id,
      campaign_name: event.campaignName,
      senderid: event.senderid,
      status: event.status,

    }


    this.rservice.downloadrDetailed(obj)
      .subscribe(
        (res: any) => {
          if (res) {

            if (res.statusCode == 200) {
              const toastData: Toast = new Toast(
                'Download request Sent!',
                'You can check your request in download center',
                'success'
              );

              this.toastService.addToast(toastData);
            }


          }
        },
        (error: HttpErrorResponse) => {
          this.rservice.badError
          this.popUp = true;
          this.deleteResponse = this.rservice.badError;


        }
      )
  }

  searchClear(event) {
    this.term = "";
  }

  retry() {
    this.noData = false;
    this.apiError = false;
    this.onSubmit(this.finalObject);
  }

  nullHandler(num: any) {

    if (num == null || num == undefined || null == "NaN") {
      return 0;
    } else {
      return num;
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
