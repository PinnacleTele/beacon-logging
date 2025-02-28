import { HttpErrorResponse } from '@angular/common/http';
import { Component, Input, OnInit, Output,EventEmitter, OnChanges, SimpleChanges,ViewChild, OnDestroy } from '@angular/core';
import { NgSelectComponent } from "@ng-select/ng-select";
import { BehaviorSubject } from 'rxjs';
import { ReportService } from "src/app/reports/Helpers/summary.service";
import { CONSTANTS, value } from 'src/app/shared/campaigns.constants';
import { CommonService } from "src/app/shared/commonservice";
@Component({
  selector: 'app-filtercomponent',
  templateUrl: './filtercomponent.component.html',
  styleUrls: ['./filtercomponent.component.css']
})
export class FiltercomponentComponent implements OnInit,OnChanges,OnDestroy {

  myOptions = value;
  
  source:boolean=false;
  
  status:boolean=false;
  
  senderid:boolean=false;
  
  campaign:boolean=false;
  
  sourceList:string[];

  campaignList:string[];

  senderidList:string[];

  

  statusList = [
                {display_value: "All"},
                {display_value: "Submitted"},
                {display_value: "Delivered"},
                {display_value: "Rejected"},
                {display_value: "Failed"}
              ];

  @Output() download = new EventEmitter<any>() ;

  @Output() submitData = new EventEmitter<any>() ;

  @Output() searchClean = new EventEmitter<any>() ;


  @Input() defaultSelection:any;

  downloadIconLoader:boolean=false;

  viewIconLoader:boolean=false;



  sourceToBeSelected :any  ;

  campaignToBeSelected :any ;

  senderidToBeSelected :any ;

  statusToBeSelected :any;

   @ViewChild('sourceid') public sourceid: NgSelectComponent;
  
   @ViewChild('ngSelect2') public ngSelect2: NgSelectComponent;

   @ViewChild('ngSelect1') public ngSelect1: NgSelectComponent;

   @ViewChild('ngSelect3') public ngSelect3: NgSelectComponent;

   @Input() showStatus:boolean = false;

  s_loader:boolean;

  c_loader:boolean;

  si_loader:boolean;

  public summaryDownloadIcon = this.summaryService.summaryDownloadApiLoading.subscribe((data:any)=>{this.downloadIconLoader = data});

  public logDownloadIcon = this.summaryService.downloadLOGapiLoading.subscribe((data:any)=>{this.downloadIconLoader = data});
  

  public summaryViewIcon = this.summaryService.summaryLoading.subscribe((data:any)=>{this.viewIconLoader = data});

  public logViewIcon = this.summaryService.detailedLoading.subscribe((data:any)=>{this.viewIconLoader = data});
  

  // public viewIcon = this.summaryService.campaignLoading.subscribe((data:any)=>{this.c_loader = data});




  public sourceLoading = this.summaryService.sourceLoading.subscribe((data:any)=>{this.s_loader = data});

  public campLoading = this.summaryService.campaignLoading.subscribe((data:any)=>{this.c_loader = data});

  public senderidLoading = this.summaryService.senderidLoading.subscribe((data:any)=>{this.si_loader = data});

  sourceNullError:boolean = false;

  campaignNullError:boolean = false;

  senderidNullError:boolean = false;

  statusNullError:boolean = false;

  SourceID: any;
  CampaignID:any;
  SenderID:any;
  CampaignName:any;


  NOSourceID: any;
  NOCampaignID:any;
  NOSenderID:any;

  SourceApiError: any;
  CampaignApiError:any;
  SenderApiError:any;

  SourceTempArray: any[]=[];
  CampaignTempArray:any[]=[];
  SenderTempArray:any[]=[];

  dynamicSourceVal: any;
  dynamicCampaignVal:any;
  dynamicSenderVal:any;


  SourcePreviousData: any;
  CampaignPreviousData: any;

  SubmitValue:any;
  // CampaignID:any;
  // SenderID:any;

  disableButton= new BehaviorSubject<boolean>(true)


  constructor(private summaryService:ReportService,private sharedservice:CommonService) { }
  

  ngOnChanges(changes: SimpleChanges): void {

    if (this.defaultSelection) {

      //this.sourceToBeSelected = "";
     
      this.soruceApiCall(this.defaultSelection) ;
      
    }
  }

disableBUtton:boolean = false;
  ngOnInit(): void {
  }

  getWidth(){

  }


  selectedSource(event){
    if (event == undefined) {
  //  this.sourceNullError = true;
  //  this.campaignNullError = true;
  //  this.disableBUtton = true;
  //  this.ngSelect2.handleClearClick();
  
   }
   else{
    this.sourceToBeSelected = event.display_name;
    this.SourceID = event.intf_grp_type;

    let obj ={
      dateselectiontype: this.defaultSelection.dateselectiontype,
      fdate: this.defaultSelection.fdate,
      tdate: this.defaultSelection.tdate,
      source: this.SourceID,
    }
    
    //this.campaignToBeSelected = "";
    this.campaignGetApiCall(obj);
   }
  

  }
 
