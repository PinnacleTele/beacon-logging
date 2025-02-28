import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { LocalStorageService } from 'src/app/authentication/local-storage.service';
import { DataSharingService } from 'src/app/core/data-sharing.service';
import { CONSTANTS } from 'src/app/shared/campaigns.constants';
import { CommonService } from 'src/app/shared/commonservice';
import { AccountService } from '../../account.service';

@Component({
    selector: 'app-my-acct-qcsettings',
    templateUrl: './my-acct-qcsettings.component.html',
    styleUrls: ['./my-acct-qcsettings.component.css']
})
export class MyAcctQcsettingsComponent implements OnInit, OnDestroy {

    clusterType :any;

    constructor(private dataShare: DataSharingService,
        private accService: AccountService,
        private localStorage: LocalStorageService,
        private commonService:CommonService) { }


    ngOnInit(): void {
        const userData:any=this.commonService.tokenDecoder();
      
        let clusterCaseChange = userData.cluster.toLowerCase();

        this.clusterType = clusterCaseChange;
        let user: any = this.localStorage.getLocal("user")
        this.userDetails = JSON.parse(user)
        if (userData.user_type != 2) {
        if (this.clusterType == 'otp') {
            this.createQl.push({ group: "createnew", ql: "account", header: "Create New", value: false, title: "Account", subText: "Create new account" })

        } else {
           this.createQl.push( 
            { group: "createnew", ql: "template", header: "Create New", value: false, title: "Template", subText: "Create new Template" },
            { group: "createnew", ql: "group", header: "Create New", value: false, title: "Group", subText: "Create new Group" },
            { group: "createnew", ql: "account", header: "Create New", value: false, title: "Account", subText: "Create new account" });
        }
        }
        if (userData.bill_type != 0 && userData.user_type != 2) {
            this.myAccounts.push({ group: "myaccount", ql: "wallet", header: "My Account", value: false, title: "Manage Wallet", subText: "Manage users wallet" })
        }


        this.getQuiclLinks();
        this.loader = this.accService.quicklinkupdating.subscribe((data) => {
            this.spinner = data;
        })
    }

    tempArray: any[] = [];

    spinner = false;

    userDetails: any;

    loader: any;



    campaignQl = [
        { group: "campaign", ql: "cq", header: "Send Campaign", value: false, title: "Quick SMS Messaging", subText: "Send Quick Campaign" },
        { group: "campaign", ql: "cg", header: "Send Campaign", value: false, title: "Group Messaging", subText: "Send Group Campaign" },
        { group: "campaign", ql: "cotm", header: "Send Campaign", value: false, title: "OTM Messaging", subText: "Send One To Many Campaign" },
        { group: "campaign", ql: "cmtm", header: "Send Campaign", value: false, title: "MTM Messaging", subText: "Send Many To Many Campaign" },
        { group: "campaign", ql: "ct", header: "Send Campaign", value: false, title: "Template Messaging", subText: "Send Template Campaign" },]


    createQl :any = [];


    myAccounts = [{ group: "myaccount", ql: "mysettings", header: "My Account", value: false, title: "My Settings", subText: "View / Edit my profile details and account settings" }]


    report = [{ group: "report", ql: "summary", header: "Reports", value: false, title: "Summary", subText: "View / Download summary reports" },
    { group: "report", ql: "detailed", header: "Reports", value: false, title: "Detailed Log", subText: "View / Download detailed report" },
    { group: "report", ql: "search", header: "Reports", value: false, title: "Search", subText: "Search by mobile / ackid" },]

    enable = false;

    quickLinkLimit = CONSTANTS.quickLinkLimit;

    // OnToggle() {
    //     this.enable = !this.enable;
    // }


