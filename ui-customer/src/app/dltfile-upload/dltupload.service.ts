import { HttpClient, HttpHeaders } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { map, catchError } from "rxjs/operators";
import { CONSTANTS_URL } from "src/app/shared/compaign.url";
import { BehaviorSubject, throwError } from 'rxjs';
import { ERROR } from "src/app/campaigns/error.data";
import { CONSTANTS } from "src/app/shared/campaigns.constants";
import { FileUploaderComponent } from "../shared/file-uploader/file-uploader.component";
import { FormControl, FormGroup } from "@angular/forms";
import { DltentityidComponent } from "./dltentityid/dltentityid.component";

@Injectable({
    providedIn: "root"
})
export class DltUploadService {

    BASE_URL = CONSTANTS_URL.GLOBAL_URL;

    STATS_URL_DLT = CONSTANTS_URL.DLT_STATS_URL;

    SENDERIDS_URL = CONSTANTS_URL.DLT_ALL_SENDER_ID_URL;

    ENTITY_ID_STATS = CONSTANTS_URL.DLT_STATS_ENTITY_ID_URL;

    uploadRecordsUrl = CONSTANTS_URL.DLT_UPLOAD_RECORDS_URL;

    DLT_ALL_TEMPLATE_URL = CONSTANTS_URL.DLT_TEMPLATES_ALL_ENTITYID_TEMPLATEID_URL;

    DLTENTITY_ID_FILTER = CONSTANTS_URL.DLT_ALL_ENTITY_ID_FILTER_URL;

    DLTTEMPLATE_ID_FILTER = CONSTANTS_URL.DLT_ALL_TEMPLATES_ID_FILTER_URL;

    entity_id_URL = CONSTANTS_URL.All_ENTITY_ID;
    // error strings
    badError: any;

    userDataSource: BehaviorSubject<Array<any>> = new BehaviorSubject([]);

    userDTSource: BehaviorSubject<Array<any>> = new BehaviorSubject([]);

    telcoEmit = new BehaviorSubject<string>("")

    constructor(public http: HttpClient) { }

    httpOptions = {
        headers: new HttpHeaders({
            Accept: "application/json",
            "Content-Type": "application/json"
        })
    };

    dltTotalRecords: any;

    searchData(value) {
        this.userDataSource.next(value);
    }

    searchDTData(value) {
        this.userDTSource.next(value);
    }

    senderIDRecord: any;
    public senderIdSearchData(value: any): Observable<any> {
        return (this.senderIDRecord = value);
    }
    entityIDRecord: any;
    public entityIdSearchData(value: any): Observable<any> {
        return (this.entityIDRecord = value);
    }
    public loadingentityIds = new BehaviorSubject<boolean>(false);

