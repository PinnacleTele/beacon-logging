import {
	Component,
	OnDestroy,
	OnInit,
	Input,
	Output,
	EventEmitter,
	OnChanges,
	SimpleChanges,
	ViewChild
} from '@angular/core';
import { FormBuilder, FormControl, Validators } from '@angular/forms';
import { Subscription } from 'rxjs';
import { map } from 'rxjs/operators';
import { TemplateCampaignService } from 'src/app/campaigns/campaign-template/service/template-campaign.service';
import { maxLengthValidator } from 'src/app/campaigns/validators/maxlength-validator';
import { minLengthValidator } from 'src/app/campaigns/validators/minlength-validator';
import { CONSTANTS } from 'src/app/shared/campaigns.constants';
import { DLTs } from 'src/app/shared/model/DLTs.model';
import { TemplateService } from '../Helpers/templates.service';
import { SelectMobileColumnComponent } from 'src/app/shared/components/select-mobile-column/select-mobile-column.component';
import { TemplateNameComponent } from 'src/app/shared/components/template-name/template-name.component';
import { CreateNewTemplateMessageComponent } from 'src/app/templates/create-new-template/create-new-template-message/create-new-template-message.component';
import { Router } from '@angular/router';
import { EntityIdComponent } from 'src/app/shared/entity-id/entity-id.component';

