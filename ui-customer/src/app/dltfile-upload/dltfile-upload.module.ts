
import { NgModule } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { SharedModule } from "../shared/shared.module";
import { NgSelectModule } from "@ng-select/ng-select";
import { FilterPipe } from "src/app/dltfile-upload/DltHleper/filter";
import { DTFilterPipe } from "src/app/dltfile-upload/DltHleper/dlttemplate.filter";
import { DropzoneModule } from "ngx-dropzone-wrapper";

import { Ng2SearchPipeModule } from 'ng2-search-filter';
import { DateRangePickerModule } from '@syncfusion/ej2-angular-calendars';
import { NgxPaginationModule } from "ngx-pagination";
import { dltFIleUploadRoutingModule } from "src/app/dltfile-upload/dltfile-upload.routing.module";
import { UploadfileComponent } from "src/app/dltfile-upload/uploadfile/uploadfile.component";
import { SaveButtonComponent } from "src/app/dltfile-upload/save-button/save-button.component";
import { DltoverviewComponent } from "src/app/dltfile-upload/dltoverview/dltoverview.component";
import { DltentityidComponent } from "./dltentityid/dltentityid.component";
import { TooltipModule } from 'ng2-tooltip-directive';
import { DltuploadlistComponent } from "src/app/dltfile-upload/dltuploadlist/dltuploadlist.component";
import { HeadertabComponent } from "src/app/dltfile-upload/headertab/headertab.component";
import { SenderIdPipePipe } from "./DltHleper/sender-id-pipe.pipe";
import { EntityIdFilterPipe } from "./DltHleper/entity-id-filter.pipe";
@NgModule({
    declarations: [

        UploadfileComponent,
        SaveButtonComponent,
        DltoverviewComponent,
        DltuploadlistComponent,
        HeadertabComponent,
        FilterPipe,
        DltentityidComponent,
        DTFilterPipe,
        SenderIdPipePipe,
        EntityIdFilterPipe
    ],
    imports: [
        CommonModule,
        SharedModule,
        FormsModule,
        dltFIleUploadRoutingModule,
        ReactiveFormsModule,
        NgSelectModule,
        DropzoneModule,
        Ng2SearchPipeModule,
        DateRangePickerModule,
        NgxPaginationModule,
        TooltipModule
    ],

    exports: [],

    providers: [

    ]
})
export class DLTFileUploadModule { }
