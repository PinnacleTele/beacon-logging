import { Component, Input, OnInit } from '@angular/core';
import { value } from "src/app/shared/campaigns.constants";

@Component({
  selector: 'app-campaigncard-detail',
  templateUrl: './campaigncard-detail.component.html',
  styleUrls: ['./campaigncard-detail.component.css']
})
export class CampaigncardDetailComponent implements OnInit {

  constructor() { }
  @Input() campaign:any;

  myOptions = value;
  detail=true

  ngOnInit(): void {
  }
  isFile(){
    if( this.campaign?.c_type== "otm" || this.campaign?.c_type== "OTM" ||this.campaign?.c_type== "MTM"||this.campaign?.c_type== "mtm" || this.campaign?.c_type== "template"||this.campaign?.c_type== "Template"){
      return "file"
    }
    else if(this.campaign?.c_type== "group" || this.campaign?.c_type== "GROUP"){
      return "group"
    }
    else{
      return "quick"
    }
   }

}
