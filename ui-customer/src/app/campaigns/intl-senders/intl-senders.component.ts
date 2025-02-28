import { HttpErrorResponse } from '@angular/common/http';
import {
	Component,
	Input,
	OnInit,
	Output,
	ViewChild,
	EventEmitter
} from '@angular/core';
import { ControlContainer } from '@angular/forms';
import { NgSelectComponent } from '@ng-select/ng-select';
import { CONSTANTS } from 'src/app/shared/campaigns.constants';
import { CampaignsService } from '../campaigns.service';
import { CommonService } from "src/app/shared/commonservice";

@Component({
	selector: 'app-intl-senders',
	templateUrl: './intl-senders.component.html',
	styleUrls: ['./intl-senders.component.css']
})
export class IntlSendersComponent implements OnInit {
	senderIdFormGroup: any;
	noSenderId = false;
	itemList: any;
	@Output() Retry = new EventEmitter<boolean>();

	@Output() ResetFocus = new EventEmitter<boolean>();
	@Output() IntlSenderId = new EventEmitter<any>();

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
	@Input() trafficType: any;
	//  @ViewChild("select") select: NgSelectComponent;

	constructor(
		private sharedService: CommonService,
		private controlContainer: ControlContainer,
		private CampaignService: CampaignsService,

	) { }

	ngOnInit(): void {

		this.senderIdFormGroup = this.controlContainer.control;
		//this.senderIdFormGroup.markAsPristine();
		this.senderIdAPIcall();


	}


	get senderId() {
		return this.senderIdFormGroup.get('intlSenderId');
	}

	onItemChange(event: any) {
	//	console.log('inside');
		const value = event?.header;

		//this.selectedValue = event;
		this.senderIdFormGroup.controls.intlSenderId.setValue(value);
		//this.entityIdFormGroup.controls.entityId.setValue(event);


		if (this.senderIdFormGroup.controls.intlSenderId.valid) {
			this.IntlSenderId.emit(
				this.senderIdFormGroup.controls.intlSenderId.value
			);
		}
		if (this.senderIdFormGroup.controls.intlSenderId.invalid) {
			this.IntlSenderId.emit(null);
		}
	}


	EnteredValue(event) {
		let val = event.target.value;
		let trimmed = val.trim();
		if (trimmed.length > 0) {
			const term = {
				header: trimmed
			}
			this.onItemChange(term);
		}
	}

	retry() {
		this.senderIdAPIcall();
	}
	senderIdAPIcall() {


		//this.senderIdFormGroup.controls.senderId.markAsUntouched();
		if (this.senderIdFormGroup.controls.senderId.errors?.apiRequestError) {
			this.senderIdFormGroup.controls.senderId.setErrors({
				apiRequestError: false
			});
		}
		
		
			this.CampaignService.findAllintlSenders(
			
			).subscribe(
				(res: any) => {
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
						this.onItemChange(res[0]);
						this.selectFrom.blur();
				  }
				},
				(error: HttpErrorResponse) => {
					let err = this.CampaignService.badError;
				this.senderIdFormGroup.controls.senderId.markAsTouched();
				this.senderIdFormGroup.controls.senderId.setErrors({
					apiRequestError: true
				});
			}
		);

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

	setfocus() {
		this.selectFrom.focus();
		console.log("intl");

	}
}
