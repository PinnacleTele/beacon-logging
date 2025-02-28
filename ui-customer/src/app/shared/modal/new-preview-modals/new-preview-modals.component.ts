import {
	AfterContentChecked,
	ChangeDetectorRef,
	Component,
	EventEmitter,
	Input,
	OnInit,
	Output
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CampaignsService } from 'src/app/campaigns/campaigns.service';
import { GenericCampaign } from 'src/app/campaigns/model/generic_campaign.model';
import { DataSharingService } from 'src/app/core/data-sharing.service';
import { Toast } from 'src/app/shared/toast/toast';
import { ToastService } from 'src/app/shared/toast/toast.service';
import { CONSTANTS } from 'src/app/shared/campaigns.constants';
import { GroupModel } from 'src/app/campaigns/model/campaign-group-model';
import { QuickSMSCampaignService } from 'src/app/campaigns/quick-sms/service/quick-sms-campaigns-service';
import { HttpErrorResponse } from '@angular/common/http';
import * as moment from 'moment';
import { ERROR } from 'src/app/campaigns/error.data';
@Component({
	selector: 'app-new-preview-modals',
	templateUrl: './new-preview-modals.component.html',
	styleUrls: ['./new-preview-modals.component.css']
})
export class NewPreviewModalsComponent implements OnInit, AfterContentChecked {
	@Input() campaignForm: any;

	@Input() campaignType: any;

	@Input() action: string;

	campaignName = '';

	mobileNumbersFormValue = '';

	files: any;

	message = '';

	preferenceType = '';

	language = '';

	senderId = '';

	intl_senderId = '';

	showPreviewModal = true;

	addedGroups: GroupModel[] = [];

	excludedGroups: GroupModel[] = [];

	// showSections
	showMobNumbersSection = false;

	showMessage = false;

	typeOfMsg = '';

	showFileSection = false;

	showGroupsSection = false;

	showExcGroupsSection = false;

	showTemplateMsg = false;

	public campaignObj: GenericCampaign;

	sending = false;

	actionPlusIng: string;

	excludedGroupLength: number | undefined;

	@Output() emitClosePreview = new EventEmitter();

	CQ = CONSTANTS.QUICK_SMS_SHORT_FORM;

	disableCancel = false;

	removeDuplicates = true;

	vlShortern = false;

	spinner: boolean = false;

	Responce: { message: string; statusCode: number };

	status: string;

	traffic: string;

	popup = false;

	payLoadRemoveDuplicates: number = 0;
	payLoadMobileNumbersAll: any;
	payLoadTemplateId: any;
	payLoadEntityId: any;
	payLoadSenderId: any;
	payLoadIntlSenderId: any;
	payLoadUrlShortern: number = 0;
	payLoadDLTTemplateId: any;
	payLoadTrafficType: any;
	payLoadCampaignName: any;
	payLoadMsgType: any;
	payLoadC_Lang:any;
	payLoadFileArray: { filename: any; r_filename: any; count: any }[] = [];
	payLoadIsUnicode:any;
	intl_allowed_value:boolean;
	@Input() trafficType: any;


	constructor(
		private campaignService: CampaignsService,
		public dataSharingService: DataSharingService,
		private cdref: ChangeDetectorRef,
		private router: Router,
		private route: ActivatedRoute,
		private toastService: ToastService,
		private cqService: QuickSMSCampaignService
	) // private scheduleService: ScheduleService
	{ }

