import { Component, Input, OnInit } from "@angular/core";
import { MobileCountStatistics } from "src/app/campaigns/model/generic-campaign-mobile-statistics";
import { CONSTANTS } from "src/app/shared/campaigns.constants";
import { QuickSMSCampaignService } from "src/app/campaigns/quick-sms/service/quick-sms-campaigns-service";

@Component({
    selector: "app-new-preview-modal-mobile-numbers",
    templateUrl: "./new-preview-modal-mobile-numbers.component.html",
    styleUrls: ["./new-preview-modal-mobile-numbers.component.css"]
})
export class NewPreviewModalMobileNumbersComponent implements OnInit {
    @Input() mobileNumbersFormValue: string;

    @Input() removeDuplicates: boolean;

    @Input() vlShortern: boolean;
    @Input() trafficType: any;


    // limit_mobiles_preview = CONSTANTS.LIMIT_DISPLAY_MOBILES_PREVIEW;
    totalMaxDigits = CONSTANTS.MAX_DIGITS_COUNT;

    optimalDomesticMaxLength = CONSTANTS.DOMESTIC_MAX_LENGTH;

    public mobileStatistics: MobileCountStatistics;

    // userFedMobileNumbers: MobileCountStatistics["mobileNumbers"];
    toDisplayMobileNumbers: MobileCountStatistics["mobileNumbers"] = [];

    valMobileNumbers: any [] = [];


    toDisplayInvalidNumbers: MobileCountStatistics["invalidNumbers"] = [];

    toDisplayUniqueNumbers: MobileCountStatistics["uniqueNumbers"] = [];

    specificMobileNumbers: string[] = []; // which set of numbers to show in the div

    moreValidNumbers: number;

    moreInvalidNumbers: number;

    moreUniqueNumbers: number;

    toShowInvalidNumbers = false; // add on click

    toShowValidNumbers = false;

    toShowUniqueNumbers = true;

    specificMoreNumbers = 0;

    duplicateChk = true;

    constructor(private quickSMSCampaignSvc: QuickSMSCampaignService) {}

    ngOnInit(): void {
        this.quickSMSCampaignSvc.splitIntoMobileNumbers(
            this.mobileNumbersFormValue,this.trafficType
        );
        this.mobileStatistics = this.quickSMSCampaignSvc.getMobileCountStatistics();

        const setOfMobileNumbers = this.mobileStatistics.mobileNumbers;
        this.logicallyDisplayinPreview("valid", setOfMobileNumbers);
        if (this.mobileStatistics?.invalidNumbers) {
            const setOfInvalidNumbers = this.mobileStatistics.invalidNumbers;
            if (setOfInvalidNumbers.length > 0) {
                this.logicallyDisplayinPreview("invalid", setOfInvalidNumbers);
            }
        }
        if (this.mobileStatistics?.uniqueNumbers) {
            const setOfUniqueNumbers = this.mobileStatistics.uniqueNumbers;

            if (setOfUniqueNumbers.length > 0) {
                this.logicallyDisplayinPreview("unique", setOfUniqueNumbers);
            }
        }
        this.specificMobileNumbers = this.toDisplayUniqueNumbers;
        this.specificMoreNumbers = this.moreUniqueNumbers;
    }

    logicallyDisplayinPreview(valid: string, setOfMobileNumbers: string[]) {
        // const userFedMobileTotalDigits = [...setOfNumbers].toString().replace(',','').length;
        let displayedMobileDigits = 0;
        const userFedMobileNumbers = setOfMobileNumbers;
        for (let i = 0; i < userFedMobileNumbers.length; i++) {
            displayedMobileDigits += userFedMobileNumbers[i].length;
            // second if condition is added to decide whether to display the
            // mobile number if it is the last number or to add +more counts
            if (
                displayedMobileDigits <= this.totalMaxDigits ||
                // ( this.userFedMobileTotalDigits-this.displayedMobileDigits < this.optimalDomesticMaxLength) ) {
                i === userFedMobileNumbers.length - 1
            ) {
                if (valid === "valid") {
                    this.toDisplayMobileNumbers.push( userFedMobileNumbers[i]);
                    this.valMobileNumbers.push( parseInt(userFedMobileNumbers[i]))
                   
                } else if (
                    valid === "invalid" &&
                    this.toDisplayInvalidNumbers
                ) {
                    this.toDisplayInvalidNumbers.push(userFedMobileNumbers[i]);
                } else if (valid === "unique" && this.toDisplayUniqueNumbers) {
                    this.toDisplayUniqueNumbers.push(userFedMobileNumbers[i]);
                }
            } else if (valid === "valid") {
                this.moreValidNumbers = userFedMobileNumbers.length - i;
                break;
            } else if (valid === "invalid") {
                this.moreInvalidNumbers = userFedMobileNumbers.length - i;
                break;
            } else if (valid === "unique") {
                this.moreUniqueNumbers = userFedMobileNumbers.length - i;
                break;
            }
        }
        this.quickSMSCampaignSvc.setValidMobileNumbers(this.valMobileNumbers)
    }

    onClickValid() {
        this.toShowValidNumbers = true;
        this.toShowInvalidNumbers = false;
        this.toShowUniqueNumbers = false;
        // this.whichMobileNumbers = "valid";
        this.specificMobileNumbers = this.toDisplayMobileNumbers;
        this.specificMoreNumbers = this.moreValidNumbers;
    }

    onClickInvalid() {
        this.toShowValidNumbers = false;
        this.toShowInvalidNumbers = true;
        this.toShowUniqueNumbers = false;
        if (this.toDisplayInvalidNumbers) {
            this.specificMobileNumbers = this.toDisplayInvalidNumbers;
            this.specificMoreNumbers = this.moreInvalidNumbers;
        }
    }

    onClickUnique() {
        this.toShowValidNumbers = false;
        this.toShowInvalidNumbers = false;
        this.toShowUniqueNumbers = true;
        if (this.toDisplayUniqueNumbers) {
            this.specificMobileNumbers = this.toDisplayUniqueNumbers;
            this.specificMoreNumbers = this.moreUniqueNumbers;
        }
    }
}
