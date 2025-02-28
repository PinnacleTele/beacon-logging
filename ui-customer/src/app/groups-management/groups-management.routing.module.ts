import { NgModule } from "@angular/core";
import { RouterModule, Routes } from "@angular/router";
import { AuthGuard } from "../authentication/auth-guard";
import { GroupContactEditComponent } from "./group-contact-edit/group-contact-edit.component";
import { GroupContactListComponent } from "./group-contact-list/group-contact-list.component";
import { GroupContactsAddComponent } from "./group-contacts-add/group-contacts-add.component";
import { GroupEditComponent } from "./group-edit/group-edit.component";
import { GroupListComponent } from "./group-list/group-list.component";

import { GroupsManagementComponent } from "./groups-management.component";
import { NewGroupComponent } from "./new-group/new-group.component";

const routes: Routes = [
    {
        path: "",
        component: GroupListComponent
    },
    {
        path: "",
        component: GroupsManagementComponent,
        canActivateChild : [AuthGuard],
        children: [
            {
                path: "new",
                component: NewGroupComponent
            },
          
            {
                path:"groupcontacts",
                component:GroupContactListComponent

            },
            {
                path:"editcontact",
                component:GroupContactEditComponent
            },
            {
                path:"addcontacts",
                component:GroupContactsAddComponent
            },{
                path:"editgroup",
                component:GroupEditComponent
                
            }

        ]
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class GroupsManagementRoutingModule {}
