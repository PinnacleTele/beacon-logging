import {
    ChangeDetectorRef,
    Component,
    AfterContentChecked,
    OnInit
} from "@angular/core";
import { DataSharingService } from "src/app/core/data-sharing.service";
import { UtilityService } from "src/app/core/utility.service";
import { CONSTANTS } from "../../../campaigns.constants";

@Component({
    selector: "app-preview-schedule",
    templateUrl: "./preview-schedule.component.html",
    styleUrls: ["./preview-schedule.component.css"]
})
export class PreviewScheduleComponent implements AfterContentChecked {
    constructor(
        private dataSharingService: DataSharingService,
        private utilityService: UtilityService,
        private cdref: ChangeDetectorRef
    ) {}

    selectedTimeStamp = "";

    dateTest: any;

    timeTest: any;

    minDate: any;

    scheduledTiming:any=[]

    ngAfterContentChecked() {
        this.cdref.detectChanges();
    }


    getScheduleDateInIST(): string {
        const date: string = this.dataSharingService.getData("dateselected");

        const time: string = this.dataSharingService.getData(
            "timeselected24Hrs"
        );
        const offset: string = this.dataSharingService.getData(
            "timezoneselected"
        );
        const zone: string = this.dataSharingService.getTime("timezone");
        const formattedInputDate = `${date} ${time} ${offset} `;

        let istDate = "";
        if (
            date &&
            time &&
            offset &&
            !formattedInputDate.toUpperCase().includes("INVALID DATE")
        ) {
            istDate = this.utilityService.convertTimeZone(
                formattedInputDate,
                "DD/MM/YYYY HH:mm A",
                CONSTANTS.DEFAULT_OFF_SET,
                "DD MMM YYYY hh:mm A"
            );
            // TODO: How to detect IST/EST/PST etc?
            istDate = `${istDate} IST`;
        }
        return istDate;
    }

}
