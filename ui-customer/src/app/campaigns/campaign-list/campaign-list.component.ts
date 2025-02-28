import { HttpErrorResponse } from '@angular/common/http';
import { THIS_EXPR } from '@angular/compiler/src/output/output_ast';
import { Component, OnInit,Output ,Renderer2,ElementRef, ViewChild, OnDestroy,} from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { CampaignsService } from "src/app/campaigns/campaigns.service";
import { SearchService } from "../c_Helper/searchservice";
import { CONSTANTS } from "src/app/shared/campaigns.constants";
import { SetIntervalService } from "src/app/campaigns/c_Helper/setintervalapicalls";

@Component({
  selector: 'app-campaign-list',
  templateUrl: './campaign-list.component.html',
  styleUrls: ['./campaign-list.component.css']

})
export class CampaignListComponent implements OnInit ,OnDestroy{

  accountUserList : any [] =[];

  accountStat_List : any ;

   onLoading :boolean = false;

   NDHeadContent = CONSTANTS.CNDHeadContent;

   NDMessageCOntent = CONSTANTS.CNDMessageCOntent;
 

 public onloading = this.campservice.loadingC_List.subscribe((data:any)=>{
  this.onLoading = data;
    });

total: number ;

totalRecord: number;

p: number = 1;

pagesize: number;

itemsPerPage: number = 10;

perpageCount: number = 10;

public searchText: any = "";

statsLoading:boolean = false;

paginateValue: any;

public statsloading = this.campservice.loadingSC_List.subscribe((data:any)=>{this.statsLoading = data});;


  noRecords: number;

  C_list = CONSTANTS.CAMPAINGNS;

  @ViewChild('toggleButton') toggleButton: ElementRef;
  @ViewChild('menu') menu: ElementRef;

  constructor(private campservice:CampaignsService,
    private s_service:SearchService,private router:Router,
    private renderer: Renderer2,
    private interval:SetIntervalService ) {
    // this.campservice.campaignList().subscribe((data:any)=>{
    //   this.campaignLists = data.campaignList

    // })

  }

  campaignLists:any[]=[];

  c_Today_Stats:any;


  ngOnInit(): void {
   
    this.router.events.subscribe((event: any) => {
      
      if (event instanceof NavigationEnd) {

          if (this.router.url.includes('/campaigns/cdetail')) {
            this.interval.setcampSearch(this.searchText);
          }  
      }
  });
    this.paginateValue = this.p;
   this.subscribeData();
   this.timeThread();

   this.campservice.closeCampaign.subscribe((data)=>{this.openDropDown=false})

   if (this.interval.flag == true) {     
     this.searchText = this.interval.csearch;
   } else {
    this.searchText="";
   }
  }

  subscribeData(){
    this.campservice.campaignListStats()
    .subscribe(
      (res: any) => {
         // if (res.statusCode === 200) {
          this.c_Today_Stats= res;
          // this.timeThread();
         // }
      },
      (error: HttpErrorResponse) => {

       let err =this.campservice.badError

      }

    )

    // return this.campaignLists;
  }
  selectedDates:any;
  listData(type){
    //this.onLoad.emit(true)

    this.selectedDates = type;
    this.campservice.campaignList(type)
    .subscribe(
      (res: any) => {
        res.forEach(element => {
         element.c_type = element.c_type.toLowerCase();
        });
        this.campaignLists = res

      this.apiError = false;
      this.retry = false;
      this.noData = false;
       // this.onLoading = false;
      if (this.campaignLists.length == 0) {
        this.noData = true;


      }
      this.getcount();
      },
      (error: HttpErrorResponse) => {
      //  this.onLoad.emit(false)
       let err =this.campservice.badError

       this.apiError = true;
       this.retry = false;

      }

    )

  }

  subscribe(event){
    if (event==true) {
      this.apiError = false;
      this.noRecords = 1;
      this.noData = false;
    }

  }
  close($event){
    this.openDropDown = !this.openDropDown;
    // this.selectEvent()
  }
retry:boolean= false;

selectEvent(value:any){

this.router.navigate(['campaigns/'+value])
}
  Retry(){
  this.retry = true;
  this.apiError = false;
  this.listData( this.selectedDates);
  }

noData:boolean = false;
apiError:boolean = false;


