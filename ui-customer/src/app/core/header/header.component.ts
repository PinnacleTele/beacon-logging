import { HttpErrorResponse } from "@angular/common/http";
import { AfterViewChecked, Component, EventEmitter, OnDestroy, OnInit, Output, ViewChild } from "@angular/core";
import { Router, NavigationEnd, RouterEvent, NavigationStart } from "@angular/router";
import * as moment from "moment";
import { AccountService } from "src/app/account/account.service";
import { EditAccountService } from "src/app/account/edit-account/edit-account.service";
import { AuthLoginService } from "src/app/authentication/auth-login.service";
import { LocalStorageService } from "src/app/authentication/local-storage.service";
import { DownloadsService } from "src/app/downloads/Helpers/downloads-service.service";
import { CONSTANTS, value } from "src/app/shared/campaigns.constants";
import { getCurrencySymbol } from '@angular/common';

import {
    EnterExitRight,
    Container1,
    EnterExitTop
} from "../../shared/animation";
import { DataSharingService } from "../data-sharing.service";
import { CampaignsService } from "src/app/campaigns/campaigns.service";
import { RateChangeReportService } from "src/app/billing-detail/Billing_helper/ratechange_report.service";
import { BillingDetailService } from "src/app/billing-detail/billing-detail.service";
import { CommonService } from "src/app/shared/commonservice";
import { PersonalInfo } from "src/app/account/edit-account/model/personal-info";


@Component({
    selector: "app-header",
    templateUrl: "./header.component.html",
    styleUrls: ["./header.component.css"],
    animations: [EnterExitRight, Container1, EnterExitTop]
})

export class HeaderComponent implements OnInit, OnDestroy, AfterViewChecked {

    response: { message: string, statusCode: number };
    status: string;
    walletBalSpinner = false;
    noRecords = false;
    selectedUsersWalletAmountTot = 0;
    walletBalErr = false;
    cliId = "";
    billType: number;
    loggedInUserWalletBal: number;
    loggedInUserSmsBal: number;
    logUserBalRetrievalErr: boolean;
    spinner = false;
    acctSpinner = false;
    usersCount: number;
    adminsCount: number;
    totalCount: number;
    apiError = false;
    statError = false;
    userType: number;
    billTypeDesc = "";
    firstNameInitial = "";
    QLsize = 0;
    QLlink: any;
    email = "";
    firstName = "";
    userName = "";
    userTypeDesc = "";
    isShowDownload = false;
    zone: string;
    weekStart: string;
    today: string;
    range = "this week"
    myOptions = value;
    popup = false;
    quickLinkSpinner = false;
    signOutspinner = false;
    quickLinkLimit = CONSTANTS.quickLinkLimit;
    spinnerCount = new Array(this.quickLinkLimit);
    currencyType = ""
    currencyFormat = CONSTANTS.curencyFormat;
    currency = "0.0-6"

    showList: boolean = false;
    // @ViewChild(HeaderComponent, { static: false })
    // header: HeaderComponent;

    intl_Allow_value: any;

    
    otherCountryBillingRate:any[]=[];

    smsRate:any;
    dltRate:any;
    billingCurrency:any;

    billingRateLoading:boolean = false;
    walletLoading :boolean = false;

    billingRateApiError:boolean = false;
    walletApiError :boolean = false;

    totalCountry:number = 0;

    loggedInUser:number;
    tokenError:boolean = false;

