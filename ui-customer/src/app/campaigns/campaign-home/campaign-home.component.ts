import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import {
  EnterExitRight,
  Container1,
  EnterExitTop
} from "../../shared/animation";
import { CampaignsService } from "src/app/campaigns/campaigns.service";

@Component({
  selector: 'app-campaign-home',
  templateUrl: './campaign-home.component.html',
  styleUrls: ['./campaign-home.component.css'],
  animations: [EnterExitRight, Container1, EnterExitTop]

})
export class CampaignHomeComponent implements OnInit,OnChanges {

  @Output() dropDown = new EventEmitter<any>() ;

  @Input() openDropDown:boolean = false;

  @Input() title:string;

  constructor(private campservice:CampaignsService) { }

  ngOnChanges(changes: SimpleChanges): void {
    
  }

  ngOnInit(): void {
  }
  onCampaignClick(){
    this.openDropDown = false;
    
  }


campaignTab(){
  
  this.campservice.openQA.next(false)
  this.openDropDown = !this.openDropDown;
  this.dropDown.emit(this.openDropDown)
}
}
