import {
	Component,
	EventEmitter,
	Input,
	OnChanges,
	OnDestroy,
	OnInit,
	Output,
	SimpleChanges
} from '@angular/core';
import { CONSTANTS } from 'src/app/shared/campaigns.constants';
import { Templates } from 'src/app/shared/model/templates.model';
import { CampaignsService } from '../../campaigns.service';
import { MessageDetails } from '../../model/generic-campaign-msg-details';
import { TemplateCampaignService } from '../service/template-campaign.service';

import { PostMsgPartsService } from 'src/app/campaigns/quick-sms/service/post-msg-parts.service';


@Component({
	selector: 'app-preview-template',
	templateUrl: './preview-template.component.html',
	styleUrls: ['./preview-template.component.css']
})
export class PreviewTemplateComponent implements OnInit, OnChanges {
	@Input() selectedTemplate: Templates;

	@Input() campaignType: string;
	@Input() charsCount: number;
	@Input() partsCount: number;
	@Input() langType: string;
	@Input() anyChange: boolean;
	@Input() handSetMessage: string;
	// langType: string;
	@Output() changeOrEdit = new EventEmitter<string>();
	@Output() templateLangType = new EventEmitter<string>();

	CTShort = CONSTANTS.TEMPLATE_SHORT_FORM
	template: Templates | any;

	newMsg = '';

	templateName = '';

	showPreview = false;

	showWarning = false;
	isStatic = false;
	staticTemplate = CONSTANTS.STATIC_TEMPLATE;
	returnObj = {
		newMsg: '',
		warning: false,
		isStatic: false
	};
	contentType: string = '';
	isUnicode1: any;

	handMessagePreview: string;

	handsetapiError:boolean =false;

	retryMessage:string;

	constructor(
		private msgpartservice: PostMsgPartsService,
		private templateSvc: TemplateCampaignService,
	) { }

	ngOnChanges(changes: SimpleChanges) {

		this.template = this.selectedTemplate;
		this.contentType =
			this.template.is_unicode == 0 || this.template.pattern_type == 0 ? 'english' : 'unicode';
		this.refreshPreview();

	}

	ngOnInit(): void {
		this.template = this.selectedTemplate;

		//this.msgDetails = this.campaignService.formulateMessageDetails();
		//	this.refreshPreview();
	}

	clickChangeOrEdit(event: string) {
		this.changeOrEdit.emit(event);
	}
	refreshPreview() {
		if (this.campaignType === 'CT') {
			let msgContent;
			if (this.template.t_content != undefined) {
				
				msgContent= this.template.t_content;
			} else {
				
				msgContent = this.template.dlt_template_content;
			}
			this.returnObj = this.templateSvc.CTMessageforPreview(
				msgContent
			);
			

			
			this.newMsg = this.returnObj.newMsg;
			this.isStatic = this.returnObj.isStatic;

			this.showWarning = this.returnObj.warning;
			 this.msgpartcount(this.newMsg);
		} else {
			let msgContent;
			if (this.template.t_content != undefined) {
				
				msgContent= this.template.t_content;
			} else {
				
				msgContent = this.template.dlt_template_content;
			}
			this.newMsg = msgContent;
			

		}
	}
spinner= false;
	msgpartcount(event: string) {
		this.retryMessage = event;
		this.handsetapiError = false;
		let msgValue = event.trim();
	this.spinner= true;
		if (msgValue.length > 0) {

			this.msgpartservice
				.getMsgPartsInfo(msgValue)
				.subscribe(
					res => {
						this.spinner= false;
						if (res.statusCode > 299 || res.statusCode < 200) {

						} else {

							this.handMessagePreview = res.msg;
							if (res.isUnicode == true) {
								this.langType = "Unicode";

							} else {
								this.langType = "Text";
							}
						}
					},
					(err) => {
						this.spinner= false;
						this.handsetapiError = true;
						return err;
					}

				);
		}



	}

	retry(){
		this.msgpartcount(this.retryMessage);
	}
}