    constructor(public router: Router,
        private localStorageService: LocalStorageService,
        private dataShare: DataSharingService,
        private accountService: AccountService,
        private editAccountService: EditAccountService,
        private authLoginSvc: AuthLoginService,
        private downloadsService: DownloadsService,
        private downloadService: DownloadsService,
        private campaignservice: CampaignsService,
        private ratechangeService : RateChangeReportService,
        private billingRateService : BillingDetailService,
        private commonService:CommonService
    ) {
        this.router.events.subscribe((eve) => {
            this.isShowDownload = false;
            if (eve instanceof NavigationEnd) {
                let domainPath = window.location.href;
                let path = domainPath.split('/');
                console.log(path);

                this.activeLink = path.includes('billing') ? '/billing' : path.includes('reports') ? '/reports' : '';
                // this.activeLink = ;


            }
        })
        this.currencyFormat = CONSTANTS.curencyFormat;

    }
    ngAfterViewChecked(): void {
        // this.downloadsService.setHeaderDownloadControl(this.header);
        // throw new Error("Method not implemented.");
    }
    ngOnDestroy(): void {

    }
    ngOnInit(): void {
        this.editAccountService
        .listenForPIChanges()
        .subscribe((data: PersonalInfo) => {
  
          this.firstName = data.firstName;
          this.firstNameInitial = this.firstName.substring(0, 1);
           
        });
        this.quickLinkCall();
        this.router.events.subscribe((eve) => {
            this.isShowQA = false;
        });
        let domainPath = window.location.href;
        let path = domainPath.split('/');
        if (path.includes('billing')) {
            this.activeLink = '/billing';
        }
        else if (path.includes('reports')) {
            this.activeLink = '/reports';
        }
        
        this.tooltipMsg();

        this.getCLIid();

        this.getAcctStatistics();
        this.currencyType = JSON.parse(this.localStorageService.getLocal('user')).billing_currency

    }
    @Output() showOverlayEvent: EventEmitter<boolean> = new EventEmitter();
    qlsubscription: any;

    isShowUP = false;

    isShowQA = false;

    showOverlay = false;

    activeLink = "";

    toggleProfileSlider() {
        this.getCLIid();
        this.getAcctStatistics();
        this.isShowQA = false;
        this.isShowUP = !this.isShowUP;
        if (this.billType == 1) {
            if (this.isShowUP) {
                this.getLoggedInUserBal();
            }
            this.billTypeDesc = "Prepaid"
        } else {
            this.billTypeDesc = "Postpaid"
        }
     }
    overlayClick() {
        this.showList = false;
    }
    toggleQuickAction() {
        // console.log("qa");
        // console.log(this.isShowQA);
        // if (!this.isShowQA) {
        //     this.isShowQA = false;
        //     console.log("if");

        // }
        // console.log(this.quickLinks);

        this.isShowQA = !this.isShowQA
        //this.campService.closeCampaign.next(false)

    }
    closeQA() {

        if (this.isShowQA == true) {
            this.isShowQA = false;
        }

    }

    showMyAcct() {
        this.isShowQA = false;
        this.router.navigate(["/myacct"]);
    }
    openQA() {
        // console.log("open");
        if (this.isShowQA == true) {
            this.isShowQA = false
        }
        // this.isShowQA=true;
    }

    blur() {


        setTimeout(() => {
            //  console.log("blur1  ", this.isShowQA);
            // this.isShowQA=false;
            this.isShowQA = !this.isShowQA

            //  console.log("blur2  ", this.isShowQA);
        }, 200)


    }
    openSideNav() {
        this.showOverlayEvent.emit(true);
    }


    dataAssign() {
        //  console.log("dataAssign");
        if (this.dataShare.data) {
            this.quickLinks = this.dataShare.data.quicklinks;
            this.QLlink = new Array(CONSTANTS.quickLinkLimit - this.dataShare.QLsize)
        }



    }

    badError: any;

    quickLinks: any = [];

    // quickLinks1: any



