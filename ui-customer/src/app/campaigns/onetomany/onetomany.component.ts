import { HttpErrorResponse } from "@angular/common/http";
import {
    AfterViewChecked,
    Component,
    OnInit,
    ViewChild,
    OnDestroy,
    ElementRef,
    Renderer2
} from "@angular/core";
import { FormBuilder, Validators } from "@angular/forms";
import { DataSharingService } from "src/app/core/data-sharing.service";
import { FileUploaderComponent } from "src/app/shared/file-uploader/file-uploader.component";
import { SingleSelectDropdownComponent } from "src/app/shared/single-select-dropdown/single-select-dropdown.component";
import { environment } from "src/environments/environment";
import { NgSelectComponent } from "@ng-select/ng-select";
import { CampaignsService } from "../campaigns.service";
import { maxLengthValidator } from "../validators/maxlength-validator";
import { minLengthValidator } from "../validators/minlength-validator";
import { CONSTANTS } from "../../shared/campaigns.constants";
import { Templates } from "src/app/shared/model/templates.model";
import { CampaignSenderIdComponent } from "../campaign-sender-id/campaign-sender-id.component";
import { UtilityService } from "src/app/core/utility.service";
import { CampaignNameComponent } from "../campaign-name/campaign-name.component";
import { EntityIdComponent } from "src/app/shared/entity-id/entity-id.component";
import { CampaignMessageComponent } from "src/app/campaigns/campaign-message/campaign-message.component";
import { ActivatedRoute, Router } from "@angular/router";
import { IntlSendersComponent } from "../intl-senders/intl-senders.component";

@Component({
    selector: "app-onetomany",
    templateUrl: "./onetomany.component.html",
    styleUrls: ["./onetomany.component.css"]
})
export class OneToManyComponent implements OnInit, AfterViewChecked, OnDestroy {


    campaignType = CONSTANTS.ONE_TO_MANY_SHORT_FORM;

    removeDuplicateChecked = false;

    pattern_validation = CONSTANTS.pattern_validation;

    minLength = CONSTANTS.minLengthCampaignName;

    maxLength = CONSTANTS.maxLengthCampaignName;

    cancelMessage = CONSTANTS.INFO_TXT.campaignCancelMessage;

    senderIdList: string[] = [];

    entityId: string;

    entity_Id: string;

	sender_Id:string

    choosenTemplate: Templates;

    dlt_templateId: string;

    showSenderId: any = false;
    showTraffic: boolean = false;

    trafficType = "";

    entityIdprePopulate: boolean;

    constructor(
        private fb: FormBuilder,
        private campService: CampaignsService,
        private dataSharingService: DataSharingService,
        private route: ActivatedRoute,
        private router: Router
    ) { }

    @ViewChild(CampaignSenderIdComponent, { static: false })
    senderId: CampaignSenderIdComponent;

    @ViewChild(EntityIdComponent, { static: false })
    entityID: EntityIdComponent;

    @ViewChild(FileUploaderComponent, { static: false })
    dropzoneControl: FileUploaderComponent;

    @ViewChild(FileUploaderComponent, { static: false })
    dropzonesetfoucs: FileUploaderComponent;

    @ViewChild(CampaignNameComponent, { static: false })
    c_name: CampaignNameComponent;

    @ViewChild(CampaignMessageComponent, { static: false })
    c_message: CampaignMessageComponent;

    @ViewChild(IntlSendersComponent, { static: false })
    intlSenderIdControl: IntlSendersComponent;

    ngAfterViewChecked(): void {
        this.campService.setDropzoneControl(this.dropzoneControl);
        this.campService.setCnameControl(this.c_name);
        this.campService.setSenderIdControl(this.senderId);
        this.campService.setentityIDControl(this.entityID);
        this.campService.setMessageControl(this.c_message);
        this.campService.setIntlSenderIdControl(this.intlSenderIdControl)

    }