    findAllentityIds() {
        this.loadingentityIds.next(true)
        return this.http
            .get<any>(this.BASE_URL + this.entity_id_URL)
            .pipe(
                map((responseData) => {
                    this.loadingentityIds.next(false);

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


    uploadDLT_URL = this.BASE_URL + CONSTANTS_URL.uploadDLT_URL;
    getTelco(): Observable<any> {
        return this.http
            .get(
                this.uploadDLT_URL,
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

    save_DLT_FIle_URL = this.BASE_URL + CONSTANTS_URL.DLT_FILE_UPLOAD_SAVE_URL;

    saveTelco(payload): Observable<any> {
        return this.http
            .post(
                this.save_DLT_FIle_URL, payload
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

    statsLoading = new BehaviorSubject<boolean>(false)
    getSTATSList() {
        this.statsLoading.next(true)
        return this.http
            .get(
                this.BASE_URL + this.STATS_URL_DLT,

                this.httpOptions
            )
            .pipe(
                map((responseData: any) => {
                    this.statsLoading.next(false)
                    return responseData as any;
                }),
                catchError((err) => {
                    this.statsLoading.next(false)
                    if (err.status == 0) {

                        let setError = ERROR.REQUEST_NOT_SEND;

                        this.badError = setError;
                    } else {
                        let setError = ERROR.SOMETHING_WENT_WRONG;
                        this.badError = setError;
                    }

                    return this.badError;
                })
            );
        // .pipe(map((data) => data as TemplateModel));
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

    senderidsLoading = new BehaviorSubject<boolean>(false)


    getAllSenderids() {
        this.senderidsLoading.next(true);
        return this.http
            .get(this.BASE_URL + this.SENDERIDS_URL, this.httpOptions)
            .pipe(
                map((responseData: any) => {
                    this.senderidsLoading.next(false)

                    return responseData as any;
                }),
                catchError((err) => {
                    this.senderidsLoading.next(false)


                    if (err.status == 0) {

                        let setError = ERROR.REQUEST_NOT_SEND;

                        this.badError = setError;
                    } else {
                        let setError = ERROR.SOMETHING_WENT_WRONG;
                        this.badError = setError;
                    }
                    return this.badError;
                })
            );

        // .pipe(map((data) => data as TemplateModel));
    }

    entityidStatsLoading = new BehaviorSubject<boolean>(false)

    getEntityIdStats() {
        this.entityidStatsLoading.next(true)

        return this.http
            .get(this.BASE_URL + this.ENTITY_ID_STATS, this.httpOptions)
            .pipe(
                map((responseData: any) => {
                    this.entityidStatsLoading.next(false)

                    return responseData as any;
                }),
                catchError((err) => {


                    this.entityidStatsLoading.next(false)
                    if (err.status == 0) {

                        let setError = ERROR.REQUEST_NOT_SEND;

                        this.badError = setError;
                    } else {
                        let setError = ERROR.SOMETHING_WENT_WRONG;
                        this.badError = setError;
                    }

                    return this.badError;
                })
            );;
    }
    dltAllLoading = new BehaviorSubject<boolean>(false)

    getAllDLtUpload(value: any): Observable<any> {
        this.dltAllLoading.next(true)

        return this.http
            .get(
                this.BASE_URL + this.uploadRecordsUrl + value
                ,
                this.httpOptions
            )
            .pipe(
                map((responseData: any) => {
                    this.dltAllLoading.next(false)

                    return responseData as any;

                }),
                catchError((err) => {
                    this.dltAllLoading.next(false)
                    if (err.status == 0) {

                        let setError = ERROR.REQUEST_NOT_SEND;

                        this.badError = setError;
                    } else {
                        let setError = ERROR.SOMETHING_WENT_WRONG;
                        this.badError = setError;
                    }
                    return this.badError;
                })
            );
    }


    dltAllTemplateLoading = new BehaviorSubject<boolean>(false)

    getAllDLtTemplate(Eid: any, Tid: any): Observable<any> {
        this.dltAllTemplateLoading.next(true)

        return this.http
            .get(
                this.BASE_URL + this.DLT_ALL_TEMPLATE_URL + Eid + `&dlt_template_id=` + Tid
                ,
                this.httpOptions
            )
            .pipe(
                map((responseData: any) => {
                    this.dltAllTemplateLoading.next(false)

                    return responseData as any;

                }),
                catchError((err) => {
                    this.dltAllTemplateLoading.next(false)
                    if (err.status == 0) {

                        let setError = ERROR.REQUEST_NOT_SEND;

                        this.badError = setError;
                    } else {
                        let setError = ERROR.SOMETHING_WENT_WRONG;
                        this.badError = setError;
                    }
                    return this.badError;
                })
            );
    }


    dltentityidFilterLoading = new BehaviorSubject<boolean>(false)

    getDLtEntityIdFilter(): Observable<any> {
        this.dltentityidFilterLoading.next(true)

        return this.http
            .get(
                this.BASE_URL + this.DLTENTITY_ID_FILTER
                ,
                this.httpOptions
            )
            .pipe(
                map((responseData: any) => {
                    this.dltentityidFilterLoading.next(false)

                    return responseData as any;

                }),
                catchError((err) => {
                    this.dltentityidFilterLoading.next(false)
                    if (err.status == 0) {

                        let setError = ERROR.REQUEST_NOT_SEND;

                        this.badError = setError;
                    } else {
                        let setError = ERROR.SOMETHING_WENT_WRONG;
                        this.badError = setError;
                    }
                    return this.badError;
                })
            );
    }


    dltTemplateidFilterLoading = new BehaviorSubject<boolean>(false)

    getDLtTemplateIdFilter(Eid: any): Observable<any> {
        this.dltTemplateidFilterLoading.next(true)

        return this.http
            .get(
                this.BASE_URL + this.DLTTEMPLATE_ID_FILTER + Eid
                ,
                this.httpOptions
            )
            .pipe(
                map((responseData: any) => {
                    this.dltTemplateidFilterLoading.next(false)

                    return responseData as any;

                }),
                catchError((err) => {
                    this.dltTemplateidFilterLoading.next(false)
                    if (err.status == 0) {

                        let setError = ERROR.REQUEST_NOT_SEND;

                        this.badError = setError;
                    } else {
                        let setError = ERROR.SOMETHING_WENT_WRONG;
                        this.badError = setError;
                    }
                    return this.badError;
                })
            );
    }

    dropzoneControl: FileUploaderComponent;

    entityId: DltentityidComponent;

    setDropzoneControl(control: FileUploaderComponent) {
        this.dropzoneControl = control;
    }

    setentityIDControl(control: DltentityidComponent) {
        this.entityId = control;
    }

    validateAllFormFields(InputForm: FormGroup) {

        const formGroup = InputForm;
        Object.keys(formGroup.controls).forEach((field) => {
            const control = formGroup.get(field);
            if (control instanceof FormControl) {
                if (control.errors?.required) {
                    control.markAsTouched({ onlySelf: true });
                }


            }
        });
        if (this.entityId && InputForm.controls.entityId.invalid) {
            this.entityId.focus();
        }

        if (this.dropzoneControl) {

            this.dropzoneControl.validateDropzoneArea();
        }

    }
}
