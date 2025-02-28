import { AfterContentChecked, ChangeDetectorRef, Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges } from '@angular/core';
import { Subscription } from 'rxjs';
import { map } from 'rxjs/operators';
import { Templates } from 'src/app/shared/model/templates.model';
import { TemplateCampaignService } from 'src/app/campaigns/campaign-template/service/template-campaign.service';
import { DLTs } from "../model/DLTs.model";
import { HttpErrorResponse } from '@angular/common/http';
import { EnterExitRight, Container1 } from '../animation';
import { value } from 'src/app/shared/campaigns.constants';

@Component({
  selector: 'app-existing-templates-slider',
  templateUrl: './existing-templates-slider.component.html',
  styleUrls: ['./existing-templates-slider.component.css'],
  animations: [EnterExitRight, Container1]
})
export class ExistingTemplatesSliderComponent implements OnInit, OnDestroy, OnChanges, AfterContentChecked {
  @Output() closeSlider = new EventEmitter<null>();
  @Output() emitSelectedTemplate = new EventEmitter<any>();
  @Output() emitSelectedDltTemplate = new EventEmitter<any>();

  subscription2: Subscription;
  subscription1: Subscription;
  cannotChooseText =
    "This template is not selectable because it does not match with the uploaded file";

  isShowTemplateSlider = false;
  templates: Templates[] = [];
  dltTemplates: DLTs[] = [];
  dltTemplatesCount: number;
  totalDlts: DLTs[] = []
  englishCount: number;
  totalCount: number;
  unicodeCount: number;
  TemplateType: string;
  searchElement = "";
  selectedTemplate: Templates;
  selectedDltTemplate: DLTs;
  usedDlts: any;
  unUsedDlts: any;
  myOptions = value;
  templateTypeSelection: string;
  @Input() choosen_dlt_id: string;
  @Input() inTemplate: any;
  entityId: any;
  @Input() entity_Id: any;
  @Input() sender_Id: any;
  @Input() showSlider = false;
  Responce: { message: string, statusCode: number }

  retryMsg: boolean = false;
  
  spinner: boolean = true;
  noMatch = false;
  noRecords: boolean = false;


  constructor(private templateSvc: TemplateCampaignService, private cdr: ChangeDetectorRef) { }


  ngOnChanges(changes: SimpleChanges): void {

    if(changes.entity_Id){
      if (typeof changes.entity_Id === 'string' || changes.entity_Id instanceof String){
        this.entityId = changes.entity_Id;
      }else{
        this.entityId = changes.entity_Id.currentValue;
      }
      /* else{
        console.log("Anytime ????", changes.entityId.currentValue.selectedValue)
        this.entityId = changes.entityId.currentValue.selectedValue;
      } */
    }

    if(changes.sender_Id){
      if (typeof changes.sender_Id === 'string' || changes.sender_Id instanceof String){
        this.sender_Id = changes.sender_Id;
      }else if (typeof changes.sender_Id.currentValue === 'string' || changes.sender_Id.currentValue instanceof String){
        this.sender_Id = changes.sender_Id.currentValue;
      } else{
        this.sender_Id = changes.sender_Id.currentValue.selectedValue;
      }
    }

    if (this.entity_Id !== undefined) {
        
      this.selectedTemplate = this.templateSvc.getSelectedTemplate();
      this.selectedDltTemplate = this.templateSvc.selectedDltTemplate;
      this.sliderSelect();
    }
    
  }

  ngOnInit(): void {

    //  this.sliderSelect();
    this.templateSvc.noMatch.subscribe((data => {
      this.noMatch = data;
    }))

    this.selectedTemplate = this.templateSvc.getSelectedTemplate();
    this.selectedDltTemplate = this.templateSvc.selectedDltTemplate;

   
    

  }


  sliderSelect() {
   
    if (this.selectedDltTemplate == undefined) {
      
      this.templateTypeSelection = 'ct';
    }else{
      this.templateTypeSelection = 'dlt';
      
    }
    

    if (this.inTemplate === 'createTemp') {
      this.dltTemplateApiCall();
    } else {
      
     if (this.templateTypeSelection == 'ct') {
        this.templateApiCall();
      } else { 
        this.dltTemplateApiCall();
      }
    }
  }


