import { Component, OnInit } from '@angular/core';
import { ControlContainer } from '@angular/forms';
import { CommonService } from 'src/app/shared/commonservice';

@Component({
  selector: 'app-edit-account-right-panel',
  templateUrl: './edit-account-right-panel.component.html',
  styleUrls: ['./edit-account-right-panel.component.css']
})
export class EditAccountRightPanelComponent implements OnInit {
  linkIndex = 1;
  rightPanelFormGroup : any;
  clusterType:any;
  constructor(private controlContainer: ControlContainer,
    private commonService:CommonService) { }

  ngOnInit(): void {
      // CU-384
      const userData:any=this.commonService.tokenDecoder();
      
      let clusterCaseChange = userData.cluster.toLowerCase();

      this.clusterType = clusterCaseChange;
    this.rightPanelFormGroup = this.controlContainer.control;
  }
  clickLinkIndex(index : number){
    this.linkIndex = index;
    
  }
  get billType(){
    return this.rightPanelFormGroup.controls.billType.value
  }
  
}
