import { HttpErrorResponse } from '@angular/common/http';
import { Component, Input, OnInit, ViewChild } from '@angular/core';
import { DltUploadService } from "src/app/dltfile-upload/dltupload.service";
import { Router } from "@angular/router";
import { openClose } from 'src/app/shared/animation';
import { DltentityidComponent } from '../dltentityid/dltentityid.component';

@Component({
  selector: 'app-save-button',
  templateUrl: './save-button.component.html',
  styleUrls: ['./save-button.component.css'],
  animations: [openClose]
})
export class SaveButtonComponent implements OnInit {

  @Input() title: any;

  @Input() dltFileFormDetail: any;

  @ViewChild(DltentityidComponent, { static: false })
  entityID: DltentityidComponent;

  status: string;


  Responce: { message: string, statusCode: number };

  popup: boolean = false;

  cancelMessage = "Are you sure want to cancel the DLT file upload ?"
  telcoLoading: boolean = false;

  constructor(private route: Router, private dltService: DltUploadService) { }

  ngOnInit(): void {
  }

  save() {


    this.dltService.validateAllFormFields(this.dltFileFormDetail);
    if (this.dltFileFormDetail.valid) {
      this.onSubmit();
      //this.spinner = true;
    }


  }
  cancel() {


  }

  onSubmit() {




    let file = [];
    this.dltFileFormDetail.controls.files.value.files.forEach(element => {
      console.log(element);
      file.push({
        "filename": element.originalname,
        "r_filename": element.r_filename,
        "count": element.total
      })

    });


    let obj = {
      entityid: this.dltFileFormDetail.controls.entityId.value,
      telco: this.dltFileFormDetail.controls.telco.value,
      files: file
    }
    this.telcoLoading = true;
    console.log(obj);
    this.dltService.saveTelco(obj)


      .subscribe(
        (responcedata: any) => {
          this.telcoLoading = false;
          if (
            responcedata.statusCode === 200
          ) {
            this.Responce = responcedata;
            this.status = responcedata.message;


            this.popup = true;
          }
          if (responcedata.statusCode === 400) {
            this.Responce = responcedata;
            this.status = responcedata.message;

            this.popup = true;
          }
        },
        (error: HttpErrorResponse) => {
          this.telcoLoading = false;
          let err = this.dltService.badError;
          this.Responce = err;
          this.status = err.message;
          this.popup = true;
        }
      )


  }

  modalClose(event: boolean) {
    this.popup = false;
  }

  tryAgain(event: any) {
    this.onSubmit();


  }

  modalcontinue(event: boolean) {
    this.route.navigate(["/dlt/uploadlist"]);

  }
}