	ngOnInit(): void {
		const user = this.campaignService.getUser();
        const user_intl_enable = user.intl_enabled_yn;
       // this.intl_allowed_value = user.intl_enabled_yn;
        if (user_intl_enable == 0) {
            this.intl_allowed_value = false;
        } else {
            this.intl_allowed_value = true;
        }

		this.campaignName = this.campaignForm.value.name;
		this.campaignName = this.campaignName.trim();
		this.removeDuplicates = this.campaignForm.controls.removeDuplicates.value;
		this.vlShortern = this.campaignForm.controls.vlShortner.value;
		console.log(this.campaignForm);

		// vl shortner
		if (this.campaignForm.controls.vlShortner.value == true) {
			this.payLoadUrlShortern = 1;
		} else {
			this.payLoadUrlShortern = 0;
		}
		// removeduplicates
		if (this.campaignForm.controls.removeDuplicates.value == true) {
			this.payLoadRemoveDuplicates = 1;
		} else {
			this.payLoadRemoveDuplicates = 0;
		}
		// entityid
		if (this.campaignForm.controls.entityId.value != undefined) {
			this.payLoadEntityId = this.campaignForm.controls.entityId.value;

		} else {
			this.payLoadEntityId = null;
		}
		// senderid
		if (this.campaignForm.controls.senderId.value != undefined) {
			this.payLoadSenderId = this.campaignForm.controls.senderId.value;

		} else {
			this.payLoadSenderId = null;
		}
		// intl senderid
		if (this.campaignForm.controls.intlSenderId.value != undefined) {
			this.payLoadIntlSenderId = this.campaignForm.controls.intlSenderId.value;

		} else {
			this.payLoadIntlSenderId = null;
		}
		// templateid
		if (this.campaignForm.controls.template?.value?.id != undefined) {
			this.payLoadTemplateId = this.campaignForm.controls.template.value?.id;
		} else {
			this.payLoadTemplateId = null;
		}
		//    dlt templateid
		if (this.campaignForm.controls.template?.value?.dlt_template_id != undefined) {
			this.payLoadDLTTemplateId = this.campaignForm.controls.template.value?.dlt_template_id;

		} else {
			this.payLoadDLTTemplateId = null;
		}

		//    traffic prefer
		if (this.campaignForm.controls.trafficType.value != undefined) {
			this.payLoadTrafficType = this.campaignForm.controls.trafficType.value;

		} else {
			this.payLoadTrafficType = null;
		}

		//    campaign name
		if (this.campaignForm.controls.name.value != undefined) {
			this.payLoadCampaignName = this.campaignForm.controls.name.value;
		} else {
			this.payLoadCampaignName = null;
		}
		// campaign message
		if (this.campaignForm.controls?.template?.value?.id != undefined) {
			this.message = this.campaignForm.controls.template?.value?.t_content;

		} else if (this.campaignForm.controls.template?.value?.dlt_template_content != undefined) {
			this.message = this.campaignForm.controls.template?.value?.dlt_template_content;
		} else {
			this.message = this.campaignForm.controls.message?.value;
		}

		// messagelang type
		
		if (this.campaignForm.controls.c_langType.value != undefined) {
			this.payLoadMsgType = this.campaignForm.controls.c_langType.value;
		} else {
			this.payLoadMsgType = null;
		}

		// isunicode
		if (this.campaignForm.controls.c_langType.value != undefined) {
			if (this.campaignForm.controls.c_langType.value == 'english') {
				this.payLoadIsUnicode = 0;
			} else {
				this.payLoadIsUnicode = 1;
			}
		//	this.payLoadIsUnicode = this.campaignForm.controls.c_langType.value;
		} else {
			this.payLoadIsUnicode = null;
		}

		// c_lang
		this.payLoadC_Lang = "";

		// file iteration
		if (this.campaignForm.controls.files?.value) {
			this.files = this.campaignForm.controls.files?.value;

			this.files.forEach((element: any) => {
				this.payLoadFileArray.push({
					filename: element.originalname,
					r_filename: element.r_filename,
					count: element.total
				});
			});
		}
	


		// for preview
		if (this.campaignForm.controls.trafficType.value == 'other') {
			this.traffic = 'other countries'
		} else {
			this.traffic = this.campaignForm.controls.trafficType.value;

		}

		this.senderId = this.campaignForm.value.senderId;
		this.intl_senderId = this.campaignForm.value.intlSenderId;

		if (['CQ'].includes(this.campaignType)) {
			this.showMobNumbersSection = true;
			this.mobileNumbersFormValue = this.campaignForm.controls.mobileNumbers.value;
			this.payLoadMobileNumbersAll = this.cqService.mobileNumbers;
		}

		if (['OTM', 'MTM', 'CT'].includes(this.campaignType)) {
			this.showFileSection = true;
			this.files = this.campaignForm.controls.files.value;

			this.typeOfMsg = this.campaignForm.controls.c_langType?.value;
		}

		if (['CQ', 'OTM', 'CG'].includes(this.campaignType)) {
			this.showMessage = true;
			this.typeOfMsg = this.campaignForm.controls.c_langType?.value;
			if (this.campaignForm.value.language) {
				this.language = this.campaignForm.value.language;
			}
		}

		if (['CG'].includes(this.campaignType)) {
			this.showGroupsSection = true;
			this.addedGroups = this.campaignForm.value.addedGroups;

			if (this.campaignForm.value.excludedGroups) {
				this.showExcGroupsSection = true;
				this.excludedGroups = this.campaignForm.value.excludedGroups;
			}
		}

		if (['CT'].includes(this.campaignType)) {
			this.showTemplateMsg = true;

		//	this.preferenceType = this.campaignForm.value.prefenceType;
		}
		const length = this.action.length;
		if (this.action.endsWith('e')) {
			this.actionPlusIng = `${this.action.slice(0, length - 1)}ing`;
		} else {
			this.actionPlusIng = `${this.action}ing`;
		}
		// this.excludedGroupLength = this.campaignObj.excludedGroups?.length

		this.dataSharingService.previewDates.forEach((ele: any) => {
			let Completedate = new Date(ele);
			let date = moment(Completedate).format('YYYY-MM-DD');
			let time = moment(Completedate).format('HH:mm:ss');
			this.scheduledTiming.push(ele);
			this.payLoadTimings.push({
				dt: date,
				time: time,
				zone_name: this.dataSharingService.getData('timezone')
			});
		});
	}
	scheduledTiming: any = [];