    routing(routePath: string) {
        let val = routePath.split('/')

        if (val[1].includes('campaigns')) {
            if (this.intl_Allow_value == 0) {

                // this.router.navigate([val[1] + '/' + 'traffic']);
                //console.log([val[1] + '/' + 'traffic' + '/' + val[2] ]);
                this.router.navigate([routePath]);
            } else {
                this.campaignservice.previousTrafficSelection("");
                this.router.navigate(["campaigns/traffic"], { queryParams: { "campType": val[2] } });

            }
        } else {
            this.router.navigate([routePath]);
        }
        this.isShowQA = false;
        //this.router.navigate([routePath]);

    }
    settings(page: number) {
        if (!this.quickLinkSpinner) {
            this.isShowUP = false;
            this.router.navigate(["/myacct"], { queryParams: { link: page } });
        }

    }
    quickLinkApiError = false;
    clusterType:any;
    quickLinkCall() {
         // loginuser
         const userData:any=this.commonService.tokenDecoder();
      
         let clusterCaseChange = userData.cluster.toLowerCase();
 
         this.clusterType = clusterCaseChange;
        if (!this.dataShare.getData("showQuickLinks")) {


            this.quickLinkApiError = false;
            this.quickLinkSpinner = true;
            this.quickLinks = [];
            this.QLlink = new Array(6);
            this.dataShare.getACCQuickLinks().subscribe((res: any) => {

                //  let data1 = this.dataShare.setquickLinks(data);
                //    this.QLlink = new Array(6 - this.getCount())
                this.dataShare.getQuickLinks().subscribe((data: any) => {
                    this.QLlink = new Array(6 - data.length);
                    this.quickLinks = res.quicklinks;
                    this.quickLinkSpinner = false;
                }, (error: HttpErrorResponse) => {
                    this.quickLinkApiError = true
                })

                // this.QLlink = new Array(3);
                //  console.log(this.getCount());

                // this.getCount(this.quickLinks)
            }, (error: HttpErrorResponse) => {
                this.quickLinkApiError = true;
            })
        }
    }
    getCLIid() {
        const user = this.localStorageService.getLocal('user');
        let userObj = null;

        if (user) {
            userObj = JSON.parse(user);
            this.cliId = userObj.cli_id;
            this.billType = userObj.bill_type;
            this.userType = userObj.user_type;
            this.firstNameInitial = userObj.firstname.substring(0, 1);
            this.firstName = userObj.firstname;
            this.email = userObj.email;
            this.userName = userObj.user;
            this.zone = userObj.tz;
            this.userTypeDesc = this.getUserType(userObj.user_type);

        }
            


    }
    getLoggedInUserBal() {
        
        this.walletBalSpinner = true;

        this.logUserBalRetrievalErr = false;
        this.accountService.getWalletBal(this.cliId.toString()).subscribe(
            (res: any) => {                
                this.walletBalSpinner = false;
                this.loggedInUserWalletBal = res.wallet_bal;
                this.loggedInUserSmsBal = res.sms_left;

            },
            (error: HttpErrorResponse) => {

                this.logUserBalRetrievalErr = true;
                this.walletBalSpinner = false;

            });
    }

    getAcctStatistics() {

        this.acctSpinner = true;
        this.statError = false;
        this.editAccountService.getAcctStatistics().
            subscribe((res: any) => {
                if (res) {
                    this.acctSpinner = false;
                    this.usersCount = res.total_users;
                    this.adminsCount = res.total_admins;
                    this.totalCount = res.total_accounts;
                    //  this.resObj = res;
                }
            },
                (error: HttpErrorResponse) => {
                    this.acctSpinner = false;
                    this.statError = true;
                });

    }

    getUserType(user: any) {
        if (user === 0) return 'Super Admin'
        if (user === 1) return 'Admin'
        if (user === 2) return 'User'
    }
    onClickSignOut() {

        this.signOutspinner = true;
        this.authLoginSvc.logout(null).subscribe(
            (res: any) => {
                this.signOutspinner = false;


            }, (error: any) => {
                this.signOutspinner = false;
                this.popup = true;
                this.response = this.authLoginSvc.internalServerErr;
                this.status = this.response.message;
                // console.log(this.response);

            }
        );

    }

    tryAgain(event: any) {
        this.popup = false;
        this.onClickSignOut();
    }

