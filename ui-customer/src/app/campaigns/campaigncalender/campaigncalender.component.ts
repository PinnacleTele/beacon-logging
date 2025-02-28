import { Component, OnInit, Input, Output, EventEmitter, OnChanges, ViewChild, OnDestroy } from '@angular/core';
import * as moment from 'moment-timezone';
import { TimeZoneService } from 'src/app/shared/service/timezone.service';
import { CampaignsService } from "src/app/campaigns/campaigns.service";
import { HttpErrorResponse } from '@angular/common/http';
import { SetIntervalService } from "src/app/campaigns/c_Helper/setintervalapicalls";
import {
  EnterExitRight,
  Container1,
  EnterExitTop
} from "../../shared/animation";
import { NavigationEnd, Router } from "@angular/router";
import { LocalStorageService } from 'src/app/authentication/local-storage.service';

@Component({
  selector: 'app-campaigncalender',
  templateUrl: './campaigncalender.component.html',
  styleUrls: ['./campaigncalender.component.css'],
  animations: [EnterExitRight, Container1, EnterExitTop]
})
export class CampaigncalenderComponent implements OnInit, OnChanges, OnDestroy {

  campaignLists: any[] = [];

  timeZone: any[] = [];

  @Input() List: any;

  @Input() campaignCount: any;


  @Input() Retry: any;

  @Output() ListEmitter = new EventEmitter<any>();

  @Output() onLoad = new EventEmitter<any>();

  @Output() searchClean = new EventEmitter<any>();

  @Output() filterEmit = new EventEmitter<any>();


  public allowEdit: boolean = false;

  onLoading: boolean = false;

  public check: boolean = true;


  public onloading = this.listservice.loadingC_List.subscribe((data: any) => { this.onLoading = data });


  public format: string = 'MMM dd yyyy';

  includeToday: boolean = false;


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
  
  defaultSelection: any = "this week";

  constructor(private timezone: TimeZoneService,
    private listservice: CampaignsService,
    private interval: SetIntervalService,
    private router: Router,
    private localStorage : LocalStorageService) {

  }




  ngOnChanges() {
    if (this.Retry == true) {

      // this.listData(this.selectedDates)
    } else {
      // this.Retry =false;
      // do nothing
    }
  }
  month;
  fullYear;
  start;
  end;
  range: number = 25
  currentUrl: any;
  previousUrl: string;

  selectedDates: any;

  selectedLabel: any;

  fromDate: any;

  toDate: any;

  label: any;

  startDate: any;
  endDate: any;

  userzone:any;

  public onCreate(event) {

  }
  onOpen(args) {

  }

  ngOnInit(): void {
    this.router.events.subscribe((event: any) => {

      if (event instanceof NavigationEnd) {
        if (this.router.url.includes('/campaigns/cdetail')) {
          this.interval.setPrevious(this.selectedDates, false);
        }
      }
    });
    let user: any = this.localStorage.getLocal("user")
    const items = JSON.parse(user)
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
    this.userzone = zone;
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
    if (this.interval.flag == true) {
      if (this.interval.previousData == undefined) {        
        this.selectedLabel = this.defaultSelection;
      }else{
        this.selectedLabel = this.interval.previousData.dateselectiontype;

      }
    //  this.selectedLabel = this.interval.previousData.dateselectiontype;
      if (this.selectedLabel == "this week") {
        this.startDate = this.weekStart;
        this.endDate = this.today;
      } else if (this.selectedLabel == "this month") {
        this.startDate = this.monthStart;
        this.endDate = this.today;
      } else if (this.selectedLabel == "last 7 days") {
        this.startDate = this.sevendaysStart;
        if (this.includeToday == true) {
          this.endDate = this.today;
        } else {
          this.endDate = this.yesterday;
        }
       
      } else if (this.selectedLabel == "last 15 days") {
        this.startDate = this.fifteenDays;
        if (this.includeToday == true) {
          this.endDate = this.today;
        } else {
          this.endDate = this.yesterday;
        }
      } else if (this.selectedLabel == "last 30 days") {
        this.startDate = this.thirtyDays;
        if (this.includeToday == true) {
          this.endDate = this.today;
        } else {
          this.endDate = this.yesterday;
        }
      } else if (this.selectedLabel == "today") {
        this.startDate = this.today;
        this.endDate = this.today;
      } else if (this.selectedLabel == "custom range") {

          this.fromDate =  this.interval.previousData.fdate;
          this.toDate =this.interval.previousData.tdate;
      
        this.startDate = new Date(moment(this.interval.previousData.fdate).format('L')) 
        this.endDate = new Date(moment(this.interval.previousData.tdate).format('L')) 
      
      }

    } else {
      this.selectedLabel = "this week"
      this.startDate = this.weekStart;
      this.endDate = this.today;
    }

    this.submitDate();
    //  this.timeThread();
  }

