import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AccountHomeComponent } from './account-home/account-home.component';
import { CreateNewAccountComponent } from './create-new-account/create-new-account.component';
import { AccountListComponent } from "./account-list/account-list.component";
import { EditAccountComponent } from './edit-account/edit-account.component';
import { AuthGuard } from '../authentication/auth-guard';
import { MyAccountComponent } from './my-account/my-account.component';


const routes: Routes = [
  { path: "", canActivate: [AuthGuard],component: AccountListComponent },
  { path: "", 
    canActivate: [AuthGuard],
    canActivateChild : [AuthGuard],
  children: [
    { path: "new",component: CreateNewAccountComponent , },
  // { path: "new",component: NewAccountComponent , },
  
    { path: "edit",component: EditAccountComponent ,},
  //  { path: "myacct",component: MyAccountComponent ,},
  ]
  },
  
 
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
 
})
export class AccountRoutingModule { 
  }
