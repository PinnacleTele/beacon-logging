import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { DltUploadService } from "src/app/dltfile-upload/dltupload.service";
import { DLTRECORDS } from "src/app/dltfile-upload/modals/modal";
import { NgSelectComponent } from "@ng-select/ng-select";
import { CommonService } from 'src/app/shared/commonservice';
import { CONSTANTS, value } from "src/app/shared/campaigns.constants";
import { BehaviorSubject } from 'rxjs';
@Component({
  selector: 'app-dltuploadlist',
  templateUrl: './dltuploadlist.component.html',
  styleUrls: ['./dltuploadlist.component.css']
})
export class DltuploadlistComponent implements OnInit , OnDestroy{

  myOptions= value;

  statList: any;



  dltRecords: DLTRECORDS[] = [];

  drp: number = 1;

  pagesize: number;

  dritemsPerPage: number = 10;

  drperpageCount: number = 10;

  public searchText: any = "";

  drtotal: number = this.dltRecords.length;

  drtotalRecord: number = this.dltRecords.length;

  dTp: number = 1;

  // pagesize: number;

  dTitemsPerPage: number = 10;

  dTperpageCount: number = 10;

  public searchText1: any = "";

  recordSelection: string = "this month";

  ei_loader: boolean;
  ti_loader: boolean;
  dltuploadloader: boolean;
  dlttemplateloader: boolean;

  public dltuploadloading = this.dltservice.dltAllLoading.subscribe((data: any) => { this.dltuploadloader = data });

  apiError: boolean = false;

  noData: boolean = false;

  NDHeadContent = CONSTANTS.DLTNDHeadContent;

  NDMessageCOntent = CONSTANTS.DLTNDMessageCOntent;

  DLTNDHeadContent = CONSTANTS.DLTUPLOADNDHeadContent;

  DLTNDMessageCOntent = CONSTANTS.DLTUPLOADNDMessageCOntent;





  public noDLTData: boolean = false;

  DLTapiError: boolean = false;

  noRecords: boolean = false;

  dTnoRecords: boolean = false;

  userZone: string = "";

  disableButton = new BehaviorSubject<boolean>(true)

  constructor(private dltservice: DltUploadService,
    private commonservice: CommonService,) {

  }

  ngOnInit(): void {
    this.userZone = this.commonservice.getUserData();
    this.statsCall();
    this.dltUploadRecords(this.recordSelection);

    this.createdDateCol = "Created Date(" + this.userZone + ")"
  }

  statsCall() {
    this.dltservice.getSTATSList().subscribe((data: any) => {
      this.statList = data;
    })
  }

  dltRecordFilter: string;

  dltUploadRecords(value) {
    this.searchText = "";
    this.noDLTData = false;
    this.recordSelection = value;
    this.dltservice.getAllDLtUpload(value)
      .subscribe(
        (res: any) => {
          this.DLTapiError = false;
          // this.noDLTData = true;
          if (res) {


            this.dltRecords = res;
            if (res.length == 0) {
              this.noDLTData = true;

            }


            this.getcount();
          }
        },
        (error: HttpErrorResponse) => {
          this.DLTapiError = true;
        }
      )
  }

  DLTretry() {
    this.noDLTData = false;
    this.DLTapiError = false;
    this.dltUploadRecords(this.recordSelection)
  }



  paginationValue(tableList: any) {
    if (tableList == 0) {
      this.drtotal = 0;
      this.drp = 1;
      this.drperpageCount = 0;
    } else {
      this.drtotal = tableList;
      this.drp = 1;
      this.dritemsPerPage = this.drperpageCount;
    }
  }

  getcount() {
    if (this.searchText.trim().length == 0) {
      this.noRecords = false;
      this.drtotal = this.dltRecords.length;
      this.drtotalRecord = this.dltRecords.length
      this.drp = 1;
      this.dritemsPerPage = this.drperpageCount;
    } else {
      if (this.dltservice.userDataSource.value.length >= 0) {
        var count = this.dltservice.userDataSource.value.length
        if (count == 0) {
          this.drtotal = 0;
          this.drp = 1;
          this.drperpageCount = 10;
          this.noRecords = true;
        } else {
          this.noRecords = false;
          this.drtotal = count;
          this.drp = 1;
          this.drperpageCount = 10;
        }
      }
    }
  }


