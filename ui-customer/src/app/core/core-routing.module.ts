import { NgModule } from "@angular/core";
import { RouterModule, Routes } from "@angular/router";

import { AuthGuard } from "../authentication/auth-guard";

const routes: Routes = [

];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
    , providers: [AuthGuard]
})
export class CoreRoutingModule { }
