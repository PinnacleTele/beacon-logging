import { Component, ElementRef, EventEmitter, Input, OnInit, Output, ViewChild } from "@angular/core";
import { ControlContainer } from "@angular/forms";
import { CampaignsService } from "src/app/campaigns/campaigns.service";
import { HttpErrorResponse } from "@angular/common/http";
import { NgSelectComponent } from '@ng-select/ng-select';
import { CommonService } from "src/app/shared/commonservice";

import { GroupsCampaignService } from "src/app/campaigns/groups/groups-campaign.service";

@Component({
    selector: "app-entity-id",
    templateUrl: "./entity-id.component.html",
    styleUrls: ["./entity-id.component.css"]
})
export class EntityIdComponent implements OnInit {

    @ViewChild('selectFrom') selectFrom: NgSelectComponent;

    @Output() entityIdEmitter = new EventEmitter<any>();

    @Output() populated = new EventEmitter<boolean>();

    @Output() retryEmitter = new EventEmitter<any>();

    entityIdFormGroup: any;

    itemList: string[] = [];

    senderIdInfoText = "info Text";

    spinner: boolean = false;

    Responce: { message: string, statusCode: number }

    status: string;

    showRetry: boolean = false;

    noDataFound = false;

    popup = false;

    @Input() title = true;

    selectedEntityid:any;

    
    tempItems: any[];

    dynamicTerm:any;

    public loading = this.campaignService.loadingentityIds.subscribe((data) => { this.spinner = data })

    constructor(
        private groupsCampaignService: GroupsCampaignService,
        private controlContainer: ControlContainer,
        private sharedService: CommonService,
        private campaignService: CampaignsService
    ) { }

    ngOnInit(): void {
        this.entityIdFormGroup = this.controlContainer.control;

        this.entityIdGet();
    }

    focus() {
        this.selectFrom.focus();
    }
    blur() {
        this.selectFrom.blur();
    }
    // focus() {
    //     const focus = document.getElementById("entityid") as HTMLImageElement;
    //     focus.focus();
    //     focus.scrollIntoView();
    // }

    entityIdGet() {
        // entityId_API_call
        if (this.showRetry) {
            this.retryEmitter.emit(true)
        }
        this.showRetry = false;
        this.campaignService.findAllentityIds().subscribe(

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
                if (this.itemList.length === 0) {
                    this.noDataFound = true;

                }

               if (this.itemList.length == 1) {
                  this.selectedEntityid  = res[0].entity_id;
                  this.populated.emit(true);
                    this.onItemChange(res[0])
                    this.blur();
              }else{
                this.populated.emit(false);
              }

            },
            (error: HttpErrorResponse) => {
                let err = this.campaignService.badError
                this.showRetry = true;
                this.entityIdFormGroup.controls.entityId.setErrors({
                    apiRequestError: true
                });

            }

        );
    }

    get entityId() {
        return this.entityIdFormGroup.get("entityId");
    }

    onItemChange(event: any) {
        this.focus();
console.log(event?.entity_id);

        this.entityIdFormGroup.controls.entityId.setValue(event?.entity_id);
        if (this.entityIdFormGroup.controls.entityId.valid) {
            this.entityIdEmitter.emit(
                this.entityIdFormGroup.controls.entityId.value
            );
        }
        if (this.entityIdFormGroup.controls.entityId.invalid) {
            this.entityIdEmitter.emit(null);
        }
    }

    templates: boolean = false
    showTemplates() {
        this.templates = true;
    }
    closeTemplates(event: boolean) {
        this.templates = false;

    }

    customSearch() {
       // console.log(this.dynamicTerm);
        if (this.dynamicTerm != undefined) {
            const value = this.selectedEntityid?.trim();
            const dynamicval = this.dynamicTerm[0];
            this.tempItems =  this.sharedService.customSerach(this.itemList,dynamicval,value);
        }
    }

    keyupcall() {
        this.tempItems = this.itemList;
    }


    sub() {

        this.spinner = false;
    }


}
