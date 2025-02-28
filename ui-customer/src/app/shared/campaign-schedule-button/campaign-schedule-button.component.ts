import { Component, Input, ViewChild, ElementRef, OnDestroy } from "@angular/core";
import { CampaignsService } from "src/app/campaigns/campaigns.service";
import { DataSharingService } from "src/app/core/data-sharing.service";
import { UtilityService } from "src/app/core/utility.service";
@Component({
    selector: "app-campaign-schedule-button",
    templateUrl: "./campaign-schedule-button.component.html",
    styleUrls: ["./campaign-schedule-button.component.css"]
})
export class CampaignScheduleButtonComponent implements OnDestroy {
    action = "schedule";

    public enable: boolean = false;

    public ZeroBalance = this.campaignService.zeroBalanceCheck.subscribe((data: any) => { this.enable = data })

    public ApiZeroBalance = this.campaignService.zeroBalanceCheckApiLoad.subscribe((data: any) => { this.enable = data })

    @Input() campaignForm: any;

    @Input() campaignType: string;

    openPreviewModal = false;

    schedule: boolean;

    fileOnUpload: boolean;

    fileProgSubscriber = this.utility.fileUploadProgress.subscribe((data: any) => { this.fileOnUpload = data })



    constructor(private campaignService: CampaignsService,
        private dataSharingService: DataSharingService,
        private utility: UtilityService) { }



    onSchedule() {
        this.action = "Schedule";
        if (this.enable) {
            //do nothing
        } else {
            if (this.campaignType == "CT" && this.campaignService.dropzoneControl) {
                this.campaignForm.controls.template.setValue(
                    this.campaignService.getSelectedTemplateInfo()
                );
                this.campaignService.dropzoneControl.reValidate();
            }
            this.validateAndExtractFormValues();
        }
    }

    validateAndExtractFormValues() {
        if (!this.campaignForm.valid) {

            this.openPreviewModal = true;
            // this.schedule = true;
            this.campaignService.validateAllFormFields(this.campaignForm,this.campaignType);
        } else {
            this.openPreviewModal = true;
            this.schedule = true;
        }
    }

    onClosePreview() {
        this.openPreviewModal = false;

    }

    closePreview(event: any) {
        this.schedule = false;
        this.dataSharingService.scheduledTimes.clear()

    }

    ngOnDestroy(): void {

        if (this.fileProgSubscriber) {

            this.fileProgSubscriber.unsubscribe();
        }
        if (this.ApiZeroBalance) {

            this.ApiZeroBalance.unsubscribe();
        }
        if (this.ZeroBalance) {

            this.ZeroBalance.unsubscribe();
        }

    }

}
