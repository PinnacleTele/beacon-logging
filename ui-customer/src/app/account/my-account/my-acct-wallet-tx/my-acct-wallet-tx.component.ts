import { getCurrencySymbol } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { LocalStorageService } from 'src/app/authentication/local-storage.service';
import { CONSTANTS } from 'src/app/shared/campaigns.constants';
import { ACCT_CONSTANTS } from '../../account.constants';
import { EditAccountService } from '../../edit-account/edit-account.service';
import { WalletTrModel } from '../../shared/accountHelper/walletTx.model';
import { MyWalletService } from '../my-wallet.service';

@Component({
  selector: 'app-my-acct-wallet-tx',
  templateUrl: './my-acct-wallet-tx.component.html',
  styleUrls: ['./my-acct-wallet-tx.component.css']
})
export class MyAcctWalletTxComponent implements OnInit {
  txList: WalletTrModel[] = [];
  usTxList :any = [];

  searchResultCount: any;
  showClearModal = false;
  showCancelModal = false;
  total: number = this.txList.length;
  noData = false;
  NDHeadContent = ACCT_CONSTANTS.zeroTxHeadContent;
  NDMessageCOntent = ACCT_CONSTANTS.zeroTxMessageCOntent;
  currencyType = CONSTANTS.currency;
  currencyFormat = CONSTANTS.curencyFormat

  totalRecord: number = this.txList.length;

  p: number = 1;

  pagesize: number;

  itemsPerPage: number = 10;

  perpageCount: number = 10;

  userSearchResultCount: boolean = false;

  ustotal: number = this.txList.length;

  usp: number = 1;

  uspagesize: number;

  usitemsPerPage: number = 10;

  ustotalRecord: number = this.usTxList.length;

  usperpageCount: number = 10;
  usSearchText :any ;
  usApiError :boolean = false
  usTxLoading :boolean = false;
  usNoData :boolean = false;
  ustxCount = 0;
  usdisplayRange :any="";

  txCount = 0;
  dateSelection: any;
  userDateSelection:any;
  displayRange = "";
  public searchText: any = "";
  txLoading = false;
  apiError: boolean = false;
  zone = "";
  order = "";
  userSearchprop = "";
  userOrder = "";
  searchprop: any = "";

  usernameIcon = 0;
  prev_balanceIcon = 0;
  new_balanceIcon = 0;
  amountIcon = 0;
  detailsIcon = 0;
  actionIcon = 0;
  txDateIcon = 2;


  yourBalanceIcon = 0;
  userBalanceIcon = 0;
      usamountIcon = 0;
      usactionIcon = 0;
      usdetailsIcon = 0;
      ustxDateIcon = 2;

  defaultProp = "created_ts_unix";

  currency: any;

  currencySymbol: any;

  userType:any;

  constructor(private myWalletService: MyWalletService,
    private localStorage: LocalStorageService,
    private editAcctService: EditAccountService) { }

  ngOnInit(): void {
    this.currency = this.editAcctService.getCurrencyType();
    this.currencySymbol = getCurrencySymbol(this.currency, "narrow");
    //need to refresh token??s
    const user = this.localStorage.getLocalValue();

    this.userType = user.user_type;

    this.dateSelection = {
      dateselectiontype: "this week",
      q:'lu',
      fdate: "",
      tdate: ""
    }

    this.userDateSelection = {
      dateselectiontype: "this week",
      q:'u',
      fdate: "",
      tdate: ""
    }
    this.displayRange = "this week";
    this.usdisplayRange = "this week";

    this.subscribeData();
    this.userSubscribeData();
    this.getZone();

  }


  sort(event) {
    this.searchprop = event.prop;
    this.order = event.order;
  }

  usersort(event) {
    this.userSearchprop = event.prop;
    this.userOrder = event.order;
  }

  getCurrencySym(event){
   return getCurrencySymbol(event, "narrow");
  }

