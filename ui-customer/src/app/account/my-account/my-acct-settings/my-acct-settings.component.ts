import { getCurrencySymbol } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { BillingDetailService } from 'src/app/billing-detail/billing-detail.service';
import { AccountService } from '../../account.service';
import { EditAccountService } from '../../edit-account/edit-account.service';
import { CONSTANTS, value } from "src/app/shared/campaigns.constants";
import { LocalStorageService } from 'src/app/authentication/local-storage.service';

@Component({
  selector: 'app-my-acct-settings',
  templateUrl: './my-acct-settings.component.html',
  styleUrls: ['./my-acct-settings.component.css']
})
export class MyAcctSettingsComponent implements OnInit {
  loggedInUser: any;
  walletBalSpinner = false;
  logUserBalRetrievalErr = false;
  walletLoading = false;
  walletApiError = false;
  loggedInUserWalletBal: any;
  loggedInUserSmsBal: any;
  totalCountry: number = 0;
  otherCountryBillingRate :any[]=[];
  billingRateApiError = false;
  billingRateLoading = false;
  cliId: any;
  smsRate: any;
  dltRate: any;
  billingCurrency: any;
  myOptions = value;
  constructor(  private billingRateService : BillingDetailService,
    private accountService: AccountService, private localStorageService:LocalStorageService
    ) { }
  ngOnInit(): void {
   this. walletApiCalls();
  }

  
  walletApiCalls(){
    //this.balanceShowList = this.balanceListDropdown();
        const user = JSON.parse(this.localStorageService.getLocal('user')) ;
         const usertype = user.bill_type;
         this.cliId = user.cli_id;
         this.loggedInUser = usertype;
         if (this.loggedInUser == 1) {
            this.getaccountInfo();
            this.getLoggedInUserBal();
            this.billingRateOtherCountries();
         }else{
            this.getaccountInfo();
            this.billingRateOtherCountries();
         }
}

getLoggedInUserBal() {
  this.walletBalSpinner = true;

  this.logUserBalRetrievalErr = false;
  this.accountService.getWalletBal(this.cliId.toString()).subscribe(
      (res: any) => {                
          this.walletBalSpinner = false;
          this.loggedInUserWalletBal = res.wallet_bal;
          this.loggedInUserSmsBal = res.sms_left;

      },
      (error: HttpErrorResponse) => {

          this.logUserBalRetrievalErr = true;
          this.walletBalSpinner = false;

      });
}

billingRateOtherCountries(){
    this.walletLoading = true;
    this.walletApiError = false;
    this.billingRateService.internationalRates().subscribe((data:any)=>{
        this.walletLoading = false;
       const rowData = data.filter((data:any)=>{
            return data.country.toLowerCase() != 'row' ;
        })
        this.totalCountry = rowData.length;
        console.log(rowData);
        
       this.otherCountryBillingRate = data;

       
    },
    (error:HttpErrorResponse)=>{
        this.walletLoading = false;
this.walletApiError = true;
    })
}

getaccountInfo(){
    this.billingRateApiError = false;

   this.billingRateLoading = true;
    const user = JSON.parse(this.localStorageService.getLocal('user')) ;
    console.log(user);
  
    const cli_id = user.cli_id;
    this.billingRateService.getAcctInfo(cli_id).subscribe((data:any)=>{
        this.billingRateLoading = false;
        this.smsRate = data.smsrate;
        this.dltRate = data.dltrate;
        this.billingCurrency = data.billing_currency;        
    },
    ((error:HttpErrorResponse)=>{
        this.billingRateLoading = false;
        this.billingRateApiError = true;
    }))
}

getC_Symbol(eve:any){
    return getCurrencySymbol(eve,'narrow');
  }

  retryGetBal(){
    this.getLoggedInUserBal();
  }

  
  retryBillingRates(){

    this.getaccountInfo();
}

retryOtherCountryRates(){
    this.billingRateOtherCountries();
}
}