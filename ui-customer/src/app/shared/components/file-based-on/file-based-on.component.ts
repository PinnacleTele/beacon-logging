import { Component, OnInit } from "@angular/core";
import { ControlContainer } from "@angular/forms";
import { Subscription } from "rxjs";
import { TemplateCampaignService } from "src/app/campaigns/campaign-template/service/template-campaign.service";

@Component({
    selector: "app-file-based-on",
    templateUrl: "./file-based-on.component.html",
    styleUrls: ["./file-based-on.component.css"]
})
export class FileBasedOnComponent implements OnInit {
    fileBasedOnFormGroup: any;

    basedOn = "column";

    subscriptionFile: Subscription;

    constructor(
        private templateSvc: TemplateCampaignService,
        private controlContainer: ControlContainer
    ) {}

    ngOnInit() {
        this.templateSvc.sendBasedOn(this.basedOn);
        this.fileBasedOnFormGroup = this.controlContainer.control;
        const prevValue = this.templateSvc.getNewTemplate();

            this.fileBasedOnFormGroup.controls.basedOn.setValue(this.basedOn);
        
    }

    toggleColumnIndex(basedOn: any) {
            
        this.basedOn = basedOn;
        this.templateSvc.populateNewTemplateBasedOn(basedOn);
        this.templateSvc.sendBasedOn(this.basedOn);
        
    }
}