  templateApiCall() {
    if(this.subscription2)  this.subscription2.unsubscribe();
    this.TemplateType = 'Templates';
    
    this.templates = [];
    this.spinner = true;
    this.subscription1 = this.templateSvc
      .findAllTemplates(this.entity_Id,this.inTemplate,this.sender_Id).subscribe(
        (templatesArray: any) => {
          this.spinner = false;
          this.retryMsg = false;
          this.noRecords = false;
          templatesArray.forEach((data: any) => this.templates.push(data));
          if (this.templates.length == 0) {
            this.noRecords = true;
          }
          this.totalCount = this.templates.length;
          this.englishCount = this.templates.filter(value => value.t_lang_type === 'english').length;
          this.unicodeCount = this.templates.filter(value => value.t_lang_type === 'unicode').length;


        },
        (error: HttpErrorResponse) => {
          let err = this.templateSvc.badError;
          this.Responce = err;
          this.retryMsg = true;
          this.spinner = false;

        }
      )
  }

  dltTemplateApiCall() {
    if(this.subscription1)this.subscription1.unsubscribe();
    this.TemplateType = 'DLT Templates';
    this.totalDlts = [];
    this.dltTemplates =[];
    this.spinner = true;
    if (this.inTemplate === 'createTemp') {
      this.sender_Id = this.entityId;
    }
    this.subscription2 = this.templateSvc
      .findAllDltTemplates(this.sender_Id,this.inTemplate).subscribe(
        (templatesArray: any) => {
          this.spinner = false;
          this.retryMsg = false;
          this.noRecords = false;
          templatesArray.forEach((data: any) => this.dltTemplates.push(data));
          if (this.dltTemplates.length == 0) {
            this.noRecords = true;
          }
          this.totalCount = this.dltTemplates.length;
          this.englishCount = this.dltTemplates.filter(value => value.pattern_type === 0).length;
          this.unicodeCount = this.dltTemplates.filter(value => value.pattern_type === 1).length;

          this.unUsedDlts = this.dltTemplates.length;

        },
        (error: HttpErrorResponse) => {
          let err = this.templateSvc.badError;
          this.Responce = err;
          this.retryMsg = true;
          this.spinner = false;
        }
      )
  }

  clearSearchBar() {
    this.searchElement = "";
  }



  loadingSpinner() {
    this.retryMsg = false
    if (this.inTemplate === 'createTemp') {
      this.dltTemplateApiCall();
    } else {

     if (this.templateTypeSelection == 'ct') {
        this.templateApiCall();
      } else {
        this.dltTemplateApiCall();
      }
    }
    this.spinner = true;

  }

  backEnter(event: any, index: any) {
    if (event.keyCode == 13) {
      this.dltTemplateSelected(index);
      this.templateSelected(index);
    }
  }

  dltTemplateSelected(index: any) {
    this.templateSvc.populateSelectedDltTemplate(index);
    this.selectedDltTemplate = this.templateSvc.selectedDltTemplate;
    this.templateSvc.populateSelectedTemplate(undefined);
    this.emitSelectedTemplate.emit(this.selectedDltTemplate);
    this.isShowTemplateSlider = false;
    this.closeSlider.emit();

  }


  templateSelected(index: any) {
    const enable = index.enabled;


    if (enable) {
      this.searchElement = "";

      //  this.showTemplateErr = false;
      this.templateSvc.populateSelectedTemplate(index);
      this.selectedTemplate = this.templateSvc.getSelectedTemplate();
      //console.log(this.selectedTemplate.t_name,'selected template');
      this.templateSvc.populateSelectedDltTemplate(undefined);
      this.emitSelectedTemplate.emit(this.selectedTemplate);

      this.isShowTemplateSlider = false;
      this.closeSlider.emit();
    } else {
      // do nothing
    }
  }

  closeTemplateSlider() {
    this.isShowTemplateSlider = false;
    this.closeSlider.emit();
  }

  templateSelection(value) {
    this.noRecords = false;
    
    if (this.templateTypeSelection != value) {
      this.templateTypeSelection = value;
      if (this.templateTypeSelection == 'ct') {
        this.templateApiCall();
      } else {
        this.dltTemplateApiCall();
      }
    }

  }

  ngOnDestroy() {
   this.templateTypeSelection = "ct";
   
  
  }
  ngAfterContentChecked() {
    this.cdr.detectChanges()
  }
  noData() {
    if (this.noMatch && !this.retryMsg && !this.spinner && !this.noRecords) {
      return true;
    }
    else {
      return false;
    }
  }
}



