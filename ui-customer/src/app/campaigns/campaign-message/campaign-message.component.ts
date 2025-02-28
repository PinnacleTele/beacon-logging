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
import { ControlContainer, FormControl, Validators } from '@angular/forms';
import { EMPTY, Subscription} from 'rxjs';
import { catchError, debounceTime, distinctUntilChanged, switchMap , tap} from 'rxjs/operators';
import { LocalStorageService } from 'src/app/authentication/local-storage.service';

import { CONSTANTS } from 'src/app/shared/campaigns.constants';
import { TemplateCampaignService } from '../campaign-template/service/template-campaign.service';
import { CampaignsService } from '../campaigns.service';
import { PostMsgPartsService } from '../quick-sms/service/post-msg-parts.service';


@Component({
	selector: 'app-campaign-message',
	templateUrl: './campaign-message.component.html',
	styleUrls: ['./campaign-message.component.css']
})
export class CampaignMessageComponent implements OnInit, OnChanges, OnDestroy {
	public campaignMsgInfoTxt = CONSTANTS.INFO_TXT.campaignMessage;

	public campaignLangInfoTxt = CONSTANTS.INFO_TXT.campaignLanguage;

	public noUnicodeTxt = CONSTANTS.ERROR_DISPLAY.noUnicodeText;

	msgPartsSubscription: Subscription;
	
	totalCharCount = 0;
	newLineCount = 0;
	partsCount: number = 0;
	shortner: any;
	shortners: any;

	@Input() camptype: any;
	@Input() cType: any;
	@Input() trafficType: any;
	//@Input() entityId: any;
	@Input() entity_Id: any;
	@Input() sender_Id: any;


	langType: any;

	mobileHandsetMessage: string;

	public campaignMsgFormGroup: any;

	public vlForm: any;
	Dlt_template_ID: string;
	campaignType = 'Others'; //not CT
	retryMessage: string;

	public handsetapiError: boolean = false;
	isShowTemplateSlider: boolean = false;
	selectedTemplate: any;
	showPreview = false;
	msgSpinner = false;
	openClearModal = false;
	@Output() choosenTemplate = new EventEmitter<any>();
	@Output() noEntityId = new EventEmitter<boolean>();
	
	
	
	constructor(
		private campaignService: CampaignsService,
		private postMsgPartsService: PostMsgPartsService,
		public controlContainer: ControlContainer,
        private templateSvc : TemplateCampaignService,
		private localStorageService:LocalStorageService
		
	) {}
	ngOnChanges(changes: SimpleChanges): void {
		if (changes.sender_Id) {
			this.sender_Id = changes.sender_Id.currentValue;
		}
		if (changes.sender_Id && (this.selectedTemplate?.t_content || this.selectedTemplate?.dlt_template_content)) {
			this.clearTemplate();
		}
	}

	ngOnInit(): void {
		this.campaignMsgFormGroup = this.controlContainer.control;
		this.vlForm = this.controlContainer.control;

		let vlshortner1 = this.localStorageService.getLocal('user');
		this.shortner = JSON.parse(vlshortner1);
		this.shortners = this.shortner.vl_shortner;
		//only when text area content changes, when template is selected getmsginfo
		this.message.valueChanges
			.pipe(
				debounceTime(400),
				distinctUntilChanged(),
				switchMap(msg => {
					const msgValue = msg.trim();
					//if condition added,  api is not hit when the message changes becaus of template selection
					if (msgValue && !(this.template.value)) {
						this.msgSpinner = true;
						this.msgType = "";
						this.retryMessage = msgValue;
						this.presetStateValues();
						return this.postMsgPartsService.getMsgPartsInfo(msgValue)
							.pipe(
								catchError((err) => {
									this.msgSpinner = false;
									this.showRetry = true;
									this.presetStateValues();
									return EMPTY;
								})//catch is added to continue further api call if error occurs in between
							);
					} else {
						if (!this.template.value) {
							this.partsCount = 0;
							this.totalCharCount = 0;
						}
						return EMPTY;
					}

				})
			).subscribe((res) => {
				this.msgSpinner = false;

				if (res.statusCode > 299 || res.statusCode < 200) {
					this.showRetry = true;

				} else {
					this.handleSuccessResponse(res);
				}
			},
				(err) => {
					this.showRetry = true;
					this.handsetapiError = true;
					this.msgSpinner = false;
					this.campaignMsgFormGroup.controls.message.setErrors({
						apiRequestError: true
					});
				})

	}

	onChange(event) {
		if (this.shortners == 1) {
			this.shortners = 1;
		} else {
			this.shortners = 0;
		}
	}

	get message(): FormControl {
		return this.campaignMsgFormGroup.controls.message;
	}

	get template(): FormControl {
		return this.campaignMsgFormGroup.controls.template;
	}

	get c_langType() {
		return this.campaignMsgFormGroup.controls.c_langType;
	}
	get parentSenderid() {
		return this.campaignMsgFormGroup.controls.senderId;
	}

	get vlshortner() {
		return this.vlForm.controls.vlShortner;
	}

	focus() {
		const focus = document.getElementById('message') as HTMLImageElement;
		if (focus) {
			focus.focus();
		}
	}

	openModal() {
		this.openClearModal = true;
	}

	clearModalResponse(response: string) {
		if (response === 'clear') {
			this.campaignMsgFormGroup.controls.message.setValue('');
			this.campaignMsgFormGroup.controls.message.setErrors({ required: true })
			this.totalCharCount = 0;
			this.partsCount = 0;
			this.openClearModal = false;
			this.isUnicode1 = false;
			this.mobileHandsetMessage = '';
		}
		if (response === 'close') {
			this.openClearModal = false;
		}
	}



