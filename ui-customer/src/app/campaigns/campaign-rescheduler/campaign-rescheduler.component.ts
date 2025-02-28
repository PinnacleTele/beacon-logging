import { Component, Input, OnInit, OnChanges, Output, EventEmitter, ViewChild } from "@angular/core";
// eslint-disable-next-line import/no-extraneous-dependencies
import { RenderDayCellEventArgs } from "@syncfusion/ej2-calendars";
// eslint-disable-next-line import/no-extraneous-dependencies
import { addClass } from "@syncfusion/ej2-base";
import * as moment from "moment";
import { PreventableEventArgs } from "@syncfusion/ej2-angular-calendars";
import { TimeZoneService } from "src/app/shared/service/timezone.service";
import { UtilityService } from "src/app/core/utility.service";
import { CONSTANTS } from "src/app/shared/campaigns.constants";
import { CampaignsService } from "../campaigns.service";
import { Router } from "@angular/router";
import { HttpErrorResponse } from "@angular/common/http";
import { LocalStorageService } from "src/app/authentication/local-storage.service";


@Component({
  selector: 'app-campaign-rescheduler',
  templateUrl: './campaign-rescheduler.component.html',
  styleUrls: ['./campaign-rescheduler.component.css']
})
export class CampaignReschedulerComponent implements OnInit {

  constructor(private timezone: TimeZoneService,
    private utility: UtilityService,
    private campaignService: CampaignsService,
    private router: Router,
    private localStorage: LocalStorageService,
  ) { }

  ngOnInit(): void {
    let user: any = this.localStorage.getLocal("user")
    this.userDetails = JSON.parse(user)
    this.actual_zone = this.userDetails.tz
    this.getTimeZones();
    this.rescheduledZone = this.scheduledZone;
    this.mindate = moment.tz(this.userDetails.tz).toDate();

  }
  timeZones: any = [];
  timeZoneList: any = [];
  userDetails: any;
  selectedTzOffSet: any;
  zone: string;
  editPopup = false;
  actual_zone = "";
  editResponse: any;
  selectedZone: string = "";
  selectedate: any;
  selectedTime: any;
  zoneDate;
  isStrictMode = true;
  offset: any;
  login_offset: any;
  zoneExe: any
  minValue = new Date();
  zoneTime;
  interval = 10;
  bufferTimeError = false;
  bufferTime = CONSTANTS.EDIT_DELETE_SCHEDULE_BUFFER;
  scheduleBufferTimeInMinutes = CONSTANTS.CAMPAIGN_SCHEDULE_BUFFER_IN_MINS;
  rescheduledZone: string;
  @Input() selected: any;
  @Input() campaignId: string;
  @Input() atId: string;
  @Input() scheduledZone: string;
  @Input() scheduledTime: string;
  // new change for firefox
  @Input() scheduleDate: string;
  @Input() scheduleTime: string;

  datePart = ""
  timePart = ""

  newOff: string;
  mindate: any


