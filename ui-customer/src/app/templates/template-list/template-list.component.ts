import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges, ViewChild } from '@angular/core';
import { SearchService } from "../search.service";
import { Router } from "@angular/router";
import { TemplateService } from "../Helpers/templates.service";
import { TemplateModel } from "../model/templatemodal";
import { HttpErrorResponse } from '@angular/common/http';
import { CONSTANTS, value } from "src/app/shared/campaigns.constants";
import { CommonService } from 'src/app/shared/commonservice';

@Component({
  selector: "app-template-list",
  templateUrl: "./template-list.component.html",
  styleUrls: ["./template-list.component.css"]
})
export class TemplateListComponent implements OnInit {

  @Output() deleteCount = new EventEmitter<number>();


  term: string;

  globalSelect: any = [];

  public searchText: any = "";

  TemplateDetail: TemplateModel[] = []

  total: number = this.TemplateDetail.length;

  totalRecord: number = this.TemplateDetail.length;

  p: number = 1;

  pagesize: number;

  itemsPerPage: number = 10;

  collection: any = [];

  EmptyArray: any = [];

  recordCount: boolean = false;

  selectedTemplate: TemplateModel[] = [];

  perpageCount: number = 10;

  checkListCount: number;

  selectAll: any = []

  start: any

  ending: any

  RecordArray: any = []

  checkedAllSelect: boolean = false

  checkedItem: any = []

  masterSelected: boolean;

  checklist: any;

  checkedList: any;

  deleteMessage = CONSTANTS.INFO_TXT.templateDeleteMessage;

  skeletonloader = true;

  SearchArray: any = [];

  SearchResultCount: number;

  selectAllCount: number = 0;

  loader: boolean = false;

  userZone: string = "";

  selectedData: any = [];

  deleteResponse: any;

  popUp = false

  deletingSpinner: boolean = false;

  showDeleteModal: boolean = false;

  showhide: boolean = false;

  listDataForRetry: any;

  searchedData:any = "";

  myOptions = value;

  templateLoading = this.template.templateLoading.subscribe((data) => { this.loader = data });

  // @Input() count : any = this.search.ArrayCount;

  constructor(public search: SearchService, private commonservice: CommonService, public router: Router, public template: TemplateService) {

  }
  // ngOnChanges(changes: SimpleChanges): void {
    
  //   this.search.userData.subscribe((data: any) => {
  //     this.SearchArray = data
  //   });
    
  // }

  getcount() {   
  //  this.searchedData = this.searchText;

    if (this.searchText.length == 0) {
      this.recordCount = false;
      this.total = this.TemplateDetail.length;
      this.p = 1;
      this.perpageCount = 10;
      this.itemsPerPage = this.perpageCount;
      this.selectAll = this.TemplateDetail;
      this.SearchArray = this.TemplateDetail;
      this.search.searchData(this.TemplateDetail);
      this.SearchResultCount = 0;
      this.pageCount();
      this.iterateArray();
    } else {

      if (this.search.ArrayCount >= 0) {
        var count = this.search.ArrayCount
        if (count == 0) {
          this.recordCount = true;
          this.total = 0;
          this.p = 1;
          this.perpageCount = 10;
        } else {

          this.recordCount = false;
          this.total = count;
          this.p = 1;
          this.perpageCount = 10;
          this.search.userData.subscribe((data: any) => {
            this.SearchArray = data
          });
          this.selectAll = this.SearchArray;
          this.SearchResultCount = this.search.ArrayCount;
        }
        this.pageCount();
        this.iterateArray();
      }
    }

  }
  dataValue: any[] = [];
  apiError: boolean = false;
  noRecords: boolean = false;

  ngOnInit(): void {
    this.userZone = this.commonservice.getUserData();
    this.subscribeData();
    this.createdTs = "CREATED DATE  (" + this.userZone + ")"
  }

