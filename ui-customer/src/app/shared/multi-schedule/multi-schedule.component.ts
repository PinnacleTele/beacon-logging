import {
    Component,
    EventEmitter,
    Input,
    OnDestroy,
    OnInit,
    Output,
    ViewChild
} from "@angular/core";
import { FormArray, FormBuilder, FormGroup } from "@angular/forms";
import * as moment from "moment-timezone";
import { LocalStorageService } from "src/app/authentication/local-storage.service";
import { DataSharingService } from "src/app/core/data-sharing.service";
import { CONSTANTS } from "../campaigns.constants";
import { CommonService } from "../commonservice";
import { MultiDateTimeComponent } from "../multi-date-time/multi-date-time.component";
import { TimeZoneService } from "../service/timezone.service";
@Component({
    selector: "app-multi-schedule",
    templateUrl: "./multi-schedule.component.html",
    styleUrls: ["./multi-schedule.component.css"]
})
export class MultiScheduleComponent implements OnInit, OnDestroy {
    tzList: any = [];

    zoneDate: any;

    defaultDate = new Date();

    minDate: any;

    timeZone: any = [];

    zoneTime: any;

    selectedTzOffSet: any = "";

    openPreviewModal = false;

    @Input() schedule: boolean;

    @Input() campaignForm: any;

    @Input() campaignType: string;

    @Input() action = "schedule";
    @Input() trafficType: any;
    
    @Output() closePreviewEmitter = new EventEmitter<boolean>();

    @ViewChild(MultiDateTimeComponent, { static: false })
    multiSchedule: MultiDateTimeComponent;

    usersData: any;

    scheduleBufferTimeInMinutes = CONSTANTS.CAMPAIGN_SCHEDULE_BUFFER_IN_MINS;

    showValidationError = false;

    errorMessage: String;

    constructor(
        private timezone: TimeZoneService,
        private localStorage: LocalStorageService,
        private fb: FormBuilder,
        private dataSharingService: DataSharingService,
        private commonService: CommonService
    ) { }

    ngOnDestroy(): void {
        this.dataSharingService.selectedISTTimes.clear();
        this.dataSharingService.scheduledTimes.clear();
    }

    ngOnInit(): void {
        this.zoneDate = this.defaultDate;

        const user: any = this.localStorage.getLocal("user");
        this.usersData = JSON.parse(user);
        let t = moment();
        if (this.usersData.tz) {
            t = moment().tz(this.usersData.tz);
        }
        this.minDate = t;

        this.dataSharingService.previewDates.clear();
        this.commonService.selectedDates.clear();

        this.timezone.getTimeZone().subscribe((dat) => {
            dat.forEach((data: any) => {
                this.timeZone.push({
                    key: data.offset,
                    value: data.display_name,
                    zone: data.zone_name,
                    shortName: data.short_name
                });
            });

            this.tzList = this.timeZone;

            let zone: any;
            this.tzList.filter((el: any) => {
                if (el.zone === this.usersData.tz) {
                    zone = el;
                }
            });

            const shortname = zone.shortName;

            this.selectedTzOffSet = zone.value;
            this.dataSharingService.dataStore.set(
                "timezoneselected",
                this.selectedTzOffSet
            );

            this.dataSharingService.dataStore.set(
                "user_tz_shortname",
                shortname
            );

            this.getSelectedTz(zone);
        });

        this.scheduleForm = this.fb.group({
            timings: this.fb.array([])
        });
        this.Dates.push(this.newShcedule());
    }

    scheduleForm: FormGroup;

    getSelectedTz(event: any) {
        this.showValidationError = false;
        while (this.Dates.value.length !== 1) {
            this.Dates.removeAt(1);
        }
        if (this.dataSharingService.scheduledTimes.size > 0) {
            this.dataSharingService.scheduledTimes.clear();
        }

        if (this.dataSharingService.previewDates.size > 0) {
            this.dataSharingService.previewDates.clear();
        }

        if (this.commonService.selectedDates.size > 0) {
            this.commonService.selectedDates.clear();
        }

        if (event && event.key) {
            this.dataSharingService.dataStore.set(
                "timezoneselected",
                event.key
            );
            this.dataSharingService.dataStore.set("timezone", event.zone);
            event.shortName = moment().tz(event.zone).format("z");
            this.dataSharingService.dataStore.set(
                "zoneShortName",
                event.shortName
            );
            const zone: string = this.dataSharingService.dataStore.get(
                "timezone"
            );
            let today = moment().tz(zone);
            today = today.add(this.scheduleBufferTimeInMinutes, "minutes");
            const year = today.get("year");
            const month = today.get("month");
            const date = today.get("date");
            const hour = today.get("hour");
            const minute = today.get("minute");
            const second = today.get("second");
            this.zoneDate = new Date(year, month, date, hour, minute, second);
            this.minDate = this.zoneDate;
            this.zoneTime = today.format("hh:mm A");
        } else {
            this.dataSharingService.dataStore.set("timezoneselected", null);
        }
    }

