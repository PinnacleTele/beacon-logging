import { Component, OnInit } from '@angular/core';
import { ControlContainer } from '@angular/forms';

@Component({
  selector: 'app-contact-name',
  templateUrl: './contact-name.component.html',
  styleUrls: ['./contact-name.component.css']
})
export class ContactNameComponent implements OnInit {
  public campaignNameFormGroup: any;

  constructor(public controlContainer: ControlContainer) { }

  maximumLength = 20

  minimumLength = 3

  ngOnInit(): void {

    this.campaignNameFormGroup = this.controlContainer.control;

  }

  get name() {

    return this.campaignNameFormGroup.controls.name;

  }
  onBlur(event) {
  
    // let editTname = (this.editTemplateName).toLowerCase().trim();
    let cname = (event.target.value as string).trim();
    this.name.setValue(cname);
    // console.log(this.campaignNameFormGroup);

  }
}
