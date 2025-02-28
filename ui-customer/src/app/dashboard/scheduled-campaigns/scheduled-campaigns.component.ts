import { HttpErrorResponse } from "@angular/common/http";
import { Component, OnInit } from "@angular/core";
import { environment } from "src/environments/environment";
import { ScheduledCampaignsList } from "../dashboard.models";
import { ScheduledCampaignsService } from "./scheduled-campaigns.service";

@Component({
    selector: "app-scheduled-campaigns",
    templateUrl: "./scheduled-campaigns.component.html",
    styleUrls: ["./scheduled-campaigns.component.css"]
})
export class ScheduledCampaignsComponent implements OnInit {
    constructor(private scheduleCampService: ScheduledCampaignsService) {}

    ngOnInit(): void {
        this.getScheduledCampaigns(this.date);
    }

    scheduledCampaigns: ScheduledCampaignsList;

    date = "today";

    onDayChange(date: string) {
        if (date === "today") {
            this.date = "today";
        } else {
            this.date = "next7days";
        }
        this.getScheduledCampaigns(this.date);
    }

    getScheduledCampaigns(day: string) {
        this.scheduleCampService.getScheduledCampaignsList({ day }).subscribe(
            (res: ScheduledCampaignsList) => {
                if (
                    res.responsestatus === environment.APIStatus.success.text &&
                    res.responsecode > environment.APIStatus.success.code
                ) {
                    this.scheduledCampaigns = res;
                } else if (
                    res.responsestatus === environment.APIStatus.error.text &&
                    res.responsecode < environment.APIStatus.error.code
                ) {
                    console.log(res.responsestatus);
                }
            },
            (error: HttpErrorResponse) => {
                console.log(error.message, error.statusText);
            }
        );
    }
}
