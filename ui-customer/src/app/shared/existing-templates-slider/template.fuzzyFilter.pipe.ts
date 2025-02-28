import { Pipe, PipeTransform } from "@angular/core";
import { TemplateCampaignService } from "src/app/campaigns/campaign-template/service/template-campaign.service";

import { Templates } from "../../shared/model/templates.model";

@Pipe({ name: "templateFilter" })
export class TemplateFilterPipe implements PipeTransform {
    constructor(private tempService: TemplateCampaignService) { }
    transform(items: Templates[], searchelement: string): Templates[] {
        searchelement = searchelement.trim();
        this.tempService.noMatch.next(false);
        if (!items) {
            return [];
        }
        if (!searchelement) {
            return items;
        }
        searchelement = searchelement.toLocaleLowerCase();
        const arr = items.filter((it) => {
            return (
                it.t_name.toLocaleLowerCase().includes(searchelement) ||
                it.t_content.toLocaleLowerCase().includes(searchelement) ||
                it.t_mobile_column.toLocaleLowerCase().includes(searchelement) ||
                it.t_lang.toLocaleLowerCase().includes(searchelement) ||
                it.t_lang_type.toLocaleLowerCase().includes(searchelement) ||
                it.t_type.toLocaleLowerCase().includes(searchelement) ||
                it.dlt_template_id.toLocaleLowerCase().includes(searchelement) ||
                it.dlt_entity_id.toLocaleLowerCase().includes(searchelement)
            );
        });
        if (arr.length == 0) {
            this.tempService.noMatch.next(true);
        }
        return arr;
    }
}
