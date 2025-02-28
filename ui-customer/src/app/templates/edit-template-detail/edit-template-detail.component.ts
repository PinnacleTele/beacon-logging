import { Component, EventEmitter, OnInit, Output } from "@angular/core";
import { Router } from "@angular/router";

import { FormGroup, FormBuilder, Validators } from "@angular/forms";
import { maxLengthValidator } from "src/app/campaigns/validators/maxlength-validator";
import { minLengthValidator } from "src/app/campaigns/validators/minlength-validator";
import { CONSTANTS } from "src/app/shared/campaigns.constants";
import { SearchService } from "../search.service";
import { TemplateModel } from "../model/templatemodal";
import { ActivatedRoute } from '@angular/router';
import { TemplateService } from "../Helpers/templates.service";
import { HttpErrorResponse } from "@angular/common/http";
@Component({
    selector: "app-edit-template-detail",
    templateUrl: "./edit-template-detail.component.html",
    styleUrls: ["./edit-template-detail.component.css"]
})
export class EditTemplateDetailComponent implements OnInit {
    updateTemplateForm: FormGroup;

    pattern_validation = CONSTANTS.pattern_validation;

    minLength = CONSTANTS.minLengthTemplateName;

    maxLength = CONSTANTS.maxLengthTemplateName;

    enableCreateButton = false;

    cancelMessage = CONSTANTS.INFO_TXT.templateEditCancelMessage;

   
    Template  : TemplateModel

    ispreview : boolean = false;
    apiError: boolean = false;
    spinner: boolean = false;
    skeleton:boolean = false;

    constructor(
        public search: SearchService,
        public router: Router,
        private fb: FormBuilder,
        private tservice: TemplateService,
        private route: ActivatedRoute    ) {
        

        this.updateTemplateForm = this.fb.group({
            id : "" ,
            templateName: [
                "",
                [
                    Validators.required,
                    minLengthValidator(this.minLength),
                    maxLengthValidator(this.maxLength),
                    Validators.pattern(this.pattern_validation)
                ]
            ]
        });
     }

    templates: any;
    Value : any;
    templateDetail: any = [];
    templateName:string;

    ngOnInit(): void {
  this.subscribeData();
       
}

subscribeData(){
    this.skeleton = true;
  //    let firstparam = this.route.snapshot.paramMap.get('id')
  this.route.queryParams.subscribe(
    params => {
        if (params['templates']) {
            this.ispreview = true;
            this.templates =  params['templates'];
      let data = this.templates;
       this.tservice.getTemplateByGivenId(data)
       .subscribe(
        (res: any) => {
            if (res) {
                this.spinner =false;
                this.skeleton = false;
                this.Value = res;
                    this.templateDetail.push(this.Value);
                       this.updateTemplateForm.controls["id"].setValue(this.Value.id);
                       this.templateName = this.Value.t_name;
                     
                    this.updateTemplateForm.controls["templateName"].setValue(this.Value.t_name);
             
        }
    },
        (error: HttpErrorResponse) => {
            this.apiError = true;
            
        }
      )   
    //    .subscribe((dat) => { 
    //     this.Value = dat;
    //     this.templateDetail.push(this.Value);
    //        this.updateTemplateForm.controls["id"].setValue(this.Value.id);
    //        this.templateName = this.Value.t_name;
         
    //     this.updateTemplateForm.controls["templateName"].setValue(this.Value.t_name);
    // } );
        } else {
            this.ispreview = false;
            this.templates =  params['previewTemplates'];
            let data = this.templates
             this.tservice.getTemplateByGivenId(data)
             .subscribe(
                (res: any) => {
                    if (res) {
                        this.spinner =false;
                        this.skeleton = false;
                        this.Value = res;
                      this.templateDetail.push(this.Value);
                     
                }
            },
                (error: HttpErrorResponse) => {
                    this.skeleton = false;
                    this.apiError = true;
                    
                }
              )              
}
})
}
retry(){
    this.apiError = false;
    this.spinner =true;
    this.subscribeData();

}

enableSave(event:any){
    if (this.Value.t_name != event) {
        this.enableCreateButton =true;
       
    }else{
        this.enableCreateButton =false;
    }
    
}

routing(routhPath:string){
    this.router.navigate([routhPath]);
}
}
