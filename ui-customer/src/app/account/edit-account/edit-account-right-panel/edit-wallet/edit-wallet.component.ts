import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { ControlContainer, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { map } from 'rxjs/operators';
import { ACCT_CONSTANTS } from 'src/app/account/account.constants';
import { AccountService } from 'src/app/account/account.service';
import { LocalStorageService } from 'src/app/authentication/local-storage.service';
import { decimalValidator } from 'src/app/campaigns/validators/decimal-validator';
import { minLengthValidator } from 'src/app/campaigns/validators/minlength-validator';
import { EnterExitRight, Container1 } from "src/app/shared/animation";
import { CONSTANTS } from 'src/app/shared/campaigns.constants';
import { CONSTANTS_URL } from 'src/app/shared/compaign.url';
import { EditAccountService } from '../../edit-account.service';

@Component({
  selector: 'app-edit-wallet',
  templateUrl: './edit-wallet.component.html',
  styleUrls: ['./edit-wallet.component.css'],
  animations: [EnterExitRight, Container1]
})
export class EditWalletComponent implements OnInit, OnDestroy {
  response: { message: string, statusCode: number };
  status: string;
  popup = false;
  spinner = false;
  editSpinner = false;
  submitSpinner = false;
  loggedInUserWalletBal: number;
  edittedUserWalletBal: number;
  linkIndex: number = 4;
  subscription: Subscription;
  showWallet = false;
  walletFormGroup: any;
  type = "add";
  cliId: string;
  cliIdEdit: string;
  disabled = false;
  decimalNumTotLength = ACCT_CONSTANTS.wallet_rate;
  decimalErrors = CONSTANTS.ERROR_DISPLAY.decimalPatternMsg;
  walletAmountInfoText = ACCT_CONSTANTS.INFO_TXT.walletAmountEdit;
  badError: any;
  BASE_URL = CONSTANTS_URL.GLOBAL_URL;
  editUserBalRetrievalErr = false;
  logUserBalRetrievalErr = false;
  walletCommentsMinLength = ACCT_CONSTANTS.genericMinFieldMinLength;
  insufficientBalErr = false;
  currencyType = CONSTANTS.currency;
  currencyFormat = CONSTANTS.curencyFormat;

  constructor(private http: HttpClient,
    private controlContainer: ControlContainer,
    private accountService: AccountService,
    private route: ActivatedRoute,
    private localStorageService: LocalStorageService,
    private editAcctService : EditAccountService
  ) { }

  ngOnInit(): void {

    this.walletFormGroup = this.controlContainer.control;
    this.getBillingType();
    this.currencyType   = this.editAcctService.currency;
    this.subscription = this.route.queryParams.subscribe(
      params => {
        this.cliIdEdit = params["accounts"];
        this.getEdittedUserWalletBalance();
      });
  }

  getBillingType() {
    //need to refresh token??
    const user = this.localStorageService.getLocal('user');
    let userObj = null;

    if (user) {
      userObj = JSON.parse(user);
    }
    if (userObj.bill_type === 1) {
      //this.showWallet = true;

      this.cliId = userObj.cli_id;


    }

  }

  getEdittedUserWalletBalance() {

    this.editSpinner = true;
    this.disabled = true;
    this.editUserBalRetrievalErr = false;
    this.accountService.getWalletBal(this.cliIdEdit).subscribe(
      (res: any) => {
        this.editSpinner = false;
        this.disabled = false;
        this.edittedUserWalletBal = res.wallet_bal;
        this.wallet.setValue(res.wallet_bal);

      },
      (error: HttpErrorResponse) => {

        this.disabled = true;
        this.editSpinner = false;
        this.editUserBalRetrievalErr = true;
        let err = this.accountService.badError
        this.response = err;
        this.status = error.message
        // this.popup=true;
      });
  }
  checkBal() {

    this.insufficientBalErr = false;
    if (this.type === "add" && this.amount.value > this.loggedInUserWalletBal) {
      this.insufficientBalErr = true;
    }
    if (this.type === "deduct" && this.amount.value > this.edittedUserWalletBal) {
      this.insufficientBalErr = true;

    }
  }
  getLoggedInUserBal() {
    this.spinner = true;
    this.disabled = true;
    this.logUserBalRetrievalErr = false;
    this.accountService.getWalletBal(this.cliId).subscribe(
      (res: any) => {
        this.spinner = false;
        this.disabled = false;
        this.loggedInUserWalletBal = res.wallet_bal;

      },
      (error: HttpErrorResponse) => {

        this.disabled = true;
        this.logUserBalRetrievalErr = true;
        this.spinner = false;
        let err = this.accountService.badError
        this.response = err;
        this.status = error.message
        // this.popup=true;
      });
  }
  onSubmit() {
    //this.accountService.clearAllValidators(this.walletFormGroup);
    this.amount.markAsTouched({ onlySelf: true });
    this.comments.markAsTouched({ onlySelf: true });
    const commentsVal = this.comments.value
    this.amount.setValue(this.amount.value.trim());
    this.checkBal();
    if (this.walletFormGroup.valid && !this.insufficientBalErr) {

      this.updateWallet();
    } else {
      //console.log(this.walletFormGroup)
    }
  }
  retryEditUserBal() {
    this.getEdittedUserWalletBalance();
  }

  retryLogUserBal() {
    this.getLoggedInUserBal();
  }
  updateWallet() {

    this.submitSpinner = true;
    this.accountService.updateWalletBal(this.cliIdEdit, this.amount.value, this.type, this.comments.value).subscribe(
      (res: any) => {
        this.submitSpinner = false;
        this.popup = true;
        this.response = res;
        this.status = this.response.message;

      },
      (error: HttpErrorResponse) => {
        this.disabled = true;
        this.submitSpinner = false;
        let err = this.accountService.badError
        this.response = err;
        this.status = error.message
        this.popup = true;

      });
  }
  onSelect(type: string) {
    this.type = type;
    this.checkBal();
  }
  showPopUp() {
    this.showWallet = true;
    this.getLoggedInUserBal();
    this.setWalletSectionValidators();
    this.amount.markAsUntouched({ onlySelf: true });
    this.comments.markAsUntouched({ onlySelf: true });
  }
  closeWallet() {
    if (!this.submitSpinner) {
      this.showWallet = false;
    }
    this.getEdittedUserWalletBalance();
    this.walletFormGroup.markAsPristine();
    this.amount.setValue("");
    this.comments.setValue("");
    this.amount.markAsUntouched({ onlySelf: true });
    this.comments.markAsUntouched({ onlySelf: true });
  }

  modalClose(event: boolean) {

    this.popup = false;
    this.getEdittedUserWalletBalance();
  }


  tryAgain(event: any) {
    this.onSubmit();

  }

  modalcontinue(event: boolean) {
    this.popup = false;
    this.showWallet = false;
    this.getEdittedUserWalletBalance();
    this.type = "add";
    this.amount.setValue("");
    this.comments.setValue("");
    this.walletFormGroup.markAsPristine();
    this.amount.markAsUntouched({ onlySelf: true });
    this.comments.markAsUntouched({ onlySelf: true });

  }



  get userName() {
    return this.walletFormGroup.controls.userName
  }
  get wallet() {
    return this.walletFormGroup.controls.walletAmount
  }

  get comments() {
    return this.walletFormGroup.controls.walletComments
  }
  get amount() {
    return this.walletFormGroup.controls.patchAmount
  }
  setWalletSectionValidators() {
    this.accountService.clearAllValidators(this.walletFormGroup);
    this.amount.setValidators([
      decimalValidator("GTzero", this.decimalNumTotLength), Validators.required
    ]);
    this.amount.updateValueAndValidity();

    this.comments.setValidators([minLengthValidator(this.walletCommentsMinLength), Validators.required]);
    this.comments.updateValueAndValidity();

  }
  ngOnDestroy() {
    this.subscription.unsubscribe();
  }

}
