import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import * as moment from "moment-timezone";
import { LocalStorageService } from 'src/app/authentication/local-storage.service';
import { TimeZoneService } from 'src/app/shared/service/timezone.service';

@Component({
  selector: 'app-rcreport-calendar',
  templateUrl: './rcreport-calendar.component.html',
  styleUrls: ['./rcreport-calendar.component.css']
})
export class RcreportCalendarComponent implements OnInit {

  constructor(private timezone: TimeZoneService,
    private localStorage: LocalStorageService) { }

     //calender variables
  public today: Date;
  public yesterday: Date;
  defaultvalue: any;
  public weekStart: Date;
  public weekEnd: Date;
  public monthStart: Date;
  public monthEnd: Date;
  public lastStart: Date;
  public lastEnd: Date;
  public yearStart: Date;
  public yearEnd: Date;
  public sevendaysStart: Date;
  public currentMonth: Date;
  public fifteenDays: Date;
  public thirtyDays: Date;
  public sixtyDays: Date;
  public fifthDays: Date;
  timeZone: any[] = [];
  public format: string = 'MMM dd yyyy';
  label: any;
  selectedDates: any;
  fromDate: any;
  toDate: any;
  customRange: boolean = false;
  selectedLabel: any;
  @Input() openDropDown: boolean = false;
  @Input() detailedSearch: boolean = false;
  @Input() numberOfRows = 0;
  @Input() fromPage = ""


  includeToday: boolean = false;
  maxDate: Date;
  startDate: any;
  endDate: any;

  // calender variable


  @Input() pageTitle: string


  ngOnInit(): void {

    let user: any = this.localStorage.getLocal("user")
    const items = JSON.parse(user);
    let zone = items.tz;

    // this.timezone.getTimeZone().subscribe((dat: any) => {
    //   dat.forEach((data: any) => {
    //     this.timeZone.push({
    //       key: data.offset,
    //       value: data.display_name,
    //       zone: data.zone_name
    //     });
    //   })

    //   this.timeZone.filter((el: any) => {
    //     if (el.zone === items.time_zone) {
    //       zone = el;
    //     }
    //   })
    // })
    // console.log(zone);
    
    this.sevendaysStart = new Date(moment().tz(zone).subtract(7, 'd').format('L'))

    this.today = new Date(moment().tz(zone).format('L'))
    this.yesterday = new Date(moment().tz(zone).subtract(1, 'd').format('L'))
    this.weekStart = new Date(moment().tz(zone).startOf('weeks').format('L'))
    this.weekEnd = new Date(moment().tz(zone).subtract(1, 'weeks').endOf('weeks').format('L'))
    this.monthEnd = new Date(moment().tz(zone).subtract(1, 'months').endOf('months').format('L'))
    this.monthStart = new Date(moment().tz(zone).subtract(0, 'months').startOf('months').format('L'))
    this.currentMonth = new Date(moment().tz(zone).subtract(0, 'months').startOf('month').format('L'))
    this.thirtyDays = new Date(moment().tz(zone).subtract(30, 'd').format('L'))
    this.fifthDays = new Date(moment().tz(zone).subtract(15, 'days').format('L'))
    this.fifteenDays = new Date(moment().tz(zone)
      .subtract(15, 'days')
      .format('L'))
    this.sixtyDays = new Date(moment().tz(zone).subtract(60, 'd').format('L'));
    this.maxDate = new Date(moment().tz(zone).format('L'))

    this.label = "this week";
    this.startDate = this.weekStart
    if (this.includeToday == true) {
      this.endDate = this.today;
    } else {
      this.endDate = this.today;
    }
    this.selectedDates = {
      dateselectiontype: "this week",
      fdate: moment().tz(zone).startOf('weeks').format('YYYY-MM-DD'),
      tdate: moment().tz(zone).format('YYYY-MM-DD')
    }
    this.selectedDateEmitter.emit(this.selectedDates)
    //this.listData( this.selectedDates);

  }

  @Output() selectedDateEmitter = new EventEmitter()

  dateFilter(event) {
    if (event.text = "") {

    }
// console.log(event);

    let date1 = event.value[0]
    let date2 = event.value[1]
    this.fromDate = moment(new Date(date1)).format('YYYY-MM-DD')
    this.toDate = moment(new Date(date2)).format('YYYY-MM-DD')

    this.customRange = !this.customRange;
    this.openDropDown = false;

    let selectionType = event.event.target.innerText;

    this.selectedLabel = event.event.target.innerText;

    let firstUpper = selectionType.substr(0, 1).toLowerCase() + selectionType.substr(1);

    this.selectedLabel = firstUpper;

    if (event.event.type !== "click") {
      this.selectedLabel = "custom range";
    } else if (selectionType == "APPLY") {
      firstUpper = 'custom range';
      this.selectedLabel = "custom range";
    } else if (!selectionType.toLowerCase().includes('today') && !selectionType.toLowerCase().includes('yesterday') && !selectionType.toLowerCase().includes('last') && !selectionType.toLowerCase().includes('this')) {
      this.selectedLabel = "custom range";
    }


    this.label = this.selectedLabel
    if (this.label && this.fromDate && this.toDate) {
      this.selectedDates = {
        dateselectiontype: this.label,
        fdate: this.fromDate,
        tdate: this.toDate
      }
    }
    //API CALL
    this.selectedDateEmitter.emit(this.selectedDates)
  }

  submitDate() {

  }

}
