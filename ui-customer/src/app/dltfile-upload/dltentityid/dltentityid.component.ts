import { Component, EventEmitter, Input, OnDestroy, OnInit, Output, ViewChild } from "@angular/core";
import { ControlContainer, Validators } from "@angular/forms";
import { HttpErrorResponse } from "@angular/common/http";
import { NgSelectComponent } from "@ng-select/ng-select";
import { DltUploadService } from "src/app/dltfile-upload/dltupload.service";
import { CONSTANTS } from "src/app/shared/campaigns.constants";
import { CommonService } from "src/app/shared/commonservice";
@Component({
    selector: 'app-dltentityid',
    templateUrl: './dltentityid.component.html',
    styleUrls: ['./dltentityid.component.css']
})
export class DltentityidComponent implements OnInit, OnDestroy {

    @ViewChild('selectFrom') selectFrom: NgSelectComponent;

    @Output() entityIdEmitter = new EventEmitter<any>();

    @Output() noEntityId = new EventEmitter<boolean>();

    @Output() retryEmitter = new EventEmitter<any>();

    @Input() dltFileUploadForm: any;

    entityIdFormGroup: any;

    itemList: string[] = [];

    senderIdInfoText = "info Text";

    spinner: boolean = false;

    Responce: { message: string, statusCode: number }

    status: string;

    showRetry: boolean = false;

    noDataFound = false;

    popup = false;

    addTag: boolean = true;

    @Input() title = true;

    patternValid = CONSTANTS.dltEntityIDPattern;

    minimumLengthvalid = CONSTANTS.dltEntityIDMinLength;

    maxValid = CONSTANTS.dltEntityIDMaxLength;

    minErrorMessage = CONSTANTS.ERROR_DISPLAY.campaignNameMinLength;

    maxErrorMessage = CONSTANTS.ERROR_DISPLAY.campaignNameMaxLength;

    patternErrorMessage = CONSTANTS.ERROR_DISPLAY.onlyNumbers;

    public loading = this.dltService.loadingentityIds.subscribe((data) => { this.spinner = data });

    showMaxValid: boolean;

    tempItems: any[];

    selectedDltEntityId: any;

    dynamicTerm:any;

    constructor(
        private controlContainer: ControlContainer,
        private dltService: DltUploadService,
        private sharedService: CommonService
    ) { }


    ngOnInit(): void {
        this.entityIdFormGroup = this.controlContainer.control;
        this.dltFileUploadForm = this.controlContainer.control;
        this.entityIdGet();
        this.setValidatorsToFormFields();
    }

    focus() {
        this.selectFrom.focus();
    }


    entityIdGet() {
        // entityId_API_call
        if (this.showRetry) {
            this.retryEmitter.emit(true)
        }
        this.showRetry = false;
        this.dltService.findAllentityIds().subscribe(

            (res: any) => {

                this.showRetry = false;
                const entites: string[] = [];
                res.forEach((ele: any) => {
                    entites.push(ele);
                });
                this.itemList = entites;
                this.tempItems = this.itemList;
                if (this.itemList.length > 0) {
                    this.dynamicTerm = Object.keys(res[0]);
                }
                if (this.itemList.length == 1) {
                    this.selectedDltEntityId = res[0].entity_id;
                    this.onItemChange(res[0])
                    this.selectFrom.blur();
                }

            },
            (error: HttpErrorResponse) => {
                let err = this.dltService.badError
                this.showRetry = true;
                this.entityIdFormGroup.controls.entityId.setErrors({
                    apiRequestError: true
                });

            }

        );
    }

    get entityId() {
        return this.entityIdFormGroup.controls.entityId;
    }

    setValidatorsToFormFields() {
        this.entityId.setValidators([
            Validators.required,
            Validators.minLength(this.minimumLengthvalid),
            Validators.maxLength(this.maxValid),
            Validators.pattern(this.patternValid)
        ]);
        this.entityId.updateValueAndValidity();


    }

    testloading = false;


    onItemChange(event: any) {
        const value = event?.entity_id;
        if (value == undefined) {
            this.tempItems = this.itemList;
        }
        this.selectedDltEntityId = value;
        this.entityIdFormGroup.controls.entityId.setValue(value);


        if (this.entityIdFormGroup.controls.entityId.valid) {
            this.entityIdEmitter.emit(
                this.entityIdFormGroup.controls.entityId.value
            );
        }
        if (this.entityIdFormGroup.controls.entityId.invalid) {
            this.entityIdEmitter.emit(null);
        }
        // this.customSearchFn = event;
    }

    templates: boolean = false
    showTemplates() {
        this.templates = true;
    }

    closeTemplates(event: boolean) {
        this.templates = false;

    }

    public charNotAllow: boolean = false;

    public minLengthWarn: boolean = false;
    customSearchFn: any;

    numberOnly(event) {
      this.tempItems = this.itemList;
        let val = event.target.value;
        let trimmed = val.trim();

        if (trimmed.length > 0) {
            const term = {
                entity_id: trimmed
            }
            this.onItemChange(term);
        }
    }

    showValid: boolean = false;


    sub() {

        this.spinner = false;
    }

    getarray() {
        return this.itemList;
    }

    entervalue: any;

    customSearch() {
        const value = this.selectedDltEntityId?.trim();
        const dynamicval = this.dynamicTerm[0];
        this.tempItems =  this.sharedService.customSerach(this.itemList,dynamicval,value);
 
    }

    keyupcall() {
        this.tempItems = this.itemList;
    }

    ngOnDestroy(): void {
        if (this.loading) {

            this.loading.unsubscribe();
        }

    }

}