	payLoadTimings: any = [];

	zoneShortName =
		this.dataSharingService.getData('zoneShortName') != undefined
			? this.dataSharingService.getData('zoneShortName')
			: ' ';

	ngAfterContentChecked() {
		this.cdref.detectChanges();
	}

	closePreview() {
		if (this.sending) {
			this.showPreviewModal = true;
		} else {
			this.emitClosePreview.emit();
		}
	}

	onClick() {
		this.sending = true;

		const date = this.dataSharingService.getData('dateselected');
		const time = this.dataSharingService.getData('timeselected24Hrs');
		const tz = this.dataSharingService.getData('timezoneselected');

		this.disableCancel = true;

		if (this.action === 'Schedule') {
			if (this.campaignType === 'OTM') {
				this.OTM_CampaignSchedule();
			} else if (this.campaignType === 'MTM') {
				this.MTM_CampaignSchedule();
			} else if (this.campaignType === 'CQ') {
				this.CQ_CampaignSchedule();
			} else if (this.campaignType === 'CT') {
				this.CT_CampaignSchedule();
			} else if (this.campaignType === 'CG') {
				this.CG_CampaignSchedule();
			}
		} else if (this.action === 'Send') {
			if (this.campaignType === 'OTM') {
				this.OTM_CampaignSend();
			} else if (this.campaignType === 'MTM') {
				this.MTM_CampaignSend();
			} else if (this.campaignType === 'CQ') {
				this.CQ_CampaignSend();
			} else if (this.campaignType === 'CT') {
				this.CT_CampaignSend();
			} else if (this.campaignType === 'CG') {
				this.CG_CampaignSend();
			}
		}
	}

	disable(): boolean {
		if (this.action === 'Schedule') {
			const date = this.dataSharingService.getData('dateselected');
			const time = this.dataSharingService.getData('timeselected24Hrs');
			const tz = this.dataSharingService.getData('timezoneselected');
			if (date && time && tz) {
				return false;
			}
			return true;
		}
		return false;
	}