  senderidpaylod(event){
     if (event == undefined) {
      // this.disableBUtton = false;
      // this.disableBUtton = true;
      // this.senderidNullError = true;
     }else{
      // this.disableBUtton = true;
     // this.senderidNullError = false;
      this.SenderID = event.id;
      // this.checkNullError();

      let obj ={
        dateselectiontype: this.defaultSelection.dateselectiontype,
        fdate: this.defaultSelection.fdate,
        tdate: this.defaultSelection.tdate,
        source: this.SourceID,
        campaign_id: this.CampaignID,
        campaignName :this.CampaignName,
        senderid: this.SenderID,
        status: this.statusToBeSelected
      }

      this.SubmitValue = obj;
     }
  }

  statuspaylod(event){
     if (event == undefined) {
    //   this.disableBUtton = false;
    //   this.disableBUtton = true;
    //   this.statusNullError = true;
      }else{
      // this.disableBUtton = true;
      // this.statusNullError = false;
      
      this.statusToBeSelected = event.display_value;
     
      let obj ={
        dateselectiontype: this.defaultSelection.dateselectiontype,
        fdate: this.defaultSelection.fdate,
        tdate: this.defaultSelection.tdate,
        source: this.SourceID,
        campaign_id: this.CampaignID,
        senderid: this.SenderID,
        status: this.statusToBeSelected,
        campaignName :this.CampaignName,
      }

      this.SubmitValue = obj;
     }
  
  }

  sendcampaignPayload(event){

     if (event == undefined) {
    
     }else{
      this.CampaignID = event.campaign_id;
      this.CampaignName = event.campaign_name;

    let obj ={
      dateselectiontype: this.defaultSelection.dateselectiontype,
      fdate: this.defaultSelection.fdate,
      tdate: this.defaultSelection.tdate,
      source: this.SourceID,
      campaign_id: this.CampaignID,
     
    }
    //this.senderidToBeSelected ="";
    
    this.senderIdGetApiCall(obj);
     }
  }


  soruceApiCall(value:any){
    this.disableButton.next(true);
    if ( this.sourceToBeSelected != undefined) {
      this.sourceid.handleClearClick();
    }
    //
    this.summaryService.getReportSource(value)
    .subscribe(
      (res: any) => {
          if (res) {
            this.disableButton.next(false);

            this.NOSourceID = false;
            this.SourceApiError = false;
            this.sourceList = res;
            this.SourceTempArray = this.sourceList;
            this.dynamicSourceVal = Object.keys(res[0]);
            //this.sourceToBeSelected = res[0].display_name;
            this.SourceID = res[0].intf_grp_type;
             if ( this.sourceList.length == 0) {
              this.disableButton.next(true);
              this.NOSourceID = true;
                }
                this.sourceNullError = false;
                let obj ={
                        dateselectiontype: this.defaultSelection.dateselectiontype,
                        fdate: this.defaultSelection.fdate,
                        tdate: this.defaultSelection.tdate,
                        source: this.SourceID,
                      }
                      this.SourcePreviousData = obj;
                      this.campaignGetApiCall(obj);
            }
      },
      (error: HttpErrorResponse) => {
        this.disableButton.next(true);

          this.SourceApiError = true;
        })
  }
  SourceRetry(){
    this.soruceApiCall(this.defaultSelection);
    
  }

  campaignGetApiCall(obj){
    this.disableButton.next(true);

    this.ngSelect2.handleClearClick();
    this.summaryService.getReportCampaign(obj)
    .subscribe(
      (res: any) => {
          if (res) {
            this.disableButton.next(false);

            this.NOCampaignID = false;
            this.CampaignApiError = false;
            this.campaignList = res;
            this.CampaignTempArray = this.campaignList;
            this.dynamicCampaignVal = Object.keys(res[0]);
           // this.campaignToBeSelected = res[0].campaign_name;
            this.CampaignID = res[0].campaign_id;
            this.CampaignName = res[0].campaign_name;
             if (res.length == 0) {
              this.NOCampaignID = true;
              this.disableButton.next(true);

                }
                this.campaignNullError = false;
                let obj ={
                        dateselectiontype: this.defaultSelection.dateselectiontype,
                        fdate: this.defaultSelection.fdate,
                        tdate: this.defaultSelection.tdate,
                        source: this.SourceID,
                        campaign_id: this.CampaignID
                      }
                      this.CampaignPreviousData = obj;
                      this.senderIdGetApiCall(obj);
            }
      },
      (error: HttpErrorResponse) => {
        this.disableButton.next(true);
          this.CampaignApiError = true;
        })
  }

