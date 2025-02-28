import { Pipe, PipeTransform } from '@angular/core';
import { RCList } from "src/app/billing-detail/Billing_helper/ratechange.model";
import {RateChangeReportService  } from "src/app/billing-detail/Billing_helper/ratechange_report.service";
@Pipe({
  name: 'rateChangeSearch',
  pure:false
})
export class RateChangeSearchPipe implements PipeTransform {

  constructor(private b_searchService : RateChangeReportService){

  }
  transform(items: RCList[], searchText: string): RCList[] {
    searchText = searchText.trim();
    if (!items) {
        return [];
    }
    if (!searchText) {
        return items;
    }
    searchText = searchText.toLocaleLowerCase();
    const arr = items.filter((it) => {
      it.new_sms_rate = it.new_sms_rate + '';
      it.old_sms_rate = it.old_sms_rate + '';
        return (
          it.username?.toLocaleLowerCase().includes(searchText) ||
          it.country?.toLowerCase().includes(searchText)   ||
          it.new_sms_rate?.toLocaleLowerCase().includes(searchText) ||
          it.old_sms_rate?.toLocaleLowerCase().includes(searchText) ||
          it.billing_currency?.toLowerCase().includes(searchText) || 
          it.modified_ts?.toLowerCase().includes(searchText) ||
          it.billing_currency?.toLowerCase().includes(searchText) ||
          it.usertype?.toLocaleLowerCase().includes(searchText)
            // it.created_ts_unix.toLocaleLowerCase().includes(searchText) ||
            // it.t_lang.toLocaleLowerCase().includes(searchText)
        );
    });
    this.b_searchService.searchBillingData(arr);
    return arr;
}

}
