import { Component, Input, OnInit } from "@angular/core";
import { CampaignsService } from "src/app/campaigns/campaigns.service";
import { MessageDetails } from "src/app/campaigns/model/generic-campaign-msg-details";

@Component({
    selector: "app-new-preview-modal-message",
    templateUrl: "./new-preview-modal-message.component.html",
    styleUrls: ["./new-preview-modal-message.component.css"]
})
export class NewPreviewModalMessageComponent implements OnInit {
    @Input() message: string;

    @Input() language: string;

    public campaignMsg: MessageDetails;

   @Input() textToDisplay = "";

    constructor(private campaignSvc: CampaignsService) {}

    ngOnInit(): void {
        this.campaignMsg = this.campaignSvc.formulateMessageDetails();

        if (this.language !== "") {
            
            this.textToDisplay = `unicode - ${this.language}`;
        }
    }
}