  getTimeZones() {
    this.timezone.getTimeZone().subscribe((data) => {
      // console.log(data);

      data.forEach((element: any) => {
        this.timeZones.push({
          key: element.offset,
          value: element.display_name,
          zone: element.zone_name,
          short_name: element.short_name
        });
      });
      this.timeZoneList = this.timeZones

      //default time zone setting
      let userzone: any;
      let scheduledzone: any;
      this.timeZoneList.filter((el: any) => {


        if (el.zone === this.userDetails.tz) {
          userzone = el;
          this.login_offset = el.key;
          this.zoneExe = moment().tz(this.userDetails.tz).format("z");
        }

        if (this.scheduledZone == el.zone) {
          this.newOff = el.key;
          scheduledzone = el;

        }

      })                                                        //2021-10-14 23:05:00 (+04)
      this.selectedTzOffSet = scheduledzone.value;
      this.offset = userzone.key
      this.zoneDate = moment(this.scheduledTime, 'YYYY-MM-DD HH:mm:ss ').toDate()
      this.zoneDate = new Date(this.zoneDate)
      this.zone = this.userDetails.tz;
      let zoneTime = moment(this.zoneDate).toDate()
      this.zoneTime = zoneTime


      const zoneDate = moment(this.zoneTime)

      if (moment(zoneDate).isAfter(moment.tz(this.rescheduledZone), 'D')) {
        this.minTime = (moment(zoneDate).startOf('day').toDate());
        //   this.datePart=this.minTime.format("DD-MM-YYYY")
      }
      else {
        let zoneTime2 = moment.tz(this.rescheduledZone).add(this.scheduleBufferTimeInMinutes, "minutes").format('LLLL')

        let zoneTime1 = this.getRoundedDate(10, new Date(zoneTime2))

        this.minTime = zoneTime1;
        //   this.datePart=this.minTime.format("DD-MM-YYYY")

      }

      this.selectedate = moment(this.scheduledTime, 'YYYY-MM-DD HH:mm:ss ').format("MMM DD YYYY")
      this.selectedTime = moment(this.scheduledTime, 'YYYY-MM-DD HH:mm:ss ').format("hh:mm A")
      this.ISTConvertion(this.selectedate, this.selectedTime, this.newOff, scheduledzone.zone);
      this.bufferTimeValidation();
    })
  }


  getSelectedTz(event) {
    this.isStrictMode = false;

    if (event) {
      this.rescheduledZone = event.zone;
      this.selectedTzOffSet = event.value;
      //this.userDetails = event;
      this.offset = event.key;
      this.newOff = event.key;
      this.zone = event.zone;
      this.selectedate = moment.tz(event.zone).format("MMM DD YYYY");
      const zoneDate = moment.tz(event.zone).format("LLLL");
      let zoneTime1 = this.getRoundedDate(10, new Date(zoneDate));
      let zoneTime = moment(zoneTime1).add(this.scheduleBufferTimeInMinutes, "minutes").toDate();
      this.mindate = zoneDate;
      this.zoneTime = zoneTime;
      this.minTime = zoneTime;
      this.zoneDate = new Date(zoneDate);
      this.choosenDateTime();

    }



  }


  onDateChange(event) {

    if (event.event) {
      this.selectedate = moment(event.value).format("MMM DD YYYY ")
      if (moment(event.value).isAfter(moment.tz(this.rescheduledZone), 'D')) {
        this.minTime = (moment(event.value).startOf('day').toDate());
        this.zoneTime = this.minTime
        this.choosenDateTime()

        this.bufferTimeValidation()

      } else {
        const zoneDate = moment.tz(this.rescheduledZone).add(2, "minutes").format("LLLL")
        let zoneDate1 = this.getRoundedDate(10, new Date(zoneDate))
        let zoneDate2 = moment(zoneDate1).add(this.scheduleBufferTimeInMinutes, "minutes")
        //  let zoneTime2 = moment(zoneDate).add(15, "minutes");
        //  let zoneTime1 = this.getRoundedDate(10, zoneTime2.toDate())
        this.zoneTime = zoneDate2.toDate();
        this.minTime = this.zoneTime
        this.bufferTimeValidation()
      }
    }

  }

  currentTime() {
    let min = moment.tz(this.userDetails.tz).toDate();
    return min
  }



  onTimeChange(event) {

    let time = ""
    time = event.text
    this.selectedTime = event.text


    this.choosenDateTime()

    this.bufferTimeValidation()


  }
  minTime: any
  getRoundedDate = (minutes, d = new Date()) => {
    let add = 0
    if ((moment(d).get("minutes") % 10) < 4 && (moment(d).get("minutes") % 10) >= 0) {
      add = 5 - (moment(d).get("minutes") % 10);
    }
    else if ((moment(d).get("minutes") % 10) == 9) {
      add = 6;
    }
    else {
      add = 10 - (moment(d).get("minutes") % 10);
    }
    let roundedDate = moment(d).add(add, "minutes").toDate();
    let roundTime = moment(d).add(add, "minutes").format("hh:mm A")
    this.minTime = roundTime
    return roundedDate
  }


