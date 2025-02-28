import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';


import { BillingDetailRoutingModule } from './billing-detail-routing.module';
import { BillingListComponent } from './billing-list/billing-list.component';
import { BillingEditComponent } from './billing-edit/billing-edit.component';
import { SharedModule } from "../shared/shared.module";
import { SMSRateComponent } from './components/sms-rate/sms-rate.component';
import { ErrorDisplayComponent } from './components/error-display/error-display.component';
import { BillingSearchPipe } from './pipe/billing-search.pipe';
import { RateChangeSearchPipe } from "./Billing_helper/ratechange_pipe";
import { DltRateComponent } from './components/dlt-rate/dlt-rate.component';
import { NgSelectModule } from '@ng-select/ng-select';
import { NgxPaginationModule } from "ngx-pagination";
import { ResponsePopupComponent } from './components/response-popup/response-popup.component';
import { TooltipModule } from 'ng2-tooltip-directive';
import { RatechangeReportComponent } from './ratechange-report/ratechange-report.component';
import { RcreportCalendarComponent } from './rcreport-calendar/rcreport-calendar.component';
import { DateRangePickerModule } from "@syncfusion/ej2-angular-calendars";


@NgModule({
  declarations: [
    BillingListComponent,
    BillingEditComponent,
    SMSRateComponent,
    ErrorDisplayComponent,
    BillingSearchPipe,
    RateChangeSearchPipe,
    DltRateComponent,
    ResponsePopupComponent,
    RatechangeReportComponent,
    RcreportCalendarComponent
  ],
  imports: [
    CommonModule,
    NgxPaginationModule,
    DateRangePickerModule,
    SharedModule,
    BillingDetailRoutingModule,
    FormsModule,
    ReactiveFormsModule,
    NgSelectModule,
    TooltipModule
  ]
})
export class BillingDetailModule { }
