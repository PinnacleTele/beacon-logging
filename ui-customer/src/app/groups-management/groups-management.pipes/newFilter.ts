import { Pipe, PipeTransform } from "@angular/core";
import { GroupModel } from "../groupsMangement.group.model";
import { SearchService } from "../groups.search.service";
@Pipe({
    name: "Filter",
    pure: false
})
export class FilterPipe implements PipeTransform {
    constructor(public search: SearchService) { }

    public count: any;

    transform(items: GroupModel[], searchText: string): GroupModel[] {
        searchText = searchText.trim();

        if (!items) {
            return [];
        }
        if (!searchText) {
            return items;
        }
        searchText = searchText.toLocaleLowerCase();
        const arr = items.filter((it) => {
            return (
                it.g_name.toLocaleLowerCase().includes(searchText) ||
                it.g_type.toLocaleLowerCase().includes(searchText) ||
                it.g_visibility.toLocaleLowerCase().includes(searchText) ||
                it.created_ts.toLocaleLowerCase().includes(searchText) ||
                it.status.toLocaleLowerCase().includes(searchText) ||
                it.total_human.toLocaleLowerCase().includes(searchText)
                // it.created_ts_unix.toLocaleLowerCase().includes(searchText) ||
                // it.t_lang.toLocaleLowerCase().includes(searchText)
            );
        });
        this.count = arr.length;
        //  console.log(arr);

        this.search.searchData(arr);
        this.search.searchcount(arr.length);
        return arr;
    }
}
