import { Component, EventEmitter, OnDestroy, OnInit, Output } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { SearchService } from "../groups.search.service"
import { contact } from '../contact.model';
import { GroupsManagementService } from '../groups-management.service';
import { HttpErrorResponse } from '@angular/common/http';
import { CONSTANTS, value} from "src/app/shared/campaigns.constants";
import { CommonService } from "src/app/shared/commonservice";

@Component({
  selector: 'app-group-contact-list',
  templateUrl: './group-contact-list.component.html',
  styleUrls: ['./group-contact-list.component.css']
})
export class GroupContactListComponent implements OnInit, OnDestroy {

  @Output() deleteCount = new EventEmitter<number>();

  public searchText: any = "";

  TemplateDetail: contact[] = []

  total: number = this.TemplateDetail.length;

  totalRecord: number = this.TemplateDetail.length;

  p: number = 1;

  pagesize: number;

  itemsPerPage: number = 10;

  collection: contact[] = [];

  recordCount: number

  selectedTemplate: contact[] = [];

  perpageCount: number = 10;

  checkListCount: number;

  selectAll: any = []

  start: any

  ending: any

  RecordArray: any = []

  checkedAllSelect: boolean = false

  checkedItem: any[] = [];

  masterSelected: boolean;

  checklist: any;

  checkedList: any;

  deleteMessage = CONSTANTS.INFO_TXT.contactDeleteMessage;

  myOptions = value;

  skeletonloader = true;

  SearchArray: any = [];

  SearchResultCount: number;

  selectAllCount: number = 0;

  group: any

  userZone: string = "";

  searchLoading = this.template.searchLoading.subscribe((data) => { this.loader = data })

  afterSearch = true;

  loader = false;

  constructor(private routes: ActivatedRoute,
    private router: Router,
    public search: SearchService,
    private commonservice: CommonService,
    private template: GroupsManagementService) {

   
    this.groupId = JSON.parse(localStorage.getItem("editGroup") + "")


  }
  ngOnDestroy(): void {
    // localStorage.removeItem("editGroup")
    if (this.searchLoading) {
      this.searchLoading.unsubscribe()
    }
  }

  getcount() {
    var count = this.TemplateDetail.length
    if (count == 0) {
      this.recordCount = 0;
      this.total = 0;
      this.p = 1;
      this.perpageCount = 10;
    } else {
      this.recordCount = 1;
      this.total = count;
      this.p = 1;
      this.perpageCount = 10;
    }
    this.pageCount();
    this.iterateArray();
  }

  dataValue: any = [];
  apiError: boolean = false;
  noRecords: boolean = false;
  groupId: string = ""
  groupType: string = ""
  groupName: string = ""
  newContacts: any = []
  totalContacts: any

  ngOnInit(): void {
    this.userZone = this.commonservice.userZone;
    this.getGroupInfo();


  }
  getGroupInfo() {
    this.template.getGroupInfo(this.groupId).subscribe((res: any) => {
      this.group = res
      this.groupType = res.g_type
      this.groupName = res.g_name
      this.subscribeData();
    }, (error: HttpErrorResponse) => {
      this.apiError = true;
    }
    )
  }
  subscribeData() {
    this.onEnter(this.searchText)
  }

  // timeout
  timeOut() {
    setTimeout(() => {
      if (this.TemplateDetail.length == 0) {
        this.masterSelected = false;
        this.total = 0;
        this.totalRecord = this.TemplateDetail.length;
        this.p = 1;
        this.perpageCount = 0;
        this.collection = this.TemplateDetail;


      }
      else if (this.searchText.length > 1) {
        this.total = this.TemplateDetail.length;
        this.p = 1;
        this.itemsPerPage = 10;
        this.collection = this.TemplateDetail;
        this.totalRecord = this.TemplateDetail.length;

      } else {
        this.total = this.TemplateDetail.length;
        this.p = 1;
        this.itemsPerPage = 10;
        this.collection = this.TemplateDetail;
        this.totalRecord = this.TemplateDetail.length;


      }
    }, 100);
  }

  //  manual check/uncheck item list 
  selectTemplate(template: contact, value: any) {
    this.pageCount();
    if (value.target.checked) {
      template.checked = true;
      if (this.checkedItem.length > 0) {
        this.manualCheckUncheck(template, value)
      } else {
        this.checkedItem.push(template)
        this.RecordArray = this.checkedItem

        this.selectAllCount = this.RecordArray.length
      }
    }
    else {
      template.checked = false;
      if (this.checkedItem.length > 0) {

        this.manualCheckUncheck(template.mobile, value)
      } else {
        let i = this.checkedItem.map((item: any) => item).indexOf(template)
        this.checkedItem.splice(i, 1)

        this.RecordArray = this.checkedItem

        this.selectAllCount = this.RecordArray.length
      }
    }
    this.isAllSelected();

  }

