import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { AccountsService } from "../shared/accountHelper/accounts.service";
import { Router } from '@angular/router';
import { AccountModel } from "src/app/account/shared/accountHelper/list.model";
import { CommonService } from 'src/app/shared/commonservice';
import {  value } from "src/app/shared/campaigns.constants";

@Component({
  selector: 'app-account-list',
  templateUrl: './account-list.component.html',
  styleUrls: ['./account-list.component.css']
})
export class AccountListComponent implements OnInit {
  isText: boolean = false;
  isUnicode: boolean = false;
  isShowContact: boolean = false;
  removeDuplicateChecked: boolean = false;
  showPreview: boolean = false;
  showClearModal: boolean = false;
  showCancelModal: boolean = false;

  // TemplateDetail:TemplateModel[]=  [ ]

  accountUserList: AccountModel[] = [];

  accountStat_List: any;

  total: number = this.accountUserList.length;

  totalRecord: number = this.accountUserList.length;

  p: number = 1;

  pagesize: number;

  itemsPerPage: number = 10;

  perpageCount: number = 10;

  public searchText: any = "";

  apiError: boolean = false;
  inactive: any;
  active: any;
  admin: any;
  user: any;
  accounts: number;

  noRecords: any;

  noData: boolean = false;

  loginuser: number;

  accountLoading = false;

  userZone: string = "";

  myOptions = value;


  constructor(private aservice: AccountsService,
    private router: Router,
    private commonservice: CommonService,
    ) {

  }

  ngOnInit(): void {
    this.userZone = this.commonservice.getUserData();

    const user = this.commonservice.getUserdetail();
    let userObj = null;
    let loggedInuserType: number;
   

    loggedInuserType = user.user_type;
    this.loginuser = this.commonservice.getUserdetail().user_type;
    // console.log(loggedInuserType,'usertype');

    this.subscribeData();
    this.createdTs = "CREATED DATE  (" + this.userZone + ")"

  }

  subscribeData() {
    this.searchText = ""
    this.apiError = false
    this.accountLoading = true
    this.aservice.getAccountsList()
      .subscribe(
        (res: any) => {
          if (res) {
            // this.accountUserList = [];
            let value = res;
            this.accountUserList = res;

            this.accountUserList.forEach((data: any) => {

              // data.userStatus = 'active'
              if (data.user_type == 0) {
                data.userType = 'super admin'
              } else if (data.user_type == 1) {
                data.userType = 'admin'
              } else if (data.user_type == 2) {
                data.userType = 'user'
              }

              if (data.acc_status == 0) {
                data.userStatus = 'active'
              } else if (data.acc_status == 1) {
                data.userStatus = 'suspended'
              } else if (data.acc_status == 2) {
                data.userStatus = 'deactivated'
              }
              data.fullName = data.firstname + data.lastname
            })
            this.noData = false;
            this.apiError = false;
            if (this.accountUserList.length == 0) {
              this.noData = true;
            }

            this.accounts = this.accountUserList.length;
            // console.log(this.accountUserList);

            this.paginationValue(this.accounts)
          }
          this.accountLoading = false;
        },
        (error: any) => {

          this.apiError = true;
          this.noData = false;
          this.accountLoading = false;
        }

      );

    this.aservice.getSTATS_List()
      .subscribe(
        (res: any) => {

          if (res) {

            this.accountStat_List = res;


          }
        },
        (error: HttpErrorResponse) => {

          /*  if(this.aservice.badError.statusCode == 401){
               setTimeout(()=>this.subscribeData(),1000)
            }*/
          this.apiError = true;
        }
      )
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

  onClickNewAcct() {

    this.router.navigate(["/accounts/new"])
  }
  editAccount(list: any) {
    this.router.navigate(['accounts/edit'],
      {
        queryParams: { accounts: list.cli_id }
        //, skipLocationChange: true  
      }
    );
    //this.search.setViewTemplatesId(list.id);

  }

  ReceivedpaginateValue(event) {
    this.p = event;
  }
  getcount() {

    if (this.searchText.length == 0) {
      this.noRecords = 1;
      this.total = this.accountUserList.length;
      this.p = 1;
      this.itemsPerPage = this.perpageCount;
    } else {

      if (this.aservice.totalRecords && this.aservice.totalRecords.length >= 0) {
        var count = this.aservice.totalRecords.length
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

  deleteMessage = 'Are you sure want to delete the Account';

  onDeletePreviewResp(response: string, list: any) {

    if (response === "yes") {
      const params: any = {}
      let deleteIds = []
      deleteIds.push(list.id)
      params.ids = deleteIds
      //   this.template.deleteSingleTemplate(params)
      //   .subscribe((data: any) => { 
      //  })

      this.subscribeData();



    }
    if (response === "no") {
      //no  
    }
  }
  // -----for pagination content show -------

  //     get startItem() {
  //       if (this.total == 0) {
  //        return ((this.p - 1) * this.itemsPerPage) ;
  //       }else  if (this.total < this.itemsPerPage){
  //        return this.p = 1;
  //      }else{    
  //        return ((this.p - 1) * this.itemsPerPage) + 1;
  //      }
  //    }

  //    get endItem() {
  //     //  console.log(this.total);

  //     var end = this.p * this.itemsPerPage;
  //     return end < this.total ? end : this.total;
  //   }

  //    get isFirstPage() {
  //      return this.p == 1 ? true : false;
  //    }

  //    get isLastPage() {
  //      return this.endItem == this.total ? true : false;
  //    }

  // //  next click event
  // next(event: any) {
  //   event.preventDefault();
  //    this.p += 1;
  //   // this.pageCount(); 
  //   // this.iterateArray();

  //  }

  // // -----previous click event
  //  prev(event: any) {
  //    event.preventDefault();
  //    this.p -= 1;
  //   //  this.pageCount();
  //   //  this.iterateArray();

  //  }
  closeClearModal() {
    // this.showClearModal = false;
  }

  closeCancelModal() {
    // this.showCancelModal = false;
  }
  order = "";
  searchprop: any = "";

  usernameIcon = 1;
  custnameIcon = 0;
  emailIcon = 0;
  typeIcon = 0;
  createdTsIcon = 0;

  defaultProp = "created_ts_unix"
  createdTs = ""

  sort(event) {
    this.searchprop = event.prop;
    this.order = event.order;
  }

  iconChange(event) {
    if (event.prop == "user") {
      this.usernameIcon = event.icon;
      this.custnameIcon = 0;
      this.emailIcon = 0;
      this.typeIcon = 0;
      this.createdTsIcon = 0;
    }
    else if (event.prop == "fullName") {
      this.custnameIcon = event.icon;
      this.usernameIcon = 0;
      this.emailIcon = 0;
      this.typeIcon = 0;
      this.createdTsIcon = 0;
    }
    else if (event.prop == "email") {
      this.emailIcon = event.icon;
      this.typeIcon = 0;
      this.usernameIcon = 0;
      this.custnameIcon = 0;
      this.createdTsIcon = 0;
    }
    else if (event.prop == "user_type") {
      
      this.typeIcon = event.icon;
      this.usernameIcon = 0;
      this.emailIcon = 0;
      this.custnameIcon = 0;
      this.createdTsIcon = 0;
    }
    else if (event.prop == "created_ts_unix") {
      this.createdTsIcon = event.icon;
      this.emailIcon = 0;
      this.typeIcon = 0;
      this.usernameIcon = 0;
      this.custnameIcon = 0;
    }

  }

}
