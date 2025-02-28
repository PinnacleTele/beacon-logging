import { HttpClient, HttpHeaders } from "@angular/common/http";
import { Injectable, ViewChild } from "@angular/core";
import { FormControl, FormGroup, Validators } from "@angular/forms";
import { BehaviorSubject, Observable, Subject, throwError } from "rxjs";
import { catchError, map } from "rxjs/operators";
import { CONSTANTS } from "src/app/shared/campaigns.constants";
import { FileUploaderComponent } from "../shared/file-uploader/file-uploader.component";
import { SingleSelectDropdownComponent } from "../shared/single-select-dropdown/single-select-dropdown.component";
import { FileUploadStatistics } from "./model/campaign-file-statistics";
import { MobileCountStatistics } from "./model/generic-campaign-mobile-statistics";
import { MessageDetails } from "./model/generic-campaign-msg-details";
import { GenericCampaign } from "./model/generic_campaign.model";
import { GroupModel } from "./model/campaign-group-model";
import { AddGroupComponent } from "./groups/add-group/add-group.component";
import { CONSTANTS_URL } from "../shared/compaign.url";
import { EntityId } from "./model/campaign-entityId-model";
import { SenderId } from "./model/campaign-senderId-model";
import { ERROR } from "./error.data";
import { CampaignNameComponent } from "src/app/campaigns/campaign-name/campaign-name.component";
import { EntityIdComponent } from "src/app/shared/entity-id/entity-id.component";
import { CampaignMessageComponent } from "src/app/campaigns/campaign-message/campaign-message.component";
import { CampaignSenderIdComponent } from "src/app/campaigns/campaign-sender-id/campaign-sender-id.component";
import { CampaignNameTemplateComponent } from "./campaign-template/campaign-name-template/campaign-name-template.component";
import { LocalStorageService } from "../authentication/local-storage.service";
import { QuickSmsMobileNumbersComponent } from "src/app/campaigns/quick-sms/quick-sms-mobile-numbers/quick-sms-mobile-numbers.component";
import { IntlSendersComponent } from "./intl-senders/intl-senders.component";
import { TemplateMessageComponent } from "./campaign-template/template-message/template-message.component";
import { TemplateNameComponent } from "../shared/components/template-name/template-name.component";
import { TemplateCampaignService } from "./campaign-template/service/template-campaign.service";
@Injectable({
    providedIn: "root"
})
export class CampaignsService {

    campaignTempErr = false;
    campaignTempApiErr = false;

    BASE_URL = CONSTANTS_URL.GLOBAL_URL;

    API_URL = CONSTANTS_URL.OTM_SEND_COMPAIGN;

    genericCampaign: GenericCampaign;

    CQ = CONSTANTS.QUICK_SMS_SHORT_FORM;

    DOMESTIC = CONSTANTS.DOMESTIC;

    message = "";

    newLineCount = 0;

    charCount = 0;

    partsCount = 0;

    duplicateChk = true;

    openQA = new BehaviorSubject<boolean>(false);

    closeCampaign = new Subject<boolean>();

    messageDetails: MessageDetails = {
        msgContent: "",
        newLineCount: 0,
        charCount: 0,
        partsCount: 0
    };
    badError: any;

    telcoVal: any;


    campaignDetailPageContent: any;



    constructor(
        public http: HttpClient,
        private localStorage: LocalStorageService,
        private tempservice : TemplateCampaignService

    ) { }

    httpOptions = {
        headers: new HttpHeaders({
            Accept: "application/json",
            "Content-Type": "application/json"
        })
    };

    senderIdControl: SingleSelectDropdownComponent;

    dropzoneControl: FileUploaderComponent;

    addGroupControl: AddGroupComponent;

    c_name: CampaignNameComponent;
    ct_name: CampaignNameTemplateComponent;

    entityId: EntityIdComponent;

    senderId: CampaignSenderIdComponent;

    c_message: CampaignMessageComponent;

    ct_message: TemplateMessageComponent;

    t_name: CampaignNameTemplateComponent;

    Tname: TemplateNameComponent;

    mobilenum: QuickSmsMobileNumbersComponent;

    intlSenderId: IntlSendersComponent;

