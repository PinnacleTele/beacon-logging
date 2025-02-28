import { Pipe, PipeTransform } from "@angular/core";
import { TemplateModel } from "./model/templatemodal";
import { SearchService } from "./search.service";
@Pipe({
    name: "Filter",
    pure: false
})
export class FilterPipe implements PipeTransform {
    constructor(public search: SearchService) { }

    public count: any;

    transform(items: TemplateModel[], searchText: string): TemplateModel[] {
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
                it.t_name.toLocaleLowerCase().includes(searchText) ||
                it.t_type.toLocaleLowerCase().includes(searchText) ||
                it.t_mobile_column.toLocaleLowerCase().includes(searchText) ||
                it.t_lang_type.toLocaleLowerCase().includes(searchText) ||
                it.t_content.toLocaleLowerCase().includes(searchText) ||
                it.created_ts.toLocaleLowerCase().includes(searchText)
                // it.created_ts_unix.toLocaleLowerCase().includes(searchText) ||
                // it.t_lang.toLocaleLowerCase().includes(searchText)
            );
        });
        this.count = arr.length;

        this.search.searchData(arr);
        this.search.searchcount(arr.length);
        return arr;
    }
}
