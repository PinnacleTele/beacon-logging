import { AfterViewChecked, Component, OnDestroy, OnInit, ViewChild } from "@angular/core";
import { FormBuilder, Validators } from "@angular/forms";
import { CONSTANTS } from "src/app/shared/campaigns.constants";
import { FileUploaderComponent } from "src/app/shared/file-uploader/file-uploader.component";
import { CampaignsService } from "../campaigns.service";
import { maxLengthValidator } from "../validators/maxlength-validator";
import { minLengthValidator } from "../validators/minlength-validator";
import { TemplateCampaignService } from "./service/template-campaign.service";
import { Templates } from "../../shared/model/templates.model";
import { HttpErrorResponse } from "@angular/common/http";
import { CampaignSenderIdComponent } from "../campaign-sender-id/campaign-sender-id.component";
import { EntityIdComponent } from "src/app/shared/entity-id/entity-id.component";

import { CampaignNameTemplateComponent } from "./campaign-name-template/campaign-name-template.component";
import { CampaignMessageComponent } from "../campaign-message/campaign-message.component";
import { TemplateNameComponent } from "src/app/shared/components/template-name/template-name.component";
import { Subscription } from "rxjs";
import { IntlSendersComponent } from "../intl-senders/intl-senders.component";
import { TemplateMessageComponent } from "src/app/campaigns/campaign-template/template-message/template-message.component";
@Component({
    selector: "app-campaign-template",
    templateUrl: "./campaign-template.component.html",
    styleUrls: ["./campaign-template.component.css"]
    //    providers: [TemplateCampaignService]
})
export class CampaignTemplateComponent implements OnInit, AfterViewChecked, OnDestroy {


    campaignType = CONSTANTS.TEMPLATE_SHORT_FORM;

    showTempMsgArea = false;

    pageTitle = "Template";

    templatePreference = "";
    showTemplateSlider = false;
    isShowTemplate = false;
    isUnicode = "";

    fileContentOrder = CONSTANTS.CAMP_TEMPLATE_FILE_CONTENT;
    chosenTemplate: Templates;
    chkIfFileExists = true;
    showPreview = false;
    templateId: string = "";
    paramsMap = new Map();
    templateMsg = "";
    cType: any;
    msgPartsSubscription: Subscription;
    totalCharCount = 0;
    newLineCount = 0;
    partsCount: number;

    files: any;

    fileError = false;

    showSenderId = false;
    showDropzone = false;

    pattern_validation = CONSTANTS.pattern_validation;

    resetFileUploader = false;

    langType: string;

    HandsetMessage: string;

    minLength = CONSTANTS.minLengthCampaignName;

    maxLength = CONSTANTS.maxLengthCampaignName;

    cancelMessage = CONSTANTS.INFO_TXT.campaignCancelMessage;

    onLoading: boolean = false;

    TemplateTypeSelection: string = "template";

    showTraffic: boolean = false;

    trafficType: any;

    entityIdprePopulate: boolean;


    constructor(
        private fb: FormBuilder,

        private campaignService: CampaignsService,
        private templateSvc: TemplateCampaignService,
    ) {


    }

    ngOnInit(): void {
        const user = this.campaignService.getUser();
        const user_intl_enable = user.intl_enabled_yn;
        // let enable = 0;
        if (user_intl_enable == 1) {
            this.showTraffic = true

        } else {
            this.showTraffic = false,
                this.trafficType = 'india'

        }
        //console.log(user_intl_enable);
    }
    @ViewChild(CampaignSenderIdComponent, { static: false })
    senderID: CampaignSenderIdComponent;

    @ViewChild(EntityIdComponent, { static: false })
    entityID: EntityIdComponent;


    @ViewChild(CampaignNameTemplateComponent, { static: false })
    c_name: CampaignNameTemplateComponent;

    @ViewChild(FileUploaderComponent, { static: false })
    dropzoneControl: FileUploaderComponent;

    @ViewChild(TemplateMessageComponent, { static: false })
    c_message: TemplateMessageComponent;

    @ViewChild(TemplateNameComponent, { static: false })
    t_name: TemplateNameComponent;

    @ViewChild(IntlSendersComponent, { static: false })
    intlSenderIdControl: IntlSendersComponent;

    @ViewChild(TemplateNameComponent, { static: false })
    Tname: TemplateNameComponent;

    subscription: Subscription = this.templateSvc.focusemit.subscribe((data) => data ? this.focus() : '');



