import { HttpClient, HttpHeaders } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { map ,catchError } from "rxjs/operators";
import { TemplateModel } from "../model/templatemodal";
import { CONSTANTS_URL } from "../../shared/compaign.url";
import { BehaviorSubject, throwError } from 'rxjs';
import { ERROR } from "src/app/campaigns/error.data";
import { FormControl, FormGroup } from "@angular/forms";
import { SelectMobileColumnComponent } from "src/app/shared/components/select-mobile-column/select-mobile-column.component";
import { TemplateNameComponent } from "src/app/shared/components/template-name/template-name.component";
import { CreateNewTemplateMessageComponent } from "src/app/templates/create-new-template/create-new-template-message/create-new-template-message.component";
import { TemplateCampaignService } from "src/app/campaigns/campaign-template/service/template-campaign.service";
@Injectable({
    providedIn: "root"
})
export class TemplateService {
    BASE_URL = CONSTANTS_URL.GLOBAL_URL;

    API_URL = CONSTANTS_URL.ALL_TEMPLATES;
    API_TIDURL = CONSTANTS_URL.TEMPLATES_API_TINFO_URL;
    API_DELETEURL = CONSTANTS_URL.TEMPLATES_API_DELETEURL;
    API_UPDATEURL = CONSTANTS_URL.TEMPLATES_API_UPDATEURL;
    API_CREATEURL = CONSTANTS_URL.TEMPLATES_API_CREATEURL;
    API_DLTTEMPLATE_URL = CONSTANTS_URL.TEMPLATES_API_DLTTEMPLATE_URL;
    GET_MSG_PARTS_API = CONSTANTS_URL.GET_MSG_PARTS_API;

// error strings
badError : any ;

    tname: TemplateNameComponent;

    tmsg:CreateNewTemplateMessageComponent;

    mobcol:SelectMobileColumnComponent;

    constructor(public http: HttpClient, private temservice:TemplateCampaignService) {}

    httpOptions = {
        headers: new HttpHeaders({
            Accept: "application/json",
            "Content-Type": "application/json"
        })
    };

    setMobCol(control:SelectMobileColumnComponent){
        this.mobcol = control;
    }

    setTname(control:TemplateNameComponent){
        this.tname = control;
    }

    setmsgArea(control:CreateNewTemplateMessageComponent){
        this.tmsg = control;
    }
templateLoading= new BehaviorSubject<boolean>(false)
    getTemplatesList() {
        this.templateLoading.next(true)
        return this.http
            .get<TemplateModel>(
                this.BASE_URL + this.API_URL,

                this.httpOptions
            )
            .pipe(
                map((responseData : any) => {
                    this.templateLoading.next(false)
                    return responseData as any;
                }),
                catchError((err) => {
                    this.templateLoading.next(false)
                    if (err.status == 0) {
                            
                        let setError = ERROR.REQUEST_NOT_SEND;

                        this.badError = setError;
                    }else {
                        let setError = ERROR.SOMETHING_WENT_WRONG;
                        this.badError =setError;
                    }                    
                    return this.badError;
                })
                );
            // .pipe(map((data) => data as TemplateModel));
    }

    templateEditNameLoading= new BehaviorSubject<boolean>(false)


     getTemplateByGivenId(id : any)   {
        this.templateEditNameLoading.next(true);
        return this.http
            .get<TemplateModel>(this.BASE_URL + this.API_TIDURL +id , this.httpOptions )
            .pipe(
                map((responseData : any) => {
                    this.templateEditNameLoading.next(false);
                    return responseData as any;
                }),
                catchError((err) => {
                    this.templateEditNameLoading.next(false);
                    if (err.status == 0) {
                            
                        let setError = ERROR.REQUEST_NOT_SEND;

                        this.badError = setError;
                    }else {
                        let setError = ERROR.SOMETHING_WENT_WRONG;
                        this.badError =setError;
                    }
                    return this.badError;
                })
                );
            
            // .pipe(map((data) => data as TemplateModel));
      }

      
     deleteSingleTemplate(id : any)   {
       return this.http
            .post(this.BASE_URL + this.API_DELETEURL,id , this.httpOptions ) 
            .pipe(
                map((responseData : any) => {
                    return responseData as any;
                }),
                catchError((err) => {
                    if (err.status == 0) {
                            
                        let setError = ERROR.REQUEST_NOT_SEND;

                        this.badError = setError;
                    }else {
                        let setError = ERROR.SOMETHING_WENT_WRONG;
                        this.badError =setError;
                    }
                    return this.badError;
                })
            );
      }

      templateUpdate(value: any): Observable<any> {
        let payload = {
            "t_name": value.templateName,
            "id": value.id
          }
        return this.http
            .post(
                this.BASE_URL + this.API_UPDATEURL,
                payload ,
                this.httpOptions
            )
            .pipe(
                map((responseData : any) => {
                    return responseData as any;
                }),
                catchError((err) => {
                    if (err.status == 0) {
                            
                        let setError = ERROR.REQUEST_NOT_SEND;

                        this.badError = setError;
                    }else {
                        let setError = ERROR.SOMETHING_WENT_WRONG;
                        this.badError =setError;
                    }
                    return this.badError;
                })
            );
    }

    createTemplate(value: any): Observable<any> {

        return this.http
            .post(
                this.BASE_URL + this.API_CREATEURL,
                value ,
                this.httpOptions
            )
            .pipe(
                map((responseData : any) => {
                    return responseData as any;
                }),
                catchError((err) => {

                    if (err.status == 0) {
                            
                        let setError = ERROR.REQUEST_NOT_SEND;

                        this.badError = setError;
                    }else {
                        let setError =ERROR.SOMETHING_WENT_WRONG;
                        this.badError =setError;
                    }
                    return this.badError;
                    // return throwError(err);
                })
            );
    }

    public loadingDLT_List = new BehaviorSubject<boolean>(false);


    findAllDltTemplates(id : any)   {
        this.loadingDLT_List.next(true)
      return this.http
           .get(this.BASE_URL + this.API_DLTTEMPLATE_URL+id , this.httpOptions ) 
           .pipe(
            map((responseData : any) => {
                this.loadingDLT_List.next(false)
                return responseData as any;
            }),
            catchError((err) => {       
                this.loadingDLT_List.next(false)    
                return throwError(err);
            })
        );
     }

   

     validateTemplateFormFields(InputForm: FormGroup){

        const formGroup = InputForm;
        const templateErr = this.temservice.templateErr;
        Object.keys(formGroup.controls).forEach((field)=>{
            const control = formGroup.get(field);
            if (control instanceof FormControl) {
                if (control.errors?.required ) {
                    
                    control.markAsTouched( { onlySelf:true } )
                }
                
            }
        });

        if((this.tname && InputForm.controls.templateName.invalid) || templateErr ){
            this.tname.focus();
        }else if (this.mobcol && InputForm.controls.mobileColumn?.invalid) {

            this.mobcol.setFocus();
        }else if(this.tmsg && InputForm.controls.newmessage?.invalid){
            this.tmsg.focus();
        }

    

     }


      
  getMsgPartsInfo(paramValue : string){
    return this.http.post(this.BASE_URL+this.GET_MSG_PARTS_API, {msg : paramValue}).pipe(
        map((responseData : any) => {
            
            //if(responseData.statusCode >299){
              
            return responseData as any;
        }),
        catchError((err) => {
          this.badError = err.error;
            return throwError(err);
        })
    );
}
}