    setDropzoneControl(control: FileUploaderComponent) {
        this.dropzoneControl = control;
    }

    setaddGroupControl(control: AddGroupComponent) {
        this.addGroupControl = control;
    }

    setCnameControl(control: CampaignNameComponent) {

        this.c_name = control;
    }

    setCnameCTControl(control: CampaignNameTemplateComponent) {
        this.ct_name = control;
    }


    setentityIDControl(control: EntityIdComponent) {
        this.entityId = control;
    }
    setSenderIdControl(control: CampaignSenderIdComponent) {
        this.senderId = control;
    }

    setMessageControl(control: CampaignMessageComponent) {
        this.c_message = control;
    }
    setTemplateMessageControl(control: TemplateMessageComponent) {
        this.ct_message = control;
    }


    setTelco(value: any) {


        this.telcoVal = value;

    }
    setTemplateNameControl(control: CampaignNameTemplateComponent) {
        this.t_name = control
    }

    setMobileNumControl(control: QuickSmsMobileNumbersComponent) {
        this.mobilenum = control
    }
    setIntlSenderIdControl(control: IntlSendersComponent) {
        this.intlSenderId = control
    }

    setTNameControl(control: TemplateNameComponent) {
        this.Tname = control
    }

    @ViewChild(FileUploaderComponent, { static: false })
    dropzonefocus: FileUploaderComponent;

    @ViewChild(CampaignNameComponent, { static: false })
    name: CampaignNameComponent;

    validateAllFormFields(campaignForm: FormGroup,cType:string) {
        console.log(this.intlSenderId);

        const templateErr = this.tempservice.templateErr
        const formGroup = campaignForm;

 if(cType!="CT"){
    if (this.c_name && campaignForm.controls.name?.invalid) {
        this.c_name.focus();
    }
    else if (this.t_name && campaignForm.controls.name?.invalid) {
        this.t_name.focus();
    }
    else if((this.Tname && campaignForm.controls.templateName?.invalid) || templateErr) {
        this.Tname.focus();
    }
    else if (this.mobilenum && campaignForm.controls.mobileNumbers?.invalid) {

        this.mobilenum.focus();

    }
    else if (this.dropzoneControl && this.dropzoneControl?.succeedFiles.length <= 0) {

        this.dropzoneControl.fileuploadset();

    }
    else if (campaignForm.controls.addedGroups && campaignForm.controls.addedGroups?.invalid) {
        this.addGroupControl.focus();
    }
    //else if (this.entityId && campaignForm.controls.entityId?.invalid) {
        //this.entityId.focus();
    //} 
    else if (this.senderId && campaignForm.controls.senderId?.invalid) {
        this.senderId.setfocus();
    }
    else if (this.c_message && campaignForm.controls.message?.invalid) {
        this.c_message.focus();
    } 
    else if (this.intlSenderId && campaignForm.controls.intlSenderId?.invalid) {


        this.intlSenderId.setfocus();
    }
 }

        Object.keys(formGroup.controls).forEach((field) => {
            const control = formGroup.get(field);
            if (control instanceof FormControl) {
                control.markAsTouched({ onlySelf: true });

            }
        });

        if (this.addGroupControl) {
            this.addGroupControl.validateAddedGroupArea();
        }

        if (this.dropzoneControl) {

            this.dropzoneControl.validateDropzoneArea();
        }

        // campaignForm.reset()
    }

    focusCampaignName(campaignForm: FormGroup) {


        const formGroup = campaignForm;

        if (this.ct_name && (this.campaignTempErr || this.campaignTempApiErr)) {

            this.ct_name.focus();

        }
    }

    populateDuplicateChk(isChecked: boolean) {
        this.duplicateChk = isChecked;
    }

    getDuplicateChk() {
        return this.duplicateChk;
    }

    formulateMessageDetails() {
        return this.messageDetails;
    }

    populateMessageDetails(
        fieldValue: any,
        totalCharCount: number,
        newLineCount: number,
        partsCount: number
    ) {

        this.messageDetails = new MessageDetails(
            fieldValue,
            newLineCount,
            totalCharCount,
            partsCount
        );
    }

    getMessageDetails() {
        return this.messageDetails;
    }