    newShcedule(): FormGroup {
        return this.fb.group({
            scheduledDate: ""
        });
    }

    onClosePreview() {
        this.openPreviewModal = false;
        this.action = "Schedule";
        this.schedule = false;
        this.closePreviewEmitter.emit(false);
    }
   

    moreSchedule(event: any) {

        const formArray = this.Dates;
        formArray.insert(0, this.fb.control(formArray.length + 1));
      
       
        
    }

    get Dates(): FormArray {
        return this.scheduleForm.get("timings") as FormArray;
    }

    removeSchedule(event: any) {              
        let index = parseInt(event)
        this.dataSharingService.previewDates.delete(event +'');
       
      
        this.Dates.removeAt(event);  
       
         // previewDates time update key value
        const lastIndex = this.dataSharingService.previewDates.size;

        for ( let i = index +1; i <= this.dataSharingService.previewDates.size ; i++) {
            this.dataSharingService.previewDates.forEach((value:any,id:any)=>{
              if (i == id) {
                    this.dataSharingService.previewDates.set(i - 1 +'',value);
                 }
             });
           if (i == this.dataSharingService.previewDates.size) {
            this.dataSharingService.previewDates.delete(lastIndex + '');
           }
        }
           // scheduledTimes time update key value
        this.dataSharingService.scheduledTimes.delete(event +'' );
        const lastIndex2 = this.dataSharingService.scheduledTimes.size;
        for ( let i = index +1; i <= this.dataSharingService.scheduledTimes.size ; i++) {
            this.dataSharingService.scheduledTimes.forEach((value:any,id:any)=>{
              if (i == id) {
                this.dataSharingService.scheduledTimes.set(i - 1 +'',value);
              }
             });
           if (i == this.dataSharingService.scheduledTimes.size) {
            this.dataSharingService.scheduledTimes.delete(lastIndex2 + '');
           }
         
        }
        // selectedISTTimes time update key value
        this.dataSharingService.selectedISTTimes.delete(event + '');
        let lastIndex3 = this.dataSharingService.selectedISTTimes.size;
        for ( let i = index +1; i <= this.dataSharingService.selectedISTTimes.size ; i++) {
            this.dataSharingService.selectedISTTimes.forEach((value:any,id:any)=>{
              if (i == id) {
                this.dataSharingService.selectedISTTimes.set(i - 1 +'',value);
              }
             });
           if (i == this.dataSharingService.selectedISTTimes.size) {
            this.dataSharingService.selectedISTTimes.delete(lastIndex3 + '');
           }
        }

        lastIndex3 = this.commonService.selectedDates.size;
        for ( let i = index +1; i <= this.commonService.selectedDates.size ; i++) {
            this.commonService.selectedDates.forEach((value:any,id:any)=>{
              if (i == id) {
                    this.commonService.selectedDates.set(i - 1 +'',value);
                 }
             });
           if (i == this.commonService.selectedDates.size) {
            this.commonService.selectedDates.delete(lastIndex + '');
           }
        }

    }

    onContinue() {
        const errorMessage = this.multiSchedule.validateDates();
        if (errorMessage.length === 0) {
            if(this.commonService.selectedDates.size == 0){
                this.dataSharingService.selectedISTTimes.forEach((v, k) => {
                    this.commonService.selectedDates.set(k,v);
                });
            }
            
            this.commonService.selectedDates.forEach((v, k) => {
                this.dataSharingService.previewDates.set(k,v);
            });
            this.schedule = false;
            this.openPreviewModal = true;
        } else {
            this.showValidationError = true;
            this.errorMessage = errorMessage;
        }
    }

    closePreview() {
        this.closePreviewEmitter.emit(false);
        this.dataSharingService.dataStore.delete("timezoneselected");
    }
}