    ngOnInit(): void {
        // This data will come from API
        const previousData = {
            "27/05/2021": "27th Camp",
            "30/05/2021": "Camp May 30",
            "11/06/2021": "11th Camp, Summer Sale, Year End Sale"
        };

        this.dataSharingService.setData("previousCamps", previousData);

        const user = this.campService.getUser();
        const user_intl_enable = user.intl_enabled_yn;
        user_intl_enable == 1 ? this.showTraffic = true : this.showTraffic = false, this.trafficType = 'india';
        console.log(user_intl_enable);
        // this.route.queryParams.subscribe(params=>
        //     { 

        //       this.trafficType=params["traffic"];
        //       if(this.trafficType == "international"){

        //         this.oneToManySMSForm.controls.entityId.setValidators([]);
        //         this.oneToManySMSForm.controls.entityId.updateValueAndValidity();
        //         this.oneToManySMSForm.controls.senderId.setValidators([]);
        //         this.oneToManySMSForm.controls.senderId.updateValueAndValidity();

        //       }
        //       if(this.trafficType == "domestic"){

        //         this.oneToManySMSForm.controls.intlSenderId.setValidators([]);
        //         this.oneToManySMSForm.controls.intlSenderId.updateValueAndValidity();

        //       }

        // });


    }

    pageTitle = CONSTANTS.ONE_TO_MANY_TITLE;

    fileContentOrder = CONSTANTS.OTM_FILE_CONTENT;

    // used to pre-select some option while loading page or saved camps.
    senderIdToBeSelected = "kotak";

    oneToManySMSForm = this.fb.group({
        name: [
            "",
            [
                Validators.required,
                minLengthValidator(this.minLength),
                maxLengthValidator(this.maxLength),
                Validators.pattern(this.pattern_validation)
            ]
        ],
        files: ["", Validators.required],
        removeDuplicates: [true],
        entityId: [null, ],
        senderId: [null, Validators.required],
        message: ["", Validators.required],
        template: [null,],
        vlShortner: [false],
        c_langType: ["english"],
        trafficType: ["india"],
        intlSenderId: [null],
    });


    getSelectedItem(event: any) {
        this.oneToManySMSForm.controls.senderId.setValue(event.key);
    }

    getFileUploadSectionData(event: any) {
        this.oneToManySMSForm.controls.files.setValue(event.files);
    }


    EntityIdRequired(event: boolean) {
        this.oneToManySMSForm.controls.senderId.markAsTouched();
        this.senderId.setfocus();
    }
	senderIdEmitter(event:any){
		this.sender_Id = event;	
	}
    
	entityIdEmitter(event){
		this.entity_Id = event;
        this.entityId = event;
		this.oneToManySMSForm.controls.entityId.setValue(event);
	}


    showRetry: boolean = false;

    noData: boolean = false;

    Retry(event) {
        if (event == true) {
            //  this.senderIdAPIcall();    
            this.showRetry = false;
        }

    }

    selectedTemplate(event: any) {
        this.choosenTemplate = event;
		this.dlt_templateId = '';
		if (event) {
			this.dlt_templateId = this.choosenTemplate.dlt_template_id;
            this.oneToManySMSForm.controls.message.setValue("");
		}
		//  this.senderIdAPIcall();
		
    }

    invalidFocus() {
        // this.oneToManyFocus();
    }


    ngOnDestroy() {
        // this.directive.oneToManyFocus();
        this.campService.populateMessageDetails("", 0, 0, 0);
    }

    resetFocus(eve) {
        console.log(eve);

        if (eve.focus == 'entityid') {
            this.entityID.focus();
        } else if (eve.focus == 'message') {
            this.c_message.focus();
        } else if (eve.focus == 'pagetop') {
            this.c_name.scroll();
        } else {
            this.senderId.setfocus();
        }
        this.oneToManySMSForm.controls.senderId.markAsUntouched();
    }
    receivedEntityId(event: string) {
        this.entityId = event;
        if (event != undefined && this.c_message) {
            // this.c_message.focus();
        }
    }


    prepopulated(event) {
        this.entityIdprePopulate = event;
    }

    trafficChange(event) {
        console.log(event);
        if (event != undefined) {
            this.trafficType = event;
            this.oneToManySMSForm.controls.trafficType.setValue(event);
            this.campService.validateBasedOnTrafiic(
                this.trafficType,
                this.oneToManySMSForm
            );
        }
    }

    changeTraffic() {
        this.router.navigate(["/campaigns/traffic"], { queryParams: { campType: "otm" } })
    }
}
