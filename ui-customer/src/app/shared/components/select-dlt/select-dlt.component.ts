import { Component, EventEmitter, Input, OnInit, Output } from "@angular/core";
import { ControlContainer } from "@angular/forms";
import { Subscription } from "rxjs";
import { TemplateCampaignService } from "src/app/campaigns/campaign-template/service/template-campaign.service";


@Component({
    selector: "app-select-dlt",
    templateUrl: "./select-dlt.component.html",
    styleUrls: ["./select-dlt.component.css"]
})
export class SelectDltComponent implements OnInit {
    selectDLTFormGroup: any;

    placeHolder = "Select DLT template";
    @Input() itemList : any;
    
    subscription2 : Subscription;
    @Output() selectedDLT = new EventEmitter<string>();

    constructor(
        private templateSvc: TemplateCampaignService,
        private controlContainer: ControlContainer
    ) {}

    ngOnInit(): void {
        this.selectDLTFormGroup = this.controlContainer.control;
        const prevValue = this.templateSvc.getNewTemplate();
       // if (prevValue.dlt !== "") {
            
        //    this.selectDLTFormGroup.controls.dlt.setValue(prevValue.dlt);
        //}
    }

    getDropdowns() {
        return this.itemList;
    }

    get dlt() {
        return this.selectDLTFormGroup.controls.dlt;
    }

    changeDLT(event: any) {
        this.templateSvc.populateNewTemplateDLT(event);
        this.selectDLTFormGroup.controls.dlt.setValue(event);
        this.selectedDLT.emit(event);
        this.templateSvc.sendDLTselected(event);
        // const selectedDLT = this.DLTList.filter(value => value.dltId === 'event');
    }
}