    templateForm = this.fb.group({
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
        vlShortner: [false],
        entityId: [null,],
        template: ["", Validators.required],
        c_langType: ["english"],
        tempmessage: [, Validators.required],
        basedOn: [null,],
        mobileColumn: [null,],
        trafficType: ['india'],
        templateName: [
            null
        ],
        saveTemplate: [0],
        senderId: [null, Validators.required],
        intlSenderId: [null],
        isStatic: [],
        isUnicode: []

    });


    ngAfterViewChecked(): void {
        // console.log("view");
        this.campaignService.setTemplateNameControl(this.c_name);
        this.campaignService.setTNameControl(this.Tname);
        // console.log(this.c_name);
        // this.templateSvc.setCampaignTemplateControl(this.c_templatComponent);
        this.campaignService.setDropzoneControl(this.dropzoneControl);
        this.campaignService.setSenderIdControl(this.senderID);
        this.campaignService.setentityIDControl(this.entityID);
        this.campaignService.setTemplateMessageControl(this.c_message);
        this.campaignService.setIntlSenderIdControl(this.intlSenderIdControl);
    }
    sendEnable = false;
    sendButtonEnable(eve) {
        // console.log(eve);

        this.sendEnable = eve
    }
    onClickSelectTemplate() {
        //   this.showDropzone = true;
        //  this.resetFileUploader = true;
        // this.showSenderId = true;
    }

    fileExists: boolean = false;
    getFileUploadSectionData(event: any) {
        this.resetFileUploader = false;
        // console.log(event.files, ":::");
        let fileCounts = 0;

        if (event.files != undefined) {
            fileCounts = +JSON.stringify(event.files.length);
        }

        this.paramsMap.clear();
        if (fileCounts > 0) {
            this.fileExists = true;
            const previewFile = this.templateSvc.getFirstFileDetails();
            for (let i = 0; i < fileCounts; i++) {
                this.paramsMap.set(event.files[i].originalname, {
                    fileName: event.files[i].originalname,
                    placeholders: event.files[i].placeholders,
                    missingParams: event.files[i].missingParams,
                    isStatic: event.files[i].isStatic
                });
            }
            if (previewFile !== undefined) {
                this.chkIfFileExists = this.paramsMap.has(previewFile.fileName);
            }

            this.templateForm.controls.files.setValue(event.files);

            if (event.files[0]) {

                this.templateSvc.populateFileDetails(this.paramsMap.get(event.files[0].originalname));
                this.showPreview = true;
            }
        } else {
            this.fileExists = false;
            this.templateForm.controls.files.setValue("");
            this.showPreview = false;
        }

    }

    entityId: string;

    receivedEntityId(event: string) {
        this.resetFileUploader = false;
        this.showTempMsgArea = false;
        this.showDropzone = false;
        this.dlt_templateId = "";
        this.entityId = event;
        if (event != undefined && this.c_message) {

            // this.c_message.focus();

        }

        console.log(this.paramsMap.size);

        if (this.paramsMap.size > 0) {
            console.log('paramap is 0');
            this.dropzoneControl.ctRemoveFile();
            this.resetFileUploader = true;
            this.paramsMap.clear();

        }
        this.showPreview = false;
        // this.senderIdAPIcall();

    }

    EntityIdRequired(event: boolean) {
        this.templateForm.controls.senderId.markAsTouched();
        this.senderID.setfocus();
    }
    dlt_templateId = "";
    senderIdList: any[] = [];
    dltselected: boolean = false;
    tempType: string;
    tempColumn: string;
    dlttempType: string;
    dltTemplateMobileColumnName: string = "";
    public columnName: any;

