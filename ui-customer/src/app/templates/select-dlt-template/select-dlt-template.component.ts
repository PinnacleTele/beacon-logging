import { Component, EventEmitter, Input, OnInit, Output ,OnChanges, SimpleChanges} from '@angular/core';
import { ControlContainer } from '@angular/forms';

import { Templates } from 'src/app/shared/model/templates.model';
import { TemplateCampaignService } from 'src/app/campaigns/campaign-template/service/template-campaign.service';
import { DLTs } from "src/app/shared/model/DLTs.model";

@Component({
  selector: 'app-select-dlt-template',
  templateUrl: './select-dlt-template.component.html',
  styleUrls: ['./select-dlt-template.component.css']
})
export class SelectDltTemplateComponent implements OnInit,OnChanges {
  fileError = false;
  isShowTemplateSlider = false;
  selectedTemplate : Templates;
  selectedDltTemplate : DLTs;
  showPreview = false;
  textcolor="text-blue-600";
  templateFormGroup : any;
 @Input() campaignType:any ;

 Action:any = 'Choose';

 @Input() inTemplate:any;

 @Input() entityId:any;

 @Input() templateMsg : any;

 @Input() previewIndexOrColumn : any;

 @Output() noEntityId = new EventEmitter<boolean>();


 
  @Output() clickSelect = new EventEmitter<null>();
  @Output() choosenTemplate = new EventEmitter<Templates>();
  @Output() choosenDltTemplate = new EventEmitter<DLTs>();
  @Output() selectedDLT = new EventEmitter<string>();
  @Output() templateMsgChg = new EventEmitter<string>();

  @Output() changeEvent = new EventEmitter<string>();



  constructor(private templateSvc: TemplateCampaignService,
    private controlContainer : ControlContainer) { }

  ngOnInit(): void {
    this.templateFormGroup = this.controlContainer.control;
    
  }

  ngOnChanges(changes: SimpleChanges): void {
   
    
    if (changes.entityId) {
    
 this.Action = "Choose";
 this.textcolor="text-blue-600";

}
if(changes.templateMsg){
  if(changes.templateMsg.currentValue == ""){
    this.Action = "Choose";
    this.textcolor="text-blue-600";
  }
}
  }

//   get dlt() {
//     return this.templateFormGroup.get("dlt");
// }

  showTemplateSlider(){
    this.clickSelect.emit();
    
   // this.fileError = this.templateSvc.getFileError();
    if(this.entityId == null ){
      this.noEntityId.emit(true)
      }
      else{
        this.isShowTemplateSlider=true;
      }
    // if(!this.fileError){
    //   this.isShowTemplateSlider = true;
    // }
  }

  changeOrEditResponse() {
    this.isShowTemplateSlider = true;
  }

  // clear(){
  //   this.Action = "choose";

  //   this.isShowTemplateSlider = false;
  //   this.choosenTemplate.emit();
    
  // }
  Dlt_template_ID:any;

  responseTemplate(event : any){
    if (this.inTemplate ==='createTemp') {
      this.Action = "Change"
    /*below if else added not to trigger any event to be emitted
      if( this.selectedDltTemplate && 
        this.selectedDltTemplate.dlt_template_content == event.dlt_template_content
        && this.selectedDltTemplate.dlt_template_id == event.dlt_template_id){
        
      }else{
        console.log("ehdsr")
        
      }
      */
     
      this.selectedDltTemplate = event;
      this.showPreview = true;
      this.textcolor ="text-red-600";
      
      this.choosenDltTemplate.emit(this.selectedDltTemplate);
      this.Dlt_template_ID=this.selectedDltTemplate.dlt_template_id;
    } else {
      this.Action = "Change"
      this.selectedTemplate = event;
      this.showPreview = true;
      this.choosenTemplate.emit(this.selectedTemplate);
      this.Dlt_template_ID=this.selectedTemplate.dlt_template_id;
    }
   // this.templateFormGroup.controls.template.setValue(this.selectedTemplate.t_name);
  }
  get templatev() {
    return this.templateFormGroup.controls.template;
  }
  
  closeSlider(){
    this.isShowTemplateSlider = false;
  }

  clearTemplate(){
   
    
    this.choosenTemplate.emit();

    
   }

}
