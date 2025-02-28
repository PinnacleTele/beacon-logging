import { Component, OnInit } from "@angular/core";
import { ControlContainer } from "@angular/forms";

import { TemplateCampaignService } from "src/app/campaigns/campaign-template/service/template-campaign.service";
import { CONSTANTS } from "../../campaigns.constants";

@Component({
    selector: "app-select-language",
    templateUrl: "./select-language.component.html",
    styleUrls: ["./select-language.component.css"]
})
export class SelectLanguageComponent implements OnInit {
    selectLangFormGroup: any;

    itemList: string[] = ["Tamil", "Malayalam", "Telugu", "Hindi", "Arabic"];

    placeHolder = "Select Language";

    public campaignLangInfoTxt = CONSTANTS.INFO_TXT.campaignLanguage;

    constructor(
        private templateSvc: TemplateCampaignService,
        private controlContainer: ControlContainer
    ) {}

    ngOnInit(): void {
        this.selectLangFormGroup = this.controlContainer.control;
        const prevValue = this.templateSvc.getNewTemplate();
        if (prevValue.t_lang !== "") {
            this.selectLangFormGroup.controls.language.setValue(
                prevValue.t_lang
            );
        }
    }

    get language() {
        return this.selectLangFormGroup.controls.language;
    }

    changeLanguage(event: any) {
        
        this.templateSvc.populateNewTemplateLanguage(event);
        this.selectLangFormGroup.controls.language.setValue(event);
        // this.itemToBeSelected = event;
    }
}