  iconChange(event) {
    if (event.prop == "new_bal") {
      this.new_balanceIcon = event.icon;
      this.amountIcon = 0;
      this.actionIcon = 0;
      this.detailsIcon = 0;
      this.txDateIcon = 0;
      this.prev_balanceIcon = 0;
    }  else if (event.prop == "old_bal") {
      this.new_balanceIcon = 0;
      this.prev_balanceIcon = event.icon;
      this.amountIcon = 0;
      this.actionIcon = 0;
      this.detailsIcon = 0;
      this.txDateIcon = 0;
    }
    else if (event.prop == "created_ts_unix") {
      this.new_balanceIcon = 0;
      this.prev_balanceIcon = 0;
      this.amountIcon = 0;
      this.actionIcon = 0;
      this.detailsIcon = 0;
      this.txDateIcon = event.icon;
    }
    else if (event.prop == "amount") {
      this.new_balanceIcon = 0;
      this.prev_balanceIcon = 0;
      this.amountIcon = event.icon;
      this.actionIcon = 0;
      this.detailsIcon = 0;
      this.txDateIcon = 0;
    }
    else if (event.prop == "description") {
      this.new_balanceIcon = 0;
      this.prev_balanceIcon = 0;
      this.amountIcon = 0;
      this.actionIcon = 0;
      this.detailsIcon = event.icon;
      this.txDateIcon = 0;
    }
    else if (event.prop == "action") {
      this.new_balanceIcon = 0;
      this.prev_balanceIcon = 0;
      this.amountIcon = 0;
      this.actionIcon = event.icon;
      this.detailsIcon = 0;
      this.txDateIcon = 0;
    }

  }

  userIconChange(event) {
    console.log(event);
    
    if (event.prop == "new_bal") {
      console.log('inside');
      
      this.yourBalanceIcon = 0;
      this.userBalanceIcon = event.icon;
      this.usamountIcon = 0;
      this.usactionIcon = 0;
      this.usdetailsIcon = 0;
      this.ustxDateIcon = 0;
      this.usernameIcon = 0;
    }  else if (event.prop == "loggedin_bal_after") {
      console.log('insde log');
      
      this.yourBalanceIcon = event.icon;
      this.userBalanceIcon = 0;
      this.usamountIcon = 0;
      this.usactionIcon = 0;
      this.usdetailsIcon = 0;
      this.ustxDateIcon = 0;
      this.usernameIcon = 0;
    }
    else if (event.prop == "username") {
      this.yourBalanceIcon = 0;
      this.userBalanceIcon = 0;
      this.usamountIcon = 0;
      this.usactionIcon = 0;
      this.usdetailsIcon = 0;
      this.ustxDateIcon = 0;
      this.usernameIcon =  event.icon;
    }
    else if (event.prop == "created_ts_unix") {
      this.yourBalanceIcon = 0;
      this.userBalanceIcon =  0;
      this.usamountIcon = 0;
      this.usactionIcon = 0;
      this.usdetailsIcon = 0;
      this.ustxDateIcon = event.icon;
      this.usernameIcon = 0;
    }
    else if (event.prop == "amount") {
      this.yourBalanceIcon = 0;
      this.userBalanceIcon = 0;
      this.usamountIcon =  event.icon;
      this.usactionIcon = 0;
      this.usdetailsIcon = 0;
      this.ustxDateIcon = 0;
      this.usernameIcon = 0;
    }
    else if (event.prop == "description") {
      this.yourBalanceIcon = 0;
      this.userBalanceIcon = 0;
      this.usamountIcon =  0;
      this.usactionIcon = 0;
      this.usdetailsIcon = event.icon;
      this.ustxDateIcon = 0;
      this.usernameIcon = 0;
    }
    else if (event.prop == "action") {
      this.yourBalanceIcon = 0;
      this.userBalanceIcon = 0;
      this.usamountIcon =  0;
      this.usactionIcon =  event.icon;
      this.usdetailsIcon = 0;
      this.ustxDateIcon = 0;
      this.usernameIcon = 0;
    }

  }



  getcount() {

    if (this.searchText.length == 0) {
      this.searchResultCount = 1;
      this.total = this.txList.length;
      this.p = 1;
      this.itemsPerPage = this.perpageCount;
    } else {

      if (this.myWalletService.totalRecords.length >= 0) {
        var count = this.myWalletService.totalRecords.length
        if (count == 0) {
          this.searchResultCount = 0;
          this.total = 0;
          this.p = 1;
          this.perpageCount = 10;
        } else {
          this.searchResultCount = 1;
          this.total = count;
          this.p = 1;
          this.perpageCount = 10;
          // this.SearchArray = this.search.totalRecords ;


        }
      }
    }
  }