    initializeMobileNumbers() {
        const mobileNumbers: MobileCountStatistics = {
            mobileNumbers: [],
            uniqueNumbers: [],
            invalidNumbers: [],
            validCount: 0,
            invalidCount: 0,
            totalCount: 0,
            uniqueCount: 0
        };
        return mobileNumbers;
    }

    extractFileDetails(files: any): FileUploadStatistics {
        const uploadedFiles: string[] = [];
        const storedFiles: string[] = [];
        let totalSize = 0;
        let totalCount = 0;

        for (const file of files) {
            uploadedFiles.push(file.originalname);
            storedFiles.push(file.r_filename);
            totalSize += file.numericSize;
            totalCount += parseInt(file.total);
        }
        return new FileUploadStatistics(
            uploadedFiles,
            storedFiles,
            totalCount,
            totalSize
        );
    }

    selectedGroups: GroupModel[] = [];

    excludedGroups: GroupModel[] = [];

    switchToUnicode(currentForm: FormGroup) {
        currentForm.controls.language.setValidators(Validators.required);
        currentForm.controls.language.updateValueAndValidity();
    }

    switchToText(currentForm: FormGroup) {

        currentForm.controls.language.clearValidators();
        currentForm.controls.language.updateValueAndValidity();
    }

    selectedTemplateInfo: any;
    setSelectedTemplateInfo(template: any){
        this.selectedTemplateInfo = template;
    }
    getSelectedTemplateInfo(){
        return this.selectedTemplateInfo;
    }

    public loadingentityIds = new BehaviorSubject<boolean>(false);
    entity_id_URL = "/cm/campaigns/entityids";
    // entity_id_URL = "/cm/campaigns/entityi";

    entityIdArray: EntityId[] = [];


    findAllentityIds() {
        this.loadingentityIds.next(true)
        return this.http
            .get<EntityId[]>(this.BASE_URL + this.entity_id_URL)
            .pipe(
                map((responseData) => {
                    this.loadingentityIds.next(false)
                    this.entityIdArray = [];
                    for (const key in responseData) {
                        if (
                            Object.prototype.hasOwnProperty.call(
                                responseData,
                                key
                            )
                        ) {
                            this.entityIdArray.push({
                                ...responseData[key]
                            });
                        }
                    }

                    return responseData;
                }),
                catchError((err) => {
                    if (err.status == 0) {
                        let setError = ERROR.ENTITY_ID_ERROR
                        this.badError = setError;
                    } else {
                        this.badError = err.error;
                    }
                    this.loadingentityIds.next(false)
                    return throwError(err);
                })
            );
    }




    public loadingsenderIds = new BehaviorSubject<boolean>(false);

    senderId_URL = this.BASE_URL + "/cm/campaigns/senderids";
    IntlsenderId_URL = this.BASE_URL + CONSTANTS_URL.INTL_SENDERID_FORCAMPAIGNS;

    senderIdArray: SenderId[] = [];

    senderIdEndpoint = "";
    findAllintlSenders() {
        return this.http
            .get<SenderId[]>(
                this.IntlsenderId_URL
            )
            .pipe(
                map((responseData: any) => {
                    this.loadingsenderIds.next(false)
                    this.senderIdArray = [];
                    return responseData as any;
                }),
                catchError((err) => {
                    this.loadingsenderIds.next(false)
                    if (err.status == 0) {

                        let setError = ERROR.SENDER_ID_ERROR;
                        this.badError = setError;
                    } else {
                        this.badError = err.error;
                    }
                    return this.badError;
                    // return throwError(err);
                })
            );

    }

    findAllsenderIds( templateId: string) {
        this.loadingsenderIds.next(true)
        //console.log(templateId);

        this.senderIdEndpoint = templateId ? this.senderId_URL  + "&dlt_template_id=" + templateId : this.senderId_URL 

        return this.http
            .get<SenderId[]>(
                this.senderIdEndpoint
            )
            .pipe(
                map((responseData: any) => {
                    this.loadingsenderIds.next(false)
                    this.senderIdArray = [];
                    return responseData as any;
                }),
                catchError((err) => {
                    this.loadingsenderIds.next(false)
                    if (err.status == 0) {

                        let setError = ERROR.SENDER_ID_ERROR;
                        this.badError = setError;
                    } else {
                        this.badError = err.error;
                    }
                    return this.badError;
                    // return throwError(err);
                })
            );

    }

