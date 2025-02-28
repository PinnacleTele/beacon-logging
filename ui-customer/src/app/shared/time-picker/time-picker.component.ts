import { Component, Input, OnInit, Output, EventEmitter } from "@angular/core";
import * as moment from "moment-timezone";
import { LocalStorageService } from "src/app/authentication/local-storage.service";

import { DataSharingService } from "src/app/core/data-sharing.service";
import { UtilityService } from "src/app/core/utility.service";
import { CONSTANTS } from "../campaigns.constants";

@Component({
    selector: "app-time-picker",
    templateUrl: "./time-picker.component.html",
    styleUrls: ["./time-picker.component.css"]
})
export class TimeComponent implements OnInit {
    interval: number;

    timeFormat12 = CONSTANTS.TIME_FORMAT_12;

    timeFormat24 = CONSTANTS.TIME_FORMAT_24;

    @Input() timeToBeSelected: any;

    @Input() zoneTime: any;

    @Input() UTCselectTime: any;

    selectedTime: any;

    mintime: any;

    isStrictMode = true;

    @Output() selectedTime1 = new EventEmitter();

    @Input() minValue: Date = new Date();

    scheduleBufferTimeInMinutes = CONSTANTS.CAMPAIGN_SCHEDULE_BUFFER_IN_MINS;

    roundingInMinutes = CONSTANTS.TIME_ROUNDING_IN_MINS;

    constructor(
        private dataSharingService: DataSharingService,
        private localStorage : LocalStorageService,
    ) {}

    ngOnInit(): void {
        const userData: any =this.localStorage.getLocal("user");
        const userDataAsJson = JSON.parse(userData);

        const timeZone = this.dataSharingService.getData("timezone");
        let today = moment.tz(timeZone);
        today = today.add(this.scheduleBufferTimeInMinutes, "minutes");
        this.minValue = new Date(
            today.get("year"),
            today.get("month"),
            today.get("date"),
            today.get("hour"),
            today.get("minute"),
            today.get("second")
        );
        this.minValue = this.getRoundedDate(
            this.roundingInMinutes,
            this.minValue
        );
        this.interval = CONSTANTS.TIME_PICKER_TIME_DIFFERENCE;
        if (this.timeToBeSelected) {
            // this.selectedTime = this.timeToBeSelected.format(this.timeFormat12);
            const selectedValue = today; // moment(this.minValue).tz(timeZone);
            // const selectedValue = moment(this.minValue).tz(timeZone);
            // this.selectedTime = selectedValue.format(this.timeFormat12);
            this.selectedTime = this.zoneTime;
            this.dataSharingService.setData(
                "timeselected24Hrs",
                selectedValue.format(this.timeFormat24)
            );
            this.selectedTime1.emit(this.selectedTime);
        } else {
            this.selectedTime = moment
                .tz(userDataAsJson.tz)
                .format(this.timeFormat12);
            this.dataSharingService.setData(
                "timeselected24Hrs",
                moment.tz(userDataAsJson.tz).format(this.timeFormat24)
            );
            this.selectedTime1.emit(this.selectedTime);
        }
    }

    onTimeSelection(args: any) {
        if (args && args.value) {
            const timezone = this.dataSharingService.getData("timezone");
            const dateselected = this.dataSharingService.getData(
                "dateselected"
            );
            const timeselected = moment(args.value).format(this.timeFormat24);
            const selectedDateTime = moment.tz(
                `${dateselected} ${timeselected}`,
                "DD/MM/YYYY HH:mm:ss",
                timezone
            );
            // const time24Hrs = moment(args.value).tz(timezone).format(this.timeFormat24);
            const time24Hrs = selectedDateTime.format(this.timeFormat24);
            this.dataSharingService.setData("timeselected24Hrs", time24Hrs);
            // this.selectedTime = moment(args.value).tz(timezone).format(this.timeFormat12);
            this.selectedTime = selectedDateTime.format(this.timeFormat12);
            this.selectedTime1.emit(this.selectedTime);
        } else {
            this.dataSharingService.setData("timeselected24Hrs", null);
            this.selectedTime1.emit(null);
        }
    }

    getRoundedDate = (minutes: number, d = new Date()) => {
        const ms = 1000 * 60 * minutes; // convert minutes to ms
        // const roundedDate = new Date(Math.round(d.getTime() / ms) * ms);
        const roundedDate = new Date(Math.ceil(d.getTime() / ms) * ms);
        const roundTime = moment(roundedDate).format("hh:mm A");
        this.zoneTime = roundTime;
        return roundedDate;
    };
}
