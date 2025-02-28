import { FileUploadStatistics } from "./campaign-file-statistics";
import { Group } from "./campaign-group-model";
import { MobileCountStatistics } from "./generic-campaign-mobile-statistics";
import { MessageDetails } from "./generic-campaign-msg-details";

export class GenericCampaign {
    constructor(
        public campaignName: string,
        public campaignType: string,
        public duplicateChk: boolean,
        public message: MessageDetails,
        public senderId: string,
        public action: string,
        public language?: string,
        public mobileNumbersStatistics?: MobileCountStatistics,
        public files?: FileUploadStatistics,
        public addedGroups?: Group[],
        public excludedGroups?: Group[]
    ) {}
}
