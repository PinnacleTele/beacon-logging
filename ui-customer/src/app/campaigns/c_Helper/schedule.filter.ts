import { Pipe, PipeTransform } from "@angular/core";
import { C_S_List } from "./c_s_listModal";
import { SearchService } from "./searchservice";
@Pipe({
    name: "campaignScheduleListFilter",
    pure: false
})
export class campaignScheduleListFilter implements PipeTransform {
    constructor(private s_service: SearchService) { }

    public count: any;

    transform(items: C_S_List[], searchText: string): C_S_List[] {
        if (!items) {
            return [];
        }
        if (!searchText) {
            return items;
        }
        searchText = searchText?.toLocaleLowerCase();
        const arr = items.filter((it) => {
            return (
                it.c_name?.toLocaleLowerCase().includes(searchText) ||
                it.msg?.toLocaleLowerCase().includes(searchText)    ||
                it.c_type?.toLocaleLowerCase().includes(searchText) ||
                it.status?.toLocaleLowerCase().includes(searchText) ||
                it.scheduled_ts?.toLowerCase().includes(searchText) || 
                it.selected_zone?.toLowerCase().includes(searchText)||
                it.total_human.includes(searchText)

            );
        });
        this.s_service.searchScheduleData(arr);
        return arr;
    }
}
