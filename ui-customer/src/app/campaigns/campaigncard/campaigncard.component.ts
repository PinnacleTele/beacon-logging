import { Component, Input, OnInit } from '@angular/core';
import { CampaignsService } from "src/app/campaigns/campaigns.service";
import { Router } from "@angular/router";
import { value } from "src/app/shared/campaigns.constants";

@Component({
  selector: 'app-campaigncard',
  templateUrl: './campaigncard.component.html',
  styleUrls: ['./campaigncard.component.css']
})
export class CampaigncardComponent implements OnInit {

  @Input() campaign:any;

  @Input() detail: any;

  myOptions = value;

  constructor(private campservice:CampaignsService, private router:Router) { }

  ngOnInit(): void {
    // console.log(this.campaign);
    
  }

  onClick(campaign:any){
    this.campservice.campaignDetailPageContent=campaign
this.router.navigate(["/campaigns/cdetail"],{ queryParams: { campaignId:campaign}})

  }

  backEnter(event:any,campaign:any){
    if(event.keyCode ==13){
      this.onClick(campaign);
    }
  }
}
