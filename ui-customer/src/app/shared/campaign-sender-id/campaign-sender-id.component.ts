import { Component, EventEmitter, OnInit, Output } from "@angular/core";
import { ControlContainer, FormBuilder, Validators } from "@angular/forms";
import { CONSTANTS } from "src/app/shared/campaigns.constants";

@Component({
    selector: "app-campaign-sender-id",
    templateUrl: "./campaign-sender-id.component.html",
    styleUrls: ["./campaign-sender-id.component.css"]
})
export class CampaignSenderIdComponent implements OnInit {
    senderIdFormGroup: any;

    itemList: string[] = ["hdfc", "sbi", "kotak", "promo", "test"];

    placeHolder = "Select Senderid";

    senderIdInfoText = CONSTANTS.INFO_TXT.campaignSenderId;

    constructor(private controlContainer: ControlContainer) {}

    ngOnInit(): void {
        this.senderIdFormGroup = this.controlContainer.control;
    }

    itemToBeSelected: string | any = null;

    get senderId() {
        return this.senderIdFormGroup.get("senderId");
    }

    focus() {
        const focus = document.getElementById("senderid") as HTMLImageElement;
        focus.focus();
        focus.scrollIntoView();
    }

    onItemChange(event: any) {
        this.senderIdFormGroup.controls.senderId.setValue(event);
     
    }
}
