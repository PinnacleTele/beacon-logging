import { Component, OnInit } from '@angular/core';
import { FormGroupDirective, FormGroup, ControlContainer } from '@angular/forms';

@Component({
  selector: 'app-contact-email',
  templateUrl: './contact-email.component.html',
  styleUrls: ['./contact-email.component.css']
})
export class ContactEmailComponent implements OnInit {

  constructor(private controlContainer: ControlContainer) { }

  contactEmailFormGroup: any;

  ngOnInit(): void {
    this.contactEmailFormGroup = this.controlContainer.control
  }

  get email() {
    return this.contactEmailFormGroup.controls.email;
  }
  blur() {
    this.email.setValue(this.email.value.trim());
    this.email.updateValueAndValidity();
  }

}
