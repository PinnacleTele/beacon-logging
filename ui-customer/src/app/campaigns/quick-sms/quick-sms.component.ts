import {
	AfterViewChecked,
	Component,
	OnInit,
	ViewChild,
	OnDestroy,
	QueryList,
	ElementRef
} from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';

import { FormBuilder, Validators } from '@angular/forms';
import { DataSharingService } from 'src/app/core/data-sharing.service';
import { maxLengthValidator } from 'src/app/campaigns/validators/maxlength-validator';
import { minLengthValidator } from 'src/app/campaigns/validators/minlength-validator';
import { MobileCountStatistics } from 'src/app/campaigns/model/generic-campaign-mobile-statistics';
import { MessageDetails } from 'src/app/campaigns/model/generic-campaign-msg-details';
import { GenericCampaign } from 'src/app/campaigns/model/generic_campaign.model';
import { CONSTANTS } from 'src/app/shared/campaigns.constants';
import { Subscription } from 'rxjs';
import { SingleSelectDropdownComponent } from 'src/app/shared/single-select-dropdown/single-select-dropdown.component';
import { NgSelectComponent } from '@ng-select/ng-select';
import { CampaignsService } from '../campaigns.service';

import { Templates } from 'src/app/shared/model/templates.model';
import { EntityIdComponent } from 'src/app/shared/entity-id/entity-id.component';
import { CampaignNameComponent } from 'src/app/campaigns/campaign-name/campaign-name.component';
import { CampaignMessageComponent } from 'src/app/campaigns/campaign-message/campaign-message.component';
import { CampaignSenderIdComponent } from 'src/app/campaigns/campaign-sender-id/campaign-sender-id.component';
import { QuickSmsMobileNumbersComponent } from 'src/app/campaigns/quick-sms/quick-sms-mobile-numbers/quick-sms-mobile-numbers.component';
import { ActivatedRoute, Router } from '@angular/router';
import { IntlSendersComponent } from '../intl-senders/intl-senders.component';

@Component({
	selector: 'app-quick-sms',
	templateUrl: './quick-sms.component.html',
	styleUrls: ['./quick-sms.component.css']
})
export class QuickSmsComponent implements OnInit, AfterViewChecked, OnDestroy {
	campaignType = CONSTANTS.QUICK_SMS_SHORT_FORM;

	cancelMessage = CONSTANTS.INFO_TXT.campaignCancelMessage;

	quickSMS: GenericCampaign;

	action: string;

	mobileCountStatistics: MobileCountStatistics;

	messageDetails: MessageDetails;

	cancel_clicked = false;

	openPreviewModal = false;

	openExitPageModal = false;

	subscription: Subscription;

	pattern_validation = CONSTANTS.pattern_validation;

	minLength = CONSTANTS.minLengthCampaignName;

	maxLength = CONSTANTS.maxLengthCampaignName;

	senderIdList: string[] = [];

	entityId: string;

	entity_Id: string;

	sender_Id:string

	choosenTemplate: Templates;

	dlt_templateId: string;

	showSenderId: any = false;

	showTraffic: boolean = false;

	entityIdprePopulate: boolean;
	trafficType: any;

	changeTrafficClicked = false;
	quickSMSForm = this.fb.group({
		name: [
			'',
			[
				Validators.required,
				minLengthValidator(this.minLength),
				maxLengthValidator(this.maxLength),
				Validators.pattern(this.pattern_validation)
			]
		],
		mobileNumbers: [, { validators: [Validators.required] }],
		removeDuplicates: [true],
		message: [, Validators.required],
		template: [],
		c_langType: ['english'],
		vlShortner: [false],
		entityId: [, ],
		senderId: [, Validators.required],
		trafficType: ['india'],
		intlSenderId: []
	});

	constructor(
		private fb: FormBuilder,
		private el: ElementRef,
		private route: ActivatedRoute,
		private dataSharingService: DataSharingService,
		private campaignService: CampaignsService,
		private router: Router
	) { }

	@ViewChild(CampaignSenderIdComponent, { static: false })
	senderId: CampaignSenderIdComponent;

	@ViewChild(EntityIdComponent, { static: false })
	entityID: EntityIdComponent;

