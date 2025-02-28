import { NgModule } from "@angular/core";
import { CommonModule } from "@angular/common";
import {
    FormsModule,
    RadioControlValueAccessor,
    ReactiveFormsModule
} from "@angular/forms";

import { NgSelectModule } from "@ng-select/ng-select";
import { DropzoneModule } from "ngx-dropzone-wrapper";
import {
    DatePickerModule,
    TimePickerModule, DateRangePickerModule
} from "@syncfusion/ej2-angular-calendars";
import { NgxPaginationModule } from "ngx-pagination";

import { SingleSelectDropdownComponent } from "./single-select-dropdown/single-select-dropdown.component";
import { FileUploaderComponent } from "./file-uploader/file-uploader.component";
import { CancelModalComponent } from "./modal/cancel-modal/cancel-modal.component";
import { DuplicateModalComponent } from "./modal/new-preview-modals/duplicate-modal/duplicate-modal.component";
import { ClearModalComponent } from "./modal/clear-modal/clear-modal.component";
import { DateComponent } from "./date-picker/date-picker.component";
import { TimeComponent } from "./time-picker/time-picker.component";
import { CampaignCancelButtonComponent } from "./campaign-cancel-button/campaign-cancel-button.component";
import { CampaignSaveButtonComponent } from "./campaign-save-button/campaign-save-button.component";
import { CampaignScheduleButtonComponent } from "./campaign-schedule-button/campaign-schedule-button.component";
import { CampaignSendButtonComponent } from "./campaign-send-button/campaign-send-button.component";
import { UploadedFilesComponent } from "./modal/new-preview-modals/uploaded-files/uploaded-files.component";
import { ToastComponent } from "./toast/toast.component";
import { FilterPipe } from "./campaigns-pipes/filter.pipe";
import { PreviewAddedGroupsComponent } from "./modal/new-preview-modals/preview-added-groups/preview-added-groups.component";
import { PreviewExcludedGroupsComponent } from "./modal/new-preview-modals/preview-excluded-groups/preview-excluded-groups.component";
import { SelectMobileColumnComponent } from "./components/select-mobile-column/select-mobile-column.component";
import { FileBasedOnComponent } from "./components/file-based-on/file-based-on.component";
import { SelectDltComponent } from "./components/select-dlt/select-dlt.component";
import { TemplateNameComponent } from "./components/template-name/template-name.component";
import { NewPreviewModalsComponent } from "./modal/new-preview-modals/new-preview-modals.component";
import { NewPreviewModalMessageComponent } from "./modal/new-preview-modals/new-preview-modal-message/new-preview-modal-message.component";
import { NewPreviewModalMobileNumbersComponent } from "./modal/new-preview-modals/new-preview-modal-mobile-numbers/new-preview-modal-mobile-numbers.component";
import { SelectLanguageComponent } from "./components/select-language/select-language.component";
import { PreviewScheduleComponent } from "./modal/new-preview-modals/preview-schedule/preview-schedule.component";
import { PreviewModalDateTimeComponent } from "./modal/new-preview-modals/preview-modal-date-time/preview-modal-date-time.component";
import { PreviewTemplateMessageComponent } from "./modal/new-preview-modals/preview-template-message/preview-template-message.component";
import { ShowFileContentComponent } from "./components/show-file-content/show-file-content.component";
import { NewFilePreviewTableComponent } from "./components/new-file-preview-table/new-file-preview-table.component";
import { SingleFileUploadComponent } from './single-file-upload/single-file-upload.component';
import { UploadService } from "./service/upload.service";
import { CreatePopupComponent } from "./components/create-popup/create-popup.component";

import { EntityIdComponent } from "./entity-id/entity-id.component";
import { CommonDeleteButtonComponent } from './common-delete-button/common-delete-button.component';
import { ExistingTemplatesSliderComponent } from "./existing-templates-slider/existing-templates-slider.component";
import { TemplateFilterPipe } from "./existing-templates-slider/template.fuzzyFilter.pipe";
import { MultiScheduleComponent } from './multi-schedule/multi-schedule.component';
import { MultiDateTimeComponent } from './multi-date-time/multi-date-time.component';
import { DltFilterPipe } from "src/app/templates/Helpers/dltTemplatepipe";

