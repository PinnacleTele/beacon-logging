import { Component, Input } from "@angular/core";
import { CampaignsService } from "src/app/campaigns/campaigns.service";
import { environment } from "../../../environments/environment";

@Component({
    selector: "app-campaign-save-button",
    templateUrl: "./campaign-save-button.component.html",
    styleUrls: ["./campaign-save-button.component.css"]
})
export class CampaignSaveButtonComponent {
    @Input() campaignForm: any;

    @Input() campaignType: string;

    openPreviewModal = false;

    action = "save";

    constructor(private campaignService: CampaignsService) {}

    get name() {
        return this.campaignForm.value.name;
    }

    onSave() {
        this.action = "Save";
        if (
            this.campaignForm.value.name &&
            this.campaignForm.get("name").valid
        ) {
            this.openPreviewModal = true;
        } else {
            // this.quickSMSForm.controls['name'].markAsTouched({ onlySelf: true });
        }
    }

    onClosePreview() {
        this.openPreviewModal = false;
    }
}
