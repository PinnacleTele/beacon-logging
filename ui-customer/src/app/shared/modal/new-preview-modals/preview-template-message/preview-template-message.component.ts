import { Component, Input, OnInit } from "@angular/core";
import { Templates } from "src/app/shared/model/templates.model";
import { TemplateCampaignService } from "src/app/campaigns/campaign-template/service/template-campaign.service";

@Component({
  selector: "app-preview-template-message",
  templateUrl: "./preview-template-message.component.html",
  styleUrls: ["./preview-template-message.component.css"]
})
export class PreviewTemplateMessageComponent implements OnInit {
  template: any;
  textToDisplay = "text";
  newLineCount = 0;
  msgContent: string;
  tName: String;
  @Input() campaignForm: any;
  @Input() campaignType: any;

  mobileColumnName: string;

  templateType: string;

  ngOnInit(): void {
    console.log(this.campaignForm.controls);

    if (this.campaignForm.controls.template.value?.id != undefined) {
      this.template = this.templateSvc.getSelectedTemplate();
      this.newLineCount = (this.campaignForm.controls.template.value.t_content.match(/\n/g) || '').length;
      this.tName = this.campaignForm.controls.template.value?.t_name;
      this.msgContent = this.campaignForm.controls.template.value.t_content;
      if (this.campaignForm.controls.template.value.t_lang_type === 'english') {

        this.textToDisplay = 'text';
      } else {
        this.textToDisplay = this.campaignForm.controls.template.value?.t_lang_type;
      }
      this.mobileColumnName = this.campaignForm.controls.template.value?.t_mobile_column;
      this.templateType = this.campaignForm.controls.template.value?.t_type;
    } else if (this.campaignForm.controls.template.value?.dlt_template_content != undefined) {

      if (this.campaignType == 'CT') {
        this.msgContent = this.campaignForm.controls.tempmessage.value;
      } else {
        this.msgContent = this.campaignForm.controls.template.value?.dlt_template_content;
      }
      this.template = this.templateSvc.getSelectedTemplate();
      this.newLineCount = (this.campaignForm.controls.tempmessage.value.match(/\n/g) || '').length;
      this.tName = this.campaignForm.controls.template.value?.dlt_template_name;

      // if (this.campaignForm.controls.template.value?.pattern_type != 0) {

      //   this.textToDisplay = 'unicode';
      // } else {
      //   this.textToDisplay = 'text';
      // }
      this.textToDisplay = this.campaignForm.controls.c_langType.value == "english" ? "text" : "unicode";
      this.mobileColumnName = this.campaignForm.controls.mobileColumn.value;
      this.templateType = this.campaignForm.controls.basedOn.value;
    } else {

      if (this.campaignType == 'CT') {
        this.msgContent = this.campaignForm.controls.tempmessage.value;
        this.newLineCount = (this.campaignForm.controls.tempmessage.value.match(/\n/g) || '').length;
        this.textToDisplay = this.campaignForm.controls.c_langType.value == "english" ? "text" : "unicode";
      } else {
        this.msgContent = this.campaignForm.controls.message.value;
        this.newLineCount = (this.campaignForm.controls.message.value.match(/\n/g) || '').length;
      }
      this.mobileColumnName = this.campaignForm.controls.mobileColumn.value;
      this.templateType = this.campaignForm.controls.basedOn.value;
    }

  }

  constructor(private templateSvc: TemplateCampaignService) { }


}
