import { NgModule } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { NgSelectModule } from "@ng-select/ng-select";
import { Ng2SearchPipeModule } from "ng2-search-filter";
import { DateRangePickerModule } from "@syncfusion/ej2-angular-calendars";
import { NgxPaginationModule } from "ngx-pagination";
import { FilterPipe } from "src/app/campaigns/c_Helper/searchfilter";
import { campaignScheduleListFilter } from "src/app/campaigns/c_Helper/schedule.filter";
import { CampaignsRoutingModule } from "./campaigns-routing.module";
import { CampaignsComponent } from "./campaigns.component";
import { HomeComponent } from "./home/home.component";
import { DropzoneModule } from "ngx-dropzone-wrapper";

import { OneToManyComponent } from "./onetomany/onetomany.component";
import { SharedModule } from "../shared/shared.module";
import { QuickSmsComponent } from "./quick-sms/quick-sms.component";
import { CampaignNameComponent } from "./campaign-name/campaign-name.component";
import { QuickSmsMobileNumbersComponent } from "./quick-sms/quick-sms-mobile-numbers/quick-sms-mobile-numbers.component";
import { CampaignMessageComponent } from "./campaign-message/campaign-message.component";
import { CampaignHeaderComponent } from "./campaign-header/campaign-header.component";
import { ManyToManyComponent } from "./manytomany/manytomany.component";
import { QuickSMSCampaignService } from "./quick-sms/service/quick-sms-campaigns-service";
import { GroupsComponent } from "./groups/groups.component";
import { AddGroupComponent } from "./groups/add-group/add-group.component";
import { ExcludeGroupComponent } from "./groups/exclude-group/exclude-group.component";
import { RemoveDuplicatesComponent } from "./remove-duplicates/remove-duplicates.component";
import { CampaignTemplateComponent } from "./campaign-template/campaign-template.component";
import { TemplateFilterPipe } from "./campaign-template/template.fuzzyFilter.pipe";
import { CampaignSenderIdComponent } from "./campaign-sender-id/campaign-sender-id.component";
import { SelectTemplateComponent } from "./campaign-template/select-template/select-template.component";
import { PreviewTemplateComponent } from "./campaign-template/preview-template/preview-template.component";
import { CampaignDetailsComponent } from "./campaign-details/campaign-details.component";
import { CampaignListComponent } from "./campaign-list/campaign-list.component";
import { CampaigncardComponent } from './campaigncard/campaigncard.component';
import { CampaigncalenderComponent } from './campaigncalender/campaigncalender.component';
import { EmptyRecordComponent } from './c_Helper/empty-record/empty-record.component';
import { LoadingskeletonComponent } from './c_Helper/loadingskeleton/loadingskeleton.component';
import { NorecordsDaterangeComponent } from './c_Helper/norecords-daterange/norecords-daterange.component';
import { CampaigncardDetailComponent } from './campaigncard-detail/campaigncard-detail.component';
import { CampaignScheduledDetailComponent } from './campaign-scheduled-detail/campaign-scheduled-detail.component';
import { CampaignReschedulerComponent } from './campaign-rescheduler/campaign-rescheduler.component';
import { CScheduleListComponent } from './c-schedule-list/c-schedule-list.component';
import { SchedulecalenderComponent } from './schedulecalender/schedulecalender.component';
import { TemplateMessageComponent } from './campaign-template/template-message/template-message.component';
import { CampaignNameTemplateComponent } from './campaign-template/campaign-name-template/campaign-name-template.component';
import { NobalancestyleComponent } from './nobalancestyle/nobalancestyle.component';
import { CampaignHomeComponent  } from "src/app/campaigns/campaign-home/campaign-home.component";
import { CampaignsDropdownComponent } from "./campaigns-dropdown/campaigns-dropdown.component";
import { TrafficTypeComponent } from "./traffic-type/traffic_type.component";
import { TooltipModule } from 'ng2-tooltip-directive';

import { CtfileuploadComponent } from './campaign-template/ctfileupload/ctfileupload.component';
import { IntlSendersComponent } from './intl-senders/intl-senders.component';
import { SelectedtrafficComponent } from './selectedtraffic/selectedtraffic.component';
@NgModule({
    declarations: [
        CampaignsComponent,
        HomeComponent,
        OneToManyComponent,
        QuickSmsComponent,
        CampaignNameComponent,
        QuickSmsMobileNumbersComponent,
        CampaignMessageComponent,
        CampaignHeaderComponent,
        ManyToManyComponent,
        GroupsComponent,
        AddGroupComponent,
        ExcludeGroupComponent,
        CampaignTemplateComponent,
        RemoveDuplicatesComponent,
        CampaignSenderIdComponent,
        TemplateFilterPipe,
        SelectTemplateComponent,
        PreviewTemplateComponent,
        CampaignDetailsComponent,
        CampaignHomeComponent,
        CampaignListComponent,
        CampaigncardComponent,
        CampaigncalenderComponent,
        FilterPipe,
        EmptyRecordComponent,
        LoadingskeletonComponent,
        NorecordsDaterangeComponent,
        CampaigncardDetailComponent,
        CampaignScheduledDetailComponent,
        CampaignReschedulerComponent,
        CScheduleListComponent,
        SchedulecalenderComponent,
        campaignScheduleListFilter,
        TemplateMessageComponent,
        CampaignNameTemplateComponent,
        CampaignsDropdownComponent,
        NobalancestyleComponent,
        TrafficTypeComponent,
        CtfileuploadComponent,
        IntlSendersComponent,
        SelectedtrafficComponent,
        
        //
    ],
    imports: [
        CommonModule,
        FormsModule,
        NgSelectModule,
        CampaignsRoutingModule,
        ReactiveFormsModule,
        SharedModule,
        DropzoneModule,
        Ng2SearchPipeModule,
        DateRangePickerModule,
        NgxPaginationModule,
        TooltipModule,
    ],

    exports: [CampaignNameComponent, CampaigncalenderComponent],

    providers: [QuickSMSCampaignService]
})
export class CampaignsModule {}
