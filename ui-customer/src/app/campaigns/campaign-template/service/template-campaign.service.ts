import { HttpClient } from "@angular/common/http";
import { Injectable, Output,EventEmitter } from "@angular/core";
import { FormGroup } from "@angular/forms";
import { BehaviorSubject, throwError, Observable, Subject } from "rxjs";
import { catchError, map } from "rxjs/operators";
import { CONSTANTS } from "src/app/shared/campaigns.constants";
import { CONSTANTS_URL } from "src/app/shared/compaign.url";
import { DLTs } from "src/app/shared/model/DLTs.model";
import { Templates } from "../../../shared/model/templates.model";
import { Validators } from '@angular/forms';
import { maxLengthValidator } from 'src/app/campaigns/validators/maxlength-validator';
import { minLengthValidator } from 'src/app/campaigns/validators/minlength-validator';
import { CampaignTemplateComponent } from "src/app/campaigns/campaign-template/campaign-template.component";

@Injectable()
export class TemplateCampaignService {
    templateErr = false;
    headerStartPattern = CONSTANTS.TEMPL_MSG_START_PATTERN;
    headerEndPattern = CONSTANTS.TEMPL_MSG_END_PATTERN;
    regExHeadersFromMsg = CONSTANTS.TEMPLATE_MSG_CURLY_PATTERN;
    BASE_URL = CONSTANTS_URL.GLOBAL_URL;

    DLT_API_URL = CONSTANTS_URL.ALL_DLT_TEMPLATES;

    TEMPLATE_API_URL = CONSTANTS_URL.TEMPLATES_CAMPAIGN;

    DLT_URL: string = this.BASE_URL + this.DLT_API_URL;

    ALL_TEMPLATES_URL: string = this.BASE_URL + this.TEMPLATE_API_URL;

    SelectDlt_TEMPLATES = this.BASE_URL + CONSTANTS_URL.DLT_TEMPLATE_CAMPAIGN;

    Dlt_TemplateForCreateTemplate = this.BASE_URL + CONSTANTS_URL.TEMPLATES_API_DLTTEMPLATE_URL;

    templatesArray: Templates[] = [];

    dltTemplatesArray: DLTs[] = [];

    pattern_validation = CONSTANTS.pattern_validation;

    minLength = CONSTANTS.minLengthCampaignName;

    maxLength = CONSTANTS.maxLengthCampaignName;


    DLTArray: DLTs[] = [];

    campaignTemplateComp: CampaignTemplateComponent;

    @Output() EmitFocus = new EventEmitter;
    // selectedTemplate = -1;

    private subjectToggle = new Subject();

    private chosenTemplateType = new BehaviorSubject<string>("none");

    private newTemplateBasedOn = new Subject<string>();

    private chosenDLT = new Subject<string>();

    fileError: boolean;

    basedOn: any;

    firstFileDetails: any;

    saveTemplate = true;

    newTemplateCreated: Templates = {
        id: "",
        t_name: "",
        t_type: "",
        t_mobile_column: "",
        dlt_entity_id: "",
        dlt_template_id: "",
        t_lang_type: "",
        t_lang: "",
        t_content: "",
        created_ts: new Date(),
        modified_ts: new Date(),
        created_ts_unix: 0,
        modified_ts_unix: 0,
        enabled: true,
        info: "",
    };

    partialNewTemplate: Templates = {
        id: "",
        t_name: "",
        t_type: "",
        t_mobile_column: "",
        dlt_entity_id: "",
        dlt_template_id: "",
        t_lang_type: "",
        t_lang: "",
        t_content: "",
        created_ts: new Date(),
        modified_ts: new Date(),
        created_ts_unix: 0,
        modified_ts_unix: 0,
        enabled: true,
        info: "",
    };

    public loadingTemplateNames$ = new BehaviorSubject<boolean>(false);
    public loadingDltTemplateNames$ = new BehaviorSubject<boolean>(false);
    public noMatch = new BehaviorSubject<boolean>(false);


    selectedDltTemplate: any;

    selectedTemplate: any;

