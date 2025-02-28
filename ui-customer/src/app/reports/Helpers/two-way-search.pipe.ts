import { Pipe, PipeTransform } from '@angular/core';
import { reportDetailModel } from '../reports-detailed/report.detail.model';

@Pipe({
  name: 'twoWaySearch'
})
export class TwoWaySearchPipe implements PipeTransform {

  transform(items, searchText: string, mode: string) {
   // console.log(searchText,mode);
    
    if (!items) {
      return [];
    }
    if (!searchText) {
      return items;
    }
    if (mode == 'ACK') {
      const arr = items.filter((it) => {
        return (
          it.ackId.includes(searchText)
        );
      });
      return arr;
    }
    else {
      const arr = items.filter((it) => {
        return (
          it.mobile.includes(searchText)
        );
      });
    //  console.log(arr)
      return arr;
    }

  }

}