  // to check item already checked 
  manualCheckUncheck(data: any, value: any) {
    // this.isAllSelected();
    if (value.target.checked) {
      let listitem = []
      listitem = this.checkedItem
      var uncheckArray = listitem.filter(
        (item: any) => item.mobile == data.mobile);
      if (uncheckArray.length == 0) {
        this.checkedItem.push(data)
      }
      this.RecordArray = this.checkedItem
      this.selectAllCount = this.RecordArray.length
    } else {
      let listitem = []
      listitem = this.checkedItem
      var uncheckArray = listitem.filter(
        (item: any) => item.mobile != data);
      this.checkedItem = uncheckArray
      this.RecordArray = this.checkedItem
      this.selectAllCount = this.RecordArray.length
    }
  }

  //  select all event
  selectall(event: any) {
    this.pageCount();
    if (event.target.checked) {
      this.masterSelected = true
      this.selectAll = this.TemplateDetail
      let checkeditem = this.selectAll.slice(this.started, this.ended)
      checkeditem.forEach((element: any) => {
        element.checked = true;
        if (!this.checkedItem.some((el: any) => el.mobile === element.mobile)) {

          this.checkedItem.push(element)

        }
      })
      this.RecordArray = this.checkedItem
      this.selectAllCount = this.RecordArray.length


    } else {

      this.pageCount();
      this.selectAll = this.TemplateDetail
      let listitem = []
      listitem = this.selectAll
      let checkeditem = listitem.slice(this.started, this.ended)

      checkeditem.forEach((element: any) => {
        this.checkedItem.filter((x: any) => {
          if (x.mobile == element.mobile) {
            x.checked = false;
            element.checked = false;
          }
        })
      })
      const emp = [true];
      this.checkedItem = this.checkedItem.filter(function (itm) {
        return emp.indexOf(itm.checked) > -1;
      });
      this.RecordArray = this.checkedItem;

      this.selectAllCount = this.RecordArray.length

    }


  }

  select(event: any) {
    // this.isShowContact = false;
  }

  checkUncheckAll() {
    for (var i = 0; i < this.RecordArray.length; i++) {
      this.RecordArray[i].checked = this.masterSelected;
    }
    this.getCheckedItemList();
  }
  isAllSelected() {

    this.iterateArray();

    this.getCheckedItemList();

  }

  getCheckedItemList() {
    this.checkedList = [];
    for (var i = 0; i < this.RecordArray.length; i++) {
      if (this.RecordArray[i].checked)
        this.checkedList.push(this.RecordArray[i]);
    }
    this.checkedList = JSON.stringify(this.checkedList);

  }

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

    this.selectAll = this.TemplateDetail;
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


  onDeletePreviewResp(response: string, list: any) {
    if (response === "yes") {
      const params: any = {}
      let deleteIds = [];

      deleteIds.push(list.mobile)
      params.ids = deleteIds;
      this.template.deleteContact(this.groupId, this.groupType, deleteIds)
        .subscribe((data: any) => {
        })
      deleteIds.forEach((dele: any) => {
        this.RecordArray.filter((x: any) => x.mobile === dele)
          .forEach((x: any) => {
            this.RecordArray.splice(this.RecordArray.indexOf(x), 1);
          });
      });
      deleteIds.forEach((dele: any) => {
        this.SearchArray.filter((x: any) => x.mobile === dele)
          .forEach((x: any) => {
            this.SearchArray.splice(this.SearchArray.indexOf(x), 1)
          });
      });
      this.selectAllCount = this.RecordArray.length;
      this.subscribeData();
    }
    if (response === "no") {
      //no  
    }
  }

  onDeleteAllPreviewResp(response: string) {
    if (response === "yes") {
      const params: any = {}
      let deleteIds: any = []
      this.RecordArray.forEach((element: any) => {
        deleteIds.push(element.mobile)
      });
      params.ids = deleteIds
      this.template.deleteContact(this.groupId, this.groupType, deleteIds)
        .subscribe((data: any) => {
          this.collection.forEach((element: any, index: number) => {
            deleteIds.forEach((deleteIds: any) => {
              if (element.mobile === deleteIds.mobile)
                this.collection.splice(index, 1);
            });
          });
        })
      deleteIds.forEach((dele: any) => {
        this.RecordArray.filter((x: any) => x.mobile === dele)
          .forEach((x: any) => {
            this.RecordArray.splice(this.RecordArray.indexOf(x), 1);
          });
      });
      deleteIds.forEach((dele: any) => {
        this.SearchArray.filter((x: any) => x.mobile === dele)
          .forEach((x: any) => {
            this.SearchArray.splice(this.SearchArray.indexOf(x), 1)

          });
      });

      this.selectAllCount = this.RecordArray.length;

      this.subscribeData();
    }
    if (response === "no") {
      // nothing 
    }
  }