    onClickToggle(event) {
        // console.log(event);

        if (event.group == "myaccount") {
            this.myAccounts.forEach(

                (ele: any) => {
                    if (ele.ql == event.ql) {
                        if (event.value) {
                            ele.value = false;
                            this.enable = true;
                        }
                        else {
                            if (this.limit < this.quickLinkLimit) {
                                ele.value = true;
                                this.enable = true;
                            }
                        }
                    }
                    // if (this.limit < this.quickLinkLimit) {
                    //     ele.value = !ele.value;
                    //     this.enable = true;
                    // }
                    // else {
                    //     ele.value = false;
                    //     // this.enable = true;
                    // }
                })

        }
        else if (event.group == "createnew") {
            this.createQl.forEach(

                (ele: any) => {
                    if (ele.ql == event.ql) {
                        if (event.value) {
                            ele.value = false;
                            this.enable = true;
                        }
                        else {
                            if (this.limit < this.quickLinkLimit) {
                                ele.value = true;
                                this.enable = true;
                            }
                        }
                    }
                    // if (this.limit < this.quickLinkLimit) {
                    //     ele.value = !ele.value;
                    //     this.enable = true;
                    // }
                    // else {
                    //     ele.value = false;
                    //     //  this.enable = true;
                    // }
                })

        }
        else if (event.group == "campaign") {
            
            this.campaignQl.forEach(

                (ele: any) => {
                    if (ele.ql == event.ql) {
                        if (event.value) {
                            ele.value = false;
                            this.enable = true;
                        }
                        else {
                            if (this.limit < this.quickLinkLimit) {
                                ele.value = true;
                                this.enable = true;
                            }
                        }
                    }
                    // if (this.limit < this.quickLinkLimit) {
                    //     ele.value = !ele.value;
                    //     this.enable = true;
                    // }

                    // else {
                    //     ele.value = false;
                    //     // this.enable = true;
                    // }
                })

        }
        else if (event.group == "report") {
            this.report.forEach(

                (ele: any) => {
                    if (ele.ql == event.ql) {
                        if (event.value) {
                            ele.value = false;
                            this.enable = true;
                        }
                        else {
                            if (this.limit < this.quickLinkLimit) {
                                ele.value = true;
                                this.enable = true;
                            }
                        }
                        // if (this.limit < this.quickLinkLimit) {
                        //     ele.value = !ele.value;
                        //     this.enable = true;
                        // }

                        // else {
                        //     ele.value = false;
                        //     // this.enable = true;
                        // }

                    }

                })

        }

    }

    getQuiclLinks() {
        this.accService.getQuickLinks().subscribe((res: any) => {
            this.reset()
            //  this.tempArray = res
            res.forEach(element => {
                if (element.group == "campaign") {
                    this.campaignQl.forEach((ele) => {
                        if (element.quicklink == ele.ql) {
                            ele.value = true;
                        }
                    })
                }
                else if (element.group == "createnew") {
                    this.createQl.forEach((ele) => {
                        if (element.quicklink == ele.ql) {
                            ele.value = true;
                        }
                    })
                }
                else if (element.group == "myaccount") {
                    this.myAccounts.forEach((ele) => {
                        if (element.quicklink == ele.ql) {
                            ele.value = true;
                        }
                    })
                }
                else if (element.group == "report") {
                    this.report.forEach((ele) => {
                        if (element.quicklink == ele.ql) {
                            ele.value = true;
                        }
                    })
                }
            });

        })
        this.tempArray = this.campaignQl
    }
    onSave() {
        let payload = [];

        this.editPopup = false;

        let temp = [this.campaignQl, this.createQl, this.myAccounts, this.report];

        const mypromise = new Promise((resolve, reject) => {
            temp.forEach((ele) => {
                ele.forEach((ele) => {
                    if (ele.value == true) {
                        // console.log(ele);
                        let obj = { "ql": ele.ql, "group": ele.group }
                        payload.push(obj)
                    }
                })
                resolve(payload);

            })
        })
        mypromise.then((payload) => {
            this.updateQls(payload)
        })
    }

    reset() {
        this.campaignQl.forEach((ele) => { ele.value = false });
        this.createQl.forEach((ele) => { ele.value = false });
        this.myAccounts.forEach((ele) => { ele.value = false });
        this.report.forEach((ele) => { ele.value = false });
        this.enable = false;
    }
    editPopup = false;
    editResponse: any;
    status: any;
    updateQls(payload: any) {
        this.accService.updateQuickLinks(payload).subscribe((res) => {
            this.dataShare.getACCQuickLinks().subscribe((data: any) => {
                
                this.dataShare.setquickLinks(data);
                this.editPopup = true;
                this.editResponse = res;
                this.enable = false;

            })

            
        }, (error: HttpErrorResponse) => {
            let err = this.accService.badError
            this.editResponse = { message: "SomeThing Went Wrong", statusCode: -401, error: "ql" }
            // this.status=error.message
            this.editPopup = true;

        })
    }

    get limit() {
        let temp = [this.campaignQl, this.createQl, this.myAccounts, this.report]
        let arr = []
        temp.forEach((ele) => {
            ele.forEach((element) => {
                if (element.value == true) {
                    arr.push(element);
                }
            })
        })

        return arr.length;
    }

    continueDeletePopup(event) {
        this.editPopup = false;
    }
    tryAgainEditPopup(event) {
        this.onSave();
    }
    closeDeletePopup(event) {
        this.editPopup = false;
    }
    onDiscard() {
        this.campaignQl = this.tempArray
    }

    ngOnDestroy(): void {
        this.loader.unsubscribe();
        //throw new Error('Method not implemented.');
    }
}
