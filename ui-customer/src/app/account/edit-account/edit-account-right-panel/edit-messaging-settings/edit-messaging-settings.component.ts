import { Component, OnInit, ViewChild } from '@angular/core';
import {  FormBuilder } from '@angular/forms';
import { NgSelectComponent } from '@ng-select/ng-select';
import { AccountService } from 'src/app/account/account.service';
import { SetValidationsService } from 'src/app/account/set-validations.service';
import { EditAccountService } from '../../edit-account.service';

@Component({
  selector: 'app-edit-messaging-settings',
  templateUrl: './edit-messaging-settings.component.html',
  styleUrls: ['./edit-messaging-settings.component.css'],
  
})
export class EditMessagingSettingsComponent implements OnInit {
  @ViewChild(NgSelectComponent) ngSelectComponent: NgSelectComponent;
  formChanged = false;
 
  itemToBeSelected = "";
  
  tzDiscarded = false;
  discarded = false;
  
  MsgSettingsFormGroup = this.fb.group({
    tzAbbr : [],
    tz :[,],
    zone : [],
    //country :[,],
    newlineChar : [,],
  });
  constructor(private accountService : AccountService,
    private editAcctSvc : EditAccountService,
    private setValidatorSvc : SetValidationsService,
    private fb :FormBuilder) { }

  ngOnInit(): void {
    this.editAcctSvc.setCtrlMsgSettings(this.MsgSettingsFormGroup);
    this.MsgSettingsFormGroup.markAsPristine();
    this.accountService.clearAllValidators(this.MsgSettingsFormGroup);
    this.setValidatorSvc.setValidatorsToMsgSettings(this.MsgSettingsFormGroup);
    this.MsgSettingsFormGroup.valueChanges.subscribe((data:any)=>{
      //  console.log(data,'data on value subscribe');
        
        this.checkIfValueChange(data);
      })
  }

   handleSelection(){
   // this.formChanged = true;
    
  }
  handleTZSelection(){
  //  this.formChanged = true;
    this.tzDiscarded = false;
  }
  
  handleDiscarded(){
    this.formChanged = false;
    this.tzDiscarded = true;
    this.editAcctSvc.setCtrlMsgSettings(this.MsgSettingsFormGroup);
    this.MsgSettingsFormGroup.markAsPristine();
  }
  
  handleUpdate(){
    this.formChanged = false;
    this.tzDiscarded = true;
    this.MsgSettingsFormGroup.markAsPristine();
  }
  get country(){
    return this.MsgSettingsFormGroup.controls.country;
  }
  checkIfValueChange(changedValue){
    let Existvalue  =this.editAcctSvc.msgSettingObj;
    const ExistzoneValue = Existvalue.zone.toLowerCase();
    const changedZoneValue = changedValue.zone.toLowerCase();
    if (changedValue?.tz) {
    
    }else{
     
      
      this.MsgSettingsFormGroup.markAsDirty();
      return
    }
    console.log(changedValue);
    
    if (ExistzoneValue != changedZoneValue ||
      Existvalue.newlineChar !=  changedValue.newlineChar) {  
      console.log('inside if',ExistzoneValue,changedZoneValue);
      this.MsgSettingsFormGroup.markAsDirty();
    } else {      
      console.log('prestine');
      
      this.MsgSettingsFormGroup.markAsPristine();
    }   
  }
}

 

 