    // send campaign requests

    cOTM_Url = CONSTANTS_URL.CAMPAIGN_COTM_SEND;

    sendOtmCampaign(value: any): Observable<any> {

        return this.http
            .post(
                this.BASE_URL + this.cOTM_Url,
                value,
                this.httpOptions
            )
            .pipe(
                map((responseData: any) => {
                    //console.log(responseData);

                    return responseData as any;
                }),
                catchError((err) => {
                    // console.log('inside catch error', err);

                    return this.errorHandling(err);
                    // return throwError(err);
                })
            );
    }

    cMTM_Url = CONSTANTS_URL.CAMPAIGN_CMTM_SEND;
    sendMtmCampaign(value: any): Observable<any> {

        return this.http
            .post(
                this.BASE_URL + this.cMTM_Url,
                value,
                this.httpOptions
            )
            .pipe(
                map((responseData: any) => {
                    return responseData as any;
                }),
                catchError((err) => {

                    return this.errorHandling(err);
                })
            );
    }


    cT_Url = CONSTANTS_URL.CAMPAIGN_CTSEND;
    sendCtCampaign(value: any): Observable<any> {

        return this.http
            .post(
                this.BASE_URL + this.cT_Url,
                value,
                this.httpOptions
            )
            .pipe(
                map((responseData: any) => {
                    return responseData as any;
                }),
                catchError((err) => {

                    return this.errorHandling(err);
                    // return throwError(err);
                })
            );
    }


    cG_Url = CONSTANTS_URL.CAMPAIGN_CGSEND;
    sendCgCampaign(value: any): Observable<any> {


        return this.http
            .post(
                this.BASE_URL + this.cG_Url,
                value,
                this.httpOptions
            )
            .pipe(
                map((responseData: any) => {

                    return responseData as any;
                }),
                catchError((err) => {


                    return this.errorHandling(err);
                    // return throwError(err);
                })
            );
    }

    cq_Url = CONSTANTS_URL.CAMPAIGN_CQSEND;
    sendCqCampaign(value: any): Observable<any> {

        return this.http
            .post(
                this.BASE_URL + this.cq_Url,
                value,
                this.httpOptions
            )
            .pipe(
                map((responseData: any) => {

                    return responseData as any;
                }),
                catchError((err) => {
                    return this.errorHandling(err);
                    // return throwError(err);
                })
            );
    }


    // schedule campaign api request

    S_MTM_Url = CONSTANTS_URL.CAMPAIGN_S_MTM_SEND;

    scheduleMtmCampaign(value: any): Observable<any> {

        return this.http
            .post(
                this.BASE_URL + this.S_MTM_Url,
                value,
                this.httpOptions
            )
            .pipe(
                map((responseData: any) => {

                    return responseData as any;
                }),
                catchError((err) => {

                    return this.errorHandling(err);;
                    // return throwError(err);
                })
            );
    }

    S_OTM_Url = CONSTANTS_URL.CAMPAIGN_S_OTM_SEND;

    scheduleOtmCampaign(value: any): Observable<any> {

        return this.http
            .post(
                this.BASE_URL + this.S_OTM_Url,
                value,
                this.httpOptions
            )
            .pipe(
                map((responseData: any) => {

                    return responseData as any;
                }),
                catchError((err) => {

                    return this.errorHandling(err);
                    // return throwError(err);
                })
            );
    }

    S_CQ_Url = CONSTANTS_URL.CAMPAIGN_S_CQ_SEND;

    scheduleCqCampaign(value: any): Observable<any> {

        return this.http
            .post(
                this.BASE_URL + this.S_CQ_Url,
                value,
                this.httpOptions
            )
            .pipe(
                map((responseData: any) => {

                    return responseData as any;
                }),
                catchError((err) => {

                    return this.errorHandling(err);;
                    // return throwError(err);
                })
            );
    }

    S_CG_Url = CONSTANTS_URL.CAMPAIGN_S_CG_SEND;