  receivedList(event){

    if (event.statusCode) {
      this.campaignLists = [];
      this.apiError = true;
      this.retry = false;

      // this.onLoading = false;
    } else {
      this.campaignLists = event;
      this.apiError = false;
      this.retry = false;
      this.noData = false;
       // this.onLoading = false;
      if (this.campaignLists.length == 0) {
        this.noData = true;

      }
      this.getcount();
    }



  }

setInterval:any;

timeThread(){
  this.setInterval = setInterval(()=>{
this.timeIntervalApi();
this.timeIntervalLIstApi();
  },CONSTANTS.apiHitTimer)
}

timeIntervalApi(){
// let pageVal = this.paginateValue;
  this.interval.campaignListStats()
    .subscribe(
      (res: any) => {
          this.c_Today_Stats = res   
          this.p = this.paginateValue;
           // this.onLoad.emit(false)   
            
      },
      (error: HttpErrorResponse) => {

       let err =this.interval.badError


      }

    )

}
timeIntervalLIstApi(){

  this.interval.campaignList(this.selectedDates)
    .subscribe(
      (res: any) => {
          this.campaignLists = res;
          this.getcount();
         this.p = this.paginateValue;
          // this.next('null',this.paginateValue)
           // this.onLoad.emit(false)   
           // this.ListEmitter.emit(this.campaignLists)
      },
      (error: HttpErrorResponse) => {

       let err =this.interval.badError;
     //  this.ListEmitter.emit(err)

      }

    )

}


ngOnDestroy() {
  this.interval.flag = false;
  if (this.setInterval) {
    clearInterval(this.setInterval);
  }
}

includesToday:string;

hideToday:boolean = false;

selectedLabel :string;

searchClean(event){

  if (event.searchText =="clean") {
    if (this.interval.flag == true) {
     this.searchText = this.interval.csearch;
     this.interval.flag=false;
   } else {
    this.searchText="";
   }
  }
  if (event.includeToday == true) {
    this.includesToday = "";
    this.hideToday =false;
    this.selectedLabel = event.label;
  }else if(event.includeToday == false){
    this.includesToday = "does not";
    this.hideToday = false;
    this.selectedLabel = event.label;
  }else{
    this.hideToday = true;
  }

}
  onClick(campaign:any){
    this.campservice.campaignDetailPageContent=campaign
    this.router.navigate(["/campaigns/cdetail"],{ queryParams: { campaignId:campaign}})

  }


  toggleProfileSlider() {
    this.campservice.openQA.next(false)
    this.openDropDown = false;
}

// cams=[1,2,3,4,5,6,7]
openDropDown:boolean = false;

campaignTab(){

  this.campservice.openQA.next(false)
  this.openDropDown = !this.openDropDown;
}

onItemChange($event){

}

dropdown:boolean;

dropdownclose(event){
  this.openDropDown = event;
}

paginationValue(tableList:any){
  if (tableList == 0) {
    this.total = 0;
    this.p = 1;
    this.perpageCount = 0;
  }else {
  this.total = tableList;
  this.p = 1;
  this.itemsPerPage = this.perpageCount;
}
}
getcount(){ 
  if (this.searchText.length == 0) {
      this.noRecords=1;
       this.total = this.campaignLists.length;
       this.totalRecord = this.campaignLists.length
      this.p = 1;
      this.itemsPerPage = this.perpageCount;
  } else {     
            if (this.s_service.data?.length >= 0) {
             var count = this.s_service.totalRecords.length
                if (count == 0) {           
                  this.total = 0;
                  this.p = 1;
                  this.perpageCount = 10;
                   this.noRecords=0;
                }else{
                  this.noRecords=1;
                  this.total = count;
                  this.p = 1;
                  this.perpageCount = 10;
                  // this.SearchArray = this.search.totalRecords ;
                }
            }
  }
}

ReceivedpaginateValue(event){
  this.paginateValue = event;
  this.p = event;
  }
  
onCampaignClick(){

}

}