	OTM_CampaignSend() {
		var fileArray: { filename: any; r_filename: any; count: any }[] = [];

		if (this.campaignType == 'OTM') {
			var campType: any = 'otm';
		}

		let data = {
			c_name: this.payLoadCampaignName,
			remove_dupe_yn: this.payLoadRemoveDuplicates,
			shorten_url_yn: this.payLoadUrlShortern,
			msg: this.message,
			c_type: campType,
			c_lang_type: this.payLoadMsgType,
			template_id: this.payLoadTemplateId,
			dlt_template_id: this.payLoadDLTTemplateId,
			dlt_entity_id: this.payLoadEntityId,
			header: this.payLoadSenderId,
			intl_header: this.payLoadIntlSenderId,
			traffic_to: this.payLoadTrafficType,
			c_lang: this.payLoadC_Lang,
			files: this.payLoadFileArray
		};

		this.campaignService.sendOtmCampaign(data).subscribe(
			(res: any) => {
				if (res.statusCode === 200) {
					this.triggerToasterForSend();
				} else if (res.statusCode === -602) {
					this.BalanceErrorHandling(res);
				}
			},
			(error: HttpErrorResponse) => {
				this.sending = false;
				this.showPreviewModal = false;
				let err = this.campaignService.badError;
				this.Responce = err;
				this.status = error.message;
				this.popup = true;
			}
		);
	}

	MTM_CampaignSend() {
		if (this.campaignType == 'MTM') {
			var campType: any = 'mtm';
		}

		let data = {
			c_name: this.campaignForm.controls.name.value,
			remove_dupe_yn: this.payLoadRemoveDuplicates,
			shorten_url_yn: this.payLoadUrlShortern,
			c_type: campType,
			c_lang_type: this.payLoadMsgType,
			dlt_entity_id: this.payLoadEntityId,
			header: this.payLoadSenderId,
			intl_header: this.payLoadIntlSenderId,
			traffic_to: this.payLoadTrafficType,
			c_lang: this.payLoadC_Lang,
			files: this.payLoadFileArray
		};

		this.campaignService.sendMtmCampaign(data).subscribe(
			(res: any) => {
				if (res.statusCode === 200) {
					this.triggerToasterForSend();
				} else if (res.statusCode === -602) {
					this.BalanceErrorHandling(res);

				}
			},
			(error: HttpErrorResponse) => {
				this.sending = false;
				this.showPreviewModal = false;
				let err = this.campaignService.badError;
				this.Responce = err;
				this.status = error.message;
				this.popup = true;
			}
		);
	}

	CQ_CampaignSend() {
		var validcount = this.cqService.getValidMobileNumbersCount();
		if (this.campaignType == 'CQ') {
			var campType: any = 'quick';
		}

		let data = {
			c_name: this.campaignForm.controls.name.value,
			mobile_list: this.payLoadMobileNumbersAll,
			remove_dupe_yn: this.payLoadRemoveDuplicates,
			shorten_url_yn: this.payLoadUrlShortern,
			msg: this.message,
			c_type: campType,
			valid: validcount,
			c_lang_type: this.payLoadMsgType,
			template_id: this.payLoadTemplateId,
			dlt_template_id: this.payLoadDLTTemplateId,
			dlt_entity_id: this.payLoadEntityId,
			header: this.payLoadSenderId,
			intl_header: this.payLoadIntlSenderId,
			traffic_to: this.payLoadTrafficType,
			c_lang: this.payLoadC_Lang,
		};
		this.campaignService.sendCqCampaign(data).subscribe(
			(res: any) => {
				if (res.statusCode === 200) {
					this.triggerToasterForSend();
				} else if (res.statusCode === -602) {
					this.BalanceErrorHandling(res);

				}
			},
			(error: HttpErrorResponse) => {
				this.sending = false;
				this.showPreviewModal = false;

				let err = this.campaignService.badError;
				this.Responce = err;
				this.status = error.message;
				this.popup = true;
			}
		);
	}

