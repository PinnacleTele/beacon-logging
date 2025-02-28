import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ControlContainer, Validators } from '@angular/forms';
import { LocalStorageService } from 'src/app/authentication/local-storage.service';
import { decimalValidator } from 'src/app/campaigns/validators/decimal-validator';
import { minLengthValidator } from 'src/app/campaigns/validators/minlength-validator';
import { CONSTANTS } from 'src/app/shared/campaigns.constants';
import { WalletuserService } from 'src/app/shared/sliders/myacct-wallet-user-slider/walletuser.service';
import { ACCT_CONSTANTS } from '../../account.constants';
import { AccountService } from '../../account.service';
import { EditAccountService } from '../../edit-account/edit-account.service';
import { WalletUsers } from '../../shared/model/wallet-users';

@Component({
  selector: 'app-my-acct-wallet',
  templateUrl: './my-acct-wallet.component.html',
  styleUrls: ['./my-acct-wallet.component.css']
})
export class MyAcctWalletComponent implements OnInit {
  users: WalletUsers[] = [];
  selectedUsers: WalletUsers[] = [];
  disabled = true;
  walletBalSpinner = false;
  showSlider = false;
  openClearModal = false;
  showSMPP = false;
  type = "add";
  cliIdArr = [];
  errorMsg = "";
  response: { message: string, statusCode: number };
  status: string;
  popup = false;
  spinner = false;
  noSharedGroups = ACCT_CONSTANTS.NO_MATCHING_USERS;
  walletAmountInfoText = ACCT_CONSTANTS.INFO_TXT.myAcctWalletInfo;
  noRecords = false;
  selectedUsersWalletAmountTot = 0;
  walletBalErr = false;

  walletFormGroup: any;
  loggedInUserWalletBal: number;
  logUserBalRetrievalErr: boolean;
  submitSpinner = false;
  sliderSpinner = false;
  sliderErr = false;
  walletCommentsMinLength = ACCT_CONSTANTS.genericMinFieldMinLength;
  currencyType = CONSTANTS.currency;
  currencyFormat = ACCT_CONSTANTS.curencyFormat;

  constructor(private accountService: AccountService,
    private controlContainer: ControlContainer,
    private sliderService: WalletuserService,
    private editAcctService: EditAccountService,
    private localStorageService : LocalStorageService) { }


  ngOnInit(): void {
//    this.currencyType   = this.editAcctService.currency;
    this.walletFormGroup = this.controlContainer.control;
    const fromSvc: WalletUsers[] = this.sliderService.getAllselectedItems();
    this.amount.setValue("");
    this.accountService.markAsUntouched(this.walletFormGroup);
    this.accountService.clearAllValidators(this.walletFormGroup);
    this.getLoggedInUserBal();
    this.setWalletSectionValidators();
  }

  getLoggedInUserBal() {
    this.walletBalSpinner = true;
    this.disabled = true;
    this.logUserBalRetrievalErr = false;
    const myAcctCliId = this.editAcctService.cliId;
    let userData = this.localStorageService.getLocal('user');
    let jsonData = JSON.parse(userData);
    this.currencyType = jsonData.billing_currency;
    
    this.accountService.getWalletBal(myAcctCliId.toString()).subscribe(
      (res: any) => {
        this.walletBalSpinner = false;
        this.disabled = false;
        this.loggedInUserWalletBal = res.wallet_bal;
        this.loggedInUserWalletBal;
        this.wallet.setValue(this.loggedInUserWalletBal);
      },
      (error: HttpErrorResponse) => {

        this.disabled = true;
        this.logUserBalRetrievalErr = true;
        this.walletBalSpinner = false;
        let err = this.accountService.badError
        this.response = err;
        this.status = error.message
        // this.popup=true;
      });
  }

  getUserSliderData() {

    this.sliderSpinner = true;
    this.sliderErr = false;
    this.disabled = true;
    this.users = [];
    this.noRecords = false;
    //this.showSlider = true;
    //this.editUserBalRetrievalErr = false;

    if (this.amount != null &&
      !this.walletBalErr &&
      this.amount.value > 0) {

      this.showSlider = true;
      this.accountService.getUsersWallet(this.amount.value, this.type).subscribe(
        (res: any) => {
          this.sliderSpinner = false;
          this.disabled = false;
          res.forEach((ele) => {
            ele.checked = false;
            if (ele.user_type === 0) {
              ele.userTypeDesc = "Super Admin"
            } else if (ele.user_type === 1) {
              ele.userTypeDesc = "Admin"
            } else if (ele.user_type === 2) {
              ele.userTypeDesc = "User"
            }

            if (ele.acc_status === 0) {
              ele.acctStatus = "Active"
            } else if (ele.acc_status === 1) {
              ele.acctStatus = "deactivated"
            }
            this.users.push(ele);

          });

          if (this.users.length == 0) {
            this.noRecords = true;
          }
          this.sliderService.list = this.users;
          this.sliderService.loopListAndMarkChecked(this.selectedUsers);

        },
        (error: HttpErrorResponse) => {

          this.disabled = true;
          this.sliderSpinner = false;
          // this.editUserBalRetrievalErr = true;
          let err = this.accountService.badError
          this.response = err;
          this.status = error.message
          this.sliderErr = true;
        });
    } else {
      this.amount.markAsTouched({ onlySelf: true });

    }




  }
  checkBal() {
    //dont do amount validation here
    this.amount.setValue(this.amount.value.trim());
    if (this.type === "add" && this.amount.value > this.loggedInUserWalletBal) {
      this.walletBalErr = true;
      this.errorMsg = "Not enough balance";
    } else {
      this.walletBalErr = false;
    }

  }
  tryAgain() {
    this.popup = false;
    this.getUserSliderData();

  }

