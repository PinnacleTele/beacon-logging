import {
    Component,
    EventEmitter,
    Input,
    OnChanges,
    OnInit,
    Output,
    SimpleChanges,
    ViewChild
} from "@angular/core";
import * as moment from "moment-timezone";
import { DataSharingService } from "src/app/core/data-sharing.service";
import { FormBuilder } from "@angular/forms";
import { CONSTANTS } from "../campaigns.constants";
import { CommonService } from "../commonservice";
import { TimeComponent } from "../time-picker/time-picker.component";
import { LocalStorageService } from "src/app/authentication/local-storage.service";

@Component({
    selector: "app-multi-date-time",
    templateUrl: "./multi-date-time.component.html",
    styleUrls: ["./multi-date-time.component.css"]
})
export class MultiDateTimeComponent implements OnInit, OnChanges {
    tzList: any = [];

    defaultDate = new Date();

    timeZone: any = [];

    selectedTzOffSet = "";

    scheduleBufferTimeInMinutes = CONSTANTS.CAMPAIGN_SCHEDULE_BUFFER_IN_MINS;

    dateAndTimeToBeSelected = moment();

    date: string;

    time: string;

    selectedDate: any;

    dateFormat = CONSTANTS.DATE_FORMAT;

    timeFormat12 = CONSTANTS.TIME_FORMAT_12;

    @Input() zoneDate: any;

    @Input() minDate: any;

    @Input() zoneTime: any;

    @Input() index: number;

    @Input() id: any =0;

    @Input() minTime: any;

    @Output() moreSchedule = new EventEmitter();

    @Output() removeScheduleEvent = new EventEmitter();

    timearray: any[] = [];

    selectedValues = [];

    selectedZoneDate: any;

    userData: any;

    idCount: any = 0 + '';

    @ViewChild("timepicker") timePicker: TimeComponent;

    constructor(
        private fb: FormBuilder,
        private localStorage: LocalStorageService,
        private dataSharingService: DataSharingService,
        private commonService: CommonService
    ) { }

    ngOnChanges(changes: SimpleChanges): void {
    //     console.log(this.id);
        
    //    console.log(this.idCount, 'before');
        if (this.idCount < this.id) {
            this.newDate();
        }
        this.idCount = this.id;
    }

    ngOnInit(): void {
        const user: any = this.localStorage.getLocal("user");
        this.userData = JSON.parse(user);
        this.dateAndTimeToBeSelected = moment()
            .tz(this.userData.tz)
            .add(this.scheduleBufferTimeInMinutes, "minutes");
    }

    dateTimeForm = this.fb.group({
        addDynamicElement: this.fb.array([])
    });

    get TimeZone() {
        return this.dataSharingService.getData("timezoneselected");
    }

    selectedTimeEvent(event: any) {
        this.timearray = [];
        this.time = event;
        this.dataSharingService.scheduledTimes.forEach((dat: any) => {
            this.timearray.push(dat);
        });
        this.newDate();
    }

    selectedDateEvent(event: any) {
        const timeZone = this.dataSharingService.getData("timezone");
        let today = moment().tz(timeZone);
        this.date = event;
        if (event !== today.format("DD/MM/YYYY")) {
            const dtString = `${this.date} 00:00:00`;
            const t = moment.tz(dtString, "DD/MM/YYYY hh:mm:ss", timeZone);
            this.minTime = new Date(
                t.get("year"),
                t.get("month"),
                t.get("date"),
                t.get("hour"),
                t.get("minute"),
                t.get("second")
            );
        } else {
            today = today.add(this.scheduleBufferTimeInMinutes, "minutes");
            this.minTime = new Date(
                today.get("year"),
                today.get("month"),
                today.get("date"),
                today.get("hour"),
                today.get("minute"),
                today.get("second")
            );
            this.zoneTime = today.format("hh:mm A");
        }
        this.newDate();
    }

    newDate() {
        if (this.date && this.time) {
            
            const selectedDt = moment(`${this.date} ${this.time}`,'DD/MM/YYYY hh:mm A').format('YYYY-MM-DD HH:mm');
            const selectedTZ = this.dataSharingService.getData("timezone");
            //console.log(`---> selectedDt ${selectedDt}  selectedTZ ${selectedTZ}`);

            const selectedDateMomentObject = moment.tz(selectedDt, selectedTZ);

            const selectedDateMomentObjectInUserTZ = selectedDateMomentObject.clone().tz(this.userData.tz);
            const userZoneAbbr = selectedDateMomentObjectInUserTZ.format("z");
            //console.log(`userData.time_zone ${this.userData.time_zone}`);
            //console.log('selectedDateMomentObjectInUserTZ '+selectedDateMomentObjectInUserTZ.format("DD MMM YYYY hh:mm A z"));

            this.selectedDate = selectedDateMomentObjectInUserTZ.format("DD MMM YYYY hh:mm A");
            this.selectedZoneDate = selectedDateMomentObject.format(
                "DD MMM YYYY hh:mm A"
            );
            const actualdate = selectedDateMomentObject.format("DD MMM YYYY hh:mm A");

            if (this.id) {
                //console.log(this.id,'inside set');
                
                this.dataSharingService.scheduledTimes.set(
                    this.id,
                    this.selectedDate
                );

                this.dataSharingService.previewDates.set(this.id, actualdate);


                this.dataSharingService.selectedISTTimes.set(
                    this.id,
                    this.selectedZoneDate
                );
                this.userTimezoneDate(userZoneAbbr);
            }
            this.commonService.selectedDates.set(this.id, actualdate);
        }
    }

    restrict:any;

    userTimezoneDate(tz: string) {        
         this.restrict =  `${this.dataSharingService.scheduledTimes.get(
                this.id
            )} ${tz}`;
    }


    getRoundedDate = (minutes: number, d = new Date()) => {
        const ms = 1000 * 60 * minutes; // convert minutes to ms
        const roundedDate = new Date(Math.round(d.getTime() / ms) * ms);

        return roundedDate;
    };

    scheduler: Map<string, boolean> = new Map<string, boolean>();

    dateTime: any;

    addSchedule() {
        this.moreSchedule.emit(this.id);
    }

    validateDates(): String {
        const timezone = this.dataSharingService.getData("timezone");
        const today = moment()
            .tz(timezone)
            .add(this.scheduleBufferTimeInMinutes - 5, "minutes")
            .seconds(0);
        // console.log(
        //     "current time + buffer ::: ",
        //     today.format("DD/MM/YYYY hh:mm:ss A Z")
        // );
        let errorMessage = "";
        const datesSelected = new Set();

        this.dataSharingService.previewDates.forEach((v, k) => {
            const dateselected = moment.tz(
                v,
                "DD MMM YYYY hh:mm:ss A",
                timezone
            );

            if (dateselected.isBefore(today)) {
                errorMessage = `${CONSTANTS.ERROR_DISPLAY.multiScheduleErrorMsg
                    } ${today.format("DD MMM YYYY hh:mm A")}`;
            }

            if (!datesSelected.has(dateselected.format("YYYY-MM-DD HH:mm"))) {
                datesSelected.add(dateselected.format("YYYY-MM-DD HH:mm"));
            } else {
                errorMessage = `${dateselected.format("DD MMM YYYY hh:mm A")}
                 ${CONSTANTS.ERROR_DISPLAY.multiScheduleDuplicateErrorMsg}`;
            }
        });
        //console.log(this.dataSharingService.previewDates);
        
        return errorMessage;
    }

    removeSchedule(index: any) {
        this.removeScheduleEvent.emit(this.id);
    }

    isError() {
        return this.scheduler.get(this.id);
    }
}
