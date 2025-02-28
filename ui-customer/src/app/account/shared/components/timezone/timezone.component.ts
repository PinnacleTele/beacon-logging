import { Component, EventEmitter, Input, OnDestroy, OnInit, Output, SimpleChanges, ViewChild } from '@angular/core';
import { ControlContainer } from '@angular/forms';
import { NgSelectComponent } from '@ng-select/ng-select';

import { ACCT_CONSTANTS } from 'src/app/account/account.constants';
import { EditAccountService } from 'src/app/account/edit-account/edit-account.service';
import { LocalStorageService } from 'src/app/authentication/local-storage.service';
import { TimeZoneService } from 'src/app/shared/service/timezone.service';

@Component({
  selector: 'app-timezone',
  templateUrl: './timezone.component.html',
  styleUrls: ['./timezone.component.css']
})
export class TimezoneComponent implements OnInit ,OnDestroy{
  @Input() discarded : boolean;
  @Input() edit : string;
  @ViewChild(NgSelectComponent) ngSelectComponent: NgSelectComponent;
  @Output() tzSelected = new EventEmitter();
  @Output() tzError = new EventEmitter<boolean>();
  tzList : string[] = [];
  timeZone: any = [];
  tzFormGroup: any;
  tzInfoText = ACCT_CONSTANTS.INFO_TXT.tz;
  spinner = false;
  Responce:{message:string,statusCode:number}
  status: string;
  popup = false;
  userObj : any;
  itemToBeSelected : any;
 
  public loading=this.timezone.loadingTZ$.subscribe(
   (data)=>{this.spinner=data})
  constructor(  private controlContainer: ControlContainer,
    private timezone: TimeZoneService,
    private localStorage : LocalStorageService,
    ) { }
 

  ngOnChanges(changes: SimpleChanges): void {
     
    if(this.discarded){
      
      this.ngSelectComponent.handleClearClick();
      this.tzFormGroup.markAsPristine();

      if(this.zone.value !== null){
        let zone :any ;
        this.tzList.filter((el:any) =>{
          
            if ( el.zone ===  this.zone.value) {  zone = el; }
        });
       
        this.itemToBeSelected = zone.value;
        this.tz.setValue(this.itemToBeSelected);
      }
    }
    
    
  }

  ngOnInit(): void {
   
    this.tzFormGroup = this.controlContainer.control;
    
    let user:any = this.localStorage.getLocal("user");
    this.userObj =JSON.parse(user);
    
    this.getAllTZ();
  }
  getAllTZ(){
    this.tzError.emit(true);
    this.popup = false;
    this.timezone.getTimeZone().subscribe((dat) => {
      dat.forEach((data: any) => {
        this.tzError.emit(false);
          this.timeZone.push({
              key: data.offset,
              value: data.display_name,
              zone: data.zone_name,
              tzAbbr : data.short_name
          });
          
      });
      this.tzList = this.timeZone;
      if(this.edit === 'edit'){// from ainfo api
        let zone :any ;
        this.tzList.filter((el:any) =>{
            if ( el.zone ===  this.zone.value) {  zone = el;  }
        });
        
        this.itemToBeSelected = zone.value;
        this.tz.setValue(this.itemToBeSelected);
        
      }else{ // from logged in user obj
        let zone :any ;
        if(this.zone.value === null){
          this.tzList.filter((el:any) =>{
             if ( el.zone ===  this.userObj.tz) {  zone = el; }
          });
          this.itemToBeSelected = zone.value;
          this.tz.setValue(this.itemToBeSelected);
          this.tzFormGroup.controls.zone.setValue(zone.zone);
          
        }
        
      }
      
    
    },
    (error : any) => {
      this.spinner = false;
      this.popup=true;
      this.tzError.emit(false);
    });
  }
  getSelectedTz(event: any) {
       
   if(event !== undefined){
    this.tzFormGroup.controls.zone.setValue(event.zone);
    this.tzFormGroup.controls.tzAbbr?.setValue(event.tzAbbr);
   }
   this.tzSelected.emit();
   
  }
  get tz(){
    return this.tzFormGroup.get("tz");
  }

  get zone(){
    return this.tzFormGroup.get("zone");
  }
 
  ngOnDestroy(): void {
    if ( this.loading) {
      this.loading.unsubscribe();
    }
  
    //throw new Error('Method not implemented.');
  }

}
