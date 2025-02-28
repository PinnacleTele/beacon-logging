import { Pipe, PipeTransform } from "@angular/core";
import { TemplateGroup } from "src/app/account/shared/model/template-group-model";
import { TemplateCampaignService } from "src/app/campaigns/campaign-template/service/template-campaign.service";
import { TGroupSliderService } from "../../service/template-group-slider.service";

@Pipe({
    name: "filterGroup"
})
export class FilterGroupPipe implements PipeTransform {
    constructor(private tempService: TemplateCampaignService, private TGsliderService:TGroupSliderService) { }
    transform(items: TemplateGroup[], searchelement: string): TemplateGroup[] {
        searchelement = searchelement.trim()
        this.tempService.noMatch.next(false);
        if (!items) {
            this.TGsliderService.filterdGroups=[];
            return [];
        }
        if (!searchelement) {
            this.TGsliderService.filterdGroups=items;
            return items;
        }
        searchelement = searchelement.toLocaleLowerCase();
        const arr = items.filter((it) => {
            return it.template_group_name.toLocaleLowerCase().includes(searchelement);

        });

        if (arr.length == 0) {
            this.tempService.noMatch.next(true)
        }
        this.TGsliderService.filterdGroups=arr
        return arr;
        // console.log("filter");
    }
}
