import { Component, OnInit, HostListener, AfterViewChecked, ViewChild } from "@angular/core";
import { Router } from "@angular/router";
import { Observable, Subscription } from "rxjs";
import { DataSharingService } from "src/app/core/data-sharing.service";
import { AuthLoginService } from "./authentication/auth-login.service";
import { LocalStorageService } from "./authentication/local-storage.service";
import { CONSTANTS } from "./shared/campaigns.constants";
import { CampaignTemplateComponent } from "src/app/campaigns/campaign-template/campaign-template.component";
import { TemplateCampaignService } from "src/app/campaigns/campaign-template/service/template-campaign.service";
import { CommonService } from "./shared/commonservice";
@Component({
    selector: "app-root",
    templateUrl: "./app.component.html",
    styleUrls: ["./app.component.css"]
})
export class AppComponent implements OnInit, AfterViewChecked {
    title = "SmsPortal";

    isLoggedIn = false;

    showOverlay = false;

    navigationEnd = "";

    url = "";

    @ViewChild(CampaignTemplateComponent, { static: false })
    c_templatComponent: CampaignTemplateComponent;



    isLoggedIn$: Observable<boolean>;
    tokenSubscription: Subscription;
    constructor(
        public templatecampService: TemplateCampaignService,
        private router: Router,
        private dataSharingService: DataSharingService,
        private authLoginService: AuthLoginService,
        private localStorage1: LocalStorageService,
        private commonService:CommonService
    ) { }

    ngAfterViewChecked(): void {
        this.templatecampService.setCampaignTemplateControl(this.c_templatComponent);
    }

    @HostListener("document:click", ["$event"])
    clickout(event) {

        if (event.target.id === "campbtn") {
            const showCampaignsDropDown = this.dataSharingService.getData(
                "showCampaignsDropDown"
            );
            this.dataSharingService.setData(
                "showCampaignsDropDown",
                !showCampaignsDropDown
            );
        } else {
            this.dataSharingService.setData("showCampaignsDropDown", false);
        }
        if (event.target.id === "billing") {
            const billingDropdown = this.dataSharingService.getData(
                "billingDropdown"
            );
            this.dataSharingService.setData(
                "billingDropdown",
                !billingDropdown
            );
        } else {
            this.dataSharingService.setData("billingDropdown", false);
        }
        if (event.target.id === "reportsdropdown") {
            const reportsDropdown = this.dataSharingService.getData(
                "reportsDropdown"
            );
            this.dataSharingService.setData(
                "reportsDropdown",
                !reportsDropdown
            );
        } else {
            this.dataSharingService.setData("reportsDropdown", false);
        }
        if (event.target.id === "quicklinks") {

            setTimeout(() => {
                const showQuickLinks = this.dataSharingService.getData(
                    "showQuickLinks"
                );
                this.dataSharingService.setData(
                    "showQuickLinks",
                    !showQuickLinks
                );
            }, 200);
        }
        else if (event.target.id == "tryagain") {

        }
        else {
            this.dataSharingService.setData("showQuickLinks", false);
        }
        if (event.target.id === "downloadsIcon") {


            const showDownloads = this.dataSharingService.getData(
                "showDownloads"
            );
            this.dataSharingService.setData(
                "showDownloads",
                !showDownloads
            );
        } else {
            if (event.target.id === "downloads") {

            }
            else {

                this.dataSharingService.setData("showDownloads", false);
            }
        }
        if (event.target.id === "walletsIcon") {


            const showWalletList = this.dataSharingService.getData(
                "showWalletList"
            );
            this.dataSharingService.setData(
                "showWalletList",
                !showWalletList
            );
        } else {
                this.dataSharingService.setData("showWalletList", false);
            
        }
    }

    ngOnInit() {
        this.commonService.tokenDecoder();
        const user = this.localStorage1.getLocal("user");
        this.url = this.router.url;
        const path = window.location.href;
        const splitOnSlash = path.split("/");
        this.navigationEnd = splitOnSlash[splitOnSlash.length - 1];
        localStorage.setItem(
            'refresh_interval', CONSTANTS.DEFAULT_REFRESH_INTERVAL.toString()

        );
        if (user != null && user !== "") {
            // console.log("user available");
            if (
                this.navigationEnd === "" ||
                this.navigationEnd === "/" ||
                this.navigationEnd === "login" ||
                this.navigationEnd === "/dashboard"
            ) {
                // console.log("user available route 1")
                this.router.navigate(["dashboard"]);
            } else if (
                this.navigationEnd === "campaigns" ||
                this.navigationEnd === "dashboard" ||
                this.navigationEnd === "accounts" ||
                this.navigationEnd === "groups" ||
                this.navigationEnd === "templates" ||
                this.navigationEnd === "reports"
            ) {
                // console.log("user available route 2")

                this.router.navigate([this.navigationEnd]);
            }
            this.authLoginService.getRefreshInterval().subscribe(
                (res: any) => {
                    //this.authLoginService.setRefreshToken().subscribe();
                    this.authLoginService.startRefreshTokenTimer();
                },

                (err) => {
                    //this.authLoginService.setRefreshToken().subscribe();
                    this.authLoginService.startRefreshTokenTimer();
                }
            );
            //console.log("from app")
            //this.authLoginService.refreshTokenRepeatedly();
        } else {
            // console.log("no token");

            this.router.navigate(["/login"]);
        }
      //  logout on localStorage Changes  not working firefox
        // window.addEventListener('storage', (e:any) => {
        //     console.log(e);            
        //     if(e.key=="token" ||e.key=="user"||e.key=="refresh_interval"){
        //     localStorage.removeItem('token');
        //     localStorage.removeItem('user');
        //     localStorage.removeItem('refresh_interval');
        //     this.authLoginService.invalidSess = true;
        //     this.authLoginService.setTimer = false;
        //     this.router.navigate(["login"]);
        //     }
        //   });

        
    }

    overlayEventHandler(p: any) {
        this.showOverlay = p;
    }
    ngOnDestroy() {

        if (this.tokenSubscription) {
            this.tokenSubscription.unsubscribe();
        }
        this.authLoginService.destroySubscription();
    }
}