import { FilterGroupPipe } from "./sliders/id-name-slider/group.filter.pipe";
import { GroupsSliderComponent } from './sliders/groups-slider/groups-slider.component';
import { FilterCampGroupPipe } from "./sliders/groups-slider/campaign.group.filter";
import { TemplateGroupSliderComponent } from './sliders/template-group-slider/template-group-slider.component';
import { SkeletonLoaderComponent } from './components/skeleton-loader/skeleton-loader.component';
import { FilePaginationComponent } from './file-pagination/file-pagination.component';
import { ListSkeletonComponent } from './skeletons/list-skeleton/list-skeleton.component';
import { DeleteResPopupComponent } from './components/delete-res-popup/delete-res-popup.component';
import { DeleteConfirmationPopupComponent } from './components/delete-confirmation-popup/delete-confirmation-popup.component';
import { TryAgainServerErrorComponent } from './components/try-again-server-error/try-again-server-error.component';
import { MyacctWalletUserSliderComponent } from './sliders/myacct-wallet-user-slider/myacct-wallet-user-slider.component';
import { WalletUsersPipe } from "./sliders/myacct-wallet-user-slider/wallet-users.pipe";
import { MyAcctUserInfoComponent } from "./components/my-acct-user-info/my-acct-user-info.component";
import { NodataforRangeComponent } from './skeletons/nodatafor-range/nodatafor-range.component';
import { ApierrorComponent } from './skeletons/apierror/apierror.component';
import { LoadingIconButtonComponent } from './components/loading-icon-button/loading-icon-button.component';
import { CampaignProgressSkeletonComponent } from "./skeletons/campaign-progress-skeleton/campaign-progress-skeleton.component";
import { CampaignFileProgressSkeletonComponent } from './skeletons/campaign-file-progress-skeleton/campaign-file-progress-skeleton.component';
import { ScheduledCampaignDetailSkeletonComponent } from './skeletons/scheduled-campaign-detail-skeleton/scheduled-campaign-detail-skeleton.component';
import { DeletemodalComponent } from './deletemodal/deletemodal.component';
import { NodataerrorComponent } from './skeletons/nodataerror/nodataerror.component';
import { NodatacreatenewComponent } from './skeletons/nodatacreatenew/nodatacreatenew.component';
import { ExcelIconComponent } from './single-file-upload/excel-icon/excel-icon.component';
import { UniquenamedropdownapierrorComponent } from './skeletons/uniquenamedropdownapierror/uniquenamedropdownapierror.component';
import { CommondeleteerrorpopupComponent } from './commondeleteerrorpopup/commondeleteerrorpopup.component';
import { EditSkeletonComponent } from './skeletons/edit-skeleton/edit-skeleton.component';
import { RouterModule, Routes } from "@angular/router";
import { SorterComponent } from './components/sorter/sorter.component';
import { SorterPipe } from './campaigns-pipes/sorter.pipe';
import { TooltipModule } from 'ng2-tooltip-directive';
import { DropdownfieldapierrorComponent } from './skeletons/dropdownfieldapierror/dropdownfieldapierror.component';


