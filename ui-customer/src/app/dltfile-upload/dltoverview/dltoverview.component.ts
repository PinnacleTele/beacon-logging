import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { DltUploadService } from "src/app/dltfile-upload/dltupload.service";
import { DLTRECORDS } from "src/app/dltfile-upload/modals/modal";
import { NgSelectComponent } from "@ng-select/ng-select";
import { CommonService } from 'src/app/shared/commonservice';
import { CONSTANTS, value } from "src/app/shared/campaigns.constants";
import { BehaviorSubject } from 'rxjs';

@Component({
  selector: 'app-dltoverview',
  templateUrl: './dltoverview.component.html',
  styleUrls: ['./dltoverview.component.css']
})
export class DltoverviewComponent implements OnInit, OnDestroy {

  // senderidToBeSelected = "all";

  myOptions = value;

  @ViewChild('entity') public entity: NgSelectComponent;

  @ViewChild('template') public template: NgSelectComponent;


  @ViewChild(NgSelectComponent) ng1: NgSelectComponent

  @ViewChild(NgSelectComponent) ng2: NgSelectComponent


  statList: any;

  senderIds: any[] = [];

  entityIds: any[] = [];

  entityIdSearchText: string;

  dltRecords: DLTRECORDS[] = [];

  dltTemplates: any[] = [];


  entityIdList: any = [];

  templateIdList: any = []


  itemlist = [];

  totalEids: number = 0;

  senderEids: number = 0;


  drp: number = 1;

  pagesize: number;

  dritemsPerPage: number = 10;

  drperpageCount: number = 10;

  public searchText: any = "";

  public entityidToBeSelected: any;

  public templateidToBeSelected: any;


  drtotal: number = this.dltRecords.length;

  drtotalRecord: number = this.dltRecords.length;

  dTp: number = 1;

  // pagesize: number;

  dTitemsPerPage: number = 10;

  dTperpageCount: number = 10;

  public searchText1: any = "";


  dTtotal: number = this.dltTemplates.length;

  dTtotalRecord: number = this.dltTemplates.length;


  recordSelection: string = "this month";

  ei_loader: boolean;
  ti_loader: boolean;
  dltuploadloader: boolean;
  dlttemplateloader: boolean;

  public dltuploadloading = this.dltservice.dltAllLoading.subscribe((data: any) => { this.dltuploadloader = data });

  public dlttemplate = this.dltservice.dltAllTemplateLoading.subscribe((data: any) => { this.dlttemplateloader = data });



  public EILoading = this.dltservice.dltentityidFilterLoading.subscribe((data: any) => { this.ei_loader = data });

  public TILoading = this.dltservice.dltTemplateidFilterLoading.subscribe((data: any) => { this.ti_loader = data });


  apiError: boolean = false;

  noData: boolean = false;

  NDHeadContent = CONSTANTS.DLTNDHeadContent;

  NDMessageCOntent = CONSTANTS.DLTNDMessageCOntent;

  DLTNDHeadContent = CONSTANTS.DLTUPLOADNDHeadContent;

  DLTNDMessageCOntent = CONSTANTS.DLTUPLOADNDMessageCOntent;

  EIDNDHeadContent = CONSTANTS.EIDNDHeadContent;

  EIDNDMessageCOntent = CONSTANTS.EIDNDMessageCOntent;

  SIDNDHeadContent = CONSTANTS.SIDNDHeadContent;

  SIDNDMessageCOntent = CONSTANTS.SIDNDMessageCOntent;

  templateidfilter: any;

  entityidfilter: any;

  public noDLTData: boolean = false;

  DLTapiError: boolean = false;

  selectProperEIDValueError: boolean = false;

  selectProperTIDValueError: boolean = false;

  // disableButton: boolean = false;

  noEidFilter: boolean = false;
  noTidFilter: boolean = false;

  ApiErrorEidFilter: boolean = false;
  ApiErrorTidFilter: boolean = false;

  nosenderId: boolean;
  noRecSenderId: any;
  senderIdApiError: any;
  senderIdSearchText: string;


  noentityId: boolean;
  noRecentityId: any;
  entityIdApiError: any;

