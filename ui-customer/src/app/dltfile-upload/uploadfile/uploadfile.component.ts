import { FormBuilder,FormControl ,Validators ,FormGroup} from "@angular/forms";
import { AfterViewChecked, Component, OnInit, ViewChild } from '@angular/core';
import { CONSTANTS } from 'src/app/shared/campaigns.constants';
import { FileUploaderComponent } from 'src/app/shared/file-uploader/file-uploader.component';
import { DltUploadService } from "../dltupload.service";
import { DltentityidComponent } from "../dltentityid/dltentityid.component";

@Component({
  selector: 'app-uploadfile',
  templateUrl: './uploadfile.component.html',
  styleUrls: ['./uploadfile.component.css']
})
export class UploadfileComponent implements OnInit, AfterViewChecked {

  fileContentOrder = CONSTANTS.DLT_FILE_CONTENT;

  telcoValue: any;

  // pattern_validation : "^[+]?[0-9]{7,15}$";

  pattern_validation = "/[a-z]/i";

  minLength = CONSTANTS.minLengthCampaignName;

  maxLength = CONSTANTS.maxLengthCampaignName;

  constructor(private fb: FormBuilder, private dltService:DltUploadService) { }

  ngAfterViewChecked(): void {
     this.dltService.setDropzoneControl(this.fileupload);
     this.dltService.setentityIDControl(this.entityID);
  }

  dltFileUploadForm: FormGroup;

  @ViewChild(FileUploaderComponent, { static: false }) 
  fileupload: FileUploaderComponent;
  
  @ViewChild(DltentityidComponent, { static: false })
  entityID: DltentityidComponent;

  ngOnInit(): void {
    this.telcoSubscribe();


    this.dltFileUploadForm = this.fb.group({
      entityId: [null, Validators.required,],
      telco: [],
      files: [, Validators.required],
    })
  }

  telcoList: any[] = []


  telcoSubscribe() {
    this.dltService.getTelco().subscribe((data: any) => {
      this.telcoList = data;
      this.telcoValue = data[0].telco;
      this.dltService.telcoEmit.next(data[0].telco)
   //   localStorage.setItem("telco",data[0].telco)
      this.dltFileUploadForm.controls.telco.setValue(this.telcoValue);
    })
  }

  indexStyle: number = 0;

  indexValue(i, value) {
    if (this.indexStyle != i) {
      this.indexStyle = i;
      this.dltFileUploadForm.controls.telco.setValue(value);
      this.telcoValue = value;
      this.dltService.telcoEmit.next(value);
  
      if (this.dltFileUploadForm.controls.files.touched) {  
        this.fileupload.resetTheDropZone();
        this.dltFileUploadForm.controls.files.reset();
      }
      if(this.dltFileUploadForm.controls.telco.value == 'Custom'){
        
        this.dltFileUploadForm.controls.entityId.setValue(null);
        this.dltFileUploadForm.controls.entityId.clearValidators();
        this.dltFileUploadForm.controls.entityId.updateValueAndValidity();
      }
    }
  

  }
  receivedEntityId(event) {

  }

  getFileUploadSectionData(event) {
    
   if (event.files.length > 0) {
      this.dltFileUploadForm.controls.files.setValue(event)
    }
    else {
      this.dltFileUploadForm.controls.files.reset();
      
    }
    this.dltFileUploadForm.controls.files.markAsTouched();
  }
  save() {
    

  }
  cancel() {
    

  }
  get files(){
    return this.dltFileUploadForm.controls.files
  }
  retryTelcos(){
    this.telcoSubscribe();
  }
}
