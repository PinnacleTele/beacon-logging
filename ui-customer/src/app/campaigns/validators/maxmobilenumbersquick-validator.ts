import { AbstractControl, ValidationErrors, ValidatorFn } from "@angular/forms";
import { QuickSMSCampaignService } from "../quick-sms/service/quick-sms-campaigns-service";

export function maxAllowedMobileNumbers(
    quickSMSCampaignService: QuickSMSCampaignService
): ValidatorFn {
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    return (control: AbstractControl): ValidationErrors | null => {
        const mobileNumbersCount: number = quickSMSCampaignService.getMobileNumbersCount();
        let maxValid = false;

        if (mobileNumbersCount > 4) {
            maxValid = true;
        }
        return maxValid ? { maxMobileCountErr: true } : null;
    };
}
