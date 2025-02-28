import { HttpErrorResponse } from '@angular/common/http';
import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { Router } from '@angular/router';
import { AccountService } from '../../../account.service';
import { SetValidationsService } from '../../../set-validations.service';
import { EditAccountService } from '../../edit-account.service';
import { SaveApisService } from './save-apis.service';
import { animation } from '@angular/animations';
import { openClose } from 'src/app/shared/animation';
import { SubServices } from 'src/app/account/shared/model/service-model';
import { Validators } from '@angular/forms';
import { Subject } from 'rxjs';

@Component({
  selector: 'app-save-button',
  templateUrl: './save-button.component.html',
  styleUrls: ['./save-button.component.css'],
  animations: [openClose]
})
export class SaveButtonComponent implements OnInit {


  spinner = false;

  popup = false;
  @Input() disableButton: any = false;
  @Input() formGroup: any;
  @Input() formChanged: boolean;
  @Input() disableSpinner: any = false;
  @Input() newChangeDetect: any = true;


  @Input() title = ""

  response: { message: string, statusCode: number };

  status: string;
  cliId: number;
  @Output() updated = new EventEmitter();
  disable = false;

  oldsmsRate:number;
  olddltRate:number;

  constructor(private router: Router,
    private editAcctSvc: EditAccountService,
    private accountSvc: AccountService,
    private saveAPIService: SaveApisService,
    private setValidatorSvc: SetValidationsService) { }
  ngOnInit(): void {
    this.cliId = this.editAcctSvc.cliId;

  }

  onCreate() {
    this.spinner = true;
    //this.campaignService.validateAllFormFields(this.newGroupForm);
    this.onSubmit();
  }



  modalClose(event: boolean) {
    this.popup = false;
  }

  tryAgain(event: any) {
    this.popup = false;
    this.onSubmit();

  }

  modalcontinue(event: boolean) {
    this.popup = false;
    this.formGroup.markAsPristine();
  }

  onSubmit() {

    if (this.title === 'PI') {
      this.patchUpdateAccount();
    } else if (this.title === 'twoFA') {
      this.patchUpdatetwoFA();
    } else if (this.title === 'MsgSettings') {
      this.patchUpdateMsgSettings();
    } else if (this.title === 'DLT') {
      this.patchUpdateDLT();
    } else if (this.title === 'Services') {
      this.patchUpdateServices();
    } else if (this.title === 'Groups') {
      this.patchUpdateGroups();
    } else if (this.title === 'encrypt') {
      this.patchUpdateEncrypt();
    } else if (this.title === 'smsDltRates') {
      const oldBillRates = this.editAcctSvc.getOldWalletRates();
      this.olddltRate = oldBillRates.dltrate;
      this.oldsmsRate = oldBillRates.smsrate;
      this.patchUpdateRates();
    }

  }
  patchUpdateRates() {
    console.log(this.formGroup);
    
    this.accountSvc.clearAllValidators(this.formGroup);
    this.setValidatorSvc.setValidatorsToWalletRates(this.formGroup);

    this.accountSvc.validateAllFormFields(this.formGroup);
    if (this.formGroup.valid) {
      this.spinner = true;
      this.saveAPIService.updateWalletRates(this.formGroup, this.cliId,this.oldsmsRate,this.olddltRate)
        .subscribe((res: any) => {
          if (res.statusCode === 200) {
            this.formGroup.markAsPristine();
            this.response = res
            this.status = res.message
            this.spinner = false;
            this.popup = true;
          }
        }, (error: HttpErrorResponse) => {

          this.response = this.saveAPIService.badError;
          this.status = this.response.message
          this.popup = true;
          this.spinner = false;
        })
    } else {
      // console.log(this.formGroup, "invalid")
    }

  }

