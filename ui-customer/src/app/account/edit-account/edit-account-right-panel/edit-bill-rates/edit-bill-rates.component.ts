import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { ControlContainer, FormBuilder } from '@angular/forms';
import { SetValidationsService } from 'src/app/account/set-validations.service';
import { EditAccountService } from '../../edit-account.service';

@Component({
  selector: 'app-edit-bill-rates',
  templateUrl: './edit-bill-rates.component.html',
  styleUrls: ['./edit-bill-rates.component.css']
})
export class EditBillRatesComponent implements OnInit ,OnDestroy{
  @Input() Editable = false;

  @Input() DisableButton :any = false ;

   DisableSpinner:any = false;

   newchangeDetected :boolean = false;

  public disableSubscibtion = this.setValidatorSvc.disableButtonToBillRate.subscribe((data:any)=>this.DisableButton = data);
  
  public disablespinnerSubscibtion = this.setValidatorSvc.disableButtonSpinner.subscribe((data:any)=>this.DisableSpinner = data);

  ratesFormGroup = this.fb.group({
    DLTRate: [,],
    SMSRate: [,],
  });

  origSMSRate :any
  origDLTRate :any
  constructor(private fb: FormBuilder,
    private editAcctService: EditAccountService,
    private setValidatorSvc: SetValidationsService) {
     
     }
  ngOnDestroy(): void {
   if (this.disableSubscibtion  ) {
     this.disableSubscibtion.unsubscribe();
   }
   if (this.disablespinnerSubscibtion  ) {
    this.disablespinnerSubscibtion.unsubscribe();
  }
  }

  ngOnInit(): void {
  
   // this.setValidatorSvc.populateBillRates(this.ratesFormGroup);
    this.editAcctService.setWalletRates(this.ratesFormGroup);
   this.origSMSRate = this.ratesFormGroup.controls.SMSRate.value;
     this.origDLTRate = this.ratesFormGroup.controls.DLTRate.value;
    
    this.setValidatorSvc.setValidatorsToWalletRates(this.ratesFormGroup);
    
    
  }

  retryToGetBillRate(){
    this.setValidatorSvc.populateBillRates(this.ratesFormGroup);
  }

  checkSmsRate(event){
    this.checkIfValueChange();
  }

  checkDltRate(event){
    this.checkIfValueChange();
  }

  checkIfValueChange(){    
    const obj = this.editAcctService.getOldWalletRates()
    if (obj.dltrate != this.dltRate ||
      obj.smsrate != this.smsRate ) {    
          
        this.newchangeDetected = true;
    } else {      
      
      this.newchangeDetected = false;
    }
    
  }

  get smsRate(){
    return this.ratesFormGroup.controls.SMSRate.value;
  }
  get dltRate(){
    return this.ratesFormGroup.controls.DLTRate.value;
  }

}