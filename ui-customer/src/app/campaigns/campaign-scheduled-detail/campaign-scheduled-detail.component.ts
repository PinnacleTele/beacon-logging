import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import * as moment from 'moment';
import { CampaignsService } from '../campaigns.service';
import { openClose } from 'src/app/shared/animation';
import { CONSTANTS, value } from 'src/app/shared/campaigns.constants';
import { SetIntervalService } from "src/app/campaigns/c_Helper/setintervalapicalls";

@Component({
	selector: 'app-campaign-scheduled-detail',
	templateUrl: './campaign-scheduled-detail.component.html',
	styleUrls: ['./campaign-scheduled-detail.component.css'],
	animations: [openClose]
})
export class CampaignScheduledDetailComponent implements OnInit {

	myOptions = value;

	constructor(private routes: ActivatedRoute,
		private router: Router,
		private interval: SetIntervalService,
		private campiagnService: CampaignsService) {
		this.routes.queryParams.subscribe(params => this.routeData = params["scheduleDetail"])
		let arr: string[] = this.routeData.split("@")
		this.campaignId = arr[0]
		this.at_id = arr[1]
	}

	ngOnInit(): void {

		this.campiagnService.scCampaignDetail(this.campaignId, this.at_id)
			.subscribe((res: any) => {
				this.campaign = res
				this.bufferTime()

			})
		this.campiagnService.scCampaignDeatilLoading.subscribe((data) => {
			this.showSkeleton = data;
		})
		this.router.onSameUrlNavigation = 'reload'
		this.history = this.router.url
	}
	history: string

	routeData: string

	campaignId: string

	at_id: string

	deleteResponse;

	campaign: any;

	responsePopup = false;

	showConfirmation = false;

	reScheduledTime: any;

	ButtonDisabler: boolean;

	showSkeleton: boolean = false;

	confirmation(event) {
		if (event == true) {
			this.campiagnService.deleteScCampaign(this.campaign.c_id, this.campaign.at_id)
				.subscribe((res: any) => {
					this.deleteResponse = res;
					this.showConfirmation = false;
					this.responsePopup = true;
				})
		}
		else {
			this.showConfirmation = false;
		}

	}

	onDelete() {
		if (this.bufferTime()) {
			this.showConfirmation = true
		}
	}

	closeDeletePopup($event) {
		this.responsePopup = false;
	}

	continueDeletePopup($event) {

		this.responsePopup = false;

		this.router.navigate(["/campaigns/scheduledlist"])
	}

	tryAgainOnPopup($event) {
		this.responsePopup = false
		this.onDelete()
		//delete tryagain
	}
	enableEdit: boolean
	onEdit() {
		if (this.bufferTime()) {
			this.enableEdit = true
		}

	}
	timeDetails(event) {
		this.reScheduledTime = event.selectedTime
		this.campaign.scheduled_ts = event.selectedTime
	}
	onCancel(event) {
		this.enableEdit = false
	}
	bufferTime() {
		if (this.campaign.scheduled_ts) {
			let currentZoneTime = moment().tz(this.campaign.selected_zone).format("LLLL")
			let scheduledZoneTime = moment(this.campaign.selected_dt, 'YYYY-MM-DD HH:mm:ss').format('YYYY-MM-DD HH:mm:ss')
			console.log(`in ${moment(scheduledZoneTime).diff(moment(currentZoneTime), "minutes")}`);
			if (moment(scheduledZoneTime).diff(moment(currentZoneTime), "minutes") <= CONSTANTS.EDIT_DELETE_SCHEDULE_BUFFER) {
				this.ButtonDisabler = true;

				return false
			}
			else {
				this.ButtonDisabler = false;
				return true
			}
		}
	}
	backEnter(event: any) {
		if (event.keyCode == 13) {
			this.interval.fromschBack("schedulecamp")
			this.router.navigate(["/campaigns/scheduledlist"])
		}
	}

	back() {
		this.interval.fromschBack("schedulecamp")
		this.router.navigate(['/campaigns/scheduledlist'])
	}

	routing(routePath: string) {
		this.router.navigate([routePath])
	}
}