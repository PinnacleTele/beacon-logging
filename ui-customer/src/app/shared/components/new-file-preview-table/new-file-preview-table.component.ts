import {
    Component,
    EventEmitter,
    Input,
    OnDestroy,
    OnInit,
    Output
} from "@angular/core";
import { Subscription } from "rxjs";
import { TemplateCampaignService } from "src/app/campaigns/campaign-template/service/template-campaign.service";
import { CONSTANTS } from "src/app/shared/campaigns.constants";
@Component({
    selector: "app-new-file-preview-table",
    templateUrl: "./new-file-preview-table.component.html",
    styleUrls: ["./new-file-preview-table.component.css"]
})
export class NewFilePreviewTableComponent implements OnInit, OnDestroy {
    fileHeaders: string[];

    showFileContents = false;

    lines: any =[];

    type: any ;

    @Output() selectedHeader = new EventEmitter<string>();

    subscriptionFile: Subscription;

    limit = CONSTANTS.TemplateFilePreviewTableLimit;
    fileDetails :any;

    constructor(private templateSvc: TemplateCampaignService) {}

    ngOnInit() {
        this.fileDetails = this.templateSvc.getFirstFileDetails();
        if (this.fileDetails) {

            this.type = this.templateSvc.basedOn;
           this.getTableContent()
           
            
        }
        this.subscriptionFile = this.templateSvc
            .getBasedOn()
            .subscribe((type) => {
                this.type = type;
                //console.log("change listened", this.type)
                this.getTableContent()
            });
    }
    
getTableContent(){
    
    this.lines = [];
    
    
    if (this.type === "index") {
        const rowsToDisplay = ( this.fileDetails.file_contents_index.length < this.limit) ?  this.fileDetails.file_contents_index.length: this.limit;
        this.fileHeaders = this.fileDetails.file_contents_index[0];
       // console.log(rowsToDisplay)
        for (let index = 1; index < rowsToDisplay; index++) {

            this.lines.push(this.fileDetails.file_contents_index[index]) ;
             //console.log(this.lines,"§§§§")
       }        
       
     
    } else  {
        const rowsToDisplay = ( this.fileDetails.file_contents_column.length < this.limit) ?  this.fileDetails.file_contents_column.length: this.limit;
        this.fileHeaders = this.fileDetails.file_contents_column[0];
      //  console.log(rowsToDisplay)
        for (let index = 1; index < rowsToDisplay; index++) {
           this.lines.push(this.fileDetails.file_contents_column[index]) ;
//           console.log(this.lines,"§§§§", this.fileHeaders)
      }
       
      

    } 
}
    onClickHeader(selectedHeader: string) {
        this.selectedHeader.emit(selectedHeader);
    }

    toggleFileContent() {
        this.showFileContents = !this.showFileContents;
    }

    ngOnDestroy() {
        this.subscriptionFile.unsubscribe();
    }
}
