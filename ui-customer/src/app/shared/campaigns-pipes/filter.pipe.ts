import { Pipe, PipeTransform } from "@angular/core";
import { Group } from "src/app/campaigns/model/campaign-group-model";
@Pipe({
    name: "Filter"
})
export class FilterPipe implements PipeTransform {
    transform(items: Group[], searchelement: string): Group[] {
        if (!items) {
            return [];
        }
        if (!searchelement) {
            return items;
        }
        searchelement = searchelement.toLocaleLowerCase();
        const arr = items.filter((it) => {
            return it.groupname.toLocaleLowerCase().includes(searchelement);

        });
        return arr;
        // console.log("filter");
    }
}
