import { HttpErrorResponse } from "@angular/common/http";
import { Component, OnInit } from "@angular/core";
import { environment } from "src/environments/environment";
import { Statistics } from "../dashboard.models";
import { StatisticsService } from "./statistics.service";

@Component({
    selector: "app-statistics",
    templateUrl: "./statistics.component.html",
    styleUrls: ["./statistics.component.css"]
})
export class StatisticsComponent implements OnInit {
    constructor(private statService: StatisticsService) {}

    statistics: Statistics;

    ngOnInit(): void {
        this.getAccountStatistics();
    }

    getAccountStatistics() {
        this.statService.getAccountStatistics().subscribe(
            (res: Statistics) => {
                if (
                    res.responsestatus === environment.APIStatus.success.text &&
                    res.responsecode > environment.APIStatus.success.code
                ) {
                    this.statistics = res;
                } else if (
                    res.responsestatus === environment.APIStatus.error.text &&
                    res.responsecode < environment.APIStatus.error.code
                ) {
                    //console.log(res.responsestatus);
                }
            },
            (error: HttpErrorResponse) => {
                
            }
        );
    }
}
