import { Component, EventEmitter, Input, OnInit, Output, SimpleChanges } from '@angular/core';
import { ControlContainer, FormControl, Validators } from '@angular/forms';
import { EMPTY, Subscription } from 'rxjs';
import { PostMsgPartsService } from 'src/app/campaigns/quick-sms/service/post-msg-parts.service';
import { CONSTANTS } from 'src/app/shared/campaigns.constants';
import { catchError, debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { TemplateCampaignService } from 'src/app/campaigns/campaign-template/service/template-campaign.service';
import { CampaignsService } from '../../campaigns.service';
import { LocalStorageService } from 'src/app/authentication/local-storage.service';


@Component({
  selector: 'app-template-message',
  templateUrl: './template-message.component.html',
  styleUrls: ['./template-message.component.css']
})
export class TemplateMessageComponent implements OnInit {

  @Input() templateMsg: any;
  @Input() isUnicode: any;
  @Input() entityId: any;
  @Input() Editable: boolean;
  @Input() trafficType: any;
  @Input() intlMsgBased: any;
  @Input() tempType: string;
  @Input() columnName: string;
  @Input() showWarning: string;

  newMsgContentForInternational: any;

  msgInfoText = "";

  type = "column";

  subscriptionFile: Subscription;

  showFileContents = false;

  dir = "ltr";

  openClearModal = false;

  source = "message";

  isText = true;

  Unicode: boolean;



  regExHeadersFromMsg = CONSTANTS.MSG_CURLY_REMOVE_PATTERN;

  @Output() tempLangType = new EventEmitter<any>();

  @Output() messageIsUnicode = new EventEmitter<any>();
  @Output() MessageContent = new EventEmitter<any>();



  public newTemplateMsgFormGroup: any;

  public newMsgFormGroup: any;

  templateLangType: any;

  @Output() MessageType = new EventEmitter<any>();



  shortner: any;

  shortners: any;

  public vlForm: any;

  newmsg: any;

  IntlmsgInfoText = "";

  dltMsgInfoText: any;

  hide: boolean = false;

  finalMsgValue = "";

  public colBasedInfoTxt = CONSTANTS.INFO_TXT.templateColBased;

  public indexBasedInfoTxt = CONSTANTS.INFO_TXT.templateIndexBased;

  constructor(private msgpartservice: PostMsgPartsService, 
    private controlContainer: ControlContainer,
    private templateSvc: TemplateCampaignService,
    public c_service: CampaignsService,
    private localStorageService:LocalStorageService

  ) { }

  ngOnChanges(changes: SimpleChanges): void {
    this.newTemplateMsgFormGroup = this.controlContainer.control;
    if (this.templateMsg && this.trafficType != 'other') {
      if (this.Editable) {
        console.log("!!DLT selected")
        //this.msgpartcount(this.templateMsg);
        this.listenForTextAreaChanges();
      } else {
        //  console.log("**  ** Created template selected", this.messageType)
        if (this.isUnicode == 'unicode') {
          this.Unicode = true;

        } else {
          this.Unicode = false;

        }
      //  this.Unicode = this.isUnicode;
        // this.msgpartcount(this.templateMsg);//API needed??

      }

    }


    if (this.newTemplateMsgFormGroup && this.trafficType != 'other') {
      console.log('inside came');
      this.dltMsgInfoText = this.templateMsg;
      this.newTemplateMsgFormGroup.controls.tempmessage.setValue(this.templateMsg);
      this.newmsg = this.templateMsg;
    }

    if (this.intlMsgBased == 'column') {
      this.IntlmsgInfoText = this.colBasedInfoTxt;
    } else {
      this.IntlmsgInfoText = this.indexBasedInfoTxt;

    }
    //   if(this.newTemplateMsgFormGroup.contains("tempmessage")){
    //     console.log('inside listen');
    //     if( this.Editable){
    //       console.log("!!DLT selected")
    //       this.msgpartcount(this.templateMsg);
    //     }
    //  //   this.listenForTextAreaChanges();
    //   }




  }
  ngOnInit(): void {

    this.newTemplateMsgFormGroup = this.controlContainer.control;

    //this.newMsgFormGroup = this.controlContainer.control;
    // console.log(this.trafficType ,'ngonint');

    if (this.trafficType == 'other') {
      this.dltMsgInfoText = "";
      this.newTemplateMsgFormGroup.controls.tempmessage.setValue('');
      this.listenForTextAreaChanges();
      // this.newmsg = this.templateMsg;
    } else {
      this.dltMsgInfoText = this.templateMsg;
      this.newTemplateMsgFormGroup.controls.tempmessage.setValue(this.templateMsg);
      this.newmsg = this.templateMsg;
    }

    this.setValidatorsToFormFields();

    this.vlForm = this.controlContainer.control;

    let vlshortner1 = this.localStorageService.getLocal("user");
    this.shortner = JSON.parse(vlshortner1);
    this.shortners = this.shortner.vl_shortner;

  }
  listenForTextAreaChanges() {
    this.tempmessage.valueChanges
      .pipe(
        debounceTime(400),
        distinctUntilChanged(),
        switchMap(msg => {
          
          const trimmedValue = msg.trim();
          console.log(this.finalMsgValue,trimmedValue.length);
          
          if (this.dltMsgInfoText != undefined && trimmedValue.length > 0) {
            this.presetValues(msg);
            console.log(msg);
          
            
            // console.log(this.finalMsgValue,"*********    in debounce ******")
            return this.msgpartservice.getMsgPartsInfo(this.finalMsgValue)
              .pipe(
                catchError((err) => {

                  this.spinner = false;
                  this.apiError = true;

                  return EMPTY;
                })//catch is added to continue further api call if error occurs in between
              );
          } else {
            this.spinner = false;
            this.apiError = false;
            return EMPTY;

          }


        })
      ).subscribe((res) => {
        this.spinner = false;
        console.log('in debounce');

        this.apiError = false;
        if (res.statusCode > 299 || res.statusCode < 200) {
          this.apiError = true;
        } else {
          this.newTemplateMsgFormGroup.controls.tempmessage.setErrors(null);

          if (res.isUnicode == true) {
            this.Unicode = true;
            this.templateLangType = 'Unicode';
            let value = {
              lType: 'unicode',
              msg: res.msg
            }

            this.tempLangType.emit(value);
          } else {
            this.Unicode = false;
            this.templateLangType = 'Text';
            let value = {
              lType: 'english',
              msg: res.msg
            }
            this.messageIsUnicode.emit(this.Unicode);
            this.tempLangType.emit(value);
          }
        }
      },
        (err) => {
          this.spinner = false;
          this.apiError = true;
          this.setApiError();
          return err;;
        })
  }

  setValidatorsToFormFields() {
    this.newTemplateMsgFormGroup.controls.tempmessage.setValidators([
      Validators.required,
    ]);
    this.newTemplateMsgFormGroup.controls.tempmessage.updateValueAndValidity();
  }


  openModal() {
    this.openClearModal = true;
  }

  onToggleShow(event: boolean) {
    this.showFileContents = event;
  }

  onChange(event) {

    if (this.shortners == 1) {
      this.shortners = 1
    } else {
      this.shortners = 0
    }
  }

  get vlshortner() {
    return this.vlForm.controls.vlShortner;
  }

  // get intltempmessage() {
  //   return this.newMsgFormGroup.controls.tempmessage;
  // }

  get tempmessage(): FormControl {
    return this.newTemplateMsgFormGroup.controls.tempmessage;
  }

  msg: any;
  spinner = false;
  apiError = false;

  retryApi() {
    this.msgpartcount(this.msg);
  }
  presetValues(event) {

    let msgValue = event.trim();
    this.spinner = true;
    this.apiError = true;
    this.msg = msgValue;
    this.staticCheck();
    const patternMatch = this.regExHeadersFromMsg;

    let replacevalue = msgValue.replace(patternMatch, "");
    this.finalMsgValue = replacevalue.trim();

    this.setApiError();


  }
  setApiError() {
    try {
      this.newTemplateMsgFormGroup.controls.tempmessage.setErrors({ apiRequestError: true });
    } catch (error) {
      this.c_service.campaignTempApiErr = true;
    }
  }

  msgpartcount(event: string) {
    if (event != undefined) {
      this.presetValues(event)
      if (this.finalMsgValue.length > 0) {
        this.spinner = true;
        this.apiError = false;
        console.log("NOT in debounce")
        this.msgpartservice
          .getMsgPartsInfo(this.finalMsgValue)
          .subscribe(
            res => {
              this.spinner = false;
              this.apiError = false;
              if (res.statusCode > 299 || res.statusCode < 200) {
                this.apiError = true;
              } else {
                try {
                  this.newTemplateMsgFormGroup.controls.tempmessage.setErrors(null);
                } catch (error) {
                  this.c_service.campaignTempApiErr = false;
                }



                if (res.isUnicode == true) {
                  this.Unicode = true;
                  this.templateLangType = 'Unicode';
                  let value = {
                    lType: 'unicode',
                    msg: res.msg
                  }

                  this.tempLangType.emit(value);
                } else {
                  this.Unicode = false;
                  this.templateLangType = 'Text';
                  let value = {
                    lType: 'english',
                    msg: res.msg
                  }
                  this.messageIsUnicode.emit(this.Unicode);
                  this.tempLangType.emit(value);
                }
              }
            },
            (err) => {
              this.spinner = false;
              this.apiError = true;
              this.setApiError();
              return err;
            }

          );
      }

    }





  }

  msgContent() {

    console.log('new message');

    this.newmsg = this.newMsgContentForInternational;
    this.newTemplateMsgFormGroup.controls.tempmessage.setValue(this.newmsg);
    this.MessageContent.emit(this.newmsg);
    this.msgpartcount(this.newmsg);
  }


  messageType: boolean = true;
  staticCheck() {
    // let msgtype;

    if (this.msg.match(this.regExHeadersFromMsg)) {
      this.messageType = false;
      //  this.newTemplateMsgFormGroup.controls.isStatic.setValue(false)
    }
    else {
      this.messageType = true;
      // this.newTemplateMsgFormGroup.controls.isStatic.setValue(true)
    }
    this.MessageType.emit(this.messageType);

  }
  get msgType() {

    return this.newTemplateMsgFormGroup.controls.isStatic.value
  }
  ngOnDestroy() {
    if (this.subscriptionFile !== undefined) {
      this.subscriptionFile.unsubscribe();
    }

    this.templateSvc.populateSelectedTemplate(undefined);
    this.templateSvc.populateSelectedDltTemplate(undefined);

  }


  /*
  dltmsg() {
    //  console.log(this.dltMsgInfoText,'inside value');
    if (this.dltMsgInfoText != undefined) {

      this.msgpartcount(this.dltMsgInfoText);

    }

  }
  */

  setValue() {

    let dltcontent = this.dltMsgInfoText.trim();
    this.newTemplateMsgFormGroup.controls.tempmessage.setValue(dltcontent);
    this.newTemplateMsgFormGroup.controls.template.setValue(null)
    console.log(this.newTemplateMsgFormGroup);

    this.MessageContent.emit(dltcontent);
  }

  focus() {
    const focus = document.getElementById('newmessage') as HTMLImageElement;
    if (focus) {
      focus.focus();
    }
  }
}