  patchUpdateEncrypt() {
    // this.updated.emit();
    this.accountSvc.clearAllValidators(this.formGroup);
    this.spinner = true;
    this.saveAPIService.updateEncryptInfo(this.formGroup, this.cliId)
      .subscribe((res: any) => {
        if (res.statusCode === 200) {

          this.formChanged = false;
          this.updated.emit();
          this.response = res
          this.status = res.message
          this.spinner = false;
          this.popup = true;
        }
      }, (error: HttpErrorResponse) => {

        this.response = this.saveAPIService.badError;
        this.status = this.response.message
        this.popup = true;
        this.spinner = false;
      })
  }
  patchUpdateServices() {
    //this.updated.emit();
    if(this.formGroup.controls.charset1){
      this.formGroup.controls.charset1.clearValidators();
      this.formGroup.controls.charset1.updateValueAndValidity();
   }
    this.formGroup.controls.subServices.clearValidators();
    this.formGroup.controls.subServices.updateValueAndValidity();
    
    // this.setValidatorSvc.setValidatorsToServices(this.formGroup);
    const services: SubServices[] = this.formGroup.controls.subServices.value;

    services.forEach((ele: SubServices) => {

      if (ele.sub_service === 'smpp') {
        this.formGroup.controls.charset1.setValidators(Validators.required);
        this.formGroup.controls.charset1.updateValueAndValidity();

      }
     
    });

    this.accountSvc.validateAllFormFields(this.formGroup);
    // console.log(this.formGroup);
    if (this.formGroup.valid) {
      this.spinner = true;
      this.saveAPIService.updateServices(this.formGroup, this.cliId)
        .subscribe((res: any) => {
          if (res.statusCode === 200) {
            
            this.formChanged = false;
            this.updated.emit();
            this.response = res
            this.status = res.message
            this.spinner = false;
            this.popup = true;
          }
          this.saveAPIService.getAcctInfo();
        }, (error: HttpErrorResponse) => {

          this.response = this.saveAPIService.badError;
          this.status = this.response.message
          this.popup = true;
          this.spinner = false;
        });
    }
  }

 
  patchUpdateGroups() {
    //this.updated.emit();
    this.accountSvc.clearAllValidators(this.formGroup);
    this.spinner = true;
    this.saveAPIService.updateGroups(this.formGroup, this.cliId)
      .subscribe((res: any) => {
        if (res.statusCode === 200) {
          this.formChanged = false;
          this.updated.emit();
          this.response = res
          this.status = res.message
          this.spinner = false;
          this.popup = true;
        }
      }, (error: HttpErrorResponse) => {

        this.response = this.saveAPIService.badError;
        this.status = this.response.message
        this.popup = true;
        this.spinner = false;
      });

  }
  patchUpdateDLT() {

    // this.updated.emit();
    this.accountSvc.clearAllValidators(this.formGroup);
    const userTypeNum = this.formGroup.controls.userType.value;
    let userType: string = "";
    //console.log("user type", userTypeNum);
    if (userTypeNum === 1) { userType = "admin"; }
    this.setValidatorSvc.setValidatorsToDLTFields(this.formGroup, userType);
    this.accountSvc.validateAllFormFields(this.formGroup);

    if (this.formGroup.valid) {
      this.spinner = true;

      this.saveAPIService.updateDLT(this.formGroup, this.cliId, userTypeNum)
        .subscribe((res: any) => {
          if (res.statusCode === 200) {
            this.formChanged = false;
            this.updated.emit();
            this.response = res
            this.status = res.message
            this.spinner = false;
            this.popup = true;
            this.editAcctSvc.dltStatus = "saved";
          }
        }, (error: HttpErrorResponse) => {

          this.response = this.saveAPIService.badError;
          this.status = this.response.message

          this.status = error.message
          this.popup = true;
          this.spinner = false;
        });
    } else {
      //console.log(this.formGroup)
    }


  }
  patchUpdateMsgSettings() {
    this.accountSvc.validateAllFormFields(this.formGroup);

    if (this.formGroup.valid) {
      this.spinner = true;
      this.saveAPIService.updateMsgSettings(this.formGroup, this.cliId)
        .subscribe((res: any) => {
          if (res.statusCode === 200) {
            this.disable = true;
            this.formGroup.markAsPristine();
            //this.updated.emit();
            this.response = res
            this.status = res.message
            this.spinner = false;
            this.popup = true;
          }
        }, (error: HttpErrorResponse) => {
          this.formChanged = true;
          this.response = this.saveAPIService.badError;
          this.status = this.response.message

          this.status = error.message
          this.popup = true;
          this.spinner = false;
        });
    } else {
      //console.log(this.formGroup);
    }
  }
  patchUpdatetwoFA() {
    // this.updated.emit(true);
    this.accountSvc.clearAllValidators(this.formGroup);
    this.spinner = true;
    this.saveAPIService.updateTwoFA(this.formGroup, this.cliId)
      .subscribe((res: any) => {
        if (res.statusCode === 200) {
          this.formChanged = false;
          this.updated.emit();
          this.response = res
          this.status = res.message
          this.spinner = false;
          this.popup = true;
        }
      }, (error: HttpErrorResponse) => {
        this.updated.emit(false);
        this.response = this.saveAPIService.badError;
        this.status = this.response.message

        this.status = error.message
        this.popup = true;
        this.spinner = false;
      })
  }



  patchUpdateAccount() {
    this.accountSvc.clearAllValidators(this.formGroup);
    this.setValidatorSvc.setValidatorsToAcctInfo(this.formGroup);
    this.accountSvc.validateAllFormFields(this.formGroup);
    if (this.formGroup.valid) {
      this.spinner = true;
      this.saveAPIService.updatePersonalInfo(this.formGroup, this.cliId)
        .subscribe((res: any) => {
          if (res.statusCode === 200) {

            this.formGroup.markAsPristine();
            this.response = res
            this.status = res.message
            this.spinner = false;
            this.popup = true;
          }
        }, (error: HttpErrorResponse) => {

          this.response = this.saveAPIService.badError;
          this.status = this.response.message

          this.status = error.message
          this.popup = true;
          this.spinner = false;
        })
    } else {
      // console.log("form is invalid");
    }

  }

}
