import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { ControlContainer } from '@angular/forms';

import { Templates } from 'src/app/shared/model/templates.model';
import { TemplateCampaignService } from '../service/template-campaign.service';

@Component({
  selector: 'app-select-template',
  templateUrl: './select-template.component.html',
  styleUrls: ['./select-template.component.css']
})
export class SelectTemplateComponent implements OnInit,OnChanges {
  fileError = false;
  isShowTemplateSlider = false;
  selectedTemplate : Templates|any;
  showPreview = false;
  templateFormGroup : any;
  textColor=""
  
  @Input() entityId:any;
  @Input() senderId:any;

  @Input() cType:any;
  @Input() showSlider : any;
  @Input() campaignType:any = "";
  @Input() trafficType: any;
  @Output() clickSelect = new EventEmitter<null>();
  @Output() choosenTemplate = new EventEmitter<Templates>();
  @Output() noEntityId = new EventEmitter<boolean>();
  action = "Add";
  constructor(private templateSvc: TemplateCampaignService,
    private controlContainer : ControlContainer) { }
  ngOnChanges(changes: SimpleChanges): void {
    if(changes.entityId != undefined){
      this.entityId=changes.entityId.currentValue;
    }

    if(changes.senderId != undefined){
      this.senderId=changes.senderId.currentValue;
    }

    if (changes.entityId || changes.entityId == undefined){
        
      this.action = "Add";
      this.textColor="text-blue-600";
     
     }
    this.clearTemplate()
    this.isShowTemplateSlider  = this.showSlider;
    
  }

  ngOnInit(): void {
    this.templateFormGroup = this.controlContainer.control;
    this.isShowTemplateSlider  = this.showSlider;
    
    
  }
  clearTemplate(){
    console.log('inside clear template');
    
    if(this.selectedTemplate)
   this.templateFormGroup.get("template").setValue(null)
   this.showPreview=false
    this.selectedTemplate=""

  }

  get template() {
    return this.templateFormGroup.get("template");
}

  showTemplateSlider(){
    this.clickSelect.emit();
    this.fileError = this.templateSvc.getFileError();
    console.log(this.entityId);
    
    if(this.entityId == null ){
      this.noEntityId.emit(true)
      
      }
      else{
        this.isShowTemplateSlider=true;
     }
    
  }

  changeOrEditResponse() {
    this.isShowTemplateSlider = true;
  }
  
  Dlt_template_ID:string;

  responseTemplate(event : Templates){
    this.selectedTemplate = event;
    this.showPreview = true;
    this.action = "Change";
    this.textColor = "text-red-600"
    this.choosenTemplate.emit(this.selectedTemplate);
    this.Dlt_template_ID=this.selectedTemplate.dlt_template_id;
   // this.templateFormGroup.controls.template.setValue(this.selectedTemplate.t_name);
  }
  get templatev() {
    return this.templateFormGroup.controls.template;
  }
  
  closeSlider(){
    this.isShowTemplateSlider = false;
  }






}
