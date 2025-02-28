import { Pipe, PipeTransform } from "@angular/core";
import { TemplateCampaignService } from "src/app/campaigns/campaign-template/service/template-campaign.service";
import { DLTs } from "src/app/shared/model/DLTs.model";
import { SearchService } from "../search.service";
@Pipe({
    name: "dltFilter",
    pure: false
})
export class DltFilterPipe implements PipeTransform {
    constructor(public search: SearchService, private tempService: TemplateCampaignService) { }

    public count: any;

    transform(items: DLTs[], searchText: string): DLTs[] {
        searchText = searchText.trim();
        this.tempService.noMatch.next(false);
        if (!items) {
            return [];
        }
        if (!searchText) {
            return items;
        }
        if (searchText) { }
        searchText = searchText.toLocaleLowerCase();
        const arr = items.filter((it) => {
            it.dlt_entity_id += ""
            it.dlt_template_id += ""
            return (
                it.dlt_entity_id.toLocaleLowerCase().includes(searchText) ||
                it.dlt_template_id.toLocaleLowerCase().includes(searchText) ||
                it.dlt_template_name.toLocaleLowerCase().includes(searchText) ||
                it.dlt_template_content.toLocaleLowerCase().includes(searchText)
            );
        });
        if (arr.length == 0) {
            this.tempService.noMatch.next(true);
        }
        return arr;
    }
}
