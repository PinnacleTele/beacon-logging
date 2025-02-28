import { HttpErrorResponse } from "@angular/common/http";
import { Component, Input, OnInit, OnChanges } from "@angular/core";
import { Router } from "@angular/router";
import { TemplateCampaignService } from "src/app/campaigns/campaign-template/service/template-campaign.service";
import { TemplateService } from "../Helpers/templates.service";
import { openClose } from "src/app/shared/animation";
@Component({
    selector: "app-template-create-button",
    templateUrl: "./template-create-button.component.html",
    styleUrls: ["./template-create-button.component.css"],
    animations: [openClose]
})
export class TemplateCreateButtonComponent implements OnInit, OnChanges {
    status: string;

    spinner = false;

    popup = false;

    @Input() newTemplateForm: any;

    @Input() title: any;


    @Input() enableButton: any;

    constructor(
      
        private templCampaignSvc: TemplateCampaignService,
        private router: Router, private tService: TemplateService
    ) { }

    ngOnInit(): void {


    }

    ngOnChanges(): void {
        let enable = this.enableButton
    }

    onCreate() {
console.log(this.newTemplateForm);
        this.tService.validateTemplateFormFields(this.newTemplateForm);
        this.onSubmit();


    }


    onSubmit() {
        if (this.title === 'Create') {
            this.newTemplate();
            // this.spinner = true;

        } else {

            this.updateTemplate();
        }

    }

    unicode: number;

    Responce: { message: string, statusCode: number }

    newTemplate() {

        const templateErr = this.templCampaignSvc.templateErr;
        if (this.newTemplateForm.valid && !templateErr) {
            if (this.newTemplateForm.controls.patternType.value == 0) {
                this.unicode = 0;

            } else {
                this.unicode = 1;
            }
            const staticCtrlVal = this.newTemplateForm.controls.isStatic.value;
   
            let data = {
                t_name: this.newTemplateForm.controls.templateName.value,
                t_type: this.newTemplateForm.controls.basedOn.value,
                t_mobile_column: this.newTemplateForm.controls.mobileColumn.value,
                dlt_entity_id: this.newTemplateForm.controls.entityId.value,
                dlt_template_id: this.newTemplateForm.controls.dlt.value,
                pattern_type: this.newTemplateForm.controls.patternType.value,
                t_content: this.newTemplateForm.controls.newmessage.value,
                is_static : +this.newTemplateForm.controls.isStatic.value,
                is_unicode: this.unicode
            }

            this.spinner = true;
            this.tService
                .createTemplate(data)
                .subscribe(
                    (res: any) => {

                        if (
                            res.statusCode === 201
                        ) {
                            this.Responce = res
                            this.status = res.message
                            this.popup = true;
                            this.spinner = false;
                        }
                        if (res.statusCode === -410) {
                            this.Responce = res
                            this.status = res.message
                            this.spinner = false;
                            this.popup = true;
                        }
                    }
                    ,
                    (error: HttpErrorResponse) => {
                        let err = this.tService.badError
                        this.Responce = err;
                        this.status = err.message
                        this.popup = true;
                        this.spinner = false;
                    }
                );
        }
    }



    updateTemplate() {
        const templateErr = this.templCampaignSvc.templateErr;
        if (this.newTemplateForm.valid && !templateErr) {
            this.tService
                .templateUpdate(this.newTemplateForm.value)
                .subscribe(
                    (res: any) => {

                        if (
                            res.statusCode === 200

                        ) {
                            this.Responce = res
                            this.status = res.message
                            this.popup = true;
                            this.spinner = false;
                        } else if (
                            res.statusCode > 200

                        ) {
                            this.Responce = res
                            this.status = res.message
                            this.popup = true;
                            this.spinner = false;
                        }
                    },
                    (error: HttpErrorResponse) => {
                        let err = this.tService.badError;
                        this.Responce = err;
                        this.status = err.message;
                        this.popup = true;
                        this.spinner = false;
                        // this.newGroupForm.setErrors({'api failure':true});
                    }
                );
        }
    }



    modalClose(event: boolean) {
        this.popup = false;
        this.spinner = false;
    }

    tryAgain(event: any) {
        let setError = {
            message: "Please Wait Some Time",
            statusCode: 100,
            error: " HTTP Server Timed Out"
        }

        this.Responce = setError;
        this.status = setError.message;
        this.popup = true;
        this.spinner = true;
        this.onSubmit();

    }

    modalcontinue(event: boolean) {
        this.popup = false;
        this.router.navigate(["/templates"]);
    }
}