    sendTemplateType(theType: string) {
        this.chosenTemplateType.next(theType);
    }

    getTemplateType() {
        return this.chosenTemplateType.asObservable();
    }
   
    sendBasedOn(theType: string) {
                
        this.basedOn = theType;
        this.newTemplateBasedOn.next(theType);
    }

    setCampaignTemplateControl(control: CampaignTemplateComponent) {
        this.campaignTemplateComp = control;
    }

    getBasedOn() {
        return this.newTemplateBasedOn.asObservable();
    }

    sendDLTselected(theType: string) {
        this.chosenDLT.next(theType);
    }

    getselectedDLT() {
        return this.chosenDLT.asObservable();
    }

    setFileError(status: boolean) {
        this.fileError = status;
    }

    getFileError() {
        return this.fileError;
    }

    sendToggleMsg() {
        this.subjectToggle.next();
    }

    getToggleMsg(): Observable<any> {
        return this.subjectToggle.asObservable();
    }

    populateNewTemplateBasedOn(val: string) {

        this.partialNewTemplate.t_type = val;
    }

    populateNewTemplateMobileCol(val: string) {

        this.partialNewTemplate.t_mobile_column = val;
    }

    populateNewTemplateDLT(val: string) {
        this.partialNewTemplate.dlt_template_id = val;
    }

    populateNewTemplateMsgType(val: string) {
        this.partialNewTemplate.t_lang_type = val;
    }

    populateNewTemplateMsg(val: string) {
        this.partialNewTemplate.t_content = val;

    }

    populateNewTemplateName(val: string) {
        this.partialNewTemplate.t_name = val;
    }

    populateNewTemplateLanguage(val: string) {
        this.partialNewTemplate.t_lang = val;
    }

    populateCompleteNewTemplate(val: Templates) {
        this.newTemplateCreated = val;
    }

    getNewTemplate() {
        return this.partialNewTemplate;
    }

    getCreatedTemplate() {
        return this.newTemplateCreated;
    }

    populateFileDetails(fileDetails: any) {

        this.firstFileDetails = fileDetails;
       /* if(this.firstFileDetails.file_contents_column){
            if(this.firstFileDetails.file_contents_column.length == 1){
                const fileHeaders = this.firstFileDetails.file_contents_column[0];
                var str = Array(fileHeaders.length).fill("");
                this.firstFileDetails.file_contents_column.push(str);
                   
            }
        } */
       
    }

    getFirstFileDetails() {
        return this.firstFileDetails;
    }

    constructor(private http: HttpClient) { }

    badError: any;

    findAllTemplates(id: any,ctype:any,entityid:any) {
        this.loadingTemplateNames$.next(true);
        return this.http.get<Templates[]>(this.ALL_TEMPLATES_URL + id + '&c_type='  + ctype + '&header=' + entityid)
            .pipe(
                map((responseData) => {
                    this.loadingTemplateNames$.next(false);
                    this.templatesArray = [];
                    for (const key in responseData) {
                        if (
                            Object.prototype.hasOwnProperty.call(responseData, key)
                        ) {
                            this.templatesArray.push({
                                ...responseData[key]
                            });
                        }
                    }

                    return this.templatesArray;
                }),
                catchError((err) => {
                    this.loadingTemplateNames$.next(false);
                    if (err.status == 0) {

                        let setError = {
                            message: "Template Request Failed Please Click Try-again.",
                            statusCode: "500",
                            error: "Template Request Failed Please Click Try-again."
                        }
                        this.badError = setError;
                    } else {
                        this.badError = err.error;
                    }

                    return throwError(err);
                })
            );
    }

    public focusemit = new BehaviorSubject<boolean>(false);

    setErrorToMobileColumn(focus:boolean){
        this.focusemit.next(focus); 
    }

    // findTotalDltTemplates(id: any) {
    //     this.loadingDltTemplateNames$.next(true);


    //     return this.http.get<DLTs[]>(this.templateDLT_URL + id).pipe(
    //         map((responseData) => {