	CT_CampaignSend() {

		if (this.campaignType == 'CT') {
			var campType: any = 'template';
		}

		let data = {
			c_name: this.campaignForm.controls.name.value,
			remove_dupe_yn: this.payLoadRemoveDuplicates,
			shorten_url_yn: this.payLoadUrlShortern,
			c_type: campType,
			c_lang_type: this.payLoadMsgType,
			template_id: this.payLoadTemplateId,
			dlt_template_id: this.payLoadDLTTemplateId,
			dlt_entity_id: this.payLoadEntityId,
			header: this.payLoadSenderId,
			msg: this.campaignForm.controls.tempmessage.value,
			intl_header: this.payLoadIntlSenderId,
			traffic_to: this.payLoadTrafficType,
			c_lang: this.payLoadC_Lang,			
			t_name: this.campaignForm.controls.templateName.value,
			t_type: this.campaignForm.controls.basedOn.value,
			t_mobile_column: this.campaignForm.controls.mobileColumn.value,
			save_template_yn: this.campaignForm.controls.saveTemplate.value,
			files: this.payLoadFileArray,
			is_static :  this.campaignForm.controls.isStatic.value,
			is_unicode : this.payLoadIsUnicode
		};

		this.campaignService.sendCtCampaign(data).subscribe(
			(res: any) => {
				if (res.statusCode === 200) {
					this.triggerToasterForSend();
				} else if (res.statusCode === -602) {
					this.BalanceErrorHandling(res);
				}
			},
			(error: HttpErrorResponse) => {
				this.sending = false;
				this.showPreviewModal = false;
				let err = this.campaignService.badError;
				this.Responce = err;
				this.status = error.message;
				this.popup = true;
			}
		);
	}

	CG_CampaignSend() {
		if (this.campaignType == 'CG') {
			var campType: any = 'group';
		}
		if (this.campaignForm.controls.eGroupIds.value === null) {
			var EGroups = [];
		} else {
			EGroups = this.campaignForm.controls.eGroupIds.value;
		}

		let data = {
			c_name: this.campaignForm.controls.name.value,
			remove_dupe_yn: this.payLoadRemoveDuplicates,
			shorten_url_yn: this.payLoadUrlShortern,
			msg: this.message,
			c_type: campType,
			c_lang_type: this.payLoadMsgType,
			template_id: this.payLoadTemplateId,
			dlt_template_id: this.payLoadDLTTemplateId,
			dlt_entity_id: this.payLoadEntityId,
			header: this.payLoadSenderId,
			intl_header: this.payLoadIntlSenderId,
			traffic_to: this.payLoadTrafficType,
			c_lang: this.payLoadC_Lang,
			group_ids: this.campaignForm.controls.groupIds.value,
			exclude_group_ids: EGroups,
		};

		this.campaignService.sendCgCampaign(data).subscribe(
			(res: any) => {
				if (res.statusCode === 200) {
					this.triggerToasterForSend();
				} else if (res.statusCode === -602) {
					this.BalanceErrorHandling(res);

				}
			},
			(error: HttpErrorResponse) => {
				this.sending = false;
				this.showPreviewModal = false;

				let err = this.campaignService.badError;
				this.Responce = err;
				this.status = error.message;
				this.popup = true;
			}
		);
	}

	triggerToasterForSend() {
		//setTimeout(() => {
		this.sending = false;
		this.closePreview();
		const toastData: Toast = new Toast(
			'Campaign Sent!',
			'You can check your campaign in campaign list',
			'success'
		);

		this.toastService.addToast(toastData);
		this.router.navigate(['/campaigns'], { relativeTo: this.route });
		//}, 2000);
	}

	//   schedule api request

	OTM_CampaignSchedule() {
		var fileArray: { filename: any; r_filename: any; count: any }[] = [];

		if (this.campaignType == 'OTM') {
			var campType: any = 'otm';
		}

		this.files.forEach((element: any) => {
			fileArray.push({
				filename: element.originalname,
				r_filename: element.r_filename,
				count: element.total
			});
		});

		let data = {
			c_name: this.campaignForm.controls.name.value,
			remove_dupe_yn: this.payLoadRemoveDuplicates,
			shorten_url_yn: this.payLoadUrlShortern,
			//msg: this.campaignForm.controls.message.value,
			msg: this.message,
			c_type: campType,
			c_lang_type: this.payLoadMsgType,
			template_id: this.payLoadTemplateId,
			dlt_template_id: this.payLoadDLTTemplateId,
			dlt_entity_id: this.payLoadEntityId,
			header: this.payLoadSenderId,
			intl_header: this.payLoadIntlSenderId,
			traffic_to: this.payLoadTrafficType,
			c_lang: this.payLoadC_Lang,
			files: this.payLoadFileArray,
			schedule_list: this.payLoadTimings
		};

		this.campaignService.scheduleOtmCampaign(data).subscribe(
			(res: any) => {
				if (res.statusCode === 200) {
					this.triggerToasterForSchedule();
				} else if (res.statusCode === -602) {
					this.BalanceErrorHandling(res);

				}
			},
			(error: HttpErrorResponse) => {
				this.sending = false;
				this.showPreviewModal = false;
				let err = this.campaignService.badError;
				this.Responce = err;
				this.status = error.message;
				this.popup = true;
			}
		);
	}

