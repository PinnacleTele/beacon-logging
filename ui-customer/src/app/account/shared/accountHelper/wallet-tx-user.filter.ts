import { Pipe, PipeTransform } from "@angular/core";
import { MyWalletService } from "../../my-account/my-wallet.service";
import { WalletTrUserModel } from "./walletTx.usermodel";
 
@Pipe({
    name: "walletTxUserListFilter",
    pure: false
})
export class walletTxUserListFilter implements PipeTransform {
    constructor(private myWalletService: MyWalletService) {}

    public count: any;

    transform(items: WalletTrUserModel[], searchText: string): WalletTrUserModel[] {
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
                String(it.amount).includes(searchText) ||
                String(it.new_bal).includes(searchText)  ||
                String(it.loggedin_bal_after).includes(searchText)
              
            );
        });
        this.myWalletService.searchUserData(arr);
        return arr;
    }
}
