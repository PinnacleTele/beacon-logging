import { NgModule } from "@angular/core";
import { RouterModule, Routes } from "@angular/router";
import { DownloadsComponent } from "./downloads.component";
import { LogDownloadComponent } from "./log-download/log-download.component";


const routes: Routes = [
    {
        path: "",
        component: DownloadsComponent,
        children: [
            {path:"log",
            component:LogDownloadComponent
        }

        ]
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class DownloadsRoutingModule {}