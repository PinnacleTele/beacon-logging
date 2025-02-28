import { Component, Input } from "@angular/core";
import { Group, GroupModel } from "src/app/campaigns/model/campaign-group-model";
import { CONSTANTS } from 'src/app/shared/campaigns.constants';

@Component({
    selector: "app-preview-added-groups",
    templateUrl: "./preview-added-groups.component.html",
    styleUrls: ["./preview-added-groups.component.css"]
})
export class PreviewAddedGroupsComponent {
    @Input() addedGroups: GroupModel[];

    @Input() removeDuplicates: boolean;

    @Input() vlShortern: boolean;

   


    get noOfContacts() {
        return this.addedGroups.reduce((a, b) => +a + +b.total, 0);
    }
}