  choosenDateTime() {
    let selected = moment(this.zoneTime).format("hh:mm A")
    const formattedInputDate = `${this.selectedate} ${selected} ${this.offset}`;
    if (this.selectedate && this.selectedTime && this.offset) {
      this.ISTConvertion(this.selectedate, this.selectedTime, this.newOff, this.rescheduledZone)
    }
  }

  ISTdate: any
  finalOut: { date: string, time: string, zone: string, selectedTime: string } = { date: "", time: "", zone: "", selectedTime: "" }

  ISTConvertion(date: string, time: string, offset: string, tzString: string) {

    const incomingDate = moment(`${date} ${time}`, 'MMM DD YYYY hh:mm A').format('YYYY-MM-DD HH:mm');
    //const tzString = this.zone;
    const incomingTZ = moment.tz(incomingDate, tzString);
    const selectedDateInUserTZ = incomingTZ.tz(this.actual_zone);
    this.ISTdate = selectedDateInUserTZ.format("MMM DD YYYY hh:mm A");

    if (date && time) {
      this.finalOut.date = moment(date).format("YYYY-MM-DD");
      this.finalOut.time = moment(time, ["h:mm A Z"]).format("HH:mm:ss");
      this.finalOut.zone = this.rescheduledZone;
      this.finalOut.selectedTime = this.ISTdate;
      this.bufferTimeValidation();
    }

  }

  @Output() timeparams = new EventEmitter();
  @Output() cancelButton = new EventEmitter<boolean>();
  onContinue() {
    if (this.bufferTimeValidation() && this.sameTimeValidation()) {


      this.campaignService.campaignReschedule(
        this.campaignId,
        this.atId, this.finalOut.date,
        this.finalOut.time,
        this.finalOut.zone).subscribe((res: any) => {
          this.editResponse = res;

          this.editPopup = true;
          this.timeparams.emit(this.finalOut)
        },
          (error: HttpErrorResponse) => {

            this.editResponse = {
              message: "Something Went Wrong",
              statusCode: 411
            }
            this.editPopup = true;
          }
        )
    }


  }
  onCancel() {
    this.cancelButton.emit(false);


  }
  tryAgainEditPopup(event) {

  }
  closeDeletePopup(event) {
    this.editPopup = event;
  }
  continueDeletePopup(event) {
    this.router.navigate(["/campaigns/scheduledlist"])
    this.editPopup = false;
    this.cancelButton.emit(false);



  }

  bufferTimeValidation() {
    let selectedDate = moment(this.finalOut.selectedTime, 'MMM DD YYYY hh:mm A').format('YYYY-MM-DD HH:mm');
    const selectedDateInUserTZ = moment.tz(selectedDate, this.userDetails.tz);
    const userCurrentTZ = moment.tz(this.userDetails.tz);

    if (this.ISTdate) {
      if (selectedDateInUserTZ.diff(userCurrentTZ, "minutes") <= this.scheduleBufferTimeInMinutes) {
        this.bufferTimeError = true;
        return false;
      }
      else {
        this.bufferTimeError = false;
        return true;
      }
    }
  }


  sameTimeValidation() {

    if (this.rescheduledZone == this.scheduledZone) {
      if (this.selectedate && this.selectedTime && this.offset) {
        const preScheduled = moment(this.scheduledTime, 'YYYY-MM-DD HH:mm:ss ')
        const formattedInputDate = `${this.selectedate} ${this.selectedTime} ${this.offset}`;
        //Oct 17 2021 07:45 PM +05:30
        const postScheduled = (this.utility.convertTimeZone(
          formattedInputDate,
          "MMM DD YYYY hh:mm A Z",
          this.offset,
          "MMM DD YYYY hh:mm A"
        ));



        if (moment(preScheduled).isSame(moment(postScheduled))) {

          return false
        }
        else {
          return true
        }
      }
    }
    else {
      return true
    }
  }
}
