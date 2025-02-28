import { Component, Input, OnInit } from "@angular/core";
import { CampaignsService } from "src/app/campaigns/campaigns.service";
import { FileUploadStatistics } from "src/app/campaigns/model/campaign-file-statistics";
import { UtilityService } from "src/app/core/utility.service";
import { CONSTANTS, value} from "src/app/shared/campaigns.constants";

@Component({
    selector: "app-uploaded-files",
    templateUrl: "./uploaded-files.component.html",
    styleUrls: ["./uploaded-files.component.css"]
})
export class UploadedFilesComponent implements OnInit {
    @Input() files: any;

    @Input() removeDuplicates: boolean;

    @Input() vlShortern: boolean;

    @Input() campaignType: any;

    uploadFiles: FileUploadStatistics;

    filesLimit = 0;

    myOptions = value;

    // show files till all filenames length ~ charactersLimit chars
    charactersLimit = 150;

    constructor(
        private campaignSvc: CampaignsService,
        public utility: UtilityService
    ) {}

    ngOnInit(): void {
        if (this.files) {
            
            
            this.uploadFiles = this.campaignSvc.extractFileDetails(this.files);
            
            let length = 0;
            for (const file of this.uploadFiles.uploadedFileNames) {
                
                if (
                    length < this.charactersLimit &&
                    length + file.length <= this.charactersLimit
                ) {
                    length += file.length;
                    this.filesLimit += 1;
                }
            }
        }
    }
}
