import { Component, EventEmitter, Input, OnInit, Output, ViewChild } from "@angular/core";
import { ControlContainer } from "@angular/forms";
import { Subscription } from "rxjs";
import { TemplateCampaignService } from "src/app/campaigns/campaign-template/service/template-campaign.service";
import { CONSTANTS } from "../../campaigns.constants";
import { NgSelectComponent } from "@ng-select/ng-select";
@Component({
    selector: "app-select-mobile-column",
    templateUrl: "./select-mobile-column.component.html",
    styleUrls: ["./select-mobile-column.component.css"]
})
export class SelectMobileColumnComponent implements OnInit {

    @ViewChild('ngSelect') public ngSelect: NgSelectComponent;

    public mobileColInfoTxt = CONSTANTS.INFO_TXT.templateMobileColumn;
    
    @Output() mobileColumnResponse = new EventEmitter<string>();
    
    fileDetails: any;

    mobileColumns: string[];

    mobileColFormGroup: any;

    subscriptionFile: Subscription;
    // type = "column";
    type:any;
    placeholder = "Select Mobile Number Column";
    

    constructor(
        private templateSvc: TemplateCampaignService,
        private controlContainer: ControlContainer
    ) {
        //console.log(this.templateSvc.basedOn,'basedon');
        
       // this.templateSvc.sendBasedOn("column");
    }

    ngOnInit(): void {
        this.type = this.templateSvc.getBasedOn();
       
        this.mobileColFormGroup = this.controlContainer.control;
        this.fileDetails = this.templateSvc.getFirstFileDetails();

        if (this.fileDetails) {
            this.type = this.templateSvc.basedOn
            this.placeholder = "Select Mobile Number "+this.type;
            if (this.type === "index") {
                this.mobileColumns = this.fileDetails.file_contents_index[0];
            } else  {
                this.mobileColumns = this.fileDetails.file_contents_column[0];

            } 
            this.mobileColFormGroup.controls.mobileColumn
            .setValue(null);
           
        }
        const prevValue = this.templateSvc.getNewTemplate();
        this.setFirstOrPreviousValue(prevValue.t_mobile_column);
    //    this.type =  this.templateSvc.getBasedOn();

    //    console.log(this.type);
       

        this.subscriptionFile = this.templateSvc
            .getBasedOn()
            .subscribe((data) => {
             this.type = data;
             this.placeholder = "Select Mobile Number "+data;
                if (data === "column" ) {
                    this.mobileColumns = this.fileDetails.file_contents_column[0];
                    this.setFirstOrPreviousValue(prevValue.t_mobile_column);
                } else if (data === "index") {
                    this.mobileColumns = this.fileDetails.file_contents_index[0];
                    this.setFirstOrPreviousValue(prevValue.t_mobile_column);
                }
            });
    }
    setFirstOrPreviousValue(previousValue : string){
        let toBeMobileCoumnVal = null;
                
        if(this.mobileColumns.includes(previousValue)){
            toBeMobileCoumnVal = previousValue;
            
        }else{
            //toBeMobileCoumnVal = this.mobileColumns[0];
            this.mobileColFormGroup.controls.mobileColumn.markAsUntouched();
            toBeMobileCoumnVal = null;
            
        }  
        this.mobileColFormGroup.controls.mobileColumn
        .setValue(toBeMobileCoumnVal);
        if(toBeMobileCoumnVal !== null){
            this.mobileColumnResponse.emit(toBeMobileCoumnVal);
        }
        
    }

    onSelectMobCol(event: any) {
        this.mobileColumnResponse.emit(event);
        this.templateSvc.populateNewTemplateMobileCol(event);
    }

    get mobileCol() {
        return this.mobileColFormGroup.controls.mobileColumn;
    }

    ClearValue(){
        this.ngSelect.handleClearClick();
    }

    setFocus(){
        this.ngSelect.focus();
    }
}