  refresh(): void {
    this.subscribeData();
    // this.searchText= ""
    //  this.getcount();
    this.router.navigate(['templates'])

    // this.getcount();
  }
  subscribeData() {
    this.apiError = false;
    this.loader = false;
    this.noRecords = false;
    this.template.getTemplatesList()
      .subscribe(
        (res: any) => {
          if (res) {
            this.dataValue = res;
            // this.noRecords = true;
            this.search.searchData(res);
            this.search.searchcount(res.length);

            if (this.dataValue.length == 0) {
              this.noRecords = true;
            }
            if (this.selectedData.length > 0) {
              this.dataValue.forEach((dele: any) => {
                dele.ischecked = false;
                this.selectedData.filter((x: any) => dele.id === x.id)
                  .forEach((x: any) => {
                    x.ischecked = true;
                    dele.ischecked = true;
                  });
              });
              this.selectAllCount = this.selectedData.length;
              //this.checkedItem = this.RecordArray;
              this.TemplateDetail = this.dataValue
              this.paginateValue();
              this.iterateArray();
            } else {
              this.dataValue.forEach((value: any) => {
                value.ischecked = false
              })
              this.TemplateDetail = this.dataValue;
            
              if (this.SearchResultCount == 1) {
                this.searchText = '';
              } else if (this.searchText.length > 1) {
                this.pageCount();
                //this.getcount();
              }
              this.paginateValue();
              this.iterateArray();
            }

            this.skeletonloader = false;
            this.apiError = false;
          }
        },
        (error: HttpErrorResponse) => {
          this.apiError = true;
        }
      )
  }

  // count for paginate
  paginateValue() {
      if (this.TemplateDetail.length == 0) {
      //  this.skeletonloader = false;
        this.total = 0;
        this.totalRecord = this.TemplateDetail.length;
        this.p = 1;
        this.perpageCount = 0;
        this.masterSelected = false;
        this.collection = this.TemplateDetail;
      } else if (this.searchText.length > 1) {
        
        this.search.userData.subscribe((data: any) => {
          this.SearchArray = data
        });        
        this.total = this.SearchArray.length;
        this.totalRecord = this.SearchArray.length;
        this.p = 1;
        this.itemsPerPage = this.perpageCount;
        this.collection = this.TemplateDetail;
      } else {
        this.total = this.TemplateDetail.length;
        this.p = 1;
        this.itemsPerPage = this.perpageCount;
        this.collection = this.TemplateDetail;
        this.totalRecord = this.TemplateDetail.length;
      }
  }

  //  manual check/uncheck item list 
  singleSelect(template: TemplateModel, value: any) {
    this.pageCount();
    if (value.target.checked) {
      template.ischecked = true;
      if (this.selectedData.length > 0) {
        this.toCheckAlreadyCheked(template, value);
      } else {
        this.selectedData.push(template)
        this.selectAllCount = this.selectedData.length
      }
    }
    else {
      template.ischecked = false;
      if (this.selectedData.length > 0) {
        this.toCheckAlreadyCheked(template, value)
      } else {
        let i = this.selectedData.map((item: any) => item).indexOf(template)
        this.selectedData.splice(i, 1)
        this.selectAllCount = this.selectedData.length
      }
    }
    this.iterateArray();
  }

  // to check item already checked 
  toCheckAlreadyCheked(datavalue: any, value: any) {
    if (value.target.checked) {
      let listitem = []
      listitem = this.selectedData;
      var uncheckArray = listitem.filter(
        (item: any) => item.id == datavalue.id);

      if (uncheckArray.length == 0) {
        this.selectedData.push(datavalue)
      }
      this.selectAllCount = this.selectedData.length;
    } else {
      let listitem = []
      listitem = this.selectedData
      var uncheckArray = listitem.filter(
        (item: any) => item.id != datavalue.id);
      this.selectedData = uncheckArray
      this.selectAllCount = this.selectedData.length
    }
  }

