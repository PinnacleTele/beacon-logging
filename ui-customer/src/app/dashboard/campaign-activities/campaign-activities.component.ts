import { HttpErrorResponse } from "@angular/common/http";
import { Component, OnInit } from "@angular/core";
import { environment } from "src/environments/environment";
import { Activities } from "../dashboard.models";
import { CampaignActivitiesService } from "./campaign-activities.service";

@Component({
    selector: "app-campaign-activities",
    templateUrl: "./campaign-activities.component.html",
    styleUrls: ["./campaign-activities.component.css"]
})
export class CampaignActivitiesComponent implements OnInit {
    constructor(private campActivityService: CampaignActivitiesService) {}

    date = "today";

    activities: Activities;

    ngOnInit(): void {
        this.getCampaignActivities(this.date);
    }

    onDayChange(date: string) {
        if (date === "today") {
            this.date = "today";
        } else {
            this.date = "yesterday";
        }
        this.getCampaignActivities(this.date);
    }

    getCampaignActivities(day: string) {
        this.campActivityService.getCampaignActivities({ day }).subscribe(
            (res: Activities) => {
                if (
                    res.responsestatus === environment.APIStatus.success.text &&
                    res.responsecode > environment.APIStatus.success.code
                ) {
                    this.activities = res;
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

    // These colors should present in style.css
    colours: string[] = [
        "red",
        "green",
        "blue",
        "yellow",
        "pink",
        "indigo",
        "purple",
        "fuchsia",
        "light-blue",
        "teal",
        "rose"
    ];

    getColour(index: number) {
        let colour = "";
        if (index > this.colours.length) {
            index %= this.colours.length;
        }
        colour = this.colours[index];
        return `text-${colour}-500`;
    }
}
