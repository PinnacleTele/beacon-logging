import { Pipe, PipeTransform } from "@angular/core";

import { Templates } from "../../shared/model/templates.model";

@Pipe({ name: "templateFilter" })
export class TemplateFilterPipe implements PipeTransform {
    transform(items: Templates[], searchelement: string): Templates[] {
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

        return arr;
    }
}