    scheduleCgCampaign(value: any): Observable<any> {


        return this.http
            .post(
                this.BASE_URL + this.S_CG_Url,
                value,
                this.httpOptions
            )
            .pipe(
                map((responseData: any) => {

                    return responseData as any;
                }),
                catchError((err) => {

                    if (err.status == 0) {

                        let setError = ERROR.REQUEST_NOT_SEND;

                        this.badError = setError;
                    } else {
                        let setError = ERROR.SOMETHING_WENT_WRONG;
                        this.badError = setError;
                    }

                    return this.badError;
                    // return throwError(err);
                })
            );
    }

    S_CT_Url = CONSTANTS_URL.CAMPAIGN_S_CT_SEND;

    scheduleCtCampaign(value: any): Observable<any> {

        return this.http
            .post(
                this.BASE_URL + this.S_CT_Url,
                value,
                this.httpOptions
            )
            .pipe(
                map((responseData: any) => {

                    return responseData as any;
                }),
                catchError((err) => {

                    if (err.status == 0) {

                        let setError = ERROR.REQUEST_NOT_SEND;

                        this.badError = setError;
                    } else {
                        let setError = ERROR.SOMETHING_WENT_WRONG;
                        this.badError = setError;
                    }
                    return this.badError;
                    // return throwError(err);
                })
            );
    }
    public loadingC_List = new BehaviorSubject<boolean>(false);

    campaignList(type): Observable<any> {
        this.loadingC_List.next(true)
        return this.http
            .post(
                this.BASE_URL + CONSTANTS_URL.CAMPAIGN_LIST, type
            )
            .pipe(
                map((responseData: any) => {

                    this.loadingC_List.next(false)
                    return responseData as any;
                }),
                catchError((err) => {
                    this.loadingC_List.next(false)
                    if (err.status == 0) {

                        let setError = ERROR.REQUEST_NOT_SEND;

                        this.badError = setError;
                    } else {


                        let setError = ERROR.SOMETHING_WENT_WRONG;
                        this.badError = setError;
                    }
                    return this.badError;
                    // return throwError(err);
                })
            );
    }
    public loadingS_List = new BehaviorSubject<boolean>(false);
    campaignListStats(): Observable<any> {
        this.loadingS_List.next(true)
        return this.http
            .get(
                this.BASE_URL + CONSTANTS_URL.CAMPAIGN_LIST_STATS
            )
            .pipe(
                map((responseData: any) => {

                    this.loadingS_List.next(false)
                    return responseData as any;
                }),
                catchError((err) => {
                    this.loadingS_List.next(false)
                    if (err.status == 0) {

                        let setError = ERROR.REQUEST_NOT_SEND;

                        this.badError = setError;
                    } else {


                        let setError = ERROR.SOMETHING_WENT_WRONG;
                        this.badError = setError;
                    }
                    return this.badError;
                    // return throwError(err);
                })
            );
    }


    public loadingC_S_List = new BehaviorSubject<boolean>(false);

