import { RemoveDuplicatesComponent } from './../../../campaigns/remove-duplicates/remove-duplicates.component';
import { Component, EventEmitter, Input, OnInit, Output, SimpleChanges } from "@angular/core";
import { ControlContainer } from "@angular/forms";
import { Subscription } from "rxjs";
import { TemplateCampaignService } from "src/app/campaigns/campaign-template/service/template-campaign.service";
import { CONSTANTS } from "src/app/shared/campaigns.constants";
import { TemplateService } from "../../Helpers/templates.service";

@Component({
    selector: 'app-create-new-template-message',
    templateUrl: './create-new-template-message.component.html',
    styleUrls: ['./create-new-template-message.component.css']
})
export class CreateNewTemplateMessageComponent implements OnInit {
    @Input() templateMsg: any;
    @Input() apiIsStaticFlag: any;
    @Input() SelectedMsg :any;

    @Input() entityId: any;
    @Input() fileUploaded: any;


    public campaignMsgInfoTxt = CONSTANTS.INFO_TXT.campaignMessage;

    public campaignLangInfoTxt = CONSTANTS.INFO_TXT.campaignLanguage;

    public noUnicodeTxt = CONSTANTS.ERROR_DISPLAY.noUnicodeText;

    public colBasedInfoTxt = CONSTANTS.INFO_TXT.templateColBased;

    public indexBasedInfoTxt = CONSTANTS.INFO_TXT.templateIndexBased;

    msgInfoText = "";

    type = "column";

    subscriptionFile: Subscription;

    showFileContents = false;

    dir = "ltr";

    openClearModal = false;

    source = "message";

    isText = true;

    isUnicode: any;

    totalCharCount = 0;

    Unicode: boolean;

    newLineCount = 0;

    partsCount = 0;

    method: any;

    checking: boolean;

    public newTemplateMsgFormGroup: any;

    regExHeadersFromMsg = CONSTANTS.MSG_CURLY_REMOVE_PATTERN;


    @Output() templateMsgChg = new EventEmitter<string>();

    @Output() templateLangType = new EventEmitter<string>();


    langType: string;



    variable: string;

    constructor(
        private templateSvc: TemplateCampaignService,
        private tempservice: TemplateService,
        private controlContainer: ControlContainer
    ) { }

    ngOnChanges(changes: SimpleChanges): void {
        if (!this.fileUploaded) {
            this.showFileContents = false;

        }
        this.newTemplateMsgFormGroup = this.controlContainer.control;

        if (this.templateMsg) {
            this.msgpartcount(this.templateMsg);
        }
        if (this.newTemplateMsgFormGroup) {
            this.newTemplateMsgFormGroup.controls.newmessage.setValue(this.templateMsg);

        }
       // console.log(this.templateMsg);
        
        this.staticCheck();

        // this.newTemplateMsgFormGroup.get('newmessage').valueChanges.
        // subscribe((myvalue: string) => {

        //    const pattern = this.regExHeadersFromMsg;

        //     let replacevalue = myvalue.replace(pattern, "");

        //    const variable = this.templateMsg.replace(pattern, "")
        //     console.log(myvalue,this.templateMsg);

        //     if (myvalue !== this.templateMsg) {
        //         this.checking = true;

        //     }else{
        //         this.checking = false;
        //     }
        //     if (variable === replacevalue){
        //         this.checking = false;
        //     }
        // })
        // this.dlt.valueChanges.pipe().subscribe((res) => {
        //     this.checking = false;
        // })

    }


prevMessageValue :any;

    ngOnInit(): void {

        this.msgInfoText = this.colBasedInfoTxt;

        this.newTemplateMsgFormGroup.controls.newmessage.setValue(this.templateMsg);
        this.prevMessageValue = this.templateMsg;

        this.subscriptionFile = this.templateSvc
            .getBasedOn()
            .subscribe((type) => {

                if (type === "column") {
                    this.msgInfoText = this.colBasedInfoTxt;
                    this.type = "column";
                } else if (type === "index") {
                    this.msgInfoText = this.indexBasedInfoTxt;
                    this.type = "index";
                }
            });

            this.newTemplateMsgFormGroup.get('newmessage').valueChanges.
            subscribe((myvalue: string) => {    
                const pattern = this.regExHeadersFromMsg;
                let oldValueReplaced = this.SelectedMsg.replace(pattern, "");
                let newValueReplaced = myvalue.replace(pattern, "");

              //  let replacevalue = myvalue.replace(pattern, "");
            
              console.log(oldValueReplaced,'old value');
              console.log(newValueReplaced,'new value');

                if (newValueReplaced !== oldValueReplaced) {
                    this.checking = true;
                }else{
                    this.checking = false;
                }
            })
            this.dlt.valueChanges.pipe().subscribe((res) => {
                console.log('inside dlt');
                
                this.checking = false;
            })
    }

