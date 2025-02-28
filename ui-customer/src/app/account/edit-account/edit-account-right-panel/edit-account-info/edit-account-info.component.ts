import { Component, OnInit } from '@angular/core';
import { ControlContainer, FormBuilder } from '@angular/forms';
import { SetValidationsService } from 'src/app/account/set-validations.service';
import { EditAccountService } from '../../edit-account.service';
import { PersonalInfo } from '../../model/personal-info';

@Component({
  selector: 'app-edit-account-info',
  templateUrl: './edit-account-info.component.html',
  styleUrls: ['./edit-account-info.component.css']
})
export class EditAccountInfoComponent implements OnInit {
   
  formChanged = false;
  //personalInfo : PersonalInfo;

  AcctInfoFormGroup = this.fb.group({
    firstName: ["", ],
    lastName: ["",],
    address: [,],
    company:[,],
    currency:[,],
  });
  constructor(private fb :FormBuilder,
    private editAcctService : EditAccountService,
    private setValidatorSvc : SetValidationsService){}
  
  ngOnInit(): void {
    
    this.editAcctService.setFormCtrlPI(this.AcctInfoFormGroup);
    this.setValidatorSvc.setValidatorsToAcctInfo(this.AcctInfoFormGroup);
    this.AcctInfoFormGroup.valueChanges.subscribe((data:any)=>{
      //  console.log(data,'data on value subscribe');
        
        this.checkIfValueChange(data);
      })
  }
  
  handleUpdate(){
    this.formChanged = false;
  }
  get firstName(){
    return this.AcctInfoFormGroup.controls.firstName;
  }
  get lastName(){
    return this.AcctInfoFormGroup.controls.lastName;
  }
  get company(){
    return this.AcctInfoFormGroup.controls.company;
  }
  get address(){
    return this.AcctInfoFormGroup.controls.address;
  }
 

  checkIfValueChange(changedValue){    
    const previousValue = this.editAcctService.personalInfo;    
    if (previousValue.firstName !== changedValue.firstName ||
      previousValue.lastName !== changedValue.lastName ||
      previousValue.address !== changedValue.address ||
      previousValue.company !== changedValue.company) {        
    } else {      
      this.AcctInfoFormGroup.markAsPristine();
    }

  }
}