	msgValue: any;
	showRetry: boolean = false;
	isUnicode1: boolean;
	retryApi() {
		this.showRetry = false;
		this.getMsgPartsInfo(this.retryMessage);
	}

	

	presetStateValues() {
		this.handsetapiError = false;
		this.totalCharCount = 0;
		this.partsCount = 0;
		this.campaignMsgFormGroup.controls.message.setErrors({
			apiRequestError: true
		});

	}
	setErrorOnControl() {
		if (this.msgType == "template") {
			this.campaignMsgFormGroup.controls.template.setErrors({
				apiRequestError: true
			}, { required: true });
		} else {
			this.campaignMsgFormGroup.controls.message.setErrors({
				apiRequestError: true
			}, { required: true });

		}
	}
	//getMsgPartsInfo will be called on template selection and on retry click
	getMsgPartsInfo(event: string) {
		this.showRetry = false;
		this.msgSpinner = true;
		this.handsetapiError = false;
		this.totalCharCount = 0;
		this.partsCount = 0;
		this.setErrorOnControl()

		const msgValue = event;
		this.retryMessage = msgValue.trim();

		if (msgValue.length > 0) {

			this.msgPartsSubscription = this.postMsgPartsService
				.getMsgPartsInfo(msgValue)
				.subscribe(
					(res) => {
						this.msgSpinner = false;

						if (res.statusCode > 299 || res.statusCode < 200) {
							this.showRetry = true;
							this.campaignMsgFormGroup.controls.c_langType.setValue(
								this.isUnicode1 ? 'unicode' : 'english'
							);
						} else {
							this.handleSuccessResponse(res);
						}
					},
					(err) => {
						let error = this.campaignService.badError;
						this.showRetry = true;

						this.handsetapiError = true;
						this.msgSpinner = false;
						this.setErrorOnControl();
					}
				);
		}
	}

	ngOnDestroy() {
		if (this.msgPartsSubscription) {
			this.msgPartsSubscription.unsubscribe();
		}


		if (this.selectedTemplate?.t_content || this.selectedTemplate?.dlt_template_content) {

			this.clearTemplate();
		}
	}
	
	openTemplateSlider() {
		this.parentSenderid.markAsTouched();
		if (this.sender_Id) {
			this.isShowTemplateSlider = true;
		} else {
			this.noEntityId.emit(true);
		}
	}
	closeSlider() {
		this.isShowTemplateSlider = false;
	}

	emittedTemplate(event: any) {

		this.selectedTemplate = event;
		this.showPreview = true;
		this.msgType = "template";
		this.message.setErrors(null);
		if (this.selectedTemplate.t_content != undefined) {
			this.callApiOnTemplateSelection(this.selectedTemplate.t_content);
			this.msgValue = this.selectedTemplate.t_content;
		} else {
			this.callApiOnTemplateSelection(
				this.selectedTemplate.dlt_template_content
			);
			this.msgValue = this.selectedTemplate.dlt_template_content;
		}
		//this.campaignMsgFormGroup.controls.template.setValue(this.selectedTemplate);
		this.choosenTemplate.emit(this.selectedTemplate);

		this.Dlt_template_ID = this.selectedTemplate.dlt_template_id;

		// this.templateFormGroup.controls.template.setValue(this.selectedTemplate.t_name);
	}
	msgType = "";
	callApiOnTemplateSelection(msg: string) {
		const fieldValue = msg.trim();
		this.newLineCount = (fieldValue.match(/\n/g) || '').length;
		this.campaignMsgFormGroup.controls.template.setValue(this.selectedTemplate);
		this.getMsgPartsInfo(fieldValue);
		this.msgType = "template"
	}
	changeOrEditResponse() {
		this.isShowTemplateSlider = true;
	}
	resetOnBlur() {
		//cu 158
		this.campaignMsgFormGroup.controls.template.setValue(null);
		this.campaignMsgFormGroup.controls.template.setValidators(null);
		this.campaignMsgFormGroup.controls.template.updateValueAndValidity();

	}
	clearTemplate() {

		this.campaignMsgFormGroup.controls.template.setValue(null);
		this.campaignMsgFormGroup.controls.template.setErrors(null);
		this.campaignMsgFormGroup.controls.template.updateValueAndValidity();

		this.msgSpinner = false;
		if (this.msgPartsSubscription) {
			this.msgPartsSubscription.unsubscribe();
		}


		this.showRetry = false;
		this.showPreview = false;
		this.totalCharCount = 0;
		this.partsCount = 0;
		this.selectedTemplate.t_content = '';
		this.mobileHandsetMessage = '';

		this.templateSvc.populateSelectedTemplate(undefined);
		this.templateSvc.populateSelectedDltTemplate(undefined);
		this.choosenTemplate.emit();
		this.isUnicode1 = false;
	}

	handleSuccessResponse(res : any){
		this.showRetry = false;
		this.mobileHandsetMessage = res.msg;
		this.totalCharCount = res.chars;
		this.partsCount = res.parts;
		this.isUnicode1 = res.isUnicode;
		if (res.isUnicode == true) {
			this.langType = 'unicode';
		} else {
			this.langType = 'Text';
		}
		this.campaignMsgFormGroup.controls.c_langType.setValue(
			this.isUnicode1 ? 'unicode' : 'english'
		);
		this.campaignMsgFormGroup.controls.message.setErrors(null);
		this.campaignMsgFormGroup.controls.template.setErrors(null);
		
		this.newLineCount = (res.msg.match(/\n/g) || '').length;
		this.campaignService.populateMessageDetails(
			res.msg,
			this.totalCharCount,
			this.newLineCount,
			this.partsCount
		);
	}
}