	MTM_CampaignSchedule() {
		if (this.campaignType == 'MTM') {
			var campType: any = 'mtm';
		}

		let data = {
			c_name: this.campaignForm.controls.name.value,
			remove_dupe_yn: this.payLoadRemoveDuplicates,
			shorten_url_yn: this.payLoadUrlShortern,
			c_type: campType,
			c_lang_type: this.payLoadMsgType,
			dlt_entity_id: this.payLoadEntityId,
			header: this.payLoadSenderId,
			intl_header: this.payLoadIntlSenderId,
			traffic_to: this.payLoadTrafficType,
			c_lang: this.payLoadC_Lang,
			files: this.payLoadFileArray,
			schedule_list: this.payLoadTimings
		};

		this.campaignService.scheduleMtmCampaign(data).subscribe(
			(res: any) => {
				if (res.statusCode === 200) {
					this.triggerToasterForSchedule();
				} else if (res.statusCode === -602) {
					this.BalanceErrorHandling(res);

				}
			},
			(error: HttpErrorResponse) => {
				this.sending = false;
				this.showPreviewModal = false;
				let err = this.campaignService.badError;
				this.Responce = err;
				this.status = error.message;
				this.popup = true;
			}
		);
	}

	CQ_CampaignSchedule() {
		var validcount = this.cqService.getValidMobileNumbersCount();

		if (this.campaignType == 'CQ') {
			var campType: any = 'quick';
		}

		let data = {
			c_name: this.campaignForm.controls.name.value,
			mobile_list: this.payLoadMobileNumbersAll,
			remove_dupe_yn: this.payLoadRemoveDuplicates,
			shorten_url_yn: this.payLoadUrlShortern,
			msg: this.message,
			c_type: campType,
			valid: validcount,
			c_lang_type: this.payLoadMsgType,
			template_id: this.payLoadTemplateId,
			dlt_template_id: this.payLoadDLTTemplateId,
			dlt_entity_id: this.payLoadEntityId,
			header: this.payLoadSenderId,
			intl_header: this.payLoadIntlSenderId,
			traffic_to: this.payLoadTrafficType,
			c_lang: this.payLoadC_Lang,
			schedule_list: this.payLoadTimings
		};


		this.campaignService.scheduleCqCampaign(data).subscribe(
			(res: any) => {
				if (res.statusCode === 200) {
					this.triggerToasterForSchedule();
				} else if (res.statusCode === -602) {
					this.BalanceErrorHandling(res);

				}
			},
			(error: HttpErrorResponse) => {
				this.sending = false;
				this.showPreviewModal = false;

				let err = this.campaignService.badError;
				this.Responce = err;
				this.status = error.message;
				this.popup = true;
			}
		);
	}

