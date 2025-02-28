import { getCurrencySymbol } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { ControlContainer } from '@angular/forms';
import { Subscription } from 'rxjs';
import { CONSTANTS } from 'src/app/shared/campaigns.constants';
import { EditAccountService } from '../edit-account.service';
import { MsgSettings } from '../model/msg-settings';
import { PersonalInfo } from '../model/personal-info';

@Component({
  selector: 'app-edit-account-left-panel',
  templateUrl: './edit-account-left-panel.component.html',
  styleUrls: ['./edit-account-left-panel.component.css']
})
export class EditAccountLeftPanelComponent implements OnInit, OnDestroy {

  LeftPanelFormGroup: any;
  showModal = false;
  action = "";
  subscription: Subscription;
  subscriptionPI: Subscription;
  currencyType = CONSTANTS.currency;
  currencyFormat = CONSTANTS.curencyFormat;
  currency = "";
  currencySymbol : any;
  constructor(
    private editAcctService: EditAccountService,
    private parentControl: ControlContainer,
    
  ) {
  }

  ngOnInit(): void {

    this.LeftPanelFormGroup = this.parentControl.control;
    this.editAcctService.setCtrlMsgSettings(this.LeftPanelFormGroup);
    this.currency = this.editAcctService.getCurrencyType();
    this.currencySymbol = getCurrencySymbol(this.currency, "narrow");
    
    //to reflect the change in tz
    this.subscription = this.editAcctService
      .listenForTzChanges()
      .subscribe((data: MsgSettings) => {

        this.zone.setValue(data.zone);
        this.tzAbbr.setValue(data.tzAbbr);

      });

    this.subscriptionPI = this.editAcctService
      .listenForPIChanges()
      .subscribe((data: PersonalInfo) => {

        this.firstName.setValue(data.firstName);
        this.lastName.setValue(data.lastName);

      });

  }
  closeModal() {
    this.showModal = false;
  }

  onChangeAccStatus(action: string) {

    this.showModal = true;
    this.action = action;
  }
  updateStatusChange(event: string) {
    this.showModal = false;

    if (event === "deactivate") {
      this.acctStatus.setValue("deactivated");
      this.action = "deactivated";
    } else {
      this.acctStatus.setValue("Active");
      this.action = "Active";
    }
  }
  get zone() {
    return this.LeftPanelFormGroup.controls.zone
  }
  get tzAbbr() {
    return this.LeftPanelFormGroup.controls.tzAbbr
  }
  get billType() {
    return this.LeftPanelFormGroup.controls.billType.value
  }
  get userType() {
    return this.LeftPanelFormGroup.controls.userType.value
  }
  get acctStatus() {
    return this.LeftPanelFormGroup.controls.acctStatus
  }

  get firstName() {
    return this.LeftPanelFormGroup.controls.firstName

  }
  get lastName() {
    return this.LeftPanelFormGroup.controls.lastName
  }
  get wallet() {
    return this.LeftPanelFormGroup.controls.walletAmount
  }
  get contactMobile() {
    return this.LeftPanelFormGroup.controls.contactMobile
  }
  get contactEmail() {
    return this.LeftPanelFormGroup.controls.contactEmail
  }
  get userName() {
    return this.LeftPanelFormGroup.controls.userName
  }
  get userCount() {
    return this.LeftPanelFormGroup.controls.userCount
  }
  get acctCount() {
    return this.LeftPanelFormGroup.controls.acctCount
  }
  get adminCount() {
    return this.LeftPanelFormGroup.controls.adminCount
  }

  ngOnDestroy() {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
    if (this.subscriptionPI) {
      this.subscriptionPI.unsubscribe();
    }

  }


}
