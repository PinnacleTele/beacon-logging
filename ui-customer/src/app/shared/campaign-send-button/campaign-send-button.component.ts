import { Component, Input, OnDestroy } from "@angular/core";
import { CampaignsService } from "src/app/campaigns/campaigns.service";
import { UtilityService } from "src/app/core/utility.service";

@Component({
    selector: "app-campaign-send-button",
    templateUrl: "./campaign-send-button.component.html",
    styleUrls: ["./campaign-send-button.component.css"]
})
export class CampaignSendButtonComponent implements OnDestroy {
    @Input() trafficType: any;
    public enable: boolean = false;

    public ZeroBalance = this.campaignService.zeroBalanceCheck.subscribe((data: any) => { this.enable = data })

    public ApiZeroBalance = this.campaignService.zeroBalanceCheckApiLoad.subscribe((data: any) => { this.enable = data })


    @Input() campaignForm: any;

    @Input() campaignType: string;

    campaignNameErr = false;
    campaignNameApiErr = false;
    openPreviewModal = false;

    action = "Send";

    showPreviewModal: any;

    fileOnUpload: boolean;

    fileProgSubscriber = this.utility.fileUploadProgress.subscribe((data: any) => { this.fileOnUpload = data })


    constructor(private campaignService: CampaignsService, private utility: UtilityService) { }



    onSend() {
        this.openPreviewModal = false;
        if(this.campaignForm.message){
            this.campaignForm.template.setErrors(null)
            this.campaignForm.template.updateValueAndValidity();
        }
        if(this.campaignForm.template){
            this.campaignForm.message.setErrors(null)
            this.campaignForm.message.updateValueAndValidity();
        }

        if (this.enable) {
            //do nothing
        } else {
            if (this.campaignType == "CT" && this.campaignService.dropzoneControl) {
                this.campaignForm.controls.template.setValue(this.campaignService.getSelectedTemplateInfo());
                this.campaignService.dropzoneControl.reValidate();
            }
            this.validateAndExtractFormValues();
        }

    }

    validateAndExtractFormValues() {
        this.openPreviewModal = false;
        console.log(this.campaignForm);
        
        if (!this.campaignForm.valid) {

            this.campaignService.validateAllFormFields(this.campaignForm,this.campaignType);
        } else {
            if (this.campaignType === "CT") {
                this.campaignNameErr = this.campaignService.campaignTempErr;
                this.campaignNameApiErr = this.campaignService.campaignTempApiErr;

                if (!this.campaignNameErr && !this.campaignNameApiErr) {
                    this.openPreviewModal = true;
                } else {

                    this.campaignService.focusCampaignName(this.campaignForm);
                }
            }
            else {
                this.openPreviewModal = true;
            }

        }
    }

    onClosePreview() {
        this.openPreviewModal = false;
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