    modalcontinue(event: boolean) {
        this.router.navigate(["/accounts"]);
    }
    modalClose(event: boolean) {
        this.popup = false;
    }

    quickLinkTab() {
        this.showNewQuickLinksDropdown();
    }

    showNewQuickLinksDropdown() {
        const showQuickLinks = this.dataShare.getData(
            "showQuickLinks"
        );
        const user = this.localStorageService.getLocal('user');
        const uservalue = JSON.parse(user);
        this.intl_Allow_value = uservalue.intl_enabled_yn;

        return showQuickLinks;
    }


    downloadTab() {
        //this.showNewCampaignsDropdown();
        this.downloadsContent()

    }

    showNewCampaignsDropdown() {
        if (this.quickLinks.length == 0) {
            this.QLlink = new Array(6);
        }

        const showDownloads = this.dataShare.getData(
            "showDownloads"
        );
        return showDownloads;
    }
    showBillingDropDown() {
        const showDownloads = this.dataShare.getData(
            "billingDropdown"
        );
        return showDownloads;
    }
    showReportsDropDown() {
        const showReports = this.dataShare.getData(
            "reportsDropdown"
        );
        return showReports;
    }
    toReports() {
        this.isShowUP = false;
        this.router.navigate(["/reports/summary"]);
    }

    downloads: any;

    noOfDownloads: number = 0;

    downloadsAPIerror = false;

    nodata = false;

    skeletonloading: boolean = false;

    downloadsContent() {




        let DateRange = {
            dateselectiontype: "this week",
            fdate: moment.tz(this.zone).startOf('weeks').format('YYYY-MM-DD'),
            tdate: moment.tz(this.zone).format('YYYY-MM-DD')
        }

        // console.log(DateRange);
        this.skeletonloading = true;
        this.downloadsAPIerror = false;
        this.downloadsService.dowloadTableData(DateRange).subscribe((res) => {
            this.noOfDownloads = res.length;
            // console.log(res);
            this.downloadsAPIerror = false;
            this.nodata = false;
            let sortedArray = [];
            // if (res.length == 1) {
            // console.log(res);
            

            sortedArray = res;
            //  }
            // else if (res.length > 1) {
            //     sortedArray = res.sort(function (left, right) {
            //         return moment.utc(left.created_ts).diff(moment.utc(right.created_ts))
            //     });
            // }


            res.forEach(element => {
               let temp = element.filters.split(',')
                let temp1 = ``
                temp.forEach(ele => {
                temp1 += `<p>${ele}</p>`
                });
                 element.filters = temp1
                 }); 
            
                       
            this.downloads = sortedArray;

            if (this.downloadService.downloadIds.length > 0) {
                // this.downloadService.downloadIds.forEach((data:any)=>{
                //     this.downloads

                // })
                this.downloadService.downloadIds.forEach((dele: any) => {
                    this.downloads.filter((x: any) => dele.id === x.id)
                        .forEach((x: any) => {
                            x.downloadStatus = true;
                           // console.log(x,"abc")
                        });
                });
            }
            //   console.log(this.downloads);


            if (this.downloads.length > 4) {
                this.downloads = this.downloads.slice(0, 4);
            }
            if (res.length == 0) {
                this.nodata = true;
            }

            this.skeletonloading = false;
        },
            (error: HttpErrorResponse) => {
                this.skeletonloading = false;
                this.downloadsAPIerror = true;
            })
    }

