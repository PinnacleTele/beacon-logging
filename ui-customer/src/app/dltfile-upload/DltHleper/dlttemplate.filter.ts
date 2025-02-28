import { Pipe, PipeTransform } from "@angular/core";
import { DLTTEMPLATE } from "../modals/dlttemplate.modal";
import { DltUploadService } from "../dltupload.service";
@Pipe({
    name: "DTFilter",
    pure: false
})
export class DTFilterPipe implements PipeTransform {
    constructor(public search: DltUploadService) { }

    public count: any;

    transform(items: DLTTEMPLATE[], searchText: string): DLTTEMPLATE[] {
        searchText = searchText.trim();
        if (!items) {
            return [];
        }
        if (!searchText) {
            return items;
        }
        searchText = searchText?.toLocaleLowerCase();
        const arr = items.filter((it) => {
            // it.total = it.total + ""
            it.dlt_entity_id += ""
            it.dlt_template_id += ""

            return (
                it.dlt_entity_id?.toLocaleLowerCase().includes(searchText) ||
                it.header?.toLocaleLowerCase().includes(searchText) ||
                it.created_ts?.toLocaleLowerCase().includes(searchText) ||
                it.dlt_template_id?.toLocaleLowerCase().includes(searchText) ||
                it.dlt_template_name.toLocaleLowerCase().includes(searchText) ||
                it.dlt_template_content.toLocaleLowerCase().includes(searchText)

            );
        });

        this.count = arr.length;
        this.search.searchDTData(arr);

        return arr;
    }
}