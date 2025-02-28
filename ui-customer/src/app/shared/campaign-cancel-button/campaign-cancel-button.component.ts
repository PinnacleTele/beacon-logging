import { Component, Input } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";

@Component({
    selector: "app-campaign-cancel-button",
    templateUrl: "./campaign-cancel-button.component.html",
    styleUrls: ["./campaign-cancel-button.component.css"]
   
})
export class CampaignCancelButtonComponent {
    @Input() cancelMessage: string;

    @Input() routePath:string

    openCancelModal = false;

    constructor(private route: ActivatedRoute, private router: Router) {}

    onCancel() {
        this.openCancelModal = true;
    }

    onCancelPreviewResp(response: string) {
        if (response === "cancel") {
            this.openCancelModal = false;
            this.router.navigate([this.routePath], { relativeTo: this.route });
        }
        if (response === "close") {
            this.openCancelModal = false;
        }
    }
}