@Component({
	selector: 'app-create-new-template',
	templateUrl: './create-new-template.component.html',
	styleUrls: ['./create-new-template.component.css']
})
export class CreateNewTemplateComponent
	implements OnInit, OnDestroy, OnChanges {
	// Initial
	isText = true;
	isUnicode: any;
	isShowContact = false;
	showPreview = false;
	removeDuplicateChecked = true;
	showMobileColSection = false;
	showCreateModal = false;
	showCancelModal = false;
	showFileContents = false;
	added = false;
	pattern_validation = CONSTANTS.pattern_validation;
	minLength = CONSTANTS.minLengthTemplateName;
	maxLength = CONSTANTS.maxLengthTemplateName;

	files: any = {};
	//end
	type = 'column';
	placeholder = 'Select Mobile Number column';
	DLTList: DLTs[] = [];
	itemList: string[] = [];
	showMessage = false;
	templateMsg: string = '';
	selectedMsg:any;
	textToDisplay = 'Column';
	previewIndexOrColumn = 'column';
	previewMobileColumn = '';
	previewTemplateName = '';
	subscription2: Subscription;
	subscriptionBasedOn: Subscription;
	newMsg = '';
	showWarning = false;
	enableCreateButton = false;
	fileDetails: any;

	returnObj = {
		newMsg: '',
		warning: false
	};
	showFileUploadSection = false;
	staticTemplate = 1;
	@ViewChild(SelectMobileColumnComponent, { static: false })
	mobileCol: SelectMobileColumnComponent;

	@ViewChild(TemplateNameComponent, { static: false })
	tname: TemplateNameComponent;

	@ViewChild(CreateNewTemplateMessageComponent, { static: false })
	tmessage: CreateNewTemplateMessageComponent;

	@ViewChild(EntityIdComponent, { static: false })
	entityID: EntityIdComponent;

	@Output() changeEvent = new EventEmitter<null>();

	cancelMessage = CONSTANTS.INFO_TXT.templateCancelMessage;
	singleFileUploaded :any;
	constructor(
		private fb: FormBuilder,
		private tservice: TemplateService,
		private templateSvc: TemplateCampaignService,
		private router: Router
	) {}

	ngOnChanges(changes: SimpleChanges): void {
		console.log(changes,this.entityID);
		
		if (this.entityId) {
			//	this.clear();
		}
	}


	newTemplateForm = this.fb.group({
		templateName: [
			'',
			[
				Validators.required,
				minLengthValidator(this.minLength),
				maxLengthValidator(this.maxLength),
				Validators.pattern(this.pattern_validation)
			]
		],
		file: [''],
		basedOn: [''],
		mobileColumn: [null],
		newmessage: ['', [Validators.required, minLengthValidator(1)]],
		language: [''],
		entityId: [null, Validators.required],
		dlt: [null, Validators.required],
		removeDuplicates: [true],
		patternType: [''],
		isStatic: [true]
	});

	ngOnInit(): void {
		this.listenToTemplateTypeChanges();
	}

	populateDLTList() {
		this.subscription2 = this.templateSvc
			.findAllDLTs()
			.pipe(
				map((DLTArray) => {
					DLTArray.forEach((data) => {
						this.DLTList.push(data);
						this.itemList.push(data.dltId);
					});
				})
			)
			.subscribe();
	}

	listenToTemplateTypeChanges() {
		this.subscriptionBasedOn = this.templateSvc
			.getBasedOn()
			.subscribe((type) => {
				this.previewIndexOrColumn = type;
				if (type === 'index') {
					this.textToDisplay = 'Position';
				}
				if (type === 'column') {
					this.textToDisplay = 'Column';
				}
				
				this.newTemplateForm.controls.basedOn.setValue(type);
				this.formulatePreview();
			});
	}

	entityId: string;
	receivedEntityId(event: string) {
		console.log(event);
		this.entityId = event;
		//this.clear();
		this.showFileUploadSection = false;
		this.showMessage = false;
		this.templateMsg = "";
		this.newMsg = "";
		this.enableCreateButton = false;
	}

	EntityIdRequired(event: boolean) {
		
		
		this.newTemplateForm.controls.entityId.markAsTouched();
	}

	onChangeMobileColumn(event: string) {
		this.previewMobileColumn = event;
		this.populateFormValuesInPreview();
	}

	onClickFileUpload(event: any) {
		
		if (event !== 'error') {
			this.templateSvc.populateFileDetails(event);
			this.newTemplateForm.controls.file.setValue(event);
			this.showMobileColSection = true;
			this.showPreview = true;
			this.added = true;
			this.singleFileUploaded = "File Uploaded"
			this.enableCreateButton = true;
			this.formulatePreview();
		}else {
			//this.clear();
			console.log("file err or deleted")
			
			if (this.mobileCol) {
				this.mobileCol.ClearValue();
			}

			this.showMobileColSection = false;
			this.showPreview = false;

			this.enableCreateButton = false;
			this.newTemplateForm.controls.file.setValue(null);
			this.added = false;
		}
	}

	ngAfterViewChecked(): void {
		this.tservice.setTname(this.tname);
		this.tservice.setMobCol(this.mobileCol);
		this.tservice.setmsgArea(this.tmessage);
	}

	populateFormValuesInPreview() {
		this.previewTemplateName = this.newTemplateForm.controls.templateName.value;
		//this.previewIndexOrColumn = this.newTemplateForm.controls.basedOn.value;
		//this.previewMobileColumn = this.newTemplateForm.controls.mobileColumn.value;
	}

	findMatchingDLTMsg(event: any) {
		
		this.showMessage = true;
		this.singleFileUploaded = "reset";
		this.resetBelowFileButtonSection();
		this.staticTemplate = event.is_static;
		
		
		if(this.staticTemplate == 0){
			// set isStatic control value to dynamic once we select a template
			this.showFileUploadSection = true;
			this.enableCreateButton = false;
			this.setValidatorsToForm();
		}else{
			// set isStatic control value to static once we select a template
			this.showFileUploadSection = false;
			this.enableCreateButton = true;
			
		}
		if (event.dlt_template_content) {
			
			this.newTemplateForm.controls.dlt.setValue(event.dlt_template_id);
			this.newTemplateForm.controls.patternType.setValue(
				event.pattern_type
			);
			this.isUnicode = event.pattern_type;
			
			this.templateMsg = event.dlt_template_content;
			this.selectedMsg = event.dlt_template_content;
			this.newMsg = this.templateMsg;
			this.showWarning = this.returnObj.warning;
		} else {
			this.showMessage = false;
			this.templateMsg = '';
			this.selectedMsg = '';
		}
	}

	msgTextAreaChanged(event: string) {
		
		this.templateMsg = event;
		if(event == ""){
			//this.singleFileUploaded = "reset";
			this.showFileUploadSection = false;
			this.showPreview = false;
			this.showMessage = false;
			this.showMobileColSection = false;
		}else{
			
			if (this.showPreview) {
				this.formulatePreview();
			}
	
			this.showFileUploadSection = !this.msgType;
			this.setValidatorsToForm();
			if(this.showFileUploadSection ){
				
			}else{
				this.enableCreateButton = true;
			}
			
			
		}
		
	}

	get msgType() {
		console.log(this.newTemplateForm.controls.isStatic.value)
		return this.newTemplateForm.controls.isStatic.value;
	}
	get file1() {
		return this.newTemplateForm.controls.file;
	}

	setValidatorsToForm() {
		
		/*if(this.file1.value){
			
		//	this.singleFileUploaded = this.file1.value;
			this.showMobileColSection = true;
			this.showPreview = true;
		}*/
		if (this.showFileUploadSection) {
			this.newTemplateForm.controls.file.setValidators([
				Validators.required
			]);
			this.newTemplateForm.controls.file.updateValueAndValidity();

			this.newTemplateForm.controls.basedOn.setValidators([
				Validators.required
			]);
			this.newTemplateForm.controls.basedOn.updateValueAndValidity();

			this.newTemplateForm.controls.mobileColumn.setValidators([
				Validators.required
			]);
			this.newTemplateForm.controls.mobileColumn.updateValueAndValidity();
			console.log('set validators');
		} else {
			this.resetBelowFileButtonSection();
			console.log('clear validators');
		}
	}

	resetBelowFileButtonSection() {
		this.showMobileColSection = false;
		this.showPreview = false;
		
		this.newTemplateForm.controls.file.setValidators([]);
		this.newTemplateForm.controls.file.updateValueAndValidity();

		this.newTemplateForm.controls.basedOn.setValidators([]);
		this.newTemplateForm.controls.basedOn.updateValueAndValidity();

		this.newTemplateForm.controls.mobileColumn.setValidators([]);
		this.newTemplateForm.controls.mobileColumn.updateValueAndValidity();
	}
formulatePreview(){
	
	const textAreaMsg = this.newTemplateForm.controls.newmessage
	.value;
	this.previewIndexOrColumn = this.newTemplateForm.controls.basedOn.value;
	if (textAreaMsg !== '' && this.showPreview) {
		this.populateFormValuesInPreview();
		this.returnObj = this.templateSvc.messageForPreview(
			textAreaMsg,
			this.previewIndexOrColumn
		);
		this.newMsg = this.returnObj.newMsg;
		this.showWarning = this.returnObj.warning;
	}
}
	ngOnDestroy() {
		// this.subscription2.unsubscribe();
		// this.subscriptionBasedOn.unsubscribe();
		this.templateSvc.populateNewTemplateMobileCol('');
		this.templateSvc.sendBasedOn('');
		this.file1.setValue(null);
		
		this.templateSvc.populateSelectedTemplate(undefined);
		this.templateSvc.populateSelectedDltTemplate(undefined);
	}
	routing(routePath: string) {
		this.router.navigate([routePath]);
	}
}