    campaign_S_List(type): Observable<any> {
        this.loadingC_S_List.next(true)
        return this.http
            .post(
                this.BASE_URL + CONSTANTS_URL.CAMPAIGN_S_LIST, type
            )
            .pipe(
                map((responseData: any) => {

                    this.loadingC_S_List.next(false)
                    return responseData as any;
                }),
                catchError((err) => {
                    this.loadingC_S_List.next(false)
                    if (err.status == 0) {

                        let setError = ERROR.REQUEST_NOT_SEND;

                        this.badError = setError;
                    } else {


                        let setError = ERROR.SOMETHING_WENT_WRONG;
                        this.badError = setError;
                    }
                    return this.badError;
                    // return throwError(err);
                })
            );
    }
    public loadingSC_List = new BehaviorSubject<boolean>(false);
    campaign_S_ListStats(): Observable<any> {
        this.loadingSC_List.next(true)
        return this.http
            .get(
                this.BASE_URL + CONSTANTS_URL.CAMPAIGN_S_LIST_STATS
            )
            .pipe(
                map((responseData: any) => {

                    this.loadingSC_List.next(false)
                    return responseData as any;
                }),
                catchError((err) => {
                    this.loadingSC_List.next(false)
                    if (err.status == 0) {

                        let setError = ERROR.REQUEST_NOT_SEND;

                        this.badError = setError;
                    } else {
                        (err);

                        let setError = ERROR.SOMETHING_WENT_WRONG;
                        this.badError = setError;
                    }
                    return this.badError;
                    // return throwError(err);
                })
            );
    }
    C_DETAIL_URL = CONSTANTS_URL.CAMPAIGN_DETAIL
    campaignDetail(campId: string): Observable<any> {

        return this.http
            .get(
                this.BASE_URL + this.C_DETAIL_URL + campId
            )
            .pipe(
                map((responseData: any) => {

                    return responseData as any;
                }),
                catchError((err) => {

                    if (err.status == 0) {

                        let setError = ERROR.REQUEST_NOT_SEND;

                        this.badError = setError;
                    } else {
                        let setError = ERROR.SOMETHING_WENT_WRONG;
                        this.badError = setError;
                    }
                    return this.badError;
                    // return throwError(err);
                })
            );
    }
    C_FILE_DETAIL = CONSTANTS_URL.CAMPAIGN_FILE_DETAIL
    campaignFileDetail(campId: string): Observable<any> {

        return this.http
            .get(
                this.BASE_URL + this.C_FILE_DETAIL + campId
            )
            .pipe(
                map((responseData: any) => {

                    return responseData as any;
                }),
                catchError((err) => {

                    if (err.status == 0) {

                        let setError = ERROR.REQUEST_NOT_SEND;

                        this.badError = setError;
                    } else {
                        let setError = ERROR.SOMETHING_WENT_WRONG;
                        this.badError = setError;
                    }
                    return this.badError;
                    // return throwError(err);
                })
            );
    }
    scCampaignURL = this.BASE_URL + CONSTANTS_URL.SC_CAMPAIGN_URL
    scCampaignDeatilLoading = new BehaviorSubject<boolean>(false);

    scCampaignDetail(campaignId: string, atId: string) {
        // this.loadingsenderIds.next(true)
        this.scCampaignDeatilLoading.next(true)
        let scCampaignEndpoint = this.scCampaignURL + campaignId + "&at_id=" + atId

        return this.http
            .get(
                scCampaignEndpoint
            )
            .pipe(

                map((responseData: any) => {
                    //  this.loadingsenderIds.next(false)
                    // this.senderIdArray = [];  
                    this.scCampaignDeatilLoading.next(false)
                    return responseData as any;
                }),
                catchError((err) => {
                    //   this.scCampaignDeatilLoading.next(false)
                    //   this.loadingsenderIds.next(false)
                    if (err.status == 0) {

                        let setError = ERROR.SENDER_ID_ERROR;
                        this.badError = setError;
                    } else {
                        this.badError = err.error;
                    }
                    return this.badError;
                    // return throwError(err);
                })
            );

    }
    DELETE_CAMP_URL = this.BASE_URL + CONSTANTS_URL.SC_DELETE_CAMPAIGN_URL
    deleteScCampaign(cId: string, atid: string) {
        let file = {
            "c_id": cId,
            "at_id": atid

        }


        return this.http.post(this.DELETE_CAMP_URL, file).pipe(
            map((responseData) => {

                return responseData

            }),
            catchError((err) => {
                if (err.status == 0) {

                    let setError = ERROR.REQUEST_NOT_SEND;

                    this.badError = setError;
                } else {
                    let setError = ERROR.SOMETHING_WENT_WRONG;
                    this.badError = setError;
                }
                return this.badError;
            })
        )
    }

    reSchedule_URL = this.BASE_URL + CONSTANTS_URL.SC_RESCHEDULE_URL
    campaignReschedule(c_id: string,
        at_id: string,
        scheduled_date: string,
        scheduled_time: string,
        scheduled_zone: string): Observable<any> {
        let payload = {
            "c_id": c_id,
            "at_id": at_id,
            "scheduled_date": scheduled_date,
            "scheduled_time": scheduled_time,
            "scheduled_zone": scheduled_zone
        }



        return this.http
            .post(
                this.reSchedule_URL,
                payload

            )
            .pipe(
                map((responseData: any) => {

                    return responseData as any;
                }),
                catchError((err) => {

                    if (err.status == 0) {

                        let setError = ERROR.REQUEST_NOT_SEND;

                        this.badError = setError;
                    } else {
                        let setError = ERROR.SOMETHING_WENT_WRONG;
                        this.badError = setError;
                    }

                    return this.badError;
                    // return throwError(err);
                })
            );
    }