	@ViewChild(CampaignNameComponent, { static: false })
	c_name: CampaignNameComponent;

	@ViewChild(CampaignMessageComponent, { static: false })
	c_message: CampaignMessageComponent;

	@ViewChild(QuickSmsMobileNumbersComponent, { static: false })
	mobileNumber: QuickSmsMobileNumbersComponent;

	@ViewChild('select')
	selectfocus: NgSelectComponent;

	@ViewChild(SingleSelectDropdownComponent, { static: false })
	senderIdControl: SingleSelectDropdownComponent;

	@ViewChild(IntlSendersComponent, { static: false })
	intlSenderIdControl: IntlSendersComponent;

	ngOnInit(): void {
		// This data will come from API

		const user = this.campaignService.getUser();
		const user_intl_enable = user.intl_enabled_yn;
		user_intl_enable == 1 ? this.showTraffic = true : this.showTraffic = false, this.trafficType = 'india';
		console.log(user_intl_enable);


	}

	ngAfterViewChecked(): void {
		this.campaignService.setCnameControl(this.c_name);
		this.campaignService.setSenderIdControl(this.senderId);
		this.campaignService.setentityIDControl(this.entityID);
		this.campaignService.setMessageControl(this.c_message);
		this.campaignService.setMobileNumControl(this.mobileNumber);
		this.campaignService.setIntlSenderIdControl(this.intlSenderIdControl)
	}

	// getSelectedItem(event: any) {
	// 	this.quickSMSForm.controls.senderId.setValue(event.key);
	// }

	get senderid() {
		return this.quickSMSForm.controls.senderId;
	}

	EntityIdRequired(event: boolean) {
		this.senderid.markAsTouched();
		this.senderId.setfocus();
	}

	senderIdEmitter(event:any){
		this.sender_Id = event;	
	}
	entityIdEmitter(event){
		this.entity_Id = event;
		//this.entityId = event;
		this.quickSMSForm.controls.entityId.setValue(event);
	}

	showRetry: boolean = false;
	noData: boolean = false;

	selectedTemplate(event: any) {
		this.choosenTemplate = event;
		this.dlt_templateId = '';
		if (event) {
			this.dlt_templateId = this.choosenTemplate.dlt_template_id;
			this.quickSMSForm.controls.message.setValue("");
		}
		//  this.senderIdAPIcall();
		
	}
	

	focusables = [
		'input',
		'NG-SELECT',
		'textarea',
		'select',
		'checkbox',
		'radio'
	];

	invalidFocus() {
		if (!this.quickSMSForm.valid) {
			let target;
			for (var i in this.quickSMSForm.controls) {
				if (!this.quickSMSForm.controls[i].valid) {
					target = this.quickSMSForm.controls[i];
					break;
				}
			}
		}
	}

	prepopulated(event) {
		this.entityIdprePopulate = event;
	}

	resetFocus(eve) {
		if (eve.focus == 'entityid') {
			this.entityID.focus();
		} else if (eve.focus == 'message') {
			this.c_message.focus();
		} else if (eve.focus == 'pagetop') {
			this.c_name.scroll();
		} else {
			this.senderId.setfocus();
		}

		this.quickSMSForm.controls.senderId.markAsUntouched();
	}
	receivedEntityId(event: string) {
		this.entityId = event;
		/*
		if (event != undefined && this.c_message) {
			 this.c_message.focus();
		}
		*/
	}

	trafficChange(event) {
		console.log(this.quickSMSForm, 'in quc');
		if (event != undefined) {
			this.trafficType = event;

			this.quickSMSForm.controls.trafficType.setValue(event);
			this.campaignService.validateBasedOnTrafiic(
				this.trafficType,
				this.quickSMSForm
			);
		}
	}

	changeTraffic() {
		this.changeTrafficClicked = true;
	}
	confirmModalResp(event: any) {
		this.changeTrafficClicked = false;

		if (event == 'clear') {

			this.router.navigate(['/campaigns/traffic'], {
				queryParams: { campType: 'cq' }
			});
		}

	}

	ngOnDestroy() {
		this.campaignService.populateMessageDetails('', 0, 0, 0);

	}
}
