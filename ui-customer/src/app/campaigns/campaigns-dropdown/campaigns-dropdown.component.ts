import { Component,OnInit } from "@angular/core";
import { Router } from "@angular/router";
import { DataSharingService } from "src/app/core/data-sharing.service";
import { EnterExitTop } from "../../shared/animation";
import {  CampaignsService} from "src/app/campaigns/campaigns.service";
@Component({
    selector: "app-campaigns-dropdown",
    templateUrl: "./campaigns-dropdown.component.html",
    styleUrls: ["./campaigns-dropdown.component.css"],
    animations: [EnterExitTop]
})
export class CampaignsDropdownComponent implements OnInit {
    constructor(private dataSharingService: DataSharingService,private router:Router,private campaignservice:CampaignsService) {}
   
    trafficAllowed: boolean =  false;

    intl_allowed_value:any;
   
    ngOnInit(): void {
      const user = this.campaignservice.getUser();
        const user_intl_enable = user.intl_enabled_yn;
        this.intl_allowed_value = user.intl_enabled_yn;
        if (user_intl_enable == 0) {
            this.trafficAllowed = false;
        } else {
            this.trafficAllowed = true;
        }
    }

    campaignTab() {
        this.showNewCampaignsDropdown();
    }

    showNewCampaignsDropdown() {
        const showCampaignsDropDown = this.dataSharingService.getData(
            "showCampaignsDropDown"
        );
        return showCampaignsDropDown;
    }

    routing(type:string){

        if (this.intl_allowed_value == 0) {
            this.router.navigate(["campaigns" + '/' + type ]);
        } else {
            this.campaignservice.previousTrafficSelection("");
            this.router.navigate(["campaigns/traffic"], {queryParams : {"campType" : type}});
          
        }
    }

}
