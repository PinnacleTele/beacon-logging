import { Pipe, PipeTransform } from "@angular/core";
import { AccountModel } from "src/app/account/shared/accountHelper/list.model";
import { AccountsService } from "./accounts.service";
@Pipe({
    name: "userListFilter",
    pure: false
})
export class userListFilter implements PipeTransform {
    constructor(private aservice: AccountsService) { }

    public count: any;

    transform(items: AccountModel[], searchText: string): AccountModel[] {
        searchText = searchText.trim();
        if (!items) {
            return [];
        }
        if (!searchText) {
            return items;
        }
        searchText = searchText.toLocaleLowerCase();
        const arr = items.filter((it) => {
            const custname = it.firstname + ' ' + it.lastname;
            return (
                it.firstname.toLocaleLowerCase().includes(searchText) ||
                it.lastname.toLocaleLowerCase().includes(searchText) ||
                it.email.toLocaleLowerCase().includes(searchText) ||
                it.created_ts.toLocaleLowerCase().includes(searchText) ||
                it.user.toLowerCase().includes(searchText) ||
                it.userStatus.toLowerCase().includes(searchText) ||
                it.userType.toLowerCase().includes(searchText) ||
                custname.toLowerCase().includes(searchText)
            );
        });
        this.aservice.searchData(arr);
        return arr;
    }
}
