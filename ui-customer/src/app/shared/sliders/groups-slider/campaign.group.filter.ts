import { Pipe, PipeTransform } from "@angular/core";
import { GroupModel } from "src/app/campaigns/model/campaign-group-model";
import { SharedGroupService } from "../../service/shared-group.service";

@Pipe({
    name: "filterCampGroup"
})

export class FilterCampGroupPipe implements PipeTransform {

    constructor(private groupService: SharedGroupService) {
        this.groupService.noMatchFound.next(false)
    }
    transform(items: GroupModel[], searchelement: string): GroupModel[] {
        this.groupService.noMatchFound.next(false)
        searchelement = searchelement.trim();
        if (!items) {
            this.groupService.filteredGroups=[];
            return [];
        }
        if (!searchelement) {
            this.groupService.filteredGroups=items;
            return items;
        }
        searchelement = searchelement.toLocaleLowerCase();
        const arr = items.filter((it) => {
            return it.g_name.toLocaleLowerCase().includes(searchelement);

        });
        // console.log(arr.length);
        if (arr.length == 0) {
            this.groupService.noMatchFound.next(true);
        }
        this.groupService.filteredGroups=arr;
        return arr;
        // console.log("filter");
    }
}
