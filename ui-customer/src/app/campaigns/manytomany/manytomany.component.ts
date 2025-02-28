import { HttpErrorResponse } from '@angular/common/http';
import {
	AfterViewChecked,
	Component,
	ElementRef,
	OnDestroy,
	OnInit,
	Renderer2,
	ViewChild
} from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { DataSharingService } from 'src/app/core/data-sharing.service';
import { FileUploaderComponent } from 'src/app/shared/file-uploader/file-uploader.component';
import { SingleSelectDropdownComponent } from 'src/app/shared/single-select-dropdown/single-select-dropdown.component';
import { CampaignsService } from '../campaigns.service';
import { maxLengthValidator } from '../validators/maxlength-validator';
import { minLengthValidator } from '../validators/minlength-validator';
import { CONSTANTS } from '../../shared/campaigns.constants';
import { CampaignSenderIdComponent } from 'src/app/campaigns/campaign-sender-id/campaign-sender-id.component';
import { EntityIdComponent } from 'src/app/shared/entity-id/entity-id.component';
import { CampaignNameComponent } from '../campaign-name/campaign-name.component';
import { CampaignMessageComponent } from '../campaign-message/campaign-message.component';
import { ActivatedRoute, Router } from '@angular/router';
import { IntlSendersComponent } from '../intl-senders/intl-senders.component';

@Component({
	selector: 'app-manytomany',
	templateUrl: './manytomany.component.html',
	styleUrls: ['./manytomany.component.css']
})
export class ManyToManyComponent implements OnInit, AfterViewChecked, OnDestroy {
	campaignType = CONSTANTS.MANY_TO_MANY_SHORT_FORM;

	pattern_validation = CONSTANTS.pattern_validation;

	minLength = CONSTANTS.minLengthCampaignName;

	maxLength = CONSTANTS.maxLengthCampaignName;

	cancelMessage = CONSTANTS.INFO_TXT.campaignCancelMessage;

	trafficType = '';

	showTraffic: boolean = false;

	entityIdprePopulate: boolean;

	
	entity_Id: string;

	sender_Id:string

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
		this.campService.setDropzoneControl(this.dropzonesetfoucs);
		this.campService.setCnameControl(this.c_name);
		this.campService.setSenderIdControl(this.senderId);
		this.campService.setentityIDControl(this.entityID);
		this.campService.setMessageControl(this.c_message);
		this.campService.setIntlSenderIdControl(this.intlSenderIdControl)

	}

	ngOnInit(): void {
		// This data will come from API
		const previousData = {
			'27/05/2021': '27th Camp',
			'30/05/2021': 'Camp May 30',
			'11/06/2021': '11th Camp, Summer Sale, Year End Sale'
		};

		this.dataSharingService.setData('previousCamps', previousData);
		const user = this.campService.getUser();
		const user_intl_enable = user.intl_enabled_yn;
		user_intl_enable == 1 ? this.showTraffic = true : this.showTraffic = false, this.trafficType = 'india';
		console.log(user_intl_enable);


	}

	senderIdEmitter(event:any){
		this.sender_Id = event;	
	}

	entityIdEmitter(event){
		this.entity_Id = event;
		this.entityId = event;
		this.manyToManySMSForm.controls.entityId.setValue(event);
	}

	pageTitle = CONSTANTS.MANY_TO_MANY_TITLE;

	fileContentOrder = CONSTANTS.MTM_FILE_CONTENT;

	delimitersList = CONSTANTS.COMPAIGN_DELIMETERLIST;

	entityId: string;

	senderIdList: string[] = [];

	showSenderId: boolean = false;

	manyToManySMSForm = this.fb.group({
		name: [
			'',
			[
				Validators.required,
				minLengthValidator(this.minLength),
				maxLengthValidator(this.maxLength),
				Validators.pattern(this.pattern_validation)
			]
		],
		c_langType: ['english'],
		files: ['', Validators.required],
		removeDuplicates: [true],
		vlShortner: [false],
		entityId: [null,],
		senderId: [null, Validators.required],
		intlSenderId: [null,],
		trafficType: ["india"],
	});

	// removeDuplicateChecked = false;

	getSelectedDelimiter(event: any) {
		this.manyToManySMSForm.controls.delimiter.setValue(event.key);
	}

	getSelectedItem(event: any) {
		this.manyToManySMSForm.controls.senderId.setValue(event.key);
	}

	getFileUploadSectionData(event: any) {
		this.manyToManySMSForm.controls.files.setValue(event.files);
	}

	EntityIdRequired(event: boolean) {
		this.manyToManySMSForm.controls.entityId.markAsTouched();
		this.senderId.setfocus();
	}

	showRetry: boolean = false;
	noData: boolean = false;

	invalidFocus() { }

	onSubmit() { }

	ngselect: SingleSelectDropdownComponent;

	focusables = [
		'input',
		'textarea',
		'select',
		'checkbox',
		'radio',
		'NG-SELECT'
	];

	checkValid() {
		if (this.manyToManySMSForm.controls.name.invalid) {
			this.c_name.focus();
		} else if (this.dropzoneControl.succeedFiles.length <= 0) {
			this.dropzoneControl.fileuploadset();
		} else if (this.manyToManySMSForm.controls.entityId.invalid) {
			this.entityID.focus();
		}
		// this.c_message.focus();
		else if (this.manyToManySMSForm.controls.senderId.invalid) {
			this.senderId.setfocus();
		}
	}

	resetFocus(eve) {
		if (eve.focus == 'entityid') {
			this.entityID.focus();
		} else if (eve.focus == 'message') {
			this.senderId.setfocus();
		} else if (eve.focus == 'pagetop') {
			this.c_name.scroll();
		} else {
			this.senderId.setfocus();
		}
		this.manyToManySMSForm.controls.senderId.markAsUntouched();
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
			this.manyToManySMSForm.controls.trafficType.setValue(event);
			this.campService.validateBasedOnTrafiic(
				this.trafficType,
				this.manyToManySMSForm
			);
		}
	}

	changeTraffic() {
		this.router.navigate(['/campaigns/traffic'], {
			queryParams: { campType: 'mtm' }
		});
	}

	ngOnDestroy(): void {
		//this.campService.previousTrafficSelection("");
	}
}