    //             this.loadingDltTemplateNames$.next(false);
    //             this.dltTemplatesArray = [];
    //             for (const key in responseData) {
    //                 if (
    //                     Object.prototype.hasOwnProperty.call(responseData, key)
    //                 ) {
    //                     this.dltTemplatesArray.push({
    //                         ...responseData[key]
    //                     });
    //                 }
    //             }

    //             return this.dltTemplatesArray;
    //         }),
    //         catchError((err) => {
    //             this.loadingDltTemplateNames$.next(false);
    //             if (err.status == 0) {

    //                 let setError = {
    //                     message: "Template Request Failed Please Click Try-again.",
    //                     statusCode: "500",
    //                     error: "Template Request Failed Please Click Try-again."
    //                 }
    //                 this.badError = setError;
    //             } else {
    //                 this.badError = err.error;
    //             }

    //             return throwError(err);
    //         })
    //     );
    // }

    findAllDltTemplates(id: any,ctype:any) {
        this.loadingDltTemplateNames$.next(true);
    const DLTEndPoint = ctype === 'createTemp' ? this.Dlt_TemplateForCreateTemplate + id  : this.SelectDlt_TEMPLATES + id + '&c_type=' + ctype;
     

        return this.http.get<DLTs[]>(DLTEndPoint).pipe(
            map((responseData) => {


                this.loadingDltTemplateNames$.next(false);
                this.dltTemplatesArray = [];
                for (const key in responseData) {
                    if (
                        Object.prototype.hasOwnProperty.call(responseData, key)
                    ) {
                        this.dltTemplatesArray.push({
                            ...responseData[key]
                        });
                    }
                }

                return this.dltTemplatesArray;
            }),
            catchError((err) => {
                this.loadingDltTemplateNames$.next(false);

                return throwError(err);
            })
        );
    }

    findAllDLTs() {
        return this.http.get<DLTs[]>(this.DLT_URL).pipe(
            map((responseData) => {


                this.DLTArray = [];
                for (const key in responseData) {
                    if (
                        Object.prototype.hasOwnProperty.call(responseData, key)
                    ) {
                        this.DLTArray.push({
                            ...responseData[key]
                        });
                    }
                }

                return this.DLTArray;
            }),
            catchError((err) => {

                return throwError(err);
            })
        );
    }

    getDLTMsg(dlt: string) {
        const particularDLT = this.DLTArray.find(
            (value) => value.dltId === dlt
        );
        return particularDLT?.message;
    }

    populateSelectedTemplate(value: any) {
        this.selectedTemplate = value;
    }

    populateSelectedDltTemplate(value: any) {
        this.selectedDltTemplate = value;


    }

    getSelectedTemplate() {
        return this.selectedTemplate;
    }

    getSelectedDltTemplate() {
        return this.selectedDltTemplate;

    }

