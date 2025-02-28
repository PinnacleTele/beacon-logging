import { Component, Input, OnInit, Output, EventEmitter } from "@angular/core";
// eslint-disable-next-line import/no-extraneous-dependencies
import { RenderDayCellEventArgs } from "@syncfusion/ej2-calendars";
// eslint-disable-next-line import/no-extraneous-dependencies
import { addClass } from "@syncfusion/ej2-base";
import * as moment from "moment-timezone";
import { PreventableEventArgs } from "@syncfusion/ej2-angular-calendars";
import { DataSharingService } from "src/app/core/data-sharing.service";
import { CONSTANTS } from "../campaigns.constants";
import { LocalStorageService } from "src/app/authentication/local-storage.service";
@Component({
    selector: "app-date-picker",
    templateUrl: "./date-picker.component.html",
    styleUrls: ["./date-picker.component.css"]
})
export class DateComponent implements OnInit {
    dateFormat = CONSTANTS.DATE_FORMAT;

    userData: any;

    scheduleBufferTimeInMinutes = CONSTANTS.CAMPAIGN_SCHEDULE_BUFFER_IN_MINS;

    constructor(private dataSharingService: DataSharingService,
        private localStorage : LocalStorageService) {
        // TODO: This data should come from campaigns page.
        const tempData = {
            "27/05/2021": "27th Camp, ABCD",
            "30/05/2021": "Camp May 30",
            "11/06/2021": "11th Camp, Summer Sale, Year End Sale"
        };

        dataSharingService.setData("previousCamps", tempData);
        const user: any =this.localStorage.getLocal("user");
        this.userData = JSON.parse(user);
    }

    @Input() minDate: any;

    @Input() maxDate: any;

    @Input() zoneDate: any;

    @Input() dateToBeSelected: moment.Moment;

    selectedDate: Date | string;

    // To show some small icon/color for these selections
    @Input() pinPreviousSelections: boolean;

    previousData: any;

    prevData: Map<string, string> = new Map<string, string>();

    ngOnInit(): void {
        if (this.pinPreviousSelections) {
            this.previousData = this.dataSharingService.getData(
                "previousCamps"
            );
            if (this.previousData) {
                for (const k of Object.keys(this.previousData)) {
                    this.prevData.set(k, this.previousData[k]);
                }
            }
        }

        const timeZone = this.dataSharingService.getData("timezone");
        let today = moment.tz(timeZone);
        today = today.add(this.scheduleBufferTimeInMinutes, "minutes");

        if (this.dateToBeSelected) {
            this.dataSharingService.setData(
                "dateselected",
                today.format(this.dateFormat)
            );
            this.zoneDate = new Date(
                today.get("year"),
                today.get("month"),
                today.get("date"),
                today.get("hour"),
                today.get("minute"),
                today.get("second")
            );
            this.selectedDate1.emit(today.format(this.dateFormat));
        } else {
            this.zoneDate = new Date(
                today.get("year"),
                today.get("month"),
                today.get("date"),
                today.get("hour"),
                today.get("minute"),
                today.get("second")
            );
            this.dataSharingService.setData(
                "dateselected",
                today.format(this.dateFormat)
            );
            this.selectedDate1.emit(today.format(this.dateFormat));
        }
    }

    pinDates(args: RenderDayCellEventArgs): void {
        if (this.pinPreviousSelections && args) {
            const dt = moment(args.date)
                .utcOffset(CONSTANTS.UTC)
                .format(this.dateFormat);
            if (this.prevData.has(dt)) {
                const tooltip = this.prevData.get(dt) || "";
                const span: HTMLElement = document.createElement("span");
                span.setAttribute("class", "e-icons highlight");
                if (args.element) {
                    addClass([args.element], ["special", "e-day"]);
                    if (args.element.firstElementChild) {
                        args.element.firstElementChild.setAttribute(
                            "title",
                            tooltip
                        );
                    }
                    args.element.setAttribute("title", tooltip);
                    args.element.appendChild(span);
                }
            }
        }
    }

    @Output() selectedDate1 = new EventEmitter();

    onDateSelection(args: any) {
        if (args && args.value) {
            const timezone = this.dataSharingService.getData("timezone");
            // const dt = moment(args.value).format(this.dateFormat);
            // const dateselected = moment.tz(`${dt} 00:00:00`, "DD/MM/YYYY HH:mm:ss", timezone);
            const dt = moment(args.value).format("DD/MM/YYYY HH:mm:ss");
            const dateselected = moment.tz(dt, "DD/MM/YYYY HH:mm:ss", timezone);
            const date = dateselected.format(this.dateFormat);
            this.dataSharingService.setData("dateselected", date);
            this.zoneDate = new Date(
                dateselected.get("year"),
                dateselected.get("month"),
                dateselected.get("date"),
                dateselected.get("hour"),
                dateselected.get("minute"),
                dateselected.get("second")
            );
            // console.log(`onDateSelection - if - args.value ${args.value} dt ${dt} date ${date} dateselected ${dateselected.format("DD/MM/YYYY hh:mm:ss A Z")} zoneDate ${this.zoneDate}`);
            this.selectedDate1.emit(date);
        } else {
            const dateselected = moment().tz(this.userData.tz);
            this.zoneDate = new Date(
                dateselected.get("year"),
                dateselected.get("month"),
                dateselected.get("date"),
                dateselected.get("hour"),
                dateselected.get("minute"),
                dateselected.get("second")
            );
            // console.log(`onDateSelection - else - args.value ${args.value} dateselected ${dateselected.format("DD/MM/YYYY hh:mm:ss A Z")} zoneDate ${this.zoneDate}`);
            this.dataSharingService.setData("dateselected", null);
        }
    }

    onClose(args: PreventableEventArgs): void {
        if (args.preventDefault) {
            args.preventDefault();
        }
    }
}
