import { CommonModule } from "@angular/common";
import { NgModule } from "@angular/core";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
//import { BrowserModule } from "@angular/platform-browser";
import { NgSelectModule } from "@ng-select/ng-select";
import { DateRangePickerModule } from "@syncfusion/ej2-angular-calendars";
import { CampaignsModule } from "../campaigns/campaigns.module";
import { SharedModule } from "../shared/shared.module";
import { ReportCalenderComponent } from "./report-calender/report-calender.component";
import { ReportsDetailedComponent } from "./reports-detailed/reports-detailed.component";
import { ReportsHeaderComponent } from "./reports-header/reports-header.component";
import { ReportsSearchComponent } from "./reports-search/reports-search.component";
import { ReportsSummaryComponent } from "./reports-summary/reports-summary.component";
import { ReportRoutingModule } from "./reports.routing.module";
import { FiltercomponentComponent } from "src/app/reports/filtercomponent/filtercomponent.component";
import { Ng2SearchPipeModule } from "ng2-search-filter";
import { NgxPaginationModule } from "ngx-pagination";
import { ReportListFilter } from "src/app/reports/Helpers/report.detail.filter";
import { TwoWaySearchPipe } from "./Helpers/two-way-search.pipe";
import { DownloaderrorpopupComponent } from "src/app/reports/Helpers/downloaderrorpopup/downloaderrorpopup.component";
import { TooltipModule } from 'ng2-tooltip-directive';

@NgModule({
    declarations: [
        ReportsDetailedComponent,
        ReportsSummaryComponent,
        ReportsSearchComponent,
        ReportsHeaderComponent,
        FiltercomponentComponent,
        ReportCalenderComponent,
        ReportListFilter,
        TwoWaySearchPipe,
        DownloaderrorpopupComponent
       
        
    ],
    imports: [
        CommonModule,
        FormsModule,
        NgSelectModule,
        ReportRoutingModule,
       // BrowserModule,
        CampaignsModule,
        DateRangePickerModule,
        NgSelectModule,
        SharedModule,
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        Ng2SearchPipeModule,
        NgxPaginationModule,
        TooltipModule
       

    ],
    exports:[
       ReportsHeaderComponent,
       ReportCalenderComponent
    ]
})
export class ReportsModule {}
