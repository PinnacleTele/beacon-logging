import { Component, Input, OnInit } from '@angular/core';

import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { CampaignsService } from "src/app/campaigns/campaigns.service";
@Component({
	selector: 'app-traffic_type',
	templateUrl: './traffic-type.component.html',
	styleUrls: ['./traffic-type.component.css']
})
export class TrafficTypeComponent implements OnInit {
	trafficFormGroup: any;

	trafficType:string="";
	pageTitle = '';
	subscriptionFile: Subscription;
	@Input() campType = '';
	constructor(
		  private campaignSvc: CampaignsService,

		private router: Router,
		private route: ActivatedRoute
	) {}

	ngOnInit() {
		this.route.queryParams.subscribe((params) => {
			this.campType = params['campType'];
			console.log(params);
			
			if (this.campType.toLowerCase() == 'cq') {
				this.pageTitle = 'Quick SMS';
			} else if (this.campType.toLowerCase() == 'otm') {
				this.pageTitle = 'One To Many';
			} else if (this.campType.toLowerCase() == 'mtm') {
				this.pageTitle = 'Many To Many';
			} else if (this.campType.toLowerCase() == 'cg') {
				this.pageTitle = 'Groups SMS';
			} else if (this.campType.toLowerCase() == 'ct') {
				this.pageTitle = 'Campaign Template';
			}
		});

		this.trafficType =	this.campaignSvc?.existingTrafficType ;
	}

	toggleTrafficType(type: any) {
		this.trafficType = type;
		this.campaignSvc.previousTrafficSelection(type);
		const routeToCampaign = '/campaigns/' + this.campType;
		this.router.navigate([routeToCampaign], {
			queryParams: { traffic: this.trafficType },
			skipLocationChange: true
		});
	}
}
