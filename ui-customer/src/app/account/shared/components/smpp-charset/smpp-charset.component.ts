import { Component, EventEmitter, Input, OnInit, Output, SimpleChanges, ViewChild } from '@angular/core';
import { ControlContainer } from '@angular/forms';
import { NgSelectComponent } from '@ng-select/ng-select';

import { ACCT_CONSTANTS } from 'src/app/account/account.constants';
import { EditAccountService } from 'src/app/account/edit-account/edit-account.service';

@Component({
  selector: 'app-smpp-charset',
  templateUrl: './smpp-charset.component.html',
  styleUrls: ['./smpp-charset.component.css']
})
export class SmppCharsetComponent implements OnInit {
  smppCharsetFormGroup : any;
  @Input() discarded : boolean;
  infoText = ACCT_CONSTANTS.INFO_TXT.SMPPchars;
  reqdText = ACCT_CONSTANTS.ERROR_DISPLAY.smppReqdText;
  itemList : string[] = [];
  @ViewChild(NgSelectComponent) smppchar: NgSelectComponent;
  
  @Output() smppChanged = new EventEmitter();
  constructor(
    private editAcctSvc : EditAccountService,
    private parentControl: ControlContainer) {
  }

  ngOnChanges(changes: SimpleChanges): void {
     
    if(this.discarded){
      console.log('indiscord');
      
      this.smppchar.handleClearClick();
      this.charsetCtrl.setValue(this.editAcctSvc.smppCharset);
      this.smppChanged.emit();
      this.smppCharsetFormGroup.markAsPristine();
  }
}
  ngOnInit(): void {
    this.smppCharsetFormGroup = this.parentControl.control ;
    this.itemList = ACCT_CONSTANTS.CHARSET;
   }
  
   get charsetCtrl() {
    return this.smppCharsetFormGroup.controls.charset1;
  }
  
getSelectedSMPP(event : any){
 
    this.smppChanged.emit();
  }
  


}