  editContact(contact: contact) {
    //route to contace edit along with contact id
    //add params for contact
    localStorage.setItem("editContact", JSON.stringify(this.group.id))
    this.router.navigate(['groups/editcontact'], { queryParams: {}, state: { data: contact } })

  }
  onAddContacts() {
    if (this.group.status == "completed") {


      localStorage.setItem("editContact", JSON.stringify(this.group.id))
      this.router.navigate(['groups/addcontacts'], { queryParams: {}, state: { data: this.group } })
    }

  }
  selectContact(contact: contact, value: any) {

    this.pageCount();
    if (value.target.checked) {

      contact.checked = true;
      if (this.checkedItem.length > 0) {

        this.manualCheckUncheck(contact, value)

      } else {

        this.checkedItem.push(contact)
        this.RecordArray = this.checkedItem

        this.selectAllCount = this.RecordArray.length
      }
    }
    else {


      contact.checked = false;
      if (this.checkedItem.length > 0) {

        this.manualCheckUncheck(contact.mobile, value)
      } else {
        let i = this.checkedItem.map((item: any) => item).indexOf(contact)
        this.checkedItem.splice(i, 1)

        this.RecordArray = this.checkedItem

        this.selectAllCount = this.RecordArray.length
      }
    }

  }
  deleteMsgGroup = "Are you sure to delete selected contacts ?"
  deleteMsgSingle = "Are you sure to delete this contact ?"

  onEnter(value?: string) {

    this.template.findAllContactList(this.groupId, this.groupType, value)
      .subscribe(
        (res: any) => {
          if (res) {
            this.dataValue = res.data;
            this.SearchArray = res.data;
            this.totalContacts = res.total;
            this.noRecords = false;
            if (this.dataValue.length == 0) {
              this.noRecords = true;
            }
            if (this.RecordArray.length > 0) {
              this.dataValue.forEach((dele: any) => {
                dele.checked = false;
                this.RecordArray.filter((x: any) => x.mobile === dele.mobile)
                  .forEach((x: any) => {
                    dele.checked = true;
                  });
                // 
              });
              this.selectAllCount = this.RecordArray.length;
              this.TemplateDetail = this.dataValue;
              this.timeOut();
              this.pageCount()
            } else {
              this.dataValue.forEach((value: any) => {
                value.checked = false
              })
              this.TemplateDetail = this.dataValue
              this.timeOut();
              if (this.SearchResultCount == 1) {
                this.searchText = ''
              } else if (this.searchText.length > 1) {
                this.pageCount();
              }
            }
            this.getcount();
            this.skeletonloader = false;
            this.apiError = false;
            this.afterSearch = value ? false : true;
          }
        },
        (error: HttpErrorResponse) => {
          this.apiError = true;
        })
  }




  clearSearch() {
    this.searchText = ""
    this.subscribeData();
  }

  resetValue() {
    this.checkedItem = [];
    this.RecordArray = [];
    this.collection = [];
    this.searchText = "";
    this.recordCount = 1;
    this.masterSelected = false;
    this.selectAllCount = 0;
  }

  onRefresh() {
    this.resetValue();
    // this.router.navigate(["/groups/groupcontacts"])
    this.template.getGroupInfo(this.groupId).subscribe((res: any) => {
      this.group = res
      this.groupType = res.g_type
      this.groupName = res.g_name

      this.template.findAllContactList(this.groupId, this.groupType, "*")
        .subscribe(
          (res: any) => {
            if (res) {
              this.dataValue = res.data;
              this.SearchArray = res.data;
              this.totalContacts = res.total;
              if (this.dataValue.length == 0) {
                this.noRecords = true;
              }
              else {
                this.noRecords = false;
              }
              this.dataValue.forEach((value: any) => {
                value.checked = false
              })
              this.TemplateDetail = this.dataValue
              this.timeOut();
              this.pageCount();
              this.getcount();
              this.skeletonloader = false;
              this.apiError = false;
            }
          },
          (error: HttpErrorResponse) => {
            this.apiError = true;
          }
        )
    }, (error: HttpErrorResponse) => {
      this.apiError = true;
    })
  }

  ReceivedpaginateValue(event) {
    this.p = event;
    this.pageCount();
    this.iterateArray();
  }

  // enter to select 

  routing(routepath: string) {
    this.router.navigate([routepath])
  }



  Order = "";
  searchprop: any = "";
  nameIcon = 0;
  mobileIcon = 1;
  emailIcon = 0;
  defaultProperty = "mobile"

  sort(event) {
    
    this.searchprop = event.prop;
    this.Order = event.order;
  }

  iconChange(event) {
    if (event.prop == "mobile") {
      this.mobileIcon = event.icon;
      this.nameIcon = 0;
      this.emailIcon = 0;

    }
    else if (event.prop == "name") {
      this.nameIcon = event.icon;
      this.mobileIcon = 0;
      this.emailIcon = 0;
    }
    else if (event.prop == "email") {
      

      this.emailIcon = event.icon;
      this.nameIcon = 0;
      this.mobileIcon = 0;
    }
  }
//   myOptions= {
//     'theme': "light"
// }
}