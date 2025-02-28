import { Pipe, PipeTransform } from '@angular/core';
import { BillingList } from "src/app/billing-detail/Billing_helper/billing.model";
import {BillingSearchService  } from "src/app/billing-detail/billing-search.service";
@Pipe({
  name: 'billingSearch',
  pure:false
})
export class BillingSearchPipe implements PipeTransform {

  constructor(private b_searchService : BillingSearchService){

  }
  transform(items: BillingList[], searchText: string): BillingList[] {
    searchText = searchText.trim();
    if (!items) {
        return [];
    }
    if (!searchText) {
        return items;
    }
    searchText = searchText?.toLocaleLowerCase();
    const arr = items.filter((it) => {
      it.row_rate = it.row_rate + '';
      it.countries_customrate_total = it.countries_customrate_total + '';
     //console.log(it);
     
        return (
            it.user?.toLocaleLowerCase().includes(searchText) ||
            it.row_rate?.toLowerCase().includes(searchText)   ||
            it.usertype?.toLocaleLowerCase().includes(searchText) ||
            it.international?.toLocaleLowerCase().includes(searchText) ||
            it.billing_currency?.toLowerCase().includes(searchText) || 
            it.modified_ts?.toLowerCase().includes(searchText) ||
            it.countries_customrate_total?.toLowerCase().includes(searchText)

        );
    });
   // console.log(arr);
    
    this.b_searchService.searchBillingData(arr);
    return arr;
}
}
