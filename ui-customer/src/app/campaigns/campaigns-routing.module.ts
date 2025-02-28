import { NgModule } from "@angular/core";
import { RouterModule, Routes } from "@angular/router";
import { CampaignTemplateComponent } from "./campaign-template/campaign-template.component";
import { GroupsComponent } from "./groups/groups.component";
import { ManyToManyComponent } from "./manytomany/manytomany.component";
import { OneToManyComponent } from "./onetomany/onetomany.component";
import { QuickSmsComponent } from "./quick-sms/quick-sms.component";
import { CampaignDetailsComponent} from "./campaign-details/campaign-details.component"
import { CampaignListComponent } from "./campaign-list/campaign-list.component";
import { CampaigncalenderComponent } from "./campaigncalender/campaigncalender.component";
import { CampaignScheduledDetailComponent } from "./campaign-scheduled-detail/campaign-scheduled-detail.component";
import { CScheduleListComponent } from "./c-schedule-list/c-schedule-list.component";
import { AuthGuard } from "../authentication/auth-guard";
import { TrafficTypeComponent } from "./traffic-type/traffic_type.component";
const routes: Routes = [
  
    { path: "", component: CampaignListComponent },
   
    {
        path: "",
        canActivateChild : [AuthGuard],
        children: [
       
            
            { path: "scheduledlist", component: CScheduleListComponent },
            {
                path: "traffic",
                component: TrafficTypeComponent
            },
            {
                path: "otm",
                component: OneToManyComponent
            },
            {  path: "cq", component: QuickSmsComponent },
            {
                path: "mtm",
                component: ManyToManyComponent
            },
            {
                path: "cg",
                component: GroupsComponent
            },
            {
                path: "ct",
                component: CampaignTemplateComponent
            },
            {
                path: "calender",
                component: CampaigncalenderComponent
            },
            {
                path:"cdetail",
                component:CampaignDetailsComponent
            },
            {
                path:"scdetail",
                component:CampaignScheduledDetailComponent
            }
        ]
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class CampaignsRoutingModule {}
