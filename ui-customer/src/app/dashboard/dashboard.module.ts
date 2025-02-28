import { NgModule } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { NgxPaginationModule } from "ngx-pagination";
import { SharedModule } from "../shared/shared.module";
import { NewdashboardComponent } from "src/app/dashboard/newdashboard/newdashboard.component";
import { DashboardRoutingModule } from "src/app/dashboard/dashboard.routing.module";
import { NgApexchartsModule } from "ng-apexcharts";
import { DloadingskeletonComponent } from "src/app/dashboard/dloadingskeleton/dloadingskeleton.component";
@NgModule({
    declarations: [
        NewdashboardComponent,
        DloadingskeletonComponent
    ],
    imports: [
        CommonModule,
        FormsModule,
        DashboardRoutingModule,
        ReactiveFormsModule,
        NgxPaginationModule,
        NgApexchartsModule,
        SharedModule,
    ],
    providers: []
})
export class DashboardModule {}