  noRecords: boolean = false;

  dTnoRecords: boolean = false;



  entityId: any = 'all';
  templateId: any = 'all';

  userZone: string = "";

  dynamicEntityValue:any;
  dynamicTemplateValue:any;

  disableButton = new BehaviorSubject<boolean>(true)

  constructor(private dltservice: DltUploadService,
    private commonservice: CommonService,) {

  }

  ngOnInit(): void {
    this.userZone = this.commonservice.getUserData();
    this.entityidFilter();
    this.statsCall();
    this.senderidsCall();
    this.entityIdStats();
    this.dltUploadRecords(this.recordSelection);

    this.createdDateCol = "Created Date(" + this.userZone + ")"
  }

  statsCall() {
    this.dltservice.getSTATSList().subscribe((data: any) => {
      this.statList = data;
    })
  }

  senderidsCall() {
    this.nosenderId = false;
    this.dltservice.getAllSenderids()
      .subscribe(
        (res: any) => {
          //this.nosenderId = true;
          if (res) {
            this.senderIdApiError = false;
            if (res.length == 0) {
              this.nosenderId = true;
              this.noRecSenderId == 0;
            }

            this.noRecSenderId == 1;
            this.senderIds = res;


            this.senderEids = this.senderIds.length;
            this.senderIdPaginationValue(this.senderIds.length)
          }
        },
        (error: HttpErrorResponse) => {
          this.senderIdApiError = true;
        }
      )
  }

  senderIDRetry() {
    this.senderidsCall();
  }

  entityIdStats() {
    this.noentityId = false;
    this.dltservice.getEntityIdStats()
      .subscribe(
        (res: any) => {
          // this.noentityId = false;
          if (res) {
            this.entityIdApiError = false;
            if (res.length == 0) {
              this.noentityId = true;
              this.noRecentityId == 0;
            }

            this.noRecentityId == 1;
            this.entityIds = res;
            console.log(this.entityIds);

            this.totalEids = this.entityIds.length;
            this.entityIdPaginationValue(this.entityIds.length)

          }
        },
        (error: HttpErrorResponse) => {
          this.entityIdApiError = true;
        }
      )
  }