    downloadProcess(id: any) {
        // if ( this.downloadService.downloadIds.length >= 0) {
        // this.downloadService.downloadIds.forEach((data:any)=>{
        //     this.downloads

        // })
        //   this.downloadService.downloadIds.forEach((dele: any) => {

        this.downloads?.forEach((x: any) => {
            if (x.id === id) {
                x.downloadStatus = false;
            }

        })
        //   .forEach((x: any) => {

        //   });

        //  });
        //  }
    }
    tryagain() {
        this.downloadsContent()

    }
    onDownloadClick(downloadData: any, from: any, to: any, status: string) {
        if (status == 'completed') {
            let UserTimeStamp = moment().format('YYYYMMDD_HHmmss')
            downloadData.downloadStatus = true;
            this.downloadService.getTextFile(downloadData.id)
                .subscribe((data: any) => {
                    const blob = new Blob([data], { type: 'application/zip' });
                    const a = document.createElement('a');
                    const objectUrl = URL.createObjectURL(blob);
                    a.href = objectUrl;
                    a.download = `log_${from}_${to}_${UserTimeStamp}.zip`;
                    a.click();
                    downloadData.downloadStatus = false;
                    URL.revokeObjectURL(objectUrl);
                });
        }
    }

    routetoDown(routerPath) {
        this.billingRate();
        this.ratechangeService.setCliIDforRoute(undefined);
        this.router.navigate(["/" + routerPath])
    }

    billingRate() {
        console.log(this.showList);

        this.showList = !this.showList;

    }

    balanceShowList :boolean = false;

    balanceListDropdown(){
        //this.balanceShowList = !this.balanceShowList;
        this.balanceShowList= this.dataShare.getData(
            "showWalletList"
        );        
            // if (balanceShowList) {
            //    // this.walletApiCalls();
            // } else {
            //     //  do nothing
            // }
        return   this.balanceShowList;
       
    }

    tooltipcontent: any;

    tooltipMsg(){
        const user = JSON.parse(this.localStorageService.getLocal('user')) ;
             const usertype = user.bill_type;
             this.loggedInUser = usertype;
        if (this.loggedInUser == 1) {
            this.tooltipcontent= "Billing Rates and Wallet"
        }
        else{
            this.tooltipcontent = "Billing Rates"
        }
    }

    walletApiCalls(){
      
        //this.balanceShowList = this.balanceListDropdown();
            const user = JSON.parse(this.localStorageService.getLocal('user')) ;
             const usertype = user.bill_type;
             this.loggedInUser = usertype;
             //console.log(this.loggedInUser);
             
             if (this.loggedInUser == 1) {
                this.getaccountInfo();
                this.getLoggedInUserBal();
                this.billingRateOtherCountries();
                
             }else{
                this.getaccountInfo();
                this.billingRateOtherCountries();
                
             }
    }


    billingRateOtherCountries(){
        this.walletLoading = true;
        this.walletApiError = false;
        this.billingRateService.internationalRates().subscribe((data:any)=>{
            this.walletLoading = false;
           const rowData = data.filter((data:any)=>{
                return data.country.toLowerCase() != 'row' ;
            })
            this.totalCountry = rowData.length;
            console.log(rowData);
            
           this.otherCountryBillingRate = data;
    
           
        },
        (error:HttpErrorResponse)=>{
            this.walletLoading = false;
    this.walletApiError = true;
        })
    }

    getaccountInfo(){
        this.billingRateApiError = false;

       this.billingRateLoading = true;
        const user = JSON.parse(this.localStorageService.getLocal('user')) ;
       // console.log(user);
      
        const cli_id = user.cli_id;
        this.billingRateService.getAcctInfo(cli_id).subscribe((data:any)=>{
            this.billingRateLoading = false;
            this.smsRate = data.smsrate;
            this.dltRate = data.dltrate;
            this.billingCurrency = data.billing_currency;        
        },
        ((error:HttpErrorResponse)=>{
            this.billingRateLoading = false;
            this.billingRateApiError = true;
        }))
    }

    getC_Symbol(eve:any){
        return getCurrencySymbol(eve,'narrow');
      }

      retryGetBal(){
        this.getLoggedInUserBal();
      }

      
      retryBillingRates(){

        this.getaccountInfo();
    }
    
    retryOtherCountryRates(){
        this.billingRateOtherCountries();
    }
    
}



