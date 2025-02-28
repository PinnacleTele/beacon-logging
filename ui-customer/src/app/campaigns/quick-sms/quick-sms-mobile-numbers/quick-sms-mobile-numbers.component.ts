import { Component, Input, OnDestroy, OnInit } from "@angular/core";
import { ControlContainer } from "@angular/forms";
import { CONSTANTS } from "src/app/shared/campaigns.constants";
import { QuickSMSCampaignService } from "../service/quick-sms-campaigns-service";
import { CampaignsService } from "../../campaigns.service";

@Component({
    selector: "app-quick-sms-mobile-numbers",
    templateUrl: "./quick-sms-mobile-numbers.component.html",
    styleUrls: ["./quick-sms-mobile-numbers.component.css"]
})
export class QuickSmsMobileNumbersComponent implements OnInit, OnDestroy {
    public mobileInfoTxt = CONSTANTS.INFO_TXT.cqMobileNumbersPart1 + CONSTANTS.MAX_MOBILE_COUNT +
    CONSTANTS.INFO_TXT.cqMobileNumbersPart2;
    invalidInfoTxt = CONSTANTS.INFO_TXT.invalidInfoTxt;
    public mobileCountError = CONSTANTS.ERROR_DISPLAY.mobileCountExceeded;
    public invalidCountError = CONSTANTS.ERROR_DISPLAY.invalidCountError;
    

    public maxCount = CONSTANTS.MAX_MOBILE_COUNT;

    maxMobileCount = CONSTANTS.MAX_MOBILE_COUNT;

    noValidCountDesc = CONSTANTS.ERROR_DISPLAY.NO_VALID_COUNT;

    openClearModal = false;

    source = "mobile numbers";

    // clickObservable: Observable<Event> = fromEvent(<HTMLElement>document.getElementById('clear'),'click');
    mobileNumbersFormGroup: any;

    mobileNumbersCount = 0;

    uniqueCount = 0;

    inValidCount = 0;

    removeDuplicateChecked = true;
    @Input() trafficType: any;

    constructor(
        private quickSMSCampaignService: QuickSMSCampaignService,
        private campaignService: CampaignsService,
        public controlContainer: ControlContainer
    ) {}

    ngOnInit(): void {
        this.mobileNumbersFormGroup = this.controlContainer.control;
    }

    openModal() {
        this.openClearModal = true;
    }

    get mobileNumbers() {
        return this.mobileNumbersFormGroup.controls.mobileNumbers;
    }

    // onRemoveDuplicatesChange(event: any) {
    //     this.removeDuplicateChecked = event.target.checked;
    //     this.campaignService.populateDuplicateChk(this.removeDuplicateChecked);
    // }

    clearModalResponse(response: string) {
        if (response === "clear") {
            this.mobileNumbersFormGroup.controls.mobileNumbers.setValue("");
            this.mobileNumbersCount = 0;
            this.uniqueCount = 0;
            this.inValidCount = 0;
            this.openClearModal = false;
        }
        if (response === "close") {
            this.openClearModal = false;
        }
    }
    checkValidCount(event: any) {
        const textAreaContent = event.target.value.trim();

        this.quickSMSCampaignService.splitIntoMobileNumbers(textAreaContent,this.trafficType);
        this.mobileNumbersCount = this.quickSMSCampaignService.getValidMobileNumbersCount();
        if (this.mobileNumbersCount === 0) {
            this.mobileNumbersFormGroup.controls.mobileNumbers.setErrors({
                noValidCountError : true
            });
        }
    }
    mobileNumbersdetails(event: any) {
        const textAreaContent = event.target.value.trim();

        this.quickSMSCampaignService.splitIntoMobileNumbers(textAreaContent, this.trafficType);
        this.mobileNumbersCount = this.quickSMSCampaignService.getValidMobileNumbersCount();
        
        if (this.mobileNumbersCount > this.maxMobileCount) {
            this.mobileNumbersFormGroup.controls.mobileNumbers.setErrors({
                maxMobileCountErr : true
            });
        }

        this.uniqueCount = this.quickSMSCampaignService.getUniqueMobileNumbersCount();
        this.inValidCount = this.quickSMSCampaignService.getInvalidMobileNumbersCount();
        if (this.inValidCount > 0) {
            this.mobileNumbersFormGroup.controls.mobileNumbers.setErrors({
                inValidNumb : true
            });
        }
    }

    focus() {
        const focus = document.getElementById("mobile") as HTMLImageElement;
        focus.focus();
        // focus.scrollIntoView();
    }
    ngOnDestroy() {
        this.quickSMSCampaignService.splitIntoMobileNumbers("", "");
    }
}
