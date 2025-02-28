import { HttpErrorResponse } from "@angular/common/http";
import { Component, EventEmitter, OnDestroy, OnInit, Output, ViewChild } from "@angular/core";
import { Router } from "@angular/router";
import { GroupsManagementService } from "../groups-management.service";
import { SearchService } from "../groups.search.service";
import { GroupModel } from "../groupsMangement.group.model";
import { CommonService } from "src/app/shared/commonservice";
import { LocalStorageService } from "src/app/authentication/local-storage.service";
import { SorterComponent } from "src/app/shared/components/sorter/sorter.component";
import { CONSTANTS, value } from "src/app/shared/campaigns.constants";

@Component({
  selector: "app-group-list",
  templateUrl: "./group-list.component.html",
  styleUrls: ["./group-list.component.css"]
})
export class GroupListComponent implements OnInit, OnDestroy {

  @Output() deleteCount = new EventEmitter<number>();


  term: string;

  globalSelect: any = [];

  public searchText: any = "";

  TemplateDetail: GroupModel[] = []

  total: number = this.TemplateDetail.length;

  totalRecord: number = this.TemplateDetail.length;

  p: number = 1;

  pagesize: number;

  itemsPerPage: number = 10;

  collection: any = [];

  EmptyArray: any = [];

  recordCount: boolean = false;

  selectedTemplate: GroupModel[] = [];

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

  deleteMessage = "Are you sure to delete this group?";

  globalDeleteMessage = 'Are you sure to delete selected groups?';

  skeletonloader = true;

  SearchArray: any = [];

  SearchResultCount: number;

  selectAllCount: number = 0;

  grouploading = false;

  userDetail: any;

  userZone: string = "";

  selectedData: any = [];

  myOptions = value;

  createdDateCol = "Created Date(" + this.userZone + ")"



  loading = this.template.groupLoading.subscribe((data) => { this.grouploading = data })
  deletingSpinner: boolean;
  showhide: boolean;

  constructor(public search: SearchService,

    public router: Router,
    private commonservice: CommonService,
    public template: GroupsManagementService,
    private localStorage: LocalStorageService) {

  }
  ngOnDestroy(): void {
    if (this.loading) {
      this.loading.unsubscribe();
    }
  }

  nameOrder = "";
  searchprop: any = "";
  groupNameIcon = 0;
  totalIcon = 0;
  groupTypeIcon = 0;
  createdTsIcon = 2;
  defaultProp = "created_ts_unix"

  sort(event) {
    this.searchprop = event.prop;
    this.nameOrder = event.order;
  }

  iconChange(event) {
    if (event.prop == "total") {
      this.totalIcon = event.icon;
      this.groupNameIcon = 0;
      this.groupTypeIcon = 0;
      this.createdTsIcon = 0;
    }
    else if (event.prop == "g_name") {
      this.groupNameIcon = event.icon;
      this.totalIcon = 0;
      this.groupTypeIcon = 0;
      this.createdTsIcon = 0;
    }
    else if (event.prop == "g_type") {
      this.groupTypeIcon = event.icon;
      this.groupNameIcon = 0;
      this.totalIcon = 0;
      this.createdTsIcon = 0;
    }
    else if (event.prop == "created_ts_unix") {
      this.createdTsIcon = event.icon;
      //     this.groupNameIcon = 0;
      this.totalIcon = 0;
      this.groupTypeIcon = 0;
    }

  }


  getcount() {
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
    //need to refresh token?? what are these localstorage data in groups, can this be editted and misused??
    const user = this.localStorage.getLocal("user");
    this.userDetail = JSON.parse(user);

    this.userZone = this.commonservice.getUserData();
    this.subscribeData();
    this.createdDateCol = "Created Date(" + this.userZone + ")"
  }

  refresh(): void {
    this.subscribeData();
    this.router.navigate(['templates/list'])
  }

