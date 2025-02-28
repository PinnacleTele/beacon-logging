import { Component, OnInit } from '@angular/core';
import { BillingSearchService } from "src/app/billing-detail/billing-search.service";
import { BillingDetailService } from "src/app/billing-detail/billing-detail.service";
import { Router } from '@angular/router';
import { getCurrencySymbol } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { RateChangeReportService } from '../Billing_helper/ratechange_report.service';
import { CommonService } from 'src/app/shared/commonservice';
import { value } from 'src/app/shared/campaigns.constants';

@Component({
  selector: 'app-billing-list',
  templateUrl: './billing-list.component.html',
  styleUrls: ['./billing-list.component.css']
})
export class BillingListComponent implements OnInit {

  total: number ;

totalRecord: number;

p: number = 1;

pagesize: number;

itemsPerPage: number = 10;

perpageCount: number = 10;

public searchText: any = "";

statsLoading:boolean = false;

paginateValue: any;

noData :boolean = false;

noSearchRecords:boolean = false;

onLoading : boolean = false;

defaultProp = "sortprop";
defaultOrder = "asc"

userNameIcon = 1;
customRateIcon = 0;
internationalIcon = 0;
rowRateIcon = 0;
lastModifyIcon = 0;
myOptions = value;
nameOrder = "";
searchprop: any = "";

apiError:boolean = false;

  sampleList = [ ];
  userZone: any;
  modify_header: string;

  constructor(private b_searchService : BillingSearchService,
    private b_service : BillingDetailService,private router:Router,
    private commonService: CommonService,
    private rcservice:RateChangeReportService) { }

  ngOnInit(): void {

    this.userZone = this.commonService.getUserData();
    this.modify_header = "MODIFIED DATE (" + this.userZone + ")";
this.getBillingList();

   
  }

  getBillingList(){
    this.apiError = false;
    this.onLoading = true;
    this.b_service.getBillingList().subscribe((data:any)=>{
      
      this.onLoading = false;
      this.sampleList = data;
      this.sampleList.forEach((data:any,i:any)=>{
        if (data.user_type == 0) {
          data.usertype = 'super admin'
        } else if (data.user_type == 1) {
          data.usertype = 'admin'
        } else if (data.user_type == 2) {
          data.usertype = 'user'
        }
  
        if (data.intl_enabled_yn == 0) {
          data.international = 'disabled'
        } else if (data.intl_enabled_yn == 1) {
          data.international = 'enabled'
        } 

        data.sortprop = i;
      })
      // console.log(this.sampleList);
      
      this.noData = false;
      if (this.sampleList.length == 0) {
        this.noData = true;
      }
      this.getcount();
    },
    ((error:HttpErrorResponse)=>{
      console.log('inside api error');
      this.onLoading = false;
      this.apiError = true;
    }))
  }

  getC_Symbol(eve:any){
    //console.log(this.getCurrencySymbol(eve));
    
    return getCurrencySymbol(eve,'narrow');
  }

  Retry(){
    this.getBillingList();
  }

  getcount(){ 
    if (this.searchText.length == 0) {
        this.noSearchRecords = false;
         this.total = this.sampleList.length;
         this.totalRecord = this.sampleList.length
        this.p = 1;
        this.itemsPerPage = this.perpageCount;
    } 
    else {     
   
              if (this.b_searchService.totalBillingList?.length >= 0) {
               
                
               var count = this.b_searchService.totalBillingList.length;
                  if (count == 0) {           
                    this.total = 0;
                    this.p = 1;
                    this.perpageCount = 10;
                    this.noSearchRecords = true;
                  }else{
                    this.noSearchRecords = false;
                    this.total = count;
                    this.p = 1;
                    this.perpageCount = 10;
                    
                  }
              }
    }
  }

  ReceivedpaginateValue(event){
    this.paginateValue = event;
    this.p = event;
    }


    edit(list){
      
      this.router.navigate(['billing/edit'],
      {
        queryParams: { id: list.cli_id, intl_en : list.intl_enabled_yn }
        //, skipLocationChange: true  
      }
    );
    }

    rateView(list){
      // console.log(list);
      this.router.navigate(['billing/brclist']);
      this.rcservice.setCliIDforRoute(list.cli_id+'');
      // this.router.navigate(['billing/brclist'],
      // {
      //  queryParams: { id: list.cli_id}
      // , skipLocationChange: true  
      // }
      // )
    }

    sort(event) {

// console.log(event);

      this.searchprop = event.prop;
      this.nameOrder = event.order;
    }
  
    iconChange(event) {
      if (event.prop == "user") {
        this.userNameIcon = event.icon;
        this.customRateIcon = 0;
        this.internationalIcon = 0;
        this.rowRateIcon = 0;
        this.lastModifyIcon = 0;
      }
      else if (event.prop == "countries_customrate_total") {
        this.userNameIcon = 0;
        this.customRateIcon = event.icon;
        this.internationalIcon = 0;
        this.rowRateIcon = 0;
        this.lastModifyIcon = 0;
  
  
      }
      else if (event.prop == "international") {
        this.userNameIcon = 0;
        this.customRateIcon = 0;
        this.internationalIcon = event.icon;
        this.rowRateIcon = 0;
        this.lastModifyIcon = 0;
  
  
      }
      else if (event.prop == "row_rate") {
        this.userNameIcon = 0;
        this.customRateIcon = 0;
        this.internationalIcon = 0;
        this.rowRateIcon = event.icon;
        this.lastModifyIcon = 0;
  
      }
      else if (event.prop == "modified_ts") {
        this.userNameIcon = 0;
        this.customRateIcon = 0;
        this.internationalIcon = 0;
        this.rowRateIcon = 0;
        this.lastModifyIcon = event.icon;
      }
     
  
    }
    
}
