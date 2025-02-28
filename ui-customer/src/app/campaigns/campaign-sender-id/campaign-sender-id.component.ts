import { HttpErrorResponse } from '@angular/common/http';
import {
	Component,
	Input,
	OnChanges,
	OnInit,
	Output,
	SimpleChanges,
	ViewChild,
	EventEmitter
} from '@angular/core';
import { ControlContainer, Validators } from '@angular/forms';
import { NgSelectComponent } from '@ng-select/ng-select';
import { CONSTANTS } from 'src/app/shared/campaigns.constants';
import { CampaignsService } from '../campaigns.service';
import { CommonService } from "src/app/shared/commonservice";


@Component({
	selector: 'app-campaign-sender-id',
	templateUrl: './campaign-sender-id.component.html',
	styleUrls: ['./campaign-sender-id.component.css']
})
export class CampaignSenderIdComponent implements OnInit, OnChanges {
	senderIdFormGroup: any;
	noSenderId = false;
	@Input() itemList: string[];

	@Input() Error: any;

	@Input() enityPopulated: boolean;

	@Input() EmptyData: any;

	@Input() entityId: any;

	@Input() templateId: any = '';

	@Output() Retry = new EventEmitter<boolean>();

	@Output() ResetFocus = new EventEmitter<boolean>();

	@Output() focus = new EventEmitter<any>();

	//  @ViewChild(NgSelectComponent) ngSelectComponent: NgSelectComponent;

	@ViewChild('selectFrom') selectFrom: NgSelectComponent;

	selectedValue: any;

	selectedCalculation: any;

	senderIdInfoText = CONSTANTS.INFO_TXT.campaignSenderId;

	spinner: boolean = false;

	 
    tempItems: any[];

    dynamicTerm:any;

	loading = this.CampaignService.loadingsenderIds.subscribe((data) => {
		this.spinner = data;
	});

	retrySender = false;
	@Input() trafficType: any;
	//  @ViewChild("select") select: NgSelectComponent;

	constructor(
		private sharedService: CommonService,
		private controlContainer: ControlContainer,
		private CampaignService: CampaignsService,
        
	) {}

	ngOnInit(): void {
		
		this.senderIdFormGroup = this.controlContainer.control;
		this.senderIdAPIcall();
		this.senderIdFormGroup.markAsPristine();
       /* const svcTemplate = this.tempSvc.getSelectedTemplate();
        
        if(svcTemplate){
            this.templateId = svcTemplate.dlt_template_id;
        }
        if(this.trafficType != "intl" &&  this.entityId ){
            this.senderIdAPIcall();
        }*/

	}

	ngOnChanges(changes: SimpleChanges): void {}

	get senderId() {
		return this.senderIdFormGroup.get('senderId');
	}

	@Output() sender_IdEmitter = new EventEmitter<any>();
	@Output() entity_IdEmitter = new EventEmitter<any>();
	onItemChange(event: any) {
		const value = event?.header;
		const entity_Id = event?.entity_id;
		this.selectedValue = value;
		this.senderIdFormGroup.controls.senderId.setValue(value);

		if (this.senderIdFormGroup.controls.senderId.valid) {
            this.sender_IdEmitter.emit(
                value
            );
        }  if(this.senderIdFormGroup.controls.senderId.valid) {
			this.entity_IdEmitter.emit(
                entity_Id
            );
		}
        if (this.senderIdFormGroup.controls.senderId.invalid) {
            this.sender_IdEmitter.emit(null);
			this.entity_IdEmitter.emit(null);
        }

	}

	setfocus() {
		this.selectFrom.focus();
	}

	retry() {
		this.senderIdAPIcall();
	}
	senderIdAPIcall() {
		// this.noSenderId = false;
		this.senderIdFormGroup.controls.senderId.markAsUntouched();
		this.retrySender = false;
		// senderId_API_call
		// if (this.entityId) {
		
		
	
		this.CampaignService.findAllsenderIds(
				this.templateId
			).subscribe(
				(res: any) => {
					this.retrySender = false;
					const senderIds: string[] = [];
					res.forEach((ele: any) => {
						senderIds.push(ele);
					});
					this.itemList = senderIds;
					this.tempItems = this.itemList;
					if (this.itemList.length > 0) {
						this.dynamicTerm = Object.keys(res[0]);
					}
					if (this.itemList.length == 0) {
						this.noSenderId = true;
					}
					if (this.itemList.length == 1) {
						  this.onItemChange(res[0])
						  this.selectFrom.blur();
					}
				},
				(error: HttpErrorResponse) => {
					let err = this.CampaignService.badError;
					this.retrySender = true;
					this.senderIdFormGroup.controls.senderId.markAsTouched();
					
				}
			);
			 // this.c_message.focus();
		// }
	}

	
    customSearch() {
		// console.log(this.dynamicTerm);
		 if (this.dynamicTerm != undefined) {
			 const value = this.selectedValue?.trim();
			 const dynamicval = this.dynamicTerm[0];
			 this.tempItems =  this.sharedService.customSerach(this.itemList,dynamicval,value);
		 }
	 }
 
	 keyupcall() {
		 this.tempItems = this.itemList;
	 }
 
}
