import { HttpErrorResponse } from "@angular/common/http";
import { Component, OnInit } from "@angular/core";
import { environment } from "src/environments/environment";
import { Campaign, CampaignsList } from "../dashboard.models";
import { CampaignListService } from "./campaign-list.service";

@Component({
    selector: "app-campaign-list",
    templateUrl: "./campaign-list.component.html",
    styleUrls: ["./campaign-list.component.css"]
})
export class CampaignListComponent implements OnInit {
    campaigns: CampaignsList = {
        responsecode: 0,
        responsestatus: "",
        data: {
            total: "0",
            completed: "0",
            tabledata: []
        }
    };

    date = "today";

    constructor(private campaignService: CampaignListService) {}

    ngOnInit(): void {
        this.getCampaignsList(this.date);
    }

    onDayChange(date: string) {
        if (date === "today") {
            this.date = "today";
        } else {
            this.date = "yesterday";
        }
        this.getCampaignsList(this.date);
    }

    getCampaignsList(day: string) {
        this.campaignService.getCampaignsList({ day }).subscribe(
            (res: CampaignsList) => {
                if (
                    res.responsestatus === environment.APIStatus.success.text &&
                    res.responsecode > environment.APIStatus.success.code
                ) {
                    this.campaigns = res;
                } else if (
                    res.responsestatus === environment.APIStatus.error.text &&
                    res.responsecode < environment.APIStatus.error.code
                ) {
                    //console.log(res.responsestatus);
                }
            },
            (error: HttpErrorResponse) => {
                //console.log(error.message, error.statusText);
            }
        );
    }

    getBorderShade(campaign: Campaign) {
        let color = "";
        switch (campaign.status) {
            case "running": {
                color = "border-blue-100";
                break;
            }
            case "completed": {
                color = "border-green-100";
                break;
            }
            case "failed": {
                color = "border-red-100";
                break;
            }
            default: {
                color = "";
                break;
            }
        }
        return color;
    }

    getStatusColor(campaign: Campaign) {
        let color = "";
        switch (campaign.status) {
            case "running": {
                color = "bg-blue-100 text-blue-600";
                break;
            }
            case "completed": {
                color = "bg-green-100 text-green-600";
                break;
            }
            case "failed": {
                color = "bg-red-100 text-red-600";
                break;
            }
            default: {
                color = "";
                break;
            }
        }
        return color;
    }
}
