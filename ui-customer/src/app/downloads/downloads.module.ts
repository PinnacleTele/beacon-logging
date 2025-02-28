import { CommonModule } from "@angular/common";
import { NgModule } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { NgxPaginationModule } from "ngx-pagination";
// import { ReportsModule } from "../reports/reports.module";
import { SharedModule } from "../shared/shared.module";
import { DateRangePickerModule } from "@syncfusion/ej2-angular-calendars";

import { DownloadSkeletonComponent } from "./download-skeleton/download-skeleton.component";
import { DownloadsRoutingModule } from "./downloads.routing.module";
import { DownloadFilterPipe } from "./Helpers/download-filter.pipe";
import { LogDownloadComponent } from "./log-download/log-download.component";
import { DownloadsCalendarComponent } from "./downloads-calendar/downloads-calendar.component";
import { TooltipModule } from 'ng2-tooltip-directive';

@NgModule({
    declarations: [
        LogDownloadComponent,
        DownloadSkeletonComponent,
        DownloadFilterPipe,
        DownloadsCalendarComponent
        
    ],
    imports: [
        CommonModule,
        FormsModule,
        SharedModule,
        DownloadsRoutingModule,
      //  ReportsModule,
        NgxPaginationModule,
        DateRangePickerModule,
        TooltipModule
        

    ],
    exports:[
      
    ]
})
export class DownloadsModule {}