  entityIDRetry() {
    //this.entityIdApiError = false;
    this.entityIdStats();
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



  entityidpaylod(event) {
    this.tempentityItems = this.entityIdList;
    if (event == undefined) {
      // this.entityidfilter = null;
      // this.templateIdList = [];
      // this.templateidToBeSelected = '';
      // this.template.handleClearClick();
      // this.selectProperEIDValueError = true;
      // this.selectProperTIDValueError = true;
    } else {
      this.entityidToBeSelected = event.entity_id;
      this.entityidfilter = event.entity_id_id;
      this.templateidToBeSelected = "";

      this.templateidFilter(this.entityidfilter, 'Eid');
    }
  }


  templateidpaylod(event) {
    this.temptemplateItems = this.templateIdList;
    if (event == undefined) {
      // this.templateidfilter = null;
      // this.selectProperTIDValueError = true;
    } else {
      //this.selectProperTIDValueError = false;
      this.templateidToBeSelected = event.dlt_template_name;
      this.templateidfilter = event.dlt_template_id;

    }
  }

  tempentityItems:any[]=[];

  customSearch() {
    const value = this.entityidToBeSelected?.trim();
    const dynamicval = this.dynamicEntityValue[0];
    this.tempentityItems =  this.commonservice.customSerach(this.entityIdList,dynamicval,value);   
}

entityKeyup(){
  this.tempentityItems = this.entityIdList;
}

entityBlur(){
  this.tempentityItems = this.entityIdList;
}


  entityidFilter() {
    this.disableButton.next(true);
    this.dltservice.getDLtEntityIdFilter()
      .subscribe(
        (res: any) => {
          //  this.noDLTData = false;
          if (res) {

            // if (res.length == 2) {
            //   this.entityidToBeSelected = res[1].entity_id;
            //   this.entityidfilter = res[1].entity_id_id;
            // } else {
              this.dynamicEntityValue = Object.keys(res[0])
             // console.log(this.dynamicEntityValue[0]);
              
              this.entityidToBeSelected = res[0].entity_id;
              this.entityidfilter = res[0].entity_id_id;
            //}
            
            this.entityIdList = res;
            this.tempentityItems = this.entityIdList;
            this.disableButton.next(false);
            this.noEidFilter = false;
            this.ApiErrorEidFilter = false;
            this.templateidFilter(this.entityidfilter, 'default');
            if (res.length == 0) {
              this.disableButton.next(true);
              this.noEidFilter = true;

            }
          }
        },
        (error: HttpErrorResponse) => {
          this.disableButton.next(true);
          this.ApiErrorEidFilter = true;
        }
      )
  }
  EntityRetry() {
    this.entityidFilter();
  }

  templateidFilter(eid: any, value: any) {
    this.disableButton.next(true);
    this.template.handleClearClick();
    this.dltservice.getDLtTemplateIdFilter(eid)
      .subscribe(
        (res: any) => {
          //  this.noDLTData = false;
          if (res) {
            this.selectProperTIDValueError = false;
            this.dynamicTemplateValue = Object.keys(res[0])
            this.templateidToBeSelected = res[0].dlt_template_name;
            this.templateidfilter = res[0].dlt_template_id;
            this.template.blur();
            this.templateIdList = res;
            this.temptemplateItems = this.templateIdList;
            this.disableButton.next(false);
            this.noTidFilter = false;
            this.ApiErrorTidFilter = false;
            if (res.length == 0) {
              this.disableButton.next(true);
              this.noTidFilter = true;
            }
            if (value == 'default') {
              this.filterValueSubmit();
            } else {
              //do nothing
            }
          }
        },
        (error: HttpErrorResponse) => {
          this.disableButton.next(true);
          this.ApiErrorTidFilter = true;
        }
      )
  }

  
  temptemplateItems:any[]=[];

  customSearch1() {
    if (this.dynamicTemplateValue != undefined) {
      const value = this.templateidToBeSelected?.trim();    
      const dynamicval = this.dynamicTemplateValue[1];
      this.temptemplateItems =  this.commonservice.customSerach(this.templateIdList,dynamicval,value);   
  
    }
}

templateKeyup(){
  this.temptemplateItems = this.templateIdList;
}

templateBlur(){
  this.temptemplateItems = this.templateIdList;
}
  TemplateRetry() {
    this.templateidFilter(this.entityidfilter, 'Eid')
  }


  filterValueSubmit() {
    if (this.ApiErrorTidFilter || this.ApiErrorEidFilter) {

    } else {
      this.AllDltTemplates();
    }
  }



  AllDltTemplates() {
    this.noData = false;
    this.searchText1 = "";
    this.dltservice.getAllDLtTemplate(this.entityidfilter, this.templateidfilter)
      .subscribe(
        (res: any) => {
          this.apiError = false;
          console.log(res);

          if (res) {
            if (res.length == 0) {
              this.noData = true;
            }

            // res.forEach(element => {
            //   element.dlt_entity_id = parseInt(element.dlt_entity_id)
            //   element.dlt_template_id = parseInt(element.dlt_template_id)

            // });
            this.dltTemplates = res;
            this.dTgetcount();
          }
        },
        (error: HttpErrorResponse) => {
          this.apiError = true;
        }
      )
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

  DTReceivedpaginateValue(event) {
    this.dTp = event;
  }

  DRReceivedpaginateValue(event) {
    this.drp = event;
  }

  retry() {
    this.noData = false;
    this.apiError = false;
    this.filterValueSubmit();
  }


  dTpaginationValue(tableList: any) {
    if (tableList == 0) {
      this.dTtotal = 0;
      this.dTp = 1;
      this.dTperpageCount = 0;
    } else {
      this.dTtotal = tableList;
      this.dTp = 1;
      this.dTitemsPerPage = this.dTperpageCount;
    }
  }
  dTgetcount() {
    if (this.searchText1.trim().length == 0) {
      this.dTnoRecords = false;
      this.dTtotal = this.dltTemplates.length;
      this.dTtotalRecord = this.dltTemplates.length
      this.dTp = 1;
      this.dTitemsPerPage = this.dTperpageCount;
    } else {
      if (this.dltservice.userDTSource.value.length >= 0) {

        var count = this.dltservice.userDTSource.value.length
        if (count == 0) {
          this.dTtotal = 0;
          this.dTp = 1;
          this.dTperpageCount = 10;
          this.dTnoRecords = true;
        } else {

          this.dTnoRecords = false;
          this.dTtotal = count;
          this.dTp = 1;
          this.dTperpageCount = 10;
        }
      }
    }
  }

  // senderid pagination

  sIdTotal: any;
  sIdPage: any = 1;
  sIdperpageCount: any = 10;
  sIdItemsPerPage: any = 20;
  senderIdPaginationValue(tableList: any) {
    // var count = this.senderIds.length
    if (tableList == 0) {
      this.sIdTotal = 0;
      this.sIdPage = 1;
      this.sIdperpageCount = 0;
    } else {
      this.sIdTotal = tableList;
      this.sIdPage = 1;
      this.sIdItemsPerPage = this.sIdItemsPerPage;
    }
  }

  senderIdReceivedpaginateValue(event) {
    this.sIdPage = event;
  }


  // entityid pagination

  eIdTotal: any;
  eIdPage: any = 1;
  eIdperpageCount: any = 10;
  eIdItemsPerPage: any = 5;
  entityIdPaginationValue(tableList: any) {
    // var count = this.senderIds.length
    if (tableList == 0) {
      this.eIdTotal = 0;
      this.eIdPage = 1;
      this.eIdperpageCount = 0;
    } else {
      this.eIdTotal = tableList;
      this.eIdPage = 1;
      this.eIdItemsPerPage = this.eIdItemsPerPage;
    }
  }

  entityIdReceivedpaginateValue(event) {
    this.eIdPage = event;
  }
  // newSearchText1() {
  //   return this.searchText1.trim();
  // }

  newSearchText() {
    return this.searchText.trim();
  }


  ngOnDestroy(): void {

    if (this.dlttemplate && this.EILoading || this.TILoading) {
      //this.dltuploadloading.unsubscribe();
      this.dlttemplate.unsubscribe();
      this.EILoading.unsubscribe();
      this.TILoading.unsubscribe();
    }

  }


  order = "";
  searchprop: any = "";

  senderIdIcon = 1;
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
  fileCreatedTsIcon = 0;

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
  noMatchingSenderids = false;
  getSenderIdcount() {
    if (this.senderIdSearchText.length == 0) {
      this.noMatchingSenderids = false;
      this.sIdTotal = this.senderIds.length;
      this.sIdPage = 1;
      // this.sIdItemsPerPage = this.sIdperpageCount;
    } else {

      if (this.dltservice.senderIDRecord && this.dltservice.senderIDRecord.length >= 0) {
        var count = this.dltservice.senderIDRecord.length
        console.log(count);

        if (count == 0) {
          this.noMatchingSenderids = true;
          this.sIdTotal = 0;
          this.sIdPage = 1;
          this.sIdItemsPerPage = 20;
        } else {
          this.noMatchingSenderids = false;
          this.sIdTotal = count;
          this.sIdPage = 1;
          this.sIdItemsPerPage = 20;
          // this.SearchArray = this.search.totalRecords ;


        }
      }
    }
  }
  noMatchingEntityid = false;
  getEntityIdcount() {
    if (this.entityIdSearchText.length == 0) {
      this.noMatchingEntityid = false;
      this.eIdTotal = this.entityIds.length;
      this.eIdPage = 1;
      // this.sIdItemsPerPage = this.sIdperpageCount;
    } else {

      if (this.dltservice.entityIDRecord && this.dltservice.entityIDRecord.length >= 0) {
        var count = this.dltservice.entityIDRecord.length
        if (count == 0) {
          this.noMatchingEntityid = true;
          this.eIdTotal = 0;
          this.eIdPage = 1;
          this.eIdperpageCount = 5;
        } else {
          this.noMatchingEntityid = false;
          this.eIdTotal = count;
          this.eIdPage = 1;
          this.eIdperpageCount = 5;
          // this.SearchArray = this.search.totalRecords ;


        }
      }
    }
  }
}