	CG_CampaignSchedule() {
		if (this.campaignType == 'CG') {
			var campType: any = 'group';
		}

		if (this.campaignForm.controls.excludedGroups.value === null) {
			var EGroups = [];
		} else {
			EGroups = this.campaignForm.controls.eGroupIds.value;
		}

		let data = {
			c_name: this.campaignForm.controls.name.value,
			remove_dupe_yn: this.payLoadRemoveDuplicates,
			shorten_url_yn: this.payLoadUrlShortern,
			msg: this.message,
			c_type: campType,
			c_lang_type: this.payLoadMsgType,
			template_id: this.payLoadTemplateId,
			dlt_template_id: this.payLoadDLTTemplateId,
			dlt_entity_id: this.payLoadEntityId,
			header: this.payLoadSenderId,
			intl_header: this.payLoadIntlSenderId,
			traffic_to: this.payLoadTrafficType,
			c_lang: this.payLoadC_Lang,
			group_ids: this.campaignForm.controls.groupIds.value,
			exclude_group_ids: EGroups,
			schedule_list: this.payLoadTimings
		};

		this.campaignService.scheduleCgCampaign(data).subscribe(
			(res: any) => {
				if (res.statusCode === 200) {
					this.triggerToasterForSchedule();
				} else if (res.statusCode === -602) {
					this.BalanceErrorHandling(res);

				}
			},
			(error: HttpErrorResponse) => {
				this.sending = false;
				this.showPreviewModal = false;

				let err = this.campaignService.badError;
				this.Responce = err;
				this.status = error.message;
				this.popup = true;
			}
		);
	}

	triggerToasterForSchedule() {
		//setTimeout(() => {
		this.sending = false;
		this.closePreview();
		const toastData: Toast = new Toast(
			'Campaign Scheduled!',
			'You can check your Scheduled campaign in schedule list',
			'success'
		);

		this.toastService.addToast(toastData);
		this.router.navigate(['/campaigns/scheduledlist'], { relativeTo: this.route });
		//}, 2000);
	}

	CT_CampaignSchedule() {
		var fileArray: { filename: any; r_filename: any; count: any }[] = [];

		this.files.forEach((element: any) => {
			fileArray.push({
				filename: element.originalname,
				r_filename: element.r_filename,
				count: element.total
			});
		});
		if (this.campaignType == 'CT') {
			var campType: any = 'template';
		}
		
		let data = {
			c_name: this.campaignForm.controls.name.value,
			remove_dupe_yn: this.payLoadRemoveDuplicates,
			shorten_url_yn: this.payLoadUrlShortern,
			c_type: campType,
			c_lang_type: this.payLoadMsgType,
			msg: this.campaignForm.controls.tempmessage.value,
			template_id: this.payLoadTemplateId,
			dlt_template_id: this.payLoadDLTTemplateId,
			dlt_entity_id: this.payLoadEntityId,
			header: this.payLoadSenderId,
			intl_header: this.payLoadIntlSenderId,
			traffic_to: this.payLoadTrafficType,
			c_lang: this.payLoadC_Lang,
			t_name: this.campaignForm.controls.templateName.value,
			t_type: this.campaignForm.controls.basedOn.value,
			t_mobile_column: this.campaignForm.controls.mobileColumn.value,
			save_template_yn: this.campaignForm.controls.saveTemplate.value,
			files: this.payLoadFileArray,
			is_unicode : this.payLoadIsUnicode,
			is_static : this.campaignForm.controls.isStatic.value,
			schedule_list: this.payLoadTimings
		};

		this.campaignService.scheduleCtCampaign(data).subscribe(
			(res: any) => {
				if (res.statusCode === 200) {
					this.triggerToasterForSchedule();
				} else if (res.statusCode === -602) {
					this.BalanceErrorHandling(res);

				}
			},
			(error: HttpErrorResponse) => {
				this.sending = false;
				this.showPreviewModal = false;
				let err = this.campaignService.badError;
				this.Responce = err;
				this.status = error.message;
				this.popup = true;
			}
		);
	}

	BalanceErrorHandling(error) {
		this.sending = false;
		this.showPreviewModal = false;
		let err = ERROR.DontHaveBalance
		this.Responce = err;
		this.status = error.message
		this.popup = true;
	}

	sub() {
		this.spinner = false;
	}

	modalClose(event: boolean) {
		this.popup = false;
		this.closePreview();
	}

	tryAgain(event: any) {
		let setError = {
			message: 'Please Wait Some Time',
			statusCode: 100,
			error: ' HTTP Server Timed Out'
		};

		this.Responce = setError;
		this.status = setError.message;
		this.popup = true;
		this.onClick();
	}

	modalcontinue(event: boolean) {
		this.sending = false;
		this.showPreviewModal = false;
	}
}
