import { Component, OnInit } from '@angular/core';
import { ControlContainer } from '@angular/forms';
import { EditAccountService } from '../../edit-account.service';

@Component({
  selector: 'app-edit-signin-securities',
  templateUrl: './edit-signin-securities.component.html',
  styleUrls: ['./edit-signin-securities.component.css']
})
export class EditSigninSecuritiesComponent implements OnInit {
  signInFormGroup : any;
  formChanged = false;
  toggle :boolean;
  constructor( private controlContainer: ControlContainer,
    private editAcctSvc : EditAccountService) {
  }

  ngOnInit(): void {
 
    this.signInFormGroup =  this.controlContainer.control;
    this.editAcctSvc.setFormCtrlTwoFA(this.signInFormGroup);
    this.toggle = this.signInFormGroup.controls.twofa.value;
  }
 
  handleUpdate(){
    this.formChanged = false;
    this.toggle = this.signInFormGroup.controls.twofa.value;
  }
  onClickToggle(event : any){
    this.formChanged = true;
    this.toggle = !this.toggle;
    this.name.setValue(this.toggle);
    
  }
  get name() {
    return this.signInFormGroup.controls.twofa;
  }
}
