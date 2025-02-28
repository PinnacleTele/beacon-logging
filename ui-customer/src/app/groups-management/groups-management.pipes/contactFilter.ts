import { Pipe, PipeTransform } from "@angular/core";
import { SearchService } from "../groups.search.service";
import { contact } from "../contact.model";

@Pipe({
    name: "contactFilter",
    pure: false
})
export class ContactFilterPipe implements PipeTransform {
    constructor(public searchvalue: SearchService) { }

    public count: any;

    transform(items: contact[], searchText: string): contact[] {
        if (!items) {
            return [];
        }
        if (!searchText) {
            return items;
        }
        searchText = searchText.toLocaleLowerCase();
        const arr = items.filter((it) => {
            return (
                // it.name?.toLocaleLowerCase().includes(searchText) ||
                // it.email?.toLocaleLowerCase().includes(searchText) ||
                it.mobile
                    .toString()
                    .toLocaleLowerCase()
                    .includes(searchText)

            );
        });
        this.count = arr.length;
        this.searchvalue.searchcount(arr.length);
        return arr;
    }
}
