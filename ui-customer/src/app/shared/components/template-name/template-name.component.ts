import { Component, EventEmitter, Input, OnInit, Output } from "@angular/core";
import { ControlContainer } from "@angular/forms";
import { BehaviorSubject ,throwError} from "rxjs";
import { map,catchError, debounceTime, distinctUntilChanged, startWith, pairwise } from "rxjs/operators";
import { TemplateCampaignService } from "src/app/campaigns/campaign-template/service/template-campaign.service";
import { CONSTANTS } from "../../campaigns.constants";
import { HttpClient, HttpErrorResponse } from "@angular/common/http";
import { THIS_EXPR } from "@angular/compiler/src/output/output_ast";
import { CONSTANTS_URL } from "../../compaign.url";

import { TemplateService } from "src/app/templates/Helpers/templates.service";

@Component({
    selector: "app-template-name",
    templateUrl: "./template-name.component.html",
    styleUrls: ["./template-name.component.css"]
})
export class TemplateNameComponent implements OnInit {
    public loadingTemplateNames$ = new BehaviorSubject<boolean>(false);

    public noUnicodeTxt = CONSTANTS.ERROR_DISPLAY.noUnicodeText;

    templateNameInfoTxt = CONSTANTS.INFO_TXT.templateName;

    uniqueTemplateNameError = CONSTANTS.ERROR_DISPLAY.uniqueTemplateName;

    public splCharsError = CONSTANTS.ERROR_DISPLAY.fieldSplChars;

    public allowedSplChars = CONSTANTS.allowed_special_characters;

    public minLengthError = CONSTANTS.ERROR_DISPLAY.fieldMinLength;

    public maxLengthError = CONSTANTS.ERROR_DISPLAY.fieldMaxLength;

    public minimumLength = CONSTANTS.minLengthTemplateName;

    public maximumLength = CONSTANTS.maxLengthTemplateName;

    public checkUniqueApiError = 'Something went wrong. Please try again'

    templateNameExists:boolean;

    templateNameFormGroup: any;
    uniqueErr = false;
    serverErr = false;

    uniqueErrCheck = false;
    editNameLoading;

    // serverErr = false;

    public loading$ = this.templateSvc.loadingTemplateNames$.asObservable();

    templateLoading = this.templateService.templateEditNameLoading.subscribe((data) => { this.editNameLoading = data })

    // public editNameLoading = this.templateService.templateEditNameLoading.asObservable();
    

    @Input() editTemplateName :any;

    @Output() Tname = new EventEmitter<string>();

    constructor(
        private templateService :TemplateService,
        private templateSvc: TemplateCampaignService,
        private controlContainer: ControlContainer,
        private http: HttpClient,
    ) {}

    ngOnInit(): void {
        this.templateNameFormGroup = this.controlContainer.control;
        this.templateName.valueChanges.pipe(
            debounceTime(400),
            distinctUntilChanged(),
            startWith(null as string),
		    pairwise(),
          ).subscribe((msg) => {
            let oldValueTrim;
            let newValueTrim;
            if (msg[0] != null) {
                const prev = msg[0];
                 oldValueTrim = prev.trim();
                const newValue = msg[1];
                newValueTrim = newValue.trim();
            } else {
                const newValue = msg[1];
                newValueTrim = newValue.trim();
            }
            const tname = newValueTrim;
            console.log(oldValueTrim,newValueTrim);
            if (oldValueTrim != newValueTrim) {
                this.chkUniqueTemplateNameExists(tname);
            }
          })
    }

    get templateName() {
        return this.templateNameFormGroup.controls.templateName;
    }
    
uniqueError:boolean = false;  

    chkUniqueTemplateNameExists(event:any) {
         this.uniqueErr = false;
         this.serverErr = false;
        this.templateSvc.templateErr =false;
        
        let Templatename = (event as string).trim();
     //  this.templateName.setValue(Templatename);
        
        if (Templatename.length > 0 && !this.templateName.errors) {
            this.Tname.emit(event);
            if (this.editTemplateName !== Templatename) {
                Templatename = Templatename.toLowerCase();
                
                this.findAllGroups(Templatename)
                .subscribe(
                  res => {
                                 
                    // this.templateNameFormGroup.controls.templateName.setErrors({
                    //     apiRequestError: true });
                      if( !res.isUnique){
                          this.uniqueErr = true;
    
                          this.templateSvc.templateErr =true;
                         this.templateNameFormGroup.controls.templateName.setErrors({
                             templateNameExists: true });
                      }
                     
                  },
                  (err) => {
                     this.serverErr = true;
                     this.templateSvc.templateErr =true;
                     this.templateNameFormGroup.controls.templateName.setErrors({
                         apiRequestError: true });
                  }
      
              );
            }
        }
       
         
            
            
      }

      trimValue(){
          const tname = (this.templateName?.value as string).trim();
        this.templateName.setValue(tname);
      }

      isUNIQE_URL= CONSTANTS_URL.GLOBAL_URL + CONSTANTS_URL.T_UNIQUE_NAME_URL;

    findAllGroups(t_name:string) {
        this.loadingTemplateNames$.next(true);
        return this.http.get<{isUnique:string}>(this.isUNIQE_URL+t_name).pipe(
            map((responseData) => {
                this.loadingTemplateNames$.next(false);
                   let  value=responseData                    
                return value;
            }),
            catchError((err) => {
                this.loadingTemplateNames$.next(false);
                return throwError(err);
            })
        );
    }


    retry(){
        //   this.templateNameFormGroup.controls.templateName.setErrors({
        //                  apiRequestError: false });
        const focus = document.getElementById("templateName") as HTMLImageElement;
        focus.focus();
        focus.blur();
    }
    focus() {
        const focus = document.getElementById("templateName") as HTMLImageElement;
        focus.focus();
        // focus.scrollIntoView();
    }
    // chkUniqueTemplateNameExists(event: any) {
    //     this.templateSvc
    //         .findAllTemplates()
    //         .pipe(
    //             map((templateNamesArray) => {
    //                 const templateNamesFromServer: string[] = [];
    //                 templateNamesArray.forEach((data) =>
    //                     templateNamesFromServer.push(data.t_name.toLowerCase())
    //                 );
    //                 if (
    //                     templateNamesFromServer.indexOf(
    //                         (event.target.value as string).toLowerCase()
    //                     ) >= 0
    //                 ) {
    //                     this.templateNameFormGroup.controls.templateName.setErrors(
    //                         {
    //                             templateNameExists: true
    //                         }
    //                     );
    //                 } else {
    //                     this.templateSvc.populateNewTemplateName(
    //                         event.target.value
    //                     );
    //                 }
    //             })
    //         )
    //         .subscribe();
    // }
}
