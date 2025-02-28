import { Component, OnInit } from '@angular/core';
import { ControlContainer } from '@angular/forms';
import { EditAccountService } from '../../edit-account.service';

@Component({
  selector: 'app-edit-data-securities-settings',
  templateUrl: './edit-data-securities-settings.component.html',
  styleUrls: ['./edit-data-securities-settings.component.css']
})
export class EditDataSecuritiesSettingsComponent implements OnInit {
  dataSecurityFormGroup : any;
  formChanged = false;
  toggleMob = false;
  toggleMsg = false;
  constructor( private controlContainer: ControlContainer,
    private editAcctSvc : EditAccountService) {
  }

  ngOnInit(): void {
 
    this.dataSecurityFormGroup =  this.controlContainer.control;
    this.editAcctSvc.setFormCtrlEncrypt(this.dataSecurityFormGroup);
    this.toggleMob = this.dataSecurityFormGroup.controls.encrytMob.value;
    this.toggleMsg = this.dataSecurityFormGroup.controls.encryMsg.value;
    
  }
   onClickToggleMob(event : any){
    this.formChanged = true;
    this.toggleMob = !this.toggleMob;
    this.mobile.setValue(this.toggleMob);
  }

  handleUpdate(){
    this.formChanged = false;
    this.toggleMob = this.dataSecurityFormGroup.controls.encrytMob.value;
    this.toggleMsg = this.dataSecurityFormGroup.controls.encryMsg.value;
  }
  onClickToggleMsg(event : any){
    this.formChanged = true;
    this.toggleMsg = !this.toggleMsg;
    this.msg.setValue(this.toggleMsg);
    
  }
  get mobile() {
    return this.dataSecurityFormGroup.controls.encrytMob;
  }
  get msg() {
    return this.dataSecurityFormGroup.controls.encryMsg;
  }
}
