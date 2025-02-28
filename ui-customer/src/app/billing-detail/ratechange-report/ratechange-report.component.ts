import { Component, OnInit } from '@angular/core';
import {RateChangeReportService  } from "src/app/billing-detail/Billing_helper/ratechange_report.service";
import { getCurrencySymbol } from '@angular/common';
import { CommonService } from "src/app/shared/commonservice";
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { value } from 'src/app/shared/campaigns.constants';
import { LocalStorageService } from 'src/app/authentication/local-storage.service';

@Component({
  selector: 'app-ratechange-report',
  templateUrl: './ratechange-report.component.html',
  styleUrls: ['./ratechange-report.component.css']
})
export class RatechangeReportComponent implements OnInit {
  noData: boolean = false;
  apiError: any =false;
  noSearchRecords: boolean = false;
  userZone: any;
  modify_header: string;
  dynamicUserVal: string[];
  cliId: any;
  userApiError: boolean=false;
  myOptions = value;
  dateSelectionLabel :any;



  constructor(private rcService: RateChangeReportService,
    private route:ActivatedRoute,
    private router: Router,
    private commService:CommonService,
    private localStorageService:LocalStorageService
    ) { }

  total: number ;

  user_loader:boolean = false;

  public senderidLoading = this.rcService.userFilterLoading.subscribe((data:any)=>{this.user_loader = data});

  userToBeSelected:string ='';

  onLoading :boolean = false;

  totalRecord: number;
  
  p: number = 1;
  
  pagesize: number;
  
  itemsPerPage: number = 10;
  
  perpageCount: number = 10;

  UserTempArray=[];

  NOUserID:boolean = false;

  rateChangeList=[];

  userId : string;
  
  public searchText: any = "";

  RCLIST : any;

  paginateValue: any;

  defaultProp = "sortprop";
  defaultOrder = "asc";

userNameIcon = 0;
oldRateIcon = 0;
newRateIcon = 0;
countryIcon = 0;
lastModifyIcon = 2;

dateselection:any;

nameOrder = "";
searchprop: any = "";

user:any;
loginUserType:any;

  ngOnInit(): void {
    const usertype = JSON.parse(this.localStorageService.getLocal('user'));
    this.loginUserType = usertype.user_type;
   // this.onLoading = true;
    this.userSubscribtion();
    this.userZone = this.commService.getUserData();
    this.modify_header = "MODIFIED DATE (" + this.userZone + ")";

    // console.log('inside ngoinit');
    
    
    // this.route.queryParams.subscribe((data: any) => {
    //   console.log('inside');
      
    //   if (data.user != undefined) {
    //     if (data.user =='zxcvbc12') {
    //       this.user = data.user;
    //     } else {
    //       this.user = undefined;
    //     }
      
    //   }else{
    //     this.user = undefined;
    //     this.userSubscribtion();
    //   }
    // })
   this.cliId =  this.rcService.cliID;
   console.log(this.cliId);
   
  }

apiLoader:boolean = false;
  listSubscribtion(obj:any){
    this.onLoading = true;
    this.apiLoader = true;
    this.apiError = false;
   
    this.rcService.getbillRateChangeList(obj).subscribe((data:any)=>{
      this.apiLoader = false;
      this.onLoading = false;
      this.RCLIST = data;
      this.RCLIST.forEach((data:any,i:any)=>{
        if (data.user_type == '0') {
          data.usertype = 'super admin'
        } else if (data.user_type == '1') {
          data.usertype = 'admin'
        } else if (data.user_type == '2') {
          data.usertype = 'user'
        }
        data.sortprop = i;
      })
      this.noData = false;
      if (this.RCLIST.length == 0) {
        this.noData = true;
      }
      this.getcount();
    },
    ((err:HttpErrorResponse)=>{
      this.apiLoader = false;
      this.onLoading = false;
     this.apiError = true;
    }))
  }

  useridRetry(){
    this.userSubscribtion();
  }

  receivedDateSelection(event){
    // console.log(event);
   
    this.dateselection = event;
  }

  retryApi(){
    this.submitCall();
  }

