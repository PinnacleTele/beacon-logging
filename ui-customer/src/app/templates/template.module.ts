import { NgModule } from "@angular/core";
import { CommonModule } from "@angular/common";

import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { NgxPaginationModule } from "ngx-pagination";
import { TemplateRoutingModule } from "./template.routing.module";
import { TemplatesComponent } from "./templates.component";
import { SharedModule } from "../shared/shared.module";
import { CampaignsModule } from "../campaigns/campaigns.module";
import { TemplateListComponent } from "./template-list/template-list.component";
import { FilterPipe } from "./template.pipe";
import { SearchService } from "./search.service";
import { CreateNewTemplateComponent } from "./create-new-template/create-new-template.component";
import { CreateNewTemplateMessageComponent } from "./create-new-template/create-new-template-message/create-new-template-message.component";
import { TemplateCreateButtonComponent } from "./template-create-button/template-create-button.component";
import { EditTemplateDetailComponent } from "./edit-template-detail/edit-template-detail.component";
import { SelectDltTemplateComponent } from "./select-dlt-template/select-dlt-template.component";
// import { CampaignsModule } from "src/app/campaigns/campaigns.module"
import { TemplatedeletepopupComponent } from "src/app/templates/templatedeletepopup/templatedeletepopup.component";
import { TooltipModule } from 'ng2-tooltip-directive';

@NgModule({
    declarations: [
        TemplatesComponent,
        TemplateListComponent,
        FilterPipe,
        CreateNewTemplateComponent,
        CreateNewTemplateMessageComponent,
        TemplateCreateButtonComponent,
        EditTemplateDetailComponent,
        SelectDltTemplateComponent,
        TemplatedeletepopupComponent,

    ],
    imports: [
        CommonModule,
        FormsModule,
        TemplateRoutingModule,
        ReactiveFormsModule,
        NgxPaginationModule,
        SharedModule,
        TooltipModule,
       // CampaignsModule,
    ],
    exports: [
        TemplatedeletepopupComponent
    ],
    providers: [SearchService]
})
export class TemplateModule { }