    campaignProgressStats(campaignId: string) {
        //  this.loadingsenderIds.next(true)
        //console.log(campaignId);

        let campaignProgressEndPoint = CONSTANTS_URL.GLOBAL_URL + CONSTANTS_URL.CAMPAIGN_PROG_STATS + campaignId

        return this.http
            .get(
                campaignProgressEndPoint
            )
            .pipe(
                map((responseData: any) => {
                    // this.loadingsenderIds.next(false)
                    //  this.senderIdArray = [];                    
                    return responseData;
                }),
                catchError((err) => {
                    //  this.loadingsenderIds.next(false)
                    if (err.status == 0) {

                        let setError = ERROR.SENDER_ID_ERROR;
                        this.badError = setError;
                    } else {
                        this.badError = err.error;
                    }
                    return this.badError;
                    // return throwError(err);
                })
            );
    }

    errorHandling(error) {
        if (error.status == 0) {

            let setError = ERROR.REQUEST_NOT_SEND;
            this.badError = setError;
        }
        else {
            let setError = ERROR.SOMETHING_WENT_WRONG;
            this.badError = setError;
        }

        return this.badError;
    }

    checkBalance = CONSTANTS_URL.EDIT_ACCT_API;

    zeroBalanceCheck = new BehaviorSubject<boolean>(false);

    zeroBalanceCheckApiLoad = new BehaviorSubject<boolean>(false);


    checkUserBalance() {

        let userid = this.localStorage.getLocal("user");
        let cli = JSON.parse(userid)
        // console.log(cli.cli_id, 'in userclid id ');
        this.zeroBalanceCheck.next(false);
        if (cli.bill_type == 1) {
            //  console.log('inside zero balance');

            this.zeroBalanceCheckApiLoad.next(true);
            this.http.get(this.BASE_URL + this.checkBalance + cli.cli_id)
                .subscribe(
                    (result: any) => {
                        this.zeroBalanceCheckApiLoad.next(false);
                        if (result.wallet <= 0) {
                            this.zeroBalanceCheck.next(true);
                        } else {
                            this.zeroBalanceCheck.next(false);
                        }
                        //console.log(result.wallet);
                    },
                    (error: any) => {
                        this.zeroBalanceCheckApiLoad.next(false);
                        this.zeroBalanceCheck.next(false);

                    }

                )
        }
    }

    getUser() {
        let userid = this.localStorage.getLocal("user");
        let cli = JSON.parse(userid);
        return cli;
    }

    validateBasedOnTrafiic(type: string, form: any) {
        console.log(form);
        if (type == 'other') {
            form.controls.entityId.setValidators([]);
            form.controls.entityId.updateValueAndValidity();
            form.controls.senderId.setValidators([]);
            form.controls.senderId.updateValueAndValidity();
            form.controls.template?.setValidators([]);
            form.controls?.template?.updateValueAndValidity();
            form.controls.intlSenderId.setValidators([Validators.required, Validators.minLength(5),
            Validators.maxLength(15)]);
            form.controls.intlSenderId.updateValueAndValidity();




        }
        if (type == "india") {
            form.controls.intlSenderId.setValidators([]);
            form.controls.intlSenderId.updateValueAndValidity();

        }
        if (type == 'both') {
            form.controls.intlSenderId.setValidators([Validators.required, Validators.minLength(5),
            Validators.maxLength(15)]);
            form.controls.intlSenderId.updateValueAndValidity();
        }
    }

    existingTrafficType: string;
    previousTrafficSelection(type: any) {
        this.existingTrafficType = type;
    }

    preSetCampaignName(form: any, type: string) {

        var today = new Date();
        const formattedDate = today.toISOString().split('T')[0];

        const time = today.getHours() + "" + today.getMinutes() + "" + today.getSeconds();
        //console.log("time...", time)

        form.controls.name.value = type + " " + formattedDate.replace(/-/g, '') + time;

    }



}