    selectedTemplate(event: any) {
        this.showTempMsgArea = true;
        this.showTemplateSlider = false;
        this.showDropzone = true;

        if (this.paramsMap.size > 0) {
            // console.log('insideparamap');

            this.resetFileUploader = true;
            this.paramsMap.clear();
            this.showPreview = false;
        } else {
            this.resetFileUploader = false;
        }

        this.showSenderId = true;
        let lang;
        this.chosenTemplate = event;
        if (event.t_content != undefined) {

            this.dltselected = false;
            this.TemplateTypeSelection = "template";
            this.templateMsg = event.t_content;
            this.tempType = event.t_type;
            this.tempColumn = event.t_mobile_column;
            lang = event.t_lang_type;
            //  this.showDropzone = true;
            this.isUnicode = event.t_lang_type;
            this.templateSvc.setValidatorsToColumnNameFields(this.templateForm, false);
        } else {
            // this.resetFileUploader = true;
            this.dltselected = true;
            this.TemplateTypeSelection = "dlttemplate";
            this.dlttempType = this.templateForm.controls.basedOn.value;
            this.dltTemplateMobileColumnName = this.templateForm.controls.mobileColumn.value;
            this.listenToTemplateTypeChanges();
            this.templateSvc.setValidatorsToColumnNameFields(this.templateForm, true);
            this.templateMsg = event.dlt_template_content;
            //this.showDropzone = false;
            if (event.pattern_type == 0) {
                this.isUnicode = 'english';
            } else {
                this.isUnicode = 'unicode';
            }
            lang = this.isUnicode;
        }
        this.templateId = event.id;
        this.dlt_templateId = event.dlt_template_id;
        this.templateForm.controls.template.setValue(event);
        this.campaignService.setSelectedTemplateInfo(event);
        this.templateForm.controls.c_langType.setValue(this.isUnicode);

    }
    columnNameUpdate() {
        console.log("update");

        if (this.tType === 'index') {
            this.templateForm?.controls.mobileColumn.setValidators([Validators.pattern("^[0-9]+$")]);
        } else {
            this.templateForm?.controls.mobileColumn.setValidators([Validators.required, Validators.maxLength(50)])
            this.templateForm?.controls.mobileColumn.updateValueAndValidity();
        }

        this.showUploadFile();
    }

    setError: boolean;

    showUploadFile() {
        if (this.columnName != undefined) {
            const trimmed = this.columnName.trim();

            if (trimmed.length > 0 && this.templateForm.controls.mobileColumn.valid) {
                this.templateForm.controls.mobileColumn.setValue(trimmed);
                // this.showDropzone = true;
                this.setError = false;
            } else if (trimmed.length == 0 || this.templateForm.controls.mobileColumn.invalid) {
                this.setError = true;
            }
            else {
                this.setError = false;
            }
        }

    }

    messageContent(event) {
        this.templateMsg = event;
    }

    messageTypeReceived(event) {
        if (event == true) {
            this.templateForm.controls.isStatic.setValue(1);
        } else {
            this.templateForm.controls.isStatic.setValue(0)
        }

    }

    isUnicodeReceived(event) {
        this.templateForm.controls.isUnicode.setValue(event);
    }
    focus() {
        const focus = document.getElementById("mobilecolumnId") as HTMLImageElement;
        this.columnNameUpdate()
        focus.focus();
        // focus.blur();
        // console.log(this.templateForm.controls.mobileColumn);

        this.templateForm.controls.mobileColumn.markAsTouched();

        // focus.scrollIntoView();
    }

    showRetry: boolean = false;
    noData: boolean = false;

    changeOrEditResponse() {

        this.showTemplateSlider = true;
    }

    onSubmit() {
        //  console.log("whats the message "+this.templateForm.controls.existingmessage.value);
    }

    get mobileCol() {
        return this.templateForm.controls.mobileColumn;
    }

    textToDisplay: string;
    tType: string;
    placeholder: string;

    listenToTemplateTypeChanges() {
        this.templateSvc
            .getBasedOn()
            .subscribe((type) => {
                this.tType = type;
                // this.resetFileUploader = false;
                if (type === 'index') {
                    this.placeholder = "Enter the index position";
                    this.textToDisplay = 'Position';
                    this.columnName = "";
                    this.showUploadFile();
                    // this.resetFileUploader = true;
                }
                if (type === 'column') {

                    this.placeholder = "Enter the column name"
                    this.textToDisplay = 'Column';
                    this.columnName = "";
                    this.showUploadFile();
                    //   this.resetFileUploader = true;
                }
                this.templateForm.controls.basedOn.setValue(this.tType);
                // const textAreaMsg = this.newTemplateForm.controls.newmessage
                // 	.value;
                //this.newTemplateForm.controls.basedOn.setValue(type);
                // if (textAreaMsg !== '') {
                // 	this.populateFormValuesInPreview();
                // 	 this.returnObj = this.templateSvc.messageForPreview(
                // 		textAreaMsg,
                // 		this.previewIndexOrColumn
                // 	);
                // 	this.newMsg = this.returnObj.newMsg;  
                // 	this.showWarning = this.returnObj.warning;
                // }
            });
    }

