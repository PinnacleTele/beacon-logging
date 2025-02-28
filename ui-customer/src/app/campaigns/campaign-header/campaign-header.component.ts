import { Component, Input } from "@angular/core";
import { Router } from "@angular/router";
import { EnterExitTop } from "src/app/shared/animation";
@Component({
    selector: "app-campaign-header",
    templateUrl: "./campaign-header.component.html",
    styleUrls: ["./campaign-header.component.css"]
})
export class CampaignHeaderComponent {
    constructor(private router:Router){}
    @Input() pageTitle: string;

    showDropDown = false;

    close() {
    }
    navigator(path:any){
        
        this.router.navigate(path)
    }

    backEnter(event:any){
        this.router.navigate(['/campaigns'])
    }

}