@NgModule({
    declarations: [
        SingleSelectDropdownComponent,
        FileUploaderComponent,
        CancelModalComponent,
        ClearModalComponent,
        PreviewScheduleComponent,
        PreviewModalDateTimeComponent,
        DuplicateModalComponent,
        DateComponent,
        TimeComponent,
        CampaignCancelButtonComponent,
        CampaignSaveButtonComponent,
        CampaignScheduleButtonComponent,
        CampaignSendButtonComponent,
        UploadedFilesComponent,
        ToastComponent,
        FilterPipe,
        PreviewAddedGroupsComponent,
        PreviewExcludedGroupsComponent,
        SelectMobileColumnComponent,
        FileBasedOnComponent,
        SelectDltComponent,
        ShowFileContentComponent,
        NewFilePreviewTableComponent,
        TemplateNameComponent,
        NewPreviewModalsComponent,
        NewPreviewModalMessageComponent,
        NewPreviewModalMobileNumbersComponent,
        SelectLanguageComponent,
        CreatePopupComponent,
        
        PreviewTemplateMessageComponent,
        SingleFileUploadComponent,
        EntityIdComponent,
        CommonDeleteButtonComponent,
        ExistingTemplatesSliderComponent,
        MyacctWalletUserSliderComponent,
        TemplateFilterPipe,
        MultiScheduleComponent,
        MultiDateTimeComponent,
        DltFilterPipe,
        FilterGroupPipe,
        GroupsSliderComponent,
        FilterCampGroupPipe,
        TemplateGroupSliderComponent,
        SkeletonLoaderComponent,
        FilePaginationComponent,
        ListSkeletonComponent,
        DeleteResPopupComponent,
        DeleteConfirmationPopupComponent,
        TryAgainServerErrorComponent,
        MyacctWalletUserSliderComponent,
        WalletUsersPipe,
        MyAcctUserInfoComponent,
        NodataforRangeComponent,
        ApierrorComponent,
        LoadingIconButtonComponent,
        CampaignProgressSkeletonComponent,
        CampaignFileProgressSkeletonComponent,
        ScheduledCampaignDetailSkeletonComponent,
        DeletemodalComponent,
        NodataerrorComponent,
        NodatacreatenewComponent,
        ExcelIconComponent,
        UniquenamedropdownapierrorComponent,
        CommondeleteerrorpopupComponent,
        EditSkeletonComponent,
        SorterComponent,
        SorterPipe,
        DropdownfieldapierrorComponent

    ],
    imports: [
        CommonModule,
        FormsModule,
        NgSelectModule,
        DropzoneModule,
        DatePickerModule,
        TimePickerModule,
        ReactiveFormsModule,
        NgxPaginationModule,
        DateRangePickerModule,
        RouterModule,
        TooltipModule


    ],
    exports: [
        FormsModule,
        ReactiveFormsModule,
        SingleSelectDropdownComponent,
        FileUploaderComponent,
        CampaignCancelButtonComponent,
        CampaignSaveButtonComponent,
        CampaignScheduleButtonComponent,
        CampaignSendButtonComponent,
        CancelModalComponent,
        ClearModalComponent,
        DateComponent,
        TimeComponent,
        ToastComponent,
        FilterPipe,
        SelectMobileColumnComponent,
        FileBasedOnComponent,
        SelectDltComponent,
        TemplateNameComponent,
        ShowFileContentComponent,
        NewFilePreviewTableComponent,
        SelectLanguageComponent,
        CreatePopupComponent,
        
        FilePaginationComponent,
        SingleFileUploadComponent,
        EntityIdComponent,
        CommonDeleteButtonComponent,
        ExistingTemplatesSliderComponent,
        TemplateFilterPipe,
        MyacctWalletUserSliderComponent,
        GroupsSliderComponent,
        TemplateGroupSliderComponent,
        ListSkeletonComponent,
        DeleteResPopupComponent,
        DeleteConfirmationPopupComponent,
        DatePickerModule,
        TimePickerModule,
        PreviewModalDateTimeComponent,
        SkeletonLoaderComponent,
        MyAcctUserInfoComponent,
        NodataforRangeComponent,
        ApierrorComponent,
        LoadingIconButtonComponent,
        CampaignProgressSkeletonComponent,
        CampaignFileProgressSkeletonComponent,
        ScheduledCampaignDetailSkeletonComponent,
        DeletemodalComponent,
        NodataerrorComponent,
        NodatacreatenewComponent,
        UniquenamedropdownapierrorComponent,
        CommondeleteerrorpopupComponent,
        EditSkeletonComponent,
        DropdownfieldapierrorComponent,
        RouterModule,
        SorterComponent,
        SorterPipe


    ],
    providers: [UploadService]
})
export class SharedModule { }
