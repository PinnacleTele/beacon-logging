import { Component, OnInit } from '@angular/core';
import { CampaignsService } from "src/app/campaigns/campaigns.service";

@Component({
  selector: 'app-nobalancestyle',
  templateUrl: './nobalancestyle.component.html',
  styleUrls: ['./nobalancestyle.component.css']
})
export class NobalancestyleComponent implements OnInit {

  Warning = "You don't have enough balance to send a campaign"

  public zeroBalance :boolean = false;

  public zeroBalanceApiLoad :boolean = false;


  public ZeroBalance = this.campservice.zeroBalanceCheck.subscribe((data:any)=>{this.zeroBalance = data});

  public ApiZeroBalance = this.campservice.zeroBalanceCheckApiLoad.subscribe((data:any)=>{this.zeroBalanceApiLoad = data})


  constructor(private campservice:CampaignsService) {  }

  ngOnInit(): void {
    this.campservice.checkUserBalance();
  }

}
