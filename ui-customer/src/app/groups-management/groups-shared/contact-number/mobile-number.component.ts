import { Component, Input, OnInit } from '@angular/core';
import { ControlContainer, FormGroup } from '@angular/forms';
import { CONSTANTS } from 'src/app/shared/campaigns.constants';

@Component({
  selector: 'app-mobile-number',
  templateUrl: './mobile-number.component.html',
  styleUrls: ['./mobile-number.component.css']
})
export class MobileNumberComponent implements OnInit {

  constructor(public controlContainer: ControlContainer) { }

  public contactMobileFormGroup: any;

  @Input() enable: boolean = false;

  minLength = CONSTANTS.generalMobileMinLength;
  maxLength = CONSTANTS.generalMobileMaxLength;

  ngOnInit(): void {
    this.contactMobileFormGroup = this.controlContainer.control;


  }

  get mobile() {
    return this.contactMobileFormGroup.controls.mobile;
  }
  blur() {
    this.mobile.setValue(this.mobile.value.trim());
    this.mobile.updateValueAndValidity();
  }

}
