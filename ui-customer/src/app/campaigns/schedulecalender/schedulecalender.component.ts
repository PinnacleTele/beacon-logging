import { Component, OnInit,Input, Output, EventEmitter, OnChanges, ViewChild, OnDestroy } from '@angular/core';
import * as moment from 'moment-timezone';
import { TimeZoneService } from 'src/app/shared/service/timezone.service';
import { CampaignsService } from "src/app/campaigns/campaigns.service";
import { HttpErrorResponse } from '@angular/common/http';
import {
  EnterExitRight,
  Container1,
  EnterExitTop
} from "../../shared/animation";
import { LocalStorageService } from 'src/app/authentication/local-storage.service';
import { NavigationEnd, Router } from '@angular/router';

import {SetIntervalService  } from "src/app/campaigns/c_Helper/setintervalapicalls";


@Component({
  selector: 'app-schedulecalender',
  templateUrl: './schedulecalender.component.html',
  styleUrls: ['./schedulecalender.component.css']
})
export class SchedulecalenderComponent implements OnInit,OnDestroy {

  campaignLists:any []=[];

  timeZone:any[]=[];

  @Input() List:any;

  @Input() campaignCount:any;


  @Input() Retry:any;

  @Output() ListEmitter = new EventEmitter<any>();

  @Output() onLoad = new EventEmitter<any>();

  @Output() filterEmit = new EventEmitter<any>();


  @Output() searchClean = new EventEmitter<any>();

  @Output() countLable = new EventEmitter<any>();


  onLoading:boolean= false;

  public onloading = this.listservice.loadingC_List.subscribe((data:any)=>{this.onLoading = data});


  public format: string = 'MMM dd yyyy';

  includeToday:boolean = false;


   public today: Date ;
   public seventhday: Date ;
   defaultvalue:any;

  public weekStart: Date;
  public weekEnd: Date ;
  public monthStart: Date;
  public monthEnd: Date;
  public lastStart: Date;
  public lastEnd: Date ;
  public yearStart: Date ;
  public yearEnd: Date;
  public sevendaysStart:Date;
  public currentMonth:Date;
  public fifteenDays:Date;
  public thirtyDays:Date;
  public sixtyDays:Date;
  public fifthDays:Date;
  selectedLabel: string;
  label:any;

  fromDate:any;

toDate:any;

month;
fullYear;
start;
end;
range:number=25;
startDate:any;
endDate:any;

selectedValue:any = '7 Days'  ;

defaultSelection = "this week";

customRange:boolean = false;

  constructor(private timezone: TimeZoneService,
    private interval : SetIntervalService,
    private router: Router,
    private listservice:CampaignsService,
    private localStorage : LocalStorageService) { }

 
  ngOnChanges() {
    if (this.Retry == true) {

     // this.listData(this.selectedDates)
      this.filterEmit.emit(this.selectedDates)
    }else{
      // this.Retry =false;
      // do nothing
    }
  }


public onCreate(event) {

}
onOpen(args) {
  


}