  CampaignRetry(){
    this.campaignGetApiCall(this.SourcePreviousData);
  }

 senderIdGetApiCall(obj){
  this.disableButton.next(true);

  this.ngSelect1.handleClearClick();
  
  if (this.showStatus == true) {
    if ( this.statusToBeSelected != undefined) {
      this.ngSelect3.handleClearClick();
    }
  }
 
 
  this.summaryService.getReportSenderid(obj)
  .subscribe(
    (res: any) => {
        if (res) {


          this.NOSenderID = false;
          this.SenderApiError = false;
          this.senderidList = res;
          this.SenderTempArray = this.senderidList;
          this.dynamicSenderVal = Object.keys(res[0]);
         // this.senderidToBeSelected = res[0].senderid;
          this.ngSelect1.blur();
         
          this.statusToBeSelected = "All"
          this.disableButton.next(false);
          this.SenderID = res[0].id;
        
           if (res.length == 0) {
            this.disableButton.next(true);

            this.NOSenderID = true;
              }
              this.senderidNullError = false;
              let obj ={
                dateselectiontype: this.defaultSelection.dateselectiontype,
                fdate: this.defaultSelection.fdate,
                tdate: this.defaultSelection.tdate,
                source: this.SourceID,
                campaign_id: this.CampaignID,
                campaignName :this.CampaignName,
                senderid: this.SenderID,
                status: this.statusToBeSelected
              }
              if (this.showStatus == true) {
                this.ngSelect3.blur();
              }
             
              this.SubmitValue = obj;
              if (this.showStatus == true) {
               // this.submitData.emit(this.SubmitValue);
              }else{
                // do nothing
              }
          }
    },
    (error: HttpErrorResponse) => {
      this.disableButton.next(true);

        this.SenderApiError = true;
      })
 }

 SenderidRetry(){
   this.senderIdGetApiCall(this.CampaignPreviousData);
   
 }

 checkNullError(){
  if (this.SenderApiError || this.CampaignApiError || this.SourceApiError ) {
    this.disableBUtton = true;
   } else {
    this.disableBUtton = false;
   }
}

  disableDownload:boolean = false;

  downloadData(){
    if (this.sourceNullError || this.campaignNullError || this.senderidNullError) {
      // do nothing
    } else {
     
      this.download.emit(this.SubmitValue);
    }
   
  }

  
  submit(){

    if (this.sourceNullError || this.campaignNullError || this.senderidNullError ) {
      // do nothing
    } else {
      this.searchClean.emit("clean")
      this.submitData.emit(this.SubmitValue);
    }
  }

  customCampSearch(){
  
    if (this.dynamicCampaignVal != undefined) {
      const selectedValue =this.campaignToBeSelected;
      const dynamicval =  this.dynamicCampaignVal[0];
     this.CampaignTempArray = this.sharedservice.customSerach(this.campaignList,dynamicval,selectedValue)
  
    }
    
  }
  campaignKeyup(){
    this.CampaignTempArray = this.campaignList;
    
  }

  campaignBlur(){
    this.CampaignTempArray = this.campaignList;

  }

  customSidSearch(){
    if (this.dynamicSenderVal != undefined) {
    const selectedValue =this.senderidToBeSelected;
    const dynamicval =  this.dynamicSenderVal[0];
   this.SenderTempArray = this.sharedservice.customSerach(this.senderidList,dynamicval,selectedValue);
   // console.log(this.dynamicCampaignVal[0],'va');
    }
  }
  senderidKeyup(){
    this.SenderTempArray = this.senderidList;
  }
  senderidBlur(){
    this.SenderTempArray = this.senderidList;
  }
  customsourceSearch(){
    if (this.dynamicSourceVal != undefined) {
    const selectedValue =this.sourceToBeSelected;
    const dynamicval =  this.dynamicSourceVal[0];
   this.SourceTempArray = this.sharedservice.customSerach(this.sourceList,dynamicval,selectedValue);
    
    }
}
sourceKeyup(){
  this.SourceTempArray = this.sourceList;
}
sourceBlur(){
  this.SourceTempArray = this.sourceList;
}
  ngOnDestroy(): void {

    if(this.sourceLoading &&  this.campLoading &&  this.senderidLoading){
      this.sourceLoading.unsubscribe();
    this.campLoading.unsubscribe();
    this.senderidLoading.unsubscribe();
  }
   
  if (this.summaryViewIcon) {
    this.summaryViewIcon.unsubscribe();
  }
  if (this.logDownloadIcon) {
    this.logDownloadIcon.unsubscribe();
  }
  if (this.summaryDownloadIcon) {
    this.summaryDownloadIcon.unsubscribe();
  }
  if (this.logViewIcon) {
    this.logViewIcon.unsubscribe();
  }
  
  }
}
