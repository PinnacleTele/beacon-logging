import { NgModule } from "@angular/core";
import { RouterModule, Routes } from "@angular/router";
import { MyAccountComponent } from "./account/my-account/my-account.component";
import { AuthGuard } from "./authentication/auth-guard";
import { ForgotPasswordComponent } from "./authentication/forgot-password/forgot-password.component";
import { LoginComponent } from "./authentication/login/login.component";

import { FrameComponent } from "./layouts/frame/frame.component";
import { LoginLayoutComponent } from "./layouts/login-layout/login-layout.component";
import { PageNotFoundComponent } from "./page-not-found/page-not-found.component";
import { PageNotFound404Component } from "./page-not-found404/page-not-found404.component";
import { UnauthorizedPageComponent } from "./unauthorized-page/unauthorized-page.component";
// import { LoginComponent } from "./authentication/login/login.component";

const routes: Routes = [
  {
    path: '',
    component: FrameComponent,

    children: [
      {
        path: 'dashboard',

        loadChildren: () => import('./dashboard/dashboard.module').then(m => m.DashboardModule)
      },
      {
        path: 'campaigns',
        loadChildren: () => import('./campaigns/campaigns.module').then(m => m.CampaignsModule),
        canActivate: [AuthGuard],
      },
      {
        path: 'groups',
        data: { preload: true },

        loadChildren: () => import('./groups-management/groups-management.module').then(m => m.GroupsManagementModule),
        canActivate: [AuthGuard],
      },
      {
        path: 'accounts',

        loadChildren: () => import('./account/account.module').then(m => m.AccountModule)

      },
      {
        path: 'billing',

        loadChildren: () => import('./billing-detail/billing-detail.module').then(m => m.BillingDetailModule)
      },
      {
        path: 'templates',
        loadChildren: () => import('./templates/template.module').then(m => m.TemplateModule),
        canActivate: [AuthGuard],
      },
      {
        path: 'reports',
        loadChildren: () => import('./reports/reports.module').then(m => m.ReportsModule)
      },
      {
        path: 'downloads',
        loadChildren: () => import('./downloads/downloads.module').then(m => m.DownloadsModule)
      },
      {
        path: 'dlt',
        loadChildren: () => import('./dltfile-upload/dltfile-upload.module').then(m => m.DLTFileUploadModule)
      },
      {
        path: "myacct",
        canActivate: [AuthGuard],
        canActivateChild: [AuthGuard],
        component: MyAccountComponent,
      },



    ]
  },
  {
    path: '',
    component: LoginLayoutComponent,

    children: [
      {
        path: 'login',
        component: LoginComponent
      },
      {
        path: 'forgotPassword',
        component: ForgotPasswordComponent
      },
    ]
  },

  //{ path: '', redirectTo:'login', pathMatch:'full'},

  {
    path: '',
    component: FrameComponent,

    children: [
      {
        path: 'page-401',
        component: UnauthorizedPageComponent
      }
    ]
  },
  {
    path: '',
    component: FrameComponent,

    children: [
      {
        path: '**',
        component: PageNotFound404Component
      }
    ]
  },




];

@NgModule({
  imports: [RouterModule.forRoot(routes, { useHash: true })],
  exports: [RouterModule]
})
export class AppRoutingModule { }