  ngOnInit(): void {


    this.router.events.subscribe((event: any) => {
      
      if (event instanceof NavigationEnd) {

          if (this.router.url.includes('campaigns/scdetail')) {
            this.interval.setscPrevious(this.selectedDates, false);
            //console.log(this.router.url,'inside detail if');
          }  
      }
  });
    let user:any =this.localStorage.getLocal("user")
    const items =JSON.parse(user)
    let zone = items.tz;

// this.timezone.getTimeZone().subscribe((dat:any)=>{
//   dat.forEach((data: any) => {
//           this.timeZone.push({
//             key: data.offset,
//             value: data.display_name,
//             zone: data.zone_name
//           });
// })

//        this.timeZone.filter((el:any) =>{
//       if ( el.zone ===  items.time_zone) {
//        zone = el;

//       }
//      })
//     })

    this.sevendaysStart= new Date(moment().tz(zone).subtract(7,'d').format('L'))

    this.today = new Date(moment().tz(zone).format('L'))
    this.seventhday = new Date(moment().tz(zone).add(6,'d').format('L'))
    this.fifteenDays = new Date(moment().tz(zone).add(14,'d').format('L'))
     this.weekStart = new Date(moment().tz(zone).startOf('weeks').format('L'))
     this.weekEnd = new Date(moment().tz(zone).add(0,'weeks').endOf('weeks').format('L'))
    this.monthEnd = new Date(moment().tz(zone).add(0,'months').endOf('months').format('L'))
    this.monthStart= new Date( moment().tz(zone).subtract(0, 'months').startOf('months').format('L'))
    this.currentMonth = new Date(moment().tz(zone).subtract(0,'months').startOf('month').format('L'))
    this.thirtyDays = new Date(moment().tz(zone).add(29,'d').format('L'))
    this.fifthDays = new Date(moment().tz(zone).subtract(15,'days').format('L'))
    // this.fifteenDays  = new Date( moment().tz(zone)
    // .subtract(15, 'days')
    // .format('L'))
    this.sixtyDays = new Date(moment().tz(zone).subtract(60,'d').format('L')) ;
  
      
    if (this.interval.scflag == true) {
      if (this.interval.scpreviousData == undefined) {
              
        this.selectedLabel = this.defaultSelection;
      }else{
        this.selectedLabel = this.interval.scpreviousData.dateselectiontype;

      }
     
     
      if ( this.selectedLabel == "this week") {
        this.startDate = this.today;
        this.endDate = this.weekEnd;
      } else if(this.selectedLabel == "this month") {
        this.startDate = this.today;
        this.endDate = this.monthEnd;
      } else if(this.selectedLabel == "next 7 days"){
        this.startDate = this.today;
        this.endDate = this.seventhday;
      } else  if ( this.selectedLabel == "next 15 days") {
        this.startDate = this.today;
        this.endDate = this.fifteenDays;
      } else if(this.selectedLabel == "next 30 days") {
        this.startDate = this.today;
        this.endDate = this.thirtyDays;
      } else if(this.selectedLabel == "today"){
        this.startDate = this.today;
        this.endDate = this.today;
      } else if(this.selectedLabel == "custom range"){
        
        this.fromDate =  this.interval.scpreviousData.fdate;
        this.toDate =this.interval.scpreviousData.tdate;

        this.startDate = new Date(moment(this.interval.scpreviousData.fdate).format('L')) 
        this.endDate = new Date(moment(this.interval.scpreviousData.tdate).format('L')) 
      }
    
    } else {
      this.selectedLabel = "this week"
      this.startDate = this.today;
      this.endDate = this.weekEnd;
    }
   // this.selectedLabel = "this week";
    
    this.submitDate();
    // this.filterEmit.emit(this.selectedDates);

  }

  

selectedDates:any;
  dateFilter(event){

//  console.log(event);
 if (event.text = "") {

 }

    let date1 = event.value[0]
    let date2 = event.value[1]
    this.fromDate = date1;
    this.toDate = date2;

   this.fromDate= moment(new Date(date1)).format('YYYY-MM-DD')
   this.toDate=  moment(new Date(date2)).format('YYYY-MM-DD')

    this.customRange = !this.customRange;
    this.openDropDown =false;

    let selectionType = event.event.target.innerText;

    this.selectedLabel = event.event.target.innerText;

    let firstUpper = selectionType.substr(0, 1).toLowerCase() + selectionType.substr(1);

    this.selectedLabel = firstUpper;

    // CU-146
    if(event.event.type !== "click"){
      this.selectedLabel = "custom range";
      this.includeToday = true;
    } else if (selectionType == "APPLY") {
      firstUpper = 'custom range'
      this.includeToday = true;
      this.selectedLabel = 'custom range'

    }else if (!selectionType.toLowerCase().includes('today') && !selectionType.toLowerCase().includes('next') && !selectionType.toLowerCase().includes('this')) {
      this.selectedLabel = "custom range";
    }
    this.submitDate();
    // console.log(  this.selectedDates);
  }

  submitDate(){
let obj;
    if(this.selectedLabel == "custom range"){
      obj ={
        searchText : "clean",
        includeToday : true
      }
    }else{
      obj ={
        searchText : "clean",
        includeToday : this.includeToday
      }
    }


  
    this.label = this.selectedLabel
  
    this.countLable.emit(this.label)
    this.selectedDates={
      dateselectiontype : this.label,
      fdate : this.fromDate,
      tdate : this.toDate
    }
  
   this.filterEmit.emit(this.selectedDates)

   this.searchClean.emit(obj)
   // this.listData(this.selectedDates);
  }
@Input() openDropDown:boolean=false;

  openDropdown(){
    this.openDropDown = !this.openDropDown;
    // this.openDropDown = true;
  }

  closeDropDown() {
    this.openDropDown = !this.openDropDown;
    
}

toggleDropDown() {
  this.openDropDown = !this.openDropDown;
  
}



selected(opt){
  this.selectedValue = opt;
  if (opt =='custom range') {
    this.customRange = !this.customRange;
  }

this.toggleDropDown();

}

ngOnDestroy() {
  this.interval.fromschBack("false")
}

}
