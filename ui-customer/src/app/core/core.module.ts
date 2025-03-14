import { NgModule, Optional, SkipSelf } from "@angular/core";
import { CommonModule } from "@angular/common";
import { HeaderComponent } from "./header/header.component";
import { SidebarComponent } from "./sidebar/sidebar.component";
import { CoreRoutingModule } from "./core-routing.module";
import { SharedModule } from "../shared/shared.module";
import { NgApexchartsModule } from "ng-apexcharts";
import { LoaderIconComponent } from './header/loader-icon/loader-icon.component';
import { TooltipModule } from 'ng2-tooltip-directive';


@NgModule({
    declarations: [SidebarComponent, HeaderComponent,  LoaderIconComponent],
    imports: [CommonModule, CoreRoutingModule, SharedModule,NgApexchartsModule, TooltipModule],
    exports: [SidebarComponent, HeaderComponent]
})
export class CoreModule {
    constructor(@Optional() @SkipSelf() parentModule: CoreModule) {
        if (parentModule) {
            throw new Error(
                "CoreModule is already loaded. Import it in the AppModule only"
            );
        }
    }
}