  userGetcount() {

    if (this.usSearchText.length == 0) {
      this.userSearchResultCount = false;
      this.ustotal = this.usTxList.length;
      this.usp = 1;
      this.usitemsPerPage = this.usperpageCount;
    } else {

      if (this.myWalletService.totalUserRecords.length >= 0) {
        var count = this.myWalletService.totalUserRecords.length
        if (count == 0) {
          this.userSearchResultCount = true;
          this.ustotal = 0;
          this.usp = 1;
          this.usperpageCount = 10;
        } else {
          this.userSearchResultCount = false;
          this.ustotal = count;
          this.usp = 1;
          this.usperpageCount = 10;
          // this.SearchArray = this.search.totalRecords ;
        }
      }
    }
  }

  OnSubmitClicked() {
    this.displayRange = this.dateSelection.dateselectiontype;
    this.searchResultCount = 1;

    this.subscribeData();
  }

  OnSubmitUserClicked() {
    this.usdisplayRange = this.userDateSelection.dateselectiontype;
    this.userSearchResultCount = false;

    this.userSubscribeData();
  }
  ReceivedpaginateValue(event) {
    this.p = event;

  }
  ReceivedUserPaginateValue(event) {
    this.usp = event;

  }
  getZone() {
    //need to refresh token??
    const user = this.localStorage.getLocal('user');
    let userObj = null;

    if (user) {
      userObj = JSON.parse(user);
    }

    this.zone = userObj.zone_abbr;
  }

  subscribeData() {
    this.searchText = ""
    this.apiError = false
    this.txLoading = true;
    this.txList = [];
    this.noData = false;
    this.myWalletService.getAllWalletTx(this.dateSelection)
      .subscribe(
        (res: any) => {
          if (res) {
            this.txLoading = false;
            this.txList = res;


            if (this.txList.length == 0) {
              this.noData = true;
            }

            this.txCount = this.txList.length;
            // console.log(this.txList);

            this.paginationValue(this.txCount)
          }
          this.txLoading = false;
        },
        (error: any) => {

          this.apiError = true;
          this.noData = false;
          this.txLoading = false;
        }

      );

  }
  userSubscribeData() {
    this.usSearchText = ""
    this.usApiError = false
    this.usTxLoading = true;
    this.usTxList = [];
    this.usNoData = false;
    this.myWalletService.getAllWalletTx(this.userDateSelection)
      .subscribe(
        (res: any) => {
          if (res) {
            this.usTxLoading = false;
            this.usTxList = res;
            console.log(res);
            


            if (this.usTxList.length == 0) {
              this.usNoData = true;
            }

            this.ustxCount = this.usTxList.length;
            // console.log(this.txList);

            this.userPaginationValue(this.ustxCount)
          }
          this.usTxLoading = false;
        },
        (error: any) => {

          this.usApiError = true;
          this.usNoData = false;
          this.usTxLoading = false;
        }

      );

  }
  paginationValue(tableList: any) {
    if (tableList == 0) {
      this.total = 0;
      this.p = 1;
      this.perpageCount = 0;
    } else {
      this.total = tableList;
      this.p = 1;
      this.perpageCount = this.itemsPerPage;
    }
  }
  userPaginationValue(usertableList: any) {
    if (usertableList == 0) {
      this.ustotal = 0;
      this.usp = 1;
      this.usperpageCount = 0;
    } else {
      this.ustotal = usertableList;
      this.usp = 1;
      this.usperpageCount = this.usitemsPerPage;
    }
  }
  receivedDateSelection(event) {

    this.dateSelection = {
      q : 'lu',
      dateselectiontype: event.dateselectiontype,
      fdate: event.fdate,
      tdate: event.tdate
    }

  }
  receivedUserDateSelection(event) {

    this.userDateSelection = {
      q : 'u',
      dateselectiontype: event.dateselectiontype,
      fdate: event.fdate,
      tdate: event.tdate
    }

  }
}
