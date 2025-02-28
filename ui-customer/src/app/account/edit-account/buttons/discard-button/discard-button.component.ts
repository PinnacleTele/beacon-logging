import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { ControlContainer } from '@angular/forms';
import { EditAccountService } from '../../edit-account.service';
import { SaveApisService } from '../save-button/save-apis.service';

@Component({
  selector: 'app-discard-button',
  templateUrl: './discard-button.component.html',
  styleUrls: ['./discard-button.component.css']
})
export class DiscardButtonComponent implements OnInit {
  @Input() formGroup: any; 
  @Input() formChanged : boolean; 
  @Input() title : string; 
  @Input() disableButton: any = false;
  @Input() disableSpinner: any = false;
  @Input() newChangeDetect: any = true;

  @Output() updated = new EventEmitter();
  
  constructor(private editAcctSvc : EditAccountService,
    private saveApiSvc : SaveApisService) { }
spinner =false;
  
  ngOnInit(): void {
   
  }
    
  onSubmit() {
    
    if (this.title === 'PI') {
      this.discardAccountInfo();
    }else if (this.title === 'twoFA') {
      this.discardwoFA();
    }else if (this.title === 'MsgSettings') {
      this.discardMsgSettings();
    }else if (this.title === 'DLT') {
      this.discardDLT();
    }else if (this.title === 'Services') {
     this.discardServices();
    }else if (this.title === 'Groups') {
      this.discardGroups();
    }else if (this.title === 'encrypt') {
      this.discardEncrypt();
    }else if (this.title === 'smsDltRates') {
      this.smsDltRates();
    }
    
  }
  discardGroups(){
    this.formChanged = false;
    this.editAcctSvc.setCtrlSharedGrps(this.formGroup);
    this.updated.emit();
  }
  discardServices(){
    this.editAcctSvc.setCtrlServices(this.formGroup);
    this.formChanged = false;
    this.updated.emit();
  }
  discardEncrypt(){
    this.editAcctSvc.setFormCtrlEncrypt(this.formGroup);
    this.formChanged = false;
    this.updated.emit();

  }
  discardwoFA(){
    this.editAcctSvc.setFormCtrlTwoFA(this.formGroup);
    this.formChanged = false;
    this.updated.emit();
  }
  discardAccountInfo(){
    this.editAcctSvc.setFormCtrlPI(this.formGroup);
    this.formChanged = false;
    this.formGroup.markAsPristine();
  }
  smsDltRates(){
    
    this.editAcctSvc.setWalletRates(this.formGroup);
    this.formChanged = false;
    this.formGroup.markAsPristine();
  }
  discardDLT(){
    this.editAcctSvc.setFormCtrlDLTCard(this.formGroup);
    this.formChanged = false;
    
    this.updated.emit();

  }
  discardMsgSettings(){
    
    this.editAcctSvc.setCtrlMsgSettings(this.formGroup);
    this.formChanged = false;
    this.updated.emit();
  }
}
