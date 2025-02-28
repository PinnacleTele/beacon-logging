import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { BillingEditComponent } from './billing-edit/billing-edit.component';
import { BillingListComponent } from './billing-list/billing-list.component';
import { RatechangeReportComponent } from "src/app/billing-detail/ratechange-report/ratechange-report.component";
import { AuthGuard } from '../authentication/auth-guard';
const routes: Routes = [
  {
    path: "",
    component: BillingListComponent,
    canActivate: [AuthGuard],
    canActivateChild: [AuthGuard],
  },
  {
    path: "edit",
    component: BillingEditComponent,
    canActivate: [AuthGuard],
    canActivateChild: [AuthGuard],
  },
  {
    path: "brclist",
    component: RatechangeReportComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class BillingDetailRoutingModule { }
