import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CONSTANTS, value } from 'src/app/shared/campaigns.constants';
import { CampaignsService } from '../campaigns.service';
import { SetIntervalService } from "src/app/campaigns/c_Helper/setintervalapicalls";

@Component({
	selector: 'app-campaign-details',
	templateUrl: './campaign-details.component.html',
	styleUrls: ['./campaign-details.component.css']
})
export class CampaignDetailsComponent implements OnInit, OnDestroy {

	constructor(private campService: CampaignsService,
		private routes: ActivatedRoute,
		private intervalservice: SetIntervalService,
		private router: Router) {


	}
	ngOnDestroy(): void {
		if (this.interval) {
			clearInterval(this.interval)
		}
		if (this.progrssCardtimer) {
			clearInterval(this.progrssCardtimer)
		}
	}

	progress: any;
	submission: any;
	platFormFilures: any;
	DNSuccess: any;
	DNFailure: any;
	DNPending: any;
	files: any[] = [];
	groups: any[] = [];
	eGroups: any[] = []
    myOptions = value;
	campaignId: string;
	campaignDetail: any;
	campaignFileDeatil: any;
	campaignFiles: any;
	showSkeleton: boolean = false;


	ngOnInit(): void {

		this.campaign = this.campService.campaignDetailPageContent
		this.routes.queryParams.subscribe(campaignId => {
			this.campaignId = campaignId['campaignId'];
			// this.campaignId="707l86ag4xazet7n9b5hhxi2vren4h7821lm"
			this.campaignProgrssCard();

			////refresh call
			this.fetchProgressCardDetails();
			//campaignDetail api call
			this.showSkeleton = true;
			this.campService.campaignDetail(this.campaignId).subscribe((res: any) => {

				this.campaignDetail = res
				this.campaignDetail.remove_dupe_yn = this.campaignDetail.remove_dupe_yn == 1 ? 'yes' : 'no';
				this.campaignDetail.shorten_url = this.campaignDetail.shorten_url == 1 ? 'yes' : 'no';
				if (this.campaignDetail.c_type.toLowerCase() != "quick") {
					//campaignFileDetail api call
					this.fileDetailCall();
					//refresh call
					this.fetchFileDetails();
				}
				this.showSkeleton = false;
			})
		})
		// righ side progressCard API call 



	}

	campaign: any;

	progressStats: any;

	percentageConvertor(num: string | null) {
		if (num != null) {
			return num + "%"
		}
		else {
			return "0%"
		}
	}

	fileExtension(file: string) {
		return file.split('.').pop();
	}
	stringToNumber(num: string) {
		return parseInt(num)
	}
	successFiles: number;
	succeessGroups: number
	perc: string = "0%"

	percentageAdder(num: string) {
		return num + "%"
	}
	fileDetailCall() {
		this.campService.campaignFileDetail(this.campaignId).subscribe((response: any) => {
			//console.log(response);
			this.campaignFileDeatil = response
			this.campaignFiles = response.data
			// console.log("file detail call");
			if (this.campaignFileDeatil.completion_percentage == 100) {
				if (this.interval) {
					clearInterval(this.interval)
				}
			}

		})
	}
	onPage = true;
	interval;

	progrssCardtimer;


	// file deatail background thread
	fetchFileDetails() {

		this.interval = setInterval(() => {
			this.fileDetailCall()
		}, CONSTANTS.apiHitTimer)
	}

	// left progress card  background thread
	fetchProgressCardDetails() {


		this.progrssCardtimer = setInterval(() => {
			this.campaignProgrssCard();;
		}, CONSTANTS.apiHitTimer)
	}





	isFile() {
		if (this.campaignDetail?.c_type == "otm" || this.campaignDetail?.c_type == "OTM" || this.campaignDetail?.c_type == "MTM" || this.campaignDetail?.c_type == "mtm" || this.campaignDetail?.c_type == "template" || this.campaignDetail?.c_type == "Template") {
			return "file"
		}
		else if (this.campaignDetail?.c_type == "group" || this.campaignDetail?.c_type == "GROUP") {
			return "group"
		}
		else {
			return "quick"
		}
	}

	campaignProgrssCard() {
		this.campService.campaignProgressStats(this.campaignId).subscribe((res: any) => {
			this.progressStats = res;
			//console.log((this.progressStats));
			if (this.progressStats.status == 'completed') {
				if (this.progrssCardtimer) {
					clearInterval(this.progrssCardtimer)
				}

			}
		})

	}

	back() {
		this.intervalservice.fromBack('campaignDetail')
		this.router.navigate(['/campaigns'])
	}
	enterBack(event: any) {
		if (event.keyCode == 13) {
			this.intervalservice.fromBack('campaignDetail')
			this.router.navigate(['/campaigns'])
		}
	}
	routing(routePath: string) {
		this.router.navigate([routePath])
	}
}
