import {
	AfterViewChecked,
	Component,
	OnDestroy,
	OnInit,
	ViewChild
} from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { CampaignSenderIdComponent } from '../campaign-sender-id/campaign-sender-id.component';
import { CONSTANTS } from 'src/app/shared/campaigns.constants';
import { EntityIdComponent } from 'src/app/shared/entity-id/entity-id.component';
import { Templates } from 'src/app/shared/model/templates.model';
import { CampaignMessageComponent } from '../campaign-message/campaign-message.component';
import { CampaignNameComponent } from '../campaign-name/campaign-name.component';
import { CampaignsService } from '../campaigns.service';
import { maxLengthValidator } from '../validators/maxlength-validator';
import { minGrouplengthValidator } from '../validators/mingrouplength-validator';
import { minLengthValidator } from '../validators/minlength-validator';
import { AddGroupComponent } from './add-group/add-group.component';
import { ActivatedRoute, Router } from '@angular/router';
import { IntlSendersComponent } from '../intl-senders/intl-senders.component';

@Component({
	selector: 'app-groups',
	templateUrl: './groups.component.html',
	styleUrls: ['./groups.component.css']
})
export class GroupsComponent implements AfterViewChecked, OnInit, OnDestroy {
	trafficType = '';
	constructor(
		private fb: FormBuilder,
		private campaignService: CampaignsService,
		private route: ActivatedRoute,
		private router: Router
	) { }

	some: string;

	showTraffic: boolean = false;

	
	entity_Id: string;

	sender_Id:string


	entityList: string[] = [];

	ngOnInit() {
		const user = this.campaignService.getUser();
		const user_intl_enable = user.intl_enabled_yn;
		user_intl_enable == 1 ? this.showTraffic = true : this.showTraffic = false, this.trafficType = 'india';
		console.log(user_intl_enable);
	}

	cancelMessage = CONSTANTS.INFO_TXT.campaignCancelMessage;

	pageTitle = 'Group SMS';

	campaignType = 'CG';

	removeDuplicateChecked = true;

	pattern_validation = CONSTANTS.pattern_validation;

	minLength = CONSTANTS.minLengthCampaignName;

	maxLength = CONSTANTS.maxLengthCampaignName;

	senderIdList: string[] = [];

	showSenderId: any = false;

	entityIdprePopulate: boolean;


	ngAfterViewChecked(): void {
		this.campaignService.setaddGroupControl(this.addGroups);
		this.campaignService.setCnameControl(this.c_name);
		this.campaignService.setSenderIdControl(this.senderId);
		this.campaignService.setentityIDControl(this.entityID);
		this.campaignService.setMessageControl(this.c_message);
		this.campaignService.setIntlSenderIdControl(this.intlSenderIdControl)

	}

	senderIdEmitter(event:any){
		this.sender_Id = event;	
	}
	entityIdEmitter(event){
		this.entity_Id = event;
		this.entityId = event;
		this.groupSMSForm.controls.entityId.setValue(event);
	}

	getSelectedSenderId(event: any) {
		this.groupSMSForm.controls.senderId.setValue(event);
	}

	groupSMSForm = this.fb.group({
		name: [
			'',
			[
				Validators.required,
				minLengthValidator(this.minLength),
				maxLengthValidator(this.maxLength),
				Validators.pattern(this.pattern_validation)
			]
		],
		addedGroups: [
			'',
			[Validators.required, minGrouplengthValidator.bind(this)]
		],
		excludedGroups: [],
		entityId: [null,],
		senderId: [null, Validators.required],
		removeDuplicates: [true],
		message: ['', Validators.required],
		vlShortner: [false],
		template: [null],
		c_langType: [],
		groupIds: [],
		intlSenderId: [null],
		trafficType: ["india"],
		eGroupIds: [null]
	});

	@ViewChild(AddGroupComponent, { static: false })
	addGroups: AddGroupComponent;

	@ViewChild(CampaignSenderIdComponent, { static: false })
	senderId: CampaignSenderIdComponent;

	@ViewChild(EntityIdComponent, { static: false })
	entityID: EntityIdComponent;

	@ViewChild(CampaignNameComponent, { static: false })
	c_name: CampaignNameComponent;

	@ViewChild(CampaignMessageComponent, { static: false })
	c_message: CampaignMessageComponent;

	@ViewChild(IntlSendersComponent, { static: false })
	intlSenderIdControl: IntlSendersComponent;

	addedGroups(event: any) {
		this.groupSMSForm.controls.addedGroups.setValue(event);
		let groupIds = [];
		event.forEach((ele: any) => {
			groupIds.push(ele.id);
		});
		this.groupSMSForm.controls.groupIds.setValue(groupIds);
	}

	excludedGroups(event: any) {
		this.groupSMSForm.controls.excludedGroups.setValue(event);
		let groupIds = [];
		event.forEach((ele: any) => {
			groupIds.push(ele.id);
		});
		this.groupSMSForm.controls.eGroupIds.setValue(groupIds);
	}

	onSubmit() {
		if (this.addedGroups.length === 0) {
			this.groupSMSForm.controls.addedGroups.setErrors({
				emptyList: true
			});
		}
	}
	onTemplateTypeSelected() { }

	entityId: string;

	receivedEntityId(event: string) {
		this.entityId = event;
		if (event != undefined && this.c_message) {
			//this.c_message.focus();
			console.log('inside foucs');

		}
		const ent = document.querySelector("app-campaign-header")
		console.log(window.pageYOffset);

	}

	EntityIdRequired(event: boolean) {
		this.groupSMSForm.controls.entityId.markAsTouched();
		this.senderId.setfocus();
	}
	showRetry: boolean = false;
	noData: boolean = false;

	onClickSelectTemplate() { }
	choosenTemplate: Templates;
	dlt_templateId: string;

	selectedTemplate(event: any) {
        this.choosenTemplate = event;
		this.dlt_templateId = '';
		if (event) {
			this.dlt_templateId = this.choosenTemplate.dlt_template_id;
			this.groupSMSForm.controls.message.setValue("");
		}
		//  this.senderIdAPIcall();
		
    }
	
	ngOnDestroy() {
		// this.directive.oneToManyFocus();
		this.campaignService.populateMessageDetails('', 0, 0, 0);
		//this.campaignService.previousTrafficSelection("");
	}

	prepopulated(event) {
		this.entityIdprePopulate = event;
	}

	resetFocus(eve) {
		console.log('reset');

		if (eve.focus == 'entityid') {
			this.entityID.focus();
		} else if (eve.focus == 'message') {
			this.c_message.focus();
		} else if (eve.focus == 'pagetop') {
			this.c_name.scroll();
		} else {
			this.senderId.setfocus();
		}
		this.groupSMSForm.controls.senderId.markAsUntouched();
	}

	trafficChange(event) {
		console.log(event);
		if (event != undefined) {
			this.trafficType = event;
			this.groupSMSForm.controls.trafficType.setValue(event);
			this.campaignService.validateBasedOnTrafiic(
				this.trafficType,
				this.groupSMSForm
			);
		}
	}

	changeTraffic() {
		this.router.navigate(['/campaigns/traffic'], {
			queryParams: { campType: 'cg' }
		});
	}
}