  userSubscribtion(){
    this.userApiError = false;
    this.rcService.getUserList()
    .subscribe(
      (res: any) => {
          if (res) {
            this.userApiError = false;
            this.rateChangeList = res;
            this.UserTempArray = this.rateChangeList;
            this.dynamicUserVal = Object.keys(res[0]);
             
          console.log( this.cliId,this.user);
          
            if ( this.cliId != undefined ) {
               console.log('inside if');
              
            const selectedUser = res.find((data:any)=>{
                return data.cli_id_str == this.cliId;
              })
              // var keys = Object.keys(selectedUser);
              // var len = keys.length;
              if (selectedUser != undefined) {
                this.userToBeSelected = selectedUser.username;
              this.userId = selectedUser.cli_id_str;
              }else{
                this.userToBeSelected = res[0].username;
                this.userId = res[0].cli_id_str;
              }
            
              
              
            }else{
              // console.log('inside else');
              
              this.userToBeSelected = res[0].username;
              this.userId = res[0].cli_id_str;
            }
           
             if ( this.rateChangeList.length == 0) {
             // this.disableButton.next(true);
              this.NOUserID = true;
                }
                this.submitCall();
                   //   this.SourcePreviousData = obj;
                  //    this.campaignGetApiCall(obj);
            }
      },
      (error: HttpErrorResponse) => {
       

          this.userApiError = true;
        })
  }

  submitCall(){
    let obj ={
      dateselectiontype: this.dateselection.dateselectiontype,
      fdate: this.dateselection.fdate,
      tdate: this.dateselection.tdate,
      cli_id_str: this.userId,
    }
    this.dateSelectionLabel =this.dateselection.dateselectiontype;
    if (!this.user_loader) {
      this.rcService.setCliIDforRoute(undefined);
      this.listSubscribtion(obj);
    }
  }

  getcount(){ 
   // this.noSearchRecords = false;
    if (this.searchText.length == 0) {
       this.noSearchRecords = false;
         this.total = this.RCLIST.length;
         this.totalRecord = this.RCLIST.length
        this.p = 1;
        this.itemsPerPage = this.perpageCount;
    } 
    else {     
    //  console.log(this.searchText,'inside else');
     
     
      // console.log( this.noSearchRecords);
              if (this.rcService.totalBillingList?.length >= 0) {
               var count = this.rcService.totalBillingList.length;
              //  console.log(count);
               
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

    getC_Symbol(eve:any){
      //console.log(this.getCurrencySymbol(eve));
      
      return getCurrencySymbol(eve,'narrow');
    }


    sort(event) {
            this.searchprop = event.prop;
            this.nameOrder = event.order;
          }
        
          iconChange(event) {
            if (event.prop == "username") {
              this.userNameIcon = event.icon;
              this.oldRateIcon = 0;
              this.newRateIcon = 0;
              this.countryIcon = 0;
              this.lastModifyIcon = 0;
            }
            else if (event.prop == "old_sms_rate") {
              this.userNameIcon = 0;
              this.oldRateIcon = event.icon;
              this.newRateIcon = 0;
              this.countryIcon = 0;
              this.lastModifyIcon = 0;
        
        
            }
            else if (event.prop == "new_sms_rate") {
              this.userNameIcon = 0;
              this.oldRateIcon = 0;
              this.newRateIcon =  event.icon;
              this.countryIcon = 0;
              this.lastModifyIcon = 0;
            }
            else if (event.prop == "country") {
              this.userNameIcon = 0;
              this.oldRateIcon =0;
              this.newRateIcon =  0;
              this.countryIcon = event.icon;
              this.lastModifyIcon = 0;
        
            }
            else if (event.prop == "modified_ts") {
              this.userNameIcon = 0;
              this.oldRateIcon =0;
              this.newRateIcon =  0;
              this.countryIcon = 0;
              this.lastModifyIcon = event.icon;
            }
           
        
          }
          


          customUserSearch(){
  
            if (this.dynamicUserVal != undefined) {
              const selectedValue =this.userToBeSelected;
              // console.log(this.dynamicUserVal[1]);
              
              const dynamicval =  this.dynamicUserVal[1];
             this.UserTempArray = this.commService.customSerach(this.rateChangeList,dynamicval,selectedValue)
          
            }
            
          }

          userKeyup(){
            this.UserTempArray = this.rateChangeList;
          }
          userBlur(){
            this.UserTempArray = this.rateChangeList;
          }
          selectedUser(event){
        
            // console.log(event);
           // if (this.userToBeSelected != event.username) {
              this.userToBeSelected = event.username;
              this.userId = event.cli_id_str;
            //}
          
           // this.makeParams();
          }


          submitToApi(){

          }
}
