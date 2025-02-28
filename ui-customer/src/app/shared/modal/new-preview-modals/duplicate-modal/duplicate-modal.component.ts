import { Component, Input, OnInit } from "@angular/core";
import { LocalStorageService } from "src/app/authentication/local-storage.service";
import { CampaignsService } from "src/app/campaigns/campaigns.service";

@Component({
    selector: "app-duplicate-modal",
    templateUrl: "./duplicate-modal.component.html",
    styleUrls: ["./duplicate-modal.component.css"]
})
export class DuplicateModalComponent implements OnInit {
    @Input() removeDuplicates: boolean;

    @Input() vlShortern: boolean;

    @Input() campaignType: string;

    wordToDisplay = "Yes";
   
    toDisplay = "No";
    shortner: any;
    shortners: any;""

    constructor(private campaignSvc: CampaignsService,private localStorageService:LocalStorageService) {}

    ngOnInit(): void {

        console.log('campaignType', this.campaignType);
        let vlshortner1 = this.localStorageService.getLocal('user');
		this.shortner = JSON.parse(vlshortner1);
		this.shortners = this.shortner.vl_shortner;
        
        if (!this.removeDuplicates) {
            this.wordToDisplay = "No";
        } else {
            this.wordToDisplay = "Yes";
        }

        if (!this.vlShortern) {
            this.toDisplay = "No";
        } else {
            this.toDisplay = "Yes";
        }
    }
}