  openModal() {
    this.openClearModal = true;
  }
  modalContinueSubmit(event: any) {
    this.popup = false;
    this.getLoggedInUserBal();
    this.reset();

  }
  modalCloseSubmit(event: any) {
    this.popup = false
  }

  clearModalResponse(response: string) {

    if (response === "clear") {
      this.onClickRemoveAll();
      this.openClearModal = false;
    }
    if (response === "close") {
      this.openClearModal = false;
    }
  }


  closeSlider() {
    this.showSlider = false;
  }

  addedItems(event: any) {

    this.selectedUsers = event;
    this.setWalletUsersValue(event);
    this.selectedUsersWalletAmountTot = 0;
    this.checkAmountAgainstUsersCount();
  }
  checkAmountAgainstUsersCount() {
    this.walletBalErr = false;
    const reqdBal = this.amount.value * this.selectedUsers.length;
    if (this.type === "add" &&
      this.loggedInUserWalletBal < reqdBal) {
      this.walletBalErr = true;
      this.errorMsg = "your current balance is " + this.loggedInUserWalletBal + " and your required balance is " + reqdBal;
    }
  }

  getCliIdsForSelectedWalletUsers() {
    this.selectedUsers.forEach((ele: WalletUsers) => {

      this.cliIdArr.push(ele.cli_id);
    });
  }
  onClickRemoveAll() {
    this.walletBalErr = false;
    this.selectedUsers = [];
    this.users.forEach((el) => {
      el.checked = false;
    });
    this.sliderService.list = this.users;
    this.sliderService.populateselectedItems(this.selectedUsers);

    this.setWalletUsersValue("");
    this.userCtrl.markAsTouched();
  }

  removeItem(i: number) {
    const deleted: WalletUsers = this.selectedUsers.splice(i, 1)[0];

    const index = this.users.findIndex(x => x.cli_id === deleted.cli_id);
    if (this.selectedUsers.length === 0) {
      this.setWalletUsersValue("");
      this.userCtrl.markAsTouched();
      this.walletBalErr = false;
    } else {
      this.checkAmountAgainstUsersCount();
    }

    if (index !== -1) {

      this.users[index].checked = false;
      this.sliderService.populateselectedItems(this.selectedUsers);
      this.setWalletUsersValue(this.selectedUsers);

    }
  }
  setWalletUsersValue(val: any) {
    this.walletFormGroup.controls.walletUsers.setValue(val);
    // if(val === ""){
    this.cliIdArr = [];
    // }else{
    this.getCliIdsForSelectedWalletUsers();
    // }

  }
  onSubmit() {
    //this.accountService.clearAllValidators(this.walletFormGroup);
    this.amount.markAsTouched({ onlySelf: true });
    this.comments.markAsTouched({ onlySelf: true });
    this.userCtrl.markAsTouched({ onlySelf: true });
    //this.checkBal();

    if (this.walletFormGroup.valid && !this.walletBalErr) {
      this.updateWallet();
    }
  }
  onSelect(type: string) {
    this.type = type;
    this.reset();
    // this.checkBal();
  }
  onClickCancel() {
    this.type = "add";
    this.reset();
  }
  reset() {
    this.walletBalErr = false;
    this.amount.setValue("");
    this.comments.setValue("");

    this.amount.markAsUntouched({ onlySelf: true });
    this.comments.markAsUntouched({ onlySelf: true });
    this.userCtrl.markAsUntouched({ onlySelf: true });
    this.selectedUsers = [];
    this.cliIdArr = [];

    this.sliderService.populateselectedItems(this.selectedUsers);

    this.setWalletUsersValue("");
  }
  updateWallet() {

    this.submitSpinner = true;

    this.accountService.updateMultiUsersWalletBal(this.amount.value, this.type, this.cliIdArr, this.comments.value).subscribe(
      (res: any) => {
        this.submitSpinner = false;

        //if(res.statusCode < 0){
        this.popup = true;
        this.response = res;
        this.status = this.response.message;
        //}
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


  get userCtrl() {
    return this.walletFormGroup.controls.walletUsers;
  }


  clearValidatorsToFormFields() {
    this.userCtrl.clearValidators();
    this.userCtrl.updateValueAndValidity();
  }

  get amount() {
    return this.walletFormGroup.controls.patchAmount
  }
  get comments() {
    return this.walletFormGroup.controls.walletComments
  }
  setWalletSectionValidators() {

    this.amount.setValidators([
      decimalValidator("GTzero", ACCT_CONSTANTS.wallet_rate),
      Validators.required
    ]);
    this.amount.updateValueAndValidity();

    this.comments.setValidators([minLengthValidator(this.walletCommentsMinLength),
    Validators.required]);
    this.comments.updateValueAndValidity();

    this.userCtrl.setValidators(Validators.required);
    this.userCtrl.updateValueAndValidity();

  }
  get wallet() {
    return this.walletFormGroup.controls.walletAmount;
  }

}
