import { NgModule } from "@angular/core";
import { BrowserModule } from "@angular/platform-browser";
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";

import { HttpClientModule, HTTP_INTERCEPTORS } from "@angular/common/http";
import { NgxPaginationModule } from "ngx-pagination";
import { Ng2SearchPipeModule } from "ng2-search-filter";
import {
    CommonModule,
    LocationStrategy,
    HashLocationStrategy
} from "@angular/common";

import { AppRoutingModule } from "./app-routing.module";
import { AppComponent } from "./app.component";
import { DashboardComponent } from "./dashboard/dashboard.component";
import { SummaryComponent } from "./dashboard/summary/summary.component";
import { CampaignListComponent } from "./dashboard/campaign-list/campaign-list.component";
import { StatisticsComponent } from "./dashboard/statistics/statistics.component";
import { CampaignActivitiesComponent } from "./dashboard/campaign-activities/campaign-activities.component";
import { ScheduledCampaignsComponent } from "./dashboard/scheduled-campaigns/scheduled-campaigns.component";
import { CampaignsModule } from "./campaigns/campaigns.module";
import { CoreModule } from "./core/core.module";
import { GroupsManagementModule } from "./groups-management/groups-management.module";
import { TemplateModule } from "./templates/template.module";
import { TemplateCampaignService } from "./campaigns/campaign-template/service/template-campaign.service";
import { GroupsCampaignService } from "./campaigns/groups/groups-campaign.service";
import { GroupsManagementService } from "./groups-management/groups-management.service";
import { AuthenticationModule } from "./authentication/authentication.module";
import { AuthInterceptorService } from "./authentication/auth-interceptor.service";
//import { AccountModule } from "./account/account.module";
import { ReportsComponent } from './reports/reports.component';
import { ReportsModule } from "./reports/reports.module";
import { FrameComponent } from "./layouts/frame/frame.component";
import { LoginLayoutComponent } from './layouts/login-layout/login-layout.component';
import { PageNotFoundComponent } from './page-not-found/page-not-found.component';
import { DltfileUploadComponent } from "./dltfile-upload/dltfile-upload.component";
import { DLTFileUploadModule } from "src/app/dltfile-upload/dltfile-upload.module";
import { DownloadsComponent } from './downloads/downloads.component';
import { DownloadsModule } from "./downloads/downloads.module";
// import { DateRangePickerModule } from '@syncfusion/ej2-angular-calendars';
import { NgApexchartsModule } from "ng-apexcharts";
import { PageNotFound404Component } from './page-not-found404/page-not-found404.component';
import { UnauthorizedPageComponent } from './unauthorized-page/unauthorized-page.component';
import { CampaignsService } from "./campaigns/campaigns.service";
import { CommonService } from "./shared/commonservice";
// import { TooltipModule } from 'ng2-tooltip-directive';



@NgModule({
    declarations: [
        AppComponent,
        DashboardComponent,
        SummaryComponent,
        CampaignListComponent,
        StatisticsComponent,
        CampaignActivitiesComponent,
        ScheduledCampaignsComponent,
        ReportsComponent,
        FrameComponent,
        LoginLayoutComponent,
        PageNotFoundComponent,
        DltfileUploadComponent,
        DownloadsComponent,
        PageNotFound404Component,
        UnauthorizedPageComponent,


        // DateRangePickerModule
    ],

    imports: [
        BrowserModule,

        BrowserAnimationsModule,
        //AccountModule,
        // TemplateModule,
        HttpClientModule,
        //  DLTFileUploadModule,
        //  CampaignsModule,
        //  GroupsManagementModule,
        // ReportsModule,
        // DownloadsModule,
        CoreModule,
        CommonModule,
        NgxPaginationModule,
        Ng2SearchPipeModule,
        AuthenticationModule,
        NgApexchartsModule,
        AppRoutingModule,
        // TooltipModule

    ],
    providers: [
        {
            provide: HTTP_INTERCEPTORS,
            useClass: AuthInterceptorService,
            multi: true
        },
        { provide: LocationStrategy, useClass: HashLocationStrategy },
        TemplateCampaignService,
        GroupsCampaignService,
        CampaignsService,
        GroupsManagementService,
        CommonService
    ],
    exports: [FrameComponent],
    bootstrap: [AppComponent]
})
export class AppModule { }
