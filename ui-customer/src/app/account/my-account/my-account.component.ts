import { getCurrencySymbol } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { LocalStorageService } from 'src/app/authentication/local-storage.service';
import { GroupModel } from 'src/app/campaigns/model/campaign-group-model';
import { CommonService } from 'src/app/shared/commonservice';

import { ACCT_CONSTANTS } from '../account.constants';
import { EditAccountService } from '../edit-account/edit-account.service';
import { MsgSettings } from '../edit-account/model/msg-settings';
import { PersonalInfo } from '../edit-account/model/personal-info';


@Component({
  selector: 'app-my-account',
  templateUrl: './my-account.component.html',
  styleUrls: ['./my-account.component.css']
})
export class MyAccountComponent implements OnInit, OnDestroy {
  accountId: number;
  apiError: boolean = false;
  templatescount = 0;
  groupsCount = 0;
  accountsCount = 0;
  //resObj : any;
  linkIndex = 1;
  spinner = false;
  email = "";
  billType: number;
  cliId = "";
  mobile: number;

  clusterType :any;
  userName = "";

  userType: number;
  servicesArr: string[] = [];
  groupsArr: GroupModel[] = [];
  userTypeDesc = "";
  subscription: Subscription;
  subscriptionTz: Subscription;
  currency = "";
  currencySymbol : any;
  sgBoxMsg = ACCT_CONSTANTS.BOX_MSG.myAcctSharedGroups;
  assignedTGName = "";
  allocTG: any;
  selectedAcctType = "";
  myAcctForm = this.fb.group({
    accountType: [],
    acctStatus: [],
    billType: [],
    userType: [],
    firstName: ["",],
    lastName: ["",],
    address: [,],
    company: [,],
    userName: [],
    contactMobile: [],
    contactEmail: [],

    walletAmount: [],
    patchAmount: [],
    walletComments: [],

    tz: [,],
    zone: [],
    twofa: [false],
    newlineChar: [,],
    encrytMob: [false,],
    encryMsg: [false,],
    charset1: ['',],
    allocatedTG: ['',],
    assignedTG: ['',],
    tzAbbr: [],
    subServices: [],
    walletUsers: [],

    templateGroups: [],
    sharedGroups: [],
    groups: [],

    acctCount: [],
    adminCount: [],
    userCount: [],
    currency: []
  });
  constructor(private fb: FormBuilder,
    private router: Router,
    private localStorage: LocalStorageService,
    private route: ActivatedRoute,
    private editAcctService: EditAccountService,
    private commonService :CommonService) { }

  ngOnInit(): void {
    const userData:any=this.commonService.tokenDecoder();
      
    let clusterCaseChange = userData.cluster.toLowerCase();

    this.clusterType = clusterCaseChange;
    
    this.getCLIid();

    this.getAcctInfo();
    this.getAcctStatistics();
    
    this.route.queryParams.subscribe(params => {

      this.linkIndex = params["link"];

      if (this.linkIndex == undefined) {
        this.linkIndex = 1;

      }
      if (this.linkIndex > 5) {
        this.router.navigate(['/pageNotFound']);
      }
      if ((this.linkIndex == 3 || this.linkIndex == 4) && this.billType == 0) {
        this.router.navigate(['/page-401']);
      }


    })
    this.subscriptionTz = this.editAcctService
      .listenForTzChanges()
      .subscribe((data: MsgSettings) => {

        this.zone.setValue(data.zone);
        this.tzAbbr.setValue(data.tzAbbr);

      });

    this.subscription = this.editAcctService
      .listenForPIChanges()
      .subscribe((data: PersonalInfo) => {

        this.firstName.setValue(data.firstName);
        this.lastName.setValue(data.lastName);

      });

  }
  getAcctStatistics() {

    this.spinner = true;
    this.apiError = false;
    this.editAcctService.getAcctStatistics().
      subscribe((res: any) => {
        if (res) {
          this.spinner = false;
          this.templatescount = res.total_templates;
          this.groupsCount = res.total_groups;
          this.accountsCount = res.total_accounts;
          //  this.resObj = res;
        }
      },
        (error: HttpErrorResponse) => {
          this.spinner = false;
          this.apiError = true;
        });

  }
  getCLIid() {
    const user = this.localStorage.getLocal('user');
    let userObj = null;

    if (user) {
      userObj = JSON.parse(user);
    }

    this.cliId = userObj.cli_id;
  }


  getAcctInfo() {

    this.spinner = true;
    this.apiError = false;
    this.editAcctService.getAcctInfoToEdit(+this.cliId).
      subscribe((res: any) => {
        if (res) {
          this.spinner = false;
          //  this.resObj = res;
          this.billType = res.bill_type;
          this.mobile = res.mobile;
          this.email = res.email;
          this.userName = res.user;
          // this.wallet = res.wallet;
          this.userType = res.user_type;
          this.currency = res.billing_currency;
          this.currencySymbol = getCurrencySymbol(this.currency, "narrow");
          this.userTypeDesc = this.getUserType(res.user_type);
          res.services.forEach((element: any) => {
            if (element.enabled_yn == 1) {
              element.checked = true;
              this.servicesArr.push(element.sub_service_name);

            }
          });

          this.groupsArr = res.assigned_groups;

          this.allocTG = res.allocated_tgroups;
          //  console.log(this.allocTG)
          this.assignedTGName = res.dlt_templ_grp_name;
          this.selectedAcctType = res.user_type;
          this.editAcctService.populateAcctInfo(res, this.myAcctForm, "myAcctPage");
        }
      },
        (error: HttpErrorResponse) => {
          this.spinner = false;
          this.apiError = true;
        });

  }

  getUserType(user: any) {
    if (user === 0) return 'Super Admin'
    if (user === 1) return 'Admin'
    if (user === 0) return 'User'
  }


  onClickTry() {
    this.getAcctInfo();
    this.getAcctStatistics();
  }
  onClickNewAcct() {
    this.router.navigate(["/accounts/new"])
  }

  clickLinkIndex(index: number) {
    console.log('inside');
    
    this.linkIndex = index;
    this.router.navigate(["/myacct"], { queryParams: { link: index } });

  }
  onClickEditProfile() {
    this.linkIndex = 2;
    this.router.navigate(["/myacct"], { queryParams: { link: 2 } });
  }
  get firstName() {
    return this.myAcctForm.controls.firstName

  }
  get lastName() {
    return this.myAcctForm.controls.lastName
  }
  get zone() {
    return this.myAcctForm.controls.zone
  }
  get tzAbbr() {
    return this.myAcctForm.controls.tzAbbr
  }
  get wallet() {
    return this.myAcctForm.controls.walletAmount.value
  }

  // enter to click

  routing(routePath: String) {
    this.router.navigate([routePath])
  }

  ngOnDestroy() {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
    if (this.subscriptionTz) {
      this.subscriptionTz.unsubscribe();
    }

  }

}
