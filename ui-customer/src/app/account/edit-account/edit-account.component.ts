import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { LocalStorageService } from 'src/app/authentication/local-storage.service';
import { CONSTANTS } from 'src/app/shared/campaigns.constants';

import { EditAccountService } from './edit-account.service';

import { TemplateGroup } from '../shared/model/template-group-model';
import { TGroupSliderService } from 'src/app/shared/service/template-group-slider.service';
import { GroupSliderService } from 'src/app/shared/service/group-slider.service';
import { ACCT_CONSTANTS } from '../account.constants';
import { SetValidationsService } from "src/app/account/set-validations.service";

@Component({
	selector: 'app-edit-account',
	templateUrl: './edit-account.component.html',
	styleUrls: ['./edit-account.component.css']
})
export class EditAccountComponent implements OnInit {
	isShowContact = false;
	removeDuplicateChecked = true;
	decimalNumTotLength = CONSTANTS.decimalTotalLength;
	showClearModal = false;
	showCancelModal = false;
	deleteMobiles = false;
	genericMinimalLength = CONSTANTS.genericMinFieldMinLength;
	genericOptimalMinLength = CONSTANTS.genericOptimalFieldMinLength;
	genericMaxLength = ACCT_CONSTANTS.genericFieldMaxLength;
	allowedSplChars = CONSTANTS.pattern_validation;
	genericMinLength = CONSTANTS.genericMinFieldMinLength;
	accountId: number;
	apiError: boolean = false;
	resObj: any;
	subscription: Subscription;
	cliId: number;
	allocatedGroups: TemplateGroup[] = [];
	assignedGroups: TemplateGroup[] = [];
	groups: TemplateGroup[] = [];
	spinner = false;
	constructor(
		private fb: FormBuilder,
		private router: Router,
		private route: ActivatedRoute,
		private editAcctService: EditAccountService,
		private groupSliderService: TGroupSliderService,
		private sharedGroupService: GroupSliderService,
		private validationService : SetValidationsService
	) {}

	ngOnInit(): void {
		this.validationService.forCreateAccountGetAinfo();

		this.getAcctInfo();
	}
	getAcctInfo() {
		this.spinner = true;
		this.apiError = false;
		this.subscription = this.route.queryParams.subscribe((params) => {
			this.accountId = params['accounts'];
			this.editAcctService.getAcctInfoToEdit(this.accountId).subscribe(
				(res: any) => {
					if (res) {
						this.spinner = false;
						this.resObj = res;
						this.editAcctService.populateAcctInfo(
							res,
							this.editAcctForm,
							'editAcctPage'
						);
					}
				},
				(error: HttpErrorResponse) => {
					this.spinner = false;
					this.apiError = true;
				}
			);
		});
	}

	editAcctForm = this.fb.group({
		accountType: [],
		acctStatus: [],
		billType: [],
		userType: [],
		firstName: [''],
		lastName: [''],
		address: [,],
		company: [,],
		userName: [],
		contactMobile: [],
		contactEmail: [],

		walletAmount: [],
		patchAmount: [],
		walletComments: [],
		rowRate: [],

		twofa: [false],
		tzAbbr: [],
		tz: [,],
		zone: [],
		//country :[,],
		newlineChar: [,],
		encrytMob: [false],
		encryMsg: [false],

		allocatedTG: [''],
		assignedTG: [''],

		templateGroups: [],
		sharedGroups: [],
		groups: [],
		acctCount: [],
		adminCount: [],
		userCount: [],
		currency : []
	});

	ngOnDestroy() {
		if (this.subscription) {
			this.subscription.unsubscribe();
		}

		this.allocatedGroups = [];
		this.assignedGroups = [];
		this.groups.forEach((el) => {
			el.checked = false;
		});
		//this.groupSliderService.groupList = this.groups;
		this.groupSliderService.groupList = [];

		this.groupSliderService.populateSelectedGroups(this.allocatedGroups);
		this.groupSliderService.assignedGroupId = 0;
		// this.sharedGroupService.groupList = this.groups;
		this.sharedGroupService.groupList = [];
		this.sharedGroupService.populateSelectedGroups(this.assignedGroups);
		this.editAcctService.cloneDLTCard = [];
		this.editAcctService.cloneDLTAssigned = [];
		this.editAcctService.assignedArr = [];
		this.editAcctService.servicesArr = [];
		this.editAcctService.currency = "";
	}
	back() {
		this.router.navigate(['/accounts']);
	}
	routing(routePath: string) {
		this.router.navigate([routePath]);
	}
}
