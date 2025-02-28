import { NgModule } from "@angular/core";
import { RouterModule, Routes } from "@angular/router";
import { TemplatesComponent } from "./templates.component";
import { TemplateListComponent } from "./template-list/template-list.component";
import { CreateNewTemplateComponent } from "./create-new-template/create-new-template.component";
import { EditTemplateDetailComponent } from "./edit-template-detail/edit-template-detail.component";
import { AuthGuard } from "../authentication/auth-guard";
const routes: Routes = [
    {
        path: "",
        component: TemplateListComponent
    },
    {
        path: "",
        component: TemplatesComponent,
        canActivateChild : [AuthGuard],
        children: [
            
            {
                path: "new",
                component: CreateNewTemplateComponent
            },
            {
                path: "edit",
                component: EditTemplateDetailComponent
            },
            {
                path: "preview",
                component: EditTemplateDetailComponent
            },
           

            
            // {
            //     path: "preview/:id",
            //     component: EditTemplateDetailComponent
            // }
        ]
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class TemplateRoutingModule {}