  DRReceivedpaginateValue(event) {
    this.drp = event;
  }

  newSearchText() {
    return this.searchText.trim();
  }


  ngOnDestroy(): void {

    if (this.dltuploadloading ) {
      this.dltuploadloading.unsubscribe();
     
    }

  }


  order = "";
  searchprop: any = "";

  senderIdIcon = 0;
  msgIcon = 0;
  entityIdIcon = 0;
  templateIdIcon = 0;
  createdTsIcon = 0;
  createdDateCol = ""

  defaultProp = "header"

  sort(event) {
    this.searchprop = event.prop;
    this.order = event.order;
  }

  iconChange(event) {
    if (event.prop == "header") {
      this.senderIdIcon = event.icon;
      this.msgIcon = 0;
      this.entityIdIcon = 0;
      this.templateIdIcon = 0;
      this.createdTsIcon = 0;
    }
    else if (event.prop == "dlt_template_content") {
      this.senderIdIcon = 0;
      this.msgIcon = event.icon;
      this.entityIdIcon = 0;
      this.templateIdIcon = 0;
      this.createdTsIcon = 0;
    }
    else if (event.prop == "dlt_entity_id") {
      this.senderIdIcon = 0;
      this.msgIcon = 0;
      this.entityIdIcon = event.icon;
      this.templateIdIcon = 0;
      this.createdTsIcon = 0;
    }
    else if (event.prop == "dlt_template_id") {
      this.senderIdIcon = 0;
      this.msgIcon = 0;
      this.entityIdIcon = 0;
      this.templateIdIcon = event.icon;
      this.createdTsIcon = 0;

    }
    else if (event.prop == "created_ts_unix") {
      this.senderIdIcon = 0;
      this.msgIcon = 0;
      this.entityIdIcon = 0;
      this.templateIdIcon = 0;
      this.createdTsIcon = event.icon;

    }

  }

  fileOrder = "";
  fileSearchprop: any = "";

  fileFromIcon = 0;
  fileTotalIcon = 0;
  fileStatusIcon = 0;
  fileEntityIdIcon = 0;
  fileCreatedTsIcon = 2;

  fileDefaultProp = "created_ts_unix"

  fileSort(event) {

    this.fileSearchprop = event.prop;
    this.fileOrder = event.order;
  }

  fileIconChange(event) {
    if (event.prop == "telco") {
      this.fileFromIcon = event.icon;
      this.fileStatusIcon = 0;
      this.fileTotalIcon = 0;
      this.fileEntityIdIcon = 0;
      this.fileCreatedTsIcon = 0;
    }
    else if (event.prop == "total") {
      this.fileTotalIcon = event.icon;
      this.fileFromIcon = 0;
      this.fileStatusIcon = 0;
      this.fileEntityIdIcon = 0;
      this.fileCreatedTsIcon = 0;
    }
    else if (event.prop == "status") {
      this.fileStatusIcon = event.icon;
      this.fileTotalIcon = 0;
      this.fileFromIcon = 0;
      this.fileEntityIdIcon = 0;
      this.fileCreatedTsIcon = 0;
    }
    else if (event.prop == "dlt_entity_id") {
      this.fileEntityIdIcon = event.icon;
      this.fileTotalIcon = 0;
      this.fileFromIcon = 0;
      this.fileStatusIcon = 0;
      this.fileCreatedTsIcon = 0;

    }
    else if (event.prop == "created_ts_unix") {
      this.fileCreatedTsIcon = event.icon;
      this.fileTotalIcon = 0;
      this.fileFromIcon = 0;
      this.fileStatusIcon = 0;
      this.fileEntityIdIcon = 0;

    }

  }

}