    saveTemp: boolean = false;
    saveTemplate(event) {
        // console.log(event.target.checked);

        if (event.target.checked) {
            this.saveTemp = true;
            this.templateForm.controls.saveTemplate.setValue(1);
            this.templateSvc.setValidatorsToNameFields(this.templateForm);
        } else {
            this.saveTemp = false;
            this.templateForm.controls.saveTemplate.setValue(0);
            this.templateForm.controls.templateName.clearValidators();
            this.templateForm.controls.templateName.updateValueAndValidity();

        }

    }

    retryFile() {
        // console.log('retry');
    }

    receivedLangType(event) {
        this.langType = event.lType;
        this.HandsetMessage = event.msg;
        // if (this.trafficType == 'other') {
        this.templateForm.controls.template.setValue(event.msg);
        this.templateForm.controls.c_langType.setValue(this.langType);
        // }
    }
    resetFocus(eve) {
        if (eve.focus == 'entityid') {
            this.entityID.focus();
        } else if (eve.focus == 'message' && this.c_message) {

            this.c_message?.focus();
        } else if (eve.focus == 'pagetop') {
            this.t_name.retry();
        } else {
            this.c_message?.focus();
        }
        this.templateForm.controls.senderId.markAsUntouched();
    }
    reValidate() {
        this.campaignTemplateFocus();
        if (this.dropzoneControl) {
            this.dropzoneControl.reValidate();
        }
    }
    mobileColumnBlur() {
        console.log("blur");

        if (this.dropzoneControl.succeedFiles.length > 0 || this.dropzoneControl.failedFiles.length > 0) {
            this.dropzoneControl?.reValidate();

        }
    }

    prepopulated(event) {
        this.entityIdprePopulate = event;
    }

    trafficChange(event) {
        if (event != undefined) {
            this.trafficType = event;
            if (this.trafficType == 'other') {
                // this.dltselected = true;
                this.showTempMsgArea = true;
                this.forInternational();
            }

            this.templateForm.controls.trafficType.setValue(event);
            this.campaignService.validateBasedOnTrafiic(
                this.trafficType,
                this.templateForm
            );
        }
    }

    forInternational() {
        this.dltselected = true;
        this.TemplateTypeSelection = "dlttemplate";
        this.showDropzone = true;

        this.listenToTemplateTypeChanges();
        this.dlttempType = this.templateForm.controls.basedOn.value;
        this.dltTemplateMobileColumnName = this.templateForm.controls.mobileColumn.value;
        this.templateSvc.setValidatorsToColumnNameFields(this.templateForm, true);
        //  this.templateMsg = event.dlt_template_content;
        //this.showDropzone = false;
        // if (event.pattern_type == 0) {
        //     this.isUnicode = 'english';
        // } else {
        //     this.isUnicode = 'unicode';
        // }
        // lang = this.isUnicode;
    }
    senderId:any;
    senderIdEmitter(event: any) {
        console.log('senderid emitter',event);
        this.senderId = event;
        this.resetFileUploader = false;
        this.showTempMsgArea = false;
        this.showDropzone = false;
        this.dlt_templateId = "";
        // this.entityId = event;
        if(event == undefined){
            this.dltselected = false;
        }
        if (event != undefined && this.c_message) {

            // this.c_message.focus();

        }

        console.log(this.paramsMap.size);

        if (this.paramsMap.size > 0) {
            console.log('paramap is 0');
            this.dropzoneControl.ctRemoveFile();
            this.resetFileUploader = true;
            this.paramsMap.clear();

        }
        this.showPreview = false;
    }
    entityIdEmitter(event) {
        console.log('entityid emitter',event);
        
        this.entityId = event;
        this.templateForm.controls.entityId.setValue(event);
    }


    ngOnDestroy(): void {
        //  this.campaignService.previousTrafficSelection("");
    }

    campaignTemplateFocus(){
        if(this.templateForm.controls.name.invalid){
            this.c_name.focus()
        }
        else if(this.senderID &&this.templateForm.controls.senderId.invalid ){
            this.senderID.setfocus();
        }
        else if(this.c_message &&this.templateForm.controls.tempmessage.invalid){
            this.c_message.focus();
        }
        else if(this.t_name &&this.templateForm.controls.templateName.invalid){
            this.t_name.focus();
        }
        else if(this.dltselected && this.showDropzone &&this.templateForm.controls.mobileColumn.invalid){
            const mobile = document.getElementById("mobilecolumnId") as HTMLImageElement;
            mobile.focus();
        }
        else if(this.intlSenderIdControl &&this.templateForm.controls.intlSenderId.invalid ){
            this.intlSenderIdControl.setfocus();
        }
    }
}
