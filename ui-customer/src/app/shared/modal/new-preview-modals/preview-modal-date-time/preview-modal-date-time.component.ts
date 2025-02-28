import { Component, Input } from "@angular/core";
//import * as moment from "moment";
import { DataSharingService } from "src/app/core/data-sharing.service";
import * as moment from "moment-timezone";
import { CONSTANTS } from "../../../campaigns.constants";
import { TimeZoneService } from "../../../service/timezone.service";

@Component({
    selector: "app-preview-modal-date-time",
    templateUrl: "./preview-modal-date-time.component.html",
    styleUrls: ["./preview-modal-date-time.component.css"]
})
export class PreviewModalDateTimeComponent {
    dateAndTimeToBeSelected = moment().add(1, "hours");

    zoneDate: any;

    minDate: any;

    zoneTime: any;

    selectedTzOffSet = "";

    tzList: any = [];

    timeZone: any = [];

    defaultDate = new Date();

    constructor(
        private dataSharingService: DataSharingService,
        private timezone: TimeZoneService
    ) {}

    ngOnInit(): void {
        this.zoneDate = this.defaultDate;
        const t = moment(this.defaultDate);
        this.minDate = t;
        const li = moment(new Date()).format("hh:mm A");
        this.zoneTime = li;
        this.timezone.getTimeZone().subscribe((dat) => {
            dat.forEach((data: any) => {
                this.timeZone.push({
                    key: data.offset,
                    value: data.display_name,
                    zone: data.zone_name
                });
            });
            this.tzList = this.timeZone;
            
        });
        // this.selectedTzOffSet = CONSTANTS.DEFAULT_OFF_SET;
        // this.dataSharingService.setData(
        //     "timezoneselected",
        //     this.selectedTzOffSet
        // );
    }

    getSelectedTz(event: any) {
        
        
        if (event && event.key) {
            this.dataSharingService.setData("timezoneselected", event.key);
            this.dataSharingService.setTime("timezone", event.zone);
        } else {
            this.dataSharingService.setData("timezoneselected", null);
        }
        const zone: string = this.dataSharingService.getTime("timezone");
        const check = moment.tz('/etc/localtime').utcOffset();
        var timedifference = new Date().getTimezoneOffset();

        
        const timDate = moment.tz(zone).format("LLLL");
        const time = moment.tz(zone).format("hh:mm A");
        const zoneDate = new Date(timDate);
        this.zoneDate = zoneDate;
        this.zoneTime = time;
        this.minDate = zoneDate;

        
    }

    showError(): boolean {
        const date = this.dataSharingService.getData("dateselected");
        const time = this.dataSharingService.getData("timeselected24Hrs");
        const tz = this.dataSharingService.getData("timezoneselected");
        if (tz) {
            return false;
        }
        if (date && time) {
            return false;
        }
        return true;
    }
}
