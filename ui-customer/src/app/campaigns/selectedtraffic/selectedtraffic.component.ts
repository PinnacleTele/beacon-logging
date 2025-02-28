import { Component, Input, OnInit, Output , EventEmitter } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CampaignsService } from 'src/app/campaigns/campaigns.service';


@Component({
  selector: 'app-selectedtraffic',
  templateUrl: './selectedtraffic.component.html',
  styleUrls: ['./selectedtraffic.component.css']
})
export class SelectedtrafficComponent implements OnInit {
 @Input() trafficType :any = "india";

 @Input() campType :any ;

 traffic_to : any;
 @Output() selectedtraffic = new EventEmitter<string>();

  constructor(private router: Router,private route: ActivatedRoute,	private campaignService: CampaignsService,) { }

  ngOnInit(): void {
    this.route.queryParams.subscribe((params) => {
			const traffic_type = params['traffic'];
      if (traffic_type == 'other') {
        this.traffic_to = 'other countries';
       
      } else {
        this.traffic_to = params['traffic'];
        
      }
      this.trafficType = params['traffic'];
      console.log(	params);
			// this.campaignService.validateBasedOnTrafiic(
			// 	this.trafficType,
			// 	this.quickSMSForm
			// );
      this.selectedtraffic.emit(this.trafficType);
		});
  
  }

  changeTraffic(){
    this.router.navigate(["campaigns/traffic"], {queryParams : {"campType" : this.campType}});
    // this.router.navigate(['/campaigns/traffic'], {
		// 	queryParams: { campType:  this.campType }
		// });
  }
}