    get newmessage() {
        return this.newTemplateMsgFormGroup.controls.newmessage;
    }

    get language() {
        return this.newTemplateMsgFormGroup.controls.language;
    }
    get dlt() {
        return this.newTemplateMsgFormGroup.controls.dlt;
    }

    openModal() {

        this.openClearModal = true;
    }

    clearModalResponse(response: string) {
        this.openClearModal = false;
        if (response === "clear") {
            this.Unicode = false;
            this.showFileContents = false;
            this.newTemplateMsgFormGroup.controls.newmessage.setValue("");


            this.newTemplateMsgFormGroup.controls.isStatic.setValue(true)
            this.templateMsgChg.emit("");

        }
    }


    onToggleShow(event: boolean) {
        console.log(5);
        this.showFileContents = event;


    }

    switchToUnicode() {
        this.isText = false;
        // this.isUnicode = true;
        //  this.campaignSvc.switchToUnicode(this.newTemplateMsgFormGroup);
        //this.templateSvc.populateNewTemplateMsgType("unicode");
    }
    switchToText() {
        this.isText = true;
        // this.isUnicode = false;
        this.dir = "ltr";
        //  this.campaignSvc.switchToText(this.newTemplateMsgFormGroup);
        //this.templateSvc.populateNewTemplateMsgType("english");
    }

    onKeyPress(event: any) {
        const fieldValue = event.target.value;
        //this.msgpartcount(event.target.value);
        this.staticCheck();
        this.newTemplateMsgFormGroup.controls.newmessage.setValue(fieldValue);
        if (
            this.isText &&
            escape(fieldValue.charAt(fieldValue.length - 1)).startsWith("%u")
        ) {
            this.templateMsgChg.emit(fieldValue);
        } else {
            this.templateMsgChg.emit(fieldValue);


        }
        this.totalCharCount = fieldValue.length;
    }

    onBlurEvent(event: any) {
        const fieldValue = event.target.value;
        // this.msgpartcount(event.target.value);
        for (let i = 0; i < fieldValue.length; i++) {
            if (this.isText && escape(fieldValue.charAt(i)).startsWith("%u")) {
                this.templateMsgChg.emit(fieldValue);
            } else {
                this.newTemplateMsgFormGroup.controls.newmessage.setValue(
                    fieldValue
                );
                //this.templateSvc.populateNewTemplateMsg(fieldValue);
                this.templateMsgChg.emit(fieldValue);
            }
        }
        this.totalCharCount = fieldValue.length;
    }

    onItemChange(event: any) {
        if (event.target.value === "Arabic") {
            this.dir = "rtl";
        }
    }

    onHeaderClick(header: string) {
        const msgTextArea = document.getElementById(
            "newmessage"
        ) as HTMLInputElement;
        msgTextArea.focus();
        const modifiedVal = this.templateSvc.onHeaderClick(header, msgTextArea, this.newTemplateMsgFormGroup);
        //this.templateSvc.populateNewTemplateMsg(msgTextArea.value);
        this.templateMsgChg.emit(modifiedVal);
        this.totalCharCount = msgTextArea.value.length;
    }


    msgpartcount(event: string) {



        let msgValue = event.trim();

        this.staticCheck();

        const patternMatch = this.regExHeadersFromMsg;

        let replacevalue = msgValue.replace(patternMatch, "");

        let finalMsgValue = replacevalue.trim();

        if (finalMsgValue.length > 0) {

            this.tempservice
                .getMsgPartsInfo(finalMsgValue)
                .subscribe(
                    res => {
                        if (res.statusCode > 299 || res.statusCode < 200) {

                        } else {
                            if (res.isUnicode == true) {
                                this.Unicode = true;
                                this.templateLangType.emit('Unicode');

                            } else {
                                this.Unicode = false;
                                this.templateLangType.emit('Text');
                            }
                        }
                    },
                    (err) => {

                        return err;
                    }

                );
        }



    }
    focus() {
        const focus = document.getElementById("newmessage") as HTMLImageElement;
        focus.focus();
    }

    ngOnDestroy() {
        if (this.subscriptionFile)
            this.subscriptionFile.unsubscribe();
    }
    toggleFileContent() {
        this.showFileContents = !this.showFileContents;
    }

    staticCheck() {
        if (this.msg.match(this.regExHeadersFromMsg)) {
            this.newTemplateMsgFormGroup.controls.isStatic.setValue(false)

        }
        else {

            this.newTemplateMsgFormGroup.controls.isStatic.setValue(true)
        }


    }



    get msgType() {

        return this.newTemplateMsgFormGroup.controls.isStatic.value

    }

    get msg() {
        return this.newTemplateMsgFormGroup.controls.newmessage.value
    }

}