    CTMessageforPreview(theTemplateMsg: string) {
        const message = theTemplateMsg;
        const fileDetails = this.getFirstFileDetails();

        let returnMsg = message;
        let warning = false;
        let isStatic = false;
        let parameters: string[] = [];
        let parametersMixedCase: string[] = [];
        const patternMatch = this.regExHeadersFromMsg;

        if (fileDetails.isStatic) {
            isStatic = fileDetails.isStatic;
        }


        parametersMixedCase = message.match(patternMatch);

        if (parametersMixedCase != null && parametersMixedCase.length > 0) {
            parameters = parametersMixedCase.map((item) => {
                return item.toLowerCase();
            });
        }
        if (parameters.length > 0) {
            for (let i = 0; i < parameters.length; i++) {

                const param = parameters[i];
                const placeHolderValue = fileDetails.placeholders[param];

                if (placeHolderValue === undefined || placeHolderValue.length <= 0) {

                    warning = true;

                } else if (placeHolderValue !== undefined && placeHolderValue.length > 0) {

                    const replaceStr = `${this.headerStartPattern}${parametersMixedCase[i]}${this.headerEndPattern}`;

                    if (returnMsg === "") {
                        returnMsg = message.replace(replaceStr, placeHolderValue);


                    } else {
                        returnMsg = returnMsg.replace(replaceStr, placeHolderValue);

                    }
                }
            }
        } else {

            returnMsg = message;
        }

        return { newMsg: returnMsg, warning, isStatic };


    }
    messageForPreview(theTemplateMsg: string, indexOrColumn: string) {

        const message = theTemplateMsg;

        const fileDetails = this.getFirstFileDetails();

        let returnMsg = message;
        let warning = false;
        let parameters: string[] = [];
        let parametersMixedCase: string[] = [];
        const patternMatch = this.regExHeadersFromMsg;


        parametersMixedCase = message.match(patternMatch);
        if (parametersMixedCase != null && parametersMixedCase.length > 0) {
            parameters = parametersMixedCase.map((item) => {
                return item.toLowerCase();
            });
        }
        let headers: string[];

        let index: number;
        let datas: any;
        if (indexOrColumn === "column") {
            datas = fileDetails.file_contents_column[1];

        } else {
            datas = fileDetails.file_contents_index[1];
        }

        const data = datas

        if (indexOrColumn === "column") {
            headers = fileDetails.file_contents_column[0].map(v => v.toLowerCase());


        } else {
            headers = fileDetails.file_contents_index[0].map(v => v.toLowerCase());
        }

console.log("headers", headers)
        if (parameters.length > 0) {
            for (let i = 0; i < parameters.length; i++) {


                index = headers.indexOf(parameters[i]);
                if (index === -1) {

                    warning = true;

                } else {
                    const replaceStr = `${this.headerStartPattern}${parametersMixedCase[i]}${this.headerEndPattern}`;

                    if (returnMsg === "") {
                        returnMsg = message.replace(replaceStr, data[index]);


                    } else {
                        returnMsg = returnMsg.replace(replaceStr, data[index]);
                    }
                }
            }
        } else {
            returnMsg = message;
        }
        return { newMsg: returnMsg, warning };
    }


    onHeaderClick(
        header: string,
        msgTextArea: HTMLInputElement,
        currentForm: FormGroup
    ) {

        msgTextArea.focus();
        const val = msgTextArea.value;
        const endPosition = msgTextArea.selectionEnd || 0;
        msgTextArea.value = `${val.slice(
            0,
            endPosition
        )}${this.headerStartPattern}${header}${this.headerEndPattern}${val.slice(endPosition)}`;
        currentForm.controls.newmessage.setValue(msgTextArea.value);
        this.populateNewTemplateMsg(msgTextArea.value);
        return msgTextArea.value;
    }

    nullifyTemplates() {
        this.templatesArray = [];
        this.dltTemplatesArray = [];
    }

    setValidatorsToNameFields(formgroup : any){

        
          formgroup.controls.templateName.setValidators([
            Validators.required,
            minLengthValidator(this.minLength),
            maxLengthValidator(this.maxLength),
            Validators.pattern(this.pattern_validation)
        ]);
       
     }

     setValidatorsToColumnNameFields(formgroup : any,set:boolean){

        if (set) {
            formgroup.controls.mobileColumn.setValidators([
                Validators.required,
              //   minLengthValidator(this.minLength),
              //   maxLengthValidator(this.maxLength),
              //   Validators.pattern(this.pattern_validation)
            ]);
        } else {
            formgroup.controls.mobileColumn.clearValidators();
            formgroup.controls.mobileColumn.updateValueAndValidity();
        }
      
     
   }
    //  setOrClearValidatorsTemplateName(saveChecked: boolean) {

    //     if (saveChecked) {

    //         console.log("setting valida");

    //         this.newTypeTemplateFormGroup.controls.templateName.setValidators([

    //             Validators.required,

    //             minLengthValidator(this.minLength),

    //             maxLengthValidator(this.maxLength),

    //             Validators.pattern(this.pattern_validation)

    //         ]);



    //         this.newTypeTemplateFormGroup.controls.templateName.updateValueAndValidity();

    //     } else {

    //         console.log("removing valida");

         

    //         this.newTypeTemplateFormGroup.controls.templateName.clearValidators();

    //         this.newTypeTemplateFormGroup.controls.templateName.updateValueAndValidity();

    //     }

    // }
}
