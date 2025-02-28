import { Component, NgModule } from "@angular/core";
import { RouterModule, Routes } from "@angular/router";
import { AuthGuard } from "../authentication/auth-guard";
import { ReportsDetailedComponent } from "./reports-detailed/reports-detailed.component";
import { ReportsSearchComponent } from "./reports-search/reports-search.component";
import { ReportsSummaryComponent } from "./reports-summary/reports-summary.component";
import { ReportsComponent } from "./reports.component";

const routes: Routes = [
    {
        path: "",
        component: ReportsComponent,
        canActivateChild : [AuthGuard],
        children: [
            {   
            path:"summary",
            component: ReportsSummaryComponent
        },
        {   
            path:"detailed",
            component: ReportsDetailedComponent
        },
        {   
            path:"search",
            component: ReportsSearchComponent
        }

        ]
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class ReportRoutingModule {}
