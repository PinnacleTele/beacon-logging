import { HttpErrorResponse } from "@angular/common/http";
import { Component, OnInit } from "@angular/core";
import { environment } from "src/environments/environment";
import { Summary } from "../dashboard.models";
import { SummaryService } from "./summary.service";

@Component({
    selector: "app-summary",
    templateUrl: "./summary.component.html",
    styleUrls: ["./summary.component.css"]
})
export class SummaryComponent implements OnInit {
    constructor(private summaryService: SummaryService) {}

    ngOnInit(): void {
        this.getSummaryCounts();
    }

    summary: Summary = {
        responsecode: 0,
        responsestatus: "",
        data: {
            today: {
                count: "0",
                total: "0"
            },
            completed: {
                count: "0",
                percantage: 0
            },
            running: {
                count: "0",
                percantage: 0
            },
            failed: {
                count: "0",
                percantage: 0
            }
        }
    };

    getSummaryCounts() {
        this.summaryService.getSummaryCounts().subscribe(
            (res: Summary) => {
                if (
                    res.responsestatus === environment.APIStatus.success.text &&
                    res.responsecode > environment.APIStatus.success.code
                ) {
                    this.summary = res;
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
