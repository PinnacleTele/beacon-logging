import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { GroupsManagementService } from '../groups-management.service';
import { CONSTANTS } from "src/app/shared/campaigns.constants";
import { emailValidator } from 'src/app/campaigns/validators/email-validator';

@Component({
  selector: 'app-group-contact-edit',
  templateUrl: './group-contact-edit.component.html',
  styleUrls: ['./group-contact-edit.component.css']
})
export class GroupContactEditComponent implements OnInit, OnDestroy {

  constructor(private router: Router,
    private fb: FormBuilder,
    private groupService: GroupsManagementService

  ) { }
  ngOnDestroy(): void {
    localStorage.removeItem("editContact")

  }

  spinner: boolean = false;

  details: any = "";

  mobile: any;

  name: any;

  email: any;

  groupName: any;

  cancelMessage = CONSTANTS.INFO_TXT.groupEditCancelMessage;

  group: any

  groupId: any

  ngOnInit(): void {
    this.groupId = JSON.parse(localStorage.getItem("editContact") + "")

    this.groupService.getGroupInfo(this.groupId).subscribe((res) => {
      this.group = res

      this.editContactForm.controls.g_id?.setValue(this.group.id)
      this.editContactForm.controls.g_type?.setValue(this.group.g_type)
    })
    this.details = history.state.data

    if (this.details?.mobile) {


    }
    else {
      this.router.navigate(["groups/groupcontacts"])
    }

    this.mobile = this.details?.mobile;
    this.name = this.details?.name;
    this.email = this.details?.email;
    // this.groupName=history.state.groupName;

    this.editContactForm.controls.mobile?.setValue(this.mobile)
    this.editContactForm.controls.name?.setValue(this.name)
    this.editContactForm.controls.email?.setValue(this.email)
    if (this.group) {


    }



  }


  editContactForm = this.fb.group({

    mobile: [""],
    name: ["", [Validators.maxLength(20), Validators.minLength(3)]],
    email: ["", emailValidator()],
    g_id: ["", Validators.required],
    g_type: ["", Validators.required]
  })


  onSave() {
    if (this.mobileControl.dirty || this.nameControl.dirty || this.emailControl.dirty) {
      this.validateAllFormFields(this.editContactForm);
      //API call
      this.spinner = true;
      setTimeout(() => this.spinner = false, 2000)

    }

  }

  get mobileControl() {
    return this.editContactForm.controls.mobile
  }
  get nameControl() {
    return this.editContactForm.controls.name
  }
  get emailControl() {
    return this.editContactForm.controls.email
  }
  onSubmit() {


  }


  validateAllFormFields(campaignForm: FormGroup) {
    const formGroup = campaignForm;
    Object.keys(formGroup.controls).forEach((field) => {
      const control = formGroup.get(field);
      if (control instanceof FormControl) {
        control.markAsTouched({ onlySelf: true });
      }
    });


  }



}
