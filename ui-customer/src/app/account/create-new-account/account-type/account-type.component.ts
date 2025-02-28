import { Component, OnInit } from '@angular/core';
import { ControlContainer, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { LocalStorageService } from 'src/app/authentication/local-storage.service';
import { CommonService } from 'src/app/shared/commonservice';
import { SetValidationsService } from '../../set-validations.service';

@Component({
  selector: 'app-account-type',
  templateUrl: './account-type.component.html',
  styleUrls: ['./account-type.component.css']
})
export class AccountTypeComponent implements OnInit {
  
  
  page1FormGroup : any;
  loggedInuserType : number;
  constructor(private fb: FormBuilder, 
    private localStorageService : LocalStorageService,
    private setValidatorService : SetValidationsService,
    private commonService:CommonService,
    private parentControl: ControlContainer) {
  }

  ngOnInit(): void {
    this.page1FormGroup = this.parentControl.control;
    const user = this.localStorageService.getLocal('user');
    
    // let userObj = null;
    // if(user){
    // }
    let userObj = this.commonService.tokenDecoder();
    this.loggedInuserType = userObj.user_type;
    const acctTypeCtrlVal = this.accountType.value;
    
    if(acctTypeCtrlVal == null){
      if(this.loggedInuserType){
        this.accountType.setValue("user");
     }else{
       this.accountType.setValue("admin");
     }
    }else{
      this.accountType.setValue(acctTypeCtrlVal);
    }
    
    
    
  }
  ngAfterViewInit() {
    if(document.getElementById("acctTypeId")){
      document.getElementById("acctTypeId").focus();
    }
    
  }
  get accountType(){
    return this.page1FormGroup.controls.accountType;
  }
    
  onClickAcctType(acctType : string){
    this.page1FormGroup.reset();
    this.setValidatorService.apiCountryRates = undefined;
    this.page1FormGroup.controls.accountType.setValue(acctType);
    
      
  }

}
