import { Component, Input } from "@angular/core";
import { Group, GroupModel } from "src/app/campaigns/model/campaign-group-model";
import { CONSTANTS} from 'src/app/shared/campaigns.constants';

@Component({
    selector: "app-preview-excluded-groups",
    templateUrl: "./preview-excluded-groups.component.html",
    styleUrls: ["./preview-excluded-groups.component.css"]
})
export class PreviewExcludedGroupsComponent {
    @Input() excludedGroups: GroupModel[];

   

    get noOfContacts() {
        return this.excludedGroups.reduce((a, b) => +a + +b.total, 0);
    }
}
