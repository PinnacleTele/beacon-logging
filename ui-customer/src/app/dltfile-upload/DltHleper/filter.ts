import { Pipe, PipeTransform } from "@angular/core";
import { DLTRECORDS } from "../modals/modal";
import { DltUploadService } from "../dltupload.service";
@Pipe({
    name: "Filter",
    pure: false
})
export class FilterPipe implements PipeTransform {
    constructor(public search: DltUploadService) { }

    public count: any;

    transform(items: DLTRECORDS[], searchText: string): DLTRECORDS[] {
        //  searchText = searchText.trim();
        if (!items) {
            return [];
        }
        if (!searchText) {
            return items;
        }
        searchText = searchText?.toLocaleLowerCase();
        const arr = items.filter((it) => {
            it.total = it.total + ""


            return (
                it.dlt_entity_id?.toLocaleLowerCase().includes(searchText) ||
                it.status?.toLocaleLowerCase().includes(searchText) ||
                it.created_ts?.toLocaleLowerCase().includes(searchText) ||
                it.telco?.toLocaleLowerCase().includes(searchText) ||
                it.total.toLocaleLowerCase().includes(searchText)

            );
        });

        this.count = arr.length;
        this.search.searchData(arr);

        return arr;
    }
}