  listData(type) {
    this.onLoad.emit(true)
    this.listservice.campaignList(type)
      .subscribe(
        (res: any) => {
          res.forEach(element => {
            element.c_type = element.c_type.toLowerCase();
          });
          this.campaignLists = res
          this.onLoad.emit(false)
          this.ListEmitter.emit(this.campaignLists)
        },
        (error: HttpErrorResponse) => {
          this.onLoad.emit(false)
          let err = this.listservice.badError
          this.ListEmitter.emit(err)

        }

      )

  }


  dateFilter(event) {    
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
    // CU-146
    if (event.event.type !== "click") {
      this.selectedLabel = "custom range";
    } else if (selectionType == "APPLY") {
      firstUpper = 'custom range';
      this.selectedLabel = "custom range";
    } else if (!selectionType.toLowerCase().includes('today') && !selectionType.toLowerCase().includes('last') && !selectionType.toLowerCase().includes('this')) {
      this.selectedLabel = "custom range";
    }

  }

  submitDate() {
    let obj;
    if (this.selectedLabel == "this week" || this.selectedLabel == "this month" ) {
      obj = {
        searchText: "clean",
        label : this.selectedLabel,
        includeToday: true
      }
    } else if (this.selectedLabel == "custom range" || this.selectedLabel == "today") {
      obj = {
        searchText: "clean",
        label : this.selectedLabel,
        includeToday: null
      }
    } else {
      obj = {
        searchText: "clean",
        label : this.selectedLabel,
        includeToday: false
      }
    }

    this.searchClean.emit(obj)

    this.label = this.selectedLabel
    this.selectedDates = {
      dateselectiontype: this.label,
      fdate: this.fromDate,
      tdate: this.toDate
    }
    this.filterEmit.emit(this.selectedDates)
  }
  @Input() openDropDown: boolean = false;

  openDropdown() {
    this.openDropDown = !this.openDropDown;
    // this.openDropDown = true;
  }

  closeDropDown() {
    this.openDropDown = !this.openDropDown;
  }

  toggleDropDown() {
    this.openDropDown = !this.openDropDown;
  }

  selectedValue: any = '7 Days';

  customRange: boolean = false;

  selected(opt) {
    this.selectedValue = opt;
    if (opt == 'custom range') {
      this.customRange = !this.customRange;
    }

    this.toggleDropDown();

  }


  setInterval: any;
  timeThread() {
    this.setInterval = setInterval(() => {
      this.timeIntervalApi();
    }, 30 * 1000)
  }

  timeIntervalApi() {

    this.interval.campaignList(this.selectedDates)
      .subscribe(
        (res: any) => {
          this.campaignLists = res
          // this.onLoad.emit(false)
          this.ListEmitter.emit(this.campaignLists)
        },
        (error: HttpErrorResponse) => {

          let err = this.interval.badError
          this.ListEmitter.emit(err)

        }

      )

  }

  ngOnDestroy() {
    this.interval.fromBack("false")
    clearInterval(this.setInterval);

  }
}
