import { Component, OnInit } from '@angular/core';
import { CONSTANTS, value} from "src/app/shared/campaigns.constants";

@Component({
  selector: 'app-scheduled-campaign-detail-skeleton',
  templateUrl: './scheduled-campaign-detail-skeleton.component.html',
  styleUrls: ['./scheduled-campaign-detail-skeleton.component.css']
})
export class ScheduledCampaignDetailSkeletonComponent implements OnInit {

  myOptions = value;

  constructor() { }

  ngOnInit(): void {
  }

}