  //  select all event
  selectall(event: any) {

    this.pageCount();
    if (this.SearchArray.length > 0) {
      this.selectAll = this.SearchArray
    } else {
      this.selectAll = this.TemplateDetail
    }
    if (event.target.checked) {
      this.masterSelected = true

      let checkeditem = this.selectAll.slice(this.started, this.ended)
      checkeditem.forEach((element: any) => {
        element.ischecked = true;
        if (!this.selectedData.some((el: any) => el.id === element.id)) {
          this.selectedData.push(element)
        }
      })
      // this.RecordArray = this.checkedItem
      this.selectAllCount = this.selectedData.length;
    } else {
      this.pageCount();
      let listitem = [];
      listitem = this.selectAll;
      let checkeditem = listitem.slice(this.started, this.ended)

      checkeditem.forEach((element: any) => {
        this.selectedData.filter((x: any) => {
          if (x.id == element.id) {
            x.ischecked = false;
            element.ischecked = false;
          }
        })
      })
      // checkeditem.forEach((element: any) => {
      //   element.ischecked = false;
      // })
      const emp = [true];
      this.selectedData = this.selectedData.filter(function (itm) {
        return emp.indexOf(itm.ischecked) > -1;
      });

      this.selectAllCount = this.selectedData.length
    }
  }

  select(event: any) {
    // this.isShowContact = false;
  }

  isAllSelected() {

    this.iterateArray();

    // this.getCheckedItemList();

  }

  // -----for pagination content show -------


  // page count for array slice
  started: number

  ended: number

  pageCount() {
    if (this.total == 0) {
      this.started = ((this.p - 1) * this.itemsPerPage);
    }
    else if (this.total < this.itemsPerPage) {
      this.started = (this.total * (this.p - 1));
      var end = this.total;
      this.ended = end < this.total ? end : this.total;
    }
    else {
      this.started = ((this.p - 1) * this.itemsPerPage);
      var end = this.p * this.itemsPerPage;
      this.ended = end < this.total ? end : this.total;
    }
  }

  // iterate array to check checked property
  iterateArray() {
    if (this.SearchArray.length > 0) {
      this.search.userData.subscribe((data: any) => {
        this.SearchArray = data
      });
      this.selectAll = this.SearchArray;
    } else {
      this.selectAll = this.TemplateDetail;
    }
    let listitem = []
    listitem = this.selectAll
    let item = listitem.slice(this.started, this.ended)
    if (item.length == 0) {
      this.masterSelected = false;
    } else {
      var ifcount = item.filter(
        (item: any) => item.ischecked == false);
      if (ifcount.length > 0) {
        this.masterSelected = false;
      } else {
        this.masterSelected = true;
      }
    }
  }

  editTemplate(list: any) {
    this.router.navigate(['templates/edit'],
      { queryParams: { templates: list.id } });
    this.search.setViewTemplatesId(list.id);

  }
  previewTemplate(list: any) {
    this.router.navigate(['templates/preview'],
      { queryParams: { previewTemplates: list.id } });
    this.search.setViewTemplatesId(list.id);

  }

  backEnter(event: any, list: any) {
    if (event.keyCode == 13) {
      this.previewTemplate(list)
    }
  }

  onDeletePreviewResp(response: string, list: any) {
    if (response === "yes") {
      this.listDataForRetry = list;
      this.deletingSpinner = true;
      const params: any = {}
      this.deleteIds = [];
      this.deleteIds.push(list.id)
      params.ids = this.deleteIds;
      this.deleteApiCall(params);
      // this.template.deleteSingleTemplate(params)
      //   .subscribe((data: any) => {
      //     this.showhide = true;
      //     this.deletingSpinner = false;

      //     if (data.statusCode == 200) {
          
      //       this.deleteResponse = data;
      //       this.popUp = true;
      //       deleteIds.forEach((dele: any) => {

      //         this.selectedData.filter((x: any) => x.id === dele)
      //           .forEach((x: any) => {
      //             this.selectedData.splice(this.selectedData.indexOf(x), 1);
      //           });
      //         this.SearchArray.filter((x: any) => x.id === dele)
      //           .forEach((x: any) => {
      //             this.SearchArray.splice(this.SearchArray.indexOf(x), 1)

      //             if (this.SearchArray.length == 0) {
      //               this.searchText = "";
      //             }
      //           });
      //       });
      //       this.selectAllCount = this.selectedData.length;

      //       this.subscribeData();
      //     }

      //   },
      //     (error: HttpErrorResponse) => {
      //       this.showhide = true;
      //       this.deletingSpinner = false;
      //       this.deleteResponse = this.template.badError;
      //       this.popUp = true;
      //       //this.apiError = true;
      //     }
      //   )

    }
    if (response === "no") {
      //no  
    }
  }

