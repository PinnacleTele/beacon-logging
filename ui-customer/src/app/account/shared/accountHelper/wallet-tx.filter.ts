import { Pipe, PipeTransform } from "@angular/core";
import { MyWalletService } from "../../my-account/my-wallet.service";
import { WalletTrModel } from "./walletTx.model";
 
@Pipe({
    name: "walletTxListFilter",
    pure: false
})
export class walletTxListFilter implements PipeTransform {
    constructor(private myWalletService: MyWalletService) {}

    public count: any;

    transform(items: WalletTrModel[], searchText: string): WalletTrModel[] {
        if (!items) {
            return [];
        }
        if (!searchText) {
            return items;
        }
        searchText = searchText.toLocaleLowerCase();
        const arr = items.filter((it) => {
            return (
                it.username.toLocaleLowerCase().includes(searchText) ||
                it.created_ts.toLocaleLowerCase().includes(searchText) ||
                it.description.toLocaleLowerCase().includes(searchText) ||
                it.action.toLocaleLowerCase().includes(searchText)  ||
                it.amount.toString().includes(searchText) ||
                it.old_bal.toString().includes(searchText)  ||
                it.new_bal.toString().includes(searchText)
              
            );
        });
        this.myWalletService.searchData(arr);
        return arr;
    }
}
