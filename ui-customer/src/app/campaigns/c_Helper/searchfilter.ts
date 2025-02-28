
import { Pipe, PipeTransform } from "@angular/core";
import { C_List } from "./c_list.model";
import { SearchService } from "./searchservice";
@Pipe({
    name: "Filter",
    pure: false
})
export class FilterPipe implements PipeTransform {
    constructor(public search: SearchService) { }

    public count: any;

    transform(items: C_List[], searchText: string): C_List[] {
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
                it.c_name.toLocaleLowerCase().includes(searchText) ||
                it.msg.toLocaleLowerCase().includes(searchText) ||
                it.c_type.toLocaleLowerCase().includes(searchText) ||
                it.status.toLocaleLowerCase().includes(searchText) ||
                it.created_ts.toLocaleLowerCase().includes(searchText)
                // it.created_ts_unix.toLocaleLowerCase().includes(searchText) ||
                // it.t_lang.toLocaleLowerCase().includes(searchText)
            );
        });
        this.count = arr.length;
        this.search.searchData(arr);

        return arr;
    }
}