  temp = [];
  ontryAgain() {
    this.onDeleteAllPreviewResp("yes");
  }


deleteIds = [];
  onDeleteAllPreviewResp(response: string) {
    if (response === "yes") {
      this.temp = this.selectedData;
      this.deletingSpinner = true;
      // this.showhide = true;
      const params: any = {}
      this.deleteIds= [];
      this.selectedData.forEach((element: any) => {
        this.deleteIds.push(element.id)
      });

      params.ids = this.deleteIds;
      this.deleteApiCall(params);

     
    }
    if (response === "no") {
      // nothing 
      this.showDeleteModal = false;
    }
  }

 // deleting list data 
  deleteApiCall(params){
    this.template.deleteSingleTemplate(params)
    .subscribe((data: any) => {
      this.showhide = true;
      this.deletingSpinner = false;
      //if (data.statusCode == 200) {
      this.deleteResponse = data;
      this.popUp = true;
      // this.collection.forEach((element: any, index: number) => {
      //   this.deleteIds.forEach((deleteIds: any) => {
      //     if (element.id === deleteIds.id)
      //       this.collection.splice(index, 1);
      //   });
      // });
      this.deleteIds.forEach((dele: any) => {
        this.selectedData.filter((x: any) => x.id === dele)
          .forEach((x: any) => {
            this.selectedData.splice(this.selectedData.indexOf(x), 1);
          });
        // });
        // deleteIds.forEach((dele: any) => {
        this.SearchArray.filter((x: any) => x.id === dele)
          .forEach((x: any) => {
            this.SearchArray.splice(this.SearchArray.indexOf(x), 1)

          });
        if (this.SearchArray.length == 0) {
          this.searchText = "";
        }
      });
      this.selectAllCount = this.selectedData.length;
      this.subscribeData();
      this.temp = [];
     //}
     },
      (error: HttpErrorResponse) => {
        this.showhide = true;
        this.deletingSpinner = false;
        // let err = {
        //   statusCode : error.status,
        //   message : error.message
        // }
        this.deleteResponse = this.template.badError;
        this.popUp = true;
        //this.apiError = true;
      })
  }
  onShowResp(event) {
    this.showhide = false;
  }

  onRefresh() {
    this.searchText = "";
    this.searchedData = this.searchText;
    this.recordCount = false;
    this.dataValue = [];
    this.selectedData = [];
    this.collection = [];
    this.masterSelected = false;

    this.selectAllCount = 0;

    this.subscribeData();
  }

  ReceivedpaginateValue(event) {
    this.p = event;
    this.pageCount();
    this.iterateArray();
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
  typeIcon = 0;
  createdTsIcon = 2;
  defaultProp = "created_ts_unix"
  createdTs = ""

  sort(event) {
    this.searchprop = event.prop;
    this.order = event.order;
  }

  iconChange(event) {
    if (event.prop == "t_name") {
      this.nameIcon = event.icon;
      this.msgIcon = 0;
      this.typeIcon = 0;
      this.createdTsIcon = 0;
    }
    else if (event.prop == "t_content") {
      this.msgIcon = event.icon;
      this.nameIcon = 0;
      this.typeIcon = 0;
      this.createdTsIcon = 0;
    }
    else if (event.prop == "t_type") {

      this.typeIcon = event.icon;
      this.nameIcon = 0;
      this.msgIcon = 0;
      this.createdTsIcon = 0;
    }
    else if (event.prop == "created_ts_unix") {
      this.createdTsIcon = event.icon;
      this.nameIcon = 0;
      this.msgIcon = 0;
      this.typeIcon = 0;
    }

  }

}
