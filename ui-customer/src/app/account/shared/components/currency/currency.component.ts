import { Component, Input, OnInit } from '@angular/core';
import { ControlContainer } from '@angular/forms';
import { ACCT_CONSTANTS } from 'src/app/account/account.constants';
import { CONSTANTS } from 'src/app/shared/campaigns.constants';
import { getCurrencySymbol } from '@angular/common';
import { LocalStorageService } from 'src/app/authentication/local-storage.service';
import { EditAccountService } from 'src/app/account/edit-account/edit-account.service';

@Component({
  selector: 'app-currency',
  templateUrl: './currency.component.html',
  styleUrls: ['./currency.component.css']
})
export class CurrencyComponent implements OnInit {
  itemToBeSelected = "";
  currencyFormGroup : any;
  currencyType = CONSTANTS.currency;
  currencyFormat = CONSTANTS.curencyFormat;
  currencyInfoText = ACCT_CONSTANTS.INFO_TXT.currency;
  spinner = false;
  @Input() fromPage = "createAcct";

  constructor(private controlContainer: ControlContainer,
    private localStorageSvc : LocalStorageService,
    private editAcctService : EditAccountService) { }

  ngOnInit(): void {
       this.currencyFormGroup = this.controlContainer.control;
    //need to refresh token?? 
    const user = this.localStorageSvc.getLocal("user");
    const userDetails = JSON.parse(user);
    this.currencyType = userDetails.billing_currency;
    this.currencyFormGroup.controls.currency.setValue(this.currencyType);
    
    
  }
  getSelectedCurrency(event: any) {
       
    if(event !== undefined){
     this.currencyFormGroup.controls.currency.setValue(event);
     
    }
    
    
   }

   get currency(){
    return this.currencyFormGroup.get("currency");
  }
}