  subscribeData() {
    this.apiError = false;
    this.grouploading = false;
    this.noRecords = false;
    this.template.findAllGroups()
      .subscribe(
        (res: any) => {
          if (res) {
            this.dataValue = res;
            this.search.searchData(res);
            this.search.searchcount(res.length);
            //  this.noRecords = true;  
            if (this.dataValue.length == 0) {
              this.noRecords = true;
            }
            if (this.selectedData.length > 0) {
              this.dataValue.forEach((dele: any) => {
                dele.checked = false;
                this.selectedData.filter((x: any) => x.id === dele.id)
                  .forEach((x: any) => {
                    dele.checked = true;
                  });
              });
              this.selectAllCount = this.selectedData.length;
              this.TemplateDetail = this.dataValue;
              this.timeOut();
              this.iterateArray();
            } else {
              this.dataValue.forEach((value: any) => {
                value.checked = false
              })
              this.TemplateDetail = this.dataValue
           

              if (this.SearchResultCount == 1) {
                this.searchText = ''
              } else if (this.searchText.length > 1) {
                //this.pageCount();
                this.getcount();
              }
              this.timeOut();
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

  // timeout
  timeOut() {
    setTimeout(() => {
      if (this.TemplateDetail.length == 0) {
        this.total = 0;
        this.totalRecord = this.TemplateDetail.length;
        this.p = 1;
        this.perpageCount = 0;
        this.masterSelected = false;
        this.collection = this.TemplateDetail;
      } else if (this.searchText.length > 1) {
      
        
        this.search.userData.subscribe((data: any) => {
          this.SearchArray = data;
        });
        this.total = this.SearchArray.length;
       
        this.p = 1;
        this.itemsPerPage = this.perpageCount;
        this.totalRecord = this.SearchArray.length;
        this.collection = this.TemplateDetail;
      } else {
        this.total = this.TemplateDetail.length;
        this.p = 1;
        this.itemsPerPage = this.perpageCount;
        this.collection = this.TemplateDetail;
        this.totalRecord = this.TemplateDetail.length;
      }
    }, 100);
  }


  //  manual check/uncheck item list 
  singleSelect(template: GroupModel, value: any) {
    this.pageCount();
    if (value.target.checked) {
      template.checked = true;
      if (this.selectedData.length > 0) {
        this.toCheckAlreadyCheked(template, value)
      } else {
        this.selectedData.push(template)
        this.selectAllCount = this.selectedData.length
      }
    }
    else {
      template.checked = false;
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
      //  this.RecordArray = this.checkedItem;
      this.selectAllCount = this.selectedData.length;
    } else {
      let listitem = []
      listitem = this.selectedData
      var uncheckArray = listitem.filter(
        (item: any) => item.id != datavalue.id);
      this.selectedData = uncheckArray
      // this.RecordArray = this.checkedItem
      this.selectAllCount = this.selectedData.length
    }
  }
  // //  manual check/uncheck item list 
  //   selectTemplate(template:GroupModel,value :any){
  //   this.pageCount();
  //     if(value.target.checked){    
  //       template.checked=true;
  //       if (this.checkedItem.length > 0) {
  //         this.manualCheckUncheck(template,value)
  //       }else {
  //         this.checkedItem.push(template)
  //         this.RecordArray = this.checkedItem

  //       this.selectAllCount = this.RecordArray.length 
  //      } 
  //     }
  //     else{
  //       template.checked=false;
  //       if (this.checkedItem.length > 0) {

  //         this.manualCheckUncheck(template,value)
  //       }else{
  //         let i=this.checkedItem.map((item:any)  =>item).indexOf(template)
  //         this.checkedItem.splice(i,1)

  //         this.RecordArray = this.checkedItem;

  //         this.selectAllCount = this.RecordArray.length 
  //        }
  //     }
  //     this.isAllSelected();
  //   }

  // // to check item already checked 
  // manualCheckUncheck(data :any,value:any){ 
  //   this.isAllSelected();
  //   if (value.target.checked) {
  //     let listitem =[]
  //     listitem = this.checkedItem
  //     var uncheckArray = listitem.filter(
  //       (item:any) => item.id == data.id);

  //       if (uncheckArray.length == 0) {
  //         this.checkedItem.push(data)
  //       }
  //       this.RecordArray = this.checkedItem
  //       this.selectAllCount = this.RecordArray.length 
  //   }  else {
  //     let listitem =[]
  //     listitem = this.checkedItem
  //     var uncheckArray = listitem.filter(
  //       (item:any) => item.id != data.id);
  //       this.checkedItem = uncheckArray
  //       this.RecordArray = this.checkedItem

  //       this.selectAllCount = this.RecordArray.length 
  //   } 

  //  }

  //  select all event
  selectall(event: any) {

    this.pageCount();


    if (this.SearchArray.length > 0) {
      this.selectAll = this.SearchArray;
    } else {

      this.selectAll = this.TemplateDetail;
    }
    if (event.target.checked) {

      this.masterSelected = true

      let checkeditem = this.selectAll.slice(this.started, this.ended)
      checkeditem.forEach((element: any) => {
        if (element.status == "completed") {
          element.checked = true;
          if (!this.selectedData.some((el: any) => el.id === element.id)) {
            this.selectedData.push(element)
          }
        }
      })
      // this.RecordArray = this.checkedItem
      this.selectAllCount = this.selectedData.length
    } else {
      this.pageCount();
      // if (this.SearchArray.length > 0) {
      //   this.selectAll = this.SearchArray
      // } else {
      //   this.selectAll = this.TemplateDetail
      // }
      let listitem = [];
      listitem = this.selectAll
      let checkeditem = listitem.slice(this.started, this.ended)
      checkeditem.forEach((element: any) => {
        this.selectedData.filter((x: any) => {
          if (x.id == element.id) {
            x.checked = false;
            element.checked = false;
          }
        })
      })
      //     checkeditem.forEach((element: any)=>{
      //       element.checked=false;
      //  })
      const emp = [true];
      this.selectedData = this.selectedData.filter(function (itm) {
        return emp.indexOf(itm.checked) > -1;
      });
      //  this.RecordArray = this.checkedItem;       
      this.selectAllCount = this.selectedData.length
    }
  }

  select(event: any) {
    // this.isShowContact = false;
  }

  // checkUncheckAll() {
  //   for (var i = 0; i < this.RecordArray.length; i++) {
  //     this.RecordArray[i].checked = this.masterSelected;
  //   }
  //   this.getCheckedItemList();
  // }
  isAllSelected() {

    this.iterateArray();

    //  this.getCheckedItemList();

  }

  // getCheckedItemList(){
  //   this.checkedList = [];
  //   for (var i = 0; i < this.RecordArray.length; i++) {
  //     if(this.RecordArray[i].checked)
  //     this.checkedList.push(this.RecordArray[i]);
  //   }
  //   this.checkedList = JSON.stringify(this.checkedList);    
  // }

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
        (item: any) => item.checked == false);
      if (ifcount.length > 0) {
        this.masterSelected = false;
      } else {
        this.masterSelected = true;
      }
    }

  }


  editTemplate(list: any) {
    // this.router.navigate(['templates/edit'+'/'+ list.id]);
    this.router.navigate(['templates/edit'],
      { queryParams: { templates: list.id } });
    this.search.setViewTemplatesId(list.id);

  }
  previewTemplate(list: any) {
    //  this.router.navigate(['templates/preview'+'/'+ list.id]);
    this.router.navigate(['templates/preview'],
      { queryParams: { previewTemplates: list.id } });
    this.search.setViewTemplatesId(list.id);

  }

  onDeletePreviewResp(response: string, list: any) {
    let API_response: any
    if (response === "yes") {
      this.deletingSpinner = true;
      const params: any = {}
      let deleteIds = [];
      deleteIds.push(list.id)
      params.ids = deleteIds;
      this.template.deleteGroup(deleteIds)
        .subscribe((data: any) => {

          this.deletingSpinner = false;
          this.showhide = true;
          this.deleteResponse = data
          API_response = data
          if (data.statusCode == 200) {
            this.popUp = true;
            deleteIds.forEach((dele: any) => {
              this.selectedData.filter((x: any) => x.id === dele)
                .forEach((x: any) => {
                  this.selectedData.splice(this.selectedData.indexOf(x), 1);
                });
            });
            deleteIds.forEach((dele: any) => {
              this.SearchArray.filter((x: any) => x.id === dele)
                .forEach((x: any) => {
                  this.SearchArray.splice(this.SearchArray.indexOf(x), 1)

                });
              if (this.SearchArray.length == 0) {
                this.searchText = ""
              }
            });




            this.selectAllCount = this.selectedData.length;
            this.subscribeData();
          }
          else if (API_response.statusCode == -401) {
            this.popUp = true;
          }
        },
          (error: HttpErrorResponse) => {
            this.popUp = true;
            this.deletingSpinner = false;
            this.showhide = true;
            this.deleteResponse = this.template.badError;
          })
    }
    if (response === "no") {
      //no  
    }
  }

  deleteResponse: any;
  popUp = false
  onDeleteAllPreviewResp(response: string) {
    let API_response: any
    if (response === "yes") {
      // this.showhide = true;
      this.deletingSpinner = true;
      const params: any = {}
      let deleteIds: any = []
      this.selectedData.forEach((element: any) => {
        deleteIds.push(element.id)
      });

      params.ids = deleteIds
      this.template.deleteGroup(deleteIds)
        .subscribe((data: any) => {
          this.showhide = true;
          this.deletingSpinner = false;
          this.deleteResponse = data;
          this.popUp = true;
          API_response = data
          if (API_response.statusCode == 200) {
            this.collection.forEach((element: any, index: number) => {
              deleteIds.forEach((deleteIds: any) => {
                if (element.id === deleteIds.id)
                  this.collection.splice(index, 1);
              });
            });
            deleteIds.forEach((dele: any) => {
              this.selectedData.filter((x: any) => x.id === dele)
                .forEach((x: any) => {
                  this.selectedData.splice(this.selectedData.indexOf(x), 1);
                });
            });
            deleteIds.forEach((dele: any) => {
              this.SearchArray.filter((x: any) => x.id === dele)
                .forEach((x: any) => {
                  this.SearchArray.splice(this.SearchArray.indexOf(x), 1)

                });


              if (this.SearchArray.length == 0) {
                this.searchText = "";
              }
            });

            this.selectAllCount = this.selectedData.length;

            // this.refresh();
            this.subscribeData();
          }
          else if (API_response.statusCode == -401) {
            this.popUp = true;
          }

        },
          (err: HttpErrorResponse) => {
            this.popUp = true;
            this.deletingSpinner = false;
            this.showhide = true;
            this.deleteResponse = this.template.badError;
          })

    }
    if (response === "no") {
      // nothing 
    }
  }

  onShowResp(event) {
    this.showhide = false;
  }

  backEnter(event: any, group: any) {
    if (event.keyCode == 13) {
      this.contactList(group);
    }
  }

  contactList(group: any) {
    this.router.navigate(['groups/groupcontacts'])
    this.template.editingGroup = group
    localStorage.setItem("editGroup", JSON.stringify(group.id))

  }
  editGroup(group: GroupModel) {
    localStorage.removeItem("editGroup")
    this.router.navigate(['groups/editgroup'])
    this.template.editingGroup = group.id
    localStorage.setItem("editGroup", JSON.stringify(group.id))

  }

  onRefresh() {
    //  this.checkedItem = [];
    this.selectedData = [];
    this.collection = [];
    this.searchText = "";
    this.recordCount = false;
    this.masterSelected = false;
    this.selectAllCount = 0;
    this.subscribeData();
  }
  closeDeletePopup(event) {
    this.getcount();
    this.popUp = false;
  }

  tryAgainOnPopup(event) {

  }
  continueDeletePopup(event) {
    this.getcount();
    this.popUp = false
  }

  ReceivedpaginateValue(event) {
    this.p = event;
    this.pageCount();
    this.iterateArray();
  }
}
