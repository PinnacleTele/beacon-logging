import { Pipe, PipeTransform } from "@angular/core";
import { WalletUsers } from "src/app/account/shared/model/wallet-users";
import { WalletuserService } from "./walletuser.service";

@Pipe({
    name: "filterUsers"
})
export class WalletUsersPipe implements PipeTransform {
    constructor(private walletService:WalletuserService){}
    transform(items: WalletUsers[], searchelement: string): WalletUsers[] {
        
        if (!items) {
            this.walletService.filterdItems=[];
            return [];
        }
        if (!searchelement) {
            this.walletService.filterdItems=items;
            return items;
        }
        searchelement = searchelement.toLocaleLowerCase();
        const arr = items.filter((it) => {
            
           return it.firstname.toLocaleLowerCase().includes(searchelement) ||
            it.lastname.toLocaleLowerCase().includes(searchelement) ||
            it.cli_id.toLocaleString().includes(searchelement) ||
           // it.wallet_bal.toLocaleLowerCase().includes(searchelement)  ||
            it.user.toLowerCase().includes(searchelement)

        });
        this.walletService.filterdItems=arr;
        return arr;
        // console.log("filter");
    }
}
