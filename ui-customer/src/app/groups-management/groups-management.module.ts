import { NgModule } from "@angular/core";
import { CommonModule } from "@angular/common";
import { NgSelectModule } from "@ng-select/ng-select";
// import { FilterPipe } from "./groups-management.pipes/filter.pipe";
import { FilterPipe } from "src/app/groups-management/groups-management.pipes/newFilter";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { RouterModule } from "@angular/router";
import { NgxPaginationModule } from "ngx-pagination";
import { GroupsManagementComponent } from "./groups-management.component";
import { GroupsManagementRoutingModule } from "./groups-management.routing.module";

import { SharedModule } from "../shared/shared.module";
import { NewGroupComponent } from "./new-group/new-group.component";
import { GroupListComponent } from "./group-list/group-list.component";
import { GroupContactListComponent } from "./group-contact-list/group-contact-list.component";
import {  MobileNumberComponent } from "src/app/groups-management/groups-shared/contact-number/mobile-number.component";
import { GroupContactEditComponent } from "./group-contact-edit/group-contact-edit.component";
import { ContactNameComponent } from "./groups-shared/contact-name/contact-name.component";
import { ContactEmailComponent } from "./groups-shared/contact-email/contact-email.component";
import { GroupContactsAddComponent } from "./group-contacts-add/group-contacts-add.component";
import { GroupEditComponent } from "./group-edit/group-edit.component";
import { ContactFilterPipe } from "./groups-management.pipes/contactFilter";
import { GroupCreateButtonComponent } from "./groups-shared/group-create-button/group-create-button.component";
import { TooltipModule } from 'ng2-tooltip-directive';

@NgModule({
    declarations: [
        GroupsManagementComponent,
        NewGroupComponent,
        GroupListComponent,
        FilterPipe,
        GroupContactListComponent,
        GroupContactEditComponent,
        GroupContactsAddComponent,
        MobileNumberComponent,     
        ContactNameComponent,
        ContactEmailComponent,
        GroupEditComponent,
        ContactFilterPipe,
        GroupCreateButtonComponent
        
    ],
    imports: [
        CommonModule,
        FormsModule,
        RouterModule,
        NgSelectModule,
        GroupsManagementRoutingModule,
        ReactiveFormsModule,
        NgxPaginationModule,
        SharedModule,
        TooltipModule,
    ],
    exports:[
        MobileNumberComponent
    ]
})
export class GroupsManagementModule